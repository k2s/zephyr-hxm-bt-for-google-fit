package com.xtmotion.zephyrhxmbtforgooglefit;

import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Device;
import com.google.android.gms.fitness.service.FitnessSensorService;
import com.google.android.gms.fitness.service.FitnessSensorServiceRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomSensorService extends FitnessSensorService {
    private static final String TAG = "XT_random";
    FitnessSensorServiceRequest mRequest;

    private Handler customHandler = new Handler();
    Random rand = new Random();

    @Override
    public void onCreate() {
        super.onCreate();
        // 1. Initialize your software sensor(s).
        // 2. Create DataSource representations of your software sensor(s).
        // 3. Initialize some data structure to keep track of a registration for each sensor.
        Log.d(TAG, "Service Started!");
    }

    @Override
    public List<DataSource> onFindDataSources(List<DataType> dataTypes) {
        // 1. Find which of your software sensors provide the data types requested.
        // 2. Return those as a list of DataSource objects.
        Log.d(TAG, "Google fit onFindDataSources");

        List<DataSource> sources = new ArrayList<DataSource>();

        try {
                DataSource src = new DataSource.Builder()
                        .setDataType(DataType.TYPE_HEART_RATE_BPM)
                        .setName("Random Heart rate sensor")
                        .setType(DataSource.TYPE_RAW)
//                        .setDevice(new Device("Zephyr", "HxM BT", mac, 0))
                        .build();
                sources.add(src);
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


        customHandler.postDelayed(updateTimerThread, 1000);

        return true;
    }

    @Override
    public boolean onUnregister(DataSource dataSource) {
        // 1. Configure this sensor to stop delivering data to the platform
        // 2. Discard the reference to the registration request object
        mRequest = null;
        customHandler.removeCallbacks(updateTimerThread);

        Log.d(TAG, "Google fit request forgotten");

        return true;
    }

    private Runnable updateTimerThread = new Runnable() {

        public void run() {

            if (mRequest != null) {
//                String val = String.format("%03d", SystemClock.uptimeMillis());
                List<DataPoint> dataPoints = new ArrayList<DataPoint>();
                DataPoint dp = DataPoint.create(mRequest.getDataSource());
                int randomNum = rand.nextInt((195 - 60) + 1) + 60;
                dp.setFloatValues((float) randomNum);
                dataPoints.add(dp);
                try {
                    mRequest.getDispatcher().publish(dataPoints);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            customHandler.postDelayed(this, 500);
        }

    };

}