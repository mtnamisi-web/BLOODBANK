package com.bloodbank.websocket;
import com.bloodbank.model.*;
import com.bloodbank.repository.*;
import com.bloodbank.service.*;
import com.bloodbank.security.*;
import com.bloodbank.websocket.*;
import com.bloodbank.controller.*;

import org.springframework.stereotype.Component; import org.springframework.web.socket.*; import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.Set; import java.util.concurrent.CopyOnWriteArraySet;
@Component
public class Broadcaster extends TextWebSocketHandler {
  private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
  @Override public void afterConnectionEstablished(WebSocketSession s){ sessions.add(s); }
  @Override public void afterConnectionClosed(WebSocketSession s, CloseStatus c){ sessions.remove(s); }
  public void send(String json){
    for (var s : sessions) try { synchronized (s) { if (s.isOpen()) s.sendMessage(new TextMessage(json)); } } catch (Exception e) { sessions.remove(s); }
  }
}
