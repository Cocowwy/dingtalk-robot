package cn.cocowwy.dingtalk;

import cn.cocowwy.util.RobotException;
import cn.cocowwy.config.RobotsProperties;
import cn.cocowwy.util.RobotUtil;
import cn.hutool.core.util.StrUtil;
import com.taobao.api.ApiException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

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
     * 根据手机号向指定人发送消息
     * @param label 机器人标识
     * @param phones 手机号集合
     * @param message 消息
     * @param title 标题
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

    /**
     * 根据用户id给指定人发送消息
     * @param label 机器人标识
     * @param userids userid集合
     * @param message 消息
     * @param title 标题
     */
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

    /**
     * 查询指定机器人的token
     * @param label 机器人标识
     * @param useCache 是否走缓存，默认走
     */
    public String getToken(String label, Boolean useCache) {
        List<RobotsProperties.Robot> robots = RobotUtil.getRobot(label, robotsProperties.getRobot());
        try {
            return RobotUtil.getRobotToken(CollectionUtils.lastElement(robots), Optional.ofNullable(useCache).orElse(true));
        } catch (RobotException | ApiException e) {
            logger.error("error" + e);
            return StrUtil.EMPTY;
        }
    }
}
