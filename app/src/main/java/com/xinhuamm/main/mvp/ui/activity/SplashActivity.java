package com.xinhuamm.main.mvp.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.igexin.sdk.PushManager;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.xinhuamm.rtc.PushIntentService;
import com.xinhuamm.rtc.PushItem;
import com.xinhuamm.rtc.R;
import com.xinhuamm.rtc.RomUtils;
import com.xinhuamm.rtc.RtcUtil;
import com.xinhuamm.rtc.sp.DataHelper;
import com.xinyi.rtc.util.RtcLogTool;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static com.xinhuamm.rtc.PushIntentService.KEY_PUSH_ITEM_BUNDLE;

/**
 * @author ming
 * demo主页
 */
public class SplashActivity extends AppCompatActivity {

    public static final String KEY_CLIENT_ID = "KEY_CLIENT_ID";
    public static final String KEY_CALL_ID = "KEY_CALL_ID";
    public static final String KEY_CALL_NAME = "KEY_CALL_NAME";
    public static final String KEY_SELF_ID = "KEY_SELF_ID";
    public static final String KEY_SELF_NAME = "KEY_SELF_NAME";

    private TextView textView;
    private EditText clientId;
    private EditText callId;
    private EditText callName;
    private EditText selfId;
    private EditText selfName;
    private RxPermissions rxPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.tv_cid);
        clientId = findViewById(R.id.client_id);
        callId = findViewById(R.id.call_id);
        callName = findViewById(R.id.call_name);
        selfId = findViewById(R.id.self_id);
        selfName = findViewById(R.id.self_name);
        clientId.setText(DataHelper.getStringSF(this, KEY_CLIENT_ID, ""));
        callId.setText(DataHelper.getStringSF(this, KEY_CALL_ID, ""));
        callName.setText(DataHelper.getStringSF(this, KEY_CALL_NAME, ""));
        selfId.setText(DataHelper.getStringSF(this, KEY_SELF_ID, ""));
        selfName.setText(DataHelper.getStringSF(this, KEY_SELF_NAME, ""));
        initPush();
        handlePushIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handlePushIntent(intent);
    }

    private void handlePushIntent(Intent intent) {
        Bundle bundle = receiveBundle(intent);
        if (null == bundle) {
            return;
        }
        for (String key : bundle.keySet()) {
            Log.e("RTC_LOG Bundle Content", "Key=" + key + ", content=" + bundle.getString(key));
        }
        Serializable pushItem = bundle.getSerializable(KEY_PUSH_ITEM_BUNDLE);
        if (pushItem == null) {
            //厂商推送
            String skipData = bundle.getString("data");
            RtcLogTool.log(skipData);
            try {
                pushItem = new Gson().fromJson(skipData, PushItem.class);
            } catch (Exception ignored) {
                return;
            }
        }
        if (pushItem instanceof PushItem) {
            RtcLogTool.log(pushItem.toString());
            RtcUtil.answer(this, (PushItem) pushItem);
        }
    }

    protected Bundle receiveBundle(Intent intent) {
        if (intent == null) {
            return null;
        }
        return RomUtils.isSamsung() ? intent.getBundleExtra(PushIntentService.KEY_PUSH_BUNDLE) : intent.getExtras();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    protected void initPush() {
        if (rxPermissions == null) {
            rxPermissions = new RxPermissions(this);
        }
        rxPermissions.request(Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        PushManager.getInstance().initialize(SplashActivity.this.getApplicationContext());
                        PushManager.getInstance().registerPushIntentService(SplashActivity.this, PushIntentService.class);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String cid) {
        textView.setText(cid);
    }

    public void copyCid(View view) {
        String cid = textView.getText().toString();
        if (TextUtils.isEmpty(cid)) {
            Toast.makeText(this, "个推cid获取中…", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            ClipData clipData = ClipData.newPlainText("复制cid", cid);
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(this, "复制cid成功", Toast.LENGTH_SHORT).show();
        }
    }

    public void call(View view) {
        String callUserClientId = clientId.getText().toString();
        String callUserId = callId.getText().toString();
        String callUserName = callName.getText().toString();
        String userId = selfId.getText().toString();
        String userName = selfName.getText().toString();
        if (TextUtils.isEmpty(callUserId)
                || TextUtils.isEmpty(userId)
                || TextUtils.isEmpty(callUserClientId)) {
            Toast.makeText(this, "用户信息未配置完整！", Toast.LENGTH_SHORT).show();
            return;
        }
        DataHelper.setStringSF(this, KEY_CLIENT_ID, callUserClientId);
        DataHelper.setStringSF(this, KEY_CALL_ID, callUserId);
        DataHelper.setStringSF(this, KEY_CALL_NAME, callUserName);
        DataHelper.setStringSF(this, KEY_SELF_ID, userId);
        DataHelper.setStringSF(this, KEY_SELF_NAME, userName);
        RtcUtil.call(this, userId, userName, callUserId, callUserClientId, callUserName);
    }

    public void save(View view) {
        String callUserClientId = clientId.getText().toString();
        String callUserId = callId.getText().toString();
        String callUserName = callName.getText().toString();
        String userId = selfId.getText().toString();
        String userName = selfName.getText().toString();
        DataHelper.setStringSF(this, KEY_CLIENT_ID, callUserClientId);
        DataHelper.setStringSF(this, KEY_CALL_ID, callUserId);
        DataHelper.setStringSF(this, KEY_CALL_NAME, callUserName);
        DataHelper.setStringSF(this, KEY_SELF_ID, userId);
        DataHelper.setStringSF(this, KEY_SELF_NAME, userName);
        Toast.makeText(this, "保存本地信息成功！", Toast.LENGTH_SHORT).show();
    }
}