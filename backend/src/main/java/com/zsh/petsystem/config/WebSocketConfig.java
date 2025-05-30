package com.zsh.petsystem.config;

import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.server.ServerEndpoint;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(ServerContainer.class)
public class WebSocketConfig {

  @Bean
  @ConditionalOnWebApplication
  public ServerEndpointExporter serverEndpointExporter() {
    return new ServerEndpointExporter();
  }
}