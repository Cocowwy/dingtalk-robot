package cn.cocowwy.constant;

/**
 * @author cocowwy.cn
 * @create 2022-03-03-17:37
 */
public enum MessageTypeEnum {
    TEXT("text"),
    LINK("link"),
    SAMPLE_LINK("sampleLink");

    private String code;

    MessageTypeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
