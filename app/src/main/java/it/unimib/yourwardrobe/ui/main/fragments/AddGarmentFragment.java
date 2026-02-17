package it.unimib.yourwardrobe.ui.main.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.os.Build;
import android.os.Bundle;
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
                if (isGranted) {
                    launchCamera();
                } else {
                    Snackbar.make(requireView(), "Permesso fotocamera necessario per scattare una foto", Snackbar.LENGTH_LONG).show();
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
                        Snackbar.make(requireView(), "Errore nel caricare l'immagine", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("PhotoPicker", "Nessuna immagine selezionata");
                }
            });
    private final ActivityResultLauncher<String> requestGalleryPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchGallery();
                } else {
                    Snackbar.make(requireView(), "Permesso galleria necessario per scegliere una foto", Snackbar.LENGTH_SHORT).show();
                }
            });
    private TextInputEditText garmentNameEditText;
    private Button addGarmentButton;
    private ProgressBar addGarmentProgressBar;

    public AddGarmentFragment() {
    }

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

        initViews(view);

        addGarmentImageView.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_add_photo));

        // -------------------------------------------------------------------------
        // Listeners
        // -------------------------------------------------------------------------

        addGarmentImageView.setOnClickListener(v -> showImagePickerDialog());

        garmentNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setGarmentName(s.toString());
            }
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
    // Observers
    // -------------------------------------------------------------------------

    private void observeViewModel(View view) {
        viewModel.getImageValidationState().observe(getViewLifecycleOwner(), state -> {
            if (state == ImageValidationState.INVALID_CONFIRMATION_NEEDED) {
                // L'immagine non è stata riconosciuta: mostra il dialog di conferma
                showInvalidGarmentConfirmationDialog();
            } else if (state == ImageValidationState.ERROR) {
                // Gestisci il caso di errore se necessario (già gestito da errorMessage)
            } else if (state == ImageValidationState.UNCHECKED) {
                // Resetta l'immagine se l'utente ha annullato
                addGarmentImageView.setImageDrawable(
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_add_photo));
                addGarmentImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
        });

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
                Snackbar.make(requireView(), "Capo aggiunto con successo!", Snackbar.LENGTH_SHORT).show();
                Navigation.findNavController(view).popBackStack();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null)
                Snackbar.make(requireView(), error, Snackbar.LENGTH_SHORT).show();
        });

        viewModel.isButtonEnabled().observe(getViewLifecycleOwner(), isEnabled ->
                addGarmentButton.setEnabled(isEnabled));

    }
    // -------------------------------------------------------------------------
    // Riconoscimento immagine
    // -------------------------------------------------------------------------

    private void showInvalidGarmentConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Capo non riconosciuto")
                .setMessage("Sembra che l'immagine selezionata non sia un capo d'abbigliamento. Vuoi continuare comunque?")
                .setPositiveButton("Continua", (dialog, which) -> {
                    viewModel.forceImageAsValid();
                })
                .setNegativeButton("Annulla", (dialog, which) -> {
                    viewModel.resetImageSelection();
                })
                .setCancelable(false)
                .show();
    }

    // -------------------------------------------------------------------------
    // Season chip (selezione singola)
    // -------------------------------------------------------------------------

    private void setupSeasonChip() {
        seasonChipGroup.removeAllViews();
        Chip addChip = new Chip(requireContext());
        addChip.setText("+");
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
            addChip.setText("+");
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

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Seleziona Stagione")
                .setItems(seasonsArray, (dialog, which) -> {
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
        addChip.setText("+");
        addChip.setChipBackgroundColor(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.md_theme_primary)));
        addChip.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary));
        subCategoryChipGroup.setEnabled(false);
        addChip.setOnClickListener(v -> {
            if (viewModel.getSelectedCategory().getValue() == null) {
                Snackbar.make(requireView(), "Seleziona prima una categoria", Snackbar.LENGTH_SHORT).show();
                return;
            }
            showSubCategorySelectionDialog();
        });
        subCategoryChipGroup.addView(addChip);
    }

    private void updateSubCategoryChip(String subCategory) {
        subCategoryChipGroup.removeAllViews();
        if (subCategory == null) {
            // Nessuna sottocategoria selezionata: mostra chip "+"
            Chip addChip = new Chip(requireContext());
            addChip.setText("+");
            addChip.setChipBackgroundColor(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.md_theme_primary)));
            addChip.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary));
            addChip.setOnClickListener(v -> {
                if (viewModel.getSelectedCategory().getValue() == null) {
                    Snackbar.make(requireView(), "Seleziona prima categoria", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                showSubCategorySelectionDialog();
            });
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
        List<String> subCategories = viewModel.getAvailableSubCategories().getValue();
        if (subCategories == null || subCategories.isEmpty()) {
            Snackbar.make(requireView(), "Nessuna sottocategoria disponibile per la categoria selezionata.", Snackbar.LENGTH_SHORT).show();
            return;
        }

        String[] subCategoriesArray = subCategories.toArray(new String[0]);
        //String currentSubCategory = viewModel.getSelectedSubCategory().getValue();
        //int currentIndex = currentSubCategory != null ? subCategories.indexOf(currentSubCategory) : -1;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Seleziona Sottocategoria")
                .setItems(subCategoriesArray, (dialog, which) -> {
                    String selectedSubCategory = subCategoriesArray[which];
                    viewModel.setSelectedSubCategory(selectedSubCategory);
                    //dialog.dismiss();
                })
                .setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // -------------------------------------------------------------------------
    // Colori / Stili / Tessuti chip (selezione multipla)
    // -------------------------------------------------------------------------

    private void setupAddChip(ChipGroup chipGroup, final String type) {
        Chip addChip = new Chip(requireContext());
        addChip.setText(R.string.plus);
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
                    } else {
                        viewModel.updateSelectedFabrics(newSelection);
                    }
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
            chip.setChipBackgroundColor(colorStateList);
            chip.setTextColor(chipTextColorStateList);
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

    private void launchCamera() {
        takePictureLauncher.launch(null);
    }

    private void launchGallery() {
        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    interface OnSelectionConfirmedListener {
        void onConfirmed(List<String> newSelection);
    }
}