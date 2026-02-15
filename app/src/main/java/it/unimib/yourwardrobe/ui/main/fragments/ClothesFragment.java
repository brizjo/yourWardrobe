package it.unimib.yourwardrobe.ui.main.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
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

import it.unimib.yourwardrobe.ui.main.viewmodel.ClothesViewModel;
import it.unimib.yourwardrobe.utils.ToastHelper;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ClothesFragment extends Fragment {


    private ClothesViewModel clothesViewModel;
    private ClothesAdapter.OnItemClickListener listener;
    private ChipGroup activeFiltersChipGroup;
    private Button addFirstGarmentButton;
    private ImageView orderButton;
    private ImageView filterButton;
    private HorizontalScrollView activeFiltersScrollView;
    private NestedScrollView categoriesScrollView;
    private RecyclerView gridRecyclerView;
    private RecyclerView recyclerViewTop;
    private RecyclerView recyclerViewBottom;
    private RecyclerView recyclerViewFootWear;
    private RecyclerView recyclerViewAccessories;
    private View emptyWardrobeView;
    private View topButtonsContainer;
    private View mainContainer;
    private ProgressBar loadingProgressBar;
    private ClothesAdapter topAdapter;
    private ClothesAdapter bottomAdapter;
    private ClothesAdapter footwearAdapter;
    private ClothesAdapter accessoriesAdapter;
    private ClothesAdapter gridAdapter;

    public ClothesFragment() {
        // Required empty public constructor
    }


    public static ClothesFragment newInstance() {
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
        this.listener = (v, item) -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("garment", item);
            Navigation.findNavController(requireView()).navigate(R.id.action_clothesFragment_to_garmentFragment, bundle);
        };
        initViews(view);

        initAdapter();

        setAdaptersLayoutManager();

        setAdapters();

        addFirstGarmentButton.setOnClickListener(v -> {
            NavHostFragment navHostFragment = (NavHostFragment) requireActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment);
            if (navHostFragment != null) {
                NavController navController = navHostFragment.getNavController();
                navController.navigate(R.id.action_clothesFragment_to_addGarmentFragment);
            }
        });

        orderButton.setOnClickListener(v -> showOrderDialog());
        Button addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            NavHostFragment navHostFragment = (NavHostFragment) requireActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment);
            if (navHostFragment != null) {
                NavController navController = navHostFragment.getNavController();
                navController.navigate(R.id.action_clothesFragment_to_addGarmentFragment);
            } else {
                // Log di errore nel caso in cui il NavHostFragment non venga trovato
                Log.e("ClothesFragment", "NavHostFragment non trovato. Impossibile navigare.");
            }
        });

        filterButton.setOnClickListener(v -> {
            showFilterCategoryMenu();
        });

        clothesViewModel = new ViewModelProvider(this).get(ClothesViewModel.class);

        observeViewModel();

    }

    private void initViews(View view){
        categoriesScrollView = view.findViewById(R.id.categories_scroll_view);
        gridRecyclerView = view.findViewById(R.id.grid_recycler_view);
        emptyWardrobeView = view.findViewById(R.id.empty_wardrobe_view);
        topButtonsContainer = view.findViewById(R.id.top_buttons_container);
        mainContainer = view.findViewById(R.id.main_container);
        loadingProgressBar = view.findViewById(R.id.loading_progressbar);
        addFirstGarmentButton = view.findViewById(R.id.add_first_garment_button);
        orderButton = view.findViewById(R.id.order_button);
        filterButton = view.findViewById(R.id.filter_button);
        activeFiltersChipGroup = view.findViewById(R.id.active_filters_chipgroup);
        activeFiltersScrollView = view.findViewById(R.id.active_filters_scrollview);
        recyclerViewTop = view.findViewById(R.id.parte_superiore_recycler_view);
        recyclerViewBottom = view.findViewById(R.id.parte_inferiore_recycler_view);
        recyclerViewFootWear = view.findViewById(R.id.calzature_recycler_view);
        recyclerViewAccessories = view.findViewById(R.id.accessori_recycler_view);
    }

    private void initAdapter(){
        topAdapter = new ClothesAdapter(new ArrayList<>(), listener);
        bottomAdapter = new ClothesAdapter(new ArrayList<>(), listener);
        footwearAdapter = new ClothesAdapter(new ArrayList<>(), listener);
        accessoriesAdapter = new ClothesAdapter(new ArrayList<>(), listener);
        gridAdapter = new ClothesAdapter(new ArrayList<>(), R.layout.item_clothes_grid, listener);
    }

    private void setAdaptersLayoutManager() {
        recyclerViewTop.setLayoutManager(new CarouselLayoutManager(new UncontainedCarouselStrategy()));
        recyclerViewBottom.setLayoutManager(new CarouselLayoutManager(new UncontainedCarouselStrategy()));
        recyclerViewFootWear.setLayoutManager(new CarouselLayoutManager(new UncontainedCarouselStrategy()));
        recyclerViewAccessories.setLayoutManager(new CarouselLayoutManager(new UncontainedCarouselStrategy()));
    }

    private void setAdapters(){
        recyclerViewTop.setAdapter(topAdapter);
        recyclerViewBottom.setAdapter(bottomAdapter);
        recyclerViewFootWear.setAdapter(footwearAdapter);
        recyclerViewAccessories.setAdapter(accessoriesAdapter);
        gridRecyclerView.setAdapter(gridAdapter);
    }

    private void observeViewModel(){
        clothesViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                if (isLoading) {
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    mainContainer.setVisibility(View.GONE);
                } else {
                    loadingProgressBar.setVisibility(View.GONE);
                    mainContainer.setVisibility(View.VISIBLE);
                }
            }
        });

        clothesViewModel.getIsWardrobeEmpty().observe(getViewLifecycleOwner(), isEmpty -> {
            if (isEmpty != null) { // Main check
                if (isEmpty) {
                    emptyWardrobeView.setVisibility(View.VISIBLE);
                    topButtonsContainer.setVisibility(View.GONE);
                    activeFiltersScrollView.setVisibility(View.GONE);
                    categoriesScrollView.setVisibility(View.GONE);
                    gridRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyWardrobeView.setVisibility(View.GONE);
                    topButtonsContainer.setVisibility(View.VISIBLE);
                    updateLayoutForDisplayMode(clothesViewModel.getDisplayMode().getValue());
                }
            }
        });

        clothesViewModel.getDisplayMode().observe(getViewLifecycleOwner(), mode -> {
            updateLayoutForDisplayMode(mode);
        });

        clothesViewModel.getGridGarments().observe(getViewLifecycleOwner(), garments -> {
            if (garments != null) {
                gridAdapter.updateGarments(garments);
            }
        });

        clothesViewModel.getTopGarments().observe(getViewLifecycleOwner(), topGarments -> {
            if (topGarments != null) {
                topAdapter.updateGarments(topGarments);

                if (topGarments.isEmpty())
                    //TODO; CORRETTA GESTIONE DI UN ARMADIO ANCORA VUOTO
                    Log.d("ClothesFragment", "L'utente non ha ancora caricato vestiti.");
            }
        });

        clothesViewModel.getBottomGarments().observe(getViewLifecycleOwner(), bottomGarments -> {
            if (bottomGarments != null) {
                bottomAdapter.updateGarments(bottomGarments);
            }
        });

        clothesViewModel.getFootwearGarments().observe(getViewLifecycleOwner(), footwear -> {
            if (footwear != null) {
                footwearAdapter.updateGarments(footwear);
            }
        });

        clothesViewModel.getAccessories().observe(getViewLifecycleOwner(), accessories -> {
            if (accessories != null) {
                accessoriesAdapter.updateGarments(accessories);
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
    }

    @Override
    public void onResume() {
        super.onResume();
        // Quando il fragment torna visibile (ad esempio, dopo essere tornati
        // da GarmentFragment), chiedi al ViewModel di ricaricare i dati.
        // Poiché il DataSource usa un listener in tempo reale, questo garantirà
        // che la UI si aggiorni con gli ultimi cambiamenti da Firebase.
        if (clothesViewModel != null) {
            clothesViewModel.fetchGarments();
        }
    }

    private void updateLayoutForDisplayMode(ClothesViewModel.DisplayMode mode) {
        if (mode == ClothesViewModel.DisplayMode.BY_CATEGORY) {
            categoriesScrollView.setVisibility(View.VISIBLE);
            gridRecyclerView.setVisibility(View.GONE);
        } else {
            categoriesScrollView.setVisibility(View.GONE);
            gridRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showOrderDialog() {
        String[] orderOptions = getResources().getStringArray(R.array.order_options);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Ordina per")
                .setItems(orderOptions, (dialog, which) -> {
                    switch (which) {
                        case 0: // Alfabetico
                            clothesViewModel.setDisplayMode(ClothesViewModel.DisplayMode.GRID_ALPHABETICAL);
                            break;
                        case 1: // Data di inserimento
                            clothesViewModel.setDisplayMode(ClothesViewModel.DisplayMode.GRID_BY_DATE);
                            break;
                        case 2: // Categoria
                            clothesViewModel.setDisplayMode(ClothesViewModel.DisplayMode.BY_CATEGORY);
                            break;
                    }
                })
                .show();
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

        dialogTitle.setText(getString(R.string.select) + title);

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

        // Popola il ChipGroup con tutte le opzioni
        for (String option : allOptions) {
            Chip chip = new Chip(requireContext());
            chip.setText(option);
            chip.setCheckable(true);
            chip.setChipBackgroundColor(colorStateList);
            chip.setTextColor(chipTextColorStateList);
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
        chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.md_theme_primary)));
        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary));
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