package org.example.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.enums.CommandEnum;
import org.example.enums.KeyEnum;
import org.example.packets.FriendInfo;
import org.example.packets.Group;
import org.example.packets.User;
import org.example.packets.handler.*;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.intf.Packet;
import org.tio.utils.lock.SetWithLock;
import org.tio.websocket.common.WsResponse;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Slf4j
public class Im extends ImConfig {

    /**
     * 阻塞发送消息
     *
     * @param channelContext 上下文信息
     * @param packet         包信息
     */
    public static void bSend(ChannelContext channelContext, Packet packet) {
        if (channelContext == null || packet == null) {
            return;
        }
        Tio.bSend(channelContext, packet);
    }

    /**
     * 发送消息
     *
     * @param channelContext 上下文信息
     * @param packet         包信息
     */
    public static void send(ChannelContext channelContext, Packet packet) {
        if (channelContext == null || packet == null) {
            return;
        }
        Tio.send(channelContext, packet);
    }

    /**
     * 绑定到群组
     *
     * @param channelContext 上下文信息
     * @param group          群组信息
     */
    public static void bindGroup(ChannelContext channelContext, Group group) {
        String groupId = group.getRoomId();
        Tio.bindGroup(channelContext, groupId);
    }

    /**
     * 绑定用户(如果配置了回调函数执行回调)
     *
     * @param channelContext IM通道上下文
     * @param user           绑定用户信息
     */
    public static void bindUser(ChannelContext channelContext, User user) {
        if (Objects.isNull(user) || StrUtil.isBlank(user.getId())) {
            log.error("user or userId is null");
            return;
        }
        String userId = user.getId();
        ImSessionContext imSessionContext = (ImSessionContext) channelContext.get(KeyEnum.IM_CHANNEL_SESSION_CONTEXT_KEY.getKey());
        Tio.bindUser(channelContext, userId);
        SetWithLock<ChannelContext> channelContextSetWithLock = Tio.getByUserid(Im.get().getTioConfig(), userId);
        ReentrantReadWriteLock.ReadLock lock = channelContextSetWithLock.getLock().readLock();
        try {
            lock.lock();
            if (CollUtil.isEmpty(channelContextSetWithLock.getObj())) {
                return;
            }
            imSessionContext.getImClientNode().setUser(user);
            get().imUserListener.onAfterBind(channelContext, user);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    public static void sendToGroup(JoinGroupNotifyBody joinGroupNotifyBody) {

        List<User> groupUsers = Im.get().messageHelper.getGroupUsers(joinGroupNotifyBody.getGroup().getRoomId());
        joinGroupNotifyBody.getGroup().setUsers(groupUsers);

        if (CollUtil.isEmpty(joinGroupNotifyBody.getUsers())) {
            return;
        }
        SetWithLock<ChannelContext> users = Tio.getByGroup(Im.get().tioConfig, joinGroupNotifyBody.getGroup().getRoomId());
        if (users == null) {
            return;
        }
        List<User> userList = new ArrayList<>();
        joinGroupNotifyBody.getUsers().forEach(x -> userList.add(get().messageHelper.getUserInfo(x.getId())));

        List<ChannelContext> channelContexts = convertChannel(users);
        String collect = userList.stream().map(User::getUsername).collect(Collectors.joining(StrUtil.COMMA));

        joinGroupNotifyBody.setMessage(collect + ",已加入群聊!");

        WsResponse wsResponse = WsResponse.fromText(RespBody.success(CommandEnum.COMMAND_JOIN_GROUP_NOTIFY_RESP, joinGroupNotifyBody), CHARSET);
        for (ChannelContext context : channelContexts) {
            send(context, wsResponse);
        }
    }

    /**
     * 聊天消息
     *
     * @param chatRespBody 聊天消息体
     */
    public static void sendToGroup(ChatRespBody chatRespBody) {
        // 构建消息体
        User userInfo = get().messageHelper.getUserInfo(chatRespBody.getSenderId());
        chatRespBody.setAvatar(userInfo.getAvatar());
        chatRespBody.setUsername(userInfo.getUsername());
        chatRespBody.setDeleted(false);
        chatRespBody.setSystem(false);

        log.info("目标数据：" + RespBody.success(CommandEnum.COMMAND_CHAT_REQ, chatRespBody));

        // 获取到群组内的所有用户
        List<User> groupUsers = Im.get().messageHelper.getGroupUsers(chatRespBody.getRoomId());
        for (User groupUser : groupUsers) {
            // 给这个用户设置未读消息
            get().messageHelper.putUnReadMessage(groupUser.getId(), chatRespBody.getRoomId(), chatRespBody.get_id());
            // 取出未读消息, 并设置未读数量
            List<String> unReadMessage = get().messageHelper.getUnReadMessage(groupUser.getId(), chatRespBody.getRoomId());
            chatRespBody.setUnreadCount(CollUtil.isEmpty(unReadMessage) ? 0 : unReadMessage.size());
            WsResponse wsResponse = WsResponse.fromText(RespBody.success(CommandEnum.COMMAND_CHAT_REQ, chatRespBody), CHARSET);
            // 发送给在线用户
            List<ChannelContext> channelContexts = getChannelByUserId(groupUser.getId());

            // 如果当前
            if (CollUtil.isNotEmpty(channelContexts)) {
                for (ChannelContext channelContext : channelContexts) {
                    send(channelContext, wsResponse);
                }
            }
        }
    }

    /**
     * 用户状态变更消息
     *
     * @param userStatusBody 用户状态消息
     * @param channelContext 群组
     */
    public static void sendToGroup(UserStatusBody userStatusBody, ChannelContext channelContext) {
        sendToGroup(userStatusBody,channelContext,false);
    }

    /**
     * 用户状态变更消息
     *
     * @param userStatusBody 用户状态消息
     * @param channelContext 群组
     */
    public static void sendToGroup(UserStatusBody userStatusBody, ChannelContext channelContext,Boolean sendAll) {
        WsResponse wsResponse = WsResponse.fromText(RespBody.success(CommandEnum.COMMAND_USER_STATUS_RESP, userStatusBody), CHARSET);
        SetWithLock<ChannelContext> users = Tio.getByGroup(Im.get().getTioConfig(), userStatusBody.getGroup().getRoomId());
        List<ChannelContext> channelContexts = convertChannel(users);
        User nowUser = getUser(channelContext, false);
        for (ChannelContext context : channelContexts) {
            User user = getUser(context);
            if (user.getId().equals(nowUser.getId()) && !sendAll) {
                continue;
            }
            send(context, wsResponse);
        }
    }

    /**
     * 群组表情回复
     *
     * @param messageReactionRespBody 表情回复
     */
    public static void sendToGroup(MessageReactionRespBody messageReactionRespBody) {
        WsResponse wsResponse = WsResponse.fromText(RespBody.success(CommandEnum.COMMAND_SEND_MESSAGE_REACTION_RESP, messageReactionRespBody), CHARSET);
        SetWithLock<ChannelContext> users = Tio.getByGroup(Im.get().getTioConfig(), messageReactionRespBody.getRoomId());
        List<ChannelContext> channelContexts = convertChannel(users);
        for (ChannelContext context : channelContexts) {
            send(context, wsResponse);
        }
    }

    private static List<ChannelContext> convertChannel(SetWithLock<ChannelContext> channelContextSetWithLock) {
        ReentrantReadWriteLock.ReadLock lock = channelContextSetWithLock.getLock().readLock();
        try {
            lock.lock();
            Set<ChannelContext> channelContexts = channelContextSetWithLock.getObj();
            return new ArrayList<>(channelContexts);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
        return new ArrayList<>();
    }

    /**
     * 移除用户, 和close方法一样，只不过不再进行重连等维护性的操作
     *
     * @param userId 用户ID
     * @param remark 移除原因描述
     */
    public static void remove(String userId, String remark) {
        SetWithLock<ChannelContext> userChannelContexts = Tio.getByUserid(ImConfig.get().getTioConfig(), userId);
        Set<ChannelContext> channels = userChannelContexts.getObj();
        if (channels.isEmpty()) {
            return;
        }
        ReentrantReadWriteLock.ReadLock readLock = userChannelContexts.getLock().readLock();
        try {
            readLock.lock();
            for (ChannelContext channelContext : channels) {
                remove(channelContext, remark);
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 移除连接
     *
     * @param channelContext 上下文信息
     * @param remark         备注
     */
    public static void remove(ChannelContext channelContext, String remark) {
        Tio.remove(channelContext, remark);
    }

    /**
     * 获取当前用户上下文完整信息
     *
     * @param channelContext 上下文信息
     * @return 用户信息（完整）
     */
    public static User getUser(ChannelContext channelContext) {
        return getUser(channelContext, true);
    }

    /**
     * 获取当前用户上下文简易
     *
     * @param channelContext 上下文信息
     * @param isAllInfo      是否包含全部信息
     * @return 用户信息（基础信息）
     */
    public static User getUser(ChannelContext channelContext, boolean isAllInfo) {
        ImSessionContext imSessionContext = (ImSessionContext) channelContext.get(KeyEnum.IM_CHANNEL_SESSION_CONTEXT_KEY.getKey());
        User user = imSessionContext.getImClientNode().getUser();
        if(user == null){
            Im.close(channelContext,"异常关闭");
        }
        if (isAllInfo) {
            return user;
        }
        return user.clone();
    }

    public static void addGroup(ChannelContext channelContext, Group group) {
        ImSessionContext imSessionContext = (ImSessionContext) channelContext.get(KeyEnum.IM_CHANNEL_SESSION_CONTEXT_KEY.getKey());
        imSessionContext.getImClientNode().getUser().addGroup(group);
    }

    public static void close(ChannelContext channelContext, String remark) {
        Tio.close(channelContext, remark);
    }

    /**
     * 判断用户是否在线
     *
     * @param id 用户ID
     * @return 是否在线
     */
    public static boolean isOnline(String id) {
        List<ChannelContext> channelByUserId = getChannelByUserId(id);
        return CollUtil.isNotEmpty(channelByUserId);
    }

    /**
     * 获取用户下所有通道
     *
     * @param id 用户主键
     * @return 所有连接
     */
    public static List<ChannelContext> getChannelByUserId(String id) {
        SetWithLock<ChannelContext> userChannelContext = Tio.getByUserid(Im.get().getTioConfig(), id);
        if (userChannelContext == null) {
            return new ArrayList<>();
        }
        return convertChannel(userChannelContext);
    }


    public static void resetGroup(Group group, String userId, Map<String, String> userFriends) {
        if (userFriends == null) {
            userFriends = get().messageHelper.getUserFriends(userId);
        }
        log.info("{}",group);
        // 获取好友信息
        String friendInfoStr = userFriends.get(group.getRoomId());
        if (StrUtil.isNotBlank(friendInfoStr)) {
            FriendInfo friendInfo = JSON.parseObject(friendInfoStr, FriendInfo.class);
            group.setFriendId(friendInfo.get_id());
            group.setRoomName(friendInfo.getRemark());
            if (StrUtil.isBlank(group.getRoomName())) {
                group.getUsers().forEach(x -> {
                    if (x.getId().equals(friendInfo.get_id())) {
                        if(StrUtil.isBlank(friendInfo.getRemark())){
                            group.setRoomName(x.getUsername());
                        }
                        group.setAvatar(x.getAvatar());
                    }
                });
            }
        }
        log.info("{}",group);
    }

}
