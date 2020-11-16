/**
 * Copyright 2018 Ricoh Company, Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.theta360.pluginapplication.task;

import android.os.AsyncTask;
import com.theta360.pluginapplication.network.HttpConnector;


public class GetCurShutterVolumeTask extends AsyncTask<Void, Void, String> {
    private Callback mCallback;

    public GetCurShutterVolumeTask( Callback callback) {
        this.mCallback = callback;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    synchronized protected String doInBackground(Void... params) {
        HttpConnector camera = new HttpConnector("127.0.0.1:8080");
        String currentShutterVolumeString = camera.getOption("_shutterVolume");

        return currentShutterVolumeString;
    }

    @Override
    protected void onPostExecute(String result) {
        mCallback.saveCurrentShutterVolume(result);
    }

    public interface Callback {
        void saveCurrentShutterVolume(String currentShutterVolume);
    }
}
