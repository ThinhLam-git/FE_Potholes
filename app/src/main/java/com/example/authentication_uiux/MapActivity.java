package com.example.authentication_uiux;

/*import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView mapWebView;
//test
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapWebView = findViewById(R.id.mapWebView);

        // Enable JavaScript for interactive maps
        WebSettings webSettings = mapWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Load the Maptive map URL (online) or HTML file (offline)
        mapWebView.setWebViewClient(new WebViewClient());

        // For Online Use (public link):
        mapWebView.loadUrl("https://fortress.maptive.com/ver4/05c9e2b0423a7988ad343a2413434d38/682160"); // Replace with your actual Maptive URL

        // For Offline Use (local HTML file):
        // mapWebView.loadUrl("file:///android_asset/your_map_file.html"); // Make sure the file is in /assets folder
    }
}*/

import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        // Initialize the SupportMapFragment and request the map asynchronously
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MapActivity", "Map is ready!");
        mMap = googleMap;

        // Set a custom camera position to focus on a specific location
        LatLng location = new LatLng(10.870894, 106.803054); // Replace with your desired coordinates
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));

        try {
            // Add a Ground Overlay if you want to use a custom map image overlay
            LatLng southwest = new LatLng(-34.1, 150.9); // Bottom-left corner of overlay
            LatLng northeast = new LatLng(-33.9, 151.1); // Top-right corner of overlay
            LatLngBounds overlayBounds = new LatLngBounds(southwest, northeast);

            GroundOverlayOptions overlayOptions = new GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromResource(R.drawable.test))
                    .positionFromBounds(overlayBounds);
            mMap.addGroundOverlay(overlayOptions);
        } catch (Exception e) {
            Log.e("MapActivity", "Error adding overlay", e);
        }
    }
}
