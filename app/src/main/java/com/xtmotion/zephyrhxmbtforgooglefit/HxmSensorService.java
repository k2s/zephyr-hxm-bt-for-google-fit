package com.xtmotion.zephyrhxmbtforgooglefit;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Device;
import com.google.android.gms.fitness.service.FitnessSensorService;
import com.google.android.gms.fitness.service.FitnessSensorServiceRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import zephyr.android.HxMBT.BTClient;

public class HxmSensorService extends FitnessSensorService {
    private static final String TAG = "XT_hxmfit";
    FitnessSensorServiceRequest mRequest;

    BTClient mHxmBt;
    HxmConnectedListener mHxmListener;

    @Override
    public void onCreate() {
        super.onCreate();
        // 1. Initialize your software sensor(s).
        // 2. Create DataSource representations of your software sensor(s).
        // 3. Initialize some data structure to keep track of a registration for each sensor.

        // TODO listen to Bluetooth events

        Log.d(TAG, "Service Started!");
    }

    @Override
    public List<DataSource> onFindDataSources(List<DataType> dataTypes) {
        // 1. Find which of your software sensors provide the data types requested.
        // 2. Return those as a list of DataSource objects.
        Log.d(TAG, "Google Fit onFindDataSources");

        List<DataSource> sources = new ArrayList<DataSource>();

        try {
            String mac = this.getHxmMac();
            if (mac != null) {

                DataSource src = new DataSource.Builder()
                        .setDataType(DataType.TYPE_HEART_RATE_BPM)
                        .setName("Heart rate from Zephyr HxM BT")
                        .setType(DataSource.TYPE_RAW)
                        .setDevice(new Device("Zephyr", "HxM BT", mac, 0))
                        .build();
                sources.add(src);
            }

        } catch (Exception e) {
            Log.e(TAG, "onFindDataSources", e);
        }

        return sources;
    }

    @Override
    public boolean onRegister(FitnessSensorServiceRequest request) {
        // 1. Determine which sensor to register with request.getDataSource().
        // 2. If a registration for this sensor already exists, replace it with this one.
        // 3. Keep (or update) a reference to the request object.
        // 4. Configure your sensor according to the request parameters.
        // 5. When the sensor has new data, deliver it to the platform by calling
        //    request.getDispatcher().publish(List<DataPoint> dataPoints)
        mRequest = request;
        Log.d(TAG, "Google fit request stored");

        String mac = request.getDataSource().getDevice().getUid();
        if (mac != null) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            mHxmBt = new BTClient(adapter, mac);
            mHxmListener = new HxmConnectedListener(mDataHandler);
            mHxmBt.addConnectedEventListener(mHxmListener);
            if (mHxmBt.IsConnected()) {
                mHxmBt.run();
                return true;
            } else {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean onUnregister(DataSource dataSource) {
        // 1. Configure this sensor to stop delivering data to the platform
        // 2. Discard the reference to the registration request object
        mRequest = null;
        // TODO delay HXR disconnect ?

        if (mHxmBt !=null) {
            mHxmBt.removeConnectedEventListener(mHxmListener);
            mHxmBt.Close();
            mHxmBt = null;
        }
        Log.d(TAG, "Google Fit request forgotten");

        return true;
    }

    private synchronized String getHxmMac() {
        if (mHxmBt != null) {
            return mHxmBt.getDevice().getAddress();
        }
        String macId = null;
        BluetoothAdapter adapter = null;

        // find paired HXM device
        adapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().startsWith("HXM")) {
                    macId = device.getAddress();
                    break;

                }
            }
        }

        return macId;
    }

    final Handler mDataHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HxmConnectedListener.HEART_RATE:
                    if (mRequest != null) {
                        List<DataPoint> dataPoints = new ArrayList<DataPoint>();
                        DataPoint dp = DataPoint.create(mRequest.getDataSource());
                        float v = msg.getData().getFloat("HeartRate");
                        dp.setFloatValues(v);
                        dataPoints.add(dp);
                        try {
                            mRequest.getDispatcher().publish(dataPoints);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }

    };


}
