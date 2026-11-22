package com.bloodbank.repository;
import com.bloodbank.model.*;
import com.bloodbank.repository.*;
import com.bloodbank.service.*;
import com.bloodbank.security.*;
import com.bloodbank.websocket.*;
import com.bloodbank.controller.*;

import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface ApptRepo extends JpaRepository<Appointment,Long>{ List<Appointment> findByDonorId(Long id); }
