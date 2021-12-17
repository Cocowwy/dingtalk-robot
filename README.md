# cocowwy-dingtalk-robot
cocowwy-dingtalk-start
钉钉群聊机器人
目前实现功能
  - 对同一个分组内的机器人，批量发送指定的消息
  - 在上述功能的基础上，实现根据手机号@指定人员
  - 在上述功能的情况下，实现消息延时发送，消息定时发送（coding...）
  - 实现一个消息能被同一个分组内的机器人随机消费，按权重消费（coding...）
 
 使用方式，引入 cocowwy-dingtalk-start 
 在业务层注入**DingTalkApi**

配置文件：

```
cocowwy:
  dingding:
    robots:
      - label: label1
        signature: 群a的机器人的signature
        webhook: 群a的机器人的Webhook
      - label: label1
        signature: 群b的机器人的signature
        webhook: 群b的机器人的Webhook
      - label: label2
        signature: 群c的机器人的signature
        webhook: 群c的机器人的Webhook
```
robots下是一个机器人数组，可以放置任意多的机器人，
如果将a，b，c机器人设置成同一个label，那么消息推送则会对同一个label组的机器人进行推送

### 钉钉群引入机器人的方式
![image](https://user-images.githubusercontent.com/63331147/146314959-2fb47b45-1e85-4d7e-a2f9-ac1824969ae0.png)

在钉钉群加入钉钉机器人，并在机器人设置中将如上的**signature**和**webhook**放在**yml**配置文件里即可


## 测试
```
/**
 * @author cocowwy.cn
 * @create 2021-12-12-10:41
 */
@Component
public class Runner implements ApplicationRunner {
    @Autowired
    private DingTalkApi dingTalkApi;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 发送的group  消息文案  群@的手机号
        dingTalkApi.sendTextAndAt("label1", "测试群发，@", Arrays.asList("群内某人手机号"));
    }
}
```
<img width="250" alt="ac8e4f392c534bfce660392c18cc2befa9ac0e2972bb3209543f0ac08e99cbe0QzpcVXNlcnNcY29jb3d3eVxBcHBEYXRhXFJvYW1pbmdcRGluZ1RhbGtcMjA1Nzg0ODY1OV92MlxJbWFnZUZpbGVzXDE2Mzk2MzI3MDcyODdfREU4NTZEQ0QtMUI1NC00MDFmLTgwNjYtOEY1REUwNjc1OEUxLnBuZw==" src="https://user-images.githubusercontent.com/63331147/146315825-4145b52a-2ff7-4bf2-88d2-1e4b650c9ef8.png" height="100">

<img width="250" alt="472e2fd1b6f6f4a5ee1c0fd6f09d12af404ebed51cea1b47eca999d6ee52055aQzpcVXNlcnNcY29jb3d3eVxBcHBEYXRhXFJvYW1pbmdcRGluZ1RhbGtcMjA1Nzg0ODY1OV92MlxJbWFnZUZpbGVzXDE2Mzk2MzI3MDcyNThfNTg4NzBCMkMtRTNDQy00NWU2LUE1MUQtMDBERDAyNTA1N0Q4LnBuZw==" src="https://user-images.githubusercontent.com/63331147/146315826-2cb0d8ed-8dc0-4f37-bd8e-8fb18cf5b776.png" height="100">
这两个机器人都是label1 ，所以会同时对这两个机器人推送消息

### Jar包引用
```
将代码down下来后，打包dingtalk-robot-spring-boot-starter，引入即可
demo是测试用的，不需要理会~

 <dependency>
     <groupId>cn.cocowwy</groupId>
     <artifactId>dingtalk-robot-spring-boot-starter</artifactId>
     <version>1.0-SNAPSHOT</version>
 </dependency>
```


