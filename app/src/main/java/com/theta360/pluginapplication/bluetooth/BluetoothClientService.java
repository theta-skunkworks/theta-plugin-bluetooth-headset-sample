package com.theta360.pluginapplication.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import com.theta360.pluginapplication.model.Constants;
import com.theta360.pluginlibrary.values.LedColor;
import com.theta360.pluginlibrary.values.LedTarget;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothClientService extends Service {
    private static final String TAG = "BluetoothClientService";
    private static final int COD_AUDIO_VIDEO_WEARABLE_HEADSET_DEVICE = 0x404;
    private static final int COD_AUDIO_VIDEO_HEADPHONES = 0x418;

    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDeviceReceiver mBluetoothDeviceReceiver;
    private BluetoothDevice mBluetoothDevice = null;
    private Context mContext;
    private BluetoothProfile mProfile = null;
    private BluetoothSocket mBluetoothSocket = null;
    private BluetoothProfile.ServiceListener mServiceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
            try {
                if (i == BluetoothProfile.HEADSET) {
                    Class bluetoothHeadset = Class
                            .forName("android.bluetooth.BluetoothHeadset");
                    Object object = bluetoothHeadset.cast(bluetoothProfile);
                    Method getConnectionState = bluetoothHeadset
                            .getDeclaredMethod("getConnectionState", BluetoothDevice.class);
                    int connectionState = (int) getConnectionState
                            .invoke(object, mBluetoothDevice);
                    if (connectionState == BluetoothProfile.STATE_DISCONNECTED) {
                        Method connect = bluetoothHeadset
                                .getDeclaredMethod("connect", BluetoothDevice.class);
                        boolean isConnected = (boolean) connect
                                .invoke(object, mBluetoothDevice);
                        Log.d(TAG, "isConnected : " + isConnected);
                        if (isConnected) {
                            mProfile = bluetoothProfile;
                        }
                    }
                }
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                    InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(int i) {
            Log.d(TAG, "BluetoothProfile.ServiceListener::onServiceDisconnected");
            mProfile = null;
        }
    };
    private BluetoothDeviceReceiver.Callback mBluetoothDeviceReceiverCallback = new BluetoothDeviceReceiver.Callback() {

        @Override
        public void onDiscoveryStarted() {
            if (Build.MODEL.equals("RICOH THETA Z1")) {
                // OLED text-bottom表示
                Intent intent = new Intent(Constants.ACTION_OLED_TEXT_SHOW);
                intent.putExtra(Constants.TEXT_BOTTOM, "discovery started");
                sendBroadcast(intent);
            } else {
                // LED3点灯
                Intent intentLedShow = new Intent("com.theta360.plugin.ACTION_LED_SHOW");
                intentLedShow.putExtra("color", LedColor.YELLOW.toString());
                intentLedShow.putExtra("target", LedTarget.LED3.toString());
                mContext.sendBroadcast(intentLedShow);
            }
        }

        @Override
        public void onDiscoveryFinished() {
            if (Build.MODEL.equals("RICOH THETA Z1")) {
                // OLED text-bottom表示
                Intent intent = new Intent(Constants.ACTION_OLED_TEXT_SHOW);
                intent.putExtra(Constants.TEXT_BOTTOM, "discovery finished");
                sendBroadcast(intent);
            } else {
                // LED3点灯
                Intent intentLedShow = new Intent("com.theta360.plugin.ACTION_LED_SHOW");
                intentLedShow.putExtra("color", LedColor.BLUE.toString());
                intentLedShow.putExtra("target", LedTarget.LED3.toString());
                mContext.sendBroadcast(intentLedShow);
            }
        }

        @Override
        public void onFound(BluetoothDevice bluetoothDevice,
                BluetoothClass bluetoothClass,
                int rssi) {
            String name = bluetoothDevice.getName();
            Log.d(TAG, "name" + name);
            if (name != null) {
                int type = bluetoothDevice.getType();
                Log.d(TAG, "type" + String.valueOf(type));
                if (type == bluetoothDevice.DEVICE_TYPE_CLASSIC || type == bluetoothDevice.DEVICE_TYPE_DUAL) {
                    int classNo = bluetoothClass.getDeviceClass();
                    Log.d(TAG, "class" + classNo);
                    if ((classNo == COD_AUDIO_VIDEO_HEADPHONES) || (classNo
                            == COD_AUDIO_VIDEO_WEARABLE_HEADSET_DEVICE)) {
                        stopClassicScan();
                        bluetoothDevice.createBond();
                    }
                }
            }
        }

        @Override
        public void onAclConnected(BluetoothDevice bluetoothDevice) {
            Log.d(TAG, "onAclConnected");
        }

        @Override
        public void onAclDisconnected(BluetoothDevice bluetoothDevice) {
            Log.d(TAG, "onAclDisconnected");
        }

        public void onBondStateChanged(BluetoothDevice bluetoothDevice, int bondState) {
            Log.d(TAG, "onBondStateChanged");
            if (bondState == BluetoothDevice.BOND_BONDED) {
                connect(bluetoothDevice);
            }
        }

        public void onConnectionStateChanged(int connectionState) {
            Log.d(TAG, "onConnectionStateChanged");
            if (Build.MODEL.equals("RICOH THETA Z1")) {
                Intent intent = new Intent(Constants.ACTION_OLED_TEXT_SHOW);
                if (connectionState == BluetoothAdapter.STATE_DISCONNECTED) {
                    intent.putExtra(Constants.TEXT_BOTTOM, "disconnected");
                } else if (connectionState == BluetoothAdapter.STATE_CONNECTED) {
                    intent.putExtra(Constants.TEXT_BOTTOM, "connected");
                }
                sendBroadcast(intent);
            } else {
                //LED3点灯
                Intent intentLedShow = new Intent("com.theta360.plugin.ACTION_LED_SHOW");
                if (connectionState == BluetoothAdapter.STATE_DISCONNECTED) {
                    intentLedShow.putExtra("color", LedColor.BLUE.toString());
                } else if (connectionState == BluetoothAdapter.STATE_CONNECTED) {
                    intentLedShow.putExtra("color", LedColor.WHITE.toString());
                }
                intentLedShow.putExtra("target", LedTarget.LED3.toString());
                mContext.sendBroadcast(intentLedShow);
            }
        }

    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = getApplicationContext();

        BluetoothManager bluetoothManager = mContext.getSystemService(BluetoothManager.class);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);

        disconnect();
        clearBondedDevice();

        mBluetoothDeviceReceiver = new BluetoothDeviceReceiver(
                mBluetoothDeviceReceiverCallback);
        mContext.registerReceiver(mBluetoothDeviceReceiver, intentFilter);

        scanStart();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mContext.unregisterReceiver(mBluetoothDeviceReceiver);
        stopClassicScan();
        disconnect();
        clearBondedDevice();
    }

    private void scanStart() {
        startClassicScan();
    }

    private void startClassicScan() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        boolean isStartDiscovery = mBluetoothAdapter.startDiscovery();
        Log.d(TAG, "startClassicScan :" + isStartDiscovery);
    }

    private void stopClassicScan() {

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        Log.d(TAG, "stopClassicScan");
    }

    private void connect(BluetoothDevice device) {
        int state = mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        if (state == BluetoothProfile.STATE_DISCONNECTED) {
            int type = device.getType();
            if (type == BluetoothDevice.DEVICE_TYPE_CLASSIC || type == BluetoothDevice.DEVICE_TYPE_DUAL) {
                int classNo = device.getBluetoothClass().getDeviceClass();
                Log.d(TAG, "class" + classNo);
                boolean isGetProfile = false;
                if ((classNo == COD_AUDIO_VIDEO_HEADPHONES) || (classNo
                        == COD_AUDIO_VIDEO_WEARABLE_HEADSET_DEVICE)) {
                    {
                        isGetProfile = mBluetoothAdapter
                                .getProfileProxy(mContext, mServiceListener,
                                        BluetoothProfile.HEADSET);
                    }
                }

                Log.d(TAG, "isGetProfile :" + isGetProfile);
                if (isGetProfile) {
                    mBluetoothDevice = device;
                }
            }
        }
    }

    private void disconnect() {
        if (mProfile != null) {
            List<BluetoothDevice> connectedDevices = mProfile.getConnectedDevices();
            for (final BluetoothDevice connectedDevice : connectedDevices) {

                try {
                    Class bluetoothHeadset = Class
                            .forName("android.bluetooth.BluetoothHeadset");
                    Object object = bluetoothHeadset.cast(mProfile);
                    Method disconnect = bluetoothHeadset
                            .getDeclaredMethod(
                                    "disconnect", BluetoothDevice.class);
                    boolean isDisconnected = (boolean) disconnect
                            .invoke(object, connectedDevice);
                    Log.d(TAG, "isDisconnected :" + isDisconnected);
                } catch
                (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                                | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            mBluetoothAdapter
                    .closeProfileProxy(BluetoothProfile.HEADSET,
                            mProfile);
            mProfile = null;
        }
        if (mBluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    String name = device.getName();
                    if (name != null) {
                        Log.d(TAG, "Bonded device name:" + name);
                    }
                }
            }
        }
    }

    private void clearBondedDevice() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice pairedDevice : pairedDevices) {
                try {
                    Method m = pairedDevice.getClass()
                            .getMethod("removeBond", (Class[]) null);
                    m.invoke(pairedDevice, (Object[]) null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
