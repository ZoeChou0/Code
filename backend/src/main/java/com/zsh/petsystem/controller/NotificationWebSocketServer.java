package com.zsh.petsystem.controller;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper; // 用于将对象转为JSON字符串

@Component
@Slf4j
@ServerEndpoint("/ws/notifications") // 定义 WebSocket 端点路径
public class NotificationWebSocketServer {

  // 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
  private static int onlineCount = 0;
  // concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
  private static CopyOnWriteArraySet<NotificationWebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();

  // 与某个客户端的连接会话，需要通过它来给客户端发送数据
  private Session session;

  private static final ObjectMapper objectMapper = new ObjectMapper(); // 用于序列化消息对象

  /**
   * 连接建立成功调用的方法
   */
  @OnOpen
  public void onOpen(Session session) {
    this.session = session;
    webSocketSet.add(this); // 加入set中
    addOnlineCount(); // 在线数加1
    log.info("有新Web连接加入！当前在线人数为: {}", getOnlineCount());
    try {
      sendMessage("连接成功"); // 可以给客户端发送一条连接成功的消息
    } catch (IOException e) {
      log.error("WebSocket IO异常:", e);
    }
  }

  /**
   * 连接关闭调用的方法
   */
  @OnClose
  public void onClose() {
    webSocketSet.remove(this); // 从set中删除
    subOnlineCount(); // 在线数减1
    log.info("一个Web连接关闭！当前在线人数为: {}", getOnlineCount());
  }

  /**
   * 收到客户端消息后调用的方法
   *
   * @param message 客户端发送过来的消息
   */
  @OnMessage
  public void onMessage(String message, Session session) {
    log.info("收到来自Web客户端 {} 的消息: {}", session.getId(), message);
    // 目前是广播，所以不处理特定客户端发来的消息，但可以预留逻辑
    // 例如，客户端可以发送心跳包 "ping"，服务器回复 "pong"
    if ("ping".equalsIgnoreCase(message)) {
      try {
        session.getBasicRemote().sendText("pong");
      } catch (IOException e) {
        log.error("发送 pong 失败:", e);
      }
    }
  }

  /**
   * 发生错误时调用
   */
  @OnError
  public void onError(Session session, Throwable error) {
    log.error("WebSocket发生错误: {} Session ID: {}", error.getMessage(), session.getId());
    error.printStackTrace();
  }

  /**
   * 实现服务器主动推送
   */
  public void sendMessage(String message) throws IOException {
    if (this.session != null && this.session.isOpen()) {
      this.session.getBasicRemote().sendText(message);
    }
  }

  /**
   * 服务器主动推送结构化消息 (例如 JSON 对象)
   */
  public void sendObject(Object messageObject) throws IOException {
    if (this.session != null && this.session.isOpen()) {
      String jsonMessage = objectMapper.writeValueAsString(messageObject);
      this.session.getBasicRemote().sendText(jsonMessage);
    }
  }

  /**
   * 群发自定义消息（文本）
   */
  public static void sendInfo(String message) {
    log.info("准备向 {} 个Web客户端广播消息: {}", webSocketSet.size(), message);
    for (NotificationWebSocketServer item : webSocketSet) {
      try {
        item.sendMessage(message);
      } catch (IOException e) {
        log.error("向Web客户端 {} 广播消息失败:", item.session.getId(), e);
        // 继续向其他客户端发送
      }
    }
  }

  /**
   * 群发自定义消息（对象，会转为JSON）
   * 我们将使用这个方法来广播 JPushBroadcastRequestDTO 类似的数据结构
   */
  public static void sendObjectInfo(Object messageObject) {
    if (messageObject == null)
      return;
    log.info("准备向 {} 个Web客户端广播对象消息: {}", webSocketSet.size(), messageObject);
    String jsonMessage;
    try {
      jsonMessage = objectMapper.writeValueAsString(messageObject);
    } catch (IOException e) {
      log.error("序列化广播对象消息失败:", e);
      return;
    }

    for (NotificationWebSocketServer item : webSocketSet) {
      try {
        item.sendMessage(jsonMessage); // 直接发送序列化后的JSON字符串
      } catch (IOException e) {
        log.error("向Web客户端 {} 广播对象消息失败:", item.session.getId(), e);
      }
    }
  }

  public static synchronized int getOnlineCount() {
    return onlineCount;
  }

  public static synchronized void addOnlineCount() {
    NotificationWebSocketServer.onlineCount++;
  }

  public static synchronized void subOnlineCount() {
    NotificationWebSocketServer.onlineCount--;
  }
}