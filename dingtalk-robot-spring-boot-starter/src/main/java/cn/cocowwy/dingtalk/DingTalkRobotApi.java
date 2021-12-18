package cn.cocowwy.dingtalk;

import cn.cocowwy.config.RobotsProperties;
import cn.cocowwy.util.RobotUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

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
     * 向指定了label的机器人
     * 根据手机号群发消息
     */
    public void sendMessageAt(String label, String message, List<StringWriter> phones) {
        List<RobotsProperties.Robot> robot = RobotUtil.getRobot(label, robotsProperties.getRobot());

        
    }
}
