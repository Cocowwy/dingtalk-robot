package cn.cocowwy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Cocowwy
 * @create 2022-05-05-16:52
 */
@ConfigurationProperties(prefix = "dingding.robots")
public class RobotsGlobalProperties {
    /**
     * 是否启用
     */
    private Boolean enable;

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }
}
