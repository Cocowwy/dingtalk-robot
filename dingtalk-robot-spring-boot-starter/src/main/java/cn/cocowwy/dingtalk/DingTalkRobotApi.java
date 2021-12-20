package cn.cocowwy.dingtalk;

import cn.cocowwy.RobotException;
import cn.cocowwy.config.RobotsProperties;
import cn.cocowwy.util.RobotUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;

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
     * 向指定的机器人根据手机号群发消息
     */
    public void sendMessageByPhonesAt(String label, List<String> phones, String message, String title) {
        try {
            List<RobotsProperties.Robot> robots = RobotUtil.getRobot(label, robotsProperties.getRobot());
            RobotsProperties.Robot robot = CollectionUtils.lastElement(robots);
            RobotUtil.sendMessage2Sb(robot, phones, message, title);
        } catch (RobotException e) {
            // ignore..
            logger.error("RobotException exception  ：", e);
        } catch (Exception e) {
            logger.error("error" + e);
        }
    }

    public void sendMessageByUserIdsAt(String label, List<String> userids, String message, String title) {
        try {
            List<RobotsProperties.Robot> robots = RobotUtil.getRobot(label, robotsProperties.getRobot());
            RobotsProperties.Robot robot = CollectionUtils.lastElement(robots);
            RobotUtil.sendMessageByUserIdsAt(robot, userids, message, title);
        } catch (RobotException e) {
            // ignore..
            logger.error("RobotException exception  ：", e);
        } catch (Exception e) {
            logger.error("error" + e);
        }
    }
}
