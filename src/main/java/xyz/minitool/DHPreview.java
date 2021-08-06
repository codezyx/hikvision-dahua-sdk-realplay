package xyz.minitool;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import xyz.minitool.sdk.dh.DHPlayCtrl;
import xyz.minitool.sdk.dh.NetSDKLib;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private static NetSDKLib.LLong mPlayPort = new NetSDKLib.LLong(0);
    private JTextField txtIp;
    private JTextField txtPort;
    private JTextField txtUser;
    private JTextField txtPass;
    private JLabel labelTip;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    setLookAndFeel();
                    DHPreview frame = new DHPreview();
                    frame.setVisible(true);
                    frame.initSdk();
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
        setTitle("大华摄像头预览");
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
        String ip = txtIp.getText().trim();
        String port = txtPort.getText().trim();
        String user = txtUser.getText().trim();
        String pass = txtPass.getText().trim();
        if ("".equals(ip)) {
            labelTip.setText("请输入IP");
        } else if ("".equals(port)) {
            labelTip.setText("请输入端口");
        } else if ("".equals(user)) {
            labelTip.setText("请输入用户");
        } else if ("".equals(pass)) {
            labelTip.setText("请输入密码");
        } else {
            // 登陆设备
            int nSpecCap = NetSDKLib.EM_LOGIN_SPAC_CAP_TYPE.EM_LOGIN_SPEC_CAP_TCP;    // TCP登入
            IntByReference nError = new IntByReference(0);
            loginHandle = netSdk.CLIENT_LoginEx2(ip, Integer.parseInt(port), user,
                    pass, nSpecCap, null, deviceInfo, nError);
            if (loginHandle.longValue() != 0) {
                labelTip.setText("登录成功");
            } else {
                int lastError = netSdk.CLIENT_GetLastError();
                labelTip.setText("登录失败，错误码" + lastError);
            }
        }
    }

    ///视频预览前设置
    public boolean prePlay() {
        boolean isOpened = playsdk.PLAY_OpenStream(mPlayPort, null, 0, STREAM_BUF_SIZE);
        if (!isOpened) {
            System.out.println("OpenStream Failed");
            labelTip.setText("打开流失败");
            return false;
        }
        boolean isPlaying = playsdk.PLAY_Play(mPlayPort, Native.getComponentPointer(panePreview));
        if (!isPlaying) {
            System.out.println("PLAYPlay Failed");
            labelTip.setText("播放失败");
            playsdk.PLAY_CloseStream(mPlayPort);
            return false;
        }
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
            labelTip.setText("开启预览成功");
            netSdk.CLIENT_SetRealDataCallBackEx(lRealHandle, CbfRealDataCallBackEx.getInstance(), null, 31);
        } else {
            labelTip.setText("开启预览失败");
        }
    }

    public void stopRealPlay() {
        try {
            boolean stopResult = playsdk.PLAY_Stop(mPlayPort);
            System.out.println("Play_Stop " + (stopResult ? "success" : "failed"));
            boolean closeStream = playsdk.PLAY_CloseStream(mPlayPort);
            System.out.println("PLAY_CloseStream " + (closeStream ? "success" : "failed"));
            if (netSdk.CLIENT_StopRealPlayEx(lRealHandle)) {
                System.out.println("StopRealPlay success");
                labelTip.setText("停止预览成功");
            } else {
                labelTip.setText("停止预览失败" + netSdk.CLIENT_GetLastError());
            }
            playsdk.PLAY_ReleasePort(mPlayPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exit() {
        stopRealPlay();
        if (loginHandle.longValue() != 0) {
            boolean clientLogout = netSdk.CLIENT_Logout(loginHandle);
            System.out.println("注销登录" + clientLogout);
        }
        netSdk.CLIENT_Cleanup();
        System.exit(0);
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
        setSize(755, 490);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerSize(0);
        splitPane.setBorder(null);
        contentPane.add(splitPane, BorderLayout.CENTER);
        paneBox = new JPanel();
        paneBox.setBorder(new MatteBorder(1, 1, 1, 1, (Color) Color.GRAY));
        splitPane.setRightComponent(paneBox);
        paneBox.setLayout(new BorderLayout(0, 0));

        panePreview = new Panel();
        paneBox.add(panePreview, BorderLayout.CENTER);
        panePreview.setLayout(new BorderLayout(0, 0));

        JSplitPane splitPane_1 = new JSplitPane();
        splitPane_1.setBorder(null);
        splitPane_1.setDividerSize(0);
        splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setLeftComponent(splitPane_1);

        JPanel panel = new JPanel();
        splitPane_1.setLeftComponent(panel);
        panel.setLayout(new GridLayout(0, 1, 0, 2));

        JPanel panel_1 = new JPanel();
        panel_1.setBorder(new TitledBorder(null, "\u6444\u50CF\u5934IP", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.add(panel_1);
        panel_1.setLayout(new BorderLayout(0, 0));

        txtIp = new JTextField();
        panel_1.add(txtIp, BorderLayout.CENTER);
        txtIp.setColumns(10);

        JPanel panel_2 = new JPanel();
        panel_2.setBorder(new TitledBorder(null, "\u6444\u50CF\u5934\u7AEF\u53E3", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.add(panel_2);
        panel_2.setLayout(new BorderLayout(0, 0));

        txtPort = new JTextField();
        txtPort.setText("37777");
        panel_2.add(txtPort, BorderLayout.CENTER);
        txtPort.setColumns(10);

        JPanel panel_3 = new JPanel();
        panel_3.setBorder(new TitledBorder(null, "\u7528\u6237\u540D", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.add(panel_3);
        panel_3.setLayout(new BorderLayout(0, 0));

        txtUser = new JTextField();
        txtUser.setText("admin");
        panel_3.add(txtUser, BorderLayout.CENTER);
        txtUser.setColumns(10);

        JPanel panel_4 = new JPanel();
        panel_4.setBorder(new TitledBorder(null, "\u5BC6\u7801", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.add(panel_4);
        panel_4.setLayout(new BorderLayout(0, 0));

        txtPass = new JTextField();
        panel_4.add(txtPass, BorderLayout.CENTER);
        txtPass.setColumns(10);

        JPanel panel_5 = new JPanel();
        panel_5.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.add(panel_5);
        panel_5.setLayout(new BorderLayout(0, 0));

        JButton btnNewButton = new JButton("开启预览");
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                login();
                startRealPlay();
            }
        });
        panel_5.add(btnNewButton, BorderLayout.CENTER);

        JPanel panel_7 = new JPanel();
        panel_7.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.add(panel_7);
        panel_7.setLayout(new BorderLayout(0, 0));

        JButton btnNewButton_1 = new JButton("关闭退出");
        btnNewButton_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
        panel_7.add(btnNewButton_1, BorderLayout.CENTER);

        JPanel panel_6 = new JPanel();
        panel_6.setBorder(new EmptyBorder(3, 3, 3, 3));
        splitPane_1.setRightComponent(panel_6);
        panel_6.setLayout(new BorderLayout(0, 0));

        labelTip = new JLabel("");
        labelTip.setVerticalAlignment(SwingConstants.TOP);
        panel_6.add(labelTip, BorderLayout.CENTER);
        splitPane_1.setDividerLocation(350);
        splitPane.setDividerLocation(150);
        setLocationRelativeTo(null);
    }

    /**
     * 设置皮肤
     */
    private static void setLookAndFeel() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            } else {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            }
        } catch (InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException
                | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}
