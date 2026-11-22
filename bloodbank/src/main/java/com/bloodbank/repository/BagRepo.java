package com.bloodbank.repository;
import com.bloodbank.model.*;
import com.bloodbank.repository.*;
import com.bloodbank.service.*;
import com.bloodbank.security.*;
import com.bloodbank.websocket.*;
import com.bloodbank.controller.*;

import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional; import java.time.LocalDate; import java.util.List;
public interface BagRepo extends JpaRepository<BloodBag,Long>{
  @Modifying @Transactional @Query("update BloodBag b set b.status='Expired' where b.status='Available' and b.expirationDate < :d")
  int expire(@Param("d") LocalDate d);
  List<BloodBag> findByStatus(String status);
  List<BloodBag> findByStatusAndBloodGroupIn(String status, List<String> groups);
}
