package xyz.minitool.sdk.hk.win32;

import com.sun.jna.*;
import com.sun.jna.ptr.ByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;

import java.util.HashMap;
import java.util.Map;

public interface W32API extends StdCallLibrary, W32Errors {
    Map UNICODE_OPTIONS = new HashMap() {
        {
            this.put("type-mapper", W32APITypeMapper.UNICODE);
            this.put("function-mapper", W32APIFunctionMapper.UNICODE);
        }
    };
    Map ASCII_OPTIONS = new HashMap() {
        {
            this.put("type-mapper", W32APITypeMapper.ASCII);
            this.put("function-mapper", W32APIFunctionMapper.ASCII);
        }
    };
    Map DEFAULT_OPTIONS = Boolean.getBoolean("w32.ascii") ? ASCII_OPTIONS : UNICODE_OPTIONS;
    HANDLE INVALID_HANDLE_VALUE = new HANDLE(Pointer.createConstant(-1L));
    HWND HWND_BROADCAST = new HWND(Pointer.createConstant(65535L));

    public static class WPARAM extends UINT_PTR {
        public WPARAM() {
            this(0L);
        }

        public WPARAM(long value) {
            super(value);
        }
    }

    public static class UINT_PTR extends IntegerType {
        public UINT_PTR() {
            super(Pointer.SIZE);
        }

        public UINT_PTR(long value) {
            super(Pointer.SIZE, value);
        }

        public Pointer toPointer() {
            return Pointer.createConstant(this.longValue());
        }
    }

    public static class LRESULT extends LONG_PTR {
        public LRESULT() {
            this(0L);
        }

        public LRESULT(long value) {
            super(value);
        }
    }

    public static class LPARAM extends LONG_PTR {
        public LPARAM() {
            this(0L);
        }

        public LPARAM(long value) {
            super(value);
        }
    }

    public static class SIZE_T extends ULONG_PTR {
        public SIZE_T() {
            this(0L);
        }

        public SIZE_T(long value) {
            super(value);
        }
    }

    public static class ULONG_PTR extends IntegerType {
        public ULONG_PTR() {
            this(0L);
        }

        public ULONG_PTR(long value) {
            super(Pointer.SIZE, value);
        }
    }

    public static class SSIZE_T extends LONG_PTR {
        public SSIZE_T() {
            this(0L);
        }

        public SSIZE_T(long value) {
            super(value);
        }
    }

    public static class LONG_PTR extends IntegerType {
        public LONG_PTR() {
            this(0L);
        }

        public LONG_PTR(long value) {
            super(Pointer.SIZE, value);
        }
    }

    public static class HANDLEByReference extends ByReference {
        public HANDLEByReference() {
            this((HANDLE)null);
        }

        public HANDLEByReference(HANDLE h) {
            super(Pointer.SIZE);
            this.setValue(h);
        }

        public void setValue(HANDLE h) {
            this.getPointer().setPointer(0L, h != null ? h.getPointer() : null);
        }

        public HANDLE getValue() {
            Pointer p = this.getPointer().getPointer(0L);
            if (p == null) {
                return null;
            } else if (INVALID_HANDLE_VALUE.getPointer().equals(p)) {
                return INVALID_HANDLE_VALUE;
            } else {
                HANDLE h = new HANDLE();
                h.setPointer(p);
                return h;
            }
        }
    }

    public static class HRESULT extends NativeLong {
        public HRESULT() {
        }
    }

    public static class HMODULE extends HINSTANCE {
        public HMODULE() {
        }
    }

    public static class HINSTANCE extends HANDLE {
        public HINSTANCE() {
        }
    }

    public static class HWND extends HANDLE {
        public HWND() {
        }

        public HWND(Pointer p) {
            super(p);
        }
    }

    public static class HRGN extends HANDLE {
        public HRGN() {
        }
    }

    public static class HBITMAP extends HANDLE {
        public HBITMAP() {
        }
    }

    public static class HICON extends HANDLE {
        public HICON() {
        }
    }

    public static class HDC extends HANDLE {
        public HDC() {
        }
    }

    public static class LONG extends IntegerType {
        public LONG() {
            this(0L);
        }

        public LONG(long value) {
            super(Native.LONG_SIZE, value);
        }
    }

    public static class DWORD extends IntegerType {
        public DWORD() {
            this(0L);
        }

        public DWORD(long value) {
            super(4, value);
        }
    }

    public static class WORD extends IntegerType {
        public WORD() {
            this(0L);
        }

        public WORD(long value) {
            super(2, value);
        }
    }

    public static class HANDLE extends PointerType {
        private boolean immutable;

        public HANDLE() {
        }

        public HANDLE(Pointer p) {
            this.setPointer(p);
            this.immutable = true;
        }

        public Object fromNative(Object nativeValue, FromNativeContext context) {
            Object o = super.fromNative(nativeValue, context);
            return INVALID_HANDLE_VALUE.equals(o) ? INVALID_HANDLE_VALUE : o;
        }

        public void setPointer(Pointer p) {
            if (this.immutable) {
                throw new UnsupportedOperationException("immutable reference");
            } else {
                super.setPointer(p);
            }
        }
    }
}
