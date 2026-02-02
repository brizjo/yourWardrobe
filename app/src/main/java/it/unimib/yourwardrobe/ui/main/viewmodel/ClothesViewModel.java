package it.unimib.yourwardrobe.ui.main.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.R;

public class ClothesViewModel extends AndroidViewModel {

    private final GarmentRepository garmentRepository;

    // LiveData per la lista completa e lo stato
    private final MutableLiveData<List<Garment>> allGarments = new MutableLiveData<>();
    private final MutableLiveData<List<Garment>> topGarments = new MutableLiveData<>();
    private final MutableLiveData<List<Garment>> bottomGarments = new MutableLiveData<>();
    private final MutableLiveData<List<Garment>> accessories = new MutableLiveData<>();
    private final MutableLiveData<List<String>> allColors = new MutableLiveData<>();
    private final MutableLiveData<List<String>> allStyles = new MutableLiveData<>();
    private final MutableLiveData<List<String>> allFabrics = new MutableLiveData<>();

    private final MutableLiveData<List<String>> activeColorFilters = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> activeStyleFilters = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> activeFabricFilters = new MutableLiveData<>(new ArrayList<>());

    private final MediatorLiveData<Map<String, List<String>>> activeFilters = new MediatorLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public ClothesViewModel(Application application, GarmentRepository garmentRepository) {
        super(application);
        this.garmentRepository = garmentRepository;
        loadFilterOptions();
        fetchGarments(); // Avvia il recupero dati alla creazione
        activeFilters.addSource(activeColorFilters, colors -> updateCombinedFilters());
        activeFilters.addSource(activeStyleFilters, styles -> updateCombinedFilters());
        activeFilters.addSource(activeFabricFilters, fabrics -> updateCombinedFilters())
;    }

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
                filterAndPostGarments(data);
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(String error, Throwable t) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });
    }

    private void filterAndPostGarments(List<Garment> fullList) {
        if (fullList == null) {
            topGarments.postValue(new ArrayList<>());
            bottomGarments.postValue(new ArrayList<>());
            accessories.postValue(new ArrayList<>());
            return;
        }
        List<Garment> tops = new ArrayList<>();
        List<Garment> bottoms = new ArrayList<>();
        List<Garment> accs = new ArrayList<>();

        for (Garment g : fullList) {
            if (g.getCategory() == null) continue;

            if ("Parte Superiore".equalsIgnoreCase(g.getCategory())) {
                tops.add(g);
            } else if ("Parte Inferiore".equalsIgnoreCase(g.getCategory())) {
                bottoms.add(g);
            } else if ("Accessori".equalsIgnoreCase(g.getCategory())) {
                accs.add(g);
            }
        }

        // Aggiorna i LiveData specifici
        topGarments.postValue(tops);
        bottomGarments.postValue(bottoms);
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
    }

    public void resetFilters() {
        activeColorFilters.setValue(new ArrayList<>());
        activeStyleFilters.setValue(new ArrayList<>());
        activeFabricFilters.setValue(new ArrayList<>());
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