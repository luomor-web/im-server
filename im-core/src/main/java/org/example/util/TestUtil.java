package org.example.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.model.Projections;
import org.bson.conversions.Bson;
import org.example.dao.MessageRepository;
import org.example.packets.bean.Message;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.*;

public class TestUtil {

    private static String[] familyName = new String[]{"刘", "张", "李", "胡", "沈", "朱", "钱", "王", "伍", "赵", "孙", "吕", "马", "秦", "毛", "成", "梅", "黄", "郭", "杨", "季", "童", "习", "郑",
            "吴", "周", "蒋", "卫", "尤", "何", "魏", "章", "郎", " 唐", "汤", "苗", "孔", "鲁", "韦", "任", "袁", "贺", "狄朱"};

    private static String[] secondName = new String[]{"艺昕", "红薯", "明远", "天蓬", "三丰", "德华", "歌", "佳", "乐", "天", "燕子", "子牛", "海", "燕", "花", "娟", "冰冰", "丽娅", "大为", "无为", "渔民", "大赋",
            "明", "远平", "克弱", "亦菲", "靓颖", "富城", "岳", "先觉", "牛", "阿狗", "阿猫", "辰", "蝴蝶", "文化", "冲之", "悟空", "行者", "悟净", "悟能", "观", "音", "乐天", "耀扬", "伊健", "炅", "娜", "春花", "秋香", "春香",
            "大为", "如来", "佛祖", "科比", "罗斯", "詹姆屎", "科神", "科蜜", "库里", "卡特", "麦迪", "乔丹", "魔术师", "加索尔", "法码尔", "南斯", "伊哥", "杜兰特", "保罗", "杭州", "爱湘", "湘湘", "昕", "函", "鬼谷子", "膑", "荡",
            "子家", "德利优视", "五方会谈", "来电话了", "轨迹", "超"};

    public static String chineseName() {
        return familyName[RandomUtil.randomInt(0, familyName.length - 1)] + secondName[RandomUtil.randomInt(0, secondName.length - 1)];
    }

    public static String avatar() {
        return "https://t1.huishahe.com/uploads/tu/202107/9999/7690765ea7.jpg";
    }

    public static int betweenDay(Date startTime, Date endTime) {
        long between = DateUtil.between(startTime, endTime, DateUnit.MS, true);
        int day = Math.toIntExact(between / (1000 * 60 * 60 * 24));
        return between % (1000 * 60 * 60 * 24) == 0 ? day : day + 1;
    }
    public static void main(String[] args) {


//        System.out.println(betweenDay(DateUtil.parse("2021-12-01 00:00:00"), DateUtil.parse("2021-12-02 23:59:59")));
//        TimeInterval timer = DateUtil.timer();
//        MessageRepository messageRepository = new MessageRepository();
//        for (int i = 0; i < 10000; i++) {
//            Message message = new Message();
//            message.setId(IdUtil.getSnowflake().nextIdStr());
//            message.setContent(i + "你好");
//            message.setRoomId("1457312478603968512");
////            message.set("1457312478603968512");
//            message.setDate(DateUtil.formatDate(new Date()));
//            message.setTimestamp(DateUtil.formatTime(new Date()));
//            message.setSenderId("1457227084663349248");
//            messageRepository.insert(message);
//        }
//        System.out.println(timer.intervalRestart());
//        List<Message> messages1 = messageRepository.find(and(eq("roomId", "1459053033268625408"), not(size("files", 0))));
//        messages1.forEach(System.out::println);
//        Pattern compile = Pattern.compile("/2/",Pattern.CASE_INSENSITIVE);
//        Pattern pattern = Pattern.compile("^.*2.*$", Pattern.CASE_INSENSITIVE);
//        List<Message> messages = messageRepository.find(and(eq("roomId", "1462764224268779520"), regex("content", pattern)));
//        messages.forEach(System.out::println);

    }

}
