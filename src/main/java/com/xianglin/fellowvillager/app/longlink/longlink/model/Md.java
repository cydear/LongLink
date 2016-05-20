package com.xianglin.fellowvillager.app.longlink.longlink.model;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

/**
 * 用于用户初始化
 * Javadoc
 *
 * @author james
 * @version 0.1, 2015-12-11
 */
public class Md implements Serializable {


    private String fromid;

    private String toid;

    private String appid;

    private String mct;

    private String appType;

    private Integer sendType;

    private Integer messageType;

    private String deviceId;

    private String sessionId;

    private Long msgKey;

    private String message;

    private String msgDate;

    private long fileLength;

    private String imgSize;

    private Integer fileTime;

    private String fileId;

    private int replyType;
    private String sKey = "";

    public String getFromid() {
        return fromid;
    }

    public void setFromid(String fromid) {
        this.fromid = fromid;
    }

    public String getToid() {
        return toid;
    }

    public void setToid(String toid) {
        this.toid = toid;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getMct() {
        return mct;
    }

    public void setMct(String mct) {
        this.mct = mct;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public Integer getSendType() {
        return sendType;
    }

    public void setSendType(Integer sendType) {
        this.sendType = sendType;
    }

    public Integer getMessageType() {
        return messageType;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getMsgKey() {
        return msgKey;
    }

    public void setMsgKey(Long msgKey) {
        this.msgKey = msgKey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMsgDate() {
        return msgDate;
    }

    public void setMsgDate(String msgDate) {
        this.msgDate = msgDate;
    }

    public long getFileLength() {
        return fileLength;
    }

    public Md setFileLength(long fileLength) {
        this.fileLength = fileLength;
        return this;
    }

    public String getImgSize() {
        return imgSize;
    }

    public void setImgSize(String imgSize) {
        this.imgSize = imgSize;
    }

    public Integer getFileTime() {
        return fileTime;
    }

    public void setFileTime(Integer fileTime) {
        this.fileTime = fileTime;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public int getReplyType() {
        return replyType;
    }

    public void setReplyType(int replyType) {
        this.replyType = replyType;
    }

    public String getSKey() {
        return sKey;
    }

    public Md setSKey(String sKey) {
        this.sKey = sKey;
        return this;
    }
    public String toString(){
       return  JSON.toJSONString(this);
    }
}