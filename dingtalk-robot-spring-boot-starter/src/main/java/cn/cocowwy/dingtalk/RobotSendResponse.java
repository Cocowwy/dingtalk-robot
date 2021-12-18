package cn.cocowwy.dingtalk;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Cocowwy
 * @since 2021/6/8
 */
@Data
public class RobotSendResponse {
    @JsonProperty("errcode")
    private Long errcode;
    @JsonProperty("errmsg")
    private String errmsg;
}
