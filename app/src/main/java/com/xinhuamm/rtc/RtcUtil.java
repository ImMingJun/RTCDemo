package com.xinhuamm.rtc;

import android.content.Context;

import com.igexin.sdk.PushManager;
import com.xinhuamm.rtc.sp.DataHelper;
import com.xinyi.rtc.RtcAnswerBeforeCallback;
import com.xinyi.rtc.RtcClient;

import static com.xinhuamm.main.mvp.ui.activity.SplashActivity.KEY_SELF_ID;
import static com.xinhuamm.main.mvp.ui.activity.SplashActivity.KEY_SELF_NAME;

/**
 * @author ming
 * @date 2020/8/10
 * for: 视频通话工具类
 */
public class RtcUtil {

    /**
     * 接听电话
     *
     * @param context      上下文
     * @param pushItemInfo 推送信息
     */
    public static void answer(Context context, PushItem pushItemInfo) {
        if (null == pushItemInfo || null == pushItemInfo.getConnectionPushBO()) {
            return;
        }
        PushChildItem rtcInfo = pushItemInfo.getConnectionPushBO();
        // 登录用户自己的userId(必传)
        String userId = DataHelper.getStringSF(context, KEY_SELF_ID, "");
        // 登录用户昵称
        String userName = DataHelper.getStringSF(context, KEY_SELF_NAME, "");
        // 登录用户头像
        String userHead = "https://dss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=2736630419,2870244992&fm=111&gp=0.jpg";
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
    }

    /**
     * 拨打电话
     * 以下参数全部必传
     *
     * @param context          上下文
     * @param userId           拨打方用户id
     * @param userName         拨打方用户名
     * @param callUserId       接听方用户id
     * @param callUserClientId 接听方个推id
     * @param callUserName     接听方用户名
     */
    public static void call(Context context,
                            String userId, String userName,
                            String callUserId, String callUserClientId, String callUserName) {
        RtcClient.call(context)
                .tenantId(BuildConfig.RTC_TENANT_ID)
                .commonHost(BuildConfig.RTC_HOST_COMMON)
                .socketHost(BuildConfig.RTC_HOST_SOCKET)
                .userId(userId)
                .userName(userName)
                .userHead("https://dss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=2634767155,932800011&fm=26&gp=0.jpg")
                .userToken("")
                .callId(callUserId)
                .callCid(callUserClientId)
                .callName(callUserName)
                .callHead("https://dss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=2736630419,2870244992&fm=111&gp=0.jpg")
                .build()
                .rtc();
    }
}
