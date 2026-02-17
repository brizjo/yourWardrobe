package it.unimib.yourwardrobe.ui.main.fragments;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.UserPreferences;
import it.unimib.yourwardrobe.ui.main.viewmodel.ProfileViewModel;
import it.unimib.yourwardrobe.ui.shared.AuthViewModel;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    private ShapeableImageView ivAvatar;
    private View avatarOverlay;
    private TextView tvUsername;
    private TextView tvEmail;
    private MaterialButton btnLogout;
    private MaterialButton btnGenerateStats;
    private MaterialButton btnPreferences;
    private LinearLayout statsContainer;
    private TextView tvGarmentsCount;
    private TextView tvOutfitsCount;
    private TextView tvWeatherCombosCount;
    private View cardGarments, cardOutfits, cardWeatherCombos;

    private AuthViewModel authViewModel;
    private ProfileViewModel profileViewModel;
    // Launcher per la galleria
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    handleImageSelected(uri);
                }
            });
    // Launcher per i permessi (Android 13+)
    private final ActivityResultLauncher<String> requestGalleryPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchGallery();
                } else {
                    Snackbar.make(requireView(), "Permesso galleria necessario", Snackbar.LENGTH_SHORT).show();
                }
            });
    private boolean statsGenerated = false;
    private List<String> selectedStyles = new ArrayList<>();
    private List<String> selectedColors = new ArrayList<>();
    private String currentAvatarUrl = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        observeViewModels();
        setupListeners();

        profileViewModel.loadPreferences();
    }

    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.ivAvatar);
        avatarOverlay = view.findViewById(R.id.avatarOverlay);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnGenerateStats = view.findViewById(R.id.btnGenerateStats);
        btnPreferences = view.findViewById(R.id.btnPreferences);
        statsContainer = view.findViewById(R.id.statsContainer);

        tvGarmentsCount = view.findViewById(R.id.tvGarmentsCount);
        tvOutfitsCount = view.findViewById(R.id.tvOutfitsCount);

        cardGarments = view.findViewById(R.id.cardGarments);
        cardOutfits = view.findViewById(R.id.cardOutfits);
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> performLogout());
        btnGenerateStats.setOnClickListener(v -> generateStatistics());
        btnPreferences.setOnClickListener(v -> showPreferencesDialog());

        // Click sull'intero container per cambiare avatar
        View avatarContainer = getView().findViewById(R.id.avatarContainer);
        avatarContainer.setOnClickListener(v -> checkGalleryPermissionAndLaunch());
    }

    private void observeViewModels() {
        authViewModel.getAuthResult().observe(getViewLifecycleOwner(), result -> {
            switch (result.status) {
                case SUCCESS:
                    if (result.data != null) {
                        tvUsername.setText(result.data.getDisplayName());
                        tvEmail.setText(result.data.getEmail());

                        UserPreferences prefs = profileViewModel.getUserPreferences().getValue();
                        if (prefs == null || prefs.getAvatarUrl() == null || prefs.getAvatarUrl().isEmpty()) {
                            Glide.with(this)
                                    .load(result.data.getPhotoUrl())
                                    .error(R.drawable.profile_avatar_placeholder)
                                    .into(ivAvatar);
                        }
                    }
                    break;
            }
        });

        profileViewModel.getUserPreferences().observe(getViewLifecycleOwner(), prefs -> {
            if (prefs != null) {
                selectedStyles = new ArrayList<>(prefs.getFavoriteStyles());
                selectedColors = new ArrayList<>(prefs.getFavoriteColors());

                if (prefs.getAvatarUrl() != null && !prefs.getAvatarUrl().isEmpty()) {
                    currentAvatarUrl = prefs.getAvatarUrl(); // ← Salva l'URL attuale
                    Glide.with(this)
                            .load(prefs.getAvatarUrl())
                            .skipMemoryCache(true)
                            .error(R.drawable.profile_avatar_placeholder)
                            .into(ivAvatar);
                }
            }
        });

        profileViewModel.getAvatarUrl().observe(getViewLifecycleOwner(), avatarUrl -> {
            // Mostra toast SOLO se è un nuovo URL diverso da quello già caricato
            if (avatarUrl != null && !avatarUrl.isEmpty() && !avatarUrl.equals(currentAvatarUrl)) {
                currentAvatarUrl = avatarUrl;
                Glide.with(this)
                        .load(avatarUrl)
                        .skipMemoryCache(true)
                        .error(R.drawable.profile_avatar_placeholder)
                        .into(ivAvatar);
                Snackbar.make(requireView(), "Avatar aggiornato!", Snackbar.LENGTH_SHORT).show();
            }
        });

        // resto del codice...
    }

    // ========== GESTIONE AVATAR ==========

    private void checkGalleryPermissionAndLaunch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                launchGallery();
            } else {
                requestGalleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            launchGallery();
        }
    }

    private void launchGallery() {
        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void handleImageSelected(Uri uri) {
        // Mostra subito l'anteprima
        Glide.with(this)
                .load(uri)
                .into(ivAvatar);

        // Carica su Firebase Storage
        profileViewModel.uploadAvatar(uri);
    }

    // ========== STATISTICHE ==========

    private void generateStatistics() {
        if (statsGenerated) {
            if (statsContainer.getVisibility() == View.VISIBLE) {
                fadeOut(statsContainer);
            } else {
                fadeIn(statsContainer);
            }
            return;
        }

        statsGenerated = true;
        btnGenerateStats.setText("Nascondi Statistiche");

        statsContainer.setVisibility(View.VISIBLE);
        statsContainer.setAlpha(0f);
        statsContainer.animate().alpha(1f).setDuration(300).start();

        profileViewModel.getTotalGarments().observe(getViewLifecycleOwner(), count -> {
            if (count != null) animateCard(cardGarments, 0, tvGarmentsCount, count);
        });

        profileViewModel.getTotalOutfits().observe(getViewLifecycleOwner(), count -> {
            if (count != null) animateCard(cardOutfits, 150, tvOutfitsCount, count);
        });

    }

    private void animateCard(View card, long delay, TextView countTextView, int finalValue) {
        card.setAlpha(0f);
        card.setTranslationY(50f);

        card.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> animateNumber(countTextView, finalValue))
                .start();
    }

    private void animateNumber(TextView textView, int targetValue) {
        ValueAnimator animator = ValueAnimator.ofInt(0, targetValue);
        animator.setDuration(800);
        animator.addUpdateListener(animation ->
                textView.setText(String.valueOf(animation.getAnimatedValue())));
        animator.start();
    }

    private void fadeIn(View view) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        view.animate().alpha(1f).setDuration(300).start();
        btnGenerateStats.setText("Nascondi Statistiche");
    }

    private void fadeOut(View view) {
        view.animate().alpha(0f).setDuration(300)
                .withEndAction(() -> view.setVisibility(View.GONE)).start();
        btnGenerateStats.setText("Mostra Statistiche");
    }

    // ========== PREFERENZE ==========

    private void showPreferencesDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_preferences, null);

        ChipGroup chipGroupStyles = dialogView.findViewById(R.id.chipGroupStyles);
        ChipGroup chipGroupColors = dialogView.findViewById(R.id.chipGroupColors);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSavePreferences);

        List<String> availableStyles = Arrays.asList("Casual", "Elegante", "Sportivo", "Vintage", "Streetwear", "Formale");
        List<String> availableColors = Arrays.asList("Nero", "Bianco", "Blu", "Rosso", "Verde", "Giallo", "Grigio", "Marrone");

        List<String> tempStyles = new ArrayList<>(selectedStyles);
        List<String> tempColors = new ArrayList<>(selectedColors);

        for (String style : availableStyles) {
            Chip chip = createFilterChip(style, tempStyles.contains(style));
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!tempStyles.contains(style)) tempStyles.add(style);
                } else {
                    tempStyles.remove(style);
                }
            });
            chipGroupStyles.addView(chip);
        }

        for (String color : availableColors) {
            Chip chip = createFilterChip(color, tempColors.contains(color));
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!tempColors.contains(color)) tempColors.add(color);
                } else {
                    tempColors.remove(color);
                }
            });
            chipGroupColors.addView(chip);
        }

        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView);

        androidx.appcompat.app.AlertDialog alertDialog = dialog.create();

        btnSave.setOnClickListener(v -> {
            profileViewModel.savePreferences(tempStyles, tempColors);
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    private Chip createFilterChip(String text, boolean isChecked) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCheckable(true);
        chip.setChecked(isChecked);

        ColorStateList bgStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{-android.R.attr.state_checked}
                },
                new int[]{
                        ContextCompat.getColor(requireContext(), R.color.md_theme_primary),
                        ContextCompat.getColor(requireContext(), R.color.md_theme_surfaceVariant)
                }
        );

        ColorStateList textStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{-android.R.attr.state_checked}
                },
                new int[]{
                        ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary),
                        ContextCompat.getColor(requireContext(), R.color.md_theme_onSurfaceVariant)
                }
        );

        chip.setChipBackgroundColor(bgStateList);
        chip.setTextColor(textStateList);

        return chip;
    }

    private void performLogout() {
        authViewModel.signOut();
    }
}