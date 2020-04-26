package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LocalIntentSender extends BroadcastReceiver {
    private static final String TAG = "StagedInstallTest";
    private static final BlockingQueue<Intent> sIntentSenderResults = new LinkedBlockingQueue<Intent>();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received intent " + prettyPrint(intent));
        sIntentSenderResults.add(intent);
    }

    /**
     * Get a LocalIntentSender.
     */
    static IntentSender getIntentSender(Context context) {
        Intent intent = new Intent(context, LocalIntentSender.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent, 0);
        return pending.getIntentSender();
    }

    /**
     * Returns the most recent Intent sent by a LocalIntentSender.
     */
    static Intent getIntentSenderResult() throws InterruptedException {
        Intent intent = sIntentSenderResults.take();
        Log.i(TAG, "Taking intent " + prettyPrint(intent));
        return intent;
    }

    private static String prettyPrint(Intent intent) {
        int sessionId = intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1);
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS,
                PackageInstaller.STATUS_FAILURE);
        String message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
        return String.format("%s: {\n"
                + "sessionId = %d\n"
                + "status = %d\n"
                + "message = %s\n"
                + "}", intent, sessionId, status, message);
    }
}