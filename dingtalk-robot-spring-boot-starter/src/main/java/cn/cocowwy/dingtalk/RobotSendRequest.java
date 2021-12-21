package cn.cocowwy.dingtalk;


import java.util.List;

/**
 * @author Cocowwy
 * @since 2021/6/8
 */
public class RobotSendRequest {
    private String msgtype;
    private Text text;
    private At at;

    public static class Text {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static class At {
        private List<String> atMobiles;
        private Boolean isAtAll;

        public List<String> getAtMobiles() {
            return atMobiles;
        }

        public void setAtMobiles(List<String> atMobiles) {
            this.atMobiles = atMobiles;
        }

        public Boolean getAtAll() {
            return isAtAll;
        }

        public void setAtAll(Boolean atAll) {
            isAtAll = atAll;
        }
    }

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public At getAt() {
        return at;
    }

    public void setAt(At at) {
        this.at = at;
    }
}
