package org.example.protocol.ws;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import org.example.commond.AbstractCmdHandler;
import org.example.commond.CommandManager;
import org.example.commond.handler.LoginReqHandler;
import org.example.config.Im;
import org.example.enums.CommandEnum;
import org.example.packets.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.websocket.common.WsRequest;
import org.tio.websocket.common.WsResponse;
import org.tio.websocket.server.handler.IWsMsgHandler;

public class WsMsgHandler implements IWsMsgHandler {
    private static final Logger log = LoggerFactory.getLogger(WsMsgHandler.class);

    public static final WsMsgHandler me = new WsMsgHandler();


    @Override
    public HttpResponse handshake(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) {
        String clientIp = httpRequest.getClientIp();
        String username = httpRequest.getParam("username");
        log.info("收到来自{}的ws握手包{}", clientIp, username);
        return httpResponse;
    }

    @Override
    public void onAfterHandshaked(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) {
        LoginReqHandler loginHandler = (LoginReqHandler) CommandManager.getCommand(CommandEnum.COMMAND_LOGIN_REQ);
        loginHandler.handler(httpRequest, channelContext);
        log.info("握手完毕{},{}", "66", "666");
    }

    @Override
    public Object onBytes(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) {
        return null;
    }

    @Override
    public Object onClose(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) {
        AbstractCmdHandler command = CommandManager.getCommand(CommandEnum.COMMAND_CLOSE_REQ);
        WsRequest request = WsRequest.fromText(Im.getUser(channelContext).getId(), Im.CHARSET);
        command.handler(request, channelContext);
        System.out.println("关闭连接");
        return null;
    }

    @Override
    public Object onText(WsRequest wsRequest, String text, ChannelContext channelContext) {
        log.info("socket消息:{}", text);
        Message message = JSON.parseObject(text, Message.class);
        CommandEnum commandEnum = CommandEnum.forNumber(message.getCmd());
        AbstractCmdHandler command = CommandManager.getCommand(commandEnum);
        WsResponse wsResponse = command.handler(wsRequest, channelContext);
        if (ObjectUtil.isNotNull(wsResponse)) {
            Im.send(channelContext, wsResponse);
        }
        return null;
    }
}
