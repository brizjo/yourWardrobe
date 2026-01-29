package it.unimib.yourwardrobe.ui.main.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.core.di.ServiceLocator;
import it.unimib.yourwardrobe.repository.UserRepository;
import it.unimib.yourwardrobe.ui.main.components.CardWeather;
import it.unimib.yourwardrobe.ui.main.viewmodel.HomeViewModel;
import it.unimib.yourwardrobe.ui.main.viewmodel.HomeViewModelFactory;
import it.unimib.yourwardrobe.utils.ToastHelper;


public class HomeFragment extends Fragment {
    private static final String TAG = HomeFragment.class.getSimpleName();
    private HomeViewModel homeViewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationClient = LocationServices
                .getFusedLocationProviderClient(requireActivity());

        var weatherRepository = ServiceLocator
                .getInstance()
                .getWeatherRepository(
                        requireActivity().getApplication()
                );


        this.homeViewModel =
                new ViewModelProvider(requireActivity(), new HomeViewModelFactory(weatherRepository, new UserRepository()))
                        .get(HomeViewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);

    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvUsername = view.findViewById(R.id.tv_username);
        CardWeather cardWeather = view.findViewById(R.id.card_weather);

        this.homeViewModel.
                currentUser
                .observe(getViewLifecycleOwner(), result -> {
                    switch (result.status) {
                        case LOADING:
                            // TODO: placeholder
                            break;
                        case SUCCESS:
                            tvUsername.setText(result.data.getDisplayName());
                            break;
                        case ERROR:
                            ToastHelper.show(getContext(), result.message, false);
                            break;
                    }
                });

        this.homeViewModel
                .currentWeatherResult
                .observe(getViewLifecycleOwner(), result -> {
                    switch (result.status) {
                        case LOADING:
                            // TODO: placeholder
                            break;
                        case SUCCESS:
                            cardWeather.setTemperature(result.data.getTemperature());
                            cardWeather.setCondition(result.data.getCondition());
                            cardWeather.setConditionIcon(result.data.getIconUrl());
                            cardWeather.setConditionBackground(result.data.getBackgroundResId());
                            break;
                        case ERROR:
                            ToastHelper.show(getContext(), result.message, false);
                            break;
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        checkPermissionAndGetLocation();
        this.homeViewModel.getCurrentUser();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPermissionAndGetLocation();
    }

    private boolean isLocationEnabled() {
        android.location.LocationManager locationManager = (android.location.LocationManager) requireContext().getSystemService(android.content.Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
    }


    private void checkPermissionAndGetLocation() {
        if (!isLocationEnabled()) {
            ToastHelper.show(getContext(), "Please enable location services", false);
            return;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }

        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void getCurrentLocation() {
        try {
            // 1. Try a high-accuracy single shot first
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            homeViewModel.getCurrentWeather(location.getLatitude(), location.getLongitude());
                        } else {
                            // 2. If single shot fails, fallback to last location
                            fetchLastLocationFallback();
                        }
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "Security Exception", e);
        }
    }

    private void fetchLastLocationFallback() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                homeViewModel.getCurrentWeather(location.getLatitude(), location.getLongitude());
            } else {
                // 3. ULTIMATE FALLBACK: If both are null, the GPS is likely "cold".
                // You should start a brief Location Callback here to force the hardware to wake up.
                requestNewLocationData();
            }
        });
    }

    private void requestNewLocationData() {
        com.google.android.gms.location.LocationRequest locationRequest =
                new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                        .setMaxUpdates(1)
                        .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                var location = locationResult.getLastLocation();
                if (location != null) {
                    homeViewModel.getCurrentWeather(location.getLatitude(), location.getLongitude());
                }
            }
        }, android.os.Looper.getMainLooper());
    }


}
