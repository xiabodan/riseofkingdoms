package com.dsemu.drastic;

public class DraSticJNI {
    /* renamed from: a */
    public static int f1551a = -1;
    /* renamed from: b */
    public static boolean f1552b = true;

    static {
        try {
            String str = "";
            System.loadLibrary("drastic_cpu");
            f1551a = getCpuType();
            switch (f1551a) {
                case 0:
                    str = "drastic";
                    break;
                case 1:
                    str = "drastic_compat";
                    break;
                case 2:
                    str = "drastic_x86";
                    break;
            }
            System.loadLibrary(str);
        } catch (UnsatisfiedLinkError unused) {
        }
    }

    public static native int addCustomCheat(String str, int[] iArr, int i, boolean z);

    public static native void applyConfig(long j);

    public static native void clearScreens(int i, int i2);

    public static native int extfxLoad(String str, int i, int i2);

    public static native void extfxRender(int i, int i2, int i3, int i4, int i5, int i6);

    public static native void extfxSetup(int i, int i2, int i3, int i4, int i5, int i6);

    public static native int findCustomCheat(int[] iArr, int i);

    public static native int fxLoad(String str, int i, int i2);

    public static native void fxRender(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, boolean z);

    public static native void fxSetup(int i, int i2, int i3, int i4, int i5, int i6);

    public static native int getCheatCount();

    public static native boolean getCheatEnabled(int i);

    public static native int getCheatFolderCount();

    public static native boolean getCheatFolderExpanded(int i);

    public static native int getCheatFolderId(int i);

    public static native boolean getCheatFolderMultiSelect(int i);

    public static native byte[] getCheatFolderName(int i);

    public static native byte[] getCheatFolderNote(int i);

    public static native byte[] getCheatName(int i);

    public static native byte[] getCheatNote(int i);

    public static native int getCpuType();

    public static native int getCustomCheatCount();

    public static native int[] getCustomCheatData(int i);

    public static native boolean getCustomCheatEnabled(int i);

    public static native byte[] getCustomCheatName(int i);

    public static native byte[] getDebugData();

    public static native String getDebugString();

    public static native int getFrameInfo();

    public static native String getInfoString();

    public static native int getPerformanceCounters();

    public static native boolean getRomIconData(String str, int[] iArr, byte[] bArr, byte[] bArr2);

    public static native long getRomSize(String str);

    public static native int getRomType(String str);

    public static native boolean getRumbleState();

    public static native int getSavingSlot();

    public static native void getScreenBuffers(int[] iArr, int[] iArr2);

    public static native void getSnapshots16Direct(String str, int[] iArr, int[] iArr2);

    public static native void getSnapshots16TopGreyscale(String str, int[] iArr);

    public static native String getVersionString(int i);

    public static native boolean insertGame(String str, int i, boolean z, long j);

    public static native boolean isNdsFile(String str);

    public static native boolean isSaving();

    public static native boolean loadState(int i);

    public static native int luaGetOverrides();

    public static native boolean luaIsActive();

    public static native void luaUpdateAxisValues(float f, float f2, float f3, float f4);

    public static native void luaUpdateRotation(int i);

    public static native void onInit(Object obj, int i);

    public static native void pauseSystem(int i);

    public static native void quitSystem();

    public static native void releaseSystem();

    public static native void removeCustomCheat(int i);

    public static native void renderFrame(int i, int i2, boolean z);

    public static native void resetDS();

    public static native boolean saveState(int i, boolean z);

    public static native void setAudioVolume(int i);

    public static native void setAutosaveInterval(int i);

    public static native void setCheatEnabled(int i, boolean z);

    public static native void setCheatFolderExpanded(int i, boolean z);

    public static native void setCustomCheatEnabled(int i, boolean z);

    public static native void setFirmwareUserdata(String str, int i);

    public static native void setHingeStatus(boolean z);

    public static native void setLastestSavestateData(int i, byte[] bArr);

    public static native void setUserDirectory(String str);

    public static native void setWhitenoiseFeed(boolean z);

    public static native void signalScreen();

    public static native boolean startGame(String str, String str2, int i, long j, int i2, boolean z, long j2);

    public static native void updateCheats(boolean z);

    public static native void updateInput(int i, int i2, int i3);

    public static native void waitScreen();
}