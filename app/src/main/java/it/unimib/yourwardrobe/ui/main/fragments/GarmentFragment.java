package it.unimib.yourwardrobe.ui.main.fragments;

import static com.google.android.material.internal.ViewUtils.hideKeyboard;
import static com.google.android.material.internal.ViewUtils.showKeyboard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import dagger.hilt.android.AndroidEntryPoint;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.stream.LongStream;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.ui.main.viewmodel.GarmentViewModel;
import it.unimib.yourwardrobe.ui.main.viewmodel.factory.GarmentViewModelFactory;
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
    private View editButtonsContainer;
    private FloatingActionButton editFab;
    private FloatingActionButton deleteFab;

    public GarmentFragment() {
        // Required empty public constructor
    }

    public static GarmentFragment newInstance() {
        GarmentFragment fragment = new GarmentFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_garment, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        garmentImageView = view.findViewById(R.id.garmentImage); // Assicurati l'ID sia corretto nel layout
        nameTextView = view.findViewById(R.id.nameGarmentText);
        nameGarmentInputLayout = view.findViewById(R.id.nameGarmentInputLayout);
        nameGarmentEditText = view.findViewById(R.id.nameGarmentEditText);
        editButtonsContainer = view.findViewById(R.id.edit_buttons_container);
        editFab = view.findViewById(R.id.edit_fab);
        deleteFab = view.findViewById(R.id.delete_fab);
        Button cancelButton = view.findViewById(R.id.cancel_button);
        Button updateButton = view.findViewById(R.id.update_button);
        colorChipGroup = view.findViewById(R.id.chip_group_garment_color);
        styleChipGroup = view.findViewById(R.id.chip_group_garment_style);
        fabricChipGroup = view.findViewById(R.id.chip_group_garment_fabric);

        viewModel = new ViewModelProvider(this).get(GarmentViewModel.class);

        if (getArguments() != null) {
            // GarmentFragmentArgs viene generato automaticamente dal plugin Navigation
            Garment garment = GarmentFragmentArgs.fromBundle(getArguments()).getGarment();
            if (garment != null) {
                viewModel.setGarment(garment);
            }
        }
        nameGarmentEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setGarmentName(s.toString());
            }
        });

        editFab.setOnClickListener(v -> {
            viewModel.enterEditMode();
        });

        deleteFab.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });

        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> viewModel.cancelChanges());
        }
        updateButton.setOnClickListener(v -> viewModel.updateGarment());

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getGarment().observe(getViewLifecycleOwner(), garment -> {
            nameTextView.setText(garment.getName());
            if (isInitialGarmentLoad) {
                nameGarmentEditText.setText(garment.getName());
                isInitialGarmentLoad = false; // Imposta il flag a false dopo il primo caricamento
            }
            // CARICAMENTO OTTIMIZZATO
            Glide.with(this)
                    .load(garment.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background) // Immagine temporanea mentre carica la prima volta
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache totale
                    .into(garmentImageView);

            populateChips(garment);
        });

        viewModel.getIsDeleted().observe(getViewLifecycleOwner(), deleted -> {
            if (deleted) {
                ToastHelper.show(getContext(), "Capo eliminato", false);
                Navigation.findNavController(requireView()).navigateUp();
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
            Garment currentGarment = viewModel.getGarment().getValue();
            populateChips(currentGarment);
        });

        viewModel.getGarmentUpdatedSuccessfully().observe(getViewLifecycleOwner(), updated -> {
            if (updated) {
                ToastHelper.show(getContext(), "Modifiche salvate con successo!", false);
                // La modalità di modifica viene già disattivata dal ViewModel,
                // quindi i pulsanti si nasconderanno automaticamente.
            } else {
                // Potresti voler mostrare un errore specifico per l'update
                ToastHelper.show(getContext(), "Errore durante il salvataggio delle modifiche.", true);
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Conferma Eliminazione")
                .setMessage("Sei sicuro di voler eliminare questo capo? L'azione è irreversibile.")
                .setNegativeButton("Annulla", (dialog, which) -> {
                    // L'utente ha premuto "Annulla", chiudo dialog
                    dialog.dismiss();
                })
                .setPositiveButton("Elimina", (dialog, which) -> {
                    // L'utente ha confermato, procedo con l'eliminazione
                    viewModel.deleteGarment();
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_error));
        }
    }

    private Chip createChip(Context context, String text, boolean isRemovable, Runnable onRemove) {
        Chip chip = new Chip(context);
        chip.setText(text);
        chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.md_theme_primary)));
        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary));
        if (isRemovable) {
            chip.setCloseIconVisible(true); // Mostra la "X"
            chip.setClickable(true);        // Rende la chip cliccabile per la "X"
            chip.setFocusable(true);
            chip.setOnCloseIconClickListener(v -> {
                if (onRemove != null) {
                    onRemove.run(); // Esegue l'azione di rimozione
                }
            });
        } else {
            chip.setClickable(false); // Non interagibile in modalità visualizzazione
            chip.setFocusable(false);
        }
        return chip;
    }

    private Chip createAddChip(Runnable onClickAction) {
        Chip chip = new Chip(requireContext());
        chip.setText("+");
        chip.setOnClickListener(v -> onClickAction.run());
        return chip;
    }

    interface OnSelectionListener {
        void onSelected(List<String> selection);
    }
    private void populateChips(Garment garment) {
        if (garment == null) return;

        // Determina se siamo in modalità modifica
        boolean isInEditMode = viewModel.getIsEditMode().getValue() != null && viewModel.getIsEditMode().getValue();

        // Pulisci sempre i gruppi prima di ripopolarli
        colorChipGroup.removeAllViews();
        styleChipGroup.removeAllViews();
        fabricChipGroup.removeAllViews();

        if (isInEditMode) {
            // Chip '+' per i colori
            Chip addColorChip = createAddChip(() -> {
                showChipSelectionDialog("Aggiungi Colori",
                        viewModel.getAllColors(), // Assumendo che il VM esponga tutti i colori
                        garment.getColor(), // Passa i colori già selezionati
                        newColors -> viewModel.addColors(newColors));
            });
            colorChipGroup.addView(addColorChip);

            // Chip '+' per gli stili
            Chip addStyleChip = createAddChip(() -> {
                showChipSelectionDialog("Aggiungi Stili",
                        viewModel.getAllStyles(),
                        garment.getStyle(),
                        newStyles -> viewModel.addStyles(newStyles));
            });
            styleChipGroup.addView(addStyleChip);

            // Chip '+' per i tessuti
            Chip addFabricChip = createAddChip(() -> {
                showChipSelectionDialog("Aggiungi Tessuti",
                        viewModel.getAllFabrics(),
                        garment.getFabric(),
                        newFabrics -> viewModel.addFabrics(newFabrics));
            });
            fabricChipGroup.addView(addFabricChip);
        }
        // Popola le chip dei colori
        if (garment.getColor() != null) {
            for (String color : garment.getColor()) {
                Chip chip = createChip(requireContext(), color, isInEditMode, () -> {
                    // Azione da eseguire quando la "X" viene premuta
                    viewModel.removeColor(color);
                });
                colorChipGroup.addView(chip);
            }
        }

        // Popola le chip degli stili
        if (garment.getStyle() != null) {
            for (String style : garment.getStyle()) {
                Chip chip = createChip(requireContext(), style, isInEditMode, () -> {
                    viewModel.removeStyle(style);
                });
                styleChipGroup.addView(chip);
            }
        }

        // Popola le chip dei tessuti
        if (garment.getFabric() != null) {
            for (String fabric : garment.getFabric()) {
                Chip chip = createChip(requireContext(), fabric, isInEditMode, () -> {
                    viewModel.removeFabric(fabric);
                });
                fabricChipGroup.addView(chip);
            }
        }
    }

    private void showChipSelectionDialog(String title, List<String> allOptions, List<String> currentSelection, OnSelectionListener listener) {
        if (allOptions == null || allOptions.isEmpty()) {
            ToastHelper.show(getContext(), "Nessuna opzione da aggiungere.", false);
            return;
        }

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.chip_selector, null); // Assumi di avere chip_selector.xml
        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        ChipGroup dialogChipGroup = dialogView.findViewById(R.id.dialog_chip_group);
        Button okButton = dialogView.findViewById(R.id.dialog_ok_button);

        dialogTitle.setText(title);

        for (String option : allOptions) {
            Chip chip = new Chip(requireContext());
            chip.setText(option);
            chip.setCheckable(true);
            // Se l'opzione è già presente, la disabilita per non poterla ri-aggiungere
            if (currentSelection != null && currentSelection.contains(option)) {
                chip.setEnabled(false);
            }
            dialogChipGroup.addView(chip);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        dialogChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> okButton.setEnabled(!checkedIds.isEmpty()));
        okButton.setEnabled(false);

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
}