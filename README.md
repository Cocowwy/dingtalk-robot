# :robot:cocowwy-dingtalk-robot
<a href="https://github.com/Cocowwy/dingtalk-robot"><img src="https://badgen.net/badge/⭐/GitHub/blue" alt="github"></a>
![License](https://img.shields.io/badge/license-Apache--2.0-green.svg)   
![image](https://user-images.githubusercontent.com/63331147/153790868-79936b70-7008-484b-a749-dac3f304d257.png)
## 说明  
拆箱即用，以便更灵活的将钉钉机器人快速的嵌入到业务里，用来应付在复杂场景里对不同业务类型的机器人来按需调用，同时会提供一些应付更复杂的业务场景的功能  
- **群机器人**
  - 对钉钉群引入的机器人进行管理，可根据业务统一调度不同群的机器人发送消息
  - 在上述功能的基础上，实现根据手机号@指定人员
  - 在上述功能的情况下，实现消息延时发送，消息定时发送（coding...）
  - **由于群机器人有1分钟20条消息的上限，实现机器人对短时间内（10s）的并发消息按顺序拼接成长消息体，解决上限问题**
- **单聊机器人**
  - 对多个单聊机器人进行统一的管理，可细粒度的控制不同机器人根据手机号发送消息
  - 对token进行自定义缓存时间，调用方可以无需考虑钉钉开放平台对token获取接口的限流，api会自动对token进行自定义时长的缓存
  - 实现一个消息能被同一个分组内的机器人随机消费，按权重消费（coding...）
  - 发送带有link链接的单聊机器人

## pom.xml：
```
<dependency>
  <groupId>cn.cocowwy</groupId>
  <artifactId>cocowwy-dingtalk-robot</artifactId>
  <version>1.0.4-RELEASE</version>
</dependency>
```

## yml：
```yml
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
- hooks 是webhook机器人数组，你可以塞一堆机器人来控制对每个机器人在不同群发的消息
- robot 是钉钉机器人数组,你也可以设置一堆机器人来私聊（骚扰）不同的人


### 如何使用？
注入即可使用相应的API，您只需在调用处传入你所需要使用的机器人的**label**即可实现调用
- **hooks** 群机器人需要注入**DingTalkGroupApi**来进行使用
- **robot** 单聊机器人需要注入**DingTalkRobotApi**来进行使用

### 使用Demo
```java
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
            // 发送群消息
            dingTalkGroupApi.sendText("群1机器人","群机器人消息" );
            
            // 发送群消息，并且@某人
            dingTalkGroupApi.sendTextAndAt("群1机器人", "消息体","Arrays.asList("需要@的手机号，不需要则传空数组")");
            
                       
            // 多消息自动拼接（群消息） 
            dingTalkGroupApi.sendFrequentlyMessage("群1机器人", "多消息自动拼接", Arrays.asList("需要@的手机号，不需要则传空数组"));
    
            // 发送机器人私聊消息
            dingTalkRobotApi.sendMessageByPhonesAt("钉钉机器人1", Arrays.asList("需要@的手机号，不需要则传空数组"), "根据手机号私聊", "标题");
        }
    }
}
```
- DingTalkGroupApi
   - getHooks 获取所有在线的群机器人label
   - sendText 向指定label的群机器人发送消息
   - sendTextByPhones 向指定label的群机器人发送消息，并且根据手机号@
   - sendTextAndAtAll 向指定群机器人发送消息，并且@所有人
   - sendFrequentlyMessage 解决群机器人1分钟的20条消息限制，该接口会在短时间内的消息自动拼接成长消息，默认缓存拼接时间为10s
- DingTalkRobotApi
   - getRobots 获取单聊机器人列表
   - sendMessageByPhonesAt 根据手机号向指定人发送消息
   - sendMessageByUserIdsAt 根据用户id给指定人发送消息
   - getToken 查询指定机器人的token
   - sendLinkMessageByPhones 发送link消息

### hooks配置方式
**点击群机器人** ---> **机器人设置** ---> **往下翻** ---> **复制webhook** ---> **安全设置点击加签,将密钥复制即可**

![image](https://user-images.githubusercontent.com/63331147/146709451-9e76d821-5012-4853-b433-760a9a26cc58.png)

### robot配置方式
**钉钉开放平台** ---> **点击开发者后台** ---> **应用开发** ---> **企业内部开发** ---> **点击机器人,复制需要的参数即可** 

![image](https://user-images.githubusercontent.com/63331147/146709663-a2db71f5-226d-4332-90b0-ffb67f14f53e.png)

