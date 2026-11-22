package com.bloodbank.controller;
import com.bloodbank.model.*;
import com.bloodbank.repository.*;
import com.bloodbank.service.*;
import com.bloodbank.security.*;
import com.bloodbank.websocket.*;
import com.bloodbank.controller.*;

import org.springframework.http.ResponseEntity; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*;
import java.time.LocalDate; import java.util.*;
@RestController @RequestMapping("/api/appointments")
public class AppointmentController {
  final ApptRepo appts; final UserRepo users; AppointmentController(ApptRepo a, UserRepo u){ appts=a; users=u; }
  @PostMapping ResponseEntity<?> book(@RequestBody Appointment a, Authentication auth){
    Long uid = (Long) auth.getPrincipal(); var u = users.findById(uid).orElseThrow();
    if (!u.isEligible()) return ResponseEntity.status(422).body(Map.of("error","You can donate again on "+u.getLastDonationDate().plusDays(56)+" (56-day rule)."));
    if (a.getAppointmentDate()==null || a.getAppointmentDate().isBefore(java.time.LocalDateTime.now())) return ResponseEntity.badRequest().body(Map.of("error","Choose a future date and time."));
    if (a.getWeightKg()!=null && a.getWeightKg()<50) return ResponseEntity.status(422).body(Map.of("error","Enter your weight; donors must weigh at least 50 kg."));
    a.setDonorId(uid); a.setStatus("Scheduled"); return ResponseEntity.ok(appts.save(a));
  }
  @GetMapping("/all") List<Appointment> all(){ return appts.findAll(); }
  @GetMapping("/mine") List<Appointment> mine(Authentication auth){ return appts.findByDonorId((Long) auth.getPrincipal()); }
  @PutMapping("/{id}/complete") ResponseEntity<?> complete(@PathVariable Long id){   // staff marks donation done -> starts cooldown
    var a = appts.findById(id).orElseThrow(); a.setStatus("Completed"); appts.save(a);
    var u = users.findById(a.getDonorId()).orElseThrow(); u.setLastDonationDate(LocalDate.now()); users.save(u); return ResponseEntity.ok(a);
  }
}
