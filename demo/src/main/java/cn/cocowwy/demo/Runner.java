package cn.cocowwy.demo;

import cn.cocowwy.dingtalk.DingTalkRobotApi;
import org.springframework.beans.factory.annotation.Autowired;
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


    @Override
    public void run(ApplicationArguments args) throws Exception {
        dingTalkRobotApi.sendMessageAt(null, null, null);
    }
}
