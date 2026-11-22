package com.bloodbank.model;
import com.bloodbank.model.*;
import com.bloodbank.repository.*;
import com.bloodbank.service.*;
import com.bloodbank.security.*;
import com.bloodbank.websocket.*;
import com.bloodbank.controller.*;

import jakarta.persistence.*; import lombok.Data; import java.time.LocalDateTime;
@Data @Entity @Table(name="blood_requests")
public class BloodRequest {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long requestId;
  Long requesterId; String patientName, hospitalName, hospitalCity, bloodGroup, componentType, urgency="Normal", status="Pending";
  Integer unitsNeeded; LocalDateTime requestedAt=LocalDateTime.now();
}
