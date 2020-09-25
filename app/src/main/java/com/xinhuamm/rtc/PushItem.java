package com.xinhuamm.rtc;


import java.io.Serializable;

/**
 * @author ming
 * 推送实体
 */
public class PushItem implements Serializable {
    private static final long serialVersionUID = -8501631236026098927L;
    private String alert;
    private String title;
    private int pushType;
    private PushChildItem connectionPushBO;

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPushType() {
        return pushType;
    }

    public void setPushType(int pushType) {
        this.pushType = pushType;
    }

    public PushChildItem getConnectionPushBO() {
        return connectionPushBO;
    }

    public void setConnectionPushBO(PushChildItem connectionPushBO) {
        this.connectionPushBO = connectionPushBO;
    }

    @Override
    public String toString() {
        return "PushItem{" +
                "alert='" + alert + '\'' +
                ", title='" + title + '\'' +
                ", pushType=" + pushType +
                ", connectionPushBO=" + connectionPushBO +
                '}';
    }
}
