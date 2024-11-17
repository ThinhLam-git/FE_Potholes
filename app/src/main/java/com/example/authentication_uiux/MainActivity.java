package com.example.authentication_uiux;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.authentication_uiux.API.UserApi;
import com.example.authentication_uiux.models.user.LoginRequest;
import com.example.authentication_uiux.models.user.LoginResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.authentication_uiux.databinding.ActivityMainBinding;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import com.mapbox.mapboxsdk.camera.CameraPosition; // Import CameraPosition here
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private UserApi apiService;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Mapbox
        Mapbox.getInstance(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Initialize Retrofit and ApiService
        Retrofit retrofit = RetrofitClient.getClient();
        apiService = retrofit.create(UserApi.class);

        // Test the API
        testApi();

        // Initialize MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        setupMap();
    }



// Other imports...

    private void setupMap() {
        String styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=" + getMapTilerKey();
        mapView.getMapAsync(map -> {
            // Set the style using the styleUrl
            map.setStyle(new Style.Builder().fromUrl(styleUrl), new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    map.getUiSettings().setAttributionMargins(15, 0, 0, 15);

                    // Animate to the desired camera position
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(47.127757, 8.579139))
                                    .zoom(10.0)
                                    .build()
                    ));
                }
            });
        });
    }

    private void testApi() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("khoa@gmail.com");
        loginRequest.setPassword("1234");

        Call<LoginResponse> call = apiService.loginUser(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    LoginResponse loginResponse = response.body();
                    Log.d("MainActivity", "Token: " + loginResponse.getToken());
                } else {
                    Log.d("MainActivity", "Login failed");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.d("MainActivity", "Error: " + t.getMessage());
            }
        });
    }

    private String getMapTilerKey() {
        try {
            return getPackageManager().getApplicationInfo(
                    getPackageName(),
                    PackageManager.GET_META_DATA).metaData.getString("com.maptiler.simplemap.mapTilerKey");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("MainActivity", "MapTiler key not found", e);
            return null; // Handle error appropriately
        } catch (NullPointerException e) {
            Log.e("MainActivity", "MetaData is null", e);
            return null; // Handle error appropriately
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}