package cn.cocowwy.util;

import cn.cocowwy.config.RobotsHookProperties;
import cn.cocowwy.config.RobotsProperties;
import cn.cocowwy.constant.MessageTypeEnum;
import cn.cocowwy.dingtalk.rqrs.RobotLinkMessage;
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
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author cocowwy.cn
 * @create 2021-12-12-11:31
 */
public class RobotUtil extends StringPool {
    private static final Log logger = LogFactory.getLog(RobotUtil.class);
    /**
     * Token缓存
     */
    private static TimedCache<String, String> tokenCachePool = CacheUtil.newTimedCache(0L);
    /**
     * 机器人hook标识--->消息处理器
     */
    public static Map<String, LargeMessageProcessor> largeMessageMap = new ConcurrentHashMap<>();
    private static final RestTemplate restTemplate = new RestTemplate();
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

    /**
     * 获取机器人的token
     */
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

    /**
     * 根据手机号发送消息
     * @param robot
     * @param phones
     * @param message
     * @param title
     * @throws Exception
     */
    public static void sendMessage2Sb(RobotsProperties.Robot robot, List<String> phones, String message, String title) throws Exception {
        List<String> userIds = getUserIdsByPhones(robot, phones);
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        sendMessageByUserIds(robot, userIds, message, title);
    }

    /**
     * 根据 userId 发送
     * @param robot
     * @param userids
     * @param message
     * @param title
     * @throws Exception
     */
    public static void sendMessageByUserIds(RobotsProperties.Robot robot, List<String> userids, String message, String title) throws Exception {
        JSONObject msg = new JSONObject();
        msg.put(MessageTypeEnum.TEXT.getCode(), message);
        msg.put(TITLE, title);
        BatchSendOTOHeaders batchSendOTOHeaders = new BatchSendOTOHeaders();
        batchSendOTOHeaders.xAcsDingtalkAccessToken = getRobotToken(robot, true);

        // 钉钉平台限制 20 ，所以需要拆分
        splitList(userids, 20).forEach(it -> {
            BatchSendOTORequest batchSendOTORequest = new BatchSendOTORequest()
                    .setRobotCode(robot.getAppKey())
                    .setUserIds(it)
                    .setMsgKey(SAMPLE_MARKDOWN)
                    .setMsgParam(String.valueOf(msg));
            try {
                client.batchSendOTOWithOptions(batchSendOTORequest, batchSendOTOHeaders, new RuntimeOptions());
            } catch (Exception e) {
                logger.error("Send by userIds error , userIds " + "[" + Arrays.toString(it.toArray()) + "] ,err msg:", e);
            }
        });


    }

    /**
     * 向指定群机器人发送消息
     * send message to dingding group
     * @param label 机器人 Label
     * @param message  文本消息体
     * @return
     */
    public static void sendText(String label, String message, List<String> phones) throws Exception {
        List<RobotsHookProperties.Robot> robotGroup = RobotUtil.getRobotGroup(label, SpringUtil.getBean(RobotsHookProperties.class).getHooks());
        RobotUtil.sendHookMessage(CollectionUtils.lastElement(robotGroup), message, phones);
    }

    /**
     * 根据手机号获取用户ID
     * @param robot
     * @param phones
     * @return
     */
    public static List<String> getUserIdsByPhones(RobotsProperties.Robot robot, List<String> phones) {
        List<String> userIds = new ArrayList<>();

        phones.stream().filter(it -> !StringUtils.isEmpty(it)).forEach(phone -> {
            JSONObject getUerId = new JSONObject();
            getUerId.put(MOBILE, phone);
            ResponseEntity<String> getUserId = null;
            try {
                getUserId = restTemplate.postForEntity(GET_USERID_URL + getRobotToken(robot, true)
                        , getUerId, String.class);
                userIds.add(String.valueOf(JSONObject.parseObject(String.valueOf(JSONObject.parseObject(getUserId.getBody()).get("result"))).get("userid")));
            } catch (Exception e) {
                // ignore .. if get userId error ,just skip it !
                logger.error("Phone " + phone + " does not exist or gets an error, ignore it,err msg:", e);
            }
        });
        return userIds;
    }

    /**
     * 根据label获取群消息机器人
     * @param label
     * @param robots
     * @return
     */
    public static List<RobotsHookProperties.Robot> getRobotGroup(String label, List<RobotsHookProperties.Robot> robots) {
        List<RobotsHookProperties.Robot> robotGroup = robots.stream()
                .filter(robot -> robot.getLabel().equals(label)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(robotGroup)) {
            throw new RobotException("This robot does not exist , please configure it and try again");
        }
        return robotGroup;
    }

    /**
     * 根据label获取机器人
     * @param label
     * @param robots
     * @return
     * @throws RobotException
     */
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
    public static void sendHookMessage(RobotsHookProperties.Robot robot, String message, List<String> phones) throws Exception {
        RobotSendRequest request = new RobotSendRequest();
        request.setMsgtype(MessageTypeEnum.TEXT.getCode());
        RobotSendRequest.At at = new RobotSendRequest.At();
        if (null != phones) {
            phones = phones.stream().filter(it -> !StringUtils.isEmpty(it)).collect(Collectors.toList());
            at.setAtMobiles(phones);
            request.setAt(at);
        }
        at.setAtAll(Boolean.FALSE);
        RobotSendRequest.Text text = new RobotSendRequest.Text();
        text.setContent(message);
        request.setText(text);
        send(robot, request);
    }

    /**
     *  频繁的发送消息，自动拼接
     *  - 用于解决钉钉机器人1min20条消息的限制
     * @param robot
     * @param message
     * @param phones
     */
    public synchronized static void sendFrequentlyMessage(RobotsHookProperties.Robot robot, String message, List<String> phones) {
        LargeMessageProcessor processor = largeMessageMap.get(robot.getLabel());
        if (null == processor) {
            // 必须先判空，直接putIfAbsent依然会产生新的线程，因为put前会先new LargeMessageProcessor
            largeMessageMap.putIfAbsent(robot.getLabel(), new LargeMessageProcessor(robot));
        }
        largeMessageMap.get(robot.getLabel()).addMessage(message,
                Optional.ofNullable(phones).
                        orElse(new ArrayList<>()).stream().filter(it -> !StringUtils.isEmpty(it)).collect(Collectors.toList()));
    }

    /**
     * 发送群消息并at所有人
     * @param robot
     * @param message
     */
    public static void sendHookMessageAtAll(RobotsHookProperties.Robot robot, String message) throws Exception {
        RobotSendRequest request = new RobotSendRequest();
        request.setMsgtype(MessageTypeEnum.TEXT.getCode());
        RobotSendRequest.At at = new RobotSendRequest.At();
        at.setAtAll(true);
        at.setAtMobiles(new ArrayList<>());
        request.setAt(at);
        RobotSendRequest.Text text = new RobotSendRequest.Text();
        text.setContent(message);
        request.setText(text);
        send(robot, request);
    }

    /**
     * 发送链接消息
     */
    public static void sendLinkMessageByPhone(RobotsProperties.Robot robot, String phone, String title, String text, String messageUrl, String picUrl) throws Exception {
        RobotLinkMessage link = new RobotLinkMessage();
        link.setTitle(title);
        link.setText(text);
        link.setPicUrl(picUrl);
        link.setMessageUrl(messageUrl);

        BatchSendOTOHeaders batchSendOTOHeaders = new BatchSendOTOHeaders();
        batchSendOTOHeaders.xAcsDingtalkAccessToken = getRobotToken(robot, true);
        BatchSendOTORequest batchSendOTORequest = new BatchSendOTORequest()
                .setRobotCode(robot.getAppKey())
                .setUserIds(getUserIdsByPhones(robot, Collections.singletonList(phone)))
                .setMsgKey(MessageTypeEnum.SAMPLE_LINK.getCode())
                .setMsgParam(JSONObject.toJSONString(link));
        client.batchSendOTOWithOptions(batchSendOTORequest, batchSendOTOHeaders, new RuntimeOptions());
    }

    /**
     * 自定义发送
     */
    public static void sendMessageByCustomHook(String webhook, String signature, String message, Boolean atAll, List<String> phones) throws Exception {
        RobotsHookProperties.Robot robot = new RobotsHookProperties.Robot();
        robot.setSignature(signature);
        robot.setWebhook(webhook);

        if (atAll == null || !atAll) {
            sendHookMessage(robot, message, phones);
        } else {
            sendHookMessageAtAll(robot, message);
        }
    }

    /**
     * 获取签名
     * @param robot
     * @param timestamp
     * @return
     */
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

    public static void send(RobotsHookProperties.Robot robot, RobotSendRequest request) throws Exception {
        Long timestamp = System.currentTimeMillis();
        String sign = obtainSign(robot, timestamp);
        String endpoint = String.format(robot.getWebhook() + URL_SUFFIX, timestamp, sign);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<RobotSendRequest> httpEntity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, httpEntity, String.class);
        if (response.getStatusCode() != HttpStatus.OK
                || Objects.requireNonNull(JSONObject.parseObject(response.getBody(), RobotSendResponse.class)).getErrcode() != 0)
            throw new RobotException("failed to send dingding message, response：" + response.getBody());
    }

    /**
     * 集合拆分
     */
    public static <T> List<List<T>> splitList(List<T> list, int len) {
        if (list == null || list.size() == 0 || len < 1) {
            return null;
        }
        List<List<T>> result = new ArrayList<>();
        int size = list.size();
        int count = (size + len - 1) / len;
        for (int i = 0; i < count; i++) {
            List<T> subList = list.subList(i * len, (Math.min((i + 1) * len, size)));
            result.add(subList);
        }
        return result;
    }

}
