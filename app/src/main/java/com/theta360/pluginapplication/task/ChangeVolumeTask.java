/**
 * Copyright 2018 Ricoh Company, Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.theta360.pluginapplication.task;

import android.content.Context;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.util.Log;
import com.theta360.pluginapplication.network.HttpConnector;

import skunkworks.headset.R;


public class ChangeVolumeTask extends AsyncTask<Void, Void, Integer> {
    public static final int ACTION_TYPE_SET_VOL = 0;
    public static final int ACTION_TYPE_UP_VOL = 1;
    public static final int ACTION_TYPE_DOWN_VOL = 2;
    public static final int ACTION_TYPE_SUTTER_VOL = 3;
    public static final int ACTION_TYPE_SUTTER_AND_MUSIC = 4;

    public static final int LANGUAGE_NO_SOUND = -1;

    private static final int VOL_CHANGED = 0;
    private static final int VOL_NOT_CHANGED = 1;

    private static final String TAG = "ChangeVolumeTask";
    private static final float MAX = 100.0f;

    private Callback mCallback;

    private final Context context;
    private int mMaxVolume;
    private int mChangingStreamVol;
    private AudioManager mAudioManager;
    private int mType;

    private int languageIndex ;
    private final Integer[] soundListChgVol = {
            R.raw.speech_volume_jp,
            R.raw.speech_volume_en
    };

    public ChangeVolumeTask(Context context, Callback callback, int val, int type, int inLangIndex) {
        this.context = context;
        this.mCallback = callback;

        Log.d(TAG, "val=" + String.valueOf(val) + ", type=" + String.valueOf(type) + ", inLangIndex=" + String.valueOf(inLangIndex) );

        mAudioManager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        mType = type;
        mChangingStreamVol = val;
        if ( SoundManagerTask.LANGUAGE_JP <= inLangIndex && inLangIndex <= SoundManagerTask.LANGUAGE_EN) {
            languageIndex = inLangIndex;
        } else {
            if( inLangIndex == LANGUAGE_NO_SOUND ) {
                languageIndex = LANGUAGE_NO_SOUND;
            } else {
                languageIndex = SoundManagerTask.LANGUAGE_EN;
            }
        }

    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Integer doInBackground(Void... params) {
        Integer ret = VOL_NOT_CHANGED;
        HttpConnector camera = new HttpConnector("127.0.0.1:8080");
        String currentShutterVolumeString = camera.getOption("_shutterVolume");
        Log.d(TAG, "camera.getOption : _shutterVolume :" + currentShutterVolumeString);
        int currnetShutterVolume = +Integer.parseInt(currentShutterVolumeString);

        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        double vol = (mMaxVolume * currnetShutterVolume) / MAX;
        int currentStreamVol = (int) Math.ceil(vol);
        int changingShutterVol;
        if (mType == ACTION_TYPE_SET_VOL) {

        } else if (mType == ACTION_TYPE_UP_VOL) {
            mChangingStreamVol = currentStreamVol + 1;
            if (mMaxVolume < mChangingStreamVol) {
                mChangingStreamVol = mMaxVolume;
            }

        } else if (mType == ACTION_TYPE_DOWN_VOL) {
            mChangingStreamVol = currentStreamVol - 1;
            if (mChangingStreamVol < 0) {
                mChangingStreamVol = 0;
            }
        } else if ( (mType == ACTION_TYPE_SUTTER_VOL) || (mType == ACTION_TYPE_SUTTER_AND_MUSIC) ) {
            int inSutterVol = mChangingStreamVol;
            if ( (int)MAX < inSutterVol ) { inSutterVol = (int)MAX; }
            if ( inSutterVol < 0 ) { inSutterVol = 0; }
            mChangingStreamVol = (int)((mMaxVolume*inSutterVol)/MAX + 0.5f);
        }

        Log.d(TAG, "calc shutter volume :mMaxVolume=" + String.valueOf(mMaxVolume));
        // calc shutter volume
        {
            vol = (MAX * mChangingStreamVol) / mMaxVolume;
            changingShutterVol = (int) Math.floor(vol);
            if (vol < 0) {
                changingShutterVol = changingShutterVol * (-1);
            }

            if (100 < changingShutterVol) {
                changingShutterVol = 100;
            }
            if (changingShutterVol < 0) {
                changingShutterVol = 0;
            }
        }
        // set shutter volume
        mCallback.saveLastShutterVolume( String.valueOf(changingShutterVol) );
        String strResult = camera
                .setOption("_shutterVolume", String.valueOf(changingShutterVol));
        Log.d(TAG, "camera.setOption : _shutterVolume : " + changingShutterVol);
        if (mChangingStreamVol != currentStreamVol) {
            ret = VOL_CHANGED;
        }

        if (changingShutterVol == currnetShutterVolume) {
            Log.d(TAG, "ret=" + String.valueOf(ret));
            Log.d(TAG, "mChangingStreamVol=" + String.valueOf(mChangingStreamVol) + ", currentStreamVol=" + String.valueOf(currentStreamVol));
            Log.d(TAG, "changingShutterVol == currnetShutterVolume :" + String.valueOf(changingShutterVol));
            languageIndex=LANGUAGE_NO_SOUND;
        }

        return ret;
    }

    @Override
    protected void onPostExecute(Integer result) {
        Log.d(TAG, "onPostExecute:result=" + String.valueOf(result));

        if (result == VOL_CHANGED) {
            // stream volumeの変更
            // mTypeがACTION_TYPE_SET_VOLの場合は、stream volumeは変更済みのため、設定しない
            // mTypeがACTION_TYPE_SUTTER_VOLの場合は 意図的に設定しない
            if ( (mType == ACTION_TYPE_UP_VOL) || (mType == ACTION_TYPE_DOWN_VOL) || (mType == ACTION_TYPE_SUTTER_AND_MUSIC) ) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mChangingStreamVol, 0);
            }

            if ( languageIndex != LANGUAGE_NO_SOUND ) {
                //サンプル音を鳴らす
                new SoundManagerTask(context, soundListChgVol[languageIndex]).execute();
            }

        }
    }

    public interface Callback {
        void saveLastShutterVolume(String lastShutterVolume);
    }

}
