package org.example.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KeyEnum {

    /**
     * 上下文Key
     */
    IM_CHANNEL_CONTEXT_KEY("im_channel_context_key"),

    /**
     * Session上下文 Key
     */
    IM_CHANNEL_SESSION_CONTEXT_KEY("im_channel_session_context_key"),

    /**
     * 用户信息 下存储人员姓名等基础信息 包括人员状态
     */
    IM_USER_INFO_KEY("USER_INFO"),

    /**
     * 用户 存储用户拥有的群组ID列表
     */
    IM_USER_GROUPS_KEY("USER_GROUPS"),

    /**
     * 组 该Key下存储群组下面的用户ID列表
     */
    IM_GROUP_USERS_KEY("GROUP_USERS"),

    /**
     * 群组信息
     */
    IM_GROUP_INFO_KEY("GROUP_INFO");


    private final String key;
}
