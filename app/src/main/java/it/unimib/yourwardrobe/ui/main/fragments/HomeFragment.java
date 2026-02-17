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
import android.widget.LinearLayout;
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
import androidx.navigation.Navigation;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.main.components.CardOutfit;
import it.unimib.yourwardrobe.ui.main.components.CardWeather;
import it.unimib.yourwardrobe.ui.main.viewmodel.HomeViewModel;
import it.unimib.yourwardrobe.utils.ToastHelper;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName();

    private HomeViewModel homeViewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private CardOutfit dailyCardOutfit;

    private double lastLat = 0;
    private double lastLon = 0;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) getCurrentLocation();
                else Toast.makeText(requireContext(), "Permesso negato", Toast.LENGTH_SHORT).show();
            });

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        observeViewModel();
    }

    @Override
    public void onStart() {
        super.onStart();
        homeViewModel.getCurrentUser();
        checkPermissionAndGetLocation();
    }

    // -------------------------------------------------------------------------
    // Binding
    // -------------------------------------------------------------------------

    private void bindViews(View view) {
        dailyCardOutfit = view.findViewById(R.id.card_daily_outfit);

        view.findViewById(R.id.btn_regenerate_outfit)
                .setOnClickListener(v -> homeViewModel.regenerateOutfit());

        view.findViewById(R.id.fab_plan_outfit)
                .setOnClickListener(v -> showPlannerSheet());

        view.findViewById(R.id.btn_got_to_add_garment)
                .setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_addGarmentFragment));

    }

    // -------------------------------------------------------------------------
    // Observers
    // -------------------------------------------------------------------------

    private void observeViewModel() {
        View root = requireView();

        homeViewModel.currentUser.observe(getViewLifecycleOwner(), result -> {
            if (result.status == it.unimib.yourwardrobe.core.functional.Result.Status.SUCCESS
                    && result.data != null) {
                ((TextView) root.findViewById(R.id.tv_username))
                        .setText(result.data.getDisplayName());
            }
        });

        homeViewModel.currentWeatherResult.observe(getViewLifecycleOwner(), result -> {
            ProgressBar pb = root.findViewById(R.id.pb_weather);
            CardWeather card = root.findViewById(R.id.card_weather);
            switch (result.status) {
                case LOADING:
                    pb.setVisibility(VISIBLE);
                    card.setVisibility(GONE);
                    break;
                case SUCCESS:
                    pb.setVisibility(GONE);
                    card.setVisibility(VISIBLE);
                    card.setTemperature(result.data.getTemperature());
                    card.setCondition(result.data.getCondition());
                    card.setConditionIcon(result.data.getIconUrl());
                    card.setConditionBackground(result.data.getBackgroundResId());
                    break;
                case ERROR:
                    pb.setVisibility(GONE);
                    ToastHelper.show(getContext(), result.message, false);
                    break;
            }
        });

        homeViewModel.outfitOfTheDay.observe(getViewLifecycleOwner(), garments -> {
            LinearLayout emptyState = root.findViewById(R.id.layout_empty_state);
            if (garments != null && !garments.isEmpty()) {
                dailyCardOutfit.setGarments(garments);
                dailyCardOutfit.setVisibility(VISIBLE);
                emptyState.setVisibility(GONE);
            } else {
                dailyCardOutfit.setVisibility(GONE);
                emptyState.setVisibility(VISIBLE);
            }
        });

        homeViewModel.isGeneratingOutfit.observe(getViewLifecycleOwner(), isGenerating -> {
            root.findViewById(R.id.btn_regenerate_outfit)
                    .setVisibility(Boolean.TRUE.equals(isGenerating) ? GONE : VISIBLE);
            root.findViewById(R.id.pb_outfit)
                    .setVisibility(Boolean.TRUE.equals(isGenerating) ? VISIBLE : GONE);
        });
    }

    // -------------------------------------------------------------------------
    // Planner sheet
    // -------------------------------------------------------------------------

    private void showPlannerSheet() {
        if (getChildFragmentManager()
                .findFragmentByTag(PlannerDialogFragment.TAG) != null) return;

        homeViewModel.resetPlannedOutfit();
        homeViewModel.resetSaveOutfitResult();

        PlannerDialogFragment.newInstance(lastLat, lastLon)
                .show(getChildFragmentManager(), PlannerDialogFragment.TAG);
    }

    // -------------------------------------------------------------------------
    // Location
    // -------------------------------------------------------------------------

    private void checkPermissionAndGetLocation() {
        if (!isLocationEnabled()) {
            ToastHelper.show(getContext(), "Attiva i servizi di localizzazione", false);
            return;
        }
        boolean fine = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (fine && coarse) getCurrentLocation();
        else requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void getCurrentLocation() {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            lastLat = location.getLatitude();
                            lastLon = location.getLongitude();
                            homeViewModel.getCurrentWeather(lastLat, lastLon);
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
                    lastLat = location.getLatitude();
                    lastLon = location.getLongitude();
                    homeViewModel.getCurrentWeather(lastLat, lastLon);
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
            LocationRequest req = new LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 1000)
                    .setMaxUpdates(1)
                    .build();
            fusedLocationClient.requestLocationUpdates(req, new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult result) {
                    var loc = result.getLastLocation();
                    if (loc != null) {
                        lastLat = loc.getLatitude();
                        lastLon = loc.getLongitude();
                        homeViewModel.getCurrentWeather(lastLat, lastLon);
                    }
                }
            }, android.os.Looper.getMainLooper());
        } catch (SecurityException e) {
            Log.e(TAG, "Security Exception", e);
        }
    }

    private boolean isLocationEnabled() {
        android.location.LocationManager lm = (android.location.LocationManager)
                requireContext().getSystemService(android.content.Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
    }
}