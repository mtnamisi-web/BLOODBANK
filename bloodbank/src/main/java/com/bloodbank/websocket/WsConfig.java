package com.bloodbank.websocket;
import com.bloodbank.model.*;
import com.bloodbank.repository.*;
import com.bloodbank.service.*;
import com.bloodbank.security.*;
import com.bloodbank.websocket.*;
import com.bloodbank.controller.*;

import org.springframework.context.annotation.Configuration; import org.springframework.web.socket.config.annotation.*;
@Configuration @EnableWebSocket
public class WsConfig implements WebSocketConfigurer {
  final Broadcaster b; WsConfig(Broadcaster b){ this.b=b; }
  @Override public void registerWebSocketHandlers(WebSocketHandlerRegistry r){ r.addHandler(b,"/ws"); }
}
