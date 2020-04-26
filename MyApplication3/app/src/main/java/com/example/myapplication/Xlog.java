package com.example.myapplication;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Xlog {
    public static final String LAUNCH_TIME_TAG = "launch-time";
    public static final String PUBLIC_TAG_PREFIX = "LBVMD-";

    private static final String TAG = "Xlog";
    private static final boolean sDebug = true;
    private static final boolean sLogToFile = sDebug && true;

    private static final SimpleDateFormat sLogLineFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final SimpleDateFormat sLogFileNameFormat = new SimpleDateFormat("yyyyMMdd");

    private static boolean sFileLog = false;
    private static HandlerThread sHandlerThread;
    private static Handler sHandler;
    private static File sDir;

    static {
        if (sLogToFile) {
            sHandlerThread = new HandlerThread("FLThread");
            sHandlerThread.start();
            sHandler = new Handler(sHandlerThread.getLooper());
            sDir = new File(Environment.getExternalStorageDirectory(), "gameplugin/Xlog/");
            sFileLog = sDir.exists() && sDir.isDirectory();
        }
    }

    private static boolean isLoggable(int i) {
        return sDebug;
    }

    private static boolean isLoggable() {
        return sDebug;
    }

    private static String levelToStr(int level) {
        switch (level) {
            case android.util.Log.VERBOSE:
                return "V";
            case android.util.Log.DEBUG:
                return "D";
            case android.util.Log.INFO:
                return "I";
            case android.util.Log.WARN:
                return "W";
            case android.util.Log.ERROR:
                return "E";
            case android.util.Log.ASSERT:
                return "A";
            default:
                return "UNKNOWN";
        }
    }

    private static File getLogFile() {
        File file = new File(sDir, String.format("Log_%s_%s.log", sLogFileNameFormat.format(new Date()), android.os.Process.myPid()));
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return file;
    }

    private static void logToFileInner(int level, String tag, String format, Object[] args, Throwable tr) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(getLogFile(), true));
            String msg = String.format(format, args);
            String log = String.format("%s %s-%s/%s %s/%s %s", sLogLineFormat.format(new Date()),
                    Process.myPid(), Process.myUid(), getProcessName(), levelToStr(level), tag, msg);
            writer.println(log);
            if (tr != null) {
                tr.printStackTrace(writer);
                writer.println();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Throwable e) {
                }
            }
        }
    }

    private static void logToFile(final int level, final String tag, final String format, final Object[] args, final Throwable tr) {
        if (sLogToFile) {
            if (!sFileLog) {
                return;
            }
            sHandler.post(new Runnable() {
                @Override
                public void run() {
                    logToFileInner(level, tag, format, args, tr);
                }
            });
        }
    }

    private static String getProcessName() {
        return "?";
    }


    private static void logToFileWtf(String tag, String format, Object[] args, Throwable tr) {
        logToFile(-1, tag, format, args, tr);
    }


    public static void v(String tag, String format, Object... args) {
        v(tag, format, null, args);
    }

    public static void v(String tag, String format, Throwable tr, Object... args) {
        if (!isLoggable(android.util.Log.VERBOSE)) {
            return;
        }
        logToFile(android.util.Log.VERBOSE, tag, format, args, tr);
        if (tr == null) {
            android.util.Log.v(tag, String.format(format, args));
        } else {
            android.util.Log.v(tag, String.format(format, args), tr);
        }
    }


    public static void d(String tag, String format, Object... args) {
        d(tag, format, null, args);
    }

    public static void d(String tag, String format, Throwable tr, Object... args) {
        if (!isLoggable(android.util.Log.DEBUG)) {
            return;
        }
        logToFile(android.util.Log.DEBUG, tag, format, args, tr);
        if (tr == null) {
            android.util.Log.d(tag, String.format(format, args));
        } else {
            android.util.Log.d(tag, String.format(format, args), tr);
        }
    }

    public static void i(String tag, String format, Object... args) {
        i(tag, format, null, args);
    }

    public static void i(String tag, String format, Throwable tr, Object... args) {
        if (!isLoggable(android.util.Log.INFO)) {
            return;
        }
        logToFile(android.util.Log.INFO, tag, format, args, tr);
        if (tr == null) {
            android.util.Log.i(tag, String.format(format, args));
        } else {
            android.util.Log.i(tag, String.format(format, args), tr);
        }
    }

    public static void w(String tag, String format, Object... args) {
        w(tag, format, null, args);
    }

    public static void w(String tag, String format, Throwable tr, Object... args) {
        if (!isLoggable(android.util.Log.WARN)) {
            return;
        }
        logToFile(android.util.Log.WARN, tag, format, args, tr);
        if (tr == null) {
            android.util.Log.w(tag, String.format(format, args));
        } else {
            android.util.Log.w(tag, String.format(format, args), tr);
        }
    }

    public static void w(String tag, Throwable tr) {
        w(tag, "Xlog.warn", tr);
    }

    public static void e(String tag, String format, Object... args) {
        e(tag, format, null, args);
    }

    public static void e(String tag, String format, Throwable tr, Object... args) {
        if (!isLoggable(android.util.Log.ERROR)) {
            return;
        }
        logToFile(android.util.Log.ERROR, tag, format, args, tr);
        if (tr == null) {
            android.util.Log.e(tag, String.format(format, args));
        } else {
            android.util.Log.e(tag, String.format(format, args), tr);
        }
    }

    public static void wtf(String tag, String format, Object... args) {
        wtf(tag, format, null, args);
    }

    public static void wtf(String tag, Throwable tr) {
        wtf(tag, "wtf", tr);
    }

    public static void wtf(String tag, String format, Throwable tr, Object... args) {
        if (!isLoggable()) {
            return;
        }
        logToFileWtf(tag, format, args, tr);
        if (tr == null) {
            android.util.Log.wtf(tag, String.format(format, args));
        } else {
            android.util.Log.wtf(tag, String.format(format, args), tr);
        }
    }

    public static void logLaunchTime(String message) {
        if (!isLoggable()) {
            return;
        }
        final StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        final String fullClassName = elements[3].getClassName();
        final String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        final String methodName = elements[3].getMethodName();
        final int lineNumber = elements[3].getLineNumber();
        android.util.Log.d(LAUNCH_TIME_TAG, className + "."
                + methodName + "()@" + lineNumber + ": " + (message != null ? message : ""));
    }
}
