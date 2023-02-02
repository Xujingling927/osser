package com.jingling.osser.service;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jingling.osser.entity.MouseLocation;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ServerEndpoint("/{userId}")
@EnableScheduling
@Getter
public class WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    private static final ObjectMapper mapper = new ObjectMapper();
    /**
     * 在线人数
     */
    private static AtomicInteger onlineCount = new AtomicInteger(0);

    /**
     * 用来存放每个客户端对应的 WebSocketServer 对象
     */
    private static ConcurrentHashMap<String, WebSocketService> webSocketMap = new ConcurrentHashMap<>();

    private static String timingText = "";

    private static Map<Long,MouseLocation> map  = new HashMap<>();

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;

    /**
     * 接收 userId
     */
    private String userId = "";

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.session = session;
        this.userId = userId;
        if (webSocketMap.containsKey(userId)) {
            webSocketMap.remove(userId);
            webSocketMap.put(userId, this);
            //加入set中
        } else {
            webSocketMap.put(userId, this);
            addOnlineCount();
        }
        logger.info(String.format("用户id：%s已连接,当前在线人数为:%s", userId, getOnlineCount()));
        System.out.println("websocket open");
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            MouseLocation mouseLocation = mapper.readValue(message, MouseLocation.class);
            mouseLocation.setUserId(Long.valueOf(userId));
            map.put(Long.valueOf(userId),mouseLocation);
        } catch (JacksonException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.error(String.format("用户:%s,网络异常!!!!!!", userId));
        }
        logger.info(String.format("用户id:%s,报文:%s", userId, timingText));
    }

    @OnClose
    public void onClose() {
        if (webSocketMap.containsKey(userId)) {
            webSocketMap.remove(userId);
            map.remove(Long.valueOf(userId));
            subOnlineCount();
        }
        logger.info(String.format("用户退出:%s,当前在线人数为:%s", userId, getOnlineCount()));
        System.out.println("websocket close");
    }

    public static synchronized AtomicInteger getOnlineCount() {
        return onlineCount;
    }

    /**
     * 实现服务器主动推送
     */
    @Scheduled(fixedRate = 5000)
    public static void sendMessage() throws IOException {
        timingText = mapper.writeValueAsString(map.values());

        logger.info(String.format("最新坐标信息:  %s", timingText));

        for (Map.Entry<String, WebSocketService> entry : webSocketMap.entrySet()) {
            entry.getValue().session.getBasicRemote().sendText(timingText);
        }

    }

    /**
     * 在线人数增加
     */
    public static synchronized void addOnlineCount() {
        WebSocketService.onlineCount.getAndIncrement();
    }

    /**
     * 在线人数减少
     */
    public static synchronized void subOnlineCount() {
        WebSocketService.onlineCount.getAndDecrement();
    }
}
