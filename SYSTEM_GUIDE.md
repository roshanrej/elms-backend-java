# ELMS — System & Domain Guide

**Employee Leave Management System** — a full-stack learning project pairing **Angular 21** (`elms/`) with **Spring Boot 4 / Java 17** (`elms-backend-java/`).

This document explains the **domain**, **architecture**, **how requests flow end-to-end**, and **why** key decisions were made. Written for a first project — honest scope, not enterprise fluff.

---

## 1. What the system does

ELMS lets an organization:

1. **Admin** sets up structure: users, teams, departments, leave types, leave policies (quotas).
2. **Employee** applies for leave, saves drafts, submits requests, tracks status, views balance.
3. **Manager** reviews team leave requests — approve, reject, handle cancellation requests.

The **backend owns the rules**. The **frontend renders** what the backend allows.

---

## 2. Domain model (the “things” in the system)

### Core entities (Java JPA → MySQL tables)

| Entity | Purpose |
|--------|---------|
| `UserEntity` | People with role (ADMIN / MANAGER / EMPLOYEE), department, optional team |
| `TeamEntity` | Group of employees under one manager |
| `DepartmentEntity` | Org unit (HR, Engineering, …) |
| `RoleEntity` | ADMIN, MANAGER, EMPLOYEE |
| `LeaveTypeEntity` | CASUAL, ANNUAL, SICK, etc. |
| `LeavePolicyEntity` | Quota per leave type per year (allocated days, notice period) |
| `LeaveBalanceEntity` | Per-user remaining/consumed days for a policy |
| `LeaveRequestEntity` | A leave application (dates, reason, status) |
| `LeaveAuditLogEntity` | History of status transitions |
| `RefreshTokenEntity` | Long-lived token for session refresh |

### Leave request statuses

```
DRAFT → PENDING → APPROVED / REJECTED
                    ↓
              CANCEL_PENDING → CANCELLED (or back to APPROVED if cancel rejected)
```

Also: `DELETED` (draft removed), `CANCELLED`.

### The `allowedActions` pattern (important design choice)

Every leave row returned to the UI includes:

```json
{
  "id": 12,
  "status": "PENDING",
  "leaveType": "CASUAL",
  "allowedActions": ["CANCEL_REQUEST"]
}
```

`LeaveRequestWorkflowServiceImpl.allowedLeaveActions()` computes this from **role + status + ownership**. The UI action menu only shows those buttons. When clicked, `validateTransition()` checks the action is still allowed.

**Why:** The frontend never guesses permissions. Backend is the source of truth.

| Role | Status | Typical actions |
|------|--------|-----------------|
| Employee (owner) | DRAFT | EDIT_DRAFT, DELETE_DRAFT, SUBMIT_REQUEST |
| Employee (owner) | PENDING | CANCEL_REQUEST |
| Employee (owner) | APPROVED | REQUEST_CANCEL |
| Manager (owning team) | PENDING | APPROVE_REQUEST, REJECT_REQUEST |
| Manager (owning team) | CANCEL_PENDING | APPROVE_CANCEL, REJECT_CANCEL |

---

## 3. Startup seed data (minimal by design)

On first run, `SeedDataConfig` auto-creates **only if tables are empty**:

- Roles: ADMIN, MANAGER, EMPLOYEE
- Departments: HR, ENGINEERING, FINANCE, …
- Leave types: CASUAL, ANNUAL, SICK, MATERNITY, PATERNITY

**Not seeded:** users, teams, policies, balances, sample requests.

### Manual setup before first demo

```
1. POST /api/auth/register  → admin, manager, employee
2. Login as ADMIN
3. Create team, assign manager, assign employee to team
4. POST admin leave policy (creates balances for existing users)
5. Login as EMPLOYEE → submit leave
6. Login as MANAGER → approve
```

---

## 4. Backend architecture (Java / Spring Boot)

### Layering

```
Controller  →  Service  →  Repository  →  MySQL
     ↓              ↓
  ApiResponseDTO   Workflow / business rules
```

| Layer | Responsibility | Example |
|-------|----------------|---------|
| **Controller** | HTTP mapping, no business logic | `EmployeeLeaveRequestController` |
| **Service** | Rules, transactions, `@PreAuthorize` | `LeaveRequestServiceImpl` |
| **Repository** | JPA queries | `LeaveRequestRepository` |
| **Mapper** | Entity → DTO | `LeaveRequestMapper` |
| **Workflow** | Status transitions + allowed actions | `LeaveRequestWorkflowServiceImpl` |

### API response envelope

Every endpoint returns:

```json
{
  "success": true,
  "data": { ... },
  "message": "Leave request approved"
}
```

Built via `ResponseHandler.success()` / `ResponseHandler.failure()`. Errors throw `RuntimeException` → `GlobalExceptionHandler` → HTTP 400 with the same shape.

### Security

- **JWT** access token (short-lived) + **refresh token** (stored in DB)
- `JwtAuthenticationFilter` reads `Authorization: Bearer …` and sets Spring Security context
- `@PreAuthorize("hasRole('EMPLOYEE')")` on service methods enforces role per operation
- Auth routes (`/api/auth/**`) are public; everything else requires authentication

### Role-scoped API prefixes

| Prefix | Role |
|--------|------|
| `/api/auth/**` | Public (login, refresh, logout, me) |
| `admin/api/**` | ADMIN operations |
| `employee/api/**` | EMPLOYEE operations |
| `manager/api/**` | MANAGER operations |

---

## 5. The Hibernate lazy-loading bug (and fix)

### Error you saw

```
Could not initialize proxy [com.elms.elms_backend.entity.LeaveTypeEntity#1] - no session
```

### Why it happened

`LeaveRequestEntity.leaveType` is `@ManyToOne(fetch = FetchType.LAZY)`. Hibernate loads a **proxy** instead of the full `LeaveTypeEntity`. That proxy can only be used **while a database session is open** (inside a `@Transactional` method).

The read methods (`getEmployeeLeaveRequests`, `getEmployeeActiveLeaveRequests`, etc.) had **no `@Transactional`**. Flow was:

1. Repository query returns `LeaveRequestEntity` list → session closes
2. `.stream().map(mapper)` runs **outside** the session
3. Mapper calls `leaveRequest.getLeaveType().getName()` → **crash**

### Fix applied

1. **`@Transactional(readOnly = true)`** on all read service methods — keeps session open for the full map operation.
2. **`JOIN FETCH`** in repository queries — loads `leaveType` (and `employee` / `team` where needed) in the same query so mapping is safe and efficient.
3. **`findByIdWithDetails`** for single-request lookups used in approve/reject/cancel.

**Lesson:** In JPA, lazy associations + mapping outside a transaction = classic first-project bug. Fetch joins or `@Transactional` on the service method fixes it.

---

## 6. Frontend architecture (Angular)

### Structure

```
elms/src/app/
├── auth/           Login, guards, interceptors, session store
├── core/           DTOs, enums, HTTP API clients, interceptors
├── features/
│   ├── admin/      Users, teams, departments, leave types, policies
│   ├── employee/   Dashboard, apply leave, my leaves, drafts, balance
│   └── manager/    Dashboard, team leaves
└── shared/         Sidebar, action menus, badges, confirm dialog, navigation config
```

### Request flow (example: employee opens “My Leaves”)

```
1. Route: /employee/leaves
2. Resolver: employeeLeaveRequestsResolver
      → LeaveService.getEmployeeLeaveRequests()
      → EmployeeLeaveRequestApi GET employee/api/leave-requests/me
3. Page ngOnInit: reads route.snapshot.data['leaves']
4. Table renders rows; action menu reads leave.allowedActions
5. User clicks Approve → confirm dialog → LeaveService.submit…()
6. API returns updated projection → page patches local array
```

**Resolvers** preload data before the page paints — no loading flicker on first visit.

### Layering (mirrors backend)

```
Page/Component
  → Feature Service (LeaveService, ManagerLeaveService)
    → HTTP API (EmployeeLeaveRequestApi)
      → unwrapApiResponse()  // checks success, shows toast on error
        → NotificationService
```

### HTTP interceptors (order matters)

1. **jwtInterceptor** — attaches `Authorization: Bearer <accessToken>`
2. **apiErrorInterceptor** — shows user-friendly toasts; skips silent/background errors
3. **tokenRefreshInterceptor** — on 401/403, refreshes token and retries; logout if refresh fails

### Auth session

- Login stores `accessToken` + `refreshToken` in `localStorage`
- `AuthStore` holds current user (name, email, role, department)
- `authGuard` + `roleGuard` protect routes (`/admin`, `/employee`, `/manager`)
- App initializer calls `restoreSession()` on startup

### UI patterns

- **`app-page-header`** — consistent page titles
- **`app-leave-status-badge`** — status chips
- **`app-leave-action-menu`** — dropdown driven by `allowedActions`
- **`app-confirm-dialog`** — confirm before destructive API calls
- **Navigation** — `shared/config/navigation.config.ts` (single source for sidebar + dashboards)

### Action menus

Menus teleport their panel to `document.body` (fixes table overflow clipping). Parent pages track `activeMenuRowId` so only one menu is open at a time.

---

## 7. End-to-end leave lifecycle

```
┌─────────┐     create policy      ┌──────────────┐
│  ADMIN  │ ─────────────────────► │ LeavePolicy  │
└─────────┘     assign team        │ + Balances   │
     │                               └──────────────┘
     ▼
┌─────────┐   POST /draft or /submit   ┌─────────────┐
│EMPLOYEE │ ─────────────────────────► │ LeaveRequest │
└─────────┘                           │  (PENDING)   │
                                        └──────┬──────┘
                                               │
┌─────────┐   POST /{id}/approve             │
│ MANAGER │ ◄────────────────────────────────┘
└─────────┘
     │
     ▼
  APPROVED → balance deducted
  Employee may REQUEST_CANCEL → CANCEL_PENDING → manager APPROVE_CANCEL → CANCELLED
```

---

## 8. Key files to study

### Backend

| File | Why read it |
|------|-------------|
| `LeaveRequestWorkflowServiceImpl.java` | allowedActions + validation |
| `LeaveRequestServiceImpl.java` | Full leave lifecycle |
| `LeaveRequestRepository.java` | Queries with JOIN FETCH |
| `SecurityConfig.java` + `JwtAuthenticationFilter.java` | Auth |
| `SeedDataConfig.java` | What auto-seeds |
| `GlobalExceptionHandler.java` | Error → ApiResponseDTO |
| `ResponseHandler.java` | Success/failure wrapper |

### Frontend

| File | Why read it |
|------|-------------|
| `employee.routes.ts` | Routes + resolvers |
| `leave-request.service.ts` | Employee API orchestration |
| `employee-leave-request-api.ts` | Raw HTTP paths |
| `api-response.utils.ts` | unwrapApiResponse |
| `leave-action-menu.ts` | Action dropdown |
| `navigation.config.ts` | Role-based nav |
| `token-refresh-interceptor.ts` | Session recovery |

---

## 9. What we deliberately kept minimal

- No analytics module (removed — was UI shell only)
- No admin “all leave requests” oversight page
- No `GET /leave-requests/{id}` — lists + patch-after-mutation is enough
- Register is a dev stub (`dummyRegister`)
- Seed is reference data only — org setup is manual (shows you understand the flow)
- Single role per user (not multi-role)

These are valid scope choices for a first project presentation.

---

## 10. Demo script (5 minutes)

1. Start MySQL, backend (`elms-backend-java`), frontend (`ng serve` in `elms/`)
2. **Admin:** login → Teams → create team → assign manager → Leave Policies → create CASUAL 2026 policy
3. **Employee:** login → Apply Leave → submit → My Leaves (see PENDING + action menu)
4. **Manager:** login → Team Leaves → approve
5. **Employee:** request cancellation on approved leave → **Manager:** approve cancel

Talking points: “Backend sends `allowedActions`, UI renders from that. Resolvers preload data. JWT + refresh handles sessions.”

---

## 11. Tech stack summary

| | Backend | Frontend |
|---|---------|----------|
| Language | Java 17 | TypeScript |
| Framework | Spring Boot 4 | Angular 21 (standalone components) |
| Database | MySQL + JPA/Hibernate | — |
| Auth | JWT + refresh tokens | localStorage + interceptors |
| UI | — | Bootstrap 5 + Bootstrap Icons |
| API format | `ApiResponseDTO<T>` | Matching TypeScript DTOs |

---

## 12. Technology concepts that emerged (and what they mean)

These are the ideas you actually used — not textbook definitions, but how they showed up in ELMS.

### A. Domain-driven thinking

**Concept:** Model the real-world problem first (leave, teams, policies), then build code around it.

**In ELMS:** `LeaveRequestStatusEnum`, `LeaveRequestActionEnum`, and the workflow service mirror how HR actually thinks: draft → submit → approve → maybe cancel.

**How to write it in future projects:**
- Start with statuses and who can do what — on paper, before controllers.
- Name enums after business language (`PENDING`, not `STATE_2`).
- Put transition rules in one place (`LeaveRequestWorkflowService`), not scattered in controllers.

**How to explain it:**
> “I separated *what a leave request is* from *what you’re allowed to do to it*. The workflow service is the rulebook; controllers just receive HTTP calls.”

---

### B. Layered architecture (separation of concerns)

**Concept:** Each layer has one job. Upper layers don’t talk to the database directly.

**In ELMS:**
```
Controller → Service → Repository → DB
              ↓
           Mapper (Entity → DTO)
```

**How to write it in future projects:**
- **Controller:** `@GetMapping`, call service, return `ResponseHandler.success(...)`. No `if (balance < 0)` here.
- **Service:** `@Transactional`, business rules, call repos.
- **Repository:** Queries only.
- **DTO:** What leaves the API — never expose `UserEntity` with password hash.

**How to explain it:**
> “If I need to change how approval works, I touch the service. If I need a new API field, I touch the DTO and mapper. The database structure can stay stable.”

---

### C. API envelope pattern (`ApiResponseDTO`)

**Concept:** Every response has the same shape so the frontend always knows where to look.

**In ELMS:**
```json
{ "success": true, "data": { ... }, "message": "..." }
```

**How to write it in future projects:**
- One wrapper class on backend; one `unwrapApiResponse()` on frontend.
- Never return raw entities or mixed error formats.
- Put human-readable text in `message` for toasts.

**How to explain it:**
> “The frontend doesn’t guess whether a call worked. It checks `success`, reads `data`, and shows `message` on failure. Same contract on every endpoint.”

---

### D. Workflow / state machine

**Concept:** An object moves through defined states; only certain actions are legal per state.

**In ELMS:** You can’t approve a `DRAFT`. You can’t cancel a `REJECTED` request. `validateTransition()` enforces this.

**How to write it in future projects:**
1. List all statuses.
2. List all actions.
3. Build a table: (role, status) → allowed actions.
4. On every mutation: check current status + action before changing.

**How to explain it:**
> “It’s like a ticket system — a closed ticket can’t be reopened by the same button. The backend checks the state before every action, and the UI only shows buttons the backend listed in `allowedActions`.”

---

### E. Capability-based UI (`allowedActions`)

**Concept:** Don’t hardcode “show Approve button if status is PENDING” all over the frontend. Ask the server what’s allowed *for this row*.

**In ELMS:** `LeaveRequestProjectionDTO.allowedActions` drives `app-leave-action-menu`.

**How to write it in future projects:**
- Backend computes permissions per record.
- Frontend: `*ngFor="let action of leave.allowedActions"`.
- After mutation, replace the row with the API response (new status + new actions).

**How to explain it:**
> “The UI doesn’t own permission logic. It renders a menu from a list the API gives back. If rules change tomorrow, I update the workflow service — not twenty Angular templates.”

---

### F. JPA / ORM + lazy loading

**Concept:** Hibernate maps Java classes to tables. `LAZY` associations load on demand — but only inside an open session.

**In ELMS:** `leaveType` on `LeaveRequestEntity` is lazy. Mapping outside `@Transactional` caused the proxy error.

**How to write it in future projects:**
- Put `@Transactional(readOnly = true)` on service methods that read + map to DTOs.
- Use `JOIN FETCH` in queries when you know you’ll need related entities.
- Prefer DTOs at the API boundary — don’t leak entities.

**How to explain it:**
> “Hibernate gives you a placeholder for leave type until you actually read it. That read must happen while the database connection is still open — inside a transactional service method. We fixed it with fetch joins and read-only transactions.”

---

### G. JWT + refresh token auth

**Concept:** Short-lived access token for API calls; long-lived refresh token to get a new access token without re-login.

**In ELMS:**
- Login → both tokens stored in `localStorage`
- `jwtInterceptor` attaches access token
- `tokenRefreshInterceptor` catches 401/403, refreshes, retries
- Refresh token row in DB; logout deletes it

**How to write it in future projects:**
1. Access token: 15–60 min, stateless JWT.
2. Refresh token: days/weeks, stored server-side, rotatable.
3. Never put refresh token only in memory if you want “stay logged in” across refresh.
4. Interceptor chain: attach token → handle errors → refresh on expiry.

**How to explain it:**
> “The access token is like a day pass — it expires quickly. The refresh token is stored in the database so I can revoke sessions on logout. When the pass expires, the app quietly gets a new one instead of kicking the user to login.”

---

### H. Role-based access control (RBAC)

**Concept:** Users have roles; operations check roles before running.

**In ELMS:**
- Spring: `@PreAuthorize("hasRole('MANAGER')")` on service methods
- Angular: `roleGuard` on `/admin`, `/employee`, `/manager` routes
- API prefixes split by role (`employee/api/...`, `manager/api/...`)

**How to write it in future projects:**
- Enforce on **backend** always — frontend guards are UX only.
- One role per user keeps first projects simple.
- Match route prefix to role so mistakes are obvious.

**How to explain it:**
> “Guards stop users from seeing the wrong screens. `@PreAuthorize` stops them from calling APIs even if they craft requests manually. Defense in depth — UI convenience plus server enforcement.”

---

### I. Angular resolvers

**Concept:** Load route data *before* the component renders.

**In ELMS:** `employeeLeaveRequestsResolver` fetches leaves; `MyLeavesPage` reads `route.snapshot.data['leaves']`.

**How to write it in future projects:**
```typescript
export const myResolver: ResolveFn<Item[]> = () =>
  inject(MyService).load().pipe(catchError(() => of([])));
```
```typescript
{ path: 'leaves', component: MyPage, resolve: { items: myResolver } }
```

**How to explain it:**
> “Instead of the page loading empty and then fetching, the router waits for data first. The user sees a complete table on first paint.”

---

### J. HTTP interceptors (cross-cutting concerns)

**Concept:** Logic that runs on every HTTP request/response without repeating it in each service.

**In ELMS:** JWT attachment, error toasts, token refresh — three interceptors in order.

**How to write it in future projects:**
- One interceptor per concern.
- Order: attach auth → handle response errors → refresh token (innermost touches network first on errors).
- Use `HttpContext` flags for silent background requests (e.g. session restore).

**How to explain it:**
> “I didn’t paste Authorization headers in fifty API files. One interceptor does it. Same for refresh — every call benefits automatically.”

---

### K. Reactive streams (RxJS)

**Concept:** APIs return `Observable<T>` — async pipelines you compose with `map`, `catchError`, `switchMap`.

**In ELMS:** `leaveService.submit(id).pipe(map(unwrapApiResponse))` → component uses `firstValueFrom` or `subscribe`.

**How to write it in future projects:**
- HTTP client returns Observables — don’t fight it.
- Unwrap API envelope in one `map` step.
- `catchError(() => of(fallback))` in resolvers so navigation doesn’t break.

**How to explain it:**
> “Each API call is a stream. I chain steps: call HTTP, unwrap the envelope, handle errors. Components stay thin — they await the final result.”

---

### L. Standalone components (modern Angular)

**Concept:** No NgModule required per feature. Components declare their own `imports: [...]`.

**In ELMS:** Every page is `standalone: true` with direct imports of `CommonModule`, `RouterLink`, shared components.

**How to write it in future projects:**
- New component: `ng g c features/foo/pages/bar --standalone`
- Import only what the template uses.
- Lazy routes: `loadChildren: () => import('./admin.routes')`

**How to explain it:**
> “I use Angular’s current style — standalone components and lazy-loaded route files per role. Smaller bundles, clearer dependencies.”

---

## 13. How to build the next feature (repeatable recipe)

When you add something new to ELMS or start a similar project:

### Backend checklist

1. **Domain:** What entity? What status changes? Who’s allowed?
2. **DTO:** Request body + response projection (include `allowedActions` if it’s interactive).
3. **Repository:** Query with `JOIN FETCH` for associations you’ll map.
4. **Service:** `@Transactional`, workflow validation, `@PreAuthorize`.
5. **Controller:** One endpoint, `ResponseHandler.success/failure`.
6. **Test manually:** Postman or frontend — happy path + illegal transition.

### Frontend checklist

1. **DTO + enum** in `core/dtos` / `core/types-enums` (mirror backend).
2. **HTTP API** method in `core/http/...`.
3. **Feature service** with `unwrapApiResponse`.
4. **Resolver** (if it’s a list/detail page).
5. **Route** in `employee.routes.ts` (or admin/manager).
6. **Page** reads `snapshot.data`, renders table/form.
7. **Action menu** binds to `allowedActions` if applicable.
8. **Nav entry** in `navigation.config.ts`.

### Naming habit that scales

| Thing | Pattern |
|-------|---------|
| Entity | `LeaveRequestEntity` |
| API response | `LeaveRequestProjectionDTO` |
| Angular DTO | `leave-request.projection.dto.ts` |
| Service | `LeaveRequestService` / `LeaveService` |
| API client | `EmployeeLeaveRequestApi` |
| Resolver | `employeeLeaveRequestsResolver` |

Keep names aligned across stack — you’ll find files faster and explain the system cleaner.

---

## 14. How to explain the system (presentation phrases)

Use these when demoing or answering questions. Sound like you understand the flow, not memorizing buzzwords.

### Opening (30 seconds)

> “ELMS is an employee leave management app. Admin sets up teams and policies. Employees submit leave requests. Managers approve or reject. The backend owns all business rules; Angular displays what the API returns, including which actions are allowed per row.”

### Architecture (if asked “how is it structured?”)

> “It’s a classic three-tier setup: Angular SPA talks REST to Spring Boot, which uses JPA to MySQL. I split APIs by role — employee, manager, admin — and use JWT with refresh tokens for auth.”

### Why `allowedActions`?

> “Instead of the UI guessing ‘can I show Approve?’, the server sends an explicit list per leave request. Buttons come from that list. When the user clicks, the server validates again. Single source of truth.”

### Why resolvers?

> “Route resolvers fetch data before the page opens. My Leaves loads with the table already populated — better UX than spinner-then-fill.”

### Why `ApiResponseDTO`?

> “Every endpoint returns the same JSON shape. The frontend has one unwrap function and one error handling path. Predictable for debugging and demos.”

### The lazy-loading bug (if asked about challenges)

> “I hit a Hibernate lazy-loading error — accessing leave type after the DB session closed. I fixed it with read-only transactions on read services and join-fetch in queries. Common first JPA issue; now I know to map inside the transaction or fetch upfront.”

### Auth (if asked)

> “Login returns access and refresh tokens. The access token goes on every request. When it expires, an interceptor uses the refresh token to get a new one. Logout deletes the refresh token server-side so that session can’t be revived.”

### What you’d improve next (shows maturity)

> “I’d add integration tests for the workflow service, a proper register flow, and maybe OpenAPI docs. I kept scope minimal so the core leave lifecycle is solid end-to-end.”

### One-sentence summaries per technology

| Technology | One-liner |
|------------|-----------|
| Spring Boot | “Runs the API and wires security, JPA, and controllers.” |
| JPA/Hibernate | “Maps Java objects to SQL tables so I work with objects, not raw JDBC.” |
| `@Transactional` | “Wraps a unit of work in one DB session — needed for lazy loading and atomic updates.” |
| Angular | “SPA that routes by role and calls the API through typed services.” |
| RxJS | “Handles async API calls as composable pipelines.” |
| JWT | “Signed token proving who you are without server session state per request.” |
| Bootstrap | “Layout and components so I focused on flow, not custom CSS from scratch.” |

---

## 15. Mental model: one request’s journey

Use this diagram verbally in a presentation:

```
User clicks "Submit" in Angular
        │
        ▼
LeaveService.submitExistingLeaveRequest(id)
        │
        ▼
jwtInterceptor adds Bearer token
        │
        ▼
POST employee/api/leave-requests/{id}/submit
        │
        ▼
JwtAuthenticationFilter → SecurityContext (EMPLOYEE)
        │
        ▼
LeaveRequestServiceImpl.submitLeaveRequest()
   @Transactional opens DB session
   validateTransition(SUBMIT_REQUEST)
   check balance / policy
   save entity + audit log
   mapWithActions → DTO with new status + allowedActions
        │
        ▼
ResponseHandler.success(dto, "Leave submitted")
        │
        ▼
unwrapApiResponse in Angular → update row in table
Toast: success
Action menu re-renders from new allowedActions
```

> “Every click is this path. User action → authenticated HTTP → transactional business logic → consistent JSON → UI patch. That’s the whole system in one sentence.”

---

*This is your system. You built a workflow-driven leave app with clear role separation, backend-owned rules, and a resolver-based Angular frontend. Sections 12–15 are your cheat sheet for the next project and for explaining what you built — in interviews, presentations, or your own notes.*