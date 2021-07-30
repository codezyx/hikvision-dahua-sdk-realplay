package xyz.minitool;

import xyz.minitool.sdk.dh.DHPlayCtrl;
import xyz.minitool.sdk.dh.NetSDKLib;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class DHPreview extends JFrame {

    private JPanel paneBox;
    private Panel panePreview;
    private final int STREAM_BUF_SIZE = 1024 * 1024 * 2;

    public static final NetSDKLib netSdk = NetSDKLib.NETSDK_INSTANCE;
    public static final DHPlayCtrl playsdk = DHPlayCtrl.INSTANCE;

    // 登陆句柄
    private NetSDKLib.LLong loginHandle = new NetSDKLib.LLong(0);

    // 监视预览句柄
    private static NetSDKLib.LLong lRealHandle = new NetSDKLib.LLong(0);

    // 设备信息扩展
    private NetSDKLib.NET_DEVICEINFO_Ex deviceInfo = new NetSDKLib.NET_DEVICEINFO_Ex();
    private String m_strIp = "192.168.0.14";
    private int m_nPort = 37777;
    private String m_strUser = "admin";
    private String m_strPassword = "admin";
    private static NetSDKLib.LLong mPlayPort = new NetSDKLib.LLong(0);

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    DHPreview frame = new DHPreview();
                    frame.setVisible(true);
                    frame.initSdk();
                    frame.login();
                    frame.startRealPlay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public DHPreview() {
        init();
    }

    public void initSdk() {
        // 初始化SDK库
        netSdk.CLIENT_Init(DisConnectCallBack.getInstance(), null);

        // 设置断线重连成功回调函数
        netSdk.CLIENT_SetAutoReconnect(HaveReConnectCallBack.getInstance(), null);

        //打开日志，可选
        NetSDKLib.LOG_SET_PRINT_INFO setLog = new NetSDKLib.LOG_SET_PRINT_INFO();
        String logPath = new File(".").getAbsoluteFile().getParent() + File.separator + "sdk_log" + File.separator + "sdk.log";
        setLog.bSetFilePath = 1;
        System.arraycopy(logPath.getBytes(), 0, setLog.szLogFilePath, 0, logPath.getBytes().length);
        setLog.bSetPrintStrategy = 1;
        setLog.nPrintStrategy = 0;
        if (!netSdk.CLIENT_LogOpen(setLog)) {
            System.err.println("Open SDK Log Failed!!!");
        }
    }

    public void login() {
        // 登陆设备
        int nSpecCap = NetSDKLib.EM_LOGIN_SPAC_CAP_TYPE.EM_LOGIN_SPEC_CAP_TCP;    // TCP登入
        IntByReference nError = new IntByReference(0);
        loginHandle = netSdk.CLIENT_LoginEx2(m_strIp, m_nPort, m_strUser,
                m_strPassword, nSpecCap, null, deviceInfo, nError);
        if (loginHandle.longValue() != 0) {
            System.out.printf("Login Device[%s] Success!\n", m_strIp);
        } else {
            System.err.printf("Login Device[%s] Fail.Error[0x%x]\n", m_strIp, netSdk.CLIENT_GetLastError());
        }
    }

    ///视频预览前设置
    public boolean prePlay() {
        boolean isOpened = playsdk.PLAY_OpenStream(mPlayPort, null, 0, STREAM_BUF_SIZE);
        if (!isOpened) {
            System.out.println("OpenStream Failed");
            return false;
        }
        boolean isPlaying = playsdk.PLAY_Play(mPlayPort, Native.getComponentPointer(panePreview));
        if (!isPlaying) {
            System.out.println("PLAYPlay Failed");
            playsdk.PLAY_CloseStream(mPlayPort);
            return false;
        }
//
//        if (isDelayPlay) {
//            if (IPlaySDK.PLAYSetDelayTime(mPlayPort, 500/*ms*/, 1000/*ms*/) == 0) {
//                Log.d(TAG,"SetDelayTime Failed");
//            }
//        }

        return true;
    }

    public void startRealPlay() {
        // 获取播放端口
        playsdk.PLAY_GetFreePort(mPlayPort);
        if (!prePlay()) {
            return;
        }
        // 开启实时监视
        lRealHandle = netSdk.CLIENT_RealPlayEx(loginHandle, 0, Native.getComponentPointer(panePreview), 0);
        if (lRealHandle.longValue() != 0) {
            System.out.println("realplay success");
            netSdk.CLIENT_SetRealDataCallBackEx(lRealHandle, CbfRealDataCallBackEx.getInstance(), null, 31);
        }
    }

    public void stopRealPlay() {
        try {
            playsdk.PLAY_Stop(mPlayPort);
            playsdk.PLAY_CloseStream(mPlayPort);
            playsdk.PLAY_RefreshPlay(mPlayPort);
            if (netSdk.CLIENT_StopRealPlayEx(lRealHandle)) {
                System.out.println("StopRealPlay success");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void LoginOut() {
        stopRealPlay();

        if (loginHandle.longValue() != 0) {
            netSdk.CLIENT_Logout(loginHandle);
        }

        netSdk.CLIENT_Cleanup();
//        System.exit(0);
    }

    /**
     * 设备断线回调
     */
    private static class DisConnectCallBack implements NetSDKLib.fDisConnect {

        private DisConnectCallBack() {
        }

        private static class CallBackHolder {
            private static DisConnectCallBack instance = new DisConnectCallBack();
        }

        public static DisConnectCallBack getInstance() {
            return CallBackHolder.instance;
        }

        public void invoke(NetSDKLib.LLong lLoginID, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            System.out.printf("Device[%s] Port[%d] DisConnect!\n", pchDVRIP, nDVRPort);
        }
    }

    /**
     * 设备重连回调
     */
    private static class HaveReConnectCallBack implements NetSDKLib.fHaveReConnect {
        private HaveReConnectCallBack() {
        }

        private static class CallBackHolder {
            private static HaveReConnectCallBack instance = new HaveReConnectCallBack();
        }

        public static HaveReConnectCallBack getInstance() {
            return CallBackHolder.instance;
        }

        public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            System.out.printf("ReConnect Device[%s] Port[%d]\n", pchDVRIP, nDVRPort);

        }
    }

    /**
     * 实时监视数据回调函数--扩展(pBuffer内存由SDK内部申请释放)
     */
    private static class CbfRealDataCallBackEx implements NetSDKLib.fRealDataCallBackEx {
        private CbfRealDataCallBackEx() {
        }

        private static class CallBackHolder {
            private static CbfRealDataCallBackEx instance = new CbfRealDataCallBackEx();
        }

        public static CbfRealDataCallBackEx getInstance() {
            return CallBackHolder.instance;
        }

        @Override
        public void invoke(NetSDKLib.LLong lRealHandle, int dwDataType, Pointer pBuffer,
                           int dwBufSize, int param, Pointer dwUser) {
            int bInput = 0;
            if (0 != lRealHandle.longValue()) {
                switch (dwDataType) {
                    case 0:
                        // 原始音视频混合数据
                        playsdk.PLAY_InputData(mPlayPort, pBuffer, dwBufSize);
                        break;
                    case 1:
                        //标准视频数据
                        break;
                    case 2:
                        //yuv 数据
                        break;
                    case 3:
                        //pcm 音频数据
                        break;
                    case 4:
                        //原始音频数据
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void init() {
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                LoginOut();
            }
        });
        paneBox = new JPanel();
        contentPane.add(paneBox, BorderLayout.CENTER);
        paneBox.setLayout(new BorderLayout(0, 0));

        panePreview = new Panel();
        paneBox.add(panePreview, BorderLayout.CENTER);
        panePreview.setLayout(new BorderLayout(0, 0));
    }

}
