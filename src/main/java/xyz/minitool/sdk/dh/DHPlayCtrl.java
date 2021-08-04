package xyz.minitool.sdk.dh;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public interface DHPlayCtrl extends Library {

    DHPlayCtrl INSTANCE = (DHPlayCtrl)Native.loadLibrary("dhplay", DHPlayCtrl.class);

    boolean PLAY_GetFreePort(NetSDKLib.LLong playPort);

    boolean PLAY_ReleasePort(NetSDKLib.LLong playPort);

    boolean PLAY_OpenStream(NetSDKLib.LLong playPort, Pointer pBuffer, int nSize, int nBufPoolSize);

    boolean PLAY_Play(NetSDKLib.LLong playPort, Pointer hWnd);

    boolean PLAY_InputData(NetSDKLib.LLong playPort, Pointer pBuffer, int bufferSize);

    boolean PLAY_Stop(NetSDKLib.LLong playPort);

    boolean PLAY_CloseStream(NetSDKLib.LLong playPort);

    boolean PLAY_RefreshPlay(NetSDKLib.LLong playPort);

}
