package it.unimib.yourwardrobe.ui.main.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.main.adapter.ClothesAdapter;
import it.unimib.yourwardrobe.ui.main.viewmodel.PersonalStylistViewModel;

@AndroidEntryPoint
public class PersonalStylistFragment extends Fragment {

    private PersonalStylistViewModel viewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getCurrentLocation();
                }
            });
    private AutoCompleteTextView filterColor, filterSeason, filterStyle;
    private MaterialButton btnGenerate, btnSave, btnRegenerate;
    private MaterialCardView cardGeneratedOutfit;
    private RecyclerView rvGeneratedGarments;
    private TextInputEditText etOutfitName;
    private TextView tvTemperature, tvSuggestedSeason;
    private ImageView ivWeatherIcon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal_stylist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PersonalStylistViewModel.class);

        initViews(view);
        setupListeners();
        observeViewModel();
        checkPermissionAndGetLocation();
    }

    private void initViews(View view) {
        filterColor = view.findViewById(R.id.filter_color);
        filterStyle = view.findViewById(R.id.filter_style);
        filterSeason = view.findViewById(R.id.filter_season);
        btnGenerate = view.findViewById(R.id.btn_generate_outfit);
        cardGeneratedOutfit = view.findViewById(R.id.card_generated_outfit);
        rvGeneratedGarments = view.findViewById(R.id.rv_generated_garments);
        etOutfitName = view.findViewById(R.id.et_outfit_name);
        btnSave = view.findViewById(R.id.btn_save_outfit);
        btnRegenerate = view.findViewById(R.id.btn_regenerate);
        tvTemperature = view.findViewById(R.id.tv_temperature);
        tvSuggestedSeason = view.findViewById(R.id.tv_suggested_season);
        ivWeatherIcon = view.findViewById(R.id.iv_weather_icon);

        rvGeneratedGarments.setLayoutManager(new GridLayoutManager(getContext(), 2));
    }

    private void setupListeners() {
        btnGenerate.setOnClickListener(v -> {
            String season = filterSeason.getText().toString();
            String color = filterColor.getText().toString();
            String style = filterStyle.getText().toString();
            viewModel.generateOutfit(season, color, style);
        });

        btnRegenerate.setOnClickListener(v -> {
            String season = filterSeason.getText().toString();
            String color = filterColor.getText().toString();
            String style = filterStyle.getText().toString();
            viewModel.generateOutfit(season, color, style);
        });

        btnSave.setOnClickListener(v -> {
            String name = etOutfitName.getText() != null ? etOutfitName.getText().toString() : "";
            viewModel.saveGeneratedOutfit(name);
        });
    }

    private void observeViewModel() {
        viewModel.getAvailableColors().observe(getViewLifecycleOwner(), colors -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, colors);
            filterColor.setAdapter(adapter);
        });

        viewModel.getAvailableStyles().observe(getViewLifecycleOwner(), styles -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, styles);
            filterStyle.setAdapter(adapter);
        });

        viewModel.getAvailableSeasons().observe(getViewLifecycleOwner(), seasons -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, seasons);
            filterSeason.setAdapter(adapter);
        });

        viewModel.getCurrentWeather().observe(getViewLifecycleOwner(), weather -> {
            if (weather != null) {
                tvTemperature.setText(weather.getTemperature());
                Glide.with(this).load(weather.getIconUrl()).into(ivWeatherIcon);
            }
        });

        viewModel.getSuggestedSeason().observe(getViewLifecycleOwner(), season -> {
            if (season != null) {
                tvSuggestedSeason.setText("Consigliata: " + season);
                filterSeason.setText(season, false);
            }
        });

        viewModel.getGeneratedOutfitGarments().observe(getViewLifecycleOwner(), garments -> {
            if (garments != null && !garments.isEmpty()) {
                cardGeneratedOutfit.setVisibility(View.VISIBLE);
                ClothesAdapter adapter = new ClothesAdapter(garments, R.layout.item_clothes_grid, (v, garment) -> {
                });
                rvGeneratedGarments.setAdapter(adapter);
            }
        });

        viewModel.getOutfitSaved().observe(getViewLifecycleOwner(), saved -> {
            if (saved != null && saved) {
                Snackbar.make(requireView(), "Outfit salvato!", Snackbar.LENGTH_LONG).show();
                cardGeneratedOutfit.setVisibility(View.GONE);
                etOutfitName.setText("");
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(requireView(), error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void checkPermissionAndGetLocation() {
        boolean hasFineLocation = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (hasFineLocation) {
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
                            viewModel.fetchWeather(location.getLatitude(), location.getLongitude());
                        }
                    });
        } catch (SecurityException e) {
            android.util.Log.e("PersonalStylist", "Errore permessi", e);
        }
    }
}