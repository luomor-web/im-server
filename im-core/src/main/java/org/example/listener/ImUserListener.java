package org.example.listener;

import org.example.packets.bean.User;
import org.tio.core.ChannelContext;

public interface ImUserListener {

    void onAfterBind(ChannelContext channelContext, User user);

    void onAfterUnbind(ChannelContext channelContext, User user);
}
