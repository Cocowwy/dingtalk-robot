package cn.cocowwy.dingtalk;


import cn.cocowwy.config.RobotsHookProperties;
import cn.cocowwy.util.RobotUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 群机器人API---钉钉通过webhook接入
 * 群机器人有消息限制 1min 20条 ,
 * 如果需要发送超过限制的消息，请使用 {@link cn.cocowwy.dingtalk.DingTalkGroupApi#sendFrequentlyMessage}
 * @author Cocowwy
 * @since 2021/6/8
 */
public class DingTalkGroupApi {

    @Autowired
    private RobotsHookProperties robotsHookProperties;

    /**
     * 获取可发送消息的群机器任标签列表
     * get dingding group's robots list
     * @return
     */
    public List<String> getHooks() {
        return robotsHookProperties.getHooks().stream().map(RobotsHookProperties.Robot::getLabel).collect(Collectors.toList());
    }

    /**
     * 向指定群机器人发送消息
     * send message to dingding group
     * @param label 机器人 Label
     * @param message  文本消息体
     * @return
     */
    public void sendText(String label, String message) throws Exception {
        List<RobotsHookProperties.Robot> robotGroup = RobotUtil.getRobotGroup(label, robotsHookProperties.getHooks());
        RobotUtil.sendHookMessage(CollectionUtils.lastElement(robotGroup), message, null);
    }

    /**
     * 向指定群机器人发送消息，并且根据手机号@
     * send messsage to dingding group and @ sb. by phones
     * @param label
     * @param phones 可传null
     */
    public void sendTextByPhones(String label, String message, List<String> phones) throws Exception {
        List<RobotsHookProperties.Robot> robotGroup = RobotUtil.getRobotGroup(label, robotsHookProperties.getHooks());
        RobotUtil.sendHookMessage(CollectionUtils.lastElement(robotGroup), message, phones);
    }

    /**
     * 向指定群机器人发送消息，并且@所有人
     * send message to dingding group and at @ all
     *
     * @param label
     */
    public void sendTextAndAtAll(String label, String message) throws Exception {
        List<RobotsHookProperties.Robot> robotGroup = RobotUtil.getRobotGroup(label, robotsHookProperties.getHooks());
        RobotUtil.sendHookMessageAtAll(CollectionUtils.lastElement(robotGroup), message);
    }

    /**
     * 用于解决群机器人1分钟的20条消息限制
     *  - 该接口会在短时间内的消息自动拼接成长消息，默认缓存拼接时间为10s
     *    如gapTime=10s
     *  - 消息结尾进行进行两次斜杠n操作来区分单个消息
     *  - 多次at人的话，会将at整合
     *
     *  if you want to send frequently message (1min > 20 times), you can use this way
     *
     * @param label 机器人的唯一标识
     * @param message 消息
     */
    public void sendFrequentlyMessage(String label, String message, List<String> phones) {
        List<RobotsHookProperties.Robot> robotGroup = RobotUtil.getRobotGroup(label, robotsHookProperties.getHooks());
        RobotUtil.sendFrequentlyMessage(CollectionUtils.lastElement(robotGroup), message, phones);
    }


    /**
     * 根据传入的webhook和签名，向指定群发送消息
     * @param webhook
     * @param signature
     * @param message
     * @param atAll 是否@所有人
     * @param phones 根据指定手机号@，可传空
     */
    public void sendMessageByCustomHook(String webhook, String signature, String message, Boolean atAll, List<String> phones) throws Exception {
        RobotUtil.sendMessageByCustomHook(webhook, signature, message, atAll, phones);
    }
}
