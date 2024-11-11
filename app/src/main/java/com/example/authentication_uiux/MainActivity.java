package com.example.authentication_uiux;

import android.os.Bundle;
import android.util.Log;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private UserApi apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        Retrofit retrofit = RetrofitClient.getClient("http://192.168.124.155:3000");
        apiService = retrofit.create(UserApi.class);

        // Test the API
        testApi();
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
}
