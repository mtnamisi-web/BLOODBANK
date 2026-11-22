package com.bloodbank.model;
import com.bloodbank.model.*;
import com.bloodbank.repository.*;
import com.bloodbank.service.*;
import com.bloodbank.security.*;
import com.bloodbank.websocket.*;
import com.bloodbank.controller.*;

import jakarta.persistence.*; import lombok.Data; import java.time.LocalDate;
@Data @Entity @Table(name="blood_inventory")
public class BloodBag {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long bagId;
  Long donorId; String bloodGroup, componentType, storageLocation, status="Available";
  Integer volumeMl=450; LocalDate collectionDate, expirationDate;
}
