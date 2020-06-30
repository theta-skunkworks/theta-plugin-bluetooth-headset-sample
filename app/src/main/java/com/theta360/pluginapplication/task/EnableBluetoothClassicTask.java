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

import android.content.Context;
import android.os.AsyncTask;
import com.theta360.pluginapplication.network.HttpConnector;


public class EnableBluetoothClassicTask extends AsyncTask<Void, Void, String> {
    private Callback mCallback;
    private Context mContext;

    public EnableBluetoothClassicTask(Context context, Callback callback) {
        this.mCallback = callback;
        mContext = context;
    }

    @Override
    synchronized protected String doInBackground(Void... params) {

        String errorMessage;
        HttpConnector camera = new HttpConnector("127.0.0.1:8080");
        String bluetoothRole = camera.getOption("_bluetoothRole");
        if ((bluetoothRole.equals("Central")) || (bluetoothRole.equals("Central_Peripheral"))) {
            errorMessage = camera.setOption("_bluetoothRole", "Peripheral");
            if (errorMessage != null) { // パラメータの設定に失敗した場合はエラーメッセージを表示
                return "NG";
            }
        }

        errorMessage = camera
                .setOption("_bluetoothClassicEnable", Boolean.toString(Boolean.TRUE));
        if (errorMessage != null) { // パラメータの設定に失敗した場合はエラーメッセージを表示
            return "NG";
        }
        String bluetoothPower = camera.getOption("_bluetoothPower");
        if (bluetoothPower.equals("OFF")) {
            errorMessage = camera.setOption("_bluetoothPower", "ON");
            if (errorMessage != null) { // パラメータの設定に失敗した場合はエラーメッセージを表示
                return "NG";
            }
        }
        errorMessage = camera
                .getOption("_bluetoothClassicEnable");
        return "OK";
    }

    @Override
    protected void onPostExecute(String result) {
        mCallback.onEnableBluetoothClassic(result);
    }

    public interface Callback {
        void onEnableBluetoothClassic(String result);
    }
}

