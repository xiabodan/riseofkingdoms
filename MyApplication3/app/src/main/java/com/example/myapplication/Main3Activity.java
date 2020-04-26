package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media.VolumeProviderCompat;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;


public class Main3Activity extends Activity {
    private final static String TAG = MainActivity.TAG;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        init();
    }

    void init() {
        /*
        MediaSessionManager mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
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
        */

        ComponentName v0_5 = new ComponentName(this.getPackageName(), MediaButtonIntentReceiver.class.getName());
        Intent v1 = new Intent("android.intent.action.MEDIA_BUTTON");
        v1.setComponent(v0_5);
        PendingIntent v1_1 = PendingIntent.getBroadcast(((Context)this), 0, v1, 0);
        MediaSessionCompat v2 = new MediaSessionCompat(((Context)this), "com.google.android.music", v0_5, v1_1);
        v2.setMediaButtonReceiver(v1_1);
        VolumeProviderCompat volumeProviderCompat = new VolumeProviderCompat(VolumeProviderCompat.VOLUME_CONTROL_FIXED, 100, 0){
            @Override
            public void onAdjustVolume(int direction) {
                Log.i(TAG, "onAdjustVolume " + direction);

                if (direction > 0) {
                    setCurrentVolume(getCurrentVolume() + 5);
                } else if (direction < 0) {
                    setCurrentVolume(getCurrentVolume() - 5);
                }
            }

            @Override
            public void onSetVolumeTo(int volume) {
                super.onSetVolumeTo(volume);
                Log.i(TAG, "onSetVolumeTo " + volume);
            }
        };

        volumeProviderCompat.setCallback(new VolumeProviderCompat.Callback() {
            @Override
            public void onVolumeChanged(VolumeProviderCompat volumeProvider) {
                int currentVolume = volumeProvider.getCurrentVolume();
                Log.i(TAG,"onVolumeChanged " + currentVolume);
            }
        });
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_REWIND | PlaybackState.ACTION_FAST_FORWARD);
        stateBuilder.setState(PlaybackState.STATE_PLAYING, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1.0f);
        v2.setPlaybackState(stateBuilder.build());
        v2.setActive(true);
        v2.setPlaybackToRemote(volumeProviderCompat);
        MediaController mediaController = new MediaController(this, (MediaSession.Token) v2.getSessionToken().getToken());
        setMediaController(mediaController);
        Log.i(MainActivity.TAG, "setMediaController " + mediaController);
    }
}
