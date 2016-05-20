package com.xianglin.fellowvillager.app.longlink.longlink.transport.connection;

import android.content.Intent;
import android.net.SSLCertificateSocketFactory;
import android.os.Bundle;

import com.xianglin.fellowvillager.app.longlink.longlink.service.ConnManager;
import com.xianglin.fellowvillager.app.longlink.longlink.service.LongLinkAppInfo;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.connection.ConnectionConfiguration.SecurityMode;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.connection.proxy.ProxyInfo;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.connectionListener.ConnectListener;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.connectionListener.ConnectionListener;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.Packet;
import com.xianglin.fellowvillager.app.longlink.longlink.transport.packet.PacketConstants;
import com.xianglin.fellowvillager.app.longlink.longlink.util.ConfigUtils;
import com.xianglin.fellowvillager.app.longlink.longlink.util.LogUtil;
import com.xianglin.mobile.common.logging.LogCatLog;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.security.auth.callback.CallbackHandler;


/**
 * 连接的包裹类
 *
 * @author alex
 */
public class PushConnection extends Connection {
    private static final String LOGTAG = ConfigUtils.TAG;

    /**
     * The socket which is used for this connection.
     */
    protected Socket socket;

    String connectionID = null;
    private String user = null;
    private boolean connected = false;
    private int msgVersion = PacketConstants.PACKET_VERSION_2;

    private int retryTimes = 0;

    PacketWriter packetWriter;
    PacketReader packetReader;

    private Timer mTimer = null;
    private int lastMsgId = -1;
    public ConnManager connManager;

    /**
     * Creates a new XMPP conection in the same way
     * {@link #XMPPConnection(ConnectionConfiguration, CallbackHandler)} does,
     * but with no callback handler for password prompting of the keystore. This
     * will work in most cases, provided the client is not required to provide a
     * certificate to the server.
     *
     * @param config the connection configuration.
     */
    public PushConnection(ConnectionConfiguration config) {
        super(config);
    }

    public String getConnectionID() {
        if (!isConnected()) {
            return null;
        }
        return connectionID;
    }

    public String getUser() {
        return user;
    }

    public boolean isConnected() {
        LogUtil.LogOut(4, LOGTAG, "isConnected()...called=" + connected
                + ", connection=" + this.hashCode());
        return connected;
    }

    public void setConnected(boolean isConnected) {
        LogUtil.LogOut(4, LOGTAG, "setConnected()...isConnected=" + isConnected);
        connected = isConnected;
    }

    public void setMsgVersion(int protoVer) {
        msgVersion = protoVer;
    }

    public int getMsgVersion() {
        return this.msgVersion;
    }

    public void setRetryTimes(int times) {
        retryTimes = times;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    /**
     * Closes the connection by setting presence to unavailable then closing the
     * stream to the XMPP server. The shutdown logic will be used during a
     * planned disconnection or when dealing with an unexpected disconnection.
     * Unlike {@link #disconnect()} the connection's packet reader, packet
     * writer, and {@link Roster} will not be removed; thus connection's state
     * is kept.
     *
     * @param unavailablePresence the presence packet to send during shutdown.
     */
    protected void shutdown() {
        LogUtil.LogOut(2, LOGTAG, "shutdown() called...");

        connected = false;
        retryTimes = 0;

        stopTimer();

        if (packetReader != null) {
            packetReader.shutdown();
        }
        if (packetWriter != null) {
            packetWriter.shutdown();
        }

        // Wait 150 ms for processes to clean-up, then shutdown.
        try {
            Thread.sleep(150);
        } catch (Exception e) {
            // Ignore.
        }

        // Close down the readers and writers.
        if (reader != null) {
            try {
                reader.close();
            } catch (Throwable ignore) { /* ignore */
            }
            reader = null;
        }
        if (writer != null) {
            try {
                writer.close();
            } catch (Throwable ignore) { /* ignore */
            }
            writer = null;
        }

        try {
            socket.close();
        } catch (Exception e) {
            // Ignore.
        }
        LogUtil.LogOut(3, LOGTAG, "shutdown()... Done!");
    }

    public void disconnect() {
        LogUtil.LogOut(3, LOGTAG, "disconnect()... called!");

        // If not connected, ignore this request.
        if (packetReader == null || packetWriter == null) {
            return;
        }

        shutdown();

        packetWriter.cleanup();
        packetWriter = null;
        packetReader.cleanup();
        packetReader = null;

        LogUtil.LogOut(3, LOGTAG, "disconnect()... done!");
    }

    public void sendPacket(Packet packet) {
        LogUtil.LogOut(4, LOGTAG, "sendPacket()... isConnected="
                + isConnected());

        try {
            if (!isConnected()) {
                throw new IllegalStateException("Have not connected to server.");
            }
            if (packet == null) {
                throw new NullPointerException("Packet is null.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        LogUtil.LogOut(5, LOGTAG, "sendPacket()... writer=" + writer.hashCode()
                + ", reader=" + reader.hashCode());

        LogUtil.LogOut(3, LOGTAG,
                "sendPacket()... packet.id=" + packet.getMsgId());
        packetWriter.sendPacket(packet);
    }

    /**
     * 创建连接
     *
     * @param config
     * @param taskListener
     * @throws PushException
     */
    private void connectUsingConfiguration(ConnectionConfiguration config,
                                           ConnectListener taskListener) throws PushException {
        LogCatLog.d(LOGTAG, "连接开始重连。。。。");
        SecurityMode securityMode = config.getSecurityMode();
        boolean sslUsed = SecurityMode.required == securityMode ? true : false;

        String host = config.getHost();
        int port = config.getPort();
        LogUtil.LogOut(3, LOGTAG,
                "PushConnection_connectUsingConfiguration:host=" + host
                        + " port=" + port + " sslUsed=" + sslUsed);

        ProxyInfo proxyInfo = config.getProxyInfo();

        try {
            if (config.getSocketFactory() == null) {
                this.socket = new Socket(host, port);
            } else {
                if (sslUsed) {
                    LogUtil.LogOut(3, LOGTAG,
                            "connectUsingConfiguration ssl is needed!");

                    // 根据接入点类型
                    if (ProxyInfo.ProxyType.SOCKS == proxyInfo.getProxyType()) {
                        LogUtil.LogOut(
                                4,
                                LOGTAG,
                                "ProxyType.SOCKS ProxyAddress:"
                                        + proxyInfo.getProxyAddress()
                                        + ", ProxyPort:"
                                        + proxyInfo.getProxyPort());

                        // 接入点为wap
                        Socket st = config.getSocketFactory().createSocket(
                                host, port);
                        SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory
                                .getDefault();
                        // SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory
                        // 		.getSocketFactory();
                        // ssf.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

                        // create the wrapper over connected socket
                        SSLSocket sslSocket = (SSLSocket) ssf.createSocket(st,
                                proxyInfo.getProxyAddress(),
                                proxyInfo.getProxyPort(), true);
                        sslSocket.setUseClientMode(true);

                        // SSL握手
                        sslSocket.startHandshake();

                        this.socket = sslSocket;
                    } else {
                        // 接入点为net
                        SSLSocket sslSocket = (SSLSocket) SSLCertificateSocketFactory
                                .getDefault().createSocket(host, port);
                        // 因为心跳间隔由服务端指定，故此处不设置读等待时间
                        // st.setSoTimeout(60 * 1000);

                        SSLSession s = sslSocket.getSession();

                        HostnameVerifier hv = HttpsURLConnection
                                .getDefaultHostnameVerifier();
                        if (!hv.verify("mobilepmgw.alipay.com", s)) {
                            LogUtil.LogOut(2, LOGTAG,
                                    "connectUsingConfiguration hostname verify failed!");

                            String errorMessage = "Expected hostname verify failed when creating socket, found"
                                    + s.getPeerPrincipal();
                            Exception se = new Exception(
                                    "HostnameVerifier : failed!");
                            throw new PushException(errorMessage, se);

                        } else {
                            this.socket = sslSocket;
                        }
                    }
                } else {
                    //这里发起tcp连接
                    this.socket = config.getSocketFactory().createSocket(host,
                            port);
                }
            }

            // 初始化writer和reader
            if (this.socket != null) {
                LogUtil.LogOut(4, LOGTAG,
                        "connectUsingConfiguration socket is ready!");

                for (int i = 0; i < 20; i++) {
                    try {
                        if (this.socket.isConnected()) {
                            break;
                        }

                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                this.socket.setTcpNoDelay(true);
                this.socket.setSoTimeout(1000 * 60 * 3);
                initConnection(taskListener);
            } else {
                LogUtil.LogOut(2, LOGTAG,
                        "connectUsingConfiguration socket is failed!");
                String errorMessage = "create socket is failed.";
                Exception se = new Exception("socket : null!");
                throw new PushException(errorMessage, se);
            }
        } catch (UnknownHostException uhe) {
            String errorMessage = "Could not connect to " + host + ":" + port
                    + ".";
            throw new PushException(errorMessage, uhe);
        } catch (Exception ioe) {
            //java.net.ConnectException: failed to connect to /172.16.12.51 (port 9999) after 30000ms: isConnected failed: EHOSTUNREACH (No route to host) TODO BY ALEX
            connecerror(host,port+"",ioe);

        }
    }

    /**
     * Initializes the connection by creating a packet reader and writer and
     * opening a XMPP stream to the server.
     *
     * @param taskListener
     * @throws PushException if establishing a connection to the server fails.
     */
    private void initConnection(ConnectListener taskListener)
            throws PushException {
        boolean isFirstInitialization = packetReader == null
                || packetWriter == null;

        // Set the reader and writer instance variables
        initReaderAndWriter();
        LogUtil.LogOut(4, LOGTAG,
                "initConnection Reader and Writer are created!");

        try {
            if (isFirstInitialization) {
                packetWriter = new PacketWriter(this);
                packetReader = new PacketReader(this);
            } else {
                packetWriter.init();
                packetReader.init();
            }

            // Start the packet writer. This will open a XMPP stream to the
            // server
            packetWriter.startup();
            // Start the packet reader. The startup() method will block until we
            // get an opening stream packet back from server.
            packetReader.startup();

            LogUtil.LogOut(5, LOGTAG, "initConnection packetReader="
                    + packetReader.hashCode() + ", and packetWriter="
                    + packetWriter.hashCode());

            LogUtil.LogOut(3, LOGTAG,
                    "initConnection Reader and Writer are ready!");

            taskListener.onSuccess(this);

        } catch (PushException ex) {
            throw ex; // Everything stoppped. Now throw the exception.
        }
    }

    public void resetConnection() {
        LogUtil.LogOut(4, LOGTAG, "resetConnection()...");
        if (null != mTimer) {
            stopTimer();
        }

        // An exception occurred in setting up the connection. Make sure we shut
        // down the
        // readers and writers and close the socket.
        if (packetWriter != null) {
            try {
                packetWriter.shutdown();
            } catch (Throwable ignore) { /* ignore */
            }
            packetWriter = null;
        }
        if (packetReader != null) {
            try {
                packetReader.shutdown();
            } catch (Throwable ignore) { /* ignore */
            }
            packetReader = null;
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (Throwable ignore) { /* ignore */
            }
            reader = null;
        }
        if (writer != null) {
            try {
                writer.close();
            } catch (Throwable ignore) { /* ignore */
            }
            writer = null;
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) { /* ignore */
            }
            socket = null;
        }

        connected = false;
    }

    private void initReaderAndWriter() throws PushException {
        try {
            reader = new DataInputStream(socket.getInputStream());
            writer = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ioe) {
            throw new PushException(
                    "Error establishing connection with server.", ioe);
        }
    }

    /**
     * Establishes a connection to the XMPP server and performs an automatic
     * login only if the previous connection state was logged (authenticated).
     * It basically creates and maintains a socket connection to the server.
     * <p/>
     * <p/>
     * Listeners will be preserved from a previous connection if the
     * reconnection occurs after an abrupt termination.
     *
     * @param taskListener
     * @throws PushException if an error occurs while trying to establish the connection.
     *                       Two possible errors can occur which will be wrapped by an
     *                       XMPPException -- UnknownHostException (XMPP error code 504),
     *                       and IOException (XMPP error code 502). The error codes and
     *                       wrapped exceptions can be used to present more appropiate
     *                       error messages to end-users.
     */
    public void connect(ConnectListener taskListener, ConnManager connManager) {
        try {
            // Establish the connection, readers and writers
            this.connManager = connManager;
            connectUsingConfiguration(config, taskListener);
            LogUtil.LogOut(3, LOGTAG, "connected successfully");
        } catch (PushException e) {
            taskListener.onFail();
            e.printStackTrace();
        }
    }

    // =========================================================================

    private Object mLock = new Object();

    protected void stopTimer() {
        synchronized (mLock) {
            if (null != mTimer) {
                mTimer.cancel(); // ֹͣtimer
                mTimer = null;
            }
        }
    }

    protected void startTimer(long lastSend, int msgId) {
        if (null != mTimer) {
            stopTimer();
        }
        lastMsgId = msgId;

        synchronized (mLock) {
            mTimer = new Timer(true);
            // 每隔-PacketReplyTimeout-检查是否收发包情况
            mTimer.schedule(new reConnTask(),
                    (long) PushCtrlConfiguration.getPacketReplyTimeout());
        }
    }

    class reConnTask extends TimerTask {
        public void run() {
            LogUtil.LogOut(3, LOGTAG, "reConnTask() curMsgId=" + lastMsgId);

            for (ConnectionListener listener : getConnectionListeners()) {
                try {

                    // 通知连接listener：需要重连
                    String errorMessage = "The reps of heart timeout.";
                    Exception err = new Exception("timeout : heart");
                    PushException pushE = new PushException(errorMessage, err);
                    pushE.setType(PushException.PUSH_EXCEPTION_RESPTIMEOUT);

                    listener.connectionClosedOnError(pushE);
                } catch (Exception e) {
                    // Catch and print any exception so we can recover from a
                    // faulty listener
                    e.printStackTrace();
                }
            }
            LogUtil.LogOut(2, LOGTAG,
                    "reConnTask() connectionClosedOnError has been notify!");
        }
    }

    public  void  connecerror(String host,String port,Exception ioe){
        try{
            String errorMessage = "Error connecting to " + host + ":" + port
                    + ".";
            LongLinkAppInfo appInfo = LongLinkAppInfo.getInstance();
            if (appInfo.getUserId() != null && !appInfo.getUserId().equals("")) {
                LogUtil.LogOut(3, LOGTAG, "===== 长链接失败=====");
                Intent intent = new Intent();
                intent.putExtra("ISSUCCESS", false);// 失败
                intent.setAction("android.intent.action.LONGLINKCONNECTHANDLER");
                this.connManager.getContext().sendBroadcast(intent);
                LogUtil.LogOut(3, LOGTAG, "===== 发送长链接失败广播=====");

                Bundle bundle = new Bundle();
                bundle.putBoolean("ISSUCCESS",false);
                bundle.putBoolean("ISREGISTER",true);
                bundle.putString("ACTION","android.intent.action.LONGLINKCONNECTHANDLER");
                this.connManager.getPacketNotifier().onReceivedPacket(bundle);// 发送消息
            }

            throw new PushException(errorMessage, ioe);
        }catch(Exception e){
            LogCatLog.e(LOGTAG,e);
        }
    }
}
