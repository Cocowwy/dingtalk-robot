package cn.cocowwy.config;

import cn.cocowwy.dingtalk.DingTalkGroupApi;
import cn.cocowwy.dingtalk.DingTalkRobotApi;
import cn.cocowwy.util.SpringUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author cocowwy.cn
 * @create 2021-12-12-10:20
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({RobotsGlobalProperties.class,
        RobotsHookProperties.class,
        RobotsProperties.class})
@ConditionalOnProperty("")
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
    @Order(Integer.MAX_VALUE)
    public SpringUtil spiringUtil() {
        return new SpringUtil();
    }
}
