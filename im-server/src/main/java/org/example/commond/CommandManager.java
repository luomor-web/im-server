package org.example.commond;

import lombok.extern.slf4j.Slf4j;
import org.example.commond.handler.*;
import org.example.commond.handler.emoticon.EmoticonOperationReqHandler;
import org.example.commond.handler.emoticon.EmoticonReqHandler;
import org.example.commond.handler.emoticon.EmoticonSearchReqHandler;
import org.example.commond.handler.message.*;
import org.example.commond.handler.room.*;
import org.example.commond.handler.system.EditProfileHandler;
import org.example.commond.handler.system.SetNewPasswordReqHandler;
import org.example.commond.handler.system.SystemTextMessageHandler;
import org.example.commond.handler.video.VideoReqHandler;
import org.example.enums.CommandEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class CommandManager {

    private static final Map<Integer, AbstractCmdHandler> handlerMap = new HashMap<>();

    static {
        try {
            registerCommand(new HeartbeatReqHandler());
            registerCommand(new LoginReqHandler());
            registerCommand(new UserReqHandler());
            registerCommand(new MessageReqHandler());
            registerCommand(new MessageSearchReqHandler());
            registerCommand(new JoinGroupReqHandler());
            registerCommand(new ChatReqHandler());
            registerCommand(new CreatGroupReqHandler());
            registerCommand(new CloseReqHandler());
            registerCommand(new MessageReadReqHandler());
            registerCommand(new UserListHandler());
            registerCommand(new MessageReactionReqHandler());
            registerCommand(new MessageForwardReqHandler());
            registerCommand(new EditProfileHandler());
            registerCommand(new SetNewPasswordReqHandler());
            registerCommand(new RemoveGroupUserReqHandler());
            registerCommand(new SetRoomAdminReqHandler());
            registerCommand(new SystemTextMessageHandler());
            registerCommand(new HandoverGroupHandler());
            registerCommand(new DisbandGroupHandler());
            registerCommand(new EditGroupProfileReqHandler());
            registerCommand(new MessageDeleteHandler());
            registerCommand(new UserGroupConfigReqHandler());
            registerCommand(new VideoReqHandler());
            registerCommand(new EmoticonSearchReqHandler());
            registerCommand(new EmoticonReqHandler());
            registerCommand(new EmoticonOperationReqHandler());
            registerCommand(new SetPublicRoomReqHandler());
            registerCommand(new SearchRoomReqHandler());
        } catch (Exception e) {
            log.info("?????????????????????");
        }

    }

    public static void registerCommand(AbstractCmdHandler imCommandHandler) {
        if (imCommandHandler == null || imCommandHandler.command() == null) {
            return;
        }
        int cmd_number = imCommandHandler.command().getValue();
        if (Objects.isNull(CommandEnum.forNumber(cmd_number))) {
            throw new RuntimeException("failed to register cmd handler, illegal cmd code:" + cmd_number + ",use Command.addAndGet () to add in the enumerated Command class!");
        }
        if (Objects.isNull(handlerMap.get(cmd_number))) {
            handlerMap.put(cmd_number, imCommandHandler);
        } else {
            throw new RuntimeException("cmd code:" + cmd_number + ",has been registered, please correct!");
        }
    }

    public static AbstractCmdHandler getCommand(CommandEnum command) {
        if (command == null) {
            throw new RuntimeException("???????????????????????????");
        }
        return handlerMap.get(command.getValue());
    }
}
