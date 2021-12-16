package cn.cocowwy.demo;

import cn.cocowwy.config.RobotsProperties;
import cn.cocowwy.dingtalk.DingTalkApi;
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
    private DingTalkApi dingTalkApi;

    @Autowired
    private RobotsProperties robotsProperties;


    @Override
    public void run(ApplicationArguments args) throws Exception {
//        dingTalkApi.sendTextAndAt("mt", "测试群发，@", Arrays.asList("18673159925"));
        dingTalkApi.sendTextAndAtAll("mt", "测试群发，@");
    }
}
