package com.bloodbank.controller;
import com.bloodbank.model.*;
import com.bloodbank.repository.*;
import com.bloodbank.service.*;
import com.bloodbank.security.*;
import com.bloodbank.websocket.*;
import com.bloodbank.controller.*;

import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api")
public class InventoryController {
  final InventoryService svc; InventoryController(InventoryService s){ svc=s; }
  @GetMapping("/inventory/summary") Map<String,Long> summary(){ return svc.summary(); }
  @GetMapping("/inventory/search") ResponseEntity<?> search(@RequestParam("blood_group") String g){
    var r = svc.search(g); return r==null ? ResponseEntity.badRequest().body(Map.of("error","Unknown blood group.")) : ResponseEntity.ok(r); }
  @PostMapping("/admin/inventory") BloodBag add(@RequestBody BloodBag b){ return svc.add(b); }
  @GetMapping("/admin/inventory") List<BloodBag> all(){ return svc.all(); }
  @PutMapping("/admin/inventory/{id}") BloodBag status(@PathVariable Long id, @RequestParam String status){ return svc.setStatus(id,status); }
}
