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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.adapter.OutfitGridAdapter;
import it.unimib.yourwardrobe.ui.main.viewmodel.OutfitMenuViewModel;
import it.unimib.yourwardrobe.utils.ToastHelper;

@AndroidEntryPoint
public class OutfitMenuFragment extends Fragment {

    private static final String TAG = "OutfitMenuFragment";
    private OutfitMenuViewModel viewModel;

    private RecyclerView recyclerViewOutfit;
    private Button createOutfitButton;
    private ImageView filterButton;
    private ImageView orderButton;
    private HorizontalScrollView activeFiltersScrollView;
    private ChipGroup activeFiltersChipGroup;

    public OutfitMenuFragment() {}

    public static OutfitMenuFragment newInstance() {
        return new OutfitMenuFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate chiamato");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView chiamato");
        return inflater.inflate(R.layout.fragment_outfit_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated chiamato");

        viewModel = new ViewModelProvider(this).get(OutfitMenuViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupButtons();
        observeViewModel();
    }

    private void initViews(View view) {
        recyclerViewOutfit = view.findViewById(R.id.outfit_recycler_view);
        createOutfitButton = view.findViewById(R.id.create_outfit_button);
        filterButton = view.findViewById(R.id.filter_button);
        orderButton = view.findViewById(R.id.order_button);
        activeFiltersScrollView = view.findViewById(R.id.active_filters_scrollview);
        activeFiltersChipGroup = view.findViewById(R.id.active_filters_chipgroup);

        if (recyclerViewOutfit == null) {
            Log.e(TAG, "❌ RecyclerView è NULL! Controlla gli ID nel layout");
            return;
        }

        Log.d(TAG, "✅ Tutte le view inizializzate correttamente");
    }

    private void setupRecyclerView() {
        recyclerViewOutfit.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewOutfit.setNestedScrollingEnabled(false);
        Log.d(TAG, "LayoutManager impostato: GridLayoutManager con 2 colonne");
    }

    private void setupButtons() {
        // Bottone crea outfit
        createOutfitButton.setOnClickListener(v -> {
            Log.d(TAG, "Click su 'Crea Outfit' button");
            Navigation.findNavController(v).navigate(
                    R.id.action_outfitFragment_to_createOutfitFragment);
        });

        // Bottone filtri
        filterButton.setOnClickListener(v -> {
            Log.d(TAG, "Click su 'Filtri' button");
            showFilterCategoryMenu();
        });

        // Bottone ordinamento
        orderButton.setOnClickListener(v -> {
            Log.d(TAG, "Click su 'Ordina' button");
            showOrderDialog();
        });
    }

    private void observeViewModel() {
        // Observe outfit
        viewModel.getOutfits().observe(getViewLifecycleOwner(), outfits -> {
            Log.d(TAG, "═══════════════════════════════════════");
            Log.d(TAG, "Observer chiamato!");
            Log.d(TAG, "Outfits ricevuti: " + (outfits != null ? outfits.size() : "NULL"));

            if (outfits != null) {
                if (outfits.isEmpty()) {
                    Log.w(TAG, "⚠️ Lista vuota - nessun outfit salvato");
                    ToastHelper.show(getContext(), "Nessun outfit trovato con questi filtri", false);
                } else {
                    Log.d(TAG, "✅ Trovati " + outfits.size() + " outfit:");
                    for (int i = 0; i < outfits.size(); i++) {
                        Log.d(TAG, "  [" + i + "] Nome: " + outfits.get(i).getName() +
                                ", Garments: " + (outfits.get(i).getGarments() != null ?
                                outfits.get(i).getGarments().size() : "null"));
                    }
                }

                // Crea e imposta adapter
                OutfitGridAdapter.OnItemClickListener listener = (itemView, outfit) -> {
                    Log.d(TAG, "Click su outfit: " + outfit.getName());
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("outfit", outfit);
                    Navigation.findNavController(itemView).navigate(
                            R.id.action_outfitFragment_to_singleOutfitFragment, bundle);
                };

                OutfitGridAdapter adapter = new OutfitGridAdapter(outfits, R.layout.item_outfit_grid, listener);
                recyclerViewOutfit.setAdapter(adapter);
                Log.d(TAG, "✅ Adapter impostato sulla RecyclerView");
                Log.d(TAG, "   Item count nell'adapter: " + adapter.getItemCount());
            } else {
                Log.e(TAG, "❌ Lista outfits è NULL!");
            }
            Log.d(TAG, "═══════════════════════════════════════");
        });

        // Observe errori
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Log.e(TAG, "❌❌❌ ERRORE dal ViewModel: " + error);
                ToastHelper.show(getContext(), "Errore: " + error, false);
            }
        });

        // Observe filtri attivi
        viewModel.getActiveFilters().observe(getViewLifecycleOwner(), activeFiltersMap -> {
            updateActiveFiltersChips(activeFiltersMap);
        });
    }

    /**
     * Aggiorna le chip dei filtri attivi
     */
    private void updateActiveFiltersChips(Map<String, List<String>> activeFiltersMap) {
        activeFiltersChipGroup.removeAllViews();
        boolean hasActiveFilters = false;

        if (activeFiltersMap != null) {
            // Chip per stili
            List<String> styles = activeFiltersMap.get("style");
            if (styles != null && !styles.isEmpty()) {
                hasActiveFilters = true;
                for (String style : styles) {
                    Chip chip = createRemovableChip(style, "style");
                    activeFiltersChipGroup.addView(chip);
                }
            }

            // Chip per stagioni
            List<String> seasons = activeFiltersMap.get("season");
            if (seasons != null && !seasons.isEmpty()) {
                hasActiveFilters = true;
                for (String season : seasons) {
                    Chip chip = createRemovableChip(season, "season");
                    activeFiltersChipGroup.addView(chip);
                }
            }

            // Chip per colori
            List<String> colors = activeFiltersMap.get("color");
            if (colors != null && !colors.isEmpty()) {
                hasActiveFilters = true;
                for (String color : colors) {
                    Chip chip = createRemovableChip(color, "color");
                    activeFiltersChipGroup.addView(chip);
                }
            }
        }

        // Mostra/nascondi la barra dei filtri
        activeFiltersScrollView.setVisibility(hasActiveFilters ? View.VISIBLE : View.GONE);
    }

    /**
     * Crea una chip removibile per i filtri attivi
     */
    private Chip createRemovableChip(String text, String type) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setChipBackgroundColor(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.md_theme_primary)));
        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary));

        chip.setOnCloseIconClickListener(v -> {
            Map<String, List<String>> currentFilters = viewModel.getActiveFilters().getValue();
            if (currentFilters != null) {
                List<String> filterList = currentFilters.get(type);
                if (filterList != null) {
                    List<String> newList = new ArrayList<>(filterList);
                    newList.remove(text);

                    switch (type) {
                        case "style":
                            viewModel.filterByStyle(newList);
                            break;
                        case "season":
                            viewModel.filterBySeason(newList);
                            break;
                        case "color":
                            viewModel.filterByColor(newList);
                            break;
                    }
                }
            }
        });

        return chip;
    }

    /**
     * Mostra dialog per selezionare la categoria di filtro
     */
    private void showFilterCategoryMenu() {
        final String[] filterCategories = {"Stile", "Stagione", "Colore"};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Filtra per...")
                .setItems(filterCategories, (dialog, which) -> {
                    switch (which) {
                        case 0: // Stile
                            showFilterValueDialog("Stile", viewModel.getAllStyles(),
                                    selectedValues -> viewModel.filterByStyle(selectedValues));
                            break;
                        case 1: // Stagione
                            showFilterValueDialog("Stagione", viewModel.getAllSeasons(),
                                    selectedValues -> viewModel.filterBySeason(selectedValues));
                            break;
                        case 2: // Colore
                            showFilterValueDialog("Colore", viewModel.getAllColors(),
                                    selectedValues -> viewModel.filterByColor(selectedValues));
                            break;
                    }
                })
                .show();
    }

    /**
     * Mostra dialog con chip selezionabili per filtrare
     */
    interface OnFilterValuesSelectedListener {
        void onSelected(List<String> selectedValues);
    }

    private void showFilterValueDialog(String title, List<String> allOptions,
                                       OnFilterValuesSelectedListener listener) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.chip_selector, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        ChipGroup dialogChipGroup = dialogView.findViewById(R.id.dialog_chip_group);
        Button okButton = dialogView.findViewById(R.id.dialog_ok_button);

        dialogTitle.setText("Seleziona " + title);

        // Colori per le chip
        int colorSelected = ContextCompat.getColor(requireContext(), R.color.md_theme_primary);
        int colorDefault = ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary);
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };
        int[] colors = new int[]{colorSelected, colorDefault};
        ColorStateList colorStateList = new ColorStateList(states, colors);

        int textColorSelected = ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary);
        int textColorDefault = ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimaryContainer);
        int[][] textStates = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };
        int[] textColors = new int[]{textColorSelected, textColorDefault};
        ColorStateList chipTextColorStateList = new ColorStateList(textStates, textColors);

        // Popola il ChipGroup
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
        okButton.setEnabled(false);
        dialogChipGroup.setOnCheckedStateChangeListener((group, checkedIds) ->
                okButton.setEnabled(!checkedIds.isEmpty()));

        okButton.setOnClickListener(v -> {
            List<String> selection = new ArrayList<>();
            for (int id : dialogChipGroup.getCheckedChipIds()) {
                Chip selectedChip = dialogChipGroup.findViewById(id);
                selection.add(selectedChip.getText().toString());
            }
            listener.onSelected(selection);
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Mostra dialog per ordinare gli outfit
     */
    private void showOrderDialog() {
        String[] orderOptions = {
                "Nome (A-Z)",
                "Nome (Z-A)",
                "Più recenti",
                "Meno recenti",
                "Numero capi"
        };

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Ordina per")
                .setItems(orderOptions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            viewModel.setSortOrder(OutfitMenuViewModel.SortOrder.BY_NAME_ASC);
                            break;
                        case 1:
                            viewModel.setSortOrder(OutfitMenuViewModel.SortOrder.BY_NAME_DESC);
                            break;
                        case 2:
                            viewModel.setSortOrder(OutfitMenuViewModel.SortOrder.BY_DATE_NEWEST);
                            break;
                        case 3:
                            viewModel.setSortOrder(OutfitMenuViewModel.SortOrder.BY_DATE_OLDEST);
                            break;
                        case 4:
                            viewModel.setSortOrder(OutfitMenuViewModel.SortOrder.BY_GARMENT_COUNT);
                            break;
                    }
                })
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - ricarico outfit");
        if (viewModel != null) {
            viewModel.fetchOutfits();
        }
    }
}