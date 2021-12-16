package cn.cocowwy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cocowwy.cn
 * @create 2021-12-12-10:28
 */
@ConfigurationProperties(prefix = "cocowwy.dingding")
public class RobotsProperties {
    private List<Robot> robots = new ArrayList<>();

    public List<Robot> getRobots() {
        return robots;
    }

    public void setRobots(List<Robot> robots) {
        this.robots = robots;
    }

    public static class Robot {
        private String label;
        private String signature;
        private String webhook;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public String getWebhook() {
            return webhook;
        }

        public void setWebhook(String webhook) {
            this.webhook = webhook;
        }
    }
}
