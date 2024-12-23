package com.example.authentication_uiux.ui.home;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Geocoder;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import org.osmdroid.views.overlay.Overlay;

import com.example.authentication_uiux.Config;
import com.example.authentication_uiux.R;
import com.example.authentication_uiux.models.PotholeData;
import com.example.authentication_uiux.API.PotholeApi;
import com.example.authentication_uiux.ui.home.MapComponents.LocationSearchManager;
import com.example.authentication_uiux.ui.home.MapComponents.NavigationFragment;
import com.example.authentication_uiux.ui.home.MapComponents.NavigationManager;
import com.example.authentication_uiux.ui.home.MapComponents.NavigationService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import android.location.Location;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment implements SensorEventListener, MapEventsReceiver {
    private static final String TAG = "HomeFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1002;
    private static final float SHAKE_THRESHOLD = 30.0f;
    private static final long ALERT_DELAY_MS = 5000;

    private MapView mapView;
    private IMapController mapController;
    private MyLocationNewOverlay locationOverlay;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastAlertTime = 0;
    private View popupView;
    private Handler popupHandler;
    private EditText searchEditText;
    private ImageButton searchButton;
    private ImageButton zoomInButton, zoomOutButton;
    private FloatingActionButton trackLocationButton;
    private FloatingActionButton navigationButton, addPotholeButton;
    private Geocoder geocoder;
    private PotholeApi potholeApi;
    private Marker currentLocationMarker;
    private boolean isLocationTracking = false;

    private LocationSearchManager searchManager;
    private NavigationManager navigationManager;
    private Location currentLocation;
    private LocationManager locationManager;
    private Polyline routeOverlay;
    private GeoPoint selectedLocation;

    private String username;

    private CardView navigationInfoCard;
    private TextView routeInfoText;
    private TextView routeDetailText;
    private Button startNavigationButton;

    private List<GeoPoint> currentRoute;
    private BroadcastReceiver navigationReceiver;

    private Dialog navigationDialog;
    private EditText editTextStart, editTextDestination;
    private TextView textDistance, textDuration;
    private LinearLayout layoutNavigationInfo;
    private GeoPoint startPoint, endPoint;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            View root = inflater.inflate(R.layout.fragment_home, container, false);

            // Initialize username
            username = getUsernameFromAuth();

            initializeViews(root);
            initializeSensors();
            initializeMap();
            setupRetrofit();

            searchManager = new LocationSearchManager();
            navigationManager = new NavigationManager();

            setupNavigationReceiver();
            setupStartNavigationButton();

            fetchPotholesFromApi();

            initializeNavigationDialog();

            navigationButton.setOnClickListener(v -> {
                // Reset points and show dialog
                startPoint = null;
                endPoint = null;
                layoutNavigationInfo.setVisibility(View.GONE);
                editTextStart.setText("");
                editTextDestination.setText("");
                navigationDialog.show();
            });

            return root;
        } catch (Exception e) {
            Log.e(TAG, "Error inflating fragment: ", e);
            throw e;
        }
    }


    private void initializeViews(View root) {
        // Initialize other views first
        mapView = root.findViewById(R.id.map);
        searchEditText = root.findViewById(R.id.search_edit_text);
        searchButton = root.findViewById(R.id.search_button);
        zoomInButton = root.findViewById(R.id.button_zoom_in);
        zoomOutButton = root.findViewById(R.id.button_zoom_out);

        // Initialize FABs
        trackLocationButton = root.findViewById(R.id.button_track_location);
        navigationButton = root.findViewById(R.id.button_navigation);
        addPotholeButton = root.findViewById(R.id.button_add_pothole);
        navigationButton = root.findViewById(R.id.button_navigation);

        // Add null checks before setting up buttons
        if (trackLocationButton == null || navigationButton == null || addPotholeButton == null) {
            Log.e(TAG, "One or more FloatingActionButtons not found in layout");
            return;
        }

        geocoder = new Geocoder(requireContext(), Locale.getDefault());
        popupHandler = new Handler(Looper.getMainLooper());

        // Setup other components
        setupSearch();
        setupButtons();

        navigationInfoCard = root.findViewById(R.id.navigation_info_card);
        routeInfoText = root.findViewById(R.id.route_info_text);
        routeDetailText = root.findViewById(R.id.route_detail_text);
        startNavigationButton = root.findViewById(R.id.start_navigation_button);
    }

    private String copyMbTilesToInternalStorage() {
        File destinationFile = new File(requireContext().getFilesDir(), "map.mbtiles");
        if (!destinationFile.exists()) {
            try (InputStream inputStream = requireContext().getAssets().open("map.mbtiles");
                 FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error copying MBTiles file", e);
            }
        }
        return destinationFile.getAbsolutePath();
    }

    private void initializeMap() {
        // Copy MBTiles from assets to internal storage
        String mbTilesPath = copyMbTilesToInternalStorage();

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        mapController = mapView.getController();
        mapController.setZoom(15.0);
        GeoPoint startPoint = new GeoPoint(10.870894, 106.803054);
        mapController.setCenter(startPoint);

        File mbTilesFile = new File(mbTilesPath);
        if (mbTilesFile.exists()) {
            try {
                ITileSource tileSource = TileSourceFactory.DEFAULT_TILE_SOURCE;
                SimpleRegisterReceiver registerReceiver = new SimpleRegisterReceiver(requireContext());

                MapTileFileArchiveProvider fileArchiveProvider = new MapTileFileArchiveProvider(
                        registerReceiver,
                        tileSource,
                        new IArchiveFile[]{(IArchiveFile) mbTilesFile}
                );

                MapTileProviderArray tileProviderArray = new MapTileProviderArray(tileSource, registerReceiver,
                        new MapTileFileArchiveProvider[]{fileArchiveProvider});

                mapView.setTileProvider(tileProviderArray);
            } catch (Exception e) {
                Log.e(TAG, "Error setting up MBTiles", e);
            }
        }

        // Location tracking overlay
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), mapView);
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);

        // Map events overlay for tap interactions
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);
        mapView.getOverlays().add(mapEventsOverlay);
    }

    //   ----------------------------SEARCH ENGINE----------------------
    private void setupSearch() {
        searchButton.setOnClickListener(v -> {
            performSearch();
            hideKeyboard(this.requireActivity());
            searchEditText.setText("");
        });
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void setupButtons() {
        if (zoomInButton != null) {
            zoomInButton.setOnClickListener(v -> mapController.zoomIn());
        }

        if (zoomOutButton != null) {
            zoomOutButton.setOnClickListener(v -> mapController.zoomOut());
        }

        if (trackLocationButton != null) {
            trackLocationButton.setOnClickListener(v -> {
                if (!isLocationTracking) {
                    checkAndRequestLocationPermission();
                } else {
                    stopLocationTracking();
                }
            });
        }


        if (addPotholeButton != null) {
            addPotholeButton.setOnClickListener(v -> {
                GeoPoint currentLoc = locationOverlay.getMyLocation();
                if (currentLoc == null) {
                    showToast("Unable to determine your location. Please try again.");
                    return;
                } else {
                    PotholeData data = new PotholeData(
                            currentLoc.getLatitude(),
                            currentLoc.getLongitude(),
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()),
                            username,
                            "reported"
                    );
                    showReportPotholeDialog(currentLoc, data);
                    mapView.invalidate();
                }
            });
        }
    }

    private static void hideKeyboard(Activity activity) {
        // Lấy đối tượng InputMethodManager
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        // Kiểm tra xem bàn phím có đang mở hay không
        if (inputMethodManager != null && activity.getCurrentFocus() != null) {
            // Ẩn bàn phím
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            searchManager.searchLocation(query, new LocationSearchManager.SearchCallBack() {
                @Override
                public void onLocationFound(double lat, double lon, String name) {
                    selectedLocation = new GeoPoint(lat, lon);

                    // Cập nhật camera để zoom vào vị trí tìm được
                    mapController.animateTo(selectedLocation);
                    mapController.setZoom(20.0);

                    // Add marker
                    Marker marker = new Marker(mapView);
                    marker.setPosition(selectedLocation);
                    marker.setTitle(name);
                    Drawable icon = getResources().getDrawable(R.drawable.ic_red_location);
                    marker.setIcon(icon);
                    mapView.getOverlays().add(marker);

                    marker.setOnMarkerClickListener((marker1, mapview1) -> {
                        String info = "Tên: " + marker1.getTitle() + "\nVị trí: " + selectedLocation.getLatitude() + ", " + selectedLocation.getLongitude();
                        showLocationDialog(info, selectedLocation);
                        return true;
                    });

                    showNavigationOption(selectedLocation);

                }
                @Override
                public void onError(String message) {
                    showToast(message);
                }
            });
        } else {
            showToast("Please enter a search query");
        }
    }

    private GeoPoint performSearch(EditText searchEditText) {
        String query = searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            searchManager.searchLocation(query, new LocationSearchManager.SearchCallBack() {
                @Override
                public void onLocationFound(double lat, double lon, String name) {
                    selectedLocation = new GeoPoint(lat, lon);

                    // Add marker
                    Marker marker = new Marker(mapView);
                    marker.setPosition(selectedLocation);
                    marker.setTitle(name);
                    Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_red_location, null);
                    marker.setIcon(icon);
                    mapView.getOverlays().add(marker);

                    marker.setOnMarkerClickListener((marker1, mapview1) -> {
                        String info = "Tên: " + marker1.getTitle() + "\nVị trí: " + selectedLocation.getLatitude() + ", " + selectedLocation.getLongitude();
                        showLocationDialog(info, selectedLocation);
                        return true;
                    });
                }
                @Override
                public void onError(String message) {
                    showToast(message);
                }
            });
            return selectedLocation;
        } else {
            showToast("Please enter a search query");
            return null;
        }
    }

    // ------------------------------ROUTE------------------------------------

    private void showLocationDialog(String info, GeoPoint selectedLocation) {
        // Create a dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Set dialog title
        builder.setTitle("Thông tin địa điểm");

        // Set dialog message
        builder.setMessage(info);

        // Add a button to start navigation
        builder.setPositiveButton("Dẫn đường", (dialog, which) -> {
            // Trigger the navigation function when the button is clicked
            dialog.dismiss();
        });

        // Add a cancel button to dismiss the dialog
        builder.setNegativeButton("Hủy", (dialog, which) -> {
            dialog.dismiss();
            clearNavigationRoute();
            mapController.setZoom(15.0);
            GeoPoint startPoint = new GeoPoint(10.870894, 106.803054);
            mapController.setCenter(startPoint);
            mapView.getOverlays().clear();
            mapView.invalidate();
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showNavigationOption(GeoPoint destination) {
        if (destination == null) {
            showToast("Điểm đến không hợp lệ");
            return;
        }

        // Get current location (using your fixed point for now)
        GeoPoint currentLoc = new GeoPoint(10.870894, 106.803054);
        //GeoPoint currentLoc = locationOverlay.getMyLocation();

        if (currentLoc != null) {
            try {
                // Show loading indicator
                showLoadingIndicator();

                // Remove existing route if present
                if (routeOverlay != null) {
                    mapView.getOverlays().remove(routeOverlay);
                }

                // Calculate new route
                navigationManager.getRoute(currentLoc, destination,
                        new NavigationManager.NavigationCallback() {
                            @Override
                            public void onRouteFound(ArrayList<GeoPoint> route, String duration, String distance) {
                                onRouteFound((List<GeoPoint>) (List<?>) route, duration, distance);
                            }

                            @Override
                            public void onRouteFound(List<GeoPoint> route, String duration, String distance) {
                                hideLoadingIndicator();

                                if (route == null || route.isEmpty()) {
                                    showToast("Không tìm thấy đường đi phù hợp");
                                    return;
                                }

                                try {
                                    // Save current route
                                    currentRoute = route;

                                    // Create and configure route overlay
                                    routeOverlay = new Polyline();
                                    routeOverlay.setPoints(route);
                                    routeOverlay.setColor(ContextCompat.getColor(requireContext(), R.color.blue));
                                    routeOverlay.setWidth(5f);
                                    mapView.getOverlays().add(routeOverlay);

                                    // Display navigation info
                                    layoutNavigationInfo.setVisibility(View.VISIBLE);
                                    routeInfoText.setText(String.format("%s • %s", distance, duration));

                                    // Calculate bounding box for the route
                                    BoundingBox boundingBox = getBoundingBoxForRoute(route);

                                    // Zoom map to show entire route
                                    mapView.zoomToBoundingBox(boundingBox, true);
                                    mapView.invalidate();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    showToast("Lỗi hiển thị đường đi: " + e.getMessage());
                                }
                            }

                            @Override
                            public void onError(String message) {
                                hideLoadingIndicator();
                                navigationInfoCard.setVisibility(View.GONE);
                                showToast(message);
                            }
                        });
            } catch (Exception e) {
                hideLoadingIndicator();
                showToast("Lỗi: " + e.getMessage());
            }
        } else {
            showToast("Vui lòng bật dịch vụ vị trí để sử dụng tính năng dẫn đường");
        }
    }

    private void showNavigationRoute(GeoPoint startPoint, GeoPoint endPoint) {
        if (startPoint == null || endPoint == null) {
            showToast("Please select both start and destination points");
            return;
        }

        fetchPotholesFromApi();

        // Remove existing route if present
        if (routeOverlay != null) {
            mapView.getOverlays().remove(routeOverlay);
        }

        // Calculate and draw new route
        navigationManager.getRoute(startPoint, endPoint,
                new NavigationManager.NavigationCallback() {
                    @Override
                    public void onRouteFound(List<GeoPoint> route, String duration, String distance) {
                        currentRoute = route;

                        // Create and configure route overlay
                        routeOverlay = new Polyline();
                        routeOverlay.setPoints(route);
                        routeOverlay.setColor(ContextCompat.getColor(requireContext(), R.color.blue));
                        routeOverlay.setWidth(5f);
                        mapView.getOverlays().add(routeOverlay);

                        // Display navigation info
                        layoutNavigationInfo.setVisibility(View.VISIBLE);
                        routeInfoText.setText(String.format("%s • %s", distance, duration));

                        // Calculate bounding box for the route
                        BoundingBox boundingBox = getBoundingBoxForRoute(route);

                        // Zoom map to show entire route
                        mapView.zoomToBoundingBox(boundingBox, true);
                        mapView.invalidate();
                    }

                    @Override
                    public void onRouteFound(ArrayList<GeoPoint> route, String duration, String distance) {
                        // Handle ArrayList version if needed
                        onRouteFound((List<GeoPoint>) (List<?>) route, duration, distance);
                    }

                    @Override
                    public void onError(String message) {
                        layoutNavigationInfo.setVisibility(View.GONE);
                        showToast(message);
                    }
                });
    }

    private List<PotholeData> fetchPotholes() {
        List<PotholeData> potholeList = new ArrayList<>();

        // Gọi API đồng bộ để lấy danh sách ổ gà
        try {
            // Tạo một lệnh Retrofit đồng bộ
            Response<List<PotholeData>> response = potholeApi.getPotholes().execute();

            if (response.isSuccessful() && response.body() != null) {
                potholeList = response.body(); // Lưu kết quả vào danh sách
                Log.d(TAG, "Fetched " + potholeList.size() + " potholes from API");
            } else {
                Log.e(TAG, "Failed to fetch potholes. Response code: " + response.code());
                if (response.errorBody() != null) {
                    Log.e(TAG, "Error body: " + response.errorBody().string());
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error fetching potholes", e);
        }

        return potholeList;
    }


    // Hàm tinh toan hop gioi han cua duong di
    private BoundingBox getBoundingBoxForRoute(List<GeoPoint> route) {
        double minLat = Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        double maxLon = Double.MIN_VALUE;

        // Iterate through the route points to find the min and max latitudes and longitudes
        for (GeoPoint point : route) {
            double lat = point.getLatitude();
            double lon = point.getLongitude();

            if (lat < minLat) minLat = lat;
            if (lat > maxLat) maxLat = lat;
            if (lon < minLon) minLon = lon;
            if (lon > maxLon) maxLon = lon;
        }

        return new BoundingBox(maxLat, maxLon, minLat, minLon);
    }

    // Add this method to clear the navigation route
    private void clearNavigationRoute() {
        if (routeOverlay != null) {
            mapView.getOverlays().remove(routeOverlay);
            routeOverlay = null;
            mapView.invalidate();
        }
    }


    private void showReportPotholeDialog(GeoPoint currentLoc, PotholeData data) {
        if (currentLoc == null) {
            showToast("Unable to determine your location. Please try again.");
            return;
        }

        // Tạo dialog xác nhận lần đầu
        new AlertDialog.Builder(requireContext())
                .setTitle("Report Pothole")
                .setMessage("Do you want to report your current location as a pothole?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Nếu người dùng chọn "Có", hiển thị dialog xác nhận lần hai
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Confirm Report")
                            .setMessage("Once reported, this action cannot be undone. Are you sure?")
                            .setPositiveButton("Yes", (confirmDialog, confirmWhich) -> {
                                // Lưu dữ liệu ổ gà và thêm marker
                                savePotholeDataToMongoDB(currentLoc);
                                addPotholeMarker(currentLoc, data);
                                showToast("Pothole reported successfully!");
                            })
                            .setNegativeButton("No", (confirmDialog, confirmWhich) -> {
                                showToast("Pothole report canceled.");
                                confirmDialog.dismiss();
                            })
                            .show();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    showToast("Pothole report canceled.");
                    dialog.dismiss();
                })
                .show();
    }

    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationTracking();
        }
    }

    private void startLocationTracking() {
        locationOverlay.enableFollowLocation();
        locationOverlay.runOnFirstFix(() -> {
            GeoPoint userLocation = locationOverlay.getMyLocation();

            if (userLocation != null) {
                requireActivity().runOnUiThread(() -> {
                    // Add current location marker
                    currentLocationMarker = new Marker(mapView);
                    currentLocationMarker.setPosition(userLocation);
                    currentLocationMarker.setTitle("Your Location");
                    mapView.getOverlays().add(currentLocationMarker);

                    mapController.setCenter(userLocation);
                    mapController.setZoom(18.0);

                    isLocationTracking = true;
                    trackLocationButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                });
            } else {
                requireActivity().runOnUiThread(() -> showToast("Unable to get current location"));
            }
        });
    }

    private void stopLocationTracking() {
        locationOverlay.disableFollowLocation();

        if (currentLocationMarker != null) {
            mapView.getOverlays().remove(currentLocationMarker);
            currentLocationMarker = null;
        }

        isLocationTracking = false;
        trackLocationButton.setImageResource(android.R.drawable.ic_menu_mylocation);
    }


//    ---------------------------------NAVIGATION--------------------------------

    private void setupNavigationReceiver() {
        navigationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("navigation.next_instruction")) {
                    String instruction = intent.getStringExtra("instruction");
                    routeDetailText.setText(instruction);
                }
            }
        };

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                navigationReceiver,
                new IntentFilter("navigation.next_instruction")
        );
    }

    private void setupStartNavigationButton() {
        startNavigationButton.setOnClickListener(v -> {
            if (currentRoute != null) {
                startNavigation();
            } else showToast("There's no any route!");
        });
    }

    private void startNavigation() {
        // Check location permission
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            showToast("Please enable location permission");
            return;
        }

        // Start navigation service
        Intent intent = new Intent(requireContext(), NavigationService.class);
        intent.putParcelableArrayListExtra("route", new ArrayList<>(currentRoute));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent);
        } else {
            requireContext().startService(intent);
        }

        // Update UI
        startNavigationButton.setText("Dừng");
        startNavigationButton.setOnClickListener(v -> stopNavigation());
    }

    private void stopNavigation() {
        // Stop navigation service
        requireContext().stopService(new Intent(requireContext(), NavigationService.class));

        // Reset UI
        startNavigationButton.setText("Bắt đầu");
        startNavigationButton.setOnClickListener(v -> startNavigation());
        routeDetailText.setText("");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (navigationReceiver != null) {
            LocalBroadcastManager.getInstance(requireContext())
                    .unregisterReceiver(navigationReceiver);
        }
        stopNavigation();
    }


    // Initialize the navigation dialog
    private void initializeNavigationDialog() {
        navigationDialog = new Dialog(requireContext());
        navigationDialog.setContentView(R.layout.dialog_navigation);
        navigationDialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        // Initialize views
        editTextStart = navigationDialog.findViewById(R.id.edit_text_start);
        editTextDestination = navigationDialog.findViewById(R.id.edit_text_destination);
        textDistance = navigationDialog.findViewById(R.id.text_distance);
        textDuration = navigationDialog.findViewById(R.id.text_duration);
        layoutNavigationInfo = navigationDialog.findViewById(R.id.layout_navigation_info);
        Button buttonNavigate = navigationDialog.findViewById(R.id.button_navigate);
        Button buttonCancel = navigationDialog.findViewById(R.id.button_cancel);

        buttonNavigate.setOnClickListener(v -> {
            String startAddress = editTextStart.getText().toString();
            String destAddress = editTextDestination.getText().toString();

            if (startAddress.isEmpty() || destAddress.isEmpty()) {
                showToast("Please enter both locations");
                return;
            }

            // Show loading indicator
            showLoadingIndicator();

            // Search for start location
            LocationSearchManager.searchLocation(startAddress, new LocationSearchManager.SearchCallBack() {
                @Override
                public void onLocationFound(double lat, double lon, String name) {
                    startPoint = new GeoPoint(lat, lon);
                    // After finding start point, search for destination
                    searchDestination(destAddress);
                }

                @Override
                public void onError(String message) {
                    hideLoadingIndicator();
                    showToast("Error finding start location: " + message);
                }
            });
        });

        // Setup cancel button
        buttonCancel.setOnClickListener(v -> {
            navigationDialog.dismiss();
            clearNavigationRoute();
        });
    }

    private void searchDestination(String destAddress) {
        LocationSearchManager.searchLocation(destAddress, new LocationSearchManager.SearchCallBack() {
            @Override
            public void onLocationFound(double lat, double lon, String name) {
                endPoint = new GeoPoint(lat, lon);
                hideLoadingIndicator();
                Marker des = new Marker(mapView);
                des.setPosition(endPoint);
                des.setTitle(name);
                Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_red_location, null);
                des.setIcon(icon);
                mapView.getOverlays().add(des);

                // Now we have both points, proceed with navigation
                if (startPoint != null && endPoint != null) {
                    showNavigationRoute(startPoint, endPoint);
                    navigationDialog.dismiss();
                }
            }

            @Override
            public void onError(String message) {
                hideLoadingIndicator();
                showToast("Error finding destination: " + message);
            }
        });
    }

    // Helper methods for loading indicator
    private ProgressDialog loadingDialog;

    private void showLoadingIndicator() {
        if (loadingDialog == null) {
            loadingDialog = new ProgressDialog(requireContext());
            loadingDialog.setMessage("Searching locations...");
            loadingDialog.setCancelable(false);
        }
        loadingDialog.show();
    }

    private void hideLoadingIndicator() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }


//    -----------------------------------POTHOLE--------------------------------

    private void showPotholePopup(GeoPoint location) {
        // Ensure we're on the main thread
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(() -> showPotholePopup(location));
            return;
        }

        // Remove any existing popup
        removePopup();

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        popupView = inflater.inflate(R.layout.popup_pothole, null);

        // Find confirm button
        MaterialButton btnConfirm = popupView.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(v -> {
            // Center the map at the current location
            mapController.setCenter(location);
            mapController.setZoom(18.0); // Optional: Set zoom level

            locationOverlay.disableMyLocation();

            //Tạo mới dữ liệu ổ gà
            PotholeData data = new PotholeData(
                    location.getLatitude(),
                    location.getLongitude(),
                    getCurrentTime(),
                    username,
                    "reported"
            );
            // Add a marker at the current location
            addPotholeMarker(location, data);

            // Save pothole data to MongoDB
            savePotholeDataToMongoDB(location);
            removePopup();
        });

        // Ensure the root view is not null
        ViewGroup rootView = requireActivity().findViewById(android.R.id.content);
        if (rootView != null) {
            // Create layout params to center the popup
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.CENTER;

            rootView.addView(popupView, params);
        }

        // Auto-dismiss after 10 seconds
        popupHandler.postDelayed(this::removePopup, 10000);
    }

    private void removePopup() {
        if (popupView != null) {
            ViewGroup rootView = requireActivity().findViewById(android.R.id.content);
            if (rootView != null) {
                rootView.removeView(popupView);
            }
            popupView = null;
        }
        popupHandler.removeCallbacksAndMessages(null);
    }

    private String getUsernameFromAuth() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("username", "default_username"); // Replace "default_username" with a suitable default value
    }

    private void savePotholeDataToMongoDB(GeoPoint location) {
        if(location == null){
            showToast("Invalid Location Data");
            return;
        }

        // Format the current time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));  // Ensure UTC time
        String currentTime = sdf.format(new Date());

        // Create PotholeData object
        PotholeData potholeData = new PotholeData(
                location.getLatitude(),
                location.getLongitude(),
                currentTime,
                username,
                "reported"
        );

        // Use Retrofit to upload the pothole data
        potholeApi.addPothole(potholeData).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    addPotholeMarker(location, potholeData); // Add pothole marker to the map
                    showToast("Pothole saved successfully");
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e(TAG, "Error saving pothole: " + errorBody);
                        showToast("Error saving pothole: " + errorBody);
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                        showToast("Error saving pothole");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error", t);
                showToast("Network error: " + t.getMessage());
            }
        });
    }

    private void addPotholeMarker(GeoPoint location, PotholeData data) {
        Marker potholeMarker = new Marker(mapView);
        potholeMarker.setPosition(location);

        //Thiết lập tiêu đề và mô tả
        potholeMarker.setTitle("Pothole");
        potholeMarker.setSnippet("Location: " + location.getLatitude() + ", " + location.getLongitude() +
                "\nDetected at: " + getCurrentTime());

        Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.pothole_icon, null);
        potholeMarker.setIcon(icon);


        //Hiển thị bong bóng thông tin
        potholeMarker.setOnMarkerClickListener((marker, mapView) -> {
            showPotholeDialog(data);
            return true; //Ngăn việc map di chuyển khi click vào marker
        });

        mapView.getOverlays().add(potholeMarker);
        mapView.invalidate(); //Redraw the map
    }

    private void showPotholeDialog(PotholeData potholeData) {
        // Tạo Dialog Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Pothole Details");

        // Inflate layout tùy chỉnh cho Dialog
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_pothole_details, null);
        builder.setView(dialogView);

        // Gán các giá trị từ PotholeData vào View
        TextView latitudeTextView = dialogView.findViewById(R.id.latitude_text_view);
        TextView longitudeTextView = dialogView.findViewById(R.id.longitude_text_view);
        TextView userTextView = dialogView.findViewById(R.id.user_text_view);
        TextView detectionTimeTextView = dialogView.findViewById(R.id.detection_time_text_view);
        Spinner statusSpinner = dialogView.findViewById(R.id.status_spinner);

        latitudeTextView.setText("Latitude: " + potholeData.getLatitude());
        longitudeTextView.setText("Longitude: " + potholeData.getLongitude());
        userTextView.setText("Reported by: " + potholeData.getUser());
        detectionTimeTextView.setText("Detected at: " + potholeData.getDetectionTime());

        // Cấu hình Spinner trạng thái
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"reported", "in progress", "solved"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        // Đặt trạng thái hiện tại
        statusSpinner.setSelection(adapter.getPosition(potholeData.getStatus()));

        // Nút "Cập nhật"
        builder.setPositiveButton("Update", (dialog, which) -> {
            String newStatus = statusSpinner.getSelectedItem().toString();
            updatePotholeStatusInMongoDB(potholeData, newStatus);
        });

        // Nút "Hủy"
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Hiển thị Dialog
        builder.show();
    }

    private void updatePotholeStatusInMongoDB(PotholeData potholeData, String newStatus) {
        potholeData.setStatus(newStatus);

        // Gửi dữ liệu cập nhật tới server qua Retrofit
        potholeApi.updatePotholeStatus(potholeData).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    showToast("Pothole status updated successfully!");
                } else {
                    showToast("Failed to update pothole status: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                showToast("Network error: " + t.getMessage());
            }
        });
    }


    // Hàm lấy thời gian hiện tại
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        return sdf.format(new Date());
    }

    private void fetchPotholesFromApi() {
        potholeApi.getPotholes().enqueue(new Callback<List<PotholeData>>() {
            @Override
            public void onResponse(@NonNull Call<List<PotholeData>> call, @NonNull Response<List<PotholeData>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    showToast("Successfully fetched potholes: " + response.body().size());

                    for (PotholeData pothole : response.body()) {
                        Log.d(TAG, "onResponse: Pothole location - Latitude: " + pothole.getLatitude() + ", Longitude: " + pothole.getLongitude());
                        GeoPoint location = new GeoPoint(pothole.getLatitude(), pothole.getLongitude());
                        addPotholeMarker(location, pothole);
                    }
                } else {
                    Log.e(TAG, "onResponse: Failed to fetch potholes. Response code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorDetails = response.errorBody().string();
                            Log.e(TAG, "onResponse: Error details - " + errorDetails);
                            showToast("Failed to fetch potholes: " + errorDetails);
                        } catch (IOException e) {
                            Log.e(TAG, "onResponse: Failed to read error body", e);
                        }
                    } else {
                        showToast("Failed to fetch potholes: Empty response body");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PotholeData>> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: Error occurred while fetching potholes", t);
                showToast("Network error: Unable to fetch potholes. " + t.getMessage());
            }
        });
    }

    // Sensor and lifecycle methods
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float delta = calculateShakeForce(event.values);
            long currentTime = System.currentTimeMillis();

            if (delta > SHAKE_THRESHOLD && currentTime - lastAlertTime > ALERT_DELAY_MS) {
                lastAlertTime = currentTime;

                // Use the main thread to show popup
                new Handler(Looper.getMainLooper()).post(() -> {
                    // Ensure we have a valid location before showing popup
                    GeoPoint currentLocation = locationOverlay.getMyLocation();
                    if (currentLocation != null) {
                        showPotholePopup(currentLocation);
                    } else {
                        showToast("Cannot determine current location");
                    }
                });
            }
        }
    }

    // ----------------------------POTHOLES ON ROUTE------------------------------------

    private boolean isPotholeNearSegment(PotholeData pothole, GeoPoint start, GeoPoint end, double threshold) {
        double distance = distanceToSegment(pothole, start, end);
        return distance <= threshold    ;
    }

    private double distanceToSegment(PotholeData point, GeoPoint start, GeoPoint end) {
        double A = point.getLatitude();
        double B = point.getLongitude();
        double C = end.getLatitude() - start.getLatitude();
        double D = end.getLongitude() - start.getLongitude();

        double dot = A * C + B * D;
        double len_sq = C * C + D * D;
        double param = -1;
        if (len_sq != 0) {
            param = dot / len_sq;
        }

        double xx, yy;
        if (param < 0) {
            xx = start.getLatitude();
            yy = start.getLongitude();
        } else if (param > 1) {
            xx = end.getLatitude();
            yy = end.getLongitude();
        } else {
            xx = start.getLatitude() + param * C;
            yy = start.getLongitude() + param * D;
        }

        double dx = point.getLatitude() - xx;
        double dy = point.getLongitude() - yy;

        return Math.sqrt(dx * dx + dy * dy) * 111320; // Chuyển đổi độ sang mét (1 độ ≈ 111.32 km)
    }


    private int countPotholesOnRoute(List<GeoPoint> latLongList, List<PotholeData> potholeList, double threshold) {
        int count = 0;
        for (PotholeData pothole : potholeList) {
            for (int i = 0; i < latLongList.size() - 1; i++) {
                GeoPoint start = latLongList.get(i);
                GeoPoint end = latLongList.get(i + 1);

                if (isPotholeNearSegment(pothole, start, end, threshold)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    private void showPotholeAlert(int count) {
        if (count > 0) {
            String message = "Warning: " + count + " potholes detected near your route!";

            // Hiển thị thông báo trong ứng dụng
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Pothole Alert")
                    .setMessage(message)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();

            // Gửi Notification
            showPotholeNotification(count);
        }
    }

    private void showPotholeNotification(int count) {
        if (count > 0) {
            String message = "Warning: " + count + " potholes detected near your route!";

            // Tạo Notification Channel (chỉ cần tạo một lần cho API >= 26)
            String channelId = "pothole_alert_channel";
            String channelName = "Pothole Alerts";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }

            // Tạo Notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), channelId)
                    .setSmallIcon(R.drawable.ic_pothole_warning)
                    .setContentTitle("Pothole Alert")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)); // Âm thanh thông báo

            // Hiển thị Notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Yêu cầu quyền nếu chưa được cấp
                requestPermissions(
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                );
                return;
            }
            notificationManager.notify(1, builder.build()); // ID = 1 để nhận diện thông báo
        }
    }



    //    -----------------------------------------------------------------------------------
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
    }

    // MapEventsReceiver methods
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        showPotholePopup(p);

        return true;
    }

    private float calculateShakeForce(float[] values) {
        float x = values[0];
        float y = values[1];
        float z = values[2];
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        potholeApi = retrofit.create(PotholeApi.class);
    }

    private void initializeSensors() {
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Configuration.getInstance().load(requireContext(), requireActivity().getPreferences(Context.MODE_PRIVATE));
        mapView.onResume();

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        fetchPotholesFromApi();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        stopLocationTracking();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDetach();
        popupHandler.removeCallbacksAndMessages(null);
    }
}