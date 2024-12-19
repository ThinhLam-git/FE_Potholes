package com.example.authentication_uiux.ui.home.MapComponents;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class PotholeDetectionService extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float THRESHOLD = 15.0f;
    private long lastDetectionTime = 0;
    private static final long DETECTION_COOLDOWN = 5000;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float acceleration = (float) Math.sqrt(x * x + y * y + z * z);

            if (acceleration > THRESHOLD &&
                    (System.currentTimeMillis() - lastDetectionTime) > DETECTION_COOLDOWN) {
                lastDetectionTime = System.currentTimeMillis();
                notifyPotholeDetected();
            }
        }
    }

    private void notifyPotholeDetected() {
        Intent intent = new Intent("POTHOLE_DETECTED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }
}