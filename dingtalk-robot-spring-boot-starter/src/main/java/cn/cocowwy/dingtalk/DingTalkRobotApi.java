package cn.cocowwy.dingtalk;

import cn.cocowwy.config.RobotsProperties;
import cn.cocowwy.util.RobotUtil;
import com.taobao.api.ApiException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 单聊机器人API
 * @author cocowwy.cn
 * @create 2021-12-12-17:22
 */
public class DingTalkRobotApi {
    private static final Log logger = LogFactory.getLog(RobotUtil.class);

    @Autowired
    private RobotsProperties robotsProperties;

    /**
     * 获取单聊机器人列表
     * @return
     */
    public List<String> getRobots() {
        return robotsProperties.getRobot().stream().map(RobotsProperties.Robot::getLabel).collect(Collectors.toList());
    }

    /**
     * 根据手机号向指定人发送消息
     * @param label 机器人标识
     * @param phones 手机号集合
     * @param message 消息
     * @param title 标题
     */
    public void sendMessageByPhones(String label, List<String> phones, String message, String title) throws Exception {
        List<RobotsProperties.Robot> robots = RobotUtil.getRobot(label, robotsProperties.getRobot());
        RobotsProperties.Robot robot = CollectionUtils.lastElement(robots);
        RobotUtil.sendMessage2Sb(robot, phones, message, title);
    }

    /**
     * 根据用户id给指定人发送消息
     * @param label 机器人标识
     * @param userids userid集合
     * @param message 消息
     * @param title 标题
     */
    public void sendMessageByUserIds(String label, List<String> userids, String message, String title) throws Exception {
        List<RobotsProperties.Robot> robots = RobotUtil.getRobot(label, robotsProperties.getRobot());
        RobotsProperties.Robot robot = CollectionUtils.lastElement(robots);
        RobotUtil.sendMessageByUserIds(robot, userids, message, title);
    }

    /**
     * 根据手机号发送Link消息
     * @param label 机器人标识
     * @param title 消息标题
     * @param phone 手机号
     * @param text 消息体
     * @param messageUrl 点击消息跳转的URL
     * @param picUrl 图片URL
     */
    public void sendLinkMessageByPhones(String label, String phone, String title, String text, String messageUrl, String picUrl) throws Exception {
        List<RobotsProperties.Robot> robots = RobotUtil.getRobot(label, robotsProperties.getRobot());
        RobotsProperties.Robot robot = CollectionUtils.lastElement(robots);
        RobotUtil.sendLinkMessageByPhone(robot, phone, title, text, messageUrl, picUrl);
    }

    /**
     * 查询指定机器人的token
     * @param label 机器人标识
     * @param useCache 是否走缓存，默认true
     */
    public String getToken(String label, Boolean useCache) throws ApiException {
        List<RobotsProperties.Robot> robots = RobotUtil.getRobot(label, robotsProperties.getRobot());
        return RobotUtil.getRobotToken(CollectionUtils.lastElement(robots), Optional.ofNullable(useCache).orElse(true));
    }
}
