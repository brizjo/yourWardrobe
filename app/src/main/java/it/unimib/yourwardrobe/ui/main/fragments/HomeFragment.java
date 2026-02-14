package it.unimib.yourwardrobe.ui.main.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.adapter.ClothesAdapter;
import it.unimib.yourwardrobe.ui.main.components.CardWeather;
import it.unimib.yourwardrobe.ui.main.viewmodel.HomeViewModel;
import it.unimib.yourwardrobe.utils.ToastHelper;

@AndroidEntryPoint
public class HomeFragment extends Fragment {
    private static final String TAG = HomeFragment.class.getSimpleName();
    private HomeViewModel homeViewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private ClothesAdapter clothesAdapter;
    private RecyclerView recyclerView;

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProgressBar pbWeather = view.findViewById(R.id.pb_weather);
        TextView tvUsername = view.findViewById(R.id.tv_username);
        CardWeather cardWeather = view.findViewById(R.id.card_weather);

        // Setup RecyclerView per l'outfit del giorno - GRID 2 COLONNE
        recyclerView = view.findViewById(R.id.rv_outfit);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setNestedScrollingEnabled(false);

        // Usa il nuovo layout item_outfit_home
        clothesAdapter = new ClothesAdapter(new ArrayList<>(),
                R.layout.item_outfit_home,
                (v, garment) -> {
                    Toast.makeText(getContext(), "Clicked: " + garment.getName(), Toast.LENGTH_SHORT).show();
                });
        recyclerView.setAdapter(clothesAdapter);

        // Observe User
        homeViewModel.currentUser.observe(getViewLifecycleOwner(), result -> {
            switch (result.status) {
                case LOADING:
                    break;
                case SUCCESS:
                    tvUsername.setText(result.data.getDisplayName());
                    break;
                case ERROR:
                    ToastHelper.show(getContext(), result.message, false);
                    break;
            }
        });

        // Observe Weather
        homeViewModel.currentWeatherResult.observe(getViewLifecycleOwner(), result -> {
            switch (result.status) {
                case LOADING:
                    cardWeather.setVisibility(GONE);
                    pbWeather.setVisibility(VISIBLE);
                    break;
                case SUCCESS:
                    pbWeather.setVisibility(GONE);
                    cardWeather.setVisibility(VISIBLE);
                    cardWeather.setTemperature(result.data.getTemperature());
                    cardWeather.setCondition(result.data.getCondition());
                    cardWeather.setConditionIcon(result.data.getIconUrl());
                    cardWeather.setConditionBackground(result.data.getBackgroundResId());
                    break;
                case ERROR:
                    pbWeather.setVisibility(GONE);
                    ToastHelper.show(getContext(), result.message, false);
                    break;
            }
        });

        // Observe Outfit del giorno
        homeViewModel.outfitOfTheDay.observe(getViewLifecycleOwner(), garments -> {
            if (garments != null && !garments.isEmpty()) {
                Log.d(TAG, "Outfit del giorno ricevuto con " + garments.size() + " capi");
                recyclerView.setVisibility(View.VISIBLE);
                clothesAdapter.updateGarments(garments);
            } else {
                Log.d(TAG, "Nessun outfit disponibile");
                recyclerView.setVisibility(View.GONE);
                clothesAdapter.updateGarments(new ArrayList<>());
            }
        });

        // Observe loading state
        homeViewModel.isGeneratingOutfit.observe(getViewLifecycleOwner(), isGenerating -> {
            if (Boolean.TRUE.equals(isGenerating)) {
                Log.d(TAG, "Generazione outfit in corso...");
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        checkPermissionAndGetLocation();
        homeViewModel.getCurrentUser();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPermissionAndGetLocation();
    }

    private boolean isLocationEnabled() {
        android.location.LocationManager locationManager = (android.location.LocationManager)
                requireContext().getSystemService(android.content.Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
    }

    private void checkPermissionAndGetLocation() {
        if (!isLocationEnabled()) {
            ToastHelper.show(getContext(), "Please enable location services", false);
            return;
        }

        boolean hasFineLocationPermission = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarseLocationPermission = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (hasFineLocationPermission && hasCoarseLocationPermission) {
            getCurrentLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void getCurrentLocation() {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            homeViewModel.getCurrentWeather(location.getLatitude(), location.getLongitude());
                        } else {
                            fetchLastLocationFallback();
                        }
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "Security Exception", e);
        }
    }

    private void fetchLastLocationFallback() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    homeViewModel.getCurrentWeather(location.getLatitude(), location.getLongitude());
                } else {
                    requestNewLocationData();
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "Security Exception", e);
        }
    }

    private void requestNewLocationData() {
        try {
            LocationRequest locationRequest = new LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 1000)
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
        } catch (SecurityException e) {
            Log.e(TAG, "Security Exception", e);
        }
    }
}