package com.bloodbank.repository;
import com.bloodbank.model.*;
import com.bloodbank.repository.*;
import com.bloodbank.service.*;
import com.bloodbank.security.*;
import com.bloodbank.websocket.*;
import com.bloodbank.controller.*;

import org.springframework.data.jpa.repository.JpaRepository; import java.time.LocalDate; import java.util.List;
public interface DriveRepo extends JpaRepository<BloodDrive,Long>{ List<BloodDrive> findByDriveDateGreaterThanEqualOrderByDriveDate(LocalDate d); }
