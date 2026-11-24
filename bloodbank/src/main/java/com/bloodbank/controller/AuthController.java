package com.bloodbank.controller;
import com.bloodbank.model.*;
import com.bloodbank.repository.*;
import com.bloodbank.service.*;
import com.bloodbank.security.*;
import com.bloodbank.websocket.*;
import com.bloodbank.controller.*;

import org.springframework.http.*; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import java.util.*;
@RestController @RequestMapping("/api/auth")
public class AuthController {
  final UserRepo users; final PasswordEncoder enc; final Jwt jwt;
  @Value("${app.password-reset.word:happy}") String resetWord;
  AuthController(UserRepo u, PasswordEncoder e, Jwt j){ users=u; enc=e; jwt=j; }
  record Reg(String fullName,String email,String password,String phone,String role,String bloodGroup,String city,java.time.LocalDate dateOfBirth){}
  record Login(String email,String password){}
  record Reset(String email,String resetWord,String newPassword){}
  @PostMapping("/register") ResponseEntity<?> register(@RequestBody Reg r){
    if (r.email()==null || r.password()==null || r.password().length()<8) return ResponseEntity.badRequest().body(Map.of("error","Email and a password of 8+ characters are required."));
    if (users.findByEmail(r.email()).isPresent()) return ResponseEntity.status(409).body(Map.of("error","That email is already registered."));
    String role = "hospital".equals(r.role()) ? "hospital" : "donor";   // admins are created directly in the database
    AppUser u = new AppUser(); u.setFullName(r.fullName()); u.setEmail(r.email()); u.setPasswordHash(enc.encode(r.password()));
    u.setPhone(r.phone()); u.setRole(role); u.setBloodGroup(r.bloodGroup()); u.setCity(r.city()); u.setDateOfBirth(r.dateOfBirth());
    users.save(u); return ResponseEntity.ok(Map.of("token",jwt.make(u),"role",role,"name",u.getFullName()));
  }
  @PostMapping("/login") ResponseEntity<?> login(@RequestBody Login l){
    var u = users.findByEmail(l.email());
    if (u.isEmpty() || !enc.matches(l.password(),u.get().getPasswordHash())) return ResponseEntity.status(401).body(Map.of("error","Wrong email or password."));
    return ResponseEntity.ok(Map.of("token",jwt.make(u.get()),"role",u.get().getRole(),"name",u.get().getFullName()));
  }
  @PostMapping("/forgot-password") ResponseEntity<?> forgotPassword(@RequestBody Reset r){
    if (r.email()==null || r.newPassword()==null || r.newPassword().length()<8)
      return ResponseEntity.badRequest().body(Map.of("error","Email and a new password of 8+ characters are required."));
    if (resetWord==null || !resetWord.equalsIgnoreCase(r.resetWord()==null?"":r.resetWord().trim()))
      return ResponseEntity.status(401).body(Map.of("error","Incorrect reset word."));
    var u = users.findByEmail(r.email());
    if (u.isEmpty()) return ResponseEntity.status(404).body(Map.of("error","No account found with that email."));
    var account = u.get();
    account.setPasswordHash(enc.encode(r.newPassword()));
    users.save(account);
    return ResponseEntity.ok(Map.of("message","Password reset. You can now sign in."));
  }
}
