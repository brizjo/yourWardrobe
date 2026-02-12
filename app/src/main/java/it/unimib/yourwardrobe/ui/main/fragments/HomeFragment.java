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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.adapter.OutfitAdapter;  // ⬅️ CAMBIATO IMPORT
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.model.Outfit;
import it.unimib.yourwardrobe.ui.main.components.CardWeather;
import it.unimib.yourwardrobe.ui.main.viewmodel.HomeViewModel;
import it.unimib.yourwardrobe.utils.ToastHelper;

public class HomeFragment extends Fragment {
    private static final String TAG = HomeFragment.class.getSimpleName();
    private HomeViewModel homeViewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
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

        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
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

        // Setup RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.rv_outfit);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 1. Create Data (esempio/demo)
        List<Outfit> data = new ArrayList<>();
        List<Garment> garments = new ArrayList<>();
        garments.add(new Garment("", "Sweater", "Top", "Sweater", "", "https://img.shopstyle-cdn.com/sim/6e/eb/6eeb25b938ff328eeda273f650b4dec3_best/drumohr-round-neck-long-sleeves-sweather.jpg", new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        garments.add(new Garment("", "Jacket", "Top", "Jacket", "", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQcT4YCub5KVdWqRCbVEKqWdXQI__zFsEe4Zg&s", new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        garments.add(new Garment("", "Pants", "Bottom", "Pants", "", "https://truewerk.com/cdn/shop/files/t1_werkpants_mens_olive_flat_lay_4825e693-f588-4813-bff0-1d4c46ce82ce.jpg?v=1759203265&width=1200", new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        garments.add(new Garment("", "Glasses", "Accessory", "Glasses", "", "https://www.blockbluelight.co.uk/cdn/shop/products/blockbluelight-blue-light-filter-computer-glasses-clear-lens-screentime-billie-computer-glasses-black-29752330322052.jpg?v=1651274298", new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        garments.add(new Garment("", "Boots", "Shoes", "Boots", "", "https://cdn.laredoute.com/cdn-cgi/image/width=500,height=500,fit=pad,dpr=1/products/5/7/f/57f72574fc41014ee6b9d79ed387afa7.jpg", new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        garments.add(new Garment("", "Earrings", "Accessory", "Earrings", "", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQdCfDtc-lffVIzg8lCPkKPa202ZiizYrcT5A&s", new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        data.add(new Outfit("My first outfit", "Casual", garments));

        // 2. Create click listener
        OutfitAdapter.OnItemClickListener listener = (v, outfit) -> {
            // TODO: Gestisci il click sull'outfit
            // Ad esempio naviga ai dettagli dell'outfit
            Toast.makeText(getContext(), "Clicked: " + outfit.getName(), Toast.LENGTH_SHORT).show();

            // Oppure naviga con Navigation Component:
            // Bundle bundle = new Bundle();
            // bundle.putSerializable("outfit", outfit);
            // Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_singleOutfitFragment, bundle);
        };

        // 3. Set Adapter con il nuovo costruttore
        OutfitAdapter adapter = new OutfitAdapter(data, R.layout.item_outfit_grid, listener);  // ⬅️ CORRETTO
        recyclerView.setAdapter(adapter);

        // Observe ViewModels
        this.homeViewModel.currentUser.observe(getViewLifecycleOwner(), result -> {
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

        this.homeViewModel.currentWeatherResult.observe(getViewLifecycleOwner(), result -> {
            switch (result.status) {
                case LOADING:
                    cardWeather.setVisibility(GONE);
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
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
    }

    private void checkPermissionAndGetLocation() {
        if (!isLocationEnabled()) {
            ToastHelper.show(getContext(), "Please enable location services", false);
            return;
        }

        boolean hasFineLocationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarseLocationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (hasFineLocationPermission && hasCoarseLocationPermission) {
            getCurrentLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
    }

    private void getCurrentLocation() {
        try {
            // 1. Try a high-accuracy single shot first
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(location -> {
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
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    homeViewModel.getCurrentWeather(location.getLatitude(), location.getLongitude());
                } else {
                    // 3. ULTIMATE FALLBACK: If both are null, the GPS is likely "cold".
                    // You should start a brief Location Callback here to force the hardware to wake up.
                    requestNewLocationData();
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "Security Exception", e);
        }
    }

    private void requestNewLocationData() {
        try {
            com.google.android.gms.location.LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).setMaxUpdates(1).build();

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