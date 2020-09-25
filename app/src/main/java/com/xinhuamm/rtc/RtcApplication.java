package com.xinhuamm.rtc;

import android.util.Log;

import com.igexin.sdk.IUserLoggerInterface;
import com.igexin.sdk.PushManager;
import com.xinyi.rtc.manager.RtcClientOverallSetting;
import com.xinyi.rtc.util.RtcLogTool;

/**
 * @author ming
 * @apiNote Application
 * @since 2020/7/31
 */
public class RtcApplication extends android.app.Application {

    private static RtcApplication instance;
    private ActivityLifecycleManager activityLifecycleManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // RTC初始化配置地址
        RtcClientOverallSetting.getInstance().setCommonHost(BuildConfig.RTC_HOST_COMMON);
        RtcClientOverallSetting.getInstance().setSocketHost(BuildConfig.RTC_HOST_SOCKET);
        RtcLogTool.LOG_ENABLE = true;
        // 个推初始化log
        PushManager.getInstance().setDebugLogger(this, new IUserLoggerInterface() {
            @Override
            public void log(String s) {
                Log.i("PUSH_LOG", s);
            }
        });
        // 其他
        activityLifecycleManager = new ActivityLifecycleManager();
        registerActivityLifecycleCallbacks(activityLifecycleManager);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterActivityLifecycleCallbacks(activityLifecycleManager);
        instance = null;
    }

    public static RtcApplication getInstance() {
        return instance;
    }

    public ActivityLifecycleManager getActivityLifecycleManager() {
        return activityLifecycleManager;
    }
}
