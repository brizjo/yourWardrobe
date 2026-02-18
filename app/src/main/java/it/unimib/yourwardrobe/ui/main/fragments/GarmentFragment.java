package it.unimib.yourwardrobe.ui.main.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.ui.common.ImageValidationState;
import it.unimib.yourwardrobe.ui.main.viewmodel.GarmentViewModel;

@AndroidEntryPoint
public class GarmentFragment extends Fragment {

    private GarmentViewModel viewModel;
    private boolean isInitialGarmentLoad = true;
    private Bitmap pendingBitmap = null;

    // ✅ font
    private Typeface popstarTypeface;
    private Typeface changoTypeface;

    private ImageView garmentImageView;

    private final ActivityResultLauncher<Void> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) handleNewImage(bitmap);
            });

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) launchCamera();
                else
                    Snackbar.make(requireView(), "Permesso fotocamera necessario!", Snackbar.LENGTH_LONG).show();
            });

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    try {
                        ImageDecoder.Source source = ImageDecoder.createSource(
                                requireActivity().getContentResolver(), uri);
                        Bitmap bitmap = ImageDecoder.decodeBitmap(source);
                        handleNewImage(bitmap);
                    } catch (IOException e) {
                        Snackbar.make(requireView(), "Errore nel caricare l'immagine", Snackbar.LENGTH_LONG).show();
                    }
                }
            });

    private final ActivityResultLauncher<String> requestGalleryPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) launchGallery();
                else
                    Toast.makeText(getContext(), "Permesso galleria necessario", Toast.LENGTH_SHORT).show();
            });

    private TextView nameTextView;
    private TextInputLayout nameGarmentInputLayout;
    private TextInputEditText nameGarmentEditText;
    private ChipGroup colorChipGroup;
    private ChipGroup styleChipGroup;
    private ChipGroup fabricChipGroup;
    private ChipGroup seasonChipGroup;
    private ChipGroup subCategoryChipGroup;
    private View editButtonsContainer;
    private FloatingActionButton editFab;
    private FloatingActionButton deleteFab;
    private Button cancelButton;
    private Button updateButton;
    private ProgressBar deleteProgressBar;
    private AlertDialog deleteConfirmationDialog;

    public GarmentFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_garment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        // ✅ carica i font
        popstarTypeface = androidx.core.content.res.ResourcesCompat
                .getFont(requireContext(), R.font.popstar);
        changoTypeface = androidx.core.content.res.ResourcesCompat
                .getFont(requireContext(), R.font.chango);

        initViews(view);

        // ✅ applica popstar ai bottoni da codice (per sicurezza oltre all'XML)
        if (popstarTypeface != null) {
            updateButton.setTypeface(popstarTypeface);
            cancelButton.setTypeface(popstarTypeface);
            nameTextView.setTypeface(popstarTypeface);
            nameGarmentEditText.setTypeface(popstarTypeface);
        }

        viewModel = new ViewModelProvider(this).get(GarmentViewModel.class);

        if (getArguments() != null) {
            Garment garment = GarmentFragmentArgs.fromBundle(getArguments()).getGarment();
            viewModel.setGarment(garment);
        }

        nameGarmentEditText.addTextChangedListener(new TextWatcher() {
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

        editFab.setOnClickListener(v -> viewModel.enterEditMode());
        deleteFab.setOnClickListener(v -> showDeleteConfirmationDialog());
        if (cancelButton != null) cancelButton.setOnClickListener(v -> viewModel.cancelChanges());
        updateButton.setOnClickListener(v -> viewModel.updateGarment());

        observeViewModel();
    }

    private void initViews(View view) {
        garmentImageView = view.findViewById(R.id.garmentImage);
        nameTextView = view.findViewById(R.id.nameGarmentText);
        nameGarmentInputLayout = view.findViewById(R.id.nameGarmentInputLayout);
        nameGarmentEditText = view.findViewById(R.id.nameGarmentEditText);
        editButtonsContainer = view.findViewById(R.id.edit_buttons_container);
        editFab = view.findViewById(R.id.edit_fab);
        deleteFab = view.findViewById(R.id.delete_fab);
        colorChipGroup = view.findViewById(R.id.chip_group_garment_color);
        styleChipGroup = view.findViewById(R.id.chip_group_garment_style);
        fabricChipGroup = view.findViewById(R.id.chip_group_garment_fabric);
        seasonChipGroup = view.findViewById(R.id.chip_group_garment_season);
        subCategoryChipGroup = view.findViewById(R.id.chip_group_garment_subcategory);
        cancelButton = view.findViewById(R.id.cancel_button);
        updateButton = view.findViewById(R.id.update_button);
    }

    // -------------------------------------------------------------------------
    // Observers
    // -------------------------------------------------------------------------

    private void observeViewModel() {
        viewModel.getGarment().observe(getViewLifecycleOwner(), garment -> {
            nameTextView.setText(garment.getName());
            if (isInitialGarmentLoad) {
                nameGarmentEditText.setText(garment.getName());
                isInitialGarmentLoad = false;
            }
            if (pendingBitmap != null) {
                garmentImageView.setImageBitmap(pendingBitmap);
            } else {
                Glide.with(this)
                        .load(garment.getImageUrl())
                        .placeholder(R.drawable.ic_launcher_background)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(garmentImageView);
            }
            populateChips(garment);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (Boolean.TRUE.equals(isLoading)) {
                editFab.hide();
                deleteFab.hide();
                updateButton.setEnabled(false);
                cancelButton.setEnabled(false);
            } else {
                Boolean isEditMode = viewModel.getIsEditMode().getValue();
                if (Boolean.TRUE.equals(isEditMode)) {
                    updateButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                }
            }
        });

        viewModel.getIsDeleted().observe(getViewLifecycleOwner(), deleted -> {
            if (Boolean.TRUE.equals(deleted)) {
                if (deleteConfirmationDialog != null && deleteConfirmationDialog.isShowing())
                    deleteConfirmationDialog.dismiss();
                Snackbar.make(requireView(), "Capo eliminato", Snackbar.LENGTH_LONG).show();
                Navigation.findNavController(requireView()).navigateUp();
            } else if (deleted != null) {
                if (deleteConfirmationDialog != null && deleteConfirmationDialog.isShowing())
                    deleteConfirmationDialog.dismiss();
            }
        });

        viewModel.getIsEditMode().observe(getViewLifecycleOwner(), isInEditMode -> {
            if (isInEditMode) {
                nameTextView.setVisibility(View.GONE);
                nameGarmentInputLayout.setVisibility(View.VISIBLE);
                editButtonsContainer.setVisibility(View.VISIBLE);
                editFab.setVisibility(View.GONE);
                deleteFab.setVisibility(View.GONE);
                nameGarmentEditText.requestFocus();
                showKeyboard(nameGarmentEditText);
                garmentImageView.setOnClickListener(v -> showImagePickerDialog());
            } else {
                nameTextView.setVisibility(View.VISIBLE);
                nameGarmentInputLayout.setVisibility(View.GONE);
                editButtonsContainer.setVisibility(View.GONE);
                editFab.setVisibility(View.VISIBLE);
                deleteFab.setVisibility(View.VISIBLE);
                nameGarmentEditText.clearFocus();
                hideKeyboard(nameGarmentEditText);
                garmentImageView.setOnClickListener(null);
                pendingBitmap = null;
            }
            populateChips(viewModel.getGarment().getValue());
        });

        viewModel.getImageValidationState().observe(getViewLifecycleOwner(), state -> {
            if (state == ImageValidationState.VALID && pendingBitmap != null) {
                garmentImageView.setImageBitmap(pendingBitmap);
                Snackbar.make(requireView(), "Immagine aggiornata", Snackbar.LENGTH_SHORT).show();
            } else if (state == ImageValidationState.INVALID_CONFIRMATION_NEEDED) {
                showInvalidImageDialog();
            } else if (state == ImageValidationState.ERROR) {
                pendingBitmap = null;
                Snackbar.make(requireView(), "Errore durante la validazione dell'immagine", Snackbar.LENGTH_SHORT).show();
            }
        });

        viewModel.getGarmentUpdatedSuccessfully().observe(getViewLifecycleOwner(), updated -> {
            if (Boolean.TRUE.equals(updated)) {
                Snackbar.make(requireView(), "Modifiche salvate con successo!", Snackbar.LENGTH_SHORT).show();
            } else if (updated != null) {
                Snackbar.make(requireView(), "Errore durante il salvataggio delle modifiche.", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    // -------------------------------------------------------------------------
    // Image picker
    // -------------------------------------------------------------------------

    private void handleNewImage(Bitmap bitmap) {
        pendingBitmap = bitmap;
        garmentImageView.setImageBitmap(bitmap);
        viewModel.onNewImageSelected(bitmap);
    }

    private void showImagePickerDialog() {
        String[] options = {"Scatta una foto", "Scegli dalla galleria"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cambia immagine")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) checkCameraPermissionAndLaunch();
                    else checkGalleryPermissionAndLaunch();
                })
                .show();
    }

    private void showInvalidImageDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Immagine non riconosciuta")
                .setMessage("L'immagine non sembra un capo d'abbigliamento. Vuoi usarla comunque?")
                .setPositiveButton("Usa comunque", (dialog, which) -> {
                    if (pendingBitmap != null) viewModel.forceImageAsValid(pendingBitmap);
                })
                .setNegativeButton("Annulla", (dialog, which) -> {
                    pendingBitmap = null;
                    viewModel.cancelImageChange();
                    Garment current = viewModel.getGarment().getValue();
                    if (current != null) {
                        Glide.with(this)
                                .load(current.getImageUrl())
                                .placeholder(R.drawable.ic_launcher_background)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(garmentImageView);
                    }
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

    private void launchCamera() {
        takePictureLauncher.launch(null);
    }

    private void launchGallery() {
        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    // -------------------------------------------------------------------------
    // Chips
    // -------------------------------------------------------------------------

    private void populateChips(Garment garment) {
        if (garment == null) return;

        boolean isInEditMode = Boolean.TRUE.equals(viewModel.getIsEditMode().getValue());

        colorChipGroup.removeAllViews();
        styleChipGroup.removeAllViews();
        fabricChipGroup.removeAllViews();
        seasonChipGroup.removeAllViews();
        subCategoryChipGroup.removeAllViews();

        if (isInEditMode) {
            colorChipGroup.addView(createAddChip(() ->
                    showChipSelectionDialog("Aggiungi Colori",
                            viewModel.getAllColors(), garment.getColor(),
                            newColors -> viewModel.addColors(newColors))));

            styleChipGroup.addView(createAddChip(() ->
                    showChipSelectionDialog("Aggiungi Stili",
                            viewModel.getAllStyles(), garment.getStyle(),
                            newStyles -> viewModel.addStyles(newStyles))));

            fabricChipGroup.addView(createAddChip(() ->
                    showChipSelectionDialog("Aggiungi Tessuti",
                            viewModel.getAllFabrics(), garment.getFabric(),
                            newFabrics -> viewModel.addFabrics(newFabrics))));

            // ✅ chip stagione edit mode — nero
            Chip seasonChip = new Chip(requireContext());
            seasonChip.setText(garment.getSeason() != null ? garment.getSeason() : "+");
            applyChangoToChip(seasonChip);
            styleChipBlack(seasonChip);
            seasonChip.setOnClickListener(v -> showSeasonSelectionDialog(garment));
            if (garment.getSeason() != null) {
                seasonChip.setCloseIconVisible(true);
                seasonChip.setOnCloseIconClickListener(v -> viewModel.clearSeason());
            }
            seasonChipGroup.addView(seasonChip);

            // ✅ chip sottocategoria edit mode — nero
            Chip subCategoryChip = new Chip(requireContext());
            subCategoryChip.setText(garment.getSubCategory() != null ? garment.getSubCategory() : "+");
            applyChangoToChip(subCategoryChip);
            styleChipBlack(subCategoryChip);
            subCategoryChip.setOnClickListener(v -> showSubCategorySelectionDialog(garment));
            if (garment.getSubCategory() != null) {
                subCategoryChip.setCloseIconVisible(true);
                subCategoryChip.setOnCloseIconClickListener(v -> viewModel.setSubCategory(null));
            }
            subCategoryChipGroup.addView(subCategoryChip);

        } else {
            if (garment.getSeason() != null)
                seasonChipGroup.addView(createChip(requireContext(), garment.getSeason(), false, null));
            if (garment.getSubCategory() != null)
                subCategoryChipGroup.addView(createChip(requireContext(), garment.getSubCategory(), false, null));
        }

        if (garment.getColor() != null) {
            for (String color : garment.getColor())
                colorChipGroup.addView(createChip(requireContext(), color, isInEditMode,
                        () -> viewModel.removeColor(color)));
        }

        if (garment.getStyle() != null) {
            for (String style : garment.getStyle())
                styleChipGroup.addView(createChip(requireContext(), style, isInEditMode,
                        () -> viewModel.removeStyle(style)));
        }

        if (garment.getFabric() != null) {
            for (String fabric : garment.getFabric())
                fabricChipGroup.addView(createChip(requireContext(), fabric, isInEditMode,
                        () -> viewModel.removeFabric(fabric)));
        }
    }

    // ✅ chip standard — nero con chango
    private Chip createChip(Context context, String text, boolean isRemovable, Runnable onRemove) {
        Chip chip = new Chip(context);
        chip.setText(text);
        applyChangoToChip(chip);
        styleChipBlack(chip);
        if (isRemovable) {
            chip.setCloseIconVisible(true);
            chip.setClickable(true);
            chip.setFocusable(true);
            chip.setOnCloseIconClickListener(v -> {
                if (onRemove != null) onRemove.run();
            });
        } else {
            chip.setClickable(false);
            chip.setFocusable(false);
        }
        return chip;
    }

    // ✅ chip "+" — nero con chango
    private Chip createAddChip(Runnable onClickAction) {
        Chip chip = new Chip(requireContext());
        chip.setText(R.string.plus);
        applyChangoToChip(chip);
        styleChipBlack(chip);
        chip.setOnClickListener(v -> onClickAction.run());
        return chip;
    }

    // ✅ helper — sfondo nero, testo e icona bianchi
    private void styleChipBlack(Chip chip) {
        chip.setChipBackgroundColor(ColorStateList.valueOf(Color.BLACK));
        chip.setTextColor(Color.WHITE);
        chip.setCloseIconTint(ColorStateList.valueOf(Color.WHITE));
    }

    // ✅ helper — font chango sui chip
    private void applyChangoToChip(Chip chip) {
        if (changoTypeface != null) chip.setTypeface(changoTypeface);
    }

    // -------------------------------------------------------------------------
    // Dialogs
    // -------------------------------------------------------------------------

    private void showSeasonSelectionDialog(Garment garment) {
        List<String> allSeasons = viewModel.getAllSeasons();
        String[] seasonsArray = allSeasons.toArray(new String[0]);
        int currentIndex = garment.getSeason() != null ? allSeasons.indexOf(garment.getSeason()) : -1;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Seleziona Stagione")
                .setSingleChoiceItems(seasonsArray, currentIndex, (dialog, which) -> {
                    viewModel.setSelectedSeason(seasonsArray[which]);
                    dialog.dismiss();
                })
                .setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showSubCategorySelectionDialog(Garment garment) {
        List<String> subCategories = viewModel.getAvailableSubcategoriesForGarment();
        if (subCategories.isEmpty()) {
            Snackbar.make(requireView(), "Nessuna sottocategoria disponibile", Snackbar.LENGTH_SHORT).show();
            return;
        }
        String[] subCategoriesArray = subCategories.toArray(new String[0]);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Seleziona Sottocategoria")
                .setItems(subCategoriesArray, (dialog, which) -> {
                    viewModel.setSubCategory(subCategoriesArray[which]);
                    dialog.dismiss();
                })
                .setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showChipSelectionDialog(String title, List<String> allOptions,
                                         List<String> currentSelection,
                                         OnSelectionListener listener) {
        if (allOptions == null || allOptions.isEmpty()) {
            Snackbar.make(requireView(), "Nessuna opzione da aggiungere", Snackbar.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.chip_selector, null);
        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        ChipGroup dialogChipGroup = dialogView.findViewById(R.id.dialog_chip_group);
        Button okButton = dialogView.findViewById(R.id.dialog_ok_button);

        dialogTitle.setText(title);
        // ✅ font popstar sul titolo del dialog
        if (popstarTypeface != null) dialogTitle.setTypeface(popstarTypeface);

        // ✅ chip del dialog — neri con chango
        for (String option : allOptions) {
            Chip chip = new Chip(requireContext());
            chip.setText(option);
            chip.setCheckable(true);
            applyChangoToChip(chip);
            chip.setChipBackgroundColor(new ColorStateList(
                    new int[][]{{android.R.attr.state_checked}, {-android.R.attr.state_checked}},
                    new int[]{Color.BLACK, Color.LTGRAY}));
            chip.setTextColor(new ColorStateList(
                    new int[][]{{android.R.attr.state_checked}, {-android.R.attr.state_checked}},
                    new int[]{Color.WHITE, Color.BLACK}));
            if (currentSelection != null && currentSelection.contains(option))
                chip.setEnabled(false);
            dialogChipGroup.addView(chip);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView).create();

        okButton.setEnabled(false);
        // ✅ font popstar sul bottone OK del dialog
        if (popstarTypeface != null) okButton.setTypeface(popstarTypeface);

        dialogChipGroup.setOnCheckedStateChangeListener((group, checkedIds) ->
                okButton.setEnabled(!checkedIds.isEmpty()));

        okButton.setOnClickListener(v -> {
            List<String> newSelection = new ArrayList<>();
            for (int id : dialogChipGroup.getCheckedChipIds()) {
                Chip selectedChip = dialogChipGroup.findViewById(id);
                newSelection.add(selectedChip.getText().toString());
            }
            listener.onSelected(newSelection);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDeleteConfirmationDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_confirm_delete, null);
        ProgressBar dialogProgressBar = dialogView.findViewById(R.id.dialog_progress_bar);

        deleteConfirmationDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Conferma Eliminazione")
                .setView(dialogView)
                .setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Elimina", (dialog, which) -> {
                })
                .create();

        deleteConfirmationDialog.show();

        Button positiveButton = deleteConfirmationDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_error));
            positiveButton.setOnClickListener(v -> {
                positiveButton.setVisibility(View.GONE);
                deleteConfirmationDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
                dialogProgressBar.setVisibility(View.VISIBLE);
                deleteConfirmationDialog.setCancelable(false);
                viewModel.deleteGarment();
            });
        }
    }

    private void showKeyboard(@NonNull View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard(@NonNull View view) {
        InputMethodManager imm = (InputMethodManager)
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    interface OnSelectionListener {
        void onSelected(List<String> selection);
    }
}