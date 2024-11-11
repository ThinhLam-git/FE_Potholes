package com.example.authentication_uiux;

import android.util.Log;
import java.net.URL;
import retrofit2.Retrofit;

public class RetrofitUtils {

    public static void logRetrofitPort(Retrofit retrofit) {
        try {
            URL url = new URL(retrofit.baseUrl().toString());
            int port = url.getPort();
            if (port == -1) {
                port = url.getDefaultPort();
            }
            Log.d("RetrofitUtils", "Retrofit is using port: " + port);
        } catch (Exception e) {
            Log.e("RetrofitUtils", "Error extracting port from Retrofit base URL", e);
        }
    }
}