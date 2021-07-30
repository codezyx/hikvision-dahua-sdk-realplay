package xyz.minitool.sdk.hk;

import xyz.minitool.sdk.hk.win32.W32API;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.NativeLongByReference;

import java.awt.*;

public class RealDataCallBack implements HCNetSDK.FRealDataCallBack_V30 {

    private final Panel panePreview;
    private final PlayCtrl playCtrl = PlayCtrl.INSTANCE;
    private final NativeLongByReference playChannel = new NativeLongByReference(new NativeLong(-1));

    public RealDataCallBack(Panel panePreview) {
        this.panePreview = panePreview;
    }

    //预览回调
    public void invoke(NativeLong lRealHandle, int dwDataType, ByteByReference pBuffer, int dwBufSize, Pointer pUser) {
        W32API.HWND hwnd = new W32API.HWND(Native.getComponentPointer(panePreview)); // get preview handle
        switch (dwDataType) {
            case HCNetSDK.NET_DVR_SYSHEAD: //系统头
                if (!playCtrl.PlayM4_GetPort(playChannel)) {//获取播放库未使用的通道号
                    break;
                }
                if (dwBufSize > 0) {
                    if (!playCtrl.PlayM4_SetStreamOpenMode(playChannel.getValue(), PlayCtrl.STREAME_REALTIME)) {//设置实时流播放模式
                        break;
                    }
                    if (!playCtrl.PlayM4_OpenStream(playChannel.getValue(), pBuffer, dwBufSize, 1024 * 1024)) {//打开流接口
                        break;
                    }
                    if (!playCtrl.PlayM4_Play(playChannel.getValue(), hwnd)) {//播放开始
                        break;
                    }
                }
            case HCNetSDK.NET_DVR_STREAMDATA:   //码流数据
                if ((dwBufSize > 0) && (playChannel.getValue().longValue() != -1)) {
                    if (!playCtrl.PlayM4_InputData(playChannel.getValue(), pBuffer, dwBufSize)) {//输入流数据
                        break;
                    }
                }
        }
    }
}