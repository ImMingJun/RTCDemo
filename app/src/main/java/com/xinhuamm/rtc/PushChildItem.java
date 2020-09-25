package com.xinhuamm.rtc;

import java.io.Serializable;

/**
 * @author ming
 * 推送实体子类
 */
public class PushChildItem implements Serializable {
    private static final long serialVersionUID = 4491116707459234284L;
    private String liveTitle;
    private String icon;
    private String hostId;
    private String userId;
    private String roomId;
    private String reportId;
    private long startDate;
    private int singleCall;

    public int getSingleCall() {
        return singleCall;
    }

    public boolean isSingleCall() {
        return singleCall == 1;
    }

    public void setSingleCall(int singleCall) {
        this.singleCall = singleCall;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLiveTitle() {
        return liveTitle;
    }

    public void setLiveTitle(String liveTitle) {
        this.liveTitle = liveTitle;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
}
