package cn.cocowwy.dingtalk;


import cn.cocowwy.config.RobotsHookProperties;
import cn.cocowwy.util.RobotUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;

/**
 * 群机器人API---钉钉通过webhook接入
 * @author Cocowwy
 * @since 2021/6/8
 */
@Slf4j
public class DingTalkGroupApi {

    @Autowired
    private RobotsHookProperties robotsHookProperties;

    /**
     * 向指定 Label 的群机器人发送群消息
     *
     * @param label 机器人 Label
     * @param message  文本消息体
     * @return
     */
    public void sendText(String label, String message) throws Exception {
        List<RobotsHookProperties.Robot> robotGroup = RobotUtil.getRobotGroup(label, robotsHookProperties.getHooks());
        RobotUtil.sendMessageAtAll(CollectionUtils.lastElement(robotGroup), message);
    }

    /**
     * 向指定 Label 的群机器人发送群消息，并且根据手机号@人员
     * @param label
     * @param phones
     */
    public void sendTextAndAt(String label, String message, List<String> phones) throws Exception {
        List<RobotsHookProperties.Robot> robotGroup = RobotUtil.getRobotGroup(label, robotsHookProperties.getHooks());
        RobotUtil.sendMessageAtAll(CollectionUtils.lastElement(robotGroup), message);
    }

    /**
     * 向指定 Label 的群机器人发送群消息，并且@所有人
     * @param label
     */
    @Deprecated
    public void sendTextAndAtAll(String label, String message) throws Exception {
        List<RobotsHookProperties.Robot>  robotGroup= RobotUtil.getRobotGroup(label, robotsHookProperties.getHooks());
        RobotUtil.sendMessageAtAll(CollectionUtils.lastElement(robotGroup), message);
    }


}
