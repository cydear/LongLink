package com.xianglin.fellowvillager.app.longlink.longlink.transport.connection;

import com.xianglin.fellowvillager.app.longlink.longlink.message.LongLinkMessageFilter;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.connection.Connection.ListenerWrapper;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.connectionListener.ConnectionListener;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.Packet;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketConstants;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketFactory;
import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.fellowvillager.app.longlink.longlink.util.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


/**
 * Listens for stream from the Push server and parses it into packet objects.
 * The packet reader also invokes all packet listeners and collectors.
 * <p/>
 *
 * @see Connection#
 * @see Connection#addPacketListener
 */
class PacketReader {
    private static final String LOGTAG = ConfigUtils.TAG;

    private static final int BUFFER_LEN = 2048 * 48;
    private static final int BUFFER_MAX = 2048 * 48;

    private Thread readerThread;
    private ExecutorService listenerExecutor;

    private PushConnection connection;
    private boolean done;

    private Semaphore connectionSemaphore;

    protected PacketReader(final PushConnection connection) {
        this.connection = connection;
        this.init();
    }

    /**
     * Initializes the reader in order to be used. The reader is initialized
     * during the first connection and when reconnecting due to an abruptly
     * disconnection.
     */
    protected void init() {
        done = false;

        readerThread = new Thread() {
            public void run() {
                parsePackets(this);
            }
        };
        readerThread.setName("Packet Reader ("
                + connection.connectionCounterValue + ")");
        readerThread.setDaemon(true);

        // Create an executor to deliver incoming packets to listeners. We'll
        // use a single
        // thread with an unbounded queue.
        listenerExecutor = Executors
                .newSingleThreadExecutor(new ThreadFactory() {
                    public Thread newThread(Runnable runnable) {
                        Thread thread = new Thread(runnable,
                                "Push Listener Processor ("
                                        + connection.connectionCounterValue
                                        + ")");
                        thread.setDaemon(true);
                        return thread;
                    }
                });

        // resetParser();
    }

    /**
     * Starts the packet reader thread and returns once a connection to the
     * server has been established. A connection will be attempted for a maximum
     * of five seconds.
     *
     * @throws PushException if the server fails to send an opening stream back for more
     *                       than five seconds.
     */
    public void startup() throws PushException {
        connectionSemaphore = new Semaphore(1);

        readerThread.start();
        // Wait for stream tag before returning. We'll wait a couple of seconds
        // before
        // giving up and throwing an error.
        try {
            connectionSemaphore.acquire();

            // A waiting thread may be woken up before the wait time or a notify
            // (although this is a rare thing). Therefore, we continue waiting
            // until either a connectionID has been set (and hence a notify was
            // made) or the total wait time has elapsed.

            // int waitTime = PushCrtlConfiguration.getPacketReplyTimeout();
            connectionSemaphore.tryAcquire(1 * 500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            // Ignore.
        }
    }

    /**
     * Shuts the packet reader down.
     */
    public void shutdown() {
        // Notify connection listeners of the connection closing if done hasn't
        // already been set.
        if (!done) {
            for (ConnectionListener listener : connection
                    .getConnectionListeners()) {
                try {
                    listener.connectionClosed();
                } catch (Exception e) {
                    // Cath and print any exception so we can recover
                    // from a faulty listener and finish the shutdown process
                    e.printStackTrace();
                }
            }
        }
        done = true;

        // Shut down the listener executor.
        listenerExecutor.shutdown();

        LogUtil.LogOut(3, LOGTAG, "shutdown()...listenerExecutor.shutdown!");
    }

    /**
     * Cleans up all resources used by the packet reader.
     */
    void cleanup() {
        connection.recvListeners.clear();
    }

    /**
     * Sends out a notification that there was an error with the connection and
     * closes the connection.
     *
     * @param e the exception that causes the connection close event.
     */
    void notifyConnectionError(PushException e) {
        // Print the stack trace to help catch the problem
        if (e != null) {
            e.printStackTrace();
        }
        LogUtil.LogOut(2, LOGTAG, "notifyConnectionError()...Exception!");

        // Notify connection listeners of the error.
        for (ConnectionListener listener : connection.getConnectionListeners()) {
            try {
                listener.connectionClosedOnError(e);
            } catch (Exception e2) {
                // Catch and print any exception so we can recover from a faulty
                // listener
                e2.printStackTrace();
            }
        }
    }

    /**
     * Parse top-level packets in order to process them further.
     *
     * @param thread the thread that is being used by the reader to parse incoming
     *               packets.
     */
    private void parsePackets(Thread thread) {
        try {
            // boolean isException = false;
            int index = 0;
            int offset = 0;
            byte[] netData = new byte[BUFFER_LEN];//4096
            LogUtil.LogOut(4, LOGTAG, "parsePackets()...");

            do {
                int temp = (BUFFER_LEN - index);// 4096- [0]
                DataInputStream r = connection.reader;// 拿到reader
                int count = r.read(netData, index, temp);// 读取数据
                //alex
                if (count == 180) {
                    String forDebug = new String(netData);
                    LogUtil.LogOut(3, LOGTAG, "数据长度为［Count == 180］"+forDebug);
                }

                //收到的数据 alex 180
                if (count <= 0) {
                    // 异常处理, 结束掉
                    // 读取异常 ,进行异常处理 break
                    LogUtil.LogOut(3, LOGTAG, "reader() count=" + count
                            + " and end of stream!");

                    String errorMessage = "reader reached the end of stream.";
                    Exception err = new Exception("-1 : end of stream");
                    PushException pushE = new PushException(errorMessage, err);
                    pushE.setType(PushException.PUSH_EXCEPTION_STREAMEOS);

                    // Close the connection and notify connection listeners of
                    // the error.
                    notifyConnectionError(pushE);

                    break;
                } else {
                    LogUtil.LogOut(3, LOGTAG, "reader() count=" + count + ", index=" + index);//4

                    int dataLen = index + count;
                    // 简单判断一下是否为有效数据
                    // 设置上限，防止溢出
                    if (dataLen > 0 && BUFFER_MAX > dataLen) {
                        byte[] rawData = new byte[dataLen];
                        System.arraycopy(netData, 0, rawData, 0, dataLen);// 数组copy

                        offset = handleRecvMsg(rawData, dataLen);
                        if (offset < dataLen) {
                            System.arraycopy(rawData, offset, netData, 0,
                                    (dataLen - offset));
                            index = dataLen - offset;
                        } else {
                            // 数据处理有异常或者处理完成,偏移量复位
                            index = 0;
                            Arrays.fill(netData, (byte) 0);
                        }
                    } else {
                        // 读取的数据有问题，丢弃
                        // 重新开始读取
                        index = 0;
                        Arrays.fill(netData, (byte) 0);
                    }

                }
            } while (!done && thread == readerThread);
        } catch (Exception e) {
            if (!done) {
                e.printStackTrace();
                LogUtil.LogOut(2, LOGTAG, "parsePackets() encounter Exception:"
                        + e.getMessage());

                String errorMessage = "reader parsePackets encounter Exception:"
                        + e.getMessage();
                Exception err = new Exception("exception : reader");
                PushException pushE = new PushException(errorMessage, err);
                pushE.setType(PushException.PUSH_EXCEPTION_CONNECEPTION);
                // Close the connection and notify connection listeners of the
                // error.
                notifyConnectionError(pushE);
            }
        }
    }

    private int handleRecvMsg(byte[] netData, int bufferLen) {
        String netStr = new String(netData);//abcdefgh?

        int leftLen = bufferLen;//本数组的总长度？
        int thisLen = 0;
        int index = 0;

        int protocolVersion = this.connection.getMsgVersion();
        InputStream in = new ByteArrayInputStream(netData);

        while (leftLen >= 2) {
            LogUtil.LogOut(4, LOGTAG, "handleRecvMsg() got valid packet protocolVersion:" + protocolVersion
                    + ", msgByte1st: " + Integer.toBinaryString(netData[0]));

            Packet recvMsg = null;
            try {
                recvMsg = PacketFactory.getPacket(protocolVersion);

                //必须先处理前两个字节
                int baseHdrLen = PacketConstants.PACKET_BASE_HEADER_LEN;//协议版本号 + 通信指令标识
                byte[] baseHdrBuf = new byte[baseHdrLen];
                int readLen = in.read(baseHdrBuf, 0, baseHdrLen);
                LogUtil.LogOut(4, LOGTAG, "handleRecvMsg() read baseHdrLen=" + readLen);

                if (readLen == baseHdrLen) {
                    recvMsg.initBaseHdrfromRead(baseHdrBuf);//msgId 3 0 初始化msgID
                } else {
                    // 基本头部不够，不再继续处理，跳出
                    break;
                }

                // 剩余的头部字段处理
                int leftHdrLen = recvMsg.getPacketHdrLen() - baseHdrLen;
                LogUtil.LogOut(4, LOGTAG, "handleRecvMsg() leftHdrLen="
                        + leftHdrLen);

                if (leftHdrLen > (leftLen - 2)) {
                    LogUtil.LogOut(2, LOGTAG, "handleRecvMsg() got error header!");
                    //不在继续处理，跳出
                    break;
                } else {
                    byte[] hdrBuf = new byte[leftHdrLen];

                    //后续头部处理
                    readLen = in.read(hdrBuf, 0, leftHdrLen);


                    if (readLen == leftHdrLen) {
                        recvMsg.initHdrfromRead(hdrBuf);// 头部处理

                        // 继续处理，把当前不支持的packet处理完再继续
                        int bodyLen = recvMsg.getDataLength();// 拿到数据的长度 进行body的读取
                        boolean temp = bodyLen <= (leftLen - recvMsg.getPacketHdrLen()) //alex
                                && bodyLen >= 0;
                        if (temp) {
                            byte[] bodyBuf = new byte[bodyLen];
                            in.read(bodyBuf, 0, bodyLen);

                            // 偏移长度
                            thisLen = (recvMsg.getDataLength() + recvMsg
                                    .getPacketHdrLen());

                            // 如果Gzipped先解压缩
//                            if (recvMsg.getmIsDataGziped() == PacketConstants.MSG_GZIP_ON) {
//                                bodyBuf = ZipUtils.UnGZipByte(bodyBuf);
//                                recvMsg.setDataLength(bodyBuf.length);
//                                LogUtil.LogOut(4, LOGTAG,
//                                        "handleRecvMsg() got zip data! datalen=" + bodyBuf.length + ", data="
//                                                + bodyBuf.toString());
//                            }
                            recvMsg.setData(bodyBuf);
                            if (!Packet.isSupport(recvMsg)) {
                                LogUtil.LogOut(2, LOGTAG,
                                        "handleRecvMsg() it's unsupported packet!");
                            } else {

                                processPacket(recvMsg);//发送数据包倒业务层
                                LongLinkMessageFilter.getInstance().packetFilter(this.connection.connManager,recvMsg,LongLinkMessageFilter.PACKETREADER);// 再次对包进行过滤 进行包分析
                            }

                        } else {
                            // 包体数据不够
                            // 不再继续处理，跳出
                            break;
                        }

                    } else {
                        LogUtil.LogOut(2, LOGTAG,
                                "handleRecvMsg() got error packet!");

                        // 头部数据都不够
                        // 不再继续处理，跳出

                        break;
                    }
                }

                // 剩余长度计算
                index = index + thisLen;
                leftLen = leftLen - thisLen;
                LogUtil.LogOut(4, LOGTAG, "handleRecvMsg() current thisLen="
                        + thisLen + ", leftLen=" + leftLen + ", index=" + index);

            } catch (Exception e) {
                // Catch and print any exception so we can recover
                e.printStackTrace();
                // 全部放弃
                index = bufferLen;
            }
        }

        LogUtil.LogOut(5, LOGTAG, "handleRecvMsg() done! leftLen=" + leftLen
                + ", index=" + index);

        return index;
    }

    /**
     * Processes a packet after it's been fully parsed by looping through the
     * installed packet collectors and listeners and letting them examine the
     * packet to see if they are a match with the filter.
     *
     * @param packet the packet to process.
     */
    private void processPacket(Packet packet) {
        if (packet == null) {
            return;
        }
        LogUtil.LogOut(5, LOGTAG,
                "processPacket() are processing one valid packet!");

        // stop发送等待定时器
        connection.stopTimer();

        // Deliver the incoming packet to listeners.
        listenerExecutor.submit(new ListenerNotification(packet));
    }

    /**
     * A runnable to notify all listeners of a packet.
     */
    private class ListenerNotification implements Runnable {

        private Packet packet;

        public ListenerNotification(Packet packet) {
            this.packet = packet;
        }

        public void run() {
            for (ListenerWrapper listenerWrapper : connection.recvListeners
                    .values()) {
                listenerWrapper.notifyListener(packet);
            }
        }
    }
}
