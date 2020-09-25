package com.xinhuamm.rtc;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.PushManager;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTNotificationMessage;
import com.igexin.sdk.message.GTTransmitMessage;
import com.xinhuamm.main.mvp.ui.activity.SplashActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.ExecutionException;

/**
 * 继承 GTIntentService 接收来自个推的消息, 所有消息在线程中回调, 如果注册了该服务, 则务必要在 AndroidManifest中声明, 否则无法接受消息<br>
 * onReceiveMessageData 处理透传消息<br>
 * onReceiveClientId 接收 cid <br>
 * onReceiveOnlineState cid 离线上线通知 <br>
 * onReceiveCommandResult 各种事件处理回执 <br>
 */
public class PushIntentService extends GTIntentService {

    public final static String KEY_PUSH_BUNDLE = "KEY_PUSH_BUNDLE";
    public final static String KEY_PUSH_ITEM_BUNDLE = "KEY_PUSH_ITEM";
    public final static String KEY_TASK_ID_BUNDLE = "KEY_TASK_ID";
    public final static String KEY_MESSAGE_ID_BUNDLE = "KEY_MESSAGE_ID";

    public final static String CID_ALL_CALL = "call";
    public final static String CNAME_ONLY_CALL = "电话通知";
    public final static int PUSH_INF_ARRIVE = 90001;
    public final static int PUSH_INF_CLICK = 90002;
    public final static int DEFAULT_NOTIFICATION_ID = 1000;
    protected NotificationManager mNotificationManager;
    protected int mNotificationId = DEFAULT_NOTIFICATION_ID;
    private String mCallChannelId;
    protected long[] mVibratePattern = new long[]{200, 200, 200, 200};

    public static final String TAG = PushHelpService.class.getName();

    public PushIntentService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mCallChannelId = getPackageName() + CID_ALL_CALL;
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createCallNotificationChannel();
    }

    private void createCallNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = mNotificationManager.getNotificationChannel(mCallChannelId);
            if (null == channel) {
                channel = new NotificationChannel(mCallChannelId, CNAME_ONLY_CALL, NotificationManager.IMPORTANCE_HIGH);
                // 让通知的标题内容在锁屏情况下也能够展示
                channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                // 设置通知出现时的震动（如果 android 设备支持的话）
                channel.enableVibration(true);
                channel.setVibrationPattern(mVibratePattern);
                // 是否响铃
                channel.setSound(getCallSoundUri(), Notification.AUDIO_ATTRIBUTES_DEFAULT);
                // 创建
                mNotificationManager.createNotificationChannel(channel);
            }
        }
    }

    private Uri getCallSoundUri() {
        return getCallSoundUri(R.raw.rtc_notification_call_ing);
    }

    private Uri getCallSoundUri(int callSoundRawRes) {
        Resources r = getResources();
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + r.getResourcePackageName(callSoundRawRes) + "/"
                + r.getResourceTypeName(callSoundRawRes) + "/"
                + r.getResourceEntryName(callSoundRawRes));
    }

    @Override
    public void onReceiveServicePid(Context context, int pid) {
    }

    /**
     * 获取透传数据
     *
     * @param context 上下文
     * @param msg     消息
     */
    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage msg) {
        String appid = msg.getAppid();
        String taskid = msg.getTaskId();
        String messageid = msg.getMessageId();
        byte[] payload = msg.getPayload();
        String pkg = msg.getPkgName();
        String cid = msg.getClientId();

        // 第三方回执调用接口，actionid范围为90000-90999，可根据业务场景执行
        boolean result = PushManager.getInstance().sendFeedbackMessage(context, taskid, messageid, PUSH_INF_ARRIVE);

        if (payload == null) {
            Log.e(TAG, "receiver payload = null");
        } else {
            String data = new String(payload);
            Log.e(TAG, "receiver payload = " + data);
            PushItem pushItem = new Gson().fromJson(data, PushItem.class);
            if (pushItem.getPushType() == 1) {
                RtcApplication application = RtcApplication.getInstance();
                if (null != application) {
                    ActivityLifecycleManager activityLifecycleManager = application.getActivityLifecycleManager();
                    if (null != activityLifecycleManager) {
                        Activity activity = activityLifecycleManager.getCurrentActivity();
                        if (null != activity && !activity.isFinishing()) {
                            activity.runOnUiThread(() -> RtcUtil.answer(activity, pushItem));
                            return;
                        }
                    }
                }
                notificationCall(pushItem);
            } else if (pushItem.getPushType() == 5) {
                notifyCallCancel();
            }
        }
    }

    private void notifyCallCancel() {
        if (mNotificationId != DEFAULT_NOTIFICATION_ID) {
            mNotificationManager.cancel(mNotificationId);
            mNotificationId = DEFAULT_NOTIFICATION_ID;
        }
    }

    public void notificationCall(PushItem item) {
        PushChildItem connectionPushBO = item.getConnectionPushBO();
        // 用主持人id+房间id来代表视频通话的唯一性
        mNotificationId = (connectionPushBO.getHostId() + connectionPushBO.getRoomId()).hashCode();
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(getPackageName(), SplashActivity.class.getName()));
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (RomUtils.isSamsung()) {
            Bundle pushBundle = new Bundle();
            pushBundle.putSerializable(KEY_PUSH_ITEM_BUNDLE, item);
            intent.putExtra(KEY_PUSH_BUNDLE, pushBundle);
        } else {
            intent.putExtra(KEY_PUSH_ITEM_BUNDLE, item);
        }
        //PendingIntent.FLAG_UPDATE_CURRENT每次都更新 注:requestCode必须有(并且不能一样)不然每次返回的数据都会一样
        PendingIntent pendingIntent = PendingIntent.getActivity(this, mNotificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String title = TextUtils.isEmpty(item.getTitle()) ? getResources().getString(R.string.app_name) : item.getTitle();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, mCallChannelId)
                //.setCustomContentView(contentView)//在OPPO r9m上无法展示出自定义的布局，包含CustomBigContentView，BigTextStyle，BigPictureStyle等
                //.setCustomBigContentView(contentView)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(TextUtils.isEmpty(item.getAlert()) ? item.getTitle() : item.getAlert())
                .setContentTitle(title)
                .setContentText(item.getAlert())
                .setAutoCancel(true)
                .setVibrate(mVibratePattern)
                .setSound(getCallSoundUri())
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (TextUtils.isEmpty(connectionPushBO.getIcon())) {
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
            NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
            style.bigText(item.getAlert());
            style.setBigContentTitle(title);
            //SummaryText没什么用 可以不设置，要删除这句话，要不在小M手机上会显示2条content
            mBuilder.setStyle(style);
        } else {
            Bitmap bitmap = null;
            try {
                bitmap = Glide.with(this)
                        .asBitmap()
                        .load(connectionPushBO.getIcon())
                        .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            if (bitmap != null) {
                mBuilder.setLargeIcon(bitmap);
            }

        }
        mNotificationManager.notify(mNotificationId, mBuilder.build());

    }

    /**
     * 获取ClientID
     * 第三方应用需要将CID上传到第三方服务器,
     * 并且将当前用户账号和CID进行关联,
     * 以便日后通过用户账号查找CID进行消息推送
     *
     * @param context  上下文
     * @param clientid 唯一标识
     */
    @Override
    public void onReceiveClientId(Context context, String clientid) {
        Log.w(TAG, "onReceiveClientId -> " + "clientid = " + clientid);
        EventBus.getDefault().post(clientid);
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean online) {
        Log.w(TAG, "onReceiveOnlineState -> " + (online ? "online" : "offline"));
    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage cmdMessage) {
        Log.w(TAG, "onReceiveCommandResult -> " + cmdMessage);
        int action = cmdMessage.getAction();

//        if (action == PushConsts.SET_TAG_RESULT) {
//            setTagResult((SetTagCmdMessage) cmdMessage);
//        } else if (action == PushConsts.BIND_ALIAS_RESULT) {
//            bindAliasResult((BindAliasCmdMessage) cmdMessage);
//        } else if (action == PushConsts.UNBIND_ALIAS_RESULT) {
//            unbindAliasResult((UnBindAliasCmdMessage) cmdMessage);
//        } else if ((action == PushConsts.THIRDPART_FEEDBACK)) {
//            feedbackResult((FeedbackCmdMessage) cmdMessage);
//        }
    }

    @Override
    public void onNotificationMessageArrived(Context context, GTNotificationMessage gtNotificationMessage) {
        Log.e(PushIntentService.class.getSimpleName(), "onNotificationMessageArrived: VALUE=" + gtNotificationMessage);
    }

    @Override
    public void onNotificationMessageClicked(Context context, GTNotificationMessage gtNotificationMessage) {
        Log.e(PushIntentService.class.getSimpleName(), "onNotificationMessageClicked: VALUE=" + gtNotificationMessage);
    }

}
