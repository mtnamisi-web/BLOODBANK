package com.bloodbank.model;
import com.bloodbank.model.*;
import com.bloodbank.repository.*;
import com.bloodbank.service.*;
import com.bloodbank.security.*;
import com.bloodbank.websocket.*;
import com.bloodbank.controller.*;

import jakarta.persistence.*; import lombok.Data; import java.time.LocalDateTime;
@Data @Entity @Table(name="appointments")
public class Appointment {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long appointmentId;
  Long donorId; String centerName, status="Scheduled"; LocalDateTime appointmentDate;
  Double weightKg, hemoglobinLevel;
}
