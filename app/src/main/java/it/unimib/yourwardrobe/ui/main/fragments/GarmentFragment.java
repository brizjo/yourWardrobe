package it.unimib.yourwardrobe.ui.main.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

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

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.core.di.ServiceLocator;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.ui.main.viewmodel.GarmentViewModel;
import it.unimib.yourwardrobe.ui.main.viewmodel.factory.GarmentViewModelFactory;
import it.unimib.yourwardrobe.utils.ToastHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GarmentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GarmentFragment extends Fragment {

    private GarmentViewModel viewModel;
    private ImageView garmentImageView;
    private TextView nameTextView;
    private ChipGroup colorChipGroup;
    private ChipGroup styleChipGroup;
    private ChipGroup fabricChipGroup;
    private View editButtonsContainer;

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
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                // Gonfia il tuo menu, esattamente come facevi prima in onCreateOptionsMenu
                menuInflater.inflate(R.menu.garment_details_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                // Gestisci il click, esattamente come facevi prima in onOptionsItemSelected
                if (menuItem.getItemId() == R.id.action_edit_garment) {
                    viewModel.enterEditMode();
                    return true; // Indica che l'evento è stato gestito
                }
                return false; // L'evento non è stato gestito da questo provider
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        garmentImageView = view.findViewById(R.id.garmentImage); // Assicurati l'ID sia corretto nel layout
        nameTextView = view.findViewById(R.id.nameGarmentText);
        editButtonsContainer = view.findViewById(R.id.edit_buttons_container);
        Button deleteButton = view.findViewById(R.id.delete_button);
        Button updateButton = view.findViewById(R.id.update_button);
        colorChipGroup = view.findViewById(R.id.chip_group_garment_color);
        styleChipGroup = view.findViewById(R.id.chip_group_garment_style);
        fabricChipGroup = view.findViewById(R.id.chip_group_garment_fabric);

        GarmentRepository repository = ServiceLocator.getInstance().getGarmentRepository();
        GarmentViewModelFactory factory = new GarmentViewModelFactory(requireActivity().getApplication(), repository);
        viewModel = new ViewModelProvider(this, factory).get(GarmentViewModel.class);

        if (getArguments() != null) {
            // GarmentFragmentArgs viene generato automaticamente dal plugin Navigation
            Garment garment = GarmentFragmentArgs.fromBundle(getArguments()).getGarment();
            if (garment != null) {
                viewModel.setGarment(garment);
            }
        }

        observeViewModel();
        if (deleteButton != null) {
            deleteButton.setOnClickListener(v -> viewModel.deleteGarment());
        }
        updateButton.setOnClickListener(v -> viewModel.updateGarment());
    }

    private void observeViewModel() {
        viewModel.getGarment().observe(getViewLifecycleOwner(), garment -> {
            nameTextView.setText(garment.getName());

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
                Navigation.findNavController(requireView()).navigateUp();
            }
        });

        viewModel.getIsEditMode().observe(getViewLifecycleOwner(), isInEditMode -> {
            if (isInEditMode) {
                editButtonsContainer.setVisibility(View.VISIBLE);
            } else {
                editButtonsContainer.setVisibility(View.GONE);
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