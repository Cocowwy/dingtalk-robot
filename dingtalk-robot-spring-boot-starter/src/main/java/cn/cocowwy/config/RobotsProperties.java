package cn.cocowwy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cocowwy.cn
 * @create 2021-12-12-17:13
 */
@ConfigurationProperties(prefix = "dingding.robots", ignoreInvalidFields = true)
public class RobotsProperties {
    private List<Robot> robot = new ArrayList<>();

    public List<Robot> getRobot() {
        return robot;
    }

    public void setRobot(List<Robot> robot) {
        this.robot = robot;
    }

    public static class Robot {
        private String label;
        private String agentId;
        private String appKey;
        private String appSecret;
        private Integer tokenRefresh = 110;

        public String getAgentId() {
            return agentId;
        }

        public void setAgentId(String agentId) {
            this.agentId = agentId;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getAppKey() {
            return appKey;
        }

        public void setAppKey(String appKey) {
            this.appKey = appKey;
        }

        public String getAppSecret() {
            return appSecret;
        }

        public void setAppSecret(String appSecret) {
            this.appSecret = appSecret;
        }

        public Integer getTokenRefresh() {
            return tokenRefresh;
        }

        public void setTokenRefresh(Integer tokenRefresh) {
            this.tokenRefresh = tokenRefresh;
        }
    }
}
