package cn.cocowwy.dingtalk;


import cn.cocowwy.config.RobotsProperties;
import cn.cocowwy.util.RobotUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author Cocowwy
 * @since 2021/6/8
 */
@Slf4j
public class DingTalkApi {

    @Autowired
    private RobotsProperties robotsProperties;

    @Autowired
    private RestTemplate restTemplate;


    /**
     * 向指定 Label 的机器人发送群消息
     *
     * @param label 机器人 Label
     * @param text  文本消息体
     * @return
     */
    public void sendText(String label, String text) throws Exception {
        List<RobotsProperties.Robot> robotGroup = RobotUtil.getRobotGroup(label, robotsProperties.getRobots());

        robotGroup.forEach(robot -> {
            RobotUtil.sendMessage(robot, text, null);
        });
    }

    /**
     * 向指定 Label 的机器人发送群消息，并且根据手机号@人员
     * @param label
     * @param phones
     */
    public void sendTextAndAt(String label, String message, List<String> phones) throws Exception {
        List<RobotsProperties.Robot> robotGroup = RobotUtil.getRobotGroup(label, robotsProperties.getRobots());

        robotGroup.forEach(robot -> {
            RobotUtil.sendMessage(robot, message, phones);
        });
    }

    /**
     * 向指定 Label 的机器人发送群消息，并且@所有人
     * @param label
     */
    public void sendTextAndAtAll(String label, String message) throws Exception {
        List<RobotsProperties.Robot> robotGroup = RobotUtil.getRobotGroup(label, robotsProperties.getRobots());

        robotGroup.forEach(robot -> {
            RobotUtil.sendMessageAtAll(robot, message);
        });
    }


}
