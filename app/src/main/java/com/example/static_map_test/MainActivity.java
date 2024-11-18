package com.example.static_map_test;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.hardware.SensorListener;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private MapView mapView;
    private static final float SHAKE_THRESHOLD = 30.0f;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isPopupVisible = false;
    private GeoPoint currentLocation = new GeoPoint(10.8700, 106.8038); //Default Location
    private AlertDialog currentPopup;
    private Handler handler;
    private Runnable dismissPopupRunnable;
    private View popupView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cấu hình OSMDroid
        Configuration.getInstance().setUserAgentValue(getPackageName());

        // Copy MBTiles từ assets sang thư mục ứng dụng
        String mbTilesPath = copyMbTilesToInternalStorage();

        // Tạo MapView
        mapView = new MapView(this);
        setContentView(mapView);

        // Cài đặt bản đồ với tệp MBTiles
        File mbTilesFile = new File(mbTilesPath);
        if (mbTilesFile.exists()) {
            try {
                ITileSource tileSource = TileSourceFactory.DEFAULT_TILE_SOURCE; // Hoặc tùy chỉnh nguồn
                SimpleRegisterReceiver registerReceiver = new SimpleRegisterReceiver(this);

                MapTileFileArchiveProvider fileArchiveProvider = new MapTileFileArchiveProvider(
                        registerReceiver,
                        tileSource,
                        new IArchiveFile[]{(IArchiveFile)mbTilesFile}
                );

                MapTileProviderArray tileProviderArray = new MapTileProviderArray(tileSource, registerReceiver,
                        new MapTileFileArchiveProvider[]{fileArchiveProvider});

                mapView.setTileProvider(tileProviderArray);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Initialize MapView
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15); // Mức zoom
        mapView.getController().setCenter(new GeoPoint(10.8700, 106.8038)); // VNU - HCMC area

        //Initialize sensor
        initializeSensors();

        //Initialize handler
        handler = new Handler();
    }

    private String copyMbTilesToInternalStorage() {
        File destinationFile = new File(getFilesDir(), "map.mbtiles");
        if (!destinationFile.exists()) {
            try (InputStream inputStream = getAssets().open("map.mbtiles");
                 FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return destinationFile.getAbsolutePath();
    }

    private void initializeSensors(){
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager != null){
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener((SensorEventListener) this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void onSensorChanged(SensorEvent event){
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double acceleration = Math.sqrt(x*x + y*y + z*z);
            if(acceleration > SHAKE_THRESHOLD && !isPopupVisible){
                showPotholeConfirmDialog();
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Không cần xử lý trong trường hợp này
    }

    private void showPotholePopup() {
        isPopupVisible = true;

        // Inflate the custom layout
        View popupView = getLayoutInflater().inflate(R.layout.popup_pothole, null);

        // Initialize the PopupWindow
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        // Set up the popup background and elevation (optional)
        popupWindow.setElevation(8);
        popupWindow.setBackgroundDrawable(getDrawable(R.drawable.popup_background));

        // Show the popup at the center of the screen
        popupWindow.showAtLocation(mapView, Gravity.CENTER, 0, 0);

        // Handle "Confirm" button click
        MaterialButton confirmButton = popupView.findViewById(R.id.btn_confirm);
        confirmButton.setOnClickListener(v -> {
            addPotholeMarker(currentLocation);
            popupWindow.dismiss();
            isPopupVisible = false;

            // Remove the dismissal runnable
            handler.removeCallbacks(dismissPopupRunnable);
        });

        // Schedule popup dismissal after 10 seconds
        dismissPopupRunnable = () -> {
            if (popupWindow.isShowing()) {
                popupWindow.dismiss();
                isPopupVisible = false;
            }
        };
        handler.postDelayed(dismissPopupRunnable, 10000); // 10 seconds
    }

    private void showPotholeConfirmDialog(){
        isPopupVisible = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pothole Detected");
        builder.setMessage("Do you want to mark this location as a pothole?");
        builder.setCancelable(false);

        // Thêm nút Confirm
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            addPotholeMarker(currentLocation);
            dialog.dismiss();
            isPopupVisible = false;
        });

        // Tạo dialog
        currentPopup = builder.create();
        currentPopup.show();

        // Đếm ngược và tự động đóng popup sau 10 giây
        dismissPopupRunnable = () -> {
            if (currentPopup != null && currentPopup.isShowing()) {
                currentPopup.dismiss();
                isPopupVisible = false;
            }
        };
        handler.postDelayed(dismissPopupRunnable, 10000); // 10 giây
    }

    private void addPotholeMarker(GeoPoint location) {
        Marker marker = new Marker(mapView);
        marker.setPosition(location);
        marker.setTitle("Pothole");
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker);
        mapView.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDetach();
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener((SensorListener) this);
        }
        if (handler != null && dismissPopupRunnable != null) {
            handler.removeCallbacks(dismissPopupRunnable);
        }
    }
}