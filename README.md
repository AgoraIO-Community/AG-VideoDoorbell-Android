# 功能说明
该应用是基于声网RTC引擎的门铃监控Demo程序，主要提供以下功能：
1. 账号系统：提供了基本的 用户账号注册、登录、登出 功能
2. 设备管理：应用绑定、解绑设备，查询已绑定设备列表
3. 呼叫系统：APP端主动呼叫设备；接听设备端来电
4. 告警管理：告警信息查询和接收

# 调试说明
1. 开发者需要更新自己在声网官网申请的appId，替换到如下配置文件中"AGORA_APPID" 字段
   app/src/main/res/values/strings.xml
   agoraCallkitSDK/src/main/res/values/strings.xml