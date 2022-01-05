package cn.cocowwy.demo;

import cn.cocowwy.dingtalk.DingTalkGroupApi;
import cn.cocowwy.dingtalk.DingTalkRobotApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


/**
 * @author cocowwy.cn
 * @create 2021-12-12-10:41
 */
@Component
public class Runner implements ApplicationRunner {
    @Autowired
    private DingTalkRobotApi dingTalkRobotApi;
    @Autowired
    private DingTalkGroupApi dingTalkGroupApi;


    @Override
    public void run(ApplicationArguments args) throws Exception {
//        String text = "事项：【MEITUAN】订单配送30分钟未送达\n" +
//                "门店名称：九龙仓LG层黄土岭店\n" +
//                "订单编码：211221914623850496\n" +
//                "骑手接单时间：Tue Dec 21 15:31:03 CST 2021";
//
//        dingTalkRobotApi.sendMessageByPhonesAt(robotDelivery, Arrays.asList("18673159925"), text, "===");
//        dingTalkGroupApi.sendText("mt", "xxxx");
        dingTalkRobotApi.getRobots().forEach(System.out::println);
        dingTalkGroupApi.getHooks().forEach(System.out::println);
    }
}
