package com.bloodbank.controller;
import com.bloodbank.model.*;
import com.bloodbank.repository.*;
import com.bloodbank.service.*;
import com.bloodbank.security.*;
import com.bloodbank.websocket.*;
import com.bloodbank.controller.*;

import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController @RequestMapping("/api")
public class RequestController {
  final RequestRepo reqs; final Broadcaster ws; RequestController(RequestRepo r, Broadcaster w){ reqs=r; ws=w; }
  @PostMapping("/requests") BloodRequest create(@RequestBody BloodRequest r, Authentication auth){
    r.setRequestId(null); r.setRequesterId((Long) auth.getPrincipal()); r.setStatus("Pending"); r.setRequestedAt(java.time.LocalDateTime.now());
    var s = reqs.save(r);
    ws.send("{\"type\":\"request\",\"urgency\":\""+esc(s.getUrgency())+"\",\"bloodGroup\":\""+esc(s.getBloodGroup())+"\",\"units\":"+(s.getUnitsNeeded()==null?0:s.getUnitsNeeded())+"}");
    return s; }
  @GetMapping("/requests") List<BloodRequest> mine(Authentication auth){ return reqs.findByRequesterId((Long) auth.getPrincipal()); }
  @GetMapping("/admin/requests") List<BloodRequest> all(){ return reqs.findAll(); }
  @PutMapping("/admin/requests/{id}") BloodRequest set(@PathVariable Long id, @RequestParam String status){
    var r = reqs.findById(id).orElseThrow(); r.setStatus(status); var s = reqs.save(r); ws.send("{\"type\":\"requestUpdate\"}"); return s; }
  static String esc(String s){ return s==null ? "" : s.replaceAll("[^A-Za-z0-9+\\- ]",""); }
}
