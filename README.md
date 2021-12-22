# cocowwy-dingtalk-robot
cocowwy-dingtalk-start
钉钉机器人
目前实现功能
- **群机器人**
  - 对钉钉群引入的机器人进行管理，可根据业务统一调度不同群的机器人发送消息
  - 在上述功能的基础上，实现根据手机号@指定人员
  - 在上述功能的情况下，实现消息延时发送，消息定时发送（coding...）
  - 由于群机器人有1分钟20条消息的上限，将开发短时间内多条消息拼成一条长消息发送（coding...）
- **单聊机器人**
  - 对多个单聊机器人进行统一的管理，可细粒度的控制不同机器人根据手机号发送消息
  - 对token进行自定义缓存时间，调用方可以无需考虑开放平台对token的限流，api会自动对token进行自定义时长的缓存
  - 实现一个消息能被同一个分组内的机器人随机消费，按权重消费（coding...）

配置文件：
```
cocowwy:
  dingding:
    robots:
      hooks:
        - label: 群1机器人
          signature: 钉钉群的机器人的signature
          webhook: 钉钉群的机器人的webhook
        - label: 群2机器人
          signature: 钉钉群的机器人的signature
          webhook: 钉钉群的机器人的webhook
      robot:
        - label: 钉钉机器人1
          agentId: 钉钉机器人的agentId
          appKey: 钉钉机器人appKey
          appSecret: 钉钉机器人appSecret
          tokenRefresh: 机器人Token的缓存时长，默认110min
        - label: 钉钉机器人2
          agentId: 钉钉机器人的agentId
          appKey: 钉钉机器人appKey
          appSecret: 钉钉机器人appSecret
```
hooks 数组是一套webhook机器人
robot 数组是一套钉钉机器人

### hooks配置方式
点击群机器人--->机器人设置--->往下翻--->复制webhook--->安全设置点击加签,将密钥复制即可
![image](https://user-images.githubusercontent.com/63331147/146709451-9e76d821-5012-4853-b433-760a9a26cc58.png)

### robot配置方式
钉钉开放平台--->点击开发者后台--->应用开发--->企业内部开发--->点击机器人,复制需要的参数即可
![image](https://user-images.githubusercontent.com/63331147/146709663-a2db71f5-226d-4332-90b0-ffb67f14f53e.png)

### 如何使用？
注入即可使用相应的api，钉钉机器人的通过label来对机器人进行区分
```
/**
 * @author cocowwy.cn
 * @create 2021-12-12-10:41
 */
@Component
public class Runner implements ApplicationRunner {
    @Autowired
    private DingTalkRobotApi dingTalkRobotApi;
    @Autowired
    private DingTalkGroupApi dingTalkGroupApi;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
            dingTalkRobotApi.sendMessageByPhonesAt("钉钉机器人1", Arrays.asList("186xxxxxxxx"), "message", "title");
        }
    }
}
```

### Jar包引用
```
将代码down下来后，打包dingtalk-robot-spring-boot-starter，引入即可
demo是测试用的，不需要理会~

<dependency>
    <groupId>cn.cocowwy</groupId>
    <artifactId>dingtalk-robot-cocowwy</artifactId>
    <version>1.0.6-SNAPSHOT</version>
</dependency>
```


