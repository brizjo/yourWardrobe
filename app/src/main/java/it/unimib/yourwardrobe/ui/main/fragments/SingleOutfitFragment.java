package it.unimib.yourwardrobe.ui.main.fragments;import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.adapter.OutfitComponentAdapter;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.model.Outfit;
import it.unimib.yourwardrobe.ui.main.components.CardOutfit;
import it.unimib.yourwardrobe.ui.main.viewmodel.SingleOutfitViewModel;
import it.unimib.yourwardrobe.utils.ToastHelper;

@AndroidEntryPoint
public class SingleOutfitFragment extends Fragment {

    private static final String TAG = "SingleOutfitFragment";
    private SingleOutfitViewModel viewModel;

    private CardOutfit collageCard;
    private TextView tvName;
    private RecyclerView rv;
    private TextView tvSeason;

    private View btnDelete, editFab, saveCancelContainer, btnUpdate, btnCancel, btnAddGarment;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_single_outfit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Inizializzazione sicura del ViewModel
        try {
            viewModel = new ViewModelProvider(this).get(SingleOutfitViewModel.class);
        } catch (Exception e) {
            Log.e(TAG, "Errore inizializzazione ViewModel", e);
            return;
        }

        // 2. Inizializzazione View con controlli null
        initViews(view);

        // 3. Recupero dati dal Bundle
        if (getArguments() != null) {
            Outfit o = (Outfit) getArguments().getSerializable("outfit");
            if (o != null) {
                viewModel.setOutfit(o);
            } else {
                Log.e(TAG, "Outfit non trovato nel bundle");
            }
        }

        // 4. Setup
        setupListeners();
        observeViewModel();
    }

    private void initViews(View view) {
        collageCard = view.findViewById(R.id.detail_outfit_collage);
        tvName = view.findViewById(R.id.tv_detail_outfit_name);
        rv = view.findViewById(R.id.rv_outfit_components_list);

        btnDelete = view.findViewById(R.id.btn_delete_outfit);
        editFab = view.findViewById(R.id.edit_fab);
        saveCancelContainer = view.findViewById(R.id.edit_buttons_container);
        btnUpdate = view.findViewById(R.id.update_button);
        btnCancel = view.findViewById(R.id.cancel_button);
        tvSeason = view.findViewById(R.id.tv_detail_outfit_season);
        btnAddGarment = view.findViewById(R.id.btn_add_component);


        // Configurazione Griglia 3 colonne
        if (rv != null) {
            rv.setLayoutManager(new GridLayoutManager(getContext(), 3));
            rv.setHasFixedSize(true);
        }
    }

    private void setupListeners() {
        // Ogni listener è protetto da controllo null per evitare crash se l'ID manca nell'XML
        if (editFab != null) editFab.setOnClickListener(v -> viewModel.enterEditMode());

        if (tvSeason != null) {
            tvSeason.setOnClickListener(v -> {
                if (Boolean.TRUE.equals(viewModel.getIsEditMode().getValue())) {
                    showSeasonSelectionDialog();
                }
            });
        }

        if (btnCancel != null) btnCancel.setOnClickListener(v -> viewModel.cancelEdit());

        if (btnUpdate != null) {
            btnUpdate.setOnClickListener(v -> {
                String newName = tvName != null ? tvName.getText().toString() : "";
                viewModel.saveChanges(newName);
            });
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Elimina Outfit")
                    .setMessage("Vuoi eliminare definitivamente questo outfit?")
                    .setPositiveButton("Elimina", (d, w) -> viewModel.deleteOutfit())
                    .setNegativeButton("Annulla", null).show());
        }
        if (btnAddGarment != null) btnAddGarment.setOnClickListener(v -> openPickerForAddition());
    }

    private void observeViewModel() {
        // Aggiornamento Dati Outfit
        viewModel.getOutfit().observe(getViewLifecycleOwner(), o -> {
            if (o != null) {
                if (collageCard != null) collageCard.setGarments(o.getGarments());
                if (tvName != null) tvName.setText(o.getName());
                if (tvSeason != null) tvSeason.setText("Stagione: " + o.getSeason());
                OutfitComponentAdapter adapter = new OutfitComponentAdapter(o.getGarments(), garment -> {
                    if (Boolean.TRUE.equals(viewModel.getIsEditMode().getValue())) {
                        // DIALOG DI MODIFICA COMPONENTE
                        String[] options = {"Sostituisci", "Rimuovi"};
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Modifica Capo")
                                .setItems(options, (dialog, which) -> {
                                    if (which == 0) openPickerForReplacement(garment);
                                    else viewModel.removeGarment(garment);
                                }).show();
                    } else {
                        navigateToGarment(garment);
                    }
                });
                if (rv != null) rv.setAdapter(adapter);
            }
        });

        // Cambio Stato UI (Edit vs Visione)
        viewModel.getIsEditMode().observe(getViewLifecycleOwner(), editing -> {
            boolean isEdit = Boolean.TRUE.equals(editing);

            if (editFab != null) editFab.setVisibility(isEdit ? View.GONE : View.VISIBLE);
            if (btnDelete != null) btnDelete.setVisibility(isEdit ? View.GONE : View.VISIBLE);
            if (btnAddGarment != null) btnAddGarment.setVisibility(isEdit ? View.VISIBLE : View.GONE);
            if (saveCancelContainer != null) saveCancelContainer.setVisibility(isEdit ? View.VISIBLE : View.GONE);
            if (tvSeason != null) {
                tvSeason.setClickable(isEdit);
                tvSeason.setAlpha(isEdit ? 0.7f : 1.0f); // Feedback visivo se è cliccabile
            }
            if (tvName != null) {
                tvName.setEnabled(isEdit);
                tvName.setFocusableInTouchMode(isEdit);
            }
        });


        // Eventi di Navigazione
        viewModel.getOutfitDeleted().observe(getViewLifecycleOwner(), deleted -> {
            if (Boolean.TRUE.equals(deleted)) {
                Navigation.findNavController(requireView()).navigateUp();
            }
        });
    }

    private void showRemoveDialog(Garment g) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Rimuovi capo")
                .setMessage("Togliere questo vestito dall'outfit?")
                .setPositiveButton("Rimuovi", (d, w) -> viewModel.removeGarment(g))
                .setNegativeButton("Annulla", null).show();
    }

    private void navigateToGarment(Garment g) {
        try {
            Bundle b = new Bundle();
            b.putSerializable("garment", g);
            Navigation.findNavController(requireView()).navigate(R.id.action_singleOutfitFragment_to_garmentFragment, b);
        } catch (Exception e) {
            Log.e(TAG, "Errore navigazione verso GarmentFragment", e);
        }
    }

    private void showSeasonSelectionDialog() {
        String[] seasons = {"Primavera", "Estate", "Autunno", "Inverno"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cambia Stagione")
                .setItems(seasons, (dialog, which) -> {
                    viewModel.updateSeason(seasons[which]);
                }).show();
    }

    /**
     * Apre il dialog per aggiungere un nuovo capo (scelta tra le 4 categorie)
     */
    private void openPickerForAddition() {
        String[] categories = {"Parte superiore", "Parte inferiore", "Scarpe", "Accessori"};
        new MaterialAlertDialogBuilder(requireContext())            .setTitle("Cosa vuoi aggiungere?")
                .setItems(categories, (dialog, which) -> {
                    int type = which + 1; // Mappa i tipi 1,2,3,4 del tuo SelectClothesDialogFragment
                    SelectClothesDialogFragment.newInstance(type, selected -> {
                        if (!selected.isEmpty()) viewModel.addGarment(selected.get(0));
                    }).show(getChildFragmentManager(), "add_garment");
                }).show();
    }

    /**
     * Apre il dialog per sostituire un capo esistente mantenendo la stessa categoria
     */
    private void openPickerForReplacement(Garment oldGarment) {
        int type = 1; // Default
        String cat = oldGarment.getCategory().toLowerCase();
        if (cat.contains("inferiore")) type = 2;
        else if (cat.contains("scarpe") || cat.contains("calzature")) type = 3;
        else if (cat.contains("accessorio")) type = 4;

        SelectClothesDialogFragment.newInstance(type, selected -> {
            if (!selected.isEmpty()) viewModel.replaceGarment(oldGarment, selected.get(0));
        }).show(getChildFragmentManager(), "replace_garment");
    }
}