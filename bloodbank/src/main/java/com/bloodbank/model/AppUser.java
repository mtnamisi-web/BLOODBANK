package com.bloodbank.model;
import com.bloodbank.model.*;
import com.bloodbank.repository.*;
import com.bloodbank.service.*;
import com.bloodbank.security.*;
import com.bloodbank.websocket.*;
import com.bloodbank.controller.*;

import jakarta.persistence.*; import lombok.Data; import java.time.LocalDate;
@Data @Entity @Table(name="users")
public class AppUser {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long userId;
  String fullName; @Column(unique=true) String email; @com.fasterxml.jackson.annotation.JsonIgnore String passwordHash;
  String phone, role, bloodGroup, city; LocalDate dateOfBirth, lastDonationDate;
  public boolean isEligible(){ return lastDonationDate==null || lastDonationDate.plusDays(56).compareTo(LocalDate.now())<=0; }
}
