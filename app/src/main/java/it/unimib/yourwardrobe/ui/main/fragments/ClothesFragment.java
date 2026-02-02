package it.unimib.yourwardrobe.ui.main.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.UncontainedCarouselStrategy;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.adapter.ClothesAdapter;
import it.unimib.yourwardrobe.core.di.ServiceLocator;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.ui.main.viewmodel.ClothesViewModel;
import it.unimib.yourwardrobe.ui.main.viewmodel.factory.ClothesViewModelFactory;
import it.unimib.yourwardrobe.utils.ToastHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ClothesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClothesFragment extends Fragment {


    private ClothesViewModel clothesViewModel;
    private RecyclerView recyclerViewTop, recyclerViewBottom, recyclerViewAccessories;
    private ClothesAdapter.OnItemClickListener listener;
    private ChipGroup activeFiltersChipGroup;
    private HorizontalScrollView activeFiltersScrollView;


    public ClothesFragment() {
        // Required empty public constructor
    }


    public static ClothesFragment newInstance(String param1, String param2) {
        ClothesFragment fragment = new ClothesFragment();
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
        return inflater.inflate(R.layout.fragment_clothes, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ClothesAdapter.OnItemClickListener listener = (v, item) -> {

            Bundle bundle = new Bundle();
            bundle.putSerializable("garment", item);
            Navigation.findNavController(v).navigate(R.id.action_clothesFragment_to_garmentFragment, bundle);
        };

        RecyclerView recyclerViewTop = view.findViewById(R.id.parte_superiore_recycler_view);
        RecyclerView recyclerViewBottom = view.findViewById(R.id.parte_inferiore_recycler_view);
        RecyclerView recyclerViewAccessories = view.findViewById(R.id.accessori_recycler_view);

        recyclerViewTop.setLayoutManager(new CarouselLayoutManager(new UncontainedCarouselStrategy()));
        recyclerViewBottom.setLayoutManager(new CarouselLayoutManager(new UncontainedCarouselStrategy()));
        recyclerViewAccessories.setLayoutManager(new CarouselLayoutManager(new UncontainedCarouselStrategy()));

        recyclerViewTop.setNestedScrollingEnabled(false);


        GarmentRepository repository = ServiceLocator.getInstance().getGarmentRepository();
        ClothesViewModelFactory factory = new ClothesViewModelFactory(requireActivity().getApplication(), repository);
        activeFiltersChipGroup = view.findViewById(R.id.active_filters_chipgroup);
        activeFiltersScrollView = view.findViewById(R.id.active_filters_scrollview);

        clothesViewModel = new ViewModelProvider(this, factory).get(ClothesViewModel.class);

        clothesViewModel.getTopGarments().observe(getViewLifecycleOwner(), topGarments -> {
            if (topGarments != null) {
                recyclerViewTop.setAdapter(new ClothesAdapter(topGarments, listener));

                if (topGarments.isEmpty())
                    //TODO; CORRETTA GESTIONE DI UN ARMADIO ANCORA VUOTO
                    Log.d("ClothesFragment", "L'utente non ha ancora caricato vestiti.");
            }
        });

        clothesViewModel.getBottomGarments().observe(getViewLifecycleOwner(), bottomGarments -> {
            if (bottomGarments != null) {
                recyclerViewBottom.setAdapter(new ClothesAdapter(bottomGarments, listener));
            }
        });

        clothesViewModel.getAccessories().observe(getViewLifecycleOwner(), accessories -> {
            if (accessories != null) {
                recyclerViewAccessories.setAdapter(new ClothesAdapter(accessories, listener));
            }
        });

        clothesViewModel.getActiveFilters().observe(getViewLifecycleOwner(), activeFiltersMap -> {
            activeFiltersChipGroup.removeAllViews(); // Pulisce i filtri precedenti
            boolean hasActiveFilters = false;

            // Recupera e aggiunge le chip per ogni categoria di filtro
            List<String> colors = activeFiltersMap.get("color");
            if (colors != null && !colors.isEmpty()) {
                hasActiveFilters = true;
                for (String color : colors) {
                    Chip chip = createRemovableChip(color, "color");
                    activeFiltersChipGroup.addView(chip);
                }
            }

            List<String> styles = activeFiltersMap.get("style");
            if (styles != null && !styles.isEmpty()) {
                hasActiveFilters = true;
                for (String style : styles) {
                    Chip chip = createRemovableChip(style, "style");
                    activeFiltersChipGroup.addView(chip);
                }
            }

            List<String> fabrics = activeFiltersMap.get("fabric");
            if (fabrics != null && !fabrics.isEmpty()) {
                hasActiveFilters = true;
                for (String fabric : fabrics) {
                    Chip chip = createRemovableChip(fabric, "fabric");
                    activeFiltersChipGroup.addView(chip);
                }
            }

            // Mostra o nasconde la barra dei filtri
            activeFiltersScrollView.setVisibility(hasActiveFilters ? View.VISIBLE : View.GONE);
        });

        clothesViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                ToastHelper.show(getContext(), error, false);
            }

        });
        Button addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_clothesFragment_to_addGarmentFragment));
        ImageView filterButton = view.findViewById(R.id.filter_button);
        filterButton.setOnClickListener(v -> {
            showFilterCategoryMenu();
        });
    }

    private void showFilterCategoryMenu() {
        // Opzioni da mostrare nel menu
        final String[] filterCategories = {"Colore", "Stile", "Tessuto"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Filtra per...")
                .setItems(filterCategories, (dialog, which) -> {
                    // L'utente ha scelto una categoria, ora mostra il popup con le chip
                    switch (which) {
                        case 0: // Colore
                            // Avvia il dialog di selezione per i colori
                            // Dobbiamo recuperare la lista di tutti i colori dal ViewModel
                            clothesViewModel.getAllColors().observe(getViewLifecycleOwner(), allColors -> {
                                if (allColors != null && !allColors.isEmpty()) {
                                    showFilterValueDialog("Colore", allColors, selectedValues -> {
                                        clothesViewModel.filterByColor(selectedValues);
                                    });
                                }
                            });
                            break;
                        case 1: // Stile
                            clothesViewModel.getAllStyles().observe(getViewLifecycleOwner(), allStyles -> {
                                if (allStyles != null && !allStyles.isEmpty()) {
                                    showFilterValueDialog("Stile", allStyles, selectedValues -> {
                                        clothesViewModel.filterByStyle(selectedValues);
                                    });
                                }
                            });
                            break;
                        case 2: // Tessuto
                            clothesViewModel.getAllFabrics().observe(getViewLifecycleOwner(), allFabrics -> {
                                if (allFabrics != null && !allFabrics.isEmpty()) {
                                    showFilterValueDialog("Tessuto", allFabrics, selectedValues -> {
                                        clothesViewModel.filterByFabric(selectedValues);
                                    });
                                }
                            });
                            break;
                    }
                })
                .show();
    }
    interface OnFilterValuesSelectedListener {
        void onSelected(List<String> selectedValues);
    }
    private void showFilterValueDialog(String title, List<String> allOptions, OnFilterValuesSelectedListener listener) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.chip_selector, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        ChipGroup dialogChipGroup = dialogView.findViewById(R.id.dialog_chip_group);
        Button okButton = dialogView.findViewById(R.id.dialog_ok_button);

        dialogTitle.setText("Seleziona " + title);

        // Popola il ChipGroup con tutte le opzioni
        for (String option : allOptions) {
            Chip chip = new Chip(requireContext());
            chip.setText(option);
            chip.setCheckable(true);
            dialogChipGroup.addView(chip);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        // Abilita/disabilita il pulsante OK
        dialogChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            okButton.setEnabled(!checkedIds.isEmpty());
        });
        okButton.setEnabled(false); // Inizia disabilitato

        okButton.setOnClickListener(v -> {
            List<String> selection = new ArrayList<>();
            for (int id : dialogChipGroup.getCheckedChipIds()) {
                Chip selectedChip = dialogChipGroup.findViewById(id);
                selection.add(selectedChip.getText().toString());
            }
            // Chiama il listener con i valori selezionati
            listener.onSelected(selection);
            dialog.dismiss();
        });

        dialog.show();
    }

    private Chip createRemovableChip(String text, String type) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            // Notifica il ViewModel della rimozione del filtro
            switch (type) {
                case "color":
                    List<String> currentColors = new ArrayList<>(clothesViewModel.getActiveFilters().getValue().get("color"));
                    currentColors.remove(text);
                    clothesViewModel.filterByColor(currentColors);
                    break;
                case "style":
                    List<String> currentStyles = new ArrayList<>(clothesViewModel.getActiveFilters().getValue().get("style"));
                    currentStyles.remove(text);
                    clothesViewModel.filterByStyle(currentStyles);
                    break;

                case "fabric":
                    List<String> currentFabrics = new ArrayList<>(clothesViewModel.getActiveFilters().getValue().get("fabric"));
                    currentFabrics.remove(text);
                    clothesViewModel.filterByFabric(currentFabrics);
                    break;
            }
        });
        return chip;
    }
}