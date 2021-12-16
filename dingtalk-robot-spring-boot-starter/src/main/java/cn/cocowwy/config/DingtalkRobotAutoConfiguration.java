package cn.cocowwy.config;

import cn.cocowwy.dingtalk.DingTalkApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author cocowwy.cn
 * @create 2021-12-12-10:20
 */
@Configuration
@EnableConfigurationProperties(RobotsProperties.class)
public class DingtalkRobotAutoConfiguration {
    @Autowired
    private RobotsProperties robotsProperties;

    @Bean
    public DingTalkApi dingTalkApi() {
        return new DingTalkApi();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
