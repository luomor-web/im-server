package org.example.config;

import org.example.listener.*;
import org.example.protocol.ws.WsMsgHandler;
import org.example.store.RedisMessageHelper;
import org.tio.server.ServerTioConfig;
import org.tio.websocket.server.WsServerStarter;

import java.io.IOException;

public class ImServerWebSocketStart {

    private final WsServerStarter wsServerStarter;
    private final ServerTioConfig serverTioConfig;

    public ImServerWebSocketStart(int port, WsMsgHandler wsMsgHandler) throws IOException {
        wsServerStarter = new WsServerStarter(port, wsMsgHandler);

        serverTioConfig = wsServerStarter.getServerTioConfig();
        serverTioConfig.setGroupListener(new ImGroupListenerAdapter(new ImServerGroupListener()));
        serverTioConfig.setName(ImConfig.PROTOCOL_NAME);
        serverTioConfig.setServerAioListener(ImServerAioListener.me);
        serverTioConfig.ipStats.addDurations(ImConfig.IpStatDuration.IPSTAT_DURATIONS);
        serverTioConfig.setHeartbeatTimeout(ImConfig.HEARTBEAT_TIMEOUT);
    }

    public static void start() throws Exception {

        ImServerWebSocketStart appStarter = new ImServerWebSocketStart(ImConfig.SERVER_PORT, WsMsgHandler.me);

        ImConfig imServerConfig = new ImConfig();
        imServerConfig.setMessageHelper(new RedisMessageHelper());
        imServerConfig.setImUserListener(new ImUserListenerAdapter(new ImServerUserListener()));
        imServerConfig.setTioConfig(appStarter.serverTioConfig);

        appStarter.wsServerStarter.start();
    }
}
