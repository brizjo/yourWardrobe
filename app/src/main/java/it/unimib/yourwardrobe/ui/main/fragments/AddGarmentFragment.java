package it.unimib.yourwardrobe.ui.main.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.main.viewmodel.AddGarmentViewModel;
import it.unimib.yourwardrobe.utils.ImageValidationState;

@AndroidEntryPoint
public class AddGarmentFragment extends Fragment {

    private ImageView addGarmentImageView;
    private AutoCompleteTextView categoryTextView;
    private ChipGroup colorChipGroup;
    private ChipGroup styleChipGroup;
    private ChipGroup fabricChipGroup;
    private ChipGroup seasonChipGroup;
    private ChipGroup subCategoryChipGroup;
    private AddGarmentViewModel viewModel;

    private final ActivityResultLauncher<Void> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    addGarmentImageView.setImageBitmap(bitmap);
                    addGarmentImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    viewModel.setGarmentImage(bitmap);
                }
            });

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) launchCamera();
                else showSnackbar("Permesso fotocamera necessario per scattare una foto");
            });

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    Glide.with(requireContext()).load(uri).centerCrop().into(addGarmentImageView);
                    try {
                        ImageDecoder.Source source = ImageDecoder.createSource(requireActivity().getContentResolver(), uri);
                        Bitmap bitmap = ImageDecoder.decodeBitmap(source);
                        viewModel.setGarmentImage(bitmap);
                    } catch (IOException e) {
                        showSnackbar("Errore nel caricare l'immagine");
                    }
                } else {
                    Log.d("PhotoPicker", "Nessuna immagine selezionata");
                }
            });

    private final ActivityResultLauncher<String> requestGalleryPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) launchGallery();
                else showSnackbar("Permesso galleria necessario per scegliere una foto");
            });

    private TextInputEditText garmentNameEditText;
    private Button addGarmentButton;
    private ProgressBar addGarmentProgressBar;

    public AddGarmentFragment() {}

    public static AddGarmentFragment newInstance() { return new AddGarmentFragment(); }

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_garment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddGarmentViewModel.class);
        initViews(view);
        applyPopstar((MaterialButton) addGarmentButton);
        addGarmentImageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_add_photo));
        addGarmentImageView.setOnClickListener(v -> showImagePickerDialog());
        garmentNameEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { viewModel.setGarmentName(s.toString()); }
        });
        categoryTextView.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedItem = (String) parent.getItemAtPosition(position);
            viewModel.setSelectedCategory(selectedItem);
            subCategoryChipGroup.setEnabled(true);
        });
        setupAddChip(colorChipGroup, "color");
        setupAddChip(styleChipGroup, "style");
        setupAddChip(fabricChipGroup, "fabric");
        setupSeasonChip();
        setupSubCategoryChip();
        addGarmentButton.setOnClickListener(v -> viewModel.saveGarment());
        observeViewModel(view);
    }

    private void initViews(View view) {
        addGarmentImageView = view.findViewById(R.id.addGarmentImage);
        categoryTextView = view.findViewById(R.id.category_text_view);
        colorChipGroup = view.findViewById(R.id.chip_group_color);
        styleChipGroup = view.findViewById(R.id.chip_group_style);
        fabricChipGroup = view.findViewById(R.id.chip_group_fabric);
        seasonChipGroup = view.findViewById(R.id.chip_group_season);
        subCategoryChipGroup = view.findViewById(R.id.chip_group_subcategory);
        garmentNameEditText = view.findViewById(R.id.garmentName);
        addGarmentButton = view.findViewById(R.id.add_garment_button);
        addGarmentProgressBar = view.findViewById(R.id.add_garment_progress_bar);
    }

    // -------------------------------------------------------------------------
    // Font helpers
    // -------------------------------------------------------------------------

    private void applyPopstar(MaterialButton button) {
        android.graphics.Typeface tf = androidx.core.content.res.ResourcesCompat
                .getFont(requireContext(), R.font.popstar);
        if (tf != null) button.setTypeface(tf);
    }

    private void applyChango(Chip chip) {
        android.graphics.Typeface tf = androidx.core.content.res.ResourcesCompat
                .getFont(requireContext(), R.font.chango);
        if (tf != null) chip.setTypeface(tf);
    }

    private void applyPopstarToTextView(TextView tv) {
        android.graphics.Typeface tf = androidx.core.content.res.ResourcesCompat
                .getFont(requireContext(), R.font.popstar);
        if (tf != null) tv.setTypeface(tf);
    }

    private void styleChipBlack(Chip chip) {
        chip.setChipBackgroundColor(ColorStateList.valueOf(Color.BLACK));
        chip.setTextColor(Color.WHITE);
        chip.setCloseIconTint(ColorStateList.valueOf(Color.WHITE));
    }

    private ColorStateList blackChipBgStateList() {
        return new ColorStateList(
                new int[][]{{android.R.attr.state_checked}, {-android.R.attr.state_checked}},
                new int[]{Color.BLACK, Color.LTGRAY});
    }

    private ColorStateList blackChipTextStateList() {
        return new ColorStateList(
                new int[][]{{android.R.attr.state_checked}, {-android.R.attr.state_checked}},
                new int[]{Color.WHITE, Color.BLACK});
    }

    // -------------------------------------------------------------------------
    // Snackbar helpers
    // -------------------------------------------------------------------------

    private void styleSnackbar(Snackbar snackbar) {
        View snackView = snackbar.getView();
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(Color.parseColor("#E65100"));
        bg.setCornerRadius(50f * getResources().getDisplayMetrics().density);
        snackView.setBackground(bg);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) snackView.getLayoutParams();
        int margin = (int) (16 * getResources().getDisplayMetrics().density);
        params.setMargins(margin, margin, margin, margin);
        snackView.setLayoutParams(params);
        TextView tv = snackView.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        tv.setMaxLines(3);
        android.graphics.Typeface tf = androidx.core.content.res.ResourcesCompat
                .getFont(requireContext(), R.font.popstar);
        if (tf != null) tv.setTypeface(tf);
    }

    private void showSnackbar(String message) {
        if (!isAdded()) return;
        Snackbar snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG);
        styleSnackbar(snackbar);
        snackbar.show();
    }

    private void showSnackbarOnActivity(String message) {
        if (!isAdded()) return;
        View activityView = requireActivity().findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(activityView, message, Snackbar.LENGTH_LONG);
        styleSnackbar(snackbar);
        snackbar.show();
    }

    // -------------------------------------------------------------------------
    // Observers
    // -------------------------------------------------------------------------

    private void observeViewModel(View view) {
        viewModel.getImageValidationState().observe(getViewLifecycleOwner(), state -> {
            if (state == ImageValidationState.INVALID_CONFIRMATION_NEEDED) {
                showInvalidGarmentConfirmationDialog();
            } else if (state == ImageValidationState.UNCHECKED) {
                addGarmentImageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_add_photo));
                addGarmentImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
        });

        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
            categoryTextView.setAdapter(adapter);
        });

        viewModel.getSelectedColors().observe(getViewLifecycleOwner(), colors ->
                updateMainChipGroup(colorChipGroup, colors, selected -> viewModel.updateSelectedColors(selected)));

        viewModel.getSelectedStyles().observe(getViewLifecycleOwner(), styles ->
                updateMainChipGroup(styleChipGroup, styles, selected -> viewModel.updateSelectedStyles(selected)));

        viewModel.getSelectedFabrics().observe(getViewLifecycleOwner(), fabrics ->
                updateMainChipGroup(fabricChipGroup, fabrics, selected -> viewModel.updateSelectedFabrics(selected)));

        viewModel.getSelectedSeason().observe(getViewLifecycleOwner(), this::updateSeasonChip);
        viewModel.getSelectedSubCategory().observe(getViewLifecycleOwner(), this::updateSubCategoryChip);

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                addGarmentButton.setVisibility(View.GONE);
                addGarmentProgressBar.setVisibility(View.VISIBLE);
            } else {
                addGarmentButton.setVisibility(View.VISIBLE);
                addGarmentProgressBar.setVisibility(View.GONE);
            }
        });

        viewModel.getGarmentAddedSuccessfully().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                showSnackbar("Capo aggiunto con successo!");
                Navigation.findNavController(view).popBackStack();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) showSnackbar(error);
        });

        viewModel.isButtonEnabled().observe(getViewLifecycleOwner(), isEnabled ->
                addGarmentButton.setEnabled(isEnabled));

        viewModel.getOfflineSaveScheduled().observe(getViewLifecycleOwner(), offline -> {
            if (offline != null && offline) {
                addGarmentButton.setEnabled(false);
                addGarmentProgressBar.setVisibility(View.GONE);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (isAdded()) {
                        Navigation.findNavController(requireView()).popBackStack();
                        showSnackbarOnActivity("Nessuna connessione. Il capo verrà salvato quando tornerà internet.");
                    }
                }, 0);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Riconoscimento immagine
    // -------------------------------------------------------------------------

    private void showInvalidGarmentConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Capo non riconosciuto")
                .setMessage("Sembra che l'immagine selezionata non sia un capo d'abbigliamento. Vuoi continuare comunque?")
                .setPositiveButton("Continua", (dialog, which) -> viewModel.forceImageAsValid())
                .setNegativeButton("Annulla", (dialog, which) -> viewModel.resetImageSelection())
                .setCancelable(false)
                .show();
    }

    // -------------------------------------------------------------------------
    // Season chip
    // -------------------------------------------------------------------------

    private void setupSeasonChip() {
        seasonChipGroup.removeAllViews();
        Chip addChip = new Chip(requireContext());
        addChip.setText("+");
        applyChango(addChip);
        styleChipBlack(addChip);
        addChip.setOnClickListener(v -> showSeasonSelectionDialog());
        seasonChipGroup.addView(addChip);
    }

    private void updateSeasonChip(String season) {
        seasonChipGroup.removeAllViews();
        if (season == null) {
            Chip addChip = new Chip(requireContext());
            addChip.setText("+");
            applyChango(addChip);
            styleChipBlack(addChip);
            addChip.setOnClickListener(v -> showSeasonSelectionDialog());
            seasonChipGroup.addView(addChip);
        } else {
            Chip chip = new Chip(requireContext());
            chip.setText(season);
            applyChango(chip);
            styleChipBlack(chip);
            chip.setCloseIconVisible(true);
            chip.setOnClickListener(v -> showSeasonSelectionDialog());
            chip.setOnCloseIconClickListener(v -> viewModel.setSelectedSeason(null));
            seasonChipGroup.addView(chip);
        }
    }

    private void showSeasonSelectionDialog() {
        List<String> allSeasons = viewModel.getAllSeasons().getValue();
        if (allSeasons == null) return;
        String[] seasonsArray = allSeasons.toArray(new String[0]);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Seleziona Stagione")
                .setItems(seasonsArray, (dialog, which) -> { viewModel.setSelectedSeason(seasonsArray[which]); dialog.dismiss(); })
                .setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // -------------------------------------------------------------------------
    // SubCategory chip
    // -------------------------------------------------------------------------

    private void setupSubCategoryChip() {
        subCategoryChipGroup.removeAllViews();
        Chip addChip = new Chip(requireContext());
        addChip.setText("+");
        applyChango(addChip);
        styleChipBlack(addChip);
        subCategoryChipGroup.setEnabled(false);
        addChip.setOnClickListener(v -> {
            if (viewModel.getSelectedCategory().getValue() == null) {
                showSnackbar("Seleziona prima una categoria");
                return;
            }
            showSubCategorySelectionDialog();
        });
        subCategoryChipGroup.addView(addChip);
    }

    private void updateSubCategoryChip(String subCategory) {
        subCategoryChipGroup.removeAllViews();
        if (subCategory == null) {
            Chip addChip = new Chip(requireContext());
            addChip.setText("+");
            applyChango(addChip);
            styleChipBlack(addChip);
            addChip.setOnClickListener(v -> {
                if (viewModel.getSelectedCategory().getValue() == null) {
                    showSnackbar("Seleziona prima una categoria");
                    return;
                }
                showSubCategorySelectionDialog();
            });
            subCategoryChipGroup.addView(addChip);
        } else {
            Chip chip = new Chip(requireContext());
            chip.setText(subCategory);
            applyChango(chip);
            styleChipBlack(chip);
            chip.setCloseIconVisible(true);
            chip.setOnClickListener(v -> showSubCategorySelectionDialog());
            chip.setOnCloseIconClickListener(v -> viewModel.setSelectedSubCategory(null));
            subCategoryChipGroup.addView(chip);
        }
    }

    private void showSubCategorySelectionDialog() {
        List<String> subCategories = viewModel.getAvailableSubCategories().getValue();
        if (subCategories == null || subCategories.isEmpty()) {
            showSnackbar("Nessuna sottocategoria disponibile per la categoria selezionata.");
            return;
        }
        String[] subCategoriesArray = subCategories.toArray(new String[0]);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Seleziona Sottocategoria")
                .setItems(subCategoriesArray, (dialog, which) -> viewModel.setSelectedSubCategory(subCategoriesArray[which]))
                .setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // -------------------------------------------------------------------------
    // Colori / Stili / Tessuti chip
    // -------------------------------------------------------------------------

    private void setupAddChip(ChipGroup chipGroup, final String type) {
        Chip addChip = new Chip(requireContext());
        addChip.setText(R.string.plus);
        applyChango(addChip);
        styleChipBlack(addChip);
        addChip.setOnClickListener(v -> {
            List<String> allOptions = null;
            List<String> currentSelection = null;
            String dialogTitle = "";
            if ("color".equals(type)) { allOptions = viewModel.getAllColors().getValue(); currentSelection = viewModel.getSelectedColors().getValue(); dialogTitle = "Seleziona Colori"; }
            else if ("style".equals(type)) { allOptions = viewModel.getAllStyles().getValue(); currentSelection = viewModel.getSelectedStyles().getValue(); dialogTitle = "Seleziona Stili"; }
            else if ("fabric".equals(type)) { allOptions = viewModel.getAllFabrics().getValue(); currentSelection = viewModel.getSelectedFabrics().getValue(); dialogTitle = "Seleziona Tessuti"; }
            if (allOptions != null && currentSelection != null) {
                showChipSelectionDialog(dialogTitle, allOptions, currentSelection, newSelection -> {
                    if ("color".equals(type)) viewModel.updateSelectedColors(newSelection);
                    else if ("style".equals(type)) viewModel.updateSelectedStyles(newSelection);
                    else viewModel.updateSelectedFabrics(newSelection);
                });
            }
        });
        chipGroup.addView(addChip, 0);
    }

    private void showChipSelectionDialog(String title, List<String> allOptions,
                                         List<String> currentSelection,
                                         OnSelectionConfirmedListener listener) {
        View dialogView = requireActivity().getLayoutInflater().inflate(R.layout.chip_selector, null);
        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        ChipGroup dialogChipGroup = dialogView.findViewById(R.id.dialog_chip_group);
        Button okButton = dialogView.findViewById(R.id.dialog_ok_button);
        dialogTitle.setText(title);
        applyPopstarToTextView(dialogTitle);
        for (String option : allOptions) {
            Chip chip = new Chip(requireContext());
            chip.setText(option);
            chip.setCheckable(true);
            applyChango(chip);
            chip.setChipBackgroundColor(blackChipBgStateList());
            chip.setTextColor(blackChipTextStateList());
            if (currentSelection.contains(option)) chip.setChecked(true);
            dialogChipGroup.addView(chip);
        }
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(dialogView).create();
        okButton.setEnabled(!dialogChipGroup.getCheckedChipIds().isEmpty());
        dialogChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> okButton.setEnabled(!checkedIds.isEmpty()));
        okButton.setOnClickListener(v -> {
            List<String> newSelection = new ArrayList<>();
            for (int id : dialogChipGroup.getCheckedChipIds()) {
                Chip selectedChip = dialogChipGroup.findViewById(id);
                newSelection.add(selectedChip.getText().toString());
            }
            listener.onConfirmed(newSelection);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void updateMainChipGroup(ChipGroup chipGroup, List<String> selectedItems,
                                     OnSelectionConfirmedListener listener) {
        for (int i = chipGroup.getChildCount() - 1; i >= 0; i--) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip && !((Chip) child).getText().toString().equals("+")) {
                chipGroup.removeView(child);
            }
        }
        for (String item : selectedItems) {
            Chip chip = new Chip(requireContext());
            chip.setText(item);
            applyChango(chip);
            styleChipBlack(chip);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                List<String> current = new ArrayList<>(selectedItems);
                current.remove(chip.getText().toString());
                listener.onConfirmed(current);
            });
            chipGroup.addView(chip);
        }
    }

    // -------------------------------------------------------------------------
    // Image picker
    // -------------------------------------------------------------------------

    private void showImagePickerDialog() {
        String[] options = getResources().getStringArray(R.array.image_picker_options);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Scegli immagine")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) checkCameraPermissionAndLaunch();
                    else if (which == 1) checkGalleryPermissionAndLaunch();
                })
                .show();
    }

    private void checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) launchCamera();
        else requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void checkGalleryPermissionAndLaunch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) launchGallery();
            else requestGalleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            launchGallery();
        }
    }

    private void launchCamera() { takePictureLauncher.launch(null); }

    private void launchGallery() {
        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    interface OnSelectionConfirmedListener {
        void onConfirmed(List<String> newSelection);
    }
}