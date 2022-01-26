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
//        dingTalkRobotApi.getRobots().forEach(System.out::println);
//        dingTalkGroupApi.getHooks().forEach(System.out::println);
        for (int i = 0; i < 100; i++) {
            Thread.sleep(1 * 1000);
            dingTalkGroupApi.sendLargeText("large", "----------测试长消息 ", Arrays.asList("18673159925"));
        }
    }
}
