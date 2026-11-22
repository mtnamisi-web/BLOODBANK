package com.bloodbank.security;
import com.bloodbank.model.*;
import com.bloodbank.repository.*;
import com.bloodbank.service.*;
import com.bloodbank.security.*;
import com.bloodbank.websocket.*;
import com.bloodbank.controller.*;

import jakarta.servlet.*; import jakarta.servlet.http.*; import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod; import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity; import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority; import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain; import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter; import java.io.IOException; import java.util.List;
@Configuration
public class SecurityConfig {
  @Bean PasswordEncoder enc(){ return new BCryptPasswordEncoder(); }
  @Bean SecurityFilterChain chain(HttpSecurity http, Jwt jwt) throws Exception {
    OncePerRequestFilter f = new OncePerRequestFilter(){
      protected void doFilterInternal(HttpServletRequest rq, HttpServletResponse rs, FilterChain c) throws ServletException, IOException {
        String h = rq.getHeader("Authorization");
        if (h!=null && h.startsWith("Bearer ")) try {
          var cl = jwt.read(h.substring(7));
          var a = new UsernamePasswordAuthenticationToken(((Number)cl.get("uid")).longValue(), null, List.of(new SimpleGrantedAuthority("ROLE_"+cl.get("role").toString().toUpperCase())));
          SecurityContextHolder.getContext().setAuthentication(a);
        } catch (Exception ignored) {}
        c.doFilter(rq, rs);
      } };
    return http.csrf(x->x.disable()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(a->a.requestMatchers("/","/index.html","/api/auth/**").permitAll()
        .requestMatchers("/ws").permitAll().requestMatchers(HttpMethod.GET,"/api/inventory/search","/api/inventory/summary","/api/drives").permitAll()
        .requestMatchers("/api/admin/**","/api/appointments/*/complete","/api/appointments/all").hasRole("ADMIN")
        .requestMatchers("/api/requests").hasAnyRole("HOSPITAL","ADMIN")
        .requestMatchers("/api/appointments").hasAnyRole("DONOR","ADMIN")
        .anyRequest().authenticated())
      .addFilterBefore(f, UsernamePasswordAuthenticationFilter.class).build();
  }
}
