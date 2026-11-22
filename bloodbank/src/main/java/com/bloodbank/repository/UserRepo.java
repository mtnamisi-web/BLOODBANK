package com.bloodbank.repository;
import com.bloodbank.model.*;
import com.bloodbank.repository.*;
import com.bloodbank.service.*;
import com.bloodbank.security.*;
import com.bloodbank.websocket.*;
import com.bloodbank.controller.*;

import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface UserRepo extends JpaRepository<AppUser,Long>{ Optional<AppUser> findByEmail(String e); }
