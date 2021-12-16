package cn.cocowwy.dingtalk;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Cocowwy
 * @since 2021/6/8
 */
public class RobotSendResponse {
    @JsonProperty("errcode")
    private Long errorCode;
    @JsonProperty("errmsg")
    private String errorMessage;

    public Long getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Long errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
