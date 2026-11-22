package com.bloodbank.controller;
import com.bloodbank.model.*;
import com.bloodbank.repository.*;
import com.bloodbank.service.*;
import com.bloodbank.security.*;
import com.bloodbank.websocket.*;
import com.bloodbank.controller.*;

import org.springframework.web.bind.annotation.*; import java.time.LocalDate; import java.util.List;
@RestController @RequestMapping("/api")
public class DriveController {
  final DriveRepo drives; DriveController(DriveRepo d){ drives=d; }
  @GetMapping("/drives") List<BloodDrive> upcoming(){ return drives.findByDriveDateGreaterThanEqualOrderByDriveDate(LocalDate.now()); }
  @PostMapping("/admin/drives") BloodDrive add(@RequestBody BloodDrive d){ d.setDriveId(null); return drives.save(d); }
}
