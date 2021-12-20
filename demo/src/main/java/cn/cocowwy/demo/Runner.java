package cn.cocowwy.demo;

import cn.cocowwy.dingtalk.DingTalkGroupApi;
import cn.cocowwy.dingtalk.DingTalkRobotApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;


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
        for (int i = 0; i < 1000; i++) {
            dingTalkRobotApi.sendMessageByPhonesAt("robot1", Arrays.asList("18673159925"), "嘻嘻嘻嘻", "外卖机器人");
        }
    }
}
