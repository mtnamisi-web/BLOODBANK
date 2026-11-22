package com.bloodbank.service;
import com.bloodbank.model.*;
import com.bloodbank.repository.*;
import com.bloodbank.service.*;
import com.bloodbank.security.*;
import com.bloodbank.websocket.*;
import com.bloodbank.controller.*;

import org.springframework.stereotype.Service; import java.time.LocalDate; import java.util.*;
@Service
public class InventoryService {
  public static final List<String> GROUPS = List.of("A+","A-","B+","B-","AB+","AB-","O+","O-");
  static final Map<String,List<String>> CAN_RECEIVE_FROM = Map.of(
    "A+",List.of("A+","A-","O+","O-"), "A-",List.of("A-","O-"), "B+",List.of("B+","B-","O+","O-"), "B-",List.of("B-","O-"),
    "AB+",GROUPS, "AB-",List.of("A-","B-","AB-","O-"), "O+",List.of("O+","O-"), "O-",List.of("O-"));
  static final Map<String,Integer> SHELF_DAYS = Map.of("Platelets",5,"Red Blood Cells",42,"Whole Blood",42,"Plasma",365);
  final BagRepo bags; final Broadcaster ws;
  InventoryService(BagRepo b, Broadcaster w){ bags=b; ws=w; }
  public Map<String,Long> summary(){
    bags.expire(LocalDate.now()); Map<String,Long> m = new LinkedHashMap<>(); GROUPS.forEach(g->m.put(g,0L));
    for (var b : bags.findByStatus("Available")) m.merge(b.getBloodGroup(),1L,Long::sum); return m;
  }
  public Map<String,Object> search(String group){
    var groups = CAN_RECEIVE_FROM.get(group); if (groups==null) return null;
    bags.expire(LocalDate.now()); Map<String,Long> counts = new TreeMap<>();
    for (var b : bags.findByStatusAndBloodGroupIn("Available",groups)) counts.merge(b.getBloodGroup()+" · "+b.getComponentType(),1L,Long::sum);
    return Map.of("requested",group,"compatibleGroups",groups,"available",counts);
  }
  public BloodBag add(BloodBag b){
    if (b.getCollectionDate()==null) b.setCollectionDate(LocalDate.now());
    b.setExpirationDate(b.getCollectionDate().plusDays(SHELF_DAYS.getOrDefault(b.getComponentType(),42)));
    b.setStatus("Available"); var saved = bags.save(b); ws.send("{\"type\":\"inventory\"}"); return saved;
  }
  public List<BloodBag> all(){ bags.expire(LocalDate.now()); return bags.findAll(); }
  public BloodBag setStatus(Long id, String status){ var b = bags.findById(id).orElseThrow(); b.setStatus(status); var s = bags.save(b); ws.send("{\"type\":\"inventory\"}"); return s; }
}
