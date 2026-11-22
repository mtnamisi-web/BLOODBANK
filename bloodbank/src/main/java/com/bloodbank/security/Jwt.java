package com.bloodbank.security;
import com.bloodbank.model.*;
import com.bloodbank.repository.*;
import com.bloodbank.service.*;
import com.bloodbank.security.*;
import com.bloodbank.websocket.*;
import com.bloodbank.controller.*;

import io.jsonwebtoken.*; import io.jsonwebtoken.security.Keys; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Component;
import javax.crypto.SecretKey; import java.util.*;
@Component
public class Jwt {
  private final SecretKey key; private final long hours;
  Jwt(@Value("${app.jwt.secret}") String s, @Value("${app.jwt.hours}") long h){ key=Keys.hmacShaKeyFor(s.getBytes()); hours=h; }
  public String make(AppUser u){ return Jwts.builder().subject(u.getEmail()).claim("role",u.getRole()).claim("uid",u.getUserId())
    .expiration(new Date(System.currentTimeMillis()+hours*3600000)).signWith(key).compact(); }
  public Claims read(String t){ return Jwts.parser().verifyWith(key).build().parseSignedClaims(t).getPayload(); }
}
