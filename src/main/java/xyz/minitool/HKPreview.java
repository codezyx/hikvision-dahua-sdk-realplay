package xyz.minitool;

import com.sun.jna.NativeLong;
import xyz.minitool.sdk.hk.HCNetSDK;
import xyz.minitool.sdk.hk.RealDataCallBack;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class HKPreview extends JFrame {

    private JPanel paneBox;
    private Panel panePreview; // 真正传给海康sdk的panel必须是awt包的Panel，不能传swing包的JPanel
    static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    static NativeLong previewHandle;// 预览句柄，关闭预览时会用到
    static NativeLong loginHandle;// 登录句柄，预览和退出登录时用到
    private RealDataCallBack realDataCallBack; // callback必须声明为成员变量，否则不能预览

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
                    HKPreview frame = new HKPreview();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public HKPreview() {
        setTitle("海康摄像头预览");
        init();
    }

    public void startPreview() {
        hCNetSDK.NET_DVR_Init();
        hCNetSDK.NET_DVR_SetLogToFile(true, null, false);
        String ip = txtIp.getText().trim();
        String port = txtPort.getText().trim();
        String user = txtUser.getText().trim();
        String pass = txtPass.getText().trim();
        if ("".equals(ip)) {
            labelTip.setText("请输入正确的IP");
        } else if ("".equals(port)) {
            labelTip.setText("请输入端口");
        } else if ("".equals(user)) {
            labelTip.setText("请输入用户");
        } else if ("".equals(pass)) {
            labelTip.setText("请输入密码");
        } else {
            realDataCallBack = new RealDataCallBack(panePreview);
            loginHandle = loginDevice(ip, port, user, pass);
            if (loginHandle.longValue() != -1) {
                labelTip.setText("登录成功");
                System.out.println("login success");
                HCNetSDK.NET_DVR_PREVIEWINFO strPreviewInfo = new HCNetSDK.NET_DVR_PREVIEWINFO();
                strPreviewInfo.lChannel = new NativeLong(1);
                startPlay(loginHandle, strPreviewInfo);
                paneBox.updateUI();
            } else {
                labelTip.setText("登录失败");
                System.out.println("login failed");
            }
        }
    }

    // 开启预览
    private void startPlay(NativeLong lUserId, HCNetSDK.NET_DVR_PREVIEWINFO struPreviewInfo) {
        struPreviewInfo.hPlayWnd = null;
        previewHandle = hCNetSDK.NET_DVR_RealPlay_V40(lUserId, struPreviewInfo, realDataCallBack, null);
        if (previewHandle.longValue() == -1) {
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            labelTip.setText("开启预览失败，错误码" + iErr);
            System.out.println("realplay err" + iErr);
        } else {
            labelTip.setText("开启预览成功");
        }
    }

    // 登录
    private NativeLong loginDevice(String ip, String port, String username, String password) {
        HCNetSDK.NET_DVR_USER_LOGIN_INFO struLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();
        HCNetSDK.NET_DVR_DEVICEINFO_V40 struDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();
        for (int i = 0; i < ip.length(); i++) {
            struLoginInfo.sDeviceAddress[i] = (byte) ip.charAt(i);
        }
        for (int i = 0; i < password.length(); i++) {
            struLoginInfo.sPassword[i] = (byte) password.charAt(i);
        }
        for (int i = 0; i < username.length(); i++) {
            struLoginInfo.sUserName[i] = (byte) username.charAt(i);
        }
        struLoginInfo.wPort = Short.parseShort(port);
        struLoginInfo.write();
        return hCNetSDK.NET_DVR_Login_V40(struLoginInfo.getPointer(), struDeviceInfo.getPointer());
    }

    private void exit() {
        if (previewHandle.longValue() > -1) {
            boolean stopRealPlay = hCNetSDK.NET_DVR_StopRealPlay(previewHandle);
            System.out.println("NET_DVR_StopRealPlay result: " + stopRealPlay);
        }
        if (loginHandle.longValue() != -1) {
            boolean dvrLogout = hCNetSDK.NET_DVR_Logout(loginHandle);
            System.out.println("NET_DVR_Logout result: " + dvrLogout);
        }
        hCNetSDK.NET_DVR_Cleanup();
        System.exit(0);
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

        JSplitPane splitPane_2 = new JSplitPane();
        splitPane_2.setDividerSize(0);
        splitPane_2.setBorder(null);
        contentPane.add(splitPane_2, BorderLayout.CENTER);

        JSplitPane splitPane_3 = new JSplitPane();
        splitPane_3.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane_3.setDividerSize(0);
        splitPane_3.setBorder(null);
        splitPane_2.setLeftComponent(splitPane_3);

        JPanel panel_8 = new JPanel();
        splitPane_3.setLeftComponent(panel_8);
        panel_8.setLayout(new GridLayout(0, 1, 0, 2));

        JPanel panel_9 = new JPanel();
        panel_9.setBorder(new TitledBorder(null, "\u6444\u50CF\u5934IP", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_8.add(panel_9);
        panel_9.setLayout(new BorderLayout(0, 0));

        txtIp = new JTextField();
        panel_9.add(txtIp, BorderLayout.CENTER);
        txtIp.setColumns(10);

        JPanel panel_10 = new JPanel();
        panel_10.setBorder(new TitledBorder(null, "\u6444\u50CF\u5934\u7AEF\u53E3", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_8.add(panel_10);
        panel_10.setLayout(new BorderLayout(0, 0));

        txtPort = new JTextField();
        panel_10.add(txtPort, BorderLayout.CENTER);
        txtPort.setText("8000");
        txtPort.setColumns(10);

        JPanel panel_11 = new JPanel();
        panel_11.setBorder(new TitledBorder(null, "\u7528\u6237\u540D", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_8.add(panel_11);
        panel_11.setLayout(new BorderLayout(0, 0));

        txtUser = new JTextField();
        panel_11.add(txtUser, BorderLayout.CENTER);
        txtUser.setText("admin");
        txtUser.setColumns(10);

        JPanel panel_12 = new JPanel();
        panel_12.setBorder(new TitledBorder(null, "\u5BC6\u7801", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_8.add(panel_12);
        panel_12.setLayout(new BorderLayout(0, 0));

        txtPass = new JTextField();
        panel_12.add(txtPass, BorderLayout.CENTER);
        txtPass.setColumns(10);

        JPanel panel_13 = new JPanel();
        panel_13.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel_8.add(panel_13);
        panel_13.setLayout(new BorderLayout(0, 0));

        JButton btnNewButton = new JButton("开启预览");
        panel_13.add(btnNewButton, BorderLayout.CENTER);
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startPreview();
            }
        });

        JPanel panel_14 = new JPanel();
        panel_14.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel_8.add(panel_14);
        panel_14.setLayout(new BorderLayout(0, 0));

        JButton btnNewButton_1 = new JButton("关闭退出");
        panel_14.add(btnNewButton_1, BorderLayout.CENTER);
        btnNewButton_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });

        JPanel panel_15 = new JPanel();
        panel_15.setBorder(new EmptyBorder(3, 3, 3, 3));
        splitPane_3.setRightComponent(panel_15);
        panel_15.setLayout(new BorderLayout(0, 0));

        labelTip = new JLabel("");
        labelTip.setVerticalAlignment(SwingConstants.TOP);
        panel_15.add(labelTip, BorderLayout.CENTER);
        splitPane_3.setDividerLocation(350);
        paneBox = new JPanel();
        splitPane_2.setRightComponent(paneBox);
        paneBox.setLayout(new BorderLayout(0, 0));

        panePreview = new Panel();
        paneBox.add(panePreview, BorderLayout.CENTER);
        panePreview.setLayout(new BorderLayout(0, 0));
        splitPane_2.setDividerLocation(150);
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
