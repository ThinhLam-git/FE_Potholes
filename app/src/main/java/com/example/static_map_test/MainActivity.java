package com.example.static_map_test;
import android.os.Bundle;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private MapView mapView;

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

        // Thiết lập MapView
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15); // Mức zoom
        mapView.getController().setCenter(new GeoPoint(10.8700, 106.8038)); // VNU - HCMC area
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDetach();
        }
    }
}
