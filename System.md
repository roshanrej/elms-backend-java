&#x20;          

|**USERS**|**ROLES**|**LEAVE\_TYPE**|**LEAVE\_POLICY**|**LEAVE\_REQUESTS**|**LEAVE\_COMMENTS**|**DEPARTMENT**|**LEAVE\_AUDIT\_LOG**|
|-|-|-|-|-|-|-|-|
|id|id|id|id|id|id|id|id|
|name|name|name|year|user\_id|leave\_id|name|leave\_id|
|email||status|leave\_type\_id|leave\_type\_id|user\_id|status|action|
|password\_hash|||leave\_type\_name|startDate|user\_role|createdAt|user\_id|
|createdAt|||allocated\_leave|endDate|message|updatedAt|user\_role|
|role\_id||||reason|createdAt||timestamp|
|status||||createdAt||||
|dept\_id||||submittedAt||||
|updatedAt||||decisionAt||||
|||||approver\_id||||
|||||status||||
|||||||||







CONSTRAINTS
-----------
---



**USERS
-----**

'email' - has to be unique.

'status' - INACTIVE | ACTIVE determines if the user is valid

'role\_id' - determines the permissions inside the system


**ROLES
-----**

'name' - has to be unique : (permissions are now enforced by business logic) (ADMIN, MANAGER, EMPLOYEE)



**LEAVE\_TYPE
----------**

'name' - has to be unique
'status' - INACTIVE | ACTIVE determines if the type is valid 
 

**LEAVE\_POLICY
------------**

'year' \&\& 'leave\_type\_id' - (HAS TO BE UNIQUE TOGETHER)

'allocated\_leave' - (question of editable or non-editable)



**LEAVE\_REQUESTS
--------------**

'status' - DRAFT → PENDING

PENDING → APPROVED | REJECTED | CANCELLED

APPROVED → CANCEL\_REQUESTED

CANCEL\_REQUESTED → CANCELLED | APPROVED

REJECTED → terminal

CANCELLED → terminal



'createdAt' - Record has to exist with populated date (NOT NULL)

'submittedAt' - Has to have data if status is anything other than Draft

'decisionAt' - 'LATEST ACTION RESULTING IN THE CURRENT STATE'

'approver\_id -'Latest actor'

'approver\_name' - 'Latest actor name'


**LEAVE\_COMMENTS
--------------**

'user\_id' - entity must be the one who requested or the reporting manager - BUSINESS DECISIONS MIGHT CHANGE STAY FLEXIBLE



**DEPARTMENT**

**----------**

'status' - ACTIVE | INACTIVE
'name' - unique 



**LEAVE\_AUDIT\_LOG
---------------**

ACTION AUDIT LOG



