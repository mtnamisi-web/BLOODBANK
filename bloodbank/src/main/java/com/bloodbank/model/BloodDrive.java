package com.bloodbank.model;
import com.bloodbank.model.*;
import com.bloodbank.repository.*;
import com.bloodbank.service.*;
import com.bloodbank.security.*;
import com.bloodbank.websocket.*;
import com.bloodbank.controller.*;

import jakarta.persistence.*; import lombok.Data; import java.time.*;
@Data @Entity @Table(name="blood_drives")
public class BloodDrive {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long driveId;
  String title, venueAddress, city, contactPhone; LocalDate driveDate; LocalTime startTime, endTime;
}
