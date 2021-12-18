package cn.cocowwy.dingtalk;

import cn.cocowwy.RobotException;
import cn.cocowwy.config.RobotsProperties;
import cn.cocowwy.util.RobotUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.io.StringWriter;
import java.util.List;

/**
 * 单聊机器人API
 * @author cocowwy.cn
 * @create 2021-12-12-17:22
 */
@Slf4j
public class DingTalkRobotApi {
    @Autowired
    private RobotsProperties robotsProperties;

    /**
     * 向指定的机器人根据手机号群发消息
     */
    public void sendMessageAt(String label, String message, List<String> phones) throws Exception {
        try {
            List<RobotsProperties.Robot> robots = RobotUtil.getRobot(label, robotsProperties.getRobot());
            RobotsProperties.Robot robot = CollectionUtils.lastElement(robots);
            RobotUtil.sendMessage2Sb(robot, phones, message);
        } catch (RobotException e) {
            // ignore..
            log.error("RobotException exception  ：", e);
        }
    }
}
