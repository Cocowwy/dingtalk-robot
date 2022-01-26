package cn.cocowwy.util;

import cn.cocowwy.config.RobotsHookProperties;
import cn.cocowwy.config.RobotsProperties;
import cn.cocowwy.dingtalk.rqrs.RobotSendRequest;
import cn.cocowwy.dingtalk.rqrs.RobotSendResponse;
import cn.cocowwy.processor.LargeMessageProcessor;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.dingtalkrobot_1_0.Client;
import com.aliyun.dingtalkrobot_1_0.models.BatchSendOTOHeaders;
import com.aliyun.dingtalkrobot_1_0.models.BatchSendOTORequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author cocowwy.cn
 * @create 2021-12-12-11:31
 */
public class RobotUtil extends StringPool {
    private static final Log logger = LogFactory.getLog(RobotUtil.class);
    private static final RestTemplate restTemplate = new RestTemplate();
    private static TimedCache<String, String> tokenCachePool = CacheUtil.newTimedCache(0L);
    private static Client client = null;

    // 机器人hook标识--->该大消息处理器
    private static Map<String, LargeMessageProcessor> largeMessageMap = new ConcurrentHashMap<>();

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

    public static String getRobotToken(RobotsProperties.Robot robot, Boolean useCache) throws ApiException {
        String token = null;
        if (useCache) {
            token = tokenCachePool.get(robot.getLabel(), Boolean.FALSE);
        }

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
            throw new RobotException("obtain token error , " + rsp.getErrmsg());
        }
        tokenCachePool.put(robot.getLabel(), rsp.getAccessToken(),
                Long.valueOf(robot.getTokenRefresh()) * 1000 * 60);
        return rsp.getAccessToken();
    }

    public static void sendMessage2Sb(RobotsProperties.Robot robot, List<String> phones, String message, String title) throws Exception {
        List<String> userIds = getUserIdsByPhones(robot, phones);

        JSONObject msg = new JSONObject();
        msg.put(TEXT, message);
        msg.put(TITLE, title);
        BatchSendOTOHeaders batchSendOTOHeaders = new BatchSendOTOHeaders();
        batchSendOTOHeaders.xAcsDingtalkAccessToken = getRobotToken(robot, true);
        BatchSendOTORequest batchSendOTORequest = new BatchSendOTORequest()
                .setRobotCode(robot.getAppKey())
                .setUserIds(userIds)
                .setMsgKey(SAMPLE_MARKDOWN)
                .setMsgParam(String.valueOf(msg));
        client.batchSendOTOWithOptions(batchSendOTORequest, batchSendOTOHeaders, new RuntimeOptions());
    }

    public static void sendMessageByUserIdsAt(RobotsProperties.Robot robot, List<String> userids, String message, String title) throws Exception {
        JSONObject msg = new JSONObject();
        msg.put(TEXT, message);
        msg.put(TITLE, title);
        BatchSendOTOHeaders batchSendOTOHeaders = new BatchSendOTOHeaders();
        batchSendOTOHeaders.xAcsDingtalkAccessToken = getRobotToken(robot, true);
        BatchSendOTORequest batchSendOTORequest = new BatchSendOTORequest()
                .setRobotCode(robot.getAppKey())
                .setUserIds(userids)
                .setMsgKey(SAMPLE_MARKDOWN)
                .setMsgParam(String.valueOf(msg));
        client.batchSendOTOWithOptions(batchSendOTORequest, batchSendOTOHeaders, new RuntimeOptions());
    }

    private static List<String> getUserIdsByPhones(RobotsProperties.Robot robot, List<String> phones) {
        // 根据手机号获取用户的userId
        List<String> userIds = new ArrayList<>();
        phones.forEach(phone -> {
            JSONObject getUerId = new JSONObject();
            getUerId.put(MOBILE, phone);
            ResponseEntity<String> getUserId = null;
            try {
                getUserId = restTemplate.postForEntity(GET_USERID_URL + getRobotToken(robot, true)
                        , getUerId, String.class);
                userIds.add(String.valueOf(JSONObject.parseObject(String.valueOf(JSONObject.parseObject(getUserId.getBody()).get("result"))).get("userid")));
            } catch (ApiException e) {
                // ingnore ..
                logger.error(phone + " get userid error ," + getUserId);
            }
        });
        return userIds;
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
        request.setMsgtype(TEXT);
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
     * 频繁发送消息
     * @param robot
     * @param message
     * @param phones
     */
    public synchronized static void sendLargeMessage(RobotsHookProperties.Robot robot, String message, List<String> phones) {

        LargeMessageProcessor processor = largeMessageMap.get(robot.getLabel());
        if (null == processor){
            largeMessageMap.putIfAbsent(robot.getLabel(), new LargeMessageProcessor(robot));
        }
        largeMessageMap.get(robot.getLabel()).addMessage(message, phones);
    }

    /**
     * 发送群消息并at所有人
     * @param robot
     * @param message
     */
    public static void sendHookMessageAtAll(RobotsHookProperties.Robot robot, String message) {
        RobotSendRequest request = new RobotSendRequest();
        request.setMsgtype(TEXT);
        RobotSendRequest.At at = new RobotSendRequest.At();
        at.setAtAll(true);
        at.setAtMobiles(null);
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

    public static void send(RobotsHookProperties.Robot robot, RobotSendRequest request) {
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
