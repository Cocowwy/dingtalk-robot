package cn.cocowwy.util;

import cn.cocowwy.config.RobotsProperties;
import cn.cocowwy.dingtalk.RobotSendRequest;
import cn.cocowwy.dingtalk.RobotSendResponse;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cocowwy.cn
 * @create 2021-12-12-11:31
 */
public class RobotUtil {
    private static final String URL_SUFFIX = "&timestamp=%s&sign=%s";

    private static final Log logger = LogFactory.getLog(RobotUtil.class);

    private static final RestTemplate restTemplate = new RestTemplate();

    public static List<RobotsProperties.Robot> getRobotGroup(String label, List<RobotsProperties.Robot> robots) throws Exception {
        List<RobotsProperties.Robot> robotGroup = robots.stream()
                .filter(robot -> robot.getLabel().equals(label)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(robotGroup)) {
            logger.error("This robot does not exist , please configure it and try again");
        }
        return robotGroup;
    }

    public static void sendMessage(RobotsProperties.Robot robot, String message, List<String> phones) {
        RobotSendRequest request = new RobotSendRequest();
        request.setMessageType("text");
        if (null != phones) {
            RobotSendRequest.At at = new RobotSendRequest.At();
            at.setAtMobiles(phones);
            at.setAtAll(Boolean.FALSE);
            request.setAt(at);
        }
        RobotSendRequest.Text text = new RobotSendRequest.Text();
        text.setContent(message);
        request.setText(text);
        send(robot, request);
    }

    public static void sendMessageAtAll(RobotsProperties.Robot robot, String message) {
        RobotSendRequest request = new RobotSendRequest();
        request.setMessageType("text");
        RobotSendRequest.At at = new RobotSendRequest.At();
        at.setAtAll(Boolean.TRUE);
        request.setAt(at);
        RobotSendRequest.Text text = new RobotSendRequest.Text();
        text.setContent(message);
        request.setText(text);
        send(robot, request);
    }

    private static String obtainSign(RobotsProperties.Robot robot, Long timestamp) {
        String stringToSign = timestamp + "\n" + robot.getSignature();
        Mac mac;
        String sign = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(robot.getSignature().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
        } catch (Exception e) {
            logger.error("Calculating the signature required by the Dingding robot is abnormal", e);
        }

        return sign;
    }

    private static void send(RobotsProperties.Robot robot, RobotSendRequest request) {
        Long timestamp = System.currentTimeMillis();
        String sign = obtainSign(robot, timestamp);
        String endpoint = String.format(robot.getWebhook() + URL_SUFFIX, timestamp, sign);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<RobotSendRequest> httpEntity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, httpEntity, String.class);
        if (response.getStatusCode() != HttpStatus.OK
                || JSONObject.parseObject(response.getBody(), RobotSendResponse.class).getErrcode() != 0) {
            logger.error("failed to send dingding message, response：" + response.getBody());
        }
    }

}
