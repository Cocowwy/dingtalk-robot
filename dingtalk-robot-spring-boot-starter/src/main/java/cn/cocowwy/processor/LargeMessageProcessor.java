package cn.cocowwy.processor;

import cn.cocowwy.config.RobotsHookProperties;
import cn.cocowwy.dingtalk.rqrs.RobotSendRequest;
import cn.cocowwy.util.RobotUtil;
import cn.cocowwy.util.StringPool;
import cn.hutool.core.collection.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 每个webhook所对应的群机器人都有一个线程处理大消息
 *   - 借鉴的nacos的消息通知机制源码设计
 * @author cocowwy.cn
 * @create 2022-01-01-15:30
 */
public class LargeMessageProcessor extends Thread {
    protected static final Logger LOGGER = LoggerFactory.getLogger(LargeMessageProcessor.class);

    private volatile String hookLabel;

    private RobotsHookProperties.Robot robot;

    private BlockingQueue<String> messagePool = new LinkedBlockingDeque<>();

    private volatile AtomicBoolean shutdown = new AtomicBoolean(true);

    private StringBuffer largeMessage = new StringBuffer();

    private Set<String> ats = new ConcurrentHashSet();

    /**
     * 消息的默认缓存时长
     */
    private volatile int DEFAULT_CACHE_MESSAGE_TIME = 10 * 1000;

    private static final String MKD_ENTER_DOUBLE = "  \n\n";

    public LargeMessageProcessor(RobotsHookProperties.Robot robot) {
        this.hookLabel = robot.getLabel();
        this.robot = robot;
        super.start();
    }

    /**
     * 添加消息
     * @param message
     * @param atPhones
     */
    public void addMessage(String message, List<String> atPhones) {
        System.out.println(message);
        try {
            messagePool.put(message);
        } catch (InterruptedException e) {
            LOGGER.error("添加消息异常，{}", e.getMessage());
        }
        ats.addAll(new HashSet<>(atPhones.size()));
    }

    /**
     * 销毁
     */
    void shutdown() {
        messagePool.clear();
        ats.clear();
        RobotUtil.largeMessageMap.remove(hookLabel);
        this.shutdown.compareAndSet(false, true);
    }

    @Override
    public void run() {
        shutdown.set(false);

        listener();

        while (!shutdown.get()) {
            // 如果没有消息则阻塞
            String take = null;
            try {
                take = messagePool.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            largeMessage.append(take);
            largeMessage.append(MKD_ENTER_DOUBLE);
        }
    }

    private void listener() {
        final Runnable job = () -> {
            LOGGER.info("add frequently listener [{}]", hookLabel);
            while (!shutdown.get()) {
                try {
                    Thread.sleep(DEFAULT_CACHE_MESSAGE_TIME);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage());
                }

                // 发送消息
                RobotSendRequest request = new RobotSendRequest();
                request.setMsgtype(StringPool.TEXT);
                if (!CollectionUtils.isEmpty(ats)) {
                    RobotSendRequest.At at = new RobotSendRequest.At();
                    at.setAtMobiles(ats.stream().collect(Collectors.toList()));
                    at.setAtAll(Boolean.FALSE);
                    request.setAt(at);
                }
                RobotSendRequest.Text text = new RobotSendRequest.Text();
                String msg = largeMessage.toString();
                text.setContent(msg);
                request.setText(text);
                RobotUtil.send(robot, request);
                // 这里如果直接清空的话 并发情况下会消息丢失
                largeMessage = largeMessage.delete(0, msg.length());
            }
        };
        new Thread(job).start();
    }
}
