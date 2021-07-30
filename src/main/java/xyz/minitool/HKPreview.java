package xyz.minitool;

import xyz.minitool.sdk.hk.HCNetSDK;
import xyz.minitool.sdk.hk.RealDataCallBack;
import com.sun.jna.NativeLong;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class HKPreview extends JFrame {

    private JPanel paneBox;
    private Panel panePreview;
    static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    static NativeLong previewHandle;// 预览句柄，关闭预览时会用到
    static NativeLong loginHandle;// 登录句柄，预览和退出登录时用到
    private RealDataCallBack realDataCallBack; // callback必须声明为成员变量，否则不能预览

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    hCNetSDK.NET_DVR_Init();
                    hCNetSDK.NET_DVR_SetLogToFile(true, null, false);
                    HKPreview frame = new HKPreview();
                    frame.setVisible(true);
                    frame.startPreview();
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
        init();
    }

    public void startPreview(){
        realDataCallBack = new RealDataCallBack(panePreview);
        loginHandle = loginDevice("192.168.3.88", "admin", "aA123456");
        if (loginHandle.longValue() != -1) {
            System.out.println("login success");
            HCNetSDK.NET_DVR_PREVIEWINFO strPreviewInfo = new HCNetSDK.NET_DVR_PREVIEWINFO();
            strPreviewInfo.lChannel = new NativeLong(1);
            startPlay(loginHandle, strPreviewInfo);
            paneBox.updateUI();
        } else {
            System.out.println("login failed");
        }
    }

    private void startPlay(NativeLong lUserId, HCNetSDK.NET_DVR_PREVIEWINFO struPreviewInfo) {
        struPreviewInfo.hPlayWnd = null;
        previewHandle = hCNetSDK.NET_DVR_RealPlay_V40(lUserId, struPreviewInfo, realDataCallBack, null);
        if (previewHandle.longValue() == -1) {
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            System.out.println("realplay err" + iErr);
        }
    }

    private NativeLong loginDevice(String ip, String username, String password) {
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
        struLoginInfo.wPort = 8000;
        struLoginInfo.write();
        return hCNetSDK.NET_DVR_Login_V40(struLoginInfo.getPointer(), struDeviceInfo.getPointer());
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
                if (previewHandle.longValue() > -1) {
                    hCNetSDK.NET_DVR_StopRealPlay(previewHandle);
                }
                if (loginHandle.longValue() != -1) {
                    hCNetSDK.NET_DVR_Logout(loginHandle);
                }
                hCNetSDK.NET_DVR_Cleanup();
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
