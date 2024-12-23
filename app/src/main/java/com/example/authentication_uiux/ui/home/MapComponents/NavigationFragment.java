package com.example.authentication_uiux.ui.home.MapComponents;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.authentication_uiux.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NavigationFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private TextView routeDetailText;
    private Button startNavigationButton;
    private List<GeoPoint> currentRoute;
    private BroadcastReceiver navigationReceiver;
    private boolean isNavigating = false;
    private ProgressDialog loadingDialog;
    private GeoPoint destination;

    public NavigationFragment() {
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupNavigationReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_navigation, container, false);
    }

    private void initViews(View view) {
        routeDetailText = view.findViewById(R.id.routeDetailText);
        startNavigationButton = view.findViewById(R.id.startNavigationButton);
        setupStartNavigationButton();
    }

    private void setupNavigationReceiver() {
        navigationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null) return;

                switch (intent.getAction()) {
                    case "navigation.next_instruction":
                        handleNextInstruction(intent);
                        break;
                    case "navigation.off_route":
                        handleOffRoute();
                        break;
                    case "navigation.progress_update":
                        handleProgressUpdate(intent);
                        break;
                    case "navigation.completed":
                        handleNavigationCompleted();
                        break;
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("navigation.next_instruction");
        filter.addAction("navigation.off_route");
        filter.addAction("navigation.progress_update");
        filter.addAction("navigation.completed");

        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(navigationReceiver, filter);
    }

    private void setupStartNavigationButton() {
        startNavigationButton.setOnClickListener(v -> {
            if (!isNavigating) {
                if (currentRoute != null && !currentRoute.isEmpty()) {
                    startNavigation();
                } else {
                    showToast("Chưa có tuyến đường nào!");
                }
            } else {
                showStopNavigationDialog();
            }
        });
    }

    private void startNavigation() {
        if (!checkAndRequestPermissions()) {
            return;
        }

        try {
            // Start navigation service
            Intent intent = new Intent(requireContext(), NavigationService.class);
            intent.putParcelableArrayListExtra("route", new ArrayList<>(currentRoute));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(intent);
            } else {
                requireContext().startService(intent);
            }

            isNavigating = true;
            updateNavigationUI();
            showToast("Bắt đầu điều hướng");

        } catch (Exception e) {
            Log.e("Navigation", "Error starting navigation", e);
            showToast("Không thể bắt đầu điều hướng");
        }
    }

    private void stopNavigation() {
        try {
            requireContext().stopService(new Intent(requireContext(), NavigationService.class));
            isNavigating = false;
            updateNavigationUI();
            showToast("Đã dừng điều hướng");
        } catch (Exception e) {
            Log.e("Navigation", "Error stopping navigation", e);
        }
    }

    private void updateNavigationUI() {
        startNavigationButton.setText(isNavigating ? "Dừng" : "Bắt đầu");
        if (!isNavigating) {
            routeDetailText.setText("");
        }
    }

    private boolean checkAndRequestPermissions() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            showToast("Vui lòng cấp quyền truy cập vị trí");
            return false;
        }
        return true;
    }

    private void handleNextInstruction(Intent intent) {
        String instruction = intent.getStringExtra("instruction");
        if (instruction != null) {
            routeDetailText.setText(instruction);
        }
    }

    private void handleOffRoute() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đi chệch hướng")
                .setMessage("Bạn đã đi chệch khỏi tuyến đường. Bạn có muốn tính toán lại tuyến đường không?")
                .setPositiveButton("Có", (dialog, which) -> recalculateRoute())
                .setNegativeButton("Không", null)
                .show();
    }

    private static final String OSRM_API_URL = "https://router.project-osrm.org/route/v1/driving/";
    private OkHttpClient client;
    private Location currentLocation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    private void recalculateRoute() {
        if (!checkAndRequestPermissions()) {
            return;
        }

        showLoadingDialog();

        // Lấy vị trí hiện tại
        LocationManager locationManager = (LocationManager) requireContext()
                .getSystemService(Context.LOCATION_SERVICE);

        try {
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            Log.e("Navigation", "Location permission denied", e);
            hideLoadingDialog();
            showError("Không thể lấy vị trí hiện tại");
            return;
        }

        if (currentLocation == null || destination == null) {
            hideLoadingDialog();
            showError("Không đủ thông tin để tính toán lại tuyến đường");
            return;
        }

        // Tạo URL cho OSRM API
        String url = String.format(Locale.US, "%s%f,%f;%f,%f?steps=true&geometries=polyline&overview=full",
                OSRM_API_URL,
                currentLocation.getLongitude(), currentLocation.getLatitude(),
                destination.getLongitude(), destination.getLatitude());

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                    hideLoadingDialog();
                    showError("Không thể kết nối với server. Vui lòng thử lại sau.");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> {
                        hideLoadingDialog();
                        showError("Lỗi server. Vui lòng thử lại sau.");
                    });
                    return;
                }

                try {
                    String jsonData = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonData);

                    // Parse route data và cập nhật UI
                    List<GeoPoint> newRoute = parseRouteFromJson(jsonObject);

                    requireActivity().runOnUiThread(() -> {
                        hideLoadingDialog();
                        if (newRoute != null && !newRoute.isEmpty()) {
                            stopNavigation(); // Dừng navigation hiện tại
                            currentRoute = newRoute; // Cập nhật route mới
                            startNavigation(); // Bắt đầu lại với route mới
                            showToast("Đã tính toán lại tuyến đường mới");

                            // Broadcast sự kiện route mới cho map fragment (nếu cần)
                            Intent intent = new Intent("navigation.route_updated");
                            intent.putParcelableArrayListExtra("new_route", new ArrayList<>(newRoute));
                            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
                        } else {
                            showError("Không tìm thấy tuyến đường phù hợp");
                        }
                    });

                } catch (JSONException e) {
                    Log.e("Navigation", "JSON parsing error", e);
                    requireActivity().runOnUiThread(() -> {
                        hideLoadingDialog();
                        showError("Lỗi xử lý dữ liệu tuyến đường");
                    });
                }
            }
        });
    }

    private List<GeoPoint> parseRouteFromJson(JSONObject jsonObject) throws JSONException {
        List<GeoPoint> routePoints = new ArrayList<>();

        JSONArray routes = jsonObject.getJSONArray("routes");
        if (routes.length() == 0) return null;

        JSONObject route = routes.getJSONObject(0);
        String geometry = route.getString("geometry");

        // Decode Google Polyline format
        List<LatLng> decodedPath = PolyUtil.decode(geometry);

        for (LatLng latLng : decodedPath) {
            routePoints.add(new GeoPoint(latLng.latitude, latLng.longitude));
        }

        return routePoints;
    }

    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new ProgressDialog(requireContext());
            loadingDialog.setMessage("Đang tính toán lại tuyến đường...");
            loadingDialog.setCancelable(false);
        }
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void showError(String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Lỗi")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void handleProgressUpdate(Intent intent) {
        double distance = intent.getDoubleExtra("distance", 0);
        int currentPoint = intent.getIntExtra("current_point_index", 0);
        int totalPoints = intent.getIntExtra("total_points", 0);

        // Cập nhật UI hiển thị tiến trình nếu cần
        String progress = String.format(Locale.getDefault(),
                "%.1f km - Điểm %d/%d",
                distance/1000,
                currentPoint + 1,
                totalPoints
        );
        // Có thể thêm TextView để hiển thị progress
    }

    private void handleNavigationCompleted() {
        showToast("Đã đến điểm đến!");
        stopNavigation();
    }

    private void showStopNavigationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Dừng điều hướng")
                .setMessage("Bạn có chắc muốn dừng điều hướng không?")
                .setPositiveButton("Có", (dialog, which) -> stopNavigation())
                .setNegativeButton("Không", null)
                .show();
    }

    // Method để set route từ bên ngoài
    public void setRoute(List<GeoPoint> route, GeoPoint destination) {
        this.currentRoute = route;
        this.destination = destination;
        if (isAdded()) {
            startNavigationButton.setEnabled(route != null && !route.isEmpty());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startNavigation();
            } else {
                showToast("Không thể điều hướng khi chưa có quyền truy cập vị trí");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (navigationReceiver != null) {
            try {
                LocalBroadcastManager.getInstance(requireContext())
                        .unregisterReceiver(navigationReceiver);
            } catch (Exception e) {
                Log.e("Navigation", "Error unregistering receiver", e);
            }
        }
        stopNavigation();
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}