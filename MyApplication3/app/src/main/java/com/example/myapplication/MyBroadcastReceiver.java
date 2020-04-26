package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import static android.app.slice.Slice.EXTRA_TOGGLE_STATE;

// [START broadcast_receiver_example]
public class MyBroadcastReceiver extends BroadcastReceiver {

    public static int sReceivedCount = 0;
    public static String EXTRA_MESSAGE = "message";

    private static Uri sliceUri = Uri.parse("content://com.example.android.slice/count");

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(EXTRA_TOGGLE_STATE)) {
            Toast.makeText(context, "Toggled:  " + intent.getBooleanExtra(
                    EXTRA_TOGGLE_STATE, false),
                    Toast.LENGTH_LONG).show();
            sReceivedCount++;
            context.getContentResolver().notifyChange(sliceUri, null);
        }
    }
}
// [END broadcast_receiver_example]

