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
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;


public class SoundManagerTask extends AsyncTask<Void, Void, Integer> {
    private static final String TAG = "SoundManagerTask";

    public static final int LANGUAGE_JP = 0;
    public static final int LANGUAGE_EN = 1;

    private final Context context;
    private Integer soundResouse;

    public SoundManagerTask(Context context, Integer inSoundResouse) {
        this.context = context;
        soundResouse = inSoundResouse;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Integer doInBackground(Void... params) {
        audioPlay(soundResouse);

        return 0;
    }

    @Override
    protected void onPostExecute(Integer result) {

    }

    private MediaPlayer mediaPlayer;

    private void audioPlay(Integer inputResouse) {

        //オーディオの設定
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)

                .build();

        Log.d(TAG, "SoundManagerTask: MediaPlayer.create()");
        mediaPlayer = MediaPlayer.create(context, inputResouse, attributes, 1);

        //再生開始
        Log.d(TAG, "SoundManagerTask: start of audio");
        mediaPlayer.start();

        // 終了を検知するリスナー
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d(TAG, "SoundManagerTask: end of audio");

                mp.release();

            }
        });
    }

}
