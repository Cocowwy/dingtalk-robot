package cn.cocowwy.util;

import cn.cocowwy.RobotException;
import cn.cocowwy.config.RobotsHookProperties;
import cn.cocowwy.config.RobotsProperties;
import cn.cocowwy.dingtalk.RobotSendRequest;
import cn.cocowwy.dingtalk.RobotSendResponse;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.dingtalkrobot_1_0.Client;
import com.aliyun.dingtalkrobot_1_0.models.BatchSendOTOHeaders;
import com.aliyun.dingtalkrobot_1_0.models.BatchSendOTORequest;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.taobao.api.ApiException;
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

    private static final String TOKEN_URL = "https://oapi.dingtalk.com/gettoken";

    private static final Log logger = LogFactory.getLog(RobotUtil.class);

    private static final RestTemplate restTemplate = new RestTemplate();

    private static TimedCache<String, String> tokenCachePool = CacheUtil.newTimedCache(0L);

    private static Client client = null;

    static {
        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        try {
            client = new Client(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getRobotToken(RobotsProperties.Robot robot) throws ApiException {
        String token = tokenCachePool.get(robot.getLabel(), Boolean.FALSE);
        if (null != token) {
            return token;
        }

        DingTalkClient client = new DefaultDingTalkClient(TOKEN_URL);
        OapiGettokenRequest req = new OapiGettokenRequest();
        req.setAppkey(robot.getAppKey());
        req.setAppsecret(robot.getAppSecret());
        req.setHttpMethod("GET");
        OapiGettokenResponse rsp = null;
        rsp = client.execute(req);

        if (!rsp.isSuccess() || rsp.getErrcode() != 0) {
            throw new RobotException("request token error , " + rsp.getErrmsg());
        }
        tokenCachePool.put(robot.getLabel(), rsp.getAccessToken(),
                Long.valueOf(robot.getTokenRefresh()) * 1000 * 60);
        return rsp.getBody();
    }

    public static void sendMessage2Sb(RobotsProperties.Robot robot, List<String> phones, String message) throws Exception {
        BatchSendOTOHeaders batchSendOTOHeaders = new BatchSendOTOHeaders();
        batchSendOTOHeaders.xAcsDingtalkAccessToken = getRobotToken(robot);
        BatchSendOTORequest batchSendOTORequest = new BatchSendOTORequest()
                .setRobotCode(robot.getAppKey())
                .setUserIds(phones)
                .setMsgKey(robot.getAppKey())
                .setMsgParam("{\"text\": " + message + "}");
        client.batchSendOTOWithOptions(batchSendOTORequest, batchSendOTOHeaders, new RuntimeOptions());
    }

    public static List<RobotsHookProperties.Robot> getRobotGroup(String label, List<RobotsHookProperties.Robot> robots) {
        List<RobotsHookProperties.Robot> robotGroup = robots.stream()
                .filter(robot -> robot.getLabel().equals(label)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(robotGroup)) {
            throw new RobotException("This robot does not exist , please configure it and try again");
        }
        return robotGroup;
    }

    public static List<RobotsProperties.Robot> getRobot(String label, List<RobotsProperties.Robot> robots) throws RobotException {
        List<RobotsProperties.Robot> robot = robots.stream()
                .filter(r -> r.getLabel().equals(label)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(robot)) {
            throw new RobotException("This robot does not exist , please configure it and try again");
        }
        return robot;
    }

    /**
     * 发送群消息
     * @param robot
     * @param message
     * @param phones
     */
    public static void sendHookMessage(RobotsHookProperties.Robot robot, String message, List<String> phones) {
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

    /**
     * 发送群消息并at所有人
     * @param robot
     * @param message
     */
    public static void sendHookMessageAtAll(RobotsHookProperties.Robot robot, String message) {
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

    private static String obtainSign(RobotsHookProperties.Robot robot, Long timestamp) {
        String stringToSign = timestamp + "\n" + robot.getSignature();
        Mac mac;
        String sign = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(robot.getSignature().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
        } catch (Exception e) {
            throw new RobotException("Calculating the signature required by the Dingding robot is abnormal");
        }

        return sign;
    }

    private static void send(RobotsHookProperties.Robot robot, RobotSendRequest request) {
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
