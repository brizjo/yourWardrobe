package it.unimib.yourwardrobe.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashSet;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.common.NetworkBannerHelper;
import it.unimib.yourwardrobe.ui.common.viewmodel.AuthViewModel;
import it.unimib.yourwardrobe.ui.welcome.WelcomeActivity;
import it.unimib.yourwardrobe.utils.Resource;
import it.unimib.yourwardrobe.workers.WeatherNotificationScheduler;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });
    private AuthViewModel authViewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean granted = Boolean.TRUE.equals(permissions.get(Manifest.permission.ACCESS_FINE_LOCATION)) ||
                        Boolean.TRUE.equals(permissions.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                if (granted) {
                    scheduleWeatherNotification();
                } else {
                    // Permesso negato, usa coordinate di default (Roma)
                    WeatherNotificationScheduler.schedule(this, 41.9028, 12.4964);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // WeatherNotificationScheduler.testNow(this, 45.4654, 9.1859);
        // Test notifiche (per testare rimuovere il commento)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.top_bar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView banner = findViewById(R.id.tv_no_connection);
        NetworkBannerHelper.observe(this, banner);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocationAndSchedule();

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setUpObservers();
        setupNavigation();
        requestNotificationPermission();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void requestLocationAndSchedule() {
        boolean fineGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (fineGranted || coarseGranted) {
            scheduleWeatherNotification();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void scheduleWeatherNotification() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                WeatherNotificationScheduler.schedule(this, location.getLatitude(), location.getLongitude());
            } else {
                // Posizione non disponibile, usa default (Roma)
                WeatherNotificationScheduler.schedule(this, 41.9028, 12.4964);
            }
        }).addOnFailureListener(e -> {
            // Errore, usa default (Roma)
            WeatherNotificationScheduler.schedule(this, 41.9028, 12.4964);
        });
    }

    public void setUpObservers() {
        authViewModel.getAuthResult().observe(this, result -> {
            if (result.status == Resource.Status.SUCCESS && result.data == null) {
                navigateToWelcomeActivity();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            return navHostFragment.getNavController().navigateUp() || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
            MaterialToolbar toolbar = findViewById(R.id.top_bar);

            NavigationUI.setupWithNavController(bottomNav, navController);

            Set<Integer> topLevelDestinations = new HashSet<>();
            topLevelDestinations.add(R.id.homeFragment);
            topLevelDestinations.add(R.id.wardrobe_nav_graph);
            topLevelDestinations.add(R.id.wardrobeFragment);
            topLevelDestinations.add(R.id.personalStylistFragment);
            topLevelDestinations.add(R.id.profileFragment);

            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations).build();
            setSupportActionBar(toolbar);
            NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                toolbar.setTitle(null);

                if (destination.getId() == R.id.addGarmentFragment ||
                        destination.getId() == R.id.garmentFragment ||
                        destination.getId() == R.id.createOutfitFragment ||
                        destination.getId() == R.id.singleOutfitFragment) {
                    bottomNav.setVisibility(android.view.View.GONE);
                } else {
                    bottomNav.setVisibility(android.view.View.VISIBLE);
                }

                boolean isTopLevelDestination = topLevelDestinations.contains(destination.getId());
                if (!isTopLevelDestination) {
                    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                }
            });

            bottomNav.setOnItemReselectedListener(item -> {
                NavOptions navOptions = new NavOptions.Builder()
                        .setPopUpTo(item.getItemId(), true)
                        .setLaunchSingleTop(true)
                        .build();
                navController.navigate(item.getItemId(), null, navOptions);
            });
        }
    }

    private void navigateToWelcomeActivity() {
        startActivity(new Intent(this, WelcomeActivity.class));
        finish();
    }


}