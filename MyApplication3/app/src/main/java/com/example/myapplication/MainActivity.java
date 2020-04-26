package com.example.myapplication;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.slice.SliceManager;
import androidx.slice.widget.SliceView;

import android.app.Person;
import android.app.admin.DevicePolicyManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.CrossProfileApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.SharedLibraryInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.graphics.drawable.Icon;
import android.hardware.biometrics.BiometricPrompt;
import android.media.AudioManager;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.os.Bundle;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.dsemu.drastic.DraSticJNI;
import com.example.myapplication.R;
import com.hailong.biometricprompt.fingerprint.FingerprintCallback;
import com.hailong.biometricprompt.fingerprint.FingerprintVerifyManager;
import com.tencent.mm.plugin.appbrand.ui.AppBrandUI;

import org.jf.util.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

import static android.app.DownloadManager.COLUMN_LOCAL_FILENAME;

public class MainActivity extends Activity {
    public static final String TAG = "xiabo";
    public static final String tag = "xiabo";
    private int flag = 0;
    MediaBrowser mMediaBrowser = null;

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Bundle bundle = getIntent().getExtras();
        Xlog.i(TAG, "onResume bundle " + bundle);
        /*if (flag != 1) {
            Intent intent = new Intent(this, Main6Activity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.setPackage(getPackageName());
            startActivity(intent);
        }*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        testFunc(11);
        setContentView(R.layout.activity_main);

        // register dynamic broadcast
        final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("xiabo", "MyDyminicReceiver onReceive " + intent);
            }
        };
        // registerReceiver(broadcastReceiver, new IntentFilter("com.tutorialspoint.DYMINIC_INTENT"));

        // register dynamic and static broadcast
        final BroadcastReceiver broadcastReceiver1 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("xiabo", "MyDyminicReceiver1 onReceive " + intent);
            }
        };
        // registerReceiver(broadcastReceiver1, new IntentFilter("com.tutorialspoint.DYMINIC_AND_STATIC_INTENT"));
        // init();

        Bundle bundle = getIntent().getExtras();
        Xlog.i(TAG, "onCreate bundle " + bundle);
        /*if (flag != 1) {
            Intent intent = new Intent(this, Main6Activity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.setPackage(getPackageName());
            startActivity(intent);
        }*/

        findViewById(R.id.tvFingerprint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FingerprintVerifyManager.Builder builder = new FingerprintVerifyManager.Builder(MainActivity.this);
                builder.callback(fingerprintCallback)
                        .fingerprintColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary))
                        .build();
            }
        });
    }

    private FingerprintCallback fingerprintCallback = new FingerprintCallback() {
        @Override
        public void onSucceeded() {
            Toast.makeText(MainActivity.this, getString(R.string.biometricprompt_verify_success), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            Toast.makeText(MainActivity.this, getString(R.string.biometricprompt_verify_failed), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onUsepwd() {
            Toast.makeText(MainActivity.this, getString(R.string.fingerprint_usepwd), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(MainActivity.this, getString(R.string.fingerprint_cancel), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onHwUnavailable() {
            Toast.makeText(MainActivity.this, getString(R.string.biometricprompt_finger_hw_unavailable), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNoneEnrolled() {
            //弹出提示框，跳转指纹添加页面
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(getString(R.string.biometricprompt_tip))
                .setMessage(getString(R.string.biometricprompt_finger_add))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.biometricprompt_finger_add_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_FINGERPRINT_ENROLL);
                        startActivity(intent);
                    }
                }).setPositiveButton(getString(R.string.biometricprompt_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();;
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String from = (String) bundle.get("from");
            Xlog.i(TAG, "onNewIntent from " + from);
            if (android.text.TextUtils.equals(from, "Main7Activity")) {
                flag = 1;
            }
        }
        Xlog.i(TAG, "onNewIntent bundle " + bundle);
    }

    void init() {
        MediaSessionCompat mSession;
        mSession = new MediaSessionCompat(this, "MusicService");
        mSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
            }

            @Override
            public void onPause() {
                super.onPause();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
            }

            @Override
            public void onStop() {
                super.onStop();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
            }
        } );
        mSession.setActive(true);
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        try {
            MediaControllerCompat mediaController = new MediaControllerCompat(this, mSession.getSessionToken());
            setMediaController((MediaController) mediaController.getMediaController());
            Log.i(MainActivity.TAG, "setMediaController " + mediaController.getMediaController());
        } catch (RemoteException e) {
        }
    }

    void testFunc(int i) {
        Log.i("xiabo", "testFunc" + i);
        MyClass1 myClass1 = new MyClass1();
        myClass1.test1();
        MyClass2 myClass2 = new MyClass2();
        myClass2.test2();
        // System.loadLibrary("dxbasenew");
        // System.loadLibrary("x3g");
    }

    public void onClickTest(View view)  throws Exception {
        // testPermissionGET_ACCOUNTS();
        // testgetAppStandbyBucket();
        // startActivity2();
        // getDownloads();
        // testActivityRecognitionProvider();
        // testDownloadProvider();
        // testMedia();
        // testProfile();
        // testxunidashi();
        // testGetImeiForSlot();
        // showDialog();
        // installApkFiles();

        // doDex2oat();
        // testSliceManager();
        // testGetRunningProcess();
        // testOpenNotification();
        // testGetRunningProcess();
        // testJobSchedulerImpl();
        // testIsolatedProcessService();
        // testPermissionGroup();
        // testLoadJNI();
        // testDexClassLoader();
        // testLoadJNI1();
        // installMultiPackage();  // 同时安装多个apk测试
        // test_setMediaController();  // 测试setMediaController后华为手机上不能调节音量
        // startWebviewActivity();
        // testFlyme8Intent();
        // testAppUpdataFromGP();
        // testOat2dex();
        // testGetPhoneInfo();
        // test360FSDSmaps();
        // testIntegerBinderTransaction();
        // mo5463b();
        // getDownloadManagerColumnsList();
        // testCheckPermissionGroup();
        // testBindService();
        // testGetSharedLibraries();
        // testHighAPI();
        // test_kuisou();
        // testDevicePolicyManager();
        // testKeyStore();
        // testnotification();
        // testContentProvider();
        testYahfa("parameterA");
    }

    private String testYahfa(String a) {
        Log.i(TAG, "testYahfa");
        return "testYahfa end";
    }

    public void testContentProvider() {
        final Uri DOWNLOADERCONTENTPROVIDER_URI = Uri.parse("content://xiabo.MyContentProvider2");
        getContentResolver().call(DOWNLOADERCONTENTPROVIDER_URI, "MyContentProviderCall", null, new Bundle());
        try {
            ContentProviderClient contentProviderClient = getContentResolver().acquireContentProviderClient(DOWNLOADERCONTENTPROVIDER_URI);
            contentProviderClient.call("MyContentProviderCall", "arg2", new Bundle());
        } catch (Exception e) {
            e.printStackTrace();
        }

        final Uri DOWNLOADERCONTENTPROVIDER_URI_taobao = Uri.parse("content://com.taobao.taobao:channel");
        getContentResolver().call(DOWNLOADERCONTENTPROVIDER_URI_taobao, "MyContentProviderCall", null, new Bundle());
    }

    @TargetApi(28)
    public void testMessagingChange_data() {
        Notification.Builder nM1 = new Notification.Builder(getApplication(), "test")
                .setStyle(null);
    }


    int id = 0;
    @TargetApi(28)
    public void testnotification() {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        List<NotificationChannel> channels = notificationManager.getNotificationChannels();
        Log.i("xiabo", "channels " + (channels == null ? "null" : channels));
        if (channels != null) {
            for (NotificationChannel notificationChannel : channels) {
                Log.i("xiabo", "notificationChannel " + notificationChannel + ", sound " + notificationChannel.getSound());
                notificationManager.deleteNotificationChannel(notificationChannel.getId());
            }
        }
//        List<NotificationChannel> channels = notificationManager.getNotificationChannels();
//        Log.i("xiabo", "channels " + (channels == null ? "null" : channels));
//        if (channels != null) {
//            for (NotificationChannel notificationChannel : channels) {
//                Log.i("xiabo", "notificationChannel " + notificationChannel);
//            }
//        }

        Notification.Builder mBuilder = new Notification.Builder(this);
        //设置小图标
        mBuilder.setSmallIcon(R.drawable.ic_camera);
        //设置标题
        mBuilder.setContentTitle("这是标题");
        //设置通知正文
        mBuilder.setContentText("这是正文，当前ID是：" + id);
        //设置摘要
        mBuilder.setSubText("这是摘要");
        //设置是否点击消息后自动clean
        mBuilder.setAutoCancel(true);
        //显示指定文本
        mBuilder.setContentInfo("Info");
        //与setContentInfo类似，但如果设置了setContentInfo则无效果
        //用于当显示了多个相同ID的Notification时，显示消息总数
        mBuilder.setNumber(2);
        //通知在状态栏显示时的文本
        mBuilder.setTicker("在状态栏上显示的文本");
        //设置优先级
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        //自定义消息时间，以毫秒为单位，当前设置为比系统时间少一小时
        mBuilder.setWhen(System.currentTimeMillis() - 3600000);
        //设置为一个正在进行的通知，此时用户无法清除通知
        mBuilder.setOngoing(true);
        //设置消息的提醒方式，震动提醒：DEFAULT_VIBRATE     声音提醒：NotificationCompat.DEFAULT_SOUND
        //三色灯提醒NotificationCompat.DEFAULT_LIGHTS     以上三种方式一起：DEFAULT_ALL
        mBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        //设置震动方式，延迟零秒，震动一秒，延迟一秒、震动一秒
        mBuilder.setVibrate(new long[]{0, 1000, 1000, 1000});
        mBuilder.setChannelId("xiabo_1");

        StringBuilder stringBuilder = new StringBuilder("content://");
        stringBuilder.append("com.whatsapp");
        stringBuilder.append(".provider.media1/items/500");
        Uri A00 = Uri.parse(stringBuilder.toString());
        Uri.Builder appendQueryParameter = A00.buildUpon().appendQueryParameter("bucketId", "193");
        mBuilder.setSound(A00);

        int permission = checkUriPermission(Uri.parse("com.whatsapp.provider.media1"), android.os.Process.myPid(), android.os.Process.myUid(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        int permission1 = checkUriPermission(Uri.parse("com.whatsapp.provider.media"), android.os.Process.myPid(), android.os.Process.myUid(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        Log.i(TAG, "permission " + permission + " permission1 " + permission1);
        grantUriPermission("com.excelliance.demo", Uri.parse("content://com.whatsapp.provider.media1"), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        mBuilder.getExtras().putString(Notification.EXTRA_BACKGROUND_IMAGE_URI, stringBuilder.toString());

        String A0C = "com.whatsapp" + ".provider.media1";
        Uri data = ContentUris.appendId(new Uri.Builder().scheme("content").authority(A0C).appendPath("item"), 193).build();
        Uri data2 = ContentUris.appendId(new Uri.Builder().scheme("content").authority(A0C).appendPath("item"), 293).build();
        Notification.MessagingStyle style = new Notification.MessagingStyle("MessagingStyleName")
                .addMessage(new Notification.MessagingStyle.Message("message", 100, " 消息").setData("typeMime", data))
                .addMessage(new Notification.MessagingStyle.Message("message2", 200, " 消息2").setData("typeMime2", data2));
        mBuilder.setStyle(style);
        createNotificationChannel(notificationManager);
        Notification notification = mBuilder.build();

        notificationManager.notify(id++, mBuilder.build());

//        List<NotificationChannelGroup> groups = notificationManager.getNotificationChannelGroups();
//        Log.i("xiabo", "notificationChannelGroup size " + groups.size());
//        if (channels != null) {
//            for (NotificationChannelGroup group : groups) {
//                Log.i("xiabo", "notificationChannelGroup " + group.getChannels().size() + "  " + group.toString());
//            }
//        }
//
//        NotificationChannelGroup group = notificationManager.getNotificationChannelGroup("xiabo_g_1");
//        Log.i("xiabo", "group channel size " + group.getChannels().size());
//        List<NotificationChannel> groupChannels = group.getChannels();
//        if (groupChannels != null) {
//            for (NotificationChannel channel : groupChannels) {
//                Log.i("xiabo", "channel " +  channel);
//            }
//        }
    }

    @TargetApi(26)
    public void createNotificationChannel(NotificationManager manager) {
        NotificationChannelGroup notificationChannelGroup1 = new NotificationChannelGroup("xiabo_g_1", "xiabo_g_name_1");
        NotificationChannelGroup notificationChannelGroup2 = new NotificationChannelGroup("xiabo_g_2", "xiabo_g_name_2");
        manager.createNotificationChannelGroups(Arrays.asList(notificationChannelGroup1, notificationChannelGroup2));

        NotificationChannel notificationChannel = new NotificationChannel("xiabo_1", "xiabo_1_name", NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationChannel.setShowBadge(true);
        notificationChannel.setBypassDnd(true);
        notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400});
        notificationChannel.setGroup("xiabo_g_1");
        notificationChannel.setDescription("xiabo_1_Description");
        StringBuilder stringBuilder = new StringBuilder("content://");
        stringBuilder.append("com.whatsapp");
        stringBuilder.append(".provider.media1/items/500");
        Uri A00 = Uri.parse(stringBuilder.toString());
        // notificationChannel.setSound(A00, null);

        NotificationChannel notificationChannel2 = new NotificationChannel("xiabo_2", "xiabo_2_name", NotificationManager.IMPORTANCE_LOW);
        notificationChannel2.enableLights(true);
        notificationChannel2.enableVibration(true);
        notificationChannel2.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationChannel2.setShowBadge(true);
        notificationChannel2.setBypassDnd(true);
        notificationChannel2.setVibrationPattern(new long[]{100, 200, 300, 400});
        notificationChannel2.setGroup("xiabo_g_2");
        notificationChannel2.setDescription("xiabo_2_Description");


        NotificationChannel notificationChannel3 = new NotificationChannel("xiabo_3", "xiabo_3_name", NotificationManager.IMPORTANCE_MAX);
        notificationChannel3.enableLights(true);
        notificationChannel3.enableVibration(true);
        notificationChannel3.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationChannel3.setShowBadge(true);
        notificationChannel3.setBypassDnd(true);
        notificationChannel3.setGroup("xiabo_g_2");
        notificationChannel3.setVibrationPattern(new long[]{100, 200, 300, 400});
        notificationChannel3.setDescription("xiabo_3_Description");

        List<NotificationChannel> notificationChannels = new ArrayList<>();
        notificationChannels.add(notificationChannel);
//        notificationChannels.add(notificationChannel2);
//        notificationChannels.add(notificationChannel3);

        manager.createNotificationChannels(notificationChannels);
    }

    public interface Consumer<T> {
        void accept(T t);
    }

    private void visitGrantableUri(Object uri, boolean userOverriddenUri) {
        Log.i(TAG, "visitGrantableUri uri " + uri);
    }

    class Noti {
        private void visitUris(Consumer<Uri> uri) {
            Log.i(TAG, "Noti visitUris uri " + uri);
        }
    }

    private void testKeyStore() {
        Noti notification = new Noti();
        Log.i(TAG, "providers: " + Arrays.toString(Security.getProviders()));
    }

    private void testDevicePolicyManager() {
        try {
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            Method method = DevicePolicyManager.class.getMethod("isDeviceProvisioned");
            boolean isDeviceProvisione = (boolean) method.invoke(devicePolicyManager, null);
            Xlog.i(TAG, "isDeviceProvisioned " + isDeviceProvisione);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private void test_kuisou() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(getPackageName(), "com.example.myapplication.CameraActivity"));
        intent.setPackage(getPackageName());
        startActivity(intent);
    }

    void testHighAPI() {
        List<SharedLibraryInfo> infos = new ArrayList<>();
        // final List<SharedLibraryInfo> libraries = getPackageManager().getSharedLibraries(PackageManager.MATCH_ALL);
        // Log.i(TAG, "libraries " + Arrays.toString(libraries.toArray()));
        List<SharedLibraryInfo> infos1 = new ArrayList<>();
        infos.addAll(infos1);
    }

    void testGetSharedLibraries() {
        final String[] libraryNames = getPackageManager().getSystemSharedLibraryNames();
        Log.i(TAG, "libraryNames " + Arrays.toString(libraryNames));
        final List<SharedLibraryInfo> sharedlibraryNames = getPackageManager().getSharedLibraries(PackageManager.MATCH_ALL);
        for (SharedLibraryInfo info : sharedlibraryNames) {
            Log.i(TAG, "info " + info.getName() + " version " + info.getLongVersion());
            try {
                Method method = SharedLibraryInfo.class.getMethod("getAllCodePaths");
                Object object = method.invoke(info, null);
                if (object != null) {
                    List<String> paths = (List<String>) object;
                    Log.i(TAG, "paths " + Arrays.toString(paths.toArray()));
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.android.chrome", PackageManager.GET_SIGNATURES | PackageManager.GET_SHARED_LIBRARY_FILES);
            final String[] sharelibs = info.applicationInfo.sharedLibraryFiles;
            Log.i(TAG, "chrome sharelibs " + Arrays.toString(sharelibs));

            PackageInfo info1 = getPackageManager().getPackageInfo("com.google.android.trichromelibrary", PackageManager.GET_SIGNATURES | PackageManager.GET_SHARED_LIBRARY_FILES);
            Log.i(TAG, "trichromelibrary info1 " + info1 + " " + info1.applicationInfo.sourceDir);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    void testBindService() {
        Intent intent = new Intent(this, MyService.class);
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(TAG, "onServiceConnected " + name + ", service " + service);
                IMyAidlInterface iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
                try {
                    IBinder iBinder2 = iMyAidlInterface.getIBinder(11);
                    Log.i(TAG, "onServiceConnected " + name + ", iBinder2 " + iBinder2);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
//        bindIsolatedService(intent, BIND_AUTO_CREATE, "0", null, serviceConnection);
//        bindIsolatedService(intent, BIND_AUTO_CREATE, "0", null, serviceConnection);
        //bindIsolatedService(intent, BIND_AUTO_CREATE, "1", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "1", null, serviceConnection);
        //bindIsolatedService(intent, BIND_AUTO_CREATE, "2", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "2", null, serviceConnection);
        //bindIsolatedService(intent, BIND_AUTO_CREATE, "3", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "3", null, serviceConnection);
        //bindIsolatedService(intent, BIND_AUTO_CREATE, "4", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "4", null, serviceConnection);
        //bindIsolatedService(intent, BIND_AUTO_CREATE, "5", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "5", null, serviceConnection);
        //bindIsolatedService(intent, BIND_AUTO_CREATE, "6", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "6", null, serviceConnection);
        //bindIsolatedService(intent, BIND_AUTO_CREATE, "7", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "7", null, serviceConnection);
        //bindIsolatedService(intent, BIND_AUTO_CREATE, "8", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "8", null, serviceConnection);
        //bindIsolatedService(intent, BIND_AUTO_CREATE, "9", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "9", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "10", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "11", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "12", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "13", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "14", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "15", null, serviceConnection);

        bindIsolatedService(intent, BIND_AUTO_CREATE, "16", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "17", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "18", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "19", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "20", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "21", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "22", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "23", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "24", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "25", null, serviceConnection);
        bindIsolatedService(intent, BIND_AUTO_CREATE, "26", null, serviceConnection);
    }

    void testCheckPermissionGroup() {
        int r = checkSelfPermission("android.permission-group.PHONE");
        Log.i(TAG, "checkSelfPermission android.permission-group.PHONE " + r);
        r = checkSelfPermission("android.permission.READ_PHONE_STATE");
        Log.i(TAG, "checkSelfPermission android.permission.READ_PHONE_STATE " + r);
    }

    String[] getDownloadManagerColumnsList() {
        String[] UNDERLYING_COLUMNS = null;
        try {
            Field UNDERLYING_COLUMNS_FIELD = android.app.DownloadManager.class.getDeclaredField("UNDERLYING_COLUMNS");
            UNDERLYING_COLUMNS_FIELD.setAccessible(true);
            Object UNDERLYING_COLUMNS_OBJECT = UNDERLYING_COLUMNS_FIELD.get(null);
            UNDERLYING_COLUMNS = UNDERLYING_COLUMNS_OBJECT instanceof String[] ? (String[]) UNDERLYING_COLUMNS_OBJECT : null;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "getDownloadManagerColumnsList " + Arrays.toString(UNDERLYING_COLUMNS));

        return UNDERLYING_COLUMNS;
    }


    public final class frq {
        /* renamed from: a */
        public final long f6963a;
        /* renamed from: b */
        public final String f6964b;
        /* renamed from: c */
        public final String f6965c;
        /* renamed from: d */
        public final String f6966d;
        /* renamed from: e */
        public final int f6967e;
        /* renamed from: f */
        public final int f6968f;
        /* renamed from: g */
        public final long f6969g;
        /* renamed from: h */
        public final long f6970h;

        public frq(long j, String str, String str2, String str3, int i, int i2, long j2, long j3) {
            this.f6963a = j;
            this.f6964b = str;
            this.f6965c = str2;
            this.f6966d = str3;
            this.f6967e = i;
            this.f6968f = i2;
            this.f6969g = j2;
            this.f6970h = j3;
        }
    }

    public final synchronized void mo5463b() {
        synchronized (this) {
            int count;
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Cursor query = downloadManager.query(new DownloadManager.Query().setFilterByStatus(31));
            Map hashMap = new HashMap();
            if (query != null) {
                for (count = query.getCount(); count > 0; count--) {
                    if (query.moveToNext()) {
                        // Long valueOf = Long.valueOf(this.f6975c.mo5185b(Long.valueOf(query.getLong(query.getColumnIndex("_id")))));
                        Long valueOf = new Long(2);
                        if (valueOf.longValue() >= 0) {
                            Log.i(TAG, "+++++++++++++++++");
                            Log.i(TAG, "ColumnCount " + query.getColumnCount());
                            int index = query.getColumnIndex("uri");
                            Log.i(TAG, index + " uri " + query.getString(index));

                            index = query.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                            Log.i(TAG,  "local_uri " + index);
                            long destinationTypeIndex = query.getColumnIndex("destination");
                            Log.i(TAG,  "destinationType " + destinationTypeIndex);

                            // Log.i(TAG, index + " local_uri " + query.getString(index));

                            Log.i(TAG, "title " + query.getString(query.getColumnIndex("title")));
                            Log.i(TAG, "status " + query.getInt(query.getColumnIndex("status")));
                            // Log.i(TAG, "reason " + query.getInt(query.getColumnIndex("reason")));
                            // Log.i(TAG, "total_size " + query.getLong(query.getColumnIndex("total_size")));
                            Log.i(TAG, "bytes_so_far " + query.getLong(query.getColumnIndex("bytes_so_far")));
                            Log.i(TAG, "-----------------");
                        }
                    }
                }
                query.close();
            } else {
            }
        }
    }

    public void testIntegerBinderTransaction() {
        Intent intent = new Intent(this, MyService.class);
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(TAG, "onServiceConnected " + name + ", service " + service);
                IMyAidlInterface iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
                try {
                    IBinder iBinder2 = iMyAidlInterface.getIBinder(11);
                    Log.i(TAG, "onServiceConnected " + name + ", iBinder2 " + iBinder2);
                    IMyAidlInterface2 iMyAidlInterface2 = IMyAidlInterface2.Stub.asInterface(iBinder2);
                    iMyAidlInterface2.testBinder2(88);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    public void test360FSDSmaps() {
        String string = stringFromJNI();
        Log.i(TAG, "stringFromJNI " + string);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public static void logi(String tag, String format, Object... args) {
        android.util.Log.i(tag, String.format(format, args));
    }

    /**
     * 复制一个目录及其子目录、文件到另外一个目录
     * @param src
     * @param dest
     * @throws IOException
     */
    private void copyFolder(File src, File dest) throws IOException {
        if (src.isDirectory()) {
            if (!dest.exists()) {
                dest.mkdir();
            }
            String files[] = src.list();
            if (files != null) {
                for (String file : files) {
                    File srcFile = new File(src, file);
                    File destFile = new File(dest, file);
                    // 递归复制
                    copyFolder(srcFile, destFile);
                }
            }
        } else {
            if (src != null) {
                InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dest);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
                in.close();
                out.close();
            }
        }
    }

    private void testGetPhoneInfo() {
        testGetImeiForSlot();
        logi(tag, "Build.BOARD: %s", Build.BOARD);
        logi(tag, "Build.MODEL: %s", Build.MODEL);
        logi(tag, "Build.PRODUCT: %s", Build.PRODUCT);
        logi(tag, "Build.MANUFACTURER: %s", Build.MANUFACTURER);
        logi(tag, "Build.SERIAL: %s", Build.SERIAL);
        logi(tag, "Build.DEVICE: %s", Build.DEVICE);
        logi(tag, "Build.FINGERPRINT: %s", Build.FINGERPRINT);
        String ANDROID_ID = Settings.System.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String ANDROID_ID1 = Settings.System.getString(getContentResolver(), Settings.System.ANDROID_ID);
        logi(tag, "ANDROID_ID: %s", ANDROID_ID);
        logi(tag, "ANDROID_ID1: %s", ANDROID_ID1);

        String path_360 = "/data/data/com.qihoo.magic/Plugin/com.tencent.mm/data/com.tencent.mm";
        File dir = new File(path_360);
        Log.i(TAG, "dir " + dir + " canRead " + dir.canRead() + " exists " + dir.exists());
        if (dir.exists() && dir.canRead()) {
            File [] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    Log.i(TAG, "file " + file.getAbsolutePath());
                }
            }

            String path_360_1 = "/data/data/com.qihoo.magic/Plugin/com.tencent.mm/data/com.tencent.mm/MicroMsg";
            File dir1 = new File(path_360_1);
            File dest1 = new File("/sdcard/Android/data/xxxx/MicroMsg");

            String path_360_2 = "/data/data/com.qihoo.magic/Plugin/com.tencent.mm/data/com.tencent.mm/shared_prefs";
            File dir2 = new File(path_360_2);
            File dest2 = new File("/sdcard/Android/data/xxxx/shared_prefs");

            String path_360_3 = "/data/data/com.qihoo.magic/Plugin/com.tencent.mm/data/com.tencent.mm/.auth_cache";
            File dir3 = new File(path_360_3);
            File dest3 = new File("/sdcard/Android/data/xxxx/.auth_cache");

            String path_360_4 = "/data/data/com.qihoo.magic/Plugin/com.tencent.mm/data/com.tencent.mm/.vfs";
            File dir4 = new File(path_360_4);
            File dest4 = new File("/sdcard/Android/data/xxxx/.vfs");

            String path_360_5 = "/data/data/com.qihoo.magic/Plugin/com.tencent.mm/data/com.tencent.mm/databases";
            File dir5 = new File(path_360_5);
            File dest5 = new File("/sdcard/Android/data/xxxx/databases");

            File dest = new File("/sdcard/Android/data/xxxx/");
            if (!dest.exists()) {
                dest.mkdir();
            }
            try {
//                copyFolder(dir1, dest1);
//                copyFolder(dir2, dest2);
//                copyFolder(dir3, dest3);
//                copyFolder(dir4, dest4);
//                copyFolder(dir5, dest5);
                copyFolder(dir, dest);  //全部拷贝
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "copy done");
        }

        /*String path_shuangkai = "/data/data/com.excelliance.dualaid/gameplugins/com.tencent.mm/";
        File dir_shuangkai = new File(path_shuangkai);
        Log.i(TAG, "dir_shuangkai " + dir + " canRead " + dir_shuangkai.canRead() + " exists " + dir_shuangkai.exists());
        if (dir_shuangkai.exists() && dir_shuangkai.canRead()) {
            File src = new File("/sdcard/Android/data/xxxx/");
            try {
                copyFolder(src, dir_shuangkai);
            } catch (IOException e) {
                e.printStackTrace();
            }
            File [] files = dir_shuangkai.listFiles();
            if (files != null) {
                for (File file : files) {
                    Log.i(TAG, "file shuangkai " + file.getAbsolutePath());
                }
            }
        }*/
        String path_shuangkai = "/data/data/com.excelliance.dualaid/gameplugins/com.tencent.mm/";
        File dir_shuangkai = new File(path_shuangkai);
        Log.i(TAG, "dir_shuangkai " + dir + " canRead " + dir_shuangkai.canRead() + " exists " + dir_shuangkai.exists());
        if (dir_shuangkai.exists() && dir_shuangkai.canRead()) {
            String path_shuangkai1 = "/data/data/com.excelliance.dualaid/gameplugins/com.tencent.mm/MicroMsg";
            String path_shuangkai2 = "/data/data/com.excelliance.dualaid/gameplugins/com.tencent.mm/shared_prefs";
            String path_shuangkai3 = "/data/data/com.excelliance.dualaid/gameplugins/com.tencent.mm/.auth_cache";
            String path_shuangkai4 = "/data/data/com.excelliance.dualaid/gameplugins/com.tencent.mm/.vfs";
            String path_shuangkai5 = "/data/data/com.excelliance.dualaid/gameplugins/com.tencent.mm/databases";
            File dir_shuangkai1 = new File(path_shuangkai1);
            File dir_shuangkai2 = new File(path_shuangkai2);
            File dir_shuangkai3 = new File(path_shuangkai3);
            File dir_shuangkai4 = new File(path_shuangkai4);
            File dir_shuangkai5 = new File(path_shuangkai5);
            File src1 = new File("/sdcard/Android/data/xxxx/MicroMsg");
            File src2 = new File("/sdcard/Android/data/xxxx/shared_prefs");
            File src3 = new File("/sdcard/Android/data/xxxx/.auth_cache");
            File src4 = new File("/sdcard/Android/data/xxxx/.vfs");
            File src5 = new File("/sdcard/Android/data/xxxx/databases");
            try {
                copyFolder(src1, dir_shuangkai1);
                copyFolder(src2, dir_shuangkai2);
                copyFolder(src3, dir_shuangkai3);
                copyFolder(src4, dir_shuangkai4);
                copyFolder(src5, dir_shuangkai5);
            } catch (IOException e) {
                e.printStackTrace();
            }
            File [] files1 = dir_shuangkai.listFiles();
            if (files1 != null) {
                for (File file : files1) {
                    Log.i(TAG, "file dir_shuangkai " + file.getAbsolutePath());
                }
            }
        }
    }

    public void testOat2dex() {
        // aaaaa();
        bbbbb();
        // ccccc();
    }

    void aaaaa() {
        String apkPath = "/data/data/com.example.myapplication/files/HwVoipService.apk";
        File odexFile64 = new File("/data/data/com.example.myapplication/files/HwVoipService.odex");
        File outApkFile = new File("/data/data/com.example.myapplication/files/HwVoipService.oatdex.apk");
        List<Utils.DexEntry> entries = null;
        try {
            entries = OatFileParser.getDexEntries(odexFile64);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (entries != null && entries.size() > 0) {
            try {
                Utils.buildIntactApk(outApkFile.getAbsolutePath(), Arrays.asList(new String[]{apkPath}), entries);
            } catch (Exception e) {
                outApkFile.delete();
                e.printStackTrace();
            }
        }
    }

    void bbbbb() {
        String apkPath = "/data/data/com.example.myapplication/files/HMS.apk";
        File odexFile64 = new File("/data/data/com.example.myapplication/files/HMS.odex");
        File outApkFile = new File("/data/data/com.example.myapplication/files/HMS.oatdex.apk");
        List<Utils.DexEntry> entries = null;
        try {
            entries = OatFileParser.getDexEntries(odexFile64);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (entries != null && entries.size() > 0) {
            try {
                Utils.buildIntactApk(outApkFile.getAbsolutePath(), Arrays.asList(new String[]{apkPath}), entries);
            } catch (Exception e) {
                outApkFile.delete();
                e.printStackTrace();
            }
        }
    }

    void ccccc() {
        String apkPath = "/data/data/com.example.myapplication/files/PaymentService.apk";
        File odexFile64 = new File("/data/data/com.example.myapplication/files/PaymentService.odex");
        File outApkFile = new File("/data/data/com.example.myapplication/files/PaymentService.oatdex.apk");
        List<Utils.DexEntry> entries = null;
        try {
            entries = OatFileParser.getDexEntries(odexFile64);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (entries != null && entries.size() > 0) {
            try {
                Utils.buildIntactApk(outApkFile.getAbsolutePath(), Arrays.asList(new String[]{apkPath}), entries);
            } catch (Exception e) {
                outApkFile.delete();
                e.printStackTrace();
            }
        }
    }

    public void testAppUpdataFromGP() {
        Intent intent = new Intent(this, Main5Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.setPackage(getPackageName());
        startActivity(intent);
    }

    public void testFlyme8Intent() {
        Intent intent = new Intent(this, AppBrandUI.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("key_appbrand_init_config", Main5Activity.class);
        ComponentName componentName = new ComponentName(getPackageName(), "com.tencent.mm.plugin.appbrand.ui.AppBrandUI");
        intent.setComponent(componentName);
        startActivity(intent);
    }

    public void startWebviewActivity() {
        Intent intent = new Intent(this, Main4Activity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(getPackageName());
        startActivity(intent);
    }

    public void test_setMediaController() {
        Intent intent = new Intent(this, Main3Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(getPackageName());
        startActivity(intent);
    }

    public void testLoadJNI1() {
        String key = DraSticJNI.getDebugString();
        Log.i(TAG, "key:" + key);
    }

    public void testDexClassLoader() {
        final File optimizedDexOutputPath = new File(Environment
                .getExternalStorageDirectory().toString()
                + File.separator
                + "libsgmain_315417600000.zip");
        DexClassLoader cl = new DexClassLoader(optimizedDexOutputPath.getAbsolutePath(), getFilesDir().getAbsolutePath(), null, getClassLoader());
        // DexClassLoader cl = new DexClassLoader(optimizedDexOutputPath.getAbsolutePath(), null, null, getClassLoader());
        // PathClassLoader cl = new PathClassLoader(optimizedDexOutputPath.getAbsolutePath(), optimizedDexOutputPath.getAbsolutePath(), getClassLoader());
        Class Clazz = null;

        try {
            Clazz = cl.loadClass("com.dynamic.DynamicTest");
            Object lib = Clazz.newInstance();
            Method m = Clazz.getDeclaredMethod("hehe");
            m.invoke(lib);
        } catch (Exception exception) {
            // Handle exception gracefully here.
            exception.printStackTrace();
        }
    }

    public void testLoadJNI() {
        System.loadLibrary("main");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.i(TAG, "onRequestPermissionsResult 1 " + Arrays.toString(permissions));
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @TargetApi(23)
    private void testPermissionGroup() {
        int permission = checkPermission(Manifest.permission.READ_CONTACTS, android.os.Process.myPid(), android.os.Process.myUid());
        Log.i(TAG, "checkPermission Manifest.permission.READ_CONTACTS " + permission);
        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1);

        int permission1 = checkPermission(Manifest.permission.WRITE_CONTACTS, android.os.Process.myPid(), android.os.Process.myUid());
        int permission2 = checkPermission(Manifest.permission.GET_ACCOUNTS, android.os.Process.myPid(), android.os.Process.myUid());
        Log.i(TAG, "checkPermission Manifest.permission.WRITE_CONTACTS " + permission1);
        Log.i(TAG, "checkPermission Manifest.permission.GET_ACCOUNTS " + permission2);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.i(TAG, "onServiceConnected " + name + " binder " + binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceConnected " + name);
        }
    };

    private ServiceConnection conn1 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.i(TAG, "onServiceConnected " + name + " binder " + binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceConnected " + name);
        }
    };

    @TargetApi(29)
    public void testIsolatedProcessService() {
        Intent intent = new Intent(this, SandboxedProcessService0.class);
        bindIsolatedService(intent, BIND_AUTO_CREATE|BIND_EXTERNAL_SERVICE, "0", null, conn);

        // Intent intent1 = new Intent(this, SandboxedProcessService1.class);
        // bindIsolatedService(intent1, BIND_AUTO_CREATE|BIND_EXTERNAL_SERVICE , "1", null, conn1);

        // Intent intent3 = new Intent(this, SandboxedProcessService1.class);
        // bindService(intent3, conn1, BIND_AUTO_CREATE|BIND_EXTERNAL_SERVICE );

        // Intent intent2 = new Intent();
        // intent2.setPackage("com.android.chrome");
        // intent2.setComponent(new ComponentName("com.android.chrome", "org.chromium.content.app.SandboxedProcessService4"));
        // bindIsolatedService(intent2, BIND_AUTO_CREATE|BIND_EXTERNAL_SERVICE , "4", null, conn1);
    }

    @TargetApi(29)
    public void onClickAddTab1(View view) {
        Intent intent = new Intent(this, SandboxedProcessService0.class);
        bindIsolatedService(intent, BIND_AUTO_CREATE|BIND_EXTERNAL_SERVICE, "1", null, conn);
    }

    @TargetApi(29)
    public void onClickAddTab2(View view) {
        Intent intent = new Intent(this, SandboxedProcessService0.class);
        bindIsolatedService(intent, BIND_AUTO_CREATE|BIND_EXTERNAL_SERVICE, "0", null, conn);
    }


    @TargetApi(21)
    public void testJobSchedulerImpl() {
        try {
            Log.i(TAG, "ro.serialno " + Build.getSerial());
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        ComponentName mServiceComponent;
        int mJobId = 0;
        // 根据JobService创建一个ComponentName对象
        mServiceComponent = new ComponentName(this, MyJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(mJobId++, mServiceComponent);
        builder.setMinimumLatency(1000);  // 设置延迟调度时间
        builder.setOverrideDeadline(2000);  // 设置该Job截至时间，在截至时间前肯定会执行该Job
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);  // 设置所需网络类型
        builder.setRequiresDeviceIdle(true);  // 设置在DeviceIdle时执行Job
        builder.setRequiresCharging(true);  // 设置在充电时执行Job

        JobScheduler jobScheduler = (JobScheduler) this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            jobScheduler.schedule(builder.build());  //  调度Job
            List<JobInfo> allPendingJobs = jobScheduler.getAllPendingJobs();
            if (allPendingJobs != null) {
                for (JobInfo jobInfo : allPendingJobs) {
                    Log.i(TAG, "getAllPendingJobs jobInfo " + jobInfo);
                    if (jobInfo.getExtras().containsKey("EXTRA_WORK_SPEC_ID")) {
                        jobScheduler.cancel(jobInfo.getId());
                    }
                }
                return;
            }
            return;
        }
    }

    public void testOpenNotification() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);

        //for Android 5-7
        intent.putExtra("app_package", getPackageName());
        intent.putExtra("app_uid", getApplicationInfo().uid);

        // for Android 8 and above
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());

        startActivity(intent);
    }

    public void testGetRunningProcess() {
        int check = checkPermission("android.permission.INTERACT_ACROSS_USERS_FULL", android.os.Process.myPid(), android.os.Process.myUid());
        Log.i(TAG, "INTERACT_ACROSS_USERS_FULL " + check);
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
        if (appProcessInfos != null) {
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : appProcessInfos) {
                Log.i(TAG, "runningAppProcessInfo " + runningAppProcessInfo.processName + " " + runningAppProcessInfo.pid + " " + runningAppProcessInfo.uid);
            }
        }
    }

    @TargetApi(28)
    public void testSliceManager() {
        String packageName = getPackageName();
        StringBuilder stringBuilder = new StringBuilder(String.valueOf(packageName).length() + 100);
        stringBuilder.append("content://");
        stringBuilder.append("com.example.android.slice");
        stringBuilder.append("/hello");
        Uri parse = Uri.parse(stringBuilder.toString());
        SliceManager.getInstance(this).grantSlicePermission("com.example.android.sliceviewer", parse);
        SliceManager.getInstance(this).checkSlicePermission( parse, android.os.Process.myPid(), android.os.Process.myUid());
    }

    @TargetApi(24)
    public void doDex2oat() {
        // dex2oat-cmdline = --debuggable --instruction-set=arm --instruction-set-features=div,atomic_ldrd_strd,-armv8a --runtime-arg -Xrelocate --boot-image=/system/framework/boot.art --runtime-arg -Xms64m --runtime-arg -Xmx512m --instruction-set-variant=kryo --instruction-set-features=default --debuggable --dex-file=/data/data/com.excelliance.dualaid/gameplugins/3/com.starbucks.cn/app_tbs/core_share/tbs_jars_fusion_dex.jar --output-vdex-fd=179 --oat-fd=180 --oat-location=/data/data/com.excelliance.dualaid/gameplugins/3/com.starbucks.cn/app_tbs/core_share/oat/arm/tbs_jars_fusion_dex.odex --compiler-filter=quicken --class-loader-context=&

        // dex2oat-cmdline = --zip-fd=16 --zip-location=tbs_jars_fusion_dex.jar --input-vdex-fd=18 --output-vdex-fd=19 --oat-fd=17 --oat-location=/data/data/com.excelliance.dualaid/gameplugins/com.starbucks.cn/app_tbs/core_share/oat/arm/tbs_jars_fusion_dex.odex --instruction-set=arm --instruction-set-variant=kryo --instruction-set-features=default --runtime-arg -Xms64m --runtime-arg -Xmx512m --compiler-filter=speed --swap-fd=20 --classpath-dir=/data/data/com.excelliance.dualaid/gameplugins/com.starbucks.cn/app_tbs/core_share --class-loader-context=&

        String dex2oatCmdStr = "--debuggable --instruction-set=arm --instruction-set-features=div,atomic_ldrd_strd,-armv8a --runtime-arg -Xrelocate --boot-image=/system/framework/boot.art --runtime-arg -Xms64m --runtime-arg -Xmx512m --instruction-set-variant=kryo --instruction-set-features=default --debuggable --dex-file=/data/data/com.excelliance.dualaid/gameplugins/3/com.starbucks.cn/app_tbs/core_share/tbs_jars_fusion_dex.jar ";

        try {
            File file = getFilesDir();
            File intput_jar =  new File(getDataDir(), "files/tbs_jars_fusion_dex.jar");
            File output_vdex = new File(getDataDir(), "files/tbs_jars_fusion_dex.vdex");
            File output_odex = new File(getDataDir(), "files/tbs_jars_fusion_dex.odex");
            FileOutputStream intput_jar_out = new FileOutputStream(intput_jar);
            java.io.FileDescriptor fd =  intput_jar_out.getFD();
            Log.i(TAG, "intput_jar_out " + fd);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        dex2oatCmdStr += String.format(" --output-vdex-fd==%d", sharedLibraries);
//        Runtime.getRuntime().exec(dex2oatCmdStr).waitFor();
    }

    void showDialog() {
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(getApplicationContext());
        AlertDialog alear = dialog.setIcon(R.drawable.ic_launcher_background).setTitle(R.string.app_name)
                .setMessage(R.string.app_name).create();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            alear.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        } else {
            alear.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
        }
        alear.getWindow().setType(WindowManager.LayoutParams.TYPE_PRIORITY_PHONE);
        // alear.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        // alear.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        alear.show();
    }

    @TargetApi(29)
    private static PackageInstaller.SessionParams createSessionParams(
            boolean staged, boolean multiPackage, boolean enableRollback, boolean inherit) {
        final int sessionMode = inherit
                ? PackageInstaller.SessionParams.MODE_INHERIT_EXISTING
                : PackageInstaller.SessionParams.MODE_FULL_INSTALL;
        final PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(sessionMode);
        if (multiPackage) {
            params.setMultiPackage();
        }
        return params;
    }

    private int createSessionId(String[] apkFileNames, PackageInstaller.SessionParams params)
            throws Exception {
        PackageInstaller packageInstaller = getPackageManager().getPackageInstaller();
        final int sessionId = packageInstaller.createSession(params);
        if (apkFileNames == null) {
            return sessionId;
        }

        ArrayList<FileDescriptor> descriptors = new ArrayList<>(apkFileNames.length);
        for (String f : apkFileNames) {
            Log.i(TAG, "apkSource:" + f);
            descriptors.add(new NormalFileDescriptor(new File(f)));
        }

        ApkSource apkSource = new DefaultApkSource(descriptors);
        final PackageInstaller.Session session = packageInstaller.openSession(sessionId);
        while (apkSource.nextApk()) {
            Log.i(TAG, "apkSource:" + apkSource.getApkName());
            try (OutputStream packageInSession = session.openWrite(apkSource.getApkName(), 0, -1);
                 final InputStream is = apkSource.openApkInputStream()) {
                final byte[] buffer = new byte[4096];
                int n;
                while ((n = is.read(buffer)) >= 0) {
                    packageInSession.write(buffer, 0, n);
                }
            }
        }
        return sessionId;
    }

    private PackageInstaller.Session getSessionOrFail(int sessionId) throws Exception {
        final PackageInstaller packageInstaller = getPackageManager().getPackageInstaller();
        return packageInstaller.openSession(sessionId);
    }

    @TargetApi(29)
    private void installMultiPackage() throws Exception {
        String[][] resources = {
                {"/data/local/tmp/base0.apk", "/data/local/tmp/split_config.en.0.apk", "/data/local/tmp/split_config.zh.0.apk"},
                {"/data/local/tmp/base1.apk"}
        };

        final PackageInstaller.SessionParams parentSessionParams =
                createSessionParams(/*staged*/false, /*multipackage*/true,
                        /*enableRollback*/false, /*inherit*/ false);
        final int parentSessionId = createSessionId(/*apkFileName*/null, parentSessionParams);
        final PackageInstaller.Session parentSession = getSessionOrFail(parentSessionId);

        ArrayList<Integer> childSessionIds = new ArrayList<>(resources.length);

        for (String[] apkSource : resources) {
            final PackageInstaller.SessionParams childSessionParams =
                    createSessionParams(/*staged*/false, /*multipackage*/false,
                            /*enableRollback*/false, /*inherit*/ false);
            final int childSessionId = createSessionId(apkSource, childSessionParams);
            childSessionIds.add(childSessionId);
            parentSession.addChildSessionId(childSessionId);

            Log.i(TAG, "childSessionId:" + childSessionId);
            PackageInstaller.Session childSession = getSessionOrFail(childSessionId);
            Log.i(TAG, "" + childSession.getParentSessionId() + " " + parentSessionId);
            Log.i(TAG, "" + getSessionInfoOrFail(childSessionId).getParentSessionId() + " " + parentSessionId);
        }


        int [] cs = parentSession.getChildSessionIds();
        Log.i(TAG, "cs:" + Arrays.toString(cs));
        // Commit the session (this will start the installation workflow).

        Intent callbackIntent = new Intent(this, RootlessSAIPIService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, callbackIntent, 0);
        // parentSession.commit(pendingIntent.getIntentSender());
        parentSession.commit(LocalIntentSender.getIntentSender(this));
        Log.i(TAG, "installMultiPackage:" + LocalIntentSender.getIntentSenderResult());
    }

    private PackageInstaller.SessionInfo getSessionInfoOrFail(int sessionId)
            throws Exception {
        final PackageInstaller packageInstaller = getPackageManager().getPackageInstaller();
        return packageInstaller.getSessionInfo(sessionId);
    }

    /**
     * Creates an empty install session with appropriate install params set.
     *
     * @return the session id of the newly created session
     */
    @TargetApi(29)
    private int createEmptyInstallSession(boolean multiPackage, boolean isApex)
            throws IOException {
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        if (multiPackage) {
            params.setMultiPackage();
        }
        return getPackageManager().getPackageInstaller().createSession(params);
    }

    @TargetApi(29)
    protected void installApkFiles() {
        List<String> apks = new ArrayList<>(3);
        apks.add("/data/local/tmp/base0.apk");
        apks.add("/data/local/tmp/split_config.en.0.apk");
        apks.add("/data/local/tmp/split_config.zh.0.apk");

        PackageInstaller.Session session = null;
        PackageInstaller.Session session2 = null;
        try {
            PackageInstaller mPackageInstaller = getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            sessionParams.setInstallLocation(PackageInfo.INSTALL_LOCATION_AUTO);

            int sessionID = mPackageInstaller.createSession(sessionParams);

            session = mPackageInstaller.openSession(sessionID);

            ArrayList<FileDescriptor> descriptors = new ArrayList<>(apks.size());
            for (String f : apks) {
                descriptors.add(new NormalFileDescriptor(new File(f)));
            }

            ApkSource apkSource = new DefaultApkSource(descriptors);
            while (apkSource.nextApk()) {
                try (InputStream inputStream = apkSource.openApkInputStream(); OutputStream outputStream = session.openWrite(apkSource.getApkName(), 0, apkSource.getApkLength())) {
                    IOUtils.copyStream(inputStream, outputStream);
                    session.fsync(outputStream);
                }
            }

            Intent callbackIntent = new Intent(this, RootlessSAIPIService.class);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, callbackIntent, 0);
            session.commit(pendingIntent.getIntentSender());
        } catch (Exception e) {
            Log.w(TAG, e);
        } finally {
            if (session != null)
                session.close();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                    WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                    params.type = WindowManager.LayoutParams.TYPE_PHONE;
                    params.format = PixelFormat.RGBA_8888;
                    // windowManager.addView(view, params);
                } else {
                    Toast.makeText(this, "ACTION_MANAGE_OVERLAY_PERMISSION 权限已被拒绝", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    @TargetApi(21)
    public void testGetImeiForSlot() {
        try {
            TelephonyManager telecomManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            Class<?> TelephonyManagerClass = telecomManager.getClass();
            Method method = TelephonyManagerClass.getMethod("getImei");
            String imei = (String) method.invoke(telecomManager, new Object[]{});
            Log.i(TAG, "imei " + imei);
            Method method1 = TelephonyManagerClass.getMethod("getDeviceId");
            String deviceId = (String) method1.invoke(telecomManager, new Object[]{});
            String deviceId1 = (String) telecomManager.getDeviceId();
            Log.i(TAG, "deviceId " + deviceId + " deviceId1 " + deviceId1);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void testxunidashi() {
        int uid = android.os.Process.myUid();
        Log.i(TAG, "uid " + uid);
    }

    public void testProfile() {
        CrossProfileApps crossProfileApps = (CrossProfileApps) getSystemService(Context.CROSS_PROFILE_APPS_SERVICE);
        List<UserHandle> userHandles = crossProfileApps.getTargetUserProfiles();
        crossProfileApps.startMainActivity(new ComponentName(this, Main2Activity.class), userHandles.get(0));
    }

    @TargetApi(21)
    private MediaBrowser.ConnectionCallback mConnectionCallback =
            new MediaBrowser.ConnectionCallback() {
                @Override
                public void onConnected() {
                    Log.d(TAG, "onConnected: session token " + mMediaBrowser.getSessionToken());
//                    mMediaBrowser.subscribe(mParentItem.getMediaId(), mSubscriptionCallback);
//                    if (mMediaBrowser.getSessionToken() == null) {
//                        throw new IllegalArgumentException("No Session token");
//                    }
//                    MediaController mediaController = new MediaController(
//                            AlbumBrowserActivity.this, mMediaBrowser.getSessionToken());
//                    mediaController.registerCallback(mMediaControllerCallback);
//                    AlbumBrowserActivity.this.setMediaController(mediaController);
//                    if (mediaController.getMetadata() != null) {
//                        MusicUtils.updateNowPlaying(AlbumBrowserActivity.this);
//                    }
                }

                @Override
                public void onConnectionFailed() {
                    Log.d(TAG, "onConnectionFailed");
                }

                @Override
                public void onConnectionSuspended() {
                    Log.d(TAG, "onConnectionSuspended");
//                    AlbumBrowserActivity.this.setMediaController(null);
                }
            };

    @TargetApi(21)
    public void testMedia() {
        MediaSessionManager mediaSessionManager = (MediaSessionManager) this.getSystemService(Context.MEDIA_SESSION_SERVICE);

        mMediaBrowser = new MediaBrowser(this, new ComponentName(this, ""), mConnectionCallback, null);

        MediaSession mediaSession = new MediaSession(this, "xiabo-media");
        MediaController mediaController = new MediaController(this, mediaSession.getSessionToken());
        mediaController.adjustVolume(AudioManager.ADJUST_RAISE, 0x8);
    }

    public void onClick(View view) {
        // ClassLoader classLoader = getClassLoader();
        // Log.i("xiabo", "classLoader " + classLoader);
        broadcastDynamicIntent();
    }

    public void broadcastDynamicIntent() {
        Intent intent = new Intent();
        intent.setAction("com.tutorialspoint.DYMINIC_INTENT");
        intent.setPackage(getPackageName());
        // intent.setComponent(new ComponentName(this, MyCustomReceiver.class));  // 这样会发送到MyCustomReceiver去
        sendBroadcast(intent);
    }

    public void onClick2_SendStatic(View view) {
        Intent intent = new Intent();
        intent.setAction("com.tutorialspoint.CUSTOM_INTENT");
        intent.setPackage(getPackageName());
        // intent.setComponent(new ComponentName(this, MyCustomReceiver.class));
        sendBroadcast(intent);
    }

    public void onClick2_SendStaticAndDynamic(View view) {
        Intent intent = new Intent();
        intent.setAction("com.tutorialspoint.DYMINIC_AND_STATIC_INTENT");
        // intent.setPackage(getPackageName());
        // intent.setComponent(new ComponentName(this, MyCustomReceiver.class));
        sendBroadcast(intent);
    }

    public void onClick2_StaticAndDynamicSetPackage(View view) {
        Intent intent = new Intent();
        intent.setAction("com.tutorialspoint.DYMINIC_AND_STATIC_INTENT");
        intent.setPackage("com.example.myapplication2");
        // intent.setComponent(new ComponentName(this, MyCustomReceiver.class));
        sendBroadcast(intent);
    }

    public void onClick2_StaticAndDynamicMyPackage(View view) {
        Intent intent = new Intent();
        intent.setAction("com.tutorialspoint.DYMINIC_AND_STATIC_INTENT");
        intent.setPackage(getPackageName());
        // intent.setComponent(new ComponentName(this, MyCustomReceiver.class));
        sendBroadcast(intent);
    }

    public void testActivityRecognitionProvider() {
        try {
            Class<?> ActivityRecognitionProvider = Class.forName("com.android.location.provider.ActivityRecognitionProvider");
            Method method = ActivityRecognitionProvider.getMethod("getSupportedActivities");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    void getDownloads() {
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_PAUSED | DownloadManager.STATUS_PENDING);
//        query.setFilterById(DownloadManager.STATUS_FAILED|DownloadManager.STATUS_PENDING|DownloadManager.STATUS_RUNNING);
        Cursor cursor = downloadManager.query(query);
        Log.i("xiabo", "cursor " + cursor + " count " + cursor.getCount());
        if (cursor != null) {
            while(cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
                long bytes = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                long total = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                long status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));

                long destinationType = cursor.getLong(cursor.getColumnIndex("destination"));
                if (destinationType == 4 ||
                        destinationType == 0 ||
                        destinationType == 6) {
                    String localPath = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                    Log.i("xiabo", "localPath " + localPath);
                }
                // return content URI for cache download
                long downloadId = cursor.getLong(cursor.getColumnIndex("_id"));
                String cache  = ContentUris.withAppendedId(Uri.parse("content://downloads/all_downloads"), downloadId).toString();
                Log.i("xiabo", "localPath cache" + cache);

                Log.i("xiabo", "cursor id " + id + " bytes " + bytes + " total " + total + " status " + status + " uri " + uri.toString());
            }
        }

        this.getContentResolver().registerContentObserver(
                Uri.parse("content://downloads/my_downloads"),
                true,
                new DownloadChangeObserver(null)
        );
    }

    void testDownloadProvider() {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    Log.v("xiabo", "action = " + action);
                    long v8 = intent.getLongExtra("extra_download_id", -1);
                    DownloadManager v0 = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                    DownloadManager.Query v1 = new DownloadManager.Query();
                    long[] v3 = new long[1];
                    int v4 = 0;
                    v3[0] = v8;
                    Cursor v0_1 = v0.query(v1.setFilterById(v3));
                    int v3_1 = -1;
                    String v5 = null;
                    if(v0_1.moveToFirst()) {
                        if(v0_1.getInt(v0_1.getColumnIndex("status")) == 8) {
                            v4 = 1;
                        }

                        if(v4 == 0) {
                            v3_1 = v0_1.getInt(v0_1.getColumnIndex("reason"));
                        }

                        v5 = v0_1.getString(v0_1.getColumnIndex("local_uri"));
                    }
                    Log.v("xiabo", "extra_download_id " + v8 + " ");
                    v0_1.close();

                    if(v4 != 0) {
                        Log.v("xiabo", "success extra_download_id " + v8 + " local_uri " + v5);
                    }
                    else {
                        Log.v("xiabo", "fail extra_download_id " + v8 + " reason " + v3_1);
                    }
                }
            }
        };
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        DownloadManager downloadManager= (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        //文件下载地址
        String url="https://img-blog.csdn.net/20140913204802735?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveGlhYm9kYW4=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center";
        //创建一个Request对象
        DownloadManager.Request request= new DownloadManager.Request(Uri.parse(url));
        //设置下载文件路径
        request.setDestinationInExternalPublicDir("itbox","xiabo.png");
        //开始下载
        long downloadId = downloadManager.enqueue(request);
    }

    class DownloadChangeObserver extends ContentObserver {
        private Handler handler;
        private long downloadId;
        public DownloadChangeObserver(Handler handler) {
            super(handler);
            this.handler = handler;
        }
        @Override
        public void onChange(boolean selfChange) {
            Log.i("xiabo", "onChange " + selfChange);
        }
    }

    public void startActivity2() {
        Intent intent = new Intent(this, Main2Activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @TargetApi(21)
    public void testgetAppStandbyBucket() {
        UsageStatsManager usageStatsManager = (UsageStatsManager)getSystemService(Context.USAGE_STATS_SERVICE);
        usageStatsManager.getAppStandbyBucket();
    }

    public void testPermissionGET_ACCOUNTS() {
        int permission = checkPermission(Manifest.permission.GET_ACCOUNTS, android.os.Process.myPid(), android.os.Process.myUid());
        Log.i("xiabo", "permission " + permission);
        AccountManager accountManager = (AccountManager) this.getSystemService(Context.ACCOUNT_SERVICE);
        Account[] accounts = accountManager.getAccounts();
        for (Account account : accounts) {
            Log.i("xiabo", "account " + account.toString());
        }
    }
}
