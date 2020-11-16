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

package com.theta360.pluginapplication.task;

import android.os.AsyncTask;
import android.util.Log;

import com.theta360.pluginapplication.network.HttpConnector;


public class RestoreBluetoothSettingsTask extends AsyncTask<Void, Void, String> {
    private static final String TAG = "RestoreBluetoothSettingsTask";

    private String bluetoothRole;
    private String bluetoothPower;

    public RestoreBluetoothSettingsTask(String saveBluetoothRole, String saveBluetoothPower) {
        bluetoothRole = saveBluetoothRole;
        bluetoothPower = saveBluetoothPower;
        Log.d(TAG, "bluetoothRole=" +bluetoothRole + ", bluetoothPower=" + bluetoothPower);
    }

    @Override
    synchronized protected String doInBackground(Void... params) {
        String ret = "OK";
        String errorMessage;
        HttpConnector camera = new HttpConnector("127.0.0.1:8080");

        if ( !bluetoothRole.equals("") ) {
            errorMessage = camera.setOption("_bluetoothRole", bluetoothRole);
            if (errorMessage != null) {
                Log.d(TAG, "setOption _bluetoothRole error : " +errorMessage);
                ret="NG";
            }
        }

        if ( !bluetoothPower.equals("") ) {
            errorMessage = camera.setOption("_bluetoothPower", bluetoothPower);
            if (errorMessage != null) { // パラメータの設定に失敗した場合はエラーメッセージを表示
                Log.d(TAG, "setOption _bluetoothPower error : " +errorMessage);
                ret="NG";
            }
        }

        return ret;
    }

    @Override
    protected void onPostExecute(String result) {
        //
    }

}

