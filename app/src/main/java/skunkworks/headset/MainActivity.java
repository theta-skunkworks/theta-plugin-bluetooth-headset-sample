/**
 * Copyright 2018 Ricoh Company, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

//package com.theta360.pluginapplication;
package skunkworks.headset;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import com.theta360.pluginapplication.bluetooth.BluetoothClientService;
import com.theta360.pluginapplication.bluetooth.MediaReceiver;
import com.theta360.pluginapplication.task.ChangeCaptureModeTask;
import com.theta360.pluginapplication.task.ChangeEvTask;
import com.theta360.pluginapplication.task.ChangeVolumeTask;
import com.theta360.pluginapplication.task.GetCurShutterVolumeTask;
import com.theta360.pluginapplication.task.RestoreBluetoothSettingsTask;
import com.theta360.pluginapplication.task.SoundManagerTask;
import com.theta360.pluginapplication.task.EnableBluetoothClassicTask;
import com.theta360.pluginapplication.task.ShutterButtonTask;
import com.theta360.pluginlibrary.activity.PluginActivity;
import com.theta360.pluginlibrary.callback.KeyCallback;
import com.theta360.pluginlibrary.receiver.KeyReceiver;
import java.util.ArrayList;


public class MainActivity extends PluginActivity {
    private static final String TAG = "MainActivity";

    private static final String ACTION_OLED_DISPLAY_SET = "com.theta360.plugin.ACTION_OLED_DISPLAY_SET";
    private MediaReceiver mMediaReceiver = null;
    //プラグイン起動時のMode長押し後 KeyUpをスルーする用
    private boolean onKeyDownModeButton = false;
    //長押し後のボタン離し認識用
    private boolean onKeyLongPressWlan = false;
    //発話言語保持用
    private int languageIndex = SoundManagerTask.LANGUAGE_EN;
    private final Integer[] soundListChgLang = {
            R.raw.speak_in_jp,
            R.raw.speak_in_en
    };
    //プラグイン起動前 Bluetooth設定保持用
    private String originalBluetoothRole = "";
    private String originalBluetoothPower = "";
    //プラグイン起動前 Shutter Volume保持用
    private String originalShutterVolume = "";
    //前回起動時の Shutter Volume保持用
    private String lastShutterVolume ="";
    //初回起動時のShutter Volume（イヤホンやスピーカーのため小さめな音から開始）
    private static final String initialShutterVolume = "26";

    private EnableBluetoothClassicTask.Callback mEnableBluetoothClassicTask = new EnableBluetoothClassicTask.Callback() {
        @Override
        public void onEnableBluetoothClassic(String result) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
            mMediaReceiver = new MediaReceiver(mMediaReceiverCallback);
            getApplicationContext().registerReceiver(mMediaReceiver, intentFilter);

            getApplicationContext()
                    .startService(
                            new Intent(getApplicationContext(), BluetoothClientService.class));
        }
        @Override
        public void saveBluetoothSettings(String bluetoothRole, String bluetoothPower) {
            Log.d(TAG, "saveBluetoothSettings: bluetoothRole=" + bluetoothRole + ", bluetoothPower=" + bluetoothPower);
            originalBluetoothRole = bluetoothRole;
            originalBluetoothPower = bluetoothPower;
        }

    };

    private ChangeVolumeTask.Callback mChangeVolumeTask = new ChangeVolumeTask.Callback() {
        @Override
        public void saveLastShutterVolume(String inLastShutterVolume) {
            lastShutterVolume = inLastShutterVolume;
            Log.d(TAG, "saveLastShutterVolume(): lastShutterVolume=" + lastShutterVolume);
        }
    };

    private GetCurShutterVolumeTask.Callback mRestoreShutterVolumeTask = new GetCurShutterVolumeTask.Callback() {
        @Override
        public void saveCurrentShutterVolume(String currentShutterVolume) {
            originalShutterVolume = currentShutterVolume;
            Log.d(TAG, "saveCurrentShutterVolume(): originalShutterVolume=" + originalShutterVolume);
        }
    };

    private MediaReceiver.Callback mMediaReceiverCallback = new MediaReceiver.Callback() {
        @Override
        public void onChangeVolume(int stream_type, int prev_vol, int vol) {
            if (stream_type == AudioManager.STREAM_MUSIC) {
                if (vol != prev_vol) {
                    new ChangeVolumeTask(getApplicationContext(), mChangeVolumeTask, vol,
                            ChangeVolumeTask.ACTION_TYPE_SET_VOL, languageIndex).execute();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //set OLED Display mode for THETA Z1
        if (Build.MODEL.equals("RICOH THETA Z1")) {
            Intent oledIntentSet = new Intent(ACTION_OLED_DISPLAY_SET);
            oledIntentSet.putExtra("display", "plugin");
            sendBroadcast(oledIntentSet);
        }

        new GetCurShutterVolumeTask(mRestoreShutterVolumeTask).execute();

        new EnableBluetoothClassicTask(getApplicationContext(), mEnableBluetoothClassicTask).execute();

        // Set enable to close by pluginlibrary, If you set false, please call close() after finishing your end processing.
        setAutoClose(true);
        // Set a callback when a button operation event is acquired.
        setKeyCallback(new KeyCallback() {
            @Override
            public void onKeyDown(int keyCode, KeyEvent event) {
                Log.d(TAG, "onKeyDown(): keyCode=" + keyCode + ", keyEvent=" + event);
                TextView viewKeyCode = (TextView) findViewById(R.id.viewKeyDown);
                String strKeyCode = String.valueOf(keyCode);
                viewKeyCode.setText(strKeyCode + "[" + event.keyCodeToString(keyCode) + "]");

                switch (keyCode) {
                    case KeyReceiver.KEYCODE_CAMERA :
                        //シャッターボタンはonKeyDownで反応させ長押しは実装しない。
                        execKeyProcess(keyCode2KeyProcess(keyCode));
                        break;
                    case KeyReceiver.KEYCODE_MEDIA_RECORD :
                        //プラグイン起動時のMode長押し後 onKeyUp() を無処理とするための仕掛け
                        onKeyDownModeButton = true;
                        break;
                    default:
                        //シャッターボタン以外の短押しはonKeyUpで判断して実行
                        break;
                }
            }

            @Override
            public void onKeyUp(int keyCode, KeyEvent event) {
                Log.d(TAG, "onKeyUp(): keyCode=" + keyCode + ", keyEvent=" + event);
                TextView viewKeyCode = (TextView) findViewById(R.id.viewKeyUp);
                String strKeyCode = String.valueOf(keyCode);
                viewKeyCode.setText(strKeyCode + "[" + event.keyCodeToString(keyCode) + "]");

                switch (keyCode) {
                    case KeyReceiver.KEYCODE_CAMERA :
                        //シャッターはonKeyDownで実行済み。onKeyUpでは無処理。
                        break;
                    case KeyReceiver.KEYCODE_WLAN_ON_OFF :
                        if (onKeyLongPressWlan) {
                            onKeyLongPressWlan=false;
                        } else {
                            execKeyProcess(keyCode2KeyProcess(keyCode));
                        }
                        break;
                    case KeyReceiver.KEYCODE_MEDIA_RECORD :
                        if (onKeyDownModeButton) {
                            execKeyProcess(keyCode2KeyProcess(keyCode));
                        }
                        onKeyDownModeButton = false;
                        break;
                    default:
                        //AVRCP経由のキーコード実行
                        execKeyProcess(keyCode2KeyProcess(keyCode));
                        break;
                }
            }

            @Override
            public void onKeyLongPress(int keyCode, KeyEvent event) {
                Log.d(TAG, "onKeyLongPress(): keyCode=" + keyCode + ", keyEvent=" + event);

                switch (keyCode) {
                    case KeyReceiver.KEYCODE_WLAN_ON_OFF:
                        onKeyLongPressWlan=true;
                        if ( languageIndex == SoundManagerTask.LANGUAGE_JP ) {
                            languageIndex = SoundManagerTask.LANGUAGE_EN;
                        } else {
                            languageIndex = SoundManagerTask.LANGUAGE_JP;
                        }
                        new SoundManagerTask(getApplicationContext(), soundListChgLang[languageIndex]).execute();

                        break;
                    default:
                        break;
                }
            }

        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        //前回起動時に保存した情報を読む
        restorePluginInfo();

        //前回起動時の音量にする（初回起動時を考慮し、ShutterVolumeとSTREAM_MUSICの両方にセットする）
        int inLastShutterVolume = Integer.parseInt(lastShutterVolume);
        new ChangeVolumeTask(getApplicationContext(), mChangeVolumeTask,
                inLastShutterVolume,
                ChangeVolumeTask.ACTION_TYPE_SUTTER_AND_MUSIC,
                ChangeVolumeTask.LANGUAGE_NO_SOUND).execute();


        int index = 0;
        while (true) {
            if (defaultKeyProcess[index][0] == -1) {
                break;
            } else {
                ArrayList<Integer> element = new ArrayList<Integer>();
                element.add(defaultKeyProcess[index][0]);
                element.add(defaultKeyProcess[index][1]);
                InitalList.add(element);
            }
            index++;
        }
        // LEDを正しく点灯させるため、わざと動画モードにしてから静止画モードにする。
        new ChangeCaptureModeTask("video").execute();
        new ChangeCaptureModeTask("image").execute();

        setKeyCode2KeyProcessList();
    }

    @Override
    protected void onPause() {
        // Do end processing
        //close();

        Log.d(TAG, "onPause(): lastShutterVolume=" + lastShutterVolume);
        Log.d(TAG, "onPause(): originalShutterVolume=" + originalShutterVolume);

        //Shutter Volumeをプラグイン起動前の状態に戻す
        //ShutterVolのみを元に戻す（STREAM_MUSICに設定すると、次回接続後に送れて反映されるので厄介）
        int inOrgShutterVolume = Integer.parseInt(originalShutterVolume);
        new ChangeVolumeTask(getApplicationContext(), mChangeVolumeTask,
                inOrgShutterVolume,
                ChangeVolumeTask.ACTION_TYPE_SUTTER_VOL,
                ChangeVolumeTask.LANGUAGE_NO_SOUND).execute();


        //Bluetooth関連の設定をプラグイン起動前の状態に戻す
        new RestoreBluetoothSettingsTask(originalBluetoothRole, originalBluetoothPower ).execute();

        //次回起動時のために必要な情報を保存
        savePluginInfo();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        getApplicationContext()
                .stopService(new Intent(getApplicationContext(), BluetoothClientService.class));
        if (mMediaReceiver != null) {
            getApplicationContext().unregisterReceiver(mMediaReceiver);
        }

        super.onDestroy();
    }

    //=====================================================
    //<<< KeyProcess >>>
    //=====================================================

    String processName[] = {
            "NOP",
            "EXEC_SHUTTER",
            "SET_EV_PLUS",
            "SET_EV_MINUS",
            "SET_VOL_PLUS",
            "SET_VOL_MINUS",
    };
    public static final int WITH_INPUT = -1;  //use key operation history
    public static final int NO_PROCESS = 0;
    public static final int EXEC_SHUTTER = 1;
    public static final int SET_EV_PLUS = 2;
    public static final int SET_EV_MINUS = 3;
    public static final int SET_VOL_PLUS = 4;
    public static final int SET_VOL_MINUS = 5;

    private void execKeyProcess(int processCode) {

        switch (processCode) {
            case EXEC_SHUTTER:
                new ShutterButtonTask().execute();
                break;
            case SET_EV_PLUS:
                new ChangeEvTask(getApplicationContext(), ChangeEvTask.EV_PLUS, languageIndex)
                        .execute();
                break;
            case SET_EV_MINUS:
                new ChangeEvTask(getApplicationContext(), ChangeEvTask.EV_MINUS, languageIndex)
                        .execute();
                break;
            case SET_VOL_PLUS:
                if ( !originalShutterVolume.equals("") ) {
                    new ChangeVolumeTask(getApplicationContext(), mChangeVolumeTask,0,
                            ChangeVolumeTask.ACTION_TYPE_UP_VOL, languageIndex).execute();
                }
                break;
            case SET_VOL_MINUS:
                if ( !originalShutterVolume.equals("") ) {
                    new ChangeVolumeTask(getApplicationContext(), mChangeVolumeTask,0,
                            ChangeVolumeTask.ACTION_TYPE_DOWN_VOL, languageIndex).execute();
                }
                break;
        }

        return;
    }

    //=====================================================
    //<<< Key Event >>>
    //=====================================================
    private static final int KEYCODE_LIST_MAX_NUM = 300;
    private ArrayList<Integer> keyCode2ProcessList = new ArrayList<Integer>();

    //Initial value for shipment
    private int defaultKeyProcess[][] = {
            //- THETA V body button -
            {KeyReceiver.KEYCODE_CAMERA, EXEC_SHUTTER},
            {KeyReceiver.KEYCODE_WLAN_ON_OFF, SET_VOL_PLUS},
            {KeyReceiver.KEYCODE_MEDIA_RECORD, SET_VOL_MINUS},

            //- Bluetooth Headset
            {KeyEvent.KEYCODE_MEDIA_PLAY, EXEC_SHUTTER},
            {KeyEvent.KEYCODE_MEDIA_PAUSE, EXEC_SHUTTER},
            {KeyEvent.KEYCODE_MEDIA_STOP, NO_PROCESS},
            {KeyEvent.KEYCODE_MEDIA_NEXT, SET_EV_PLUS},
            {KeyEvent.KEYCODE_MEDIA_PREVIOUS, SET_EV_MINUS},
            {-1, NO_PROCESS}
    };

    private ArrayList<ArrayList<Integer>> InitalList = new ArrayList<ArrayList<Integer>>();

    private void setKeyCode2KeyProcessList() {
        int result = 0;

        // Reset keyCode2ProcessList
        keyCode2ProcessList.clear();

        // Make keyCode2ProcessList
        for (int i = 0; i < KEYCODE_LIST_MAX_NUM; i++) {

            boolean flag = false;
            for (int j = 0; j < InitalList.size(); j++) {
                if (i == InitalList.get(j).get(0)) {
                    keyCode2ProcessList.add(InitalList.get(j).get(1));
                    InitalList.remove(j);
                    flag = true;
                    break;
                }
            }
            if (flag == false) {
                keyCode2ProcessList.add(NO_PROCESS);
            }
        }
        Log.d(TAG, "keyCode2ProcessList.size()=" + String.valueOf(keyCode2ProcessList.size()));

        return;
    }

    private int keyCode2KeyProcess(int inKeyCode) {
        int result = NO_PROCESS;

        try {
            Integer listDat = keyCode2ProcessList.get(inKeyCode);

            if (listDat == NO_PROCESS) {
                // update key history
                keyCode2ProcessList.set(inKeyCode, WITH_INPUT);
            } else if (listDat == WITH_INPUT) {
                result = NO_PROCESS;
            } else {
                result = listDat;
            }

        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG,
                    "keyCode2KeyProcess() : Undefined KeyCode=" + String.valueOf(inKeyCode) + ", "
                            + e.getMessage());
            result = NO_PROCESS;
        }

        Log.d(TAG, "keyCode2KeyProcess() : result=" + processName[result]);
        return result;
    }

    //==============================================================
    // 設定保存・復帰
    //==============================================================
    private static final String SAVE_KEY_LANG_INDEX  = "languageIndex";
    private static final String SAVE_KEY_LAST_SHUTTER_VOL  = "lastSutterVolume";
    SharedPreferences sharedPreferences;
    void restorePluginInfo() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        languageIndex = sharedPreferences.getInt(SAVE_KEY_LANG_INDEX, SoundManagerTask.LANGUAGE_EN);
        lastShutterVolume = sharedPreferences.getString(SAVE_KEY_LAST_SHUTTER_VOL, initialShutterVolume);
    }
    void savePluginInfo() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(SAVE_KEY_LANG_INDEX, languageIndex);
        editor.putString(SAVE_KEY_LAST_SHUTTER_VOL, lastShutterVolume);
        editor.commit();
    }
}
