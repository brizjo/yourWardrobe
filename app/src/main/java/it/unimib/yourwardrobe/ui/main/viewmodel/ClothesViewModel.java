package it.unimib.yourwardrobe.ui.main.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.utils.Callback;

@HiltViewModel
public class ClothesViewModel extends AndroidViewModel {

    private final GarmentRepository garmentRepository;
    private final String categoryTop;
    private final String categoryBottom;
    private final String categoryFootWear;
    private final String categoryAccessories;
    private final MutableLiveData<List<Garment>> allGarments = new MutableLiveData<>();
    private final MutableLiveData<List<Garment>> topGarments = new MutableLiveData<>();
    private final MutableLiveData<List<Garment>> bottomGarments = new MutableLiveData<>();
    private final MutableLiveData<List<Garment>> footwearGarments = new MutableLiveData<>();
    private final MutableLiveData<List<Garment>> accessories = new MutableLiveData<>();
    private final MutableLiveData<List<String>> allColors = new MutableLiveData<>();
    private final MutableLiveData<List<String>> allStyles = new MutableLiveData<>();
    private final MutableLiveData<List<String>> allFabrics = new MutableLiveData<>();
    private final MutableLiveData<List<String>> activeColorFilters = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> activeStyleFilters = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> activeFabricFilters = new MutableLiveData<>(new ArrayList<>());
    private final MediatorLiveData<Map<String, List<String>>> activeFilters = new MediatorLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isWardrobeEmpty = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);
    private final MutableLiveData<DisplayMode> displayMode = new MutableLiveData<>(DisplayMode.BY_CATEGORY);
    private final MutableLiveData<List<Garment>> gridGarments = new MutableLiveData<>();
    @Inject
    public ClothesViewModel(Application application, GarmentRepository garmentRepository) {
        super(application);
        this.garmentRepository = garmentRepository;
        this.categoryTop = application.getString(R.string.top_garment);
        this.categoryBottom = application.getString(R.string.bottom_garment);
        this.categoryFootWear = application.getString(R.string.footwear);
        this.categoryAccessories = application.getString(R.string.accessory);
        loadFilterOptions();
        fetchGarments(); // Avvia il recupero dati alla creazione
        activeFilters.addSource(activeColorFilters, colors -> updateCombinedFilters());
        activeFilters.addSource(activeStyleFilters, styles -> updateCombinedFilters());
        activeFilters.addSource(activeFabricFilters, fabrics -> updateCombinedFilters())
        ;
    }

    private void updateCombinedFilters() {
        Map<String, List<String>> combined = new HashMap<>();
        combined.put("color", activeColorFilters.getValue());
        combined.put("style", activeStyleFilters.getValue());
        combined.put("fabric", activeFabricFilters.getValue());
        activeFilters.setValue(combined);

        // Riapplica tutti i filtri quando uno cambia
        applyAllFilters();
    }

    /*
     * Recupera i colori, stili e tessuti da risorse.
     */
    private void loadFilterOptions() {
        Application app = getApplication();
        allColors.setValue(Arrays.asList(app.getResources().getStringArray(R.array.garment_color)));
        allStyles.setValue(Arrays.asList(app.getResources().getStringArray(R.array.garment_styles)));
        allFabrics.setValue(Arrays.asList(app.getResources().getStringArray(R.array.fabric_types)));
    }

    /**
     * Recupera i vestiti dal repository (che usa un real-time listener su Firestore)
     */
    public void fetchGarments() {
        isLoading.setValue(true);
        garmentRepository.getGarments(new Callback<List<Garment>>() {
            @Override
            public void onSuccess(List<Garment> data) {
                allGarments.postValue(data);
                if (data == null || data.isEmpty()) {
                    isWardrobeEmpty.postValue(true);
                    topGarments.postValue(new ArrayList<>());
                    bottomGarments.postValue(new ArrayList<>());
                    footwearGarments.postValue(new ArrayList<>());
                    accessories.postValue(new ArrayList<>());
                    gridGarments.postValue(new ArrayList<>());
                } else {
                    isWardrobeEmpty.postValue(false);
                    applyAllFilters();
                }
                filterAndPostGarments(data);
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(String error, Throwable t) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
                isWardrobeEmpty.postValue(true);
            }
        });
    }

    private void filterAndPostGarments(List<Garment> fullList) {
        if (fullList == null) {
            topGarments.postValue(new ArrayList<>());
            bottomGarments.postValue(new ArrayList<>());
            accessories.postValue(new ArrayList<>());
            footwearGarments.postValue(new ArrayList<>());
            return;
        }
        List<Garment> tops = new ArrayList<>();
        List<Garment> bottoms = new ArrayList<>();
        List<Garment> shoes = new ArrayList<>();
        List<Garment> accs = new ArrayList<>();

        for (Garment g : fullList) {
            if (g.getCategory() == null) continue;

            if (categoryTop.equalsIgnoreCase(g.getCategory())) {
                tops.add(g);
            } else if (categoryBottom.equalsIgnoreCase(g.getCategory())) {
                bottoms.add(g);
            } else if (categoryFootWear.equalsIgnoreCase(g.getCategory())) {
                shoes.add(g);
            } else if (categoryAccessories.equalsIgnoreCase(g.getCategory())) {
                accs.add(g);
            }

        }

        topGarments.postValue(tops);
        bottomGarments.postValue(bottoms);
        footwearGarments.postValue(shoes);
        accessories.postValue(accs);
    }

    public void filterByColor(List<String> selectedColors) {
        activeColorFilters.setValue(selectedColors);
    }

    public void filterByStyle(List<String> selectedStyles) {
        activeStyleFilters.setValue(selectedStyles);
    }

    public void filterByFabric(List<String> selectedFabrics) {
        activeFabricFilters.setValue(selectedFabrics);
    }

    private void applyAllFilters() {
        List<Garment> fullList = allGarments.getValue();
        if (fullList == null) return;

        List<String> colors = activeColorFilters.getValue();
        List<String> styles = activeStyleFilters.getValue();
        List<String> fabrics = activeFabricFilters.getValue();

        // Filtra la lista
        List<Garment> filteredList = fullList.stream()
                .filter(garment -> (colors == null || colors.isEmpty() || (garment.getColor() != null && !Collections.disjoint(garment.getColor(), colors))))
                .filter(garment -> (styles == null || styles.isEmpty() || (garment.getStyle() != null && !Collections.disjoint(garment.getStyle(), styles))))
                .filter(garment -> (fabrics == null || fabrics.isEmpty() || (garment.getFabric() != null && !Collections.disjoint(garment.getFabric(), fabrics))))
                .collect(Collectors.toList());

        filterAndPostGarments(filteredList);
        updateDisplayedGarments(displayMode.getValue(), filteredList);
    }

    public void resetFilters() {
        activeColorFilters.setValue(new ArrayList<>());
        activeStyleFilters.setValue(new ArrayList<>());
        activeFabricFilters.setValue(new ArrayList<>());
    }

    private void updateDisplayedGarments(DisplayMode mode, List<Garment> listToDisplay) {
        //List<Garment> fullList = allGarments.getValue();
        if (listToDisplay == null) return;

        List<Garment> listForGrid = new ArrayList<>(listToDisplay); // Lavora su una copia

        switch (mode) {
            case GRID_ALPHABETICAL:
                // Ordina per nome, ignorando maiuscole/minuscole
                listForGrid.sort(Comparator.comparing(Garment::getName, String.CASE_INSENSITIVE_ORDER));
                gridGarments.setValue(listForGrid);
                break;
            case GRID_BY_DATE:
                if (listForGrid.stream().allMatch(g -> g.getCreatedAt() != null)) {
                    listForGrid.sort(Comparator.comparing(Garment::getCreatedAt).reversed()); // reversed() per i più recenti prima
                }
                gridGarments.setValue(listForGrid);
                break;
            case BY_CATEGORY:
            default:
                // La logica di filtraggio per categoria rimane la stessa
                gridGarments.setValue(new ArrayList<>());
                break;
        }
    }

    private void updateDisplayedGarments(DisplayMode mode) {
        // Quando la modalità viene cambiata, riapplica i filtri esistenti
        applyAllFilters();
    }

    public LiveData<Boolean> getIsWardrobeEmpty() {
        return isWardrobeEmpty;
    }

    public LiveData<DisplayMode> getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode mode) {
        if (this.displayMode.getValue() == mode)
            return; // Non fare nulla se la modalità è già quella

        this.displayMode.setValue(mode);
        updateDisplayedGarments(mode);
    }

    public LiveData<List<Garment>> getGridGarments() {
        return gridGarments;
    }

    public LiveData<Map<String, List<String>>> getActiveFilters() {
        return activeFilters;
    }

    public LiveData<List<Garment>> getAllGarments() {
        return allGarments;
    }

    public LiveData<List<Garment>> getTopGarments() {
        return topGarments;
    }

    public LiveData<List<Garment>> getBottomGarments() {
        return bottomGarments;
    }

    public LiveData<List<Garment>> getFootwearGarments() {
        return footwearGarments;
    }

    public LiveData<List<Garment>> getAccessories() {
        return accessories;
    }

    public LiveData<List<String>> getAllColors() {
        return allColors;
    }

    public LiveData<List<String>> getAllStyles() {
        return allStyles;
    }

    public LiveData<List<String>> getAllFabrics() {
        return allFabrics;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public enum DisplayMode {
        BY_CATEGORY,
        GRID_ALPHABETICAL,
        GRID_BY_DATE
    }


    /**
     * Helper per filtrare i vestiti per categoria specifica.
     * Utile per popolare le diverse RecyclerView del ClothesFragment.
     */
//    public List<Garment> filterByCategory(List<Garment> fullList, String category) {
//        List<Garment> filtered = new ArrayList<>();
//        if (fullList == null) return filtered;
//
//        for (Garment g : fullList) {
//            if (category.equalsIgnoreCase(g.getCategory())) {
//                filtered.add(g);
//            }
//        }
//        return filtered;
//    }

}