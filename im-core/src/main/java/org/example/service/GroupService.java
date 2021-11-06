package org.example.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.example.dao.GroupRepository;
import org.example.packets.LastMessage;
import org.example.packets.bean.Group;
import org.example.packets.bean.Message;

import static com.mongodb.client.model.Filters.eq;

public class GroupService {
    private final GroupRepository groupRepository;

    public GroupService() {
        groupRepository = new GroupRepository();
    }

    public void insertOne(Group group) {
        groupRepository.insert(group);
    }

    public void saveOrUpdate(Group build) {
        groupRepository.saveOrUpdate(eq("roomId", build.getRoomId()), build);
    }

    public void saveOrUpdateById(Group build) {
        groupRepository.saveOrUpdateById(build.clone());
    }

    public Group getGroupInfo(String roomId) {
        return groupRepository.findById(roomId);
    }

    public void updateLastMessage(Message message) {
        Group group = groupRepository.findById(message.getRoomId());
        LastMessage lastMessage = BeanUtil.copyProperties(message, LastMessage.class);
        if (StrUtil.isBlank(message.getContent()) && CollUtil.isNotEmpty(message.getFiles())) {
            if (message.getFiles().size() == 1) {
                lastMessage.setContent("[文件] - " + message.getFiles().get(0).getName());
            }
            else{
                lastMessage.setContent("[文件] - " + message.getFiles().get(0).getName() + "等多个文件");
            }
        }
        group.setLastMessage(lastMessage);
        groupRepository.updateById(group);
    }

    public void updateById(Group userGroup) {
        groupRepository.updateById(userGroup.clone());
    }
}