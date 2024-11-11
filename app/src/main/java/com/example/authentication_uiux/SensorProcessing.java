package com.example.authentication_uiux;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SensorProcessing extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float lastX, lastY, lastZ;
    private boolean isInitialized = false;
    private static final float SHAKE_THRESHOLD = 30.0f;

    private TextView xAxisTextView;
    private TextView yAxisTextView;
    private TextView zAxisTextView;
    private TextView shakeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_processing);

        xAxisTextView = findViewById(R.id.xAxisTextView);
        yAxisTextView = findViewById(R.id.yAxisTextView);
        zAxisTextView = findViewById(R.id.zAxisTextView);
        shakeTextView = findViewById(R.id.shakeTextView);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Toast.makeText(this, "Không tìm thấy cảm biến gia tốc", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Hiển thị giá trị x, y, z thời gian thực
            xAxisTextView.setText("X: " + x);
            yAxisTextView.setText("Y: " + y);
            zAxisTextView.setText("Z: " + z);

            if (!isInitialized) {
                lastX = x;
                lastY = y;
                lastZ = z;
                isInitialized = true;
            } else {
                float deltaX = Math.abs(lastX - x);
                float deltaY = Math.abs(lastY - y);
                float deltaZ = Math.abs(lastZ - z);

                // Kiểm tra ngưỡng rung lắc
                if (deltaX > SHAKE_THRESHOLD || deltaY > SHAKE_THRESHOLD || deltaZ > SHAKE_THRESHOLD) {
                    shakeTextView.setText("Rung lắc phát hiện!");
                } else {
                    shakeTextView.setText("Không có rung lắc mạnh");
                }

                lastX = x;
                lastY = y;
                lastZ = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Không cần xử lý
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
}
