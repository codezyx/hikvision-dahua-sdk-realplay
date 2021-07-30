package xyz.minitool.sdk.hk;

import xyz.minitool.sdk.hk.win32.W32API;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.NativeLongByReference;

public interface PlayCtrl extends Library {

    PlayCtrl INSTANCE = (PlayCtrl) Native.loadLibrary("PlayCtrl", PlayCtrl.class);

    public static final int STREAME_REALTIME = 0;

    public static final int STREAME_FILE = 1;

    boolean PlayM4_GetPort(NativeLongByReference nPort);

    boolean PlayM4_OpenStream(NativeLong nPort, ByteByReference pFileHeadBuf, int nSize, int nBufPoolSize);

    boolean PlayM4_InputData(NativeLong nPort, ByteByReference pBuf, int nSize);

    boolean PlayM4_CloseStream(NativeLong nPort);

    boolean PlayM4_SetStreamOpenMode(NativeLong nPort, int nMode);

    boolean PlayM4_Play(NativeLong nPort, W32API.HWND hWnd);

    boolean PlayM4_Stop(NativeLong nPort);

    boolean PlayM4_SetSecretKey(NativeLong nPort, NativeLong lKeyType, String pSecretKey, NativeLong lKeyLen);
}
