package com.xianglin.fellowvillager.app.longlink.longlink.message;

import com.xianglin.fellowvillager.app.longlink.longlink.service.ConnManager;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.Packet;
import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.mobile.common.logagent.LogSendManager;
import com.xianglin.mobile.common.logging.LogCatLog;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 底层消息过滤
 *
 * Javadoc
 *
 * @author james
 * @version 0.1, 2015-12-10
 */
public class LongLinkMessageFilter {

    private static final String TAG = ConfigUtils.TAG;

    private static volatile LongLinkMessageFilter longLinkMessageFilter;
    private static String filePath = "/sdcard/XiangLin/log/longlink.txt";
    public static final  String  PACKETREADER = "reader";

    public static final  String  PACKETWRITER = "writer";
    public LongLinkMessageFilter(){

    }


    public synchronized  static LongLinkMessageFilter getInstance(){
        if (longLinkMessageFilter == null){
            longLinkMessageFilter = new LongLinkMessageFilter();
        }
        return longLinkMessageFilter;
    }

    /**
     * 包过滤
     * @param packet 数据包
     * @param  type  数据包类型， PACKETREADER 收回来的包
     *                          PACKETWRITER 需要发送的包
     * @return
     */
    public boolean packetFilter(ConnManager connManager,Packet packet,String type){
        if (type.equals(PACKETREADER)){
            longLink(connManager.getXlId(),"读"+packet.toString());
            LogCatLog.d(TAG,"LongLinkMessageFilter 数据归纳 读的数据包"+packet.toString());
        }else if(type.equals(PACKETWRITER)){
            longLink(connManager.getXlId(),"写"+packet.toString());
            LogCatLog.d(TAG,"LongLinkMessageFilter 数据归纳 写的数据包"+packet.toString());
        }

        return true;

    }


    /**
     * 长连接日志
     *
     * @param longLinkMessage
     */
    public static void longLink(String xlId,String longLinkMessage) {
        RandomAccessFile randomFile = null;
        try {
            File file = new File(filePath);
            if (file.length() > 1024*1024){
                LogSendManager.uploadLogFile(xlId, file.getAbsolutePath());
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("\n======longlink==========\n");
            stringBuffer.append("长连接数据:\n" + longLinkMessage+"\n");
            stringBuffer.append("======longlink==========\n");

            randomFile = new RandomAccessFile(file.getAbsoluteFile(), "rw");
            long fileLength = randomFile.length();
            randomFile.seek(fileLength);
            randomFile.write(stringBuffer.toString().getBytes("UTF-8"));
        } catch (Exception e) {
            LogCatLog.e(TAG, "写入日志失败", e);
        } finally {
            if (randomFile != null) {
                try {
                    randomFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }





}
