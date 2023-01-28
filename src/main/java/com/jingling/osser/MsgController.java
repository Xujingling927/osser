package com.jingling.osser;

import com.jingling.osser.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MsgController {
    @Autowired
    private WebSocketService webSocketService;

    @PostMapping ("/sendMsg")
    public void sendMsg(@RequestBody String message) throws Exception {
        webSocketService.sendMessage(message);
    }

}
