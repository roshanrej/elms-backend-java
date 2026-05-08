package com.elms.elms_backend.config;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.jaas.memory.InMemoryConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity

public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity){
      return httpSecurity
              .csrf(customizer -> customizer.disable())
              .authorizeHttpRequests(request -> request.requestMatchers("/api/auth/**").permitAll().anyRequest().authenticated())
              .build();

    }


//    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService){
//
//    }

}
