# cocowwy-dingtalk-robot
cocowwy-dingtalk-start
钉钉机器人
目前实现功能
- 群机器人
  - 对钉钉群引入的机器人进行管理，可根据业务统一调度不同群的机器人发送消息
  - 在上述功能的基础上，实现根据手机号@指定人员
  - 在上述功能的情况下，实现消息延时发送，消息定时发送（coding...）
- 单聊机器人
  - 对单聊机器人进行集群管理，可细粒度的控制不同机器人根据手机号发送消息
  - 对token进行自定义缓存时间，调用者可以无需考虑开放平台对token的限流，api会自动对token进行自定义时长的缓存
  - 实现一个消息能被同一个分组内的机器人随机消费，按权重消费（coding...）
 
 使用方式，引入 cocowwy-dingtalk-start 
 在业务层注入**DingTalkApi**

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
点击群机器人--->机器人设置--->往下翻，复制webhook，安全设置点击加签，将密钥复制即可


在钉钉群加入钉钉机器人，并在机器人设置中将如上的**signature**和**webhook**放在**yml**配置文件里即可

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


