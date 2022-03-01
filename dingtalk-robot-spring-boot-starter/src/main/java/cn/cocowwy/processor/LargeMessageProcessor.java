package cn.cocowwy.processor;

import cn.cocowwy.config.RobotsHookProperties;
import cn.cocowwy.util.RobotUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 每个webhook所对应的群机器人都有一个线程处理大消息
 *   - 借鉴的nacos的NotifyCenter源码设计
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
     * 消息的默认缓存发送时长
     */
    private volatile int DEFAULT_CACHE_MESSAGE_TIME = 10 * 1000;

    /**
     * DEFAULT_CACHE_MESSAGE_TIME * monitor = 监听器空闲等待时间
     * 10 min 内未获取到新的消息时，销毁执行器和监听器
     */
    private volatile Integer monitor = new Integer(60);

    private static final String MKD_ENTER_DOUBLE = "  \n\n";

    private String processorThreadNameSbufix = "@@Processor";
    private String listenerThreadNameSbufix = "@@Listener";

    public  LargeMessageProcessor(RobotsHookProperties.Robot robot) {
        this.hookLabel = robot.getLabel();
        this.robot = robot;
        this.processorThreadNameSbufix = this.hookLabel + processorThreadNameSbufix;
        this.listenerThreadNameSbufix = this.hookLabel + listenerThreadNameSbufix;
        super.start();
    }

    /**
     * 添加消息
     * @param message
     * @param atPhones
     */
    public void addMessage(String message, List<String> atPhones) {
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
        Thread.currentThread().setName(processorThreadNameSbufix);
        LOGGER.info("Processor [{}] is running ", processorThreadNameSbufix);
        shutdown.set(false);

        listener();

        while (!shutdown.get()) {
            // 如果没有消息则阻塞
            String take = null;
            try {
                take = messagePool.poll(1L, TimeUnit.SECONDS);
                // 增加指定等待时长，便于通过监听shutdown状态来达到销毁这个线程
                if (take == null) {
                    continue;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            largeMessage.append(take);
            largeMessage.append(MKD_ENTER_DOUBLE);
        }

        RobotUtil.largeMessageMap.remove(hookLabel);
        LOGGER.info("destroy processor [{}]", Thread.currentThread().getName());
    }

    private void listener() {
        final Runnable job = () -> {
            LOGGER.info("Frequently listener [{}] is running", listenerThreadNameSbufix);

            int originalMonitor = monitor;

            while (!shutdown.get()) {
                try {
                    Thread.sleep(DEFAULT_CACHE_MESSAGE_TIME);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage());
                }

                if (largeMessage == null || largeMessage.length() == 0) {
                    // 销毁消息处理器以及监听器线程
                    if (monitor.equals(0)) {
                        RobotUtil.largeMessageMap.remove(hookLabel);
                        shutdown.set(true);
                        LOGGER.info("destroy listener [{}]", Thread.currentThread().getName());
                        return;
                    }

                    monitor--;
                    continue;
                }
                // 发送消息
                try {
                    RobotUtil.sendText(hookLabel, largeMessage.toString(), ats.stream().collect(Collectors.toList()));
                } catch (Exception e) {
                    LOGGER.error("send frequently msg error ,", e);
                }
                // 这里如果直接清空的话 并发情况下会消息丢失，只能截取字段
                largeMessage = largeMessage.delete(0, largeMessage.length());

                monitor = originalMonitor;
            }
        };
        new Thread(job, listenerThreadNameSbufix).start();
    }
}
