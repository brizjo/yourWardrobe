package it.unimib.yourwardrobe.ui.main.fragments;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.main.viewmodel.AddGarmentViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import it.unimib.yourwardrobe.utils.ToastHelper;

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
    private TextInputEditText garmentNameEditText;
    private Button addGarmentButton;
    private ProgressBar addGarmentProgressBar;

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchCamera();
                } else {
                    Toast.makeText(getContext(), "Permesso fotocamera necessario per scattare una foto", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Void> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    addGarmentImageView.setImageBitmap(bitmap);
                    addGarmentImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    viewModel.setGarmentImage(bitmap);
                }
            });

    private final ActivityResultLauncher<String> requestGalleryPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchGallery();
                } else {
                    Toast.makeText(getContext(), "Permesso galleria necessario per scegliere una foto", Toast.LENGTH_SHORT).show();
                }
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
                        ToastHelper.show(getContext(), "Errore nel caricare l'immagine", true);
                    }
                } else {
                    Log.d("PhotoPicker", "Nessuna immagine selezionata");
                }
            });

    public AddGarmentFragment() {}

    public static AddGarmentFragment newInstance() {
        return new AddGarmentFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_garment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AddGarmentViewModel.class);

        addGarmentImageView  = view.findViewById(R.id.addGarmentImage);
        categoryTextView     = view.findViewById(R.id.category_text_view);
        colorChipGroup       = view.findViewById(R.id.chip_group_color);
        styleChipGroup       = view.findViewById(R.id.chip_group_style);
        fabricChipGroup      = view.findViewById(R.id.chip_group_fabric);
        seasonChipGroup      = view.findViewById(R.id.chip_group_season);
        subCategoryChipGroup = view.findViewById(R.id.chip_group_subcategory);
        garmentNameEditText  = view.findViewById(R.id.garmentName);
        addGarmentButton     = view.findViewById(R.id.add_garment_button);
        addGarmentProgressBar = view.findViewById(R.id.add_garment_progress_bar);

        addGarmentImageView.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_add_photo));

        // -------------------------------------------------------------------------
        // Observers
        // -------------------------------------------------------------------------

        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
            categoryTextView.setAdapter(adapter);
        });

        viewModel.getSelectedColors().observe(getViewLifecycleOwner(), colors ->
                updateMainChipGroup(colorChipGroup, colors,
                        selected -> viewModel.updateSelectedColors(selected)));

        viewModel.getSelectedStyles().observe(getViewLifecycleOwner(), styles ->
                updateMainChipGroup(styleChipGroup, styles,
                        selected -> viewModel.updateSelectedStyles(selected)));

        viewModel.getSelectedFabrics().observe(getViewLifecycleOwner(), fabrics ->
                updateMainChipGroup(fabricChipGroup, fabrics,
                        selected -> viewModel.updateSelectedFabrics(selected)));

        // Stagione: chip singola che mostra la selezione corrente
        viewModel.getSelectedSeason().observe(getViewLifecycleOwner(), season -> {
            updateSeasonChip(season);
        });

        // Sottocategoria: chip singola che mostra la selezione corrente
        viewModel.getSelectedSubCategory().observe(getViewLifecycleOwner(), subCategory -> {
            updateSubCategoryChip(subCategory);
        });

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
                ToastHelper.show(getContext(), "Capo aggiunto con successo!", false);
                Navigation.findNavController(view).popBackStack();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) ToastHelper.show(getContext(), error, false);
        });

        viewModel.isButtonEnabled().observe(getViewLifecycleOwner(), isEnabled ->
                addGarmentButton.setEnabled(isEnabled));

        // -------------------------------------------------------------------------
        // Listeners
        // -------------------------------------------------------------------------

        addGarmentImageView.setOnClickListener(v -> showImagePickerDialog());

        garmentNameEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setGarmentName(s.toString());
            }
        });

        categoryTextView.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedItem = (String) parent.getItemAtPosition(position);
            viewModel.setSelectedCategory(selectedItem);
        });

        setupAddChip(colorChipGroup, "color");
        setupAddChip(styleChipGroup, "style");
        setupAddChip(fabricChipGroup, "fabric");
        setupSeasonChip();
        setupSubCategoryChip();

        addGarmentButton.setOnClickListener(v -> viewModel.saveGarment());
    }

    // -------------------------------------------------------------------------
    // Season chip (selezione singola)
    // -------------------------------------------------------------------------

    private void setupSeasonChip() {
        seasonChipGroup.removeAllViews();
        Chip addChip = new Chip(requireContext());
        addChip.setText("+ Stagione");
        addChip.setChipBackgroundColor(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.md_theme_primary)));
        addChip.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary));
        addChip.setOnClickListener(v -> showSeasonSelectionDialog());
        seasonChipGroup.addView(addChip);
    }

    private void updateSeasonChip(String season) {
        seasonChipGroup.removeAllViews();
        if (season == null) {
            // Nessuna stagione selezionata: mostra chip "+"
            Chip addChip = new Chip(requireContext());
            addChip.setText("+ Stagione");
            addChip.setChipBackgroundColor(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.md_theme_primary)));
            addChip.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary));
            addChip.setOnClickListener(v -> showSeasonSelectionDialog());
            seasonChipGroup.addView(addChip);
        } else {
            // Stagione selezionata: mostra chip con testo e "X" per rimuoverla
            Chip chip = new Chip(requireContext());
            chip.setText(season);
            chip.setCloseIconVisible(true);
            chip.setChipBackgroundColor(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.md_theme_primary)));
            chip.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary));
            chip.setOnClickListener(v -> showSeasonSelectionDialog());
            chip.setOnCloseIconClickListener(v -> viewModel.setSelectedSeason(null));
            seasonChipGroup.addView(chip);
        }
    }

    private void showSeasonSelectionDialog() {
        List<String> allSeasons = viewModel.getAllSeasons().getValue();
        if (allSeasons == null) return;

        String[] seasonsArray = allSeasons.toArray(new String[0]);
        String currentSeason = viewModel.getSelectedSeason().getValue();
        int currentIndex = currentSeason != null ? allSeasons.indexOf(currentSeason) : -1;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Seleziona Stagione")
                .setSingleChoiceItems(seasonsArray, currentIndex, (dialog, which) -> {
                    viewModel.setSelectedSeason(seasonsArray[which]);
                    dialog.dismiss();
                })
                .setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // -------------------------------------------------------------------------
    // SubCategory chip (selezione singola)
    // -------------------------------------------------------------------------

    private void setupSubCategoryChip() {
        subCategoryChipGroup.removeAllViews();
        Chip addChip = new Chip(requireContext());
        addChip.setText("+ Sottocategoria");
        addChip.setChipBackgroundColor(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.md_theme_primary)));
        addChip.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary));
        addChip.setOnClickListener(v -> showSubCategorySelectionDialog());
        subCategoryChipGroup.addView(addChip);
    }

    private void updateSubCategoryChip(String subCategory) {
        subCategoryChipGroup.removeAllViews();
        if (subCategory == null) {
            // Nessuna sottocategoria selezionata: mostra chip "+"
            Chip addChip = new Chip(requireContext());
            addChip.setText("+ Sottocategoria");
            addChip.setChipBackgroundColor(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.md_theme_primary)));
            addChip.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary));
            addChip.setOnClickListener(v -> showSubCategorySelectionDialog());
            subCategoryChipGroup.addView(addChip);
        } else {
            // Sottocategoria selezionata: mostra chip con testo e "X" per rimuoverla
            Chip chip = new Chip(requireContext());
            chip.setText(subCategory);
            chip.setCloseIconVisible(true);
            chip.setChipBackgroundColor(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.md_theme_primary)));
            chip.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary));
            chip.setOnClickListener(v -> showSubCategorySelectionDialog());
            chip.setOnCloseIconClickListener(v -> viewModel.setSelectedSubCategory(null));
            subCategoryChipGroup.addView(chip);
        }
    }

    private void showSubCategorySelectionDialog() {
        List<String> allSubCategories = viewModel.getAllSubCategories().getValue();
        if (allSubCategories == null) return;

        String[] subCategoriesArray = allSubCategories.toArray(new String[0]);
        String currentSubCategory = viewModel.getSelectedSubCategory().getValue();
        int currentIndex = currentSubCategory != null ? allSubCategories.indexOf(currentSubCategory) : -1;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Seleziona Sottocategoria")
                .setSingleChoiceItems(subCategoriesArray, currentIndex, (dialog, which) -> {
                    viewModel.setSelectedSubCategory(subCategoriesArray[which]);
                    dialog.dismiss();
                })
                .setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // -------------------------------------------------------------------------
    // Colori / Stili / Tessuti chip (selezione multipla)
    // -------------------------------------------------------------------------

    private void setupAddChip(ChipGroup chipGroup, final String type) {
        Chip addChip = new Chip(requireContext());
        addChip.setText("+");
        addChip.setChipBackgroundColor(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.md_theme_primary)));
        addChip.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary));
        addChip.setOnClickListener(v -> {
            List<String> allOptions = null;
            List<String> currentSelection = null;
            String dialogTitle = "";

            if ("color".equals(type)) {
                allOptions = viewModel.getAllColors().getValue();
                currentSelection = viewModel.getSelectedColors().getValue();
                dialogTitle = "Seleziona Colori";
            } else if ("style".equals(type)) {
                allOptions = viewModel.getAllStyles().getValue();
                currentSelection = viewModel.getSelectedStyles().getValue();
                dialogTitle = "Seleziona Stili";
            } else if ("fabric".equals(type)) {
                allOptions = viewModel.getAllFabrics().getValue();
                currentSelection = viewModel.getSelectedFabrics().getValue();
                dialogTitle = "Seleziona Tessuti";
            }

            if (allOptions != null && currentSelection != null) {
                showChipSelectionDialog(dialogTitle, allOptions, currentSelection, newSelection -> {
                    if ("color".equals(type)) {
                        viewModel.updateSelectedColors(newSelection);
                    } else if ("style".equals(type)) {
                        viewModel.updateSelectedStyles(newSelection);
                    } else if ("fabric".equals(type)) {
                        viewModel.updateSelectedFabrics(newSelection);
                    }
                });
            }
        });
        chipGroup.addView(addChip, 0);
    }

    interface OnSelectionConfirmedListener {
        void onConfirmed(List<String> newSelection);
    }

    private void showChipSelectionDialog(String title, List<String> allOptions,
                                         List<String> currentSelection,
                                         OnSelectionConfirmedListener listener) {
        View dialogView = requireActivity().getLayoutInflater().inflate(R.layout.chip_selector, null);
        TextView dialogTitle      = dialogView.findViewById(R.id.dialog_title);
        ChipGroup dialogChipGroup = dialogView.findViewById(R.id.dialog_chip_group);
        Button okButton           = dialogView.findViewById(R.id.dialog_ok_button);

        dialogTitle.setText(title);

        int colorSelected = ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimaryContainer);
        int colorDefault = ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary);
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked}, // Stato: selezionato (checked)
                new int[]{-android.R.attr.state_checked}  // Stato: non selezionato
        };
        int[] colors = new int[]{
                colorSelected,
                colorDefault
        };
        ColorStateList colorStateList = new ColorStateList(states, colors);

        int textColorSelected = ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary); // Bianco quando selezionato
        int textColorDefault = ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimaryContainer);   // Nero/scuro quando non selezionato

        int[][] textStates = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };
        int[] textColors = new int[]{
                textColorSelected,
                textColorDefault
        };
        ColorStateList chipTextColorStateList = new ColorStateList(textStates, textColors);

        // Popola il dialog
        for (String option : allOptions) {
            Chip chip = new Chip(requireContext());
            chip.setText(option);
            chip.setCheckable(true);
            chip.setChipBackgroundColor(bgColorStateList);
            chip.setTextColor(textColorStateList);
            if (currentSelection.contains(option)) chip.setChecked(true);
            dialogChipGroup.addView(chip);
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        okButton.setEnabled(!dialogChipGroup.getCheckedChipIds().isEmpty());
        dialogChipGroup.setOnCheckedStateChangeListener((group, checkedIds) ->
                okButton.setEnabled(!checkedIds.isEmpty()));

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
            chip.setCloseIconVisible(true);
            chip.setChipBackgroundColor(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.md_theme_primary)));
            chip.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary));
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
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

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

    private void launchCamera() { takePictureLauncher.launch(null); }

    private void launchGallery() {
        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }
}