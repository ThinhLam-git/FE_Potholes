package com.example.authentication_uiux.ui.home.MapComponents;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.authentication_uiux.R;

import org.osmdroid.util.GeoPoint;

import java.util.List;

public class NavigationService extends Service implements LocationListener {
    private static final int NOTIFICATION_ID = 1;
    private LocationManager locationManager;
    private List<GeoPoint> routePoints;
    private int currentPointIndex = 0;
    private static final float MIN_DISTANCE = 10; // meters

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra("route")) {
            routePoints = intent.getParcelableArrayListExtra("route");
            startForegroundService();
            startLocationUpdates();
        }
        return START_STICKY;
    }

    private void startForegroundService() {
        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, "navigation_channel")
                .setContentTitle("Navigation Active")
                .setContentText("Turn-by-turn navigation is running")
                .setSmallIcon(R.drawable.ic_navigation)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "navigation_channel",
                    "Navigation Service",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000, // 1 second
                    1,    // 1 meter
                    this
            );
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (routePoints == null || currentPointIndex >= routePoints.size()) return;

        GeoPoint currentPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        GeoPoint targetPoint = routePoints.get(currentPointIndex);


        double distance = currentPoint.distanceToAsDouble(targetPoint);

        if (distance < MIN_DISTANCE) {
            currentPointIndex++;
            if (currentPointIndex < routePoints.size()) {
                // Broadcast next navigation instruction
                Intent intent = new Intent("navigation.next_instruction");
                intent.putExtra("instruction", getNextInstruction(currentPoint, targetPoint));
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            } else {
                // Navigation completed
                stopSelf();
            }
        }
    }

    private String getNextInstruction(GeoPoint current, GeoPoint next) {
        // Calculate bearing between points
        double bearing = current.bearingTo(next);

        // Convert bearing to direction instruction
        if (bearing >= 337.5 || bearing < 22.5) return "Đi thẳng";
        else if (bearing >= 22.5 && bearing < 67.5) return "Rẽ phải nhẹ";
        else if (bearing >= 67.5 && bearing < 112.5) return "Rẽ phải";
        else if (bearing >= 112.5 && bearing < 157.5) return "Rẽ phải gắt";
        else if (bearing >= 157.5 && bearing < 202.5) return "Quay lại";
        else if (bearing >= 202.5 && bearing < 247.5) return "Rẽ trái gắt";
        else if (bearing >= 247.5 && bearing < 292.5) return "Rẽ trái";
        else return "Rẽ trái nhẹ";
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
