package com.jingling.osser.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
@Component
@ServerEndpoint("/websocket/{userId}")
public class WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);
    /**
     * 在线人数
     */
    private static AtomicInteger onlineCount = new AtomicInteger(0);

    /**
     * 用来存放每个客户端对应的 WebSocketServer 对象
     */
    private static ConcurrentHashMap<String, WebSocketService> webSocketMap = new ConcurrentHashMap<>();

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
        logger.info("用户连接:" + userId + ",当前在线人数为:" + getOnlineCount());
        try {
            sendMessage("连接成功");
        } catch (Exception e) {
            logger.error("用户:" + userId + ",网络异常!!!!!!");
        }

        System.out.println("websocket open");
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            sendMessage("俺收到了!你的消息是："+message);
        } catch (Exception e) {
            logger.error("用户:" + userId + ",网络异常!!!!!!");
        }
        logger.info("用户消息:" + userId + ",报文:" + message);
        System.out.println("websocket message: " + message);
    }

    @OnClose
    public void onClose() {
        if (webSocketMap.containsKey(userId)) {
            webSocketMap.remove(userId);
            subOnlineCount();
        }
        logger.info("用户退出:" + userId + ",当前在线人数为:" + getOnlineCount());
        System.out.println("websocket close");
    }

    public static synchronized AtomicInteger getOnlineCount() {
        return onlineCount;
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws Exception {
//        webSocketMap.get(userId).session.getBasicRemote().sendText(message);
        if (webSocketMap.get(userId).session.isOpen()) {
            webSocketMap.get(userId).session.getBasicRemote().sendText(message);
        }else {
            logger.info("用户"+userId+"已经关闭连接");
        }
//        this.session.getBasicRemote().sendText(message);
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
