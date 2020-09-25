# 开发环境配置
### 1. 工程配置
- gradle.properties文件中添加
~~~
android.useAndroidX=true
android.enableJetifier=true
~~~
### 2. 依赖配置
- 项目级build.gradle文件中添加
~~~
maven { url "https://jitpack.io" }
maven { url 'https://dl.bintray.com/xhmm/maven' }
~~~
- module的build.gradle文件中添加
~~~
android {
    //……
    // java8
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    //……
}
dependencies {
    implementation 'mobile.xinhuamm:mobile_rtc:2.2.8'
}
~~~
### 3. SDK配置
~~~
RtcClientOverallSetting.getInstance().setCommonHost(BuildConfig.RTC_HOST_COMMON);
RtcClientOverallSetting.getInstance().setSocketHost(BuildConfig.RTC_HOST_SOCKET);
~~~
可以通过此方法配置鉴权地址和WebSocket连接地址，配置完后打开拨打或接听页面的基本参数就不需要配置了，若不通过此方法配置也可以在打开拨打或接听页面方法参数中直接配置。具体查看调用方式。
# SDK调用方式
- 接听
~~~
// 登录用户自己的userId(必传)
String userId = DataHelper.getStringSF(context, KEY_SELF_ID, "");
// 登录用户昵称
String userName = DataHelper.getStringSF(context, KEY_SELF_NAME, "");
// 用户头像，没有可以不传
String userHead = "";
// 用户信息token，没有可以不传
String userToken = "";
// 主持人/发起者 id (必传)
String hostId = rtcInfo.getHostId();
// 单人 主持人/发起者名称 多人 现场标题
String hostName = rtcInfo.getLiveTitle();
// 主持人/发起者 头像
String hostHead = rtcInfo.getIcon();
// 房间号(必传)
String roomId = rtcInfo.getRoomId();
// 房间名称
String roomName = rtcInfo.getLiveTitle();
// 报道id
String reportId = rtcInfo.getReportId();
// 拨打时间点
long callTime = rtcInfo.getStartDate();
// 是否为单人通话
boolean isSingleCall = rtcInfo.isSingleCall();
RtcClient.answer(context)
        .isSingleCall(isSingleCall)
        .callTime(callTime)
        .answerBeforeCallback(new RtcAnswerBeforeCallback() {
            @Override
            public boolean canAnswer() {
                // 编写业务层逻辑，优先判断App是否可以进入接听电话页面
                // 还可以在此处处理接听前的业务逻辑，比如：暂停音视频播放
                return true;
            }
            @Override
            public void roomError() {
                // 房间不存在或者接口异常
                // 若成功进入接听页面代表房间正常存在，则不会走此方法
                // 可用于业务层逻辑判断
            }
        })
        .commonHost(BuildConfig.RTC_HOST_COMMON)
        .socketHost(BuildConfig.RTC_HOST_SOCKET)
        .userId(isSingleCall ? userId : PushManager.getInstance().getClientid(context))
        .userName(userName)
        .userHead(userHead)
        .userToken(userToken)
        .hostId(hostId)
        .hostName(hostName)
        .hostHead(hostHead)
        .reportId(reportId)
        .roomId(roomId)
        .roomName(roomName)
        .build()
        .rtc();
~~~
- 拨打
~~~
RtcClient.call(context)
        .tenantId(BuildConfig.RTC_TENANT_ID)
        .commonHost(BuildConfig.RTC_HOST_COMMON)
        .socketHost(BuildConfig.RTC_HOST_SOCKET)
        .userId(userId)
        .userName(userName)
        .userHead("")
        .userToken("")
        .callId(callUserId)
        .callCid(callUserClientId)
        .callName(callUserName)
        .callHead("")
        .build()
        .rtc();
~~~
# API说明
- 启动接听页面
~~~
/**
 * 启动接听页面
 *
 * @param context 上下文
 * @return 构建者
 */
public static AnswerBuilder answer(Context context) {
    return new AnswerBuilder(context);
}
~~~
- 启动拨打页面
~~~
/**
 * 启动拨打页面
 *
 * @param context 上下文
 * @return 构建者
 */
public static CallBuilder call(Context context) {
    return new CallBuilder(context);
}
~~~
- 获取通话状态
~~~
/**
 * 获取通话状态
 *
 * @return 通话状态
 */
public static RtcChatStatus getCallState() {
    return RtcManager.getInstance().getRtcStatus();
}
~~~
~~~
public enum RtcChatStatus {
    /**
     * 空闲状态
     */
    CHAT_IDLE,
    /**
     * 待接听中
     */
    ANSWER_ING,
    /**
     * 拨打中
     */
    CALL_ING,
    /**
     * 通话 中
     */
    CHAT_ON,
    /**
     * 结束通话
     */
    CHAT_OFF,
    /**
     * 通话异常
     */
    CHAT_ERROR,
    /**
     * 连接中
     */
    CHAT_CONNECTION
}
~~~
- 判断通话是否过期
~~~
/**
 * 判断通话是否过期
 * 默认3分钟
 *
 * @param callSendWhen 拨打时间
 * @return 是否过期
 */
public static boolean callTimeOut(long callSendWhen) {
    return System.currentTimeMillis() - callSendWhen > timeOut;
}
~~~
- 设置通话是否过期超时时长
~~~
/**
 * 设置超时时长
 *
 * @param timeOut 超时
 */
public static void setTimeOut(long timeOut) {
    RtcClient.timeOut = timeOut;
}
~~~
- 打开通话页面
~~~
/**
 * 打开通话页面
 */
public void rtc() {
    if (null == rtcParam) {
        return;
    }
    if (hasInternet(context)) {
        if (rtcParam.isCall()) {
            // 通话拨打逻辑
            if (canCall(rtcParam)) {
                VideoChatSingleActivity.launchCall(
                        NullObjectCheck.requestObjectNotNull(context, "context"),
                        NullObjectCheck.requestObjectNotNull(rtcParam, "rtcParam"));
            }
        } else {
            // 通话接听逻辑
            if (null != answerBeforeCallback) {
                // 外部回调判断是否可以接听
                if (answerBeforeCallback.canAnswer()) {
                    answer();
                }
            } else {
                answer();
            }
        }
    } else {
        Toast.makeText(context, R.string.rtc_string_no_net, Toast.LENGTH_SHORT).show();
    }
}
~~~
- 判断是否可以进入拨打页面
~~~
/**
 * 判断是否可以拨打电话
 * 拨打条件：
 * 1、本人用户id不为空
 * 2、通话状态为空闲状态
 *
 * @param rtcParam 参数
 * @return 是否
 */
private boolean canCall(RtcParam rtcParam) {
    return null != rtcParam
            && !TextUtils.isEmpty(rtcParam.getUserId())
            && RtcClient.getCallState() == RtcChatStatus.CHAT_IDLE;
}
~~~
- 判断是否可以进入接听页面
~~~
/**
 * 判断是否可以接听逻辑
 * 接听条件：
 * 1、本地登录用户数据不为空
 * 2、房间号不为空
 * 3、本地接听状态未闲置状态
 * 4、拨打方发起通话时间不超过一段时长，默认3分钟
 *
 * @param rtcParam 接听参数
 * @return true：可以接听；false：不可以接听
 */
private boolean canAnswer(RtcParam rtcParam) {
    return !TextUtils.isEmpty(rtcParam.getUserId())
            && !TextUtils.isEmpty(rtcParam.getRoomId())
            && RtcClient.getCallState() == RtcChatStatus.CHAT_IDLE
            && !RtcClient.callTimeOut(callTime);
}
~~~
- 打开接听页面
~~~
/**
 * 进入接听页面，需先判断是否满足接听条件
 */
private void answer() {
    if (canAnswer(rtcParam)) {
        // 接听，需判断房间是否还存在
        RoomExecute roomExecute = new RoomExecute(rtcParam.getRoomId());
        roomExecute.getRoomData(new RoomDataCallback() {
            @Override
            public void onRoomExist() {
                if (isSingleCall) {
                    VideoChatSingleActivity.launchAnswer(
                            NullObjectCheck.requestObjectNotNull(context, "context"),
                            NullObjectCheck.requestObjectNotNull(rtcParam, "rtcParam"));
                } else {
                    VideoChatMultiActivity.launchSelf(
                            NullObjectCheck.requestObjectNotNull(context, "context"),
                            NullObjectCheck.requestObjectNotNull(rtcParam, "rtcParam"));
                }
            }
            @Override
            public void onRoomError(int errorCode) {
                RtcLogTool.log("错误码：" + errorCode);
            }
        });
    }
}
~~~
- 日志输出
~~~
RtcLogTool.LOG_ENABLE = true;
~~~
# 备注
- 修改视频通话悬浮窗样式可以通过复写VideoChatSingleActivity或VideoChatMultiActivity的布局文件，图片资源同理。
- 关于个推配置流程请查看[个推官网](http://docs.getui.com/getui/mobile/android/androidstudio/)
- 关于个推厂商推送配置流程请继续往下
# 厂商推送
- 根据个推厂商推送PDF文档所示流程进行申请和配置操作
- 提供包名+打开厂商推送后的Activity路径给租户配置人员。demo示例：com.youwen.conference/com.xinhuamm.main.mvp.ui.activity.SplashActivity
- 在manifest.xml文件中给Activity配置android:exported="true"属性。具体可查看demo
- 在Activity中处理厂商推送的内容。具体逻辑可查看demo
- demo中的厂商推送无效，仅作参考，具体问题请查看接入文档或咨询相关人员
# 版本更新记录
- 2.2.8
1. 优化三方库依赖配置
- 2.2.7
1. 修复单人通话双方不显示头像的问题
- 2.2.6
1. 优化浮窗权限请求逻辑。
2. 修复已知bug。
- 2.2.5
1. 多人通话支持断线重连。
2. 兼容接口响应参数更改导致的奔溃问题。
3. 升级OkHttp4.8.0解决域名校验失败问题。
- 2.2.4
1. 修复若干bug。
2. 增加推送铃声及时挂断功能。
- 2.2.3
1. 简化配置方式。
2. 支持网络断开后重连3次失败自动挂断。
3. 增加摄像头关闭显示默认头像逻辑。
4. 超30s未接听增加提示"对方手机可能不在身边"。
5. 网络情况监听，当网络不佳时提示。
- 2.2.2
1. 修复超时挂断未通知Socket消息的问题。
- 2.2.1
1. 接听后本人画面从主屏幕移动到小屏。
2. 增加接听前自定义逻辑处理回调。
- 2.2.0
1. 支持单对单视频通话。