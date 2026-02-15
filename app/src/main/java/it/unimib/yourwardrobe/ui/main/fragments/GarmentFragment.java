package it.unimib.yourwardrobe.ui.main.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.ui.main.viewmodel.GarmentViewModel;
import it.unimib.yourwardrobe.utils.ToastHelper;

@AndroidEntryPoint
public class GarmentFragment extends Fragment {

    private GarmentViewModel viewModel;
    private boolean isInitialGarmentLoad = true;

    private ImageView garmentImageView;
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

    public GarmentFragment() {}

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

        initViews(view);

        viewModel = new ViewModelProvider(this).get(GarmentViewModel.class);

        if (getArguments() != null) {
            Garment garment = GarmentFragmentArgs.fromBundle(getArguments()).getGarment();
            if (garment != null) viewModel.setGarment(garment);
        }

        nameGarmentEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
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

    private void initViews(View view){
        garmentImageView       = view.findViewById(R.id.garmentImage);
        nameTextView           = view.findViewById(R.id.nameGarmentText);
        nameGarmentInputLayout = view.findViewById(R.id.nameGarmentInputLayout);
        nameGarmentEditText    = view.findViewById(R.id.nameGarmentEditText);
        editButtonsContainer   = view.findViewById(R.id.edit_buttons_container);
        editFab                = view.findViewById(R.id.edit_fab);
        deleteFab              = view.findViewById(R.id.delete_fab);
        colorChipGroup         = view.findViewById(R.id.chip_group_garment_color);
        styleChipGroup         = view.findViewById(R.id.chip_group_garment_style);
        fabricChipGroup        = view.findViewById(R.id.chip_group_garment_fabric);
        seasonChipGroup        = view.findViewById(R.id.chip_group_garment_season);
        subCategoryChipGroup   = view.findViewById(R.id.chip_group_garment_subcategory);
        cancelButton    = view.findViewById(R.id.cancel_button);
        updateButton    = view.findViewById(R.id.update_button);
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
            Glide.with(this)
                    .load(garment.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(garmentImageView);
            populateChips(garment);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (Boolean.TRUE.equals(isLoading)) {
                deleteProgressBar.setVisibility(View.VISIBLE);
                editFab.hide();
                deleteFab.hide();
                editButtonsContainer.setVisibility(View.GONE);
            }
        });

        viewModel.getIsDeleted().observe(getViewLifecycleOwner(), deleted -> {
            if (Boolean.TRUE.equals(deleted)) {
                if (deleteConfirmationDialog != null && deleteConfirmationDialog.isShowing()) {
                    deleteConfirmationDialog.dismiss();
                }
                ToastHelper.show(getContext(), "Capo eliminato", false);
                Navigation.findNavController(requireView()).navigateUp();
            } else if (deleted != null) {
                if (deleteConfirmationDialog != null && deleteConfirmationDialog.isShowing()) {
                    deleteConfirmationDialog.dismiss();
                }
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
            } else {
                nameTextView.setVisibility(View.VISIBLE);
                nameGarmentInputLayout.setVisibility(View.GONE);
                editButtonsContainer.setVisibility(View.GONE);
                editFab.setVisibility(View.VISIBLE);
                deleteFab.setVisibility(View.VISIBLE);
                nameGarmentEditText.clearFocus();
                hideKeyboard(nameGarmentEditText);
            }
            populateChips(viewModel.getGarment().getValue());
        });

        viewModel.getGarmentUpdatedSuccessfully().observe(getViewLifecycleOwner(), updated -> {
            if (Boolean.TRUE.equals(updated)) {
                ToastHelper.show(getContext(), "Modifiche salvate con successo!", false);
            } else if (updated != null) {
                ToastHelper.show(getContext(), "Errore durante il salvataggio delle modifiche.", true);
            }
        });
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
            // Colori (multipla)
            colorChipGroup.addView(createAddChip(() ->
                    showChipSelectionDialog("Aggiungi Colori",
                            viewModel.getAllColors(), garment.getColor(),
                            newColors -> viewModel.addColors(newColors))));

            // Stili (multipla)
            styleChipGroup.addView(createAddChip(() ->
                    showChipSelectionDialog("Aggiungi Stili",
                            viewModel.getAllStyles(), garment.getStyle(),
                            newStyles -> viewModel.addStyles(newStyles))));

            // Tessuti (multipla)
            fabricChipGroup.addView(createAddChip(() ->
                    showChipSelectionDialog("Aggiungi Tessuti",
                            viewModel.getAllFabrics(), garment.getFabric(),
                            newFabrics -> viewModel.addFabrics(newFabrics))));

            // Stagione (singola): chip cliccabile per aprire il dialog di selezione
            Chip seasonChip = new Chip(requireContext());
            seasonChip.setText(garment.getSeason() != null ? garment.getSeason() : "+");
            seasonChip.setChipBackgroundColor(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.md_theme_primary)));
            seasonChip.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary));
            seasonChip.setOnClickListener(v -> showSeasonSelectionDialog(garment));
            if (garment.getSeason() != null) {
                seasonChip.setCloseIconVisible(true);
                seasonChip.setOnCloseIconClickListener(v -> viewModel.clearSeason());
            }
            seasonChipGroup.addView(seasonChip);

            // Sottocategoria (singola): chip cliccabile per aprire il dialog di selezione
            Chip subCategoryChip = new Chip(requireContext());
            subCategoryChip.setText(garment.getSubCategory() != null ? garment.getSubCategory() : "+");
            subCategoryChip.setChipBackgroundColor(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.md_theme_primary)));
            subCategoryChip.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary));
            subCategoryChip.setOnClickListener(v -> showSubCategorySelectionDialog(garment));
            if (garment.getSubCategory() != null) {
                subCategoryChip.setCloseIconVisible(true);
                subCategoryChip.setOnCloseIconClickListener(v -> viewModel.setSubCategory(null));
            }
            subCategoryChipGroup.addView(subCategoryChip);

        } else {
            // Modalità visualizzazione (non edit)
            if (garment.getSeason() != null) {
                seasonChipGroup.addView(
                        createChip(requireContext(), garment.getSeason(), false, null));
            }
            if (garment.getSubCategory() != null) {
                subCategoryChipGroup.addView(
                        createChip(requireContext(), garment.getSubCategory(), false, null));
            }
        }

        // Colori (sempre visibili)
        if (garment.getColor() != null) {
            for (String color : garment.getColor()) {
                colorChipGroup.addView(createChip(requireContext(), color, isInEditMode,
                        () -> viewModel.removeColor(color)));
            }
        }

        // Stili (sempre visibili)
        if (garment.getStyle() != null) {
            for (String style : garment.getStyle()) {
                styleChipGroup.addView(createChip(requireContext(), style, isInEditMode,
                        () -> viewModel.removeStyle(style)));
            }
        }

        // Tessuti (sempre visibili)
        if (garment.getFabric() != null) {
            for (String fabric : garment.getFabric()) {
                fabricChipGroup.addView(createChip(requireContext(), fabric, isInEditMode,
                        () -> viewModel.removeFabric(fabric)));
            }
        }
    }

    private Chip createChip(Context context, String text, boolean isRemovable, Runnable onRemove) {
        Chip chip = new Chip(context);
        chip.setText(text);
        chip.setChipBackgroundColor(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.md_theme_primary)));
        chip.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary));
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

    private Chip createAddChip(Runnable onClickAction) {
        Chip chip = new Chip(requireContext());
        chip.setText("+");
        chip.setChipBackgroundColor(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.md_theme_primary)));
        chip.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary));
        chip.setOnClickListener(v -> onClickAction.run());
        return chip;
    }

    // -------------------------------------------------------------------------
    // Dialog stagione (selezione singola)
    // -------------------------------------------------------------------------

    private void showSeasonSelectionDialog(Garment garment) {
        List<String> allSeasons = viewModel.getAllSeasons();
        String[] seasonsArray = allSeasons.toArray(new String[0]);

        int currentIndex = garment.getSeason() != null
                ? allSeasons.indexOf(garment.getSeason())
                : -1;

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
    // Dialog sottocategoria (selezione singola)
    // -------------------------------------------------------------------------

    private void showSubCategorySelectionDialog(Garment garment) {
        List<String> subCategories = viewModel.getAvailableSubcategoriesForGarment();
        if (subCategories.isEmpty()) {
            ToastHelper.show(getContext(), "Nessuna sottocategoria disponibile.", false);
            return;
        }
        String[] subCategoriesArray = subCategories.toArray(new String[0]);
        String currentSubCategory = garment.getSubCategory();
        //int checkedItem = subCategories.indexOf(currentSubCategory);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Seleziona Sottocategoria")
                .setItems(subCategoriesArray, (dialog, which) -> {
                    String selectedSubCategory = subCategoriesArray[which];
                    viewModel.setSubCategory(selectedSubCategory);
                    dialog.dismiss();
                })
                .setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // -------------------------------------------------------------------------
    // Dialog selezione chip multipla (colori, stili, tessuti)
    // -------------------------------------------------------------------------

    interface OnSelectionListener {
        void onSelected(List<String> selection);
    }

    private void showChipSelectionDialog(String title, List<String> allOptions,
                                         List<String> currentSelection,
                                         OnSelectionListener listener) {
        if (allOptions == null || allOptions.isEmpty()) {
            ToastHelper.show(getContext(), "Nessuna opzione da aggiungere.", false);
            return;
        }

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.chip_selector, null);
        TextView dialogTitle      = dialogView.findViewById(R.id.dialog_title);
        ChipGroup dialogChipGroup = dialogView.findViewById(R.id.dialog_chip_group);
        Button okButton           = dialogView.findViewById(R.id.dialog_ok_button);

        dialogTitle.setText(title);

        int colorSelected = ContextCompat.getColor(requireContext(), R.color.md_theme_primary);
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

        for (String option : allOptions) {
            Chip chip = new Chip(requireContext());
            chip.setText(option);
            chip.setChipBackgroundColor(colorStateList);
            chip.setTextColor(chipTextColorStateList);
            chip.setCheckable(true);
            if (currentSelection != null && currentSelection.contains(option)) {
                chip.setEnabled(false);
            }
            dialogChipGroup.addView(chip);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        okButton.setEnabled(false);
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

    // -------------------------------------------------------------------------
    // Delete dialog
    // -------------------------------------------------------------------------

    private void showDeleteConfirmationDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_confirm_delete, null);
        ProgressBar dialogProgressBar = dialogView.findViewById(R.id.dialog_progress_bar);

        deleteConfirmationDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Conferma Eliminazione")
                .setView(dialogView)
                .setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Elimina", (dialog, which) -> {})
                .create();

        deleteConfirmationDialog.show();

        Button positiveButton = deleteConfirmationDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.md_theme_error));
            positiveButton.setOnClickListener(v -> {
                positiveButton.setVisibility(View.GONE);
                deleteConfirmationDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setVisibility(View.GONE);
                dialogProgressBar.setVisibility(View.VISIBLE);
                deleteConfirmationDialog.setCancelable(false);
                viewModel.deleteGarment();
            });
        }
    }

    // -------------------------------------------------------------------------
    // Keyboard helpers
    // -------------------------------------------------------------------------

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
}