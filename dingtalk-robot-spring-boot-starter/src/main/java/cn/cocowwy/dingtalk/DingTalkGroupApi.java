package cn.cocowwy.dingtalk;


import cn.cocowwy.config.RobotsHookProperties;
import cn.cocowwy.util.RobotUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 群机器人API---钉钉通过webhook接入
 * 群机器人有消息限制 1min 20条
 * @author Cocowwy
 * @since 2021/6/8
 */
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
    public void sendText(String label, String message) {
        List<RobotsHookProperties.Robot> robotGroup = RobotUtil.getRobotGroup(label, robotsHookProperties.getHooks());
        RobotUtil.sendHookMessageAtAll(CollectionUtils.lastElement(robotGroup), message);
    }

    /**
     * 向指定 Label 的群机器人发送群消息，并且根据手机号@人员
     *
     * @param label
     * @param phones
     */
    public void sendTextAndAt(String label, String message, List<String> phones) {
        List<RobotsHookProperties.Robot> robotGroup = RobotUtil.getRobotGroup(label, robotsHookProperties.getHooks());
        RobotUtil.sendHookMessage(CollectionUtils.lastElement(robotGroup), message, phones);
    }

    /**
     * 向指定 Label 的群机器人发送群消息，并且@所有人
     *
     * @param label
     */
    @Deprecated
    public void sendTextAndAtAll(String label, String message) {
        List<RobotsHookProperties.Robot> robotGroup = RobotUtil.getRobotGroup(label, robotsHookProperties.getHooks());
        RobotUtil.sendHookMessageAtAll(CollectionUtils.lastElement(robotGroup), message);
    }
}
