package cn.cocowwy.config;

import cn.cocowwy.dingtalk.DingTalkGroupApi;
import cn.cocowwy.dingtalk.DingTalkRobotApi;
import cn.cocowwy.util.SpringUtil;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author cocowwy.cn
 * @create 2021-12-12-10:20
 */
@Configuration
@EnableConfigurationProperties({RobotsHookProperties.class, RobotsProperties.class})
public class DingtalkRobotAutoConfiguration {

    @Bean
    public DingTalkGroupApi dingTalkGroupApi() {
        return new DingTalkGroupApi();
    }

    @Bean
    public DingTalkRobotApi dingTalkRobotApi() {
        return new DingTalkRobotApi();
    }

    @Bean
    public SpringUtil spiringUtil(){
        return new SpringUtil();
    }
}
