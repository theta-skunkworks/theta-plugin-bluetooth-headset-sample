package com.theta360.pluginapplication.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.util.Log;

public class MediaReceiver extends BroadcastReceiver {
    private static final String TAG = "MediaReceiver";
    private Callback mCallback;
    private int mPrevReceiveVolumeStreameType;
    private int mPrevReceiveVolumeStreamValue;

    public MediaReceiver(@NonNull Callback callback) {
        mPrevReceiveVolumeStreameType = 0;
        mPrevReceiveVolumeStreamValue = 0;
        mCallback = callback;
    }

    @Override
    synchronized public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();
        Log.d(TAG, "action =" + action);

        if (action.equals("android.media.VOLUME_CHANGED_ACTION")) {
            Log.d(TAG, "android.media.VOLUME_CHANGED_ACTION");

            int stream_type = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", 0);
            int stream_vol = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", 0);
            int prev_stream_vol = intent
                    .getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", 0);
            if (stream_type == AudioManager.STREAM_MUSIC) {
                if ((mPrevReceiveVolumeStreameType != stream_type) && (mPrevReceiveVolumeStreamValue
                        == stream_vol)) {
                    Log.d(TAG, "EXTRA_VOLUME_STREAM_TYPE :" + stream_type);
                    Log.d(TAG, "EXTRA_VOLUME_STREAM_VALUE :" + stream_vol);
                    Log.d(TAG, "EXTRA_PREV_VOLUME_STREAM_VALUE :" + prev_stream_vol);

                    mCallback.onChangeVolume(stream_type, prev_stream_vol, stream_vol);
                }
            }
            mPrevReceiveVolumeStreameType = stream_type;
            mPrevReceiveVolumeStreamValue = stream_vol;
        }
    }

    public interface Callback {
        void onChangeVolume(int stream_type, int prev_vol, int vol);
    }
}



