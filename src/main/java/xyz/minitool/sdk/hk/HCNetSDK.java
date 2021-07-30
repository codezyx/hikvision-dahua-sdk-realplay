
package xyz.minitool.sdk.hk;

import xyz.minitool.sdk.hk.win32.W32API;
import com.sun.jna.*;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.NativeLongByReference;


//SDK接口说明,HCNetSDK.dll
public interface HCNetSDK extends Library {

    HCNetSDK INSTANCE = (HCNetSDK) Native.loadLibrary("hcnetsdk", HCNetSDK.class);


    int STREAM_ID_LEN = 32;
    int NET_DVR_DEV_ADDRESS_MAX_LEN = 129;
    int NET_DVR_LOGIN_USERNAME_MAX_LEN = 64;
    int NET_DVR_LOGIN_PASSWD_MAX_LEN = 64;
    int SERIALNO_LEN = 48;   //序列号长度
    int NET_DVR_SYSHEAD = 1;//系统头数据
    int NET_DVR_STREAMDATA = 2;//视频流数据（包括复合流和音视频分开的视频流数据）

    boolean NET_DVR_Init();

    boolean NET_DVR_Cleanup();

    //启用日志文件写入接口
    boolean NET_DVR_SetLogToFile(boolean bLogEnable, String strLogDir, boolean bAutoDel);

    NativeLong NET_DVR_RealPlay_V40(NativeLong lUserID, NET_DVR_PREVIEWINFO lpPreviewInfo, FRealDataCallBack_V30 fRealDataCall, Pointer pUser);

    int NET_DVR_GetLastError();

    String NET_DVR_GetErrorMsg(NativeLongByReference pErrorNo);

    NativeLong NET_DVR_Login_V40(Pointer pLoginInfo, Pointer lpDeviceInfo);

    boolean NET_DVR_Logout(NativeLong lUserID);

    boolean NET_DVR_StopRealPlay(NativeLong lRealHandle);

    boolean NET_DVR_SetConnectTime(int dwWaitTime, int dwTryTimes);

    boolean NET_DVR_SetReconnect(int dwInterval, boolean bEnableRecon);

    /***API函数声明,详细说明见API手册***/
    interface FRealDataCallBack_V30 extends Callback {
        void invoke(NativeLong lRealHandle, int dwDataType,
                    ByteByReference pBuffer, int dwBufSize, Pointer pUser);
    }

    class NET_DVR_PREVIEWINFO extends Structure {
        public NativeLong lChannel;
        public int dwStreamType;
        public int dwLinkMode;
        public W32API.HWND hPlayWnd;
        public boolean bBlocked;
        public boolean bPassbackRecord;
        public byte byPreviewMode;
        public byte[] byStreamID = new byte[STREAM_ID_LEN];
        public byte byProtoType;
        public byte[] byRes1 = new byte[2];
        public int dwDisplayBufNum;
        public byte[] byRes = new byte[216];
    }

    class NET_DVR_USER_LOGIN_INFO extends Structure {
        public byte[] sDeviceAddress = new byte[NET_DVR_DEV_ADDRESS_MAX_LEN];
        public byte byUseTransport;
        public short wPort;
        public byte[] sUserName = new byte[NET_DVR_LOGIN_USERNAME_MAX_LEN];
        public byte[] sPassword = new byte[NET_DVR_LOGIN_PASSWD_MAX_LEN];
        public FLoginResultCallBack cbLoginResult;
        Pointer pUser;
        public int bUseAsynLogin;
        public byte[] byRes2 = new byte[128];
    }

    interface FLoginResultCallBack extends Callback {
        int invoke(NativeLong lUserID, int dwResult, Pointer lpDeviceinfo, Pointer pUser);
    }

    //NET_DVR_Login_V40()参数
    class NET_DVR_DEVICEINFO_V40 extends Structure {
        public NET_DVR_DEVICEINFO_V30 struDeviceV30 = new NET_DVR_DEVICEINFO_V30();
        public byte bySupportLock;
        public byte byRetryLoginTime;
        public byte byPasswordLevel;
        public byte byRes1;
        public int dwSurplusLockTime;
        public byte[] byRes2 = new byte[256];
    }

    //NET_DVR_Login_V30()参数结构
    class NET_DVR_DEVICEINFO_V30 extends Structure {
        public byte[] sSerialNumber = new byte[SERIALNO_LEN];  //序列号
        public byte byAlarmInPortNum;                //报警输入个数
        public byte byAlarmOutPortNum;                //报警输出个数
        public byte byDiskNum;                    //硬盘个数
        public byte byDVRType;                    //设备类型, 1:DVR 2:ATM DVR 3:DVS ......
        public byte byChanNum;                    //模拟通道个数
        public byte byStartChan;                    //起始通道号,例如DVS-1,DVR - 1
        public byte byAudioChanNum;                //语音通道数
        public byte byIPChanNum;                    //最大数字通道个数
        public byte[] byRes1 = new byte[24];                    //保留
    }
}