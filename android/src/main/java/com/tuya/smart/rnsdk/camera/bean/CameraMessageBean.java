package com.tuya.smart.rnsdk.camera.bean;

public class CameraMessageBean {
    private String dateTime;
    private String msgTypeContent;
    private String msgContent;
    private int msgType;
    private String attachPics;
    private String[] attachVideos;
    private String actionURL;
    private String icon;
    private String msgSrcId;
    private String id;
    private long time;
    private boolean isDelete;
    private String msgCode;

    public CameraMessageBean() {
    }

    public String getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getMsgTypeContent() {
        return this.msgTypeContent;
    }

    public void setMsgTypeContent(String msgTypeContent) {
        this.msgTypeContent = msgTypeContent;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    public int getMsgType() {
        return this.msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public String getAttachPics() {
        return this.attachPics;
    }

    public void setAttachPics(String attachPics) {
        this.attachPics = attachPics;
    }

    public String getActionURL() {
        return this.actionURL;
    }

    public void setActionURL(String actionURL) {
        this.actionURL = actionURL;
    }

    public String getIcon() {
        return this.icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getMsgSrcId() {
        return this.msgSrcId;
    }

    public void setMsgSrcId(String msgSrcId) {
        this.msgSrcId = msgSrcId;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isDelete() {
        return this.isDelete;
    }

    public void setDelete(boolean delete) {
        this.isDelete = delete;
    }

    public String[] getAttachVideos() {
        return this.attachVideos;
    }

    public void setAttachVideos(String[] attachVideos) {
        this.attachVideos = attachVideos;
    }

    public String getMsgCode() {
        return this.msgCode;
    }

    public void setMsgCode(String msgCode) {
        this.msgCode = msgCode;
    }
}