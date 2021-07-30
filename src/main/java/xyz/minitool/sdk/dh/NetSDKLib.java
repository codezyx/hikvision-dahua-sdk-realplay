package xyz.minitool.sdk.dh;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;

/**
 * NetSDK JNA接口封装
 */
public interface NetSDKLib extends Library {

    NetSDKLib NETSDK_INSTANCE = (NetSDKLib) Native.loadLibrary("dhnetsdk", NetSDKLib.class);

    int NET_SERIALNO_LEN = 48;
    int MAX_LOG_PATH_LEN = 260;  // 日志路径名最大长度

    //  JNA直接调用方法定义，cbDisConnect 实际情况并不回调Java代码，仅为定义可以使用如下方式进行定义。 fDisConnect 回调
    boolean CLIENT_Init(Callback cbDisConnect, Pointer dwUser);

    //  JNA直接调用方法定义，设置断线重连成功回调函数，设置后SDK内部断线自动重连, fHaveReConnect 回调
    void CLIENT_SetAutoReconnect(Callback cbAutoConnect, Pointer dwUser);

    // 设置连接设备超时时间和尝试次数
    void CLIENT_SetConnectTime(int nWaitTime, int nTryTimes);

    // 打开日志功能
    // pstLogPrintInfo指向LOG_SET_PRINT_INFO的指针
    boolean CLIENT_LogOpen(LOG_SET_PRINT_INFO pstLogPrintInfo);

    // 关闭日志功能
    boolean CLIENT_LogClose();

    // 设置登陆网络环境
    void CLIENT_SetNetworkParam(NET_PARAM pNetParam);

    //  JNA直接调用方法定义，登陆扩展接口///////////////////////////////////////////////////
    //  nSpecCap 对应  EM_LOGIN_SPAC_CAP_TYPE 登陆类型
    LLong CLIENT_LoginEx2(String pchDVRIP, int wDVRPort, String pchUserName, String pchPassword, int nSpecCap, Pointer pCapParam, NET_DEVICEINFO_Ex lpDeviceInfo, IntByReference error/*= 0*/);

    // 高安全级别登陆
    LLong CLIENT_LoginWithHighLevelSecurity(NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY pstInParam, NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY pstOutParam);

    // 返回函数执行失败代码
    int CLIENT_GetLastError();

    // 开始实时监视
    // rType  : NET_RealPlayType    返回监控句柄
    LLong CLIENT_RealPlayEx(LLong lLoginID, int nChannelID, Pointer hWnd, int rType);

    // 设置实时监视数据回调函数扩展接口    lRealHandle监控句柄,fRealDataCallBackEx 回调
    boolean CLIENT_SetRealDataCallBackEx(LLong lRealHandle, Callback cbRealData, Pointer dwUser, int dwFlag);

    // 停止实时预览--扩展     lRealHandle为CLIENT_RealPlayEx的返回值
    boolean CLIENT_StopRealPlayEx(LLong lRealHandle);

    //  JNA直接调用方法定义，向设备注销
    boolean CLIENT_Logout(LLong lLoginID);

    //  JNA直接调用方法定义，SDK退出清理
    void CLIENT_Cleanup();

    /************************************************************************
     ** 结构体
     ***********************************************************************/
    // 设置登入时的相关参数
    class NET_PARAM extends Structure {
        public int nWaittime;                // 等待超时时间(毫秒为单位)，为0默认5000ms
        public int nConnectTime;                // 连接超时时间(毫秒为单位)，为0默认1500ms
        public int nConnectTryNum;           // 连接尝试次数，为0默认1次
        public int nSubConnectSpaceTime;        // 子连接之间的等待时间(毫秒为单位)，为0默认10ms
        public int nGetDevInfoTime;            // 获取设备信息超时时间，为0默认1000ms
        public int nConnectBufSize;            // 每个连接接收数据缓冲大小(字节为单位)，为0默认250*1024
        public int nGetConnInfoTime;         // 获取子连接信息超时时间(毫秒为单位)，为0默认1000ms
        public int nSearchRecordTime;        // 按时间查询录像文件的超时时间(毫秒为单位),为0默认为3000ms
        public int nsubDisconnetTime;        // 检测子链接断线等待时间(毫秒为单位)，为0默认为60000ms
        public byte byNetType;                // 网络类型, 0-LAN, 1-WAN
        public byte byPlaybackBufSize;        // 回放数据接收缓冲大小（M为单位），为0默认为4M
        public byte bDetectDisconnTime;       // 心跳检测断线时间(单位为秒),为0默认为60s,最小时间为2s
        public byte bKeepLifeInterval;        // 心跳包发送间隔(单位为秒),为0默认为10s,最小间隔为2s
        public int nPicBufSize;              // 实时图片接收缓冲大小（字节为单位），为0默认为2*1024*1024
        public byte[] bReserved = new byte[4];  // 保留字段字段
    }

    // 设备信息扩展///////////////////////////////////////////////////
    class NET_DEVICEINFO_Ex extends Structure {
        public byte[] sSerialNumber = new byte[NET_SERIALNO_LEN];    // 序列号
        public int byAlarmInPortNum;                              // DVR报警输入个数
        public int byAlarmOutPortNum;                             // DVR报警输出个数
        public int byDiskNum;                                     // DVR硬盘个数
        public int byDVRType;                                     // DVR类型,见枚举NET_DEVICE_TYPE
        public int byChanNum;                                     // DVR通道个数
        public byte byLimitLoginTime;                              // 在线超时时间,为0表示不限制登陆,非0表示限制的分钟数
        public byte byLeftLogTimes;                                // 当登陆失败原因为密码错误时,通过此参数通知用户,剩余登陆次数,为0时表示此参数无效
        public byte[] bReserved = new byte[2];                       // 保留字节,字节对齐
        public int byLockLeftTime;                                // 当登陆失败,用户解锁剩余时间（秒数）, -1表示设备未设置该参数
        public byte[] Reserved = new byte[24];                       // 保留
    }

    // SDK全局日志打印信息
    class LOG_SET_PRINT_INFO extends Structure {
        public int dwSize;
        public int bSetFilePath;//是否重设日志路径, BOOL类型，取值0或1
        public byte[] szLogFilePath = new byte[MAX_LOG_PATH_LEN];//日志路径(默认"./sdk_log/sdk_log.log")
        public int bSetFileSize;//是否重设日志文件大小, BOOL类型，取值0或1
        public int nFileSize;//每个日志文件的大小(默认大小10240),单位:比特, 类型为unsigned int
        public int bSetFileNum;//是否重设日志文件个数, BOOL类型，取值0或1
        public int nFileNum;//绕接日志文件个数(默认大小10), 类型为unsigned int
        public int bSetPrintStrategy;//是否重设日志打印输出策略, BOOL类型，取值0或1
        public int nPrintStrategy;//日志输出策略,0:输出到文件(默认); 1:输出到窗口, 类型为unsigned int
        public byte[] byReserved = new byte[4];                            // 字节对齐
        public Pointer cbSDKLogCallBack;                        // 日志回调，需要将sdk日志回调出来时设置，默认为NULL
        public Pointer dwUser;                                    // 用户数据

        public LOG_SET_PRINT_INFO() {
            this.dwSize = this.size();
        }
    }

    // CLIENT_LoginWithHighLevelSecurity 输出参数
    class NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY extends Structure {
        public int dwSize;                            // 结构体大小
        public NET_DEVICEINFO_Ex stuDeviceInfo;                    // 设备信息
        public int nError;                            // 错误码，见 CLIENT_Login 接口错误码
        public byte[] byReserved = new byte[132];        // 预留字段

        public NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY() {
            this.dwSize = this.size();
        }// 此结构体大小
    }

    ;

    // CLIENT_LoginWithHighLevelSecurity 输入参数
    class NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY extends Structure {
        public int dwSize;                            // 结构体大小
        public byte[] szIP = new byte[64];                // IP
        public int nPort;                            // 端口
        public byte[] szUserName = new byte[64];        // 用户名
        public byte[] szPassword = new byte[64];        // 密码
        public int emSpecCap;                        // 登录模式
        public byte[] byReserved = new byte[4];            // 字节对齐
        public Pointer pCapParam;                        // 见 CLIENT_LoginEx 接口 pCapParam 与 nSpecCap 关系

        public NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY() {
            this.dwSize = this.size();
        }// 此结构体大小
    }

    ;

    // 对应接口 CLIENT_LoginEx2/////////////////////////////////////////////////////////
    class EM_LOGIN_SPAC_CAP_TYPE extends Structure {
        public static final int EM_LOGIN_SPEC_CAP_TCP = 0;    // TCP登陆, 默认方式
        public static final int EM_LOGIN_SPEC_CAP_ANY = 1;    // 无条件登陆
        public static final int EM_LOGIN_SPEC_CAP_SERVER_CONN = 2;    // 主动注册的登入
        public static final int EM_LOGIN_SPEC_CAP_MULTICAST = 3;    // 组播登陆
        public static final int EM_LOGIN_SPEC_CAP_UDP = 4;    // UDP方式下的登入
        public static final int EM_LOGIN_SPEC_CAP_MAIN_CONN_ONLY = 6;    // 只建主连接下的登入
        public static final int EM_LOGIN_SPEC_CAP_SSL = 7;    // SSL加密方式登陆

        public static final int EM_LOGIN_SPEC_CAP_INTELLIGENT_BOX = 9;    // 登录智能盒远程设备
        public static final int EM_LOGIN_SPEC_CAP_NO_CONFIG = 10;   // 登录设备后不做取配置操作
        public static final int EM_LOGIN_SPEC_CAP_U_LOGIN = 11;   // 用U盾设备的登入
        public static final int EM_LOGIN_SPEC_CAP_LDAP = 12;   // LDAP方式登录
        public static final int EM_LOGIN_SPEC_CAP_AD = 13;   // AD（ActiveDirectory）登录方式
        public static final int EM_LOGIN_SPEC_CAP_RADIUS = 14;   // Radius 登录方式
        public static final int EM_LOGIN_SPEC_CAP_SOCKET_5 = 15;   // Socks5登陆方式
        public static final int EM_LOGIN_SPEC_CAP_CLOUD = 16;   // 云登陆方式
        public static final int EM_LOGIN_SPEC_CAP_AUTH_TWICE = 17;   // 二次鉴权登陆方式
        public static final int EM_LOGIN_SPEC_CAP_TS = 18;   // TS码流客户端登陆方式
        public static final int EM_LOGIN_SPEC_CAP_P2P = 19;   // 为P2P登陆方式
        public static final int EM_LOGIN_SPEC_CAP_MOBILE = 20;   // 手机客户端登陆
    }

    /***********************************************************************
     ** 回调
     ***********************************************************************/
    //JNA Callback方法定义,断线回调
    interface fDisConnect extends Callback {
        public void invoke(LLong lLoginID, String pchDVRIP, int nDVRPort, Pointer dwUser);
    }

    // 网络连接恢复回调函数原形
    interface fHaveReConnect extends Callback {
        public void invoke(LLong lLoginID, String pchDVRIP, int nDVRPort, Pointer dwUser);
    }

    // 实时监视数据回调函数--扩展(pBuffer内存由SDK内部申请释放)
    // lRealHandle实时监视           dwDataType: 0-原始数据   1-帧数据    2-yuv数据   3-pcm音频数据
    // pBuffer对应BYTE*
    // param:当类型为0(原始数据)和2(YUV数据) 时为0。当回调的数据类型为1时param为一个tagVideoFrameParam结构体指针。
    // param:当数据类型是3时,param也是一个tagCBPCMDataParam结构体指针
    public interface fRealDataCallBackEx extends Callback {
        public void invoke(LLong lRealHandle, int dwDataType, Pointer pBuffer, int dwBufSize, int param, Pointer dwUser);
    }

    class LLong extends IntegerType {
        private static final long serialVersionUID = 1L;

        /**
         * Size of a native long, in bytes.
         */
        public static int size;

        static {
            size = Native.LONG_SIZE;
            if (Utils.getOsPrefix().equalsIgnoreCase("linux-amd64")
                    || Utils.getOsPrefix().equalsIgnoreCase("win32-amd64")
                    || Utils.getOsPrefix().equalsIgnoreCase("mac-64")) {
                size = 8;
            } else if (Utils.getOsPrefix().equalsIgnoreCase("linux-i386")
                    || Utils.getOsPrefix().equalsIgnoreCase("win32-x86")) {
                size = 4;
            }
        }

        /**
         * Create a zero-valued LLong.
         */
        public LLong() {
            this(0);
        }

        /**
         * Create a LLong with the given value.
         */
        public LLong(long value) {
            super(size, value);
        }
    }
//
//    class SdkStructure extends Structure {
//        @Override
//        protected List<String> getFieldOrder() {
//            List<String> fieldOrderList = new ArrayList<String>();
//            for (Class<?> cls = getClass();
//                 !cls.equals(SdkStructure.class);
//                 cls = cls.getSuperclass()) {
//                Field[] fields = cls.getDeclaredFields();
//                int modifiers;
//                for (Field field : fields) {
//                    modifiers = field.getModifiers();
//                    if (Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
//                        continue;
//                    }
//                    fieldOrderList.add(field.getName());
//                }
//            }
//            return fieldOrderList;
//        }
//        @Override
//        public int fieldOffset(String name) {
//            return super.fieldOffset(name);
//        }
//    }
}



