package it.unimib.yourwardrobe.ui.main.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.model.Outfit;
import it.unimib.yourwardrobe.domain.repository.OutfitRepository;

@HiltViewModel
public class OutfitMenuViewModel extends ViewModel {

    private static final String TAG = "OutfitMenuViewModel";
    private final OutfitRepository outfitRepository;

    private final MutableLiveData<List<Outfit>> outfits = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Map<String, List<String>>> activeFilters = new MutableLiveData<>(new HashMap<>());

    private List<Outfit> allOutfits = new ArrayList<>();
    private SortOrder currentSortOrder = SortOrder.BY_NAME_ASC;

    public enum SortOrder {
        BY_NAME_ASC,
        BY_NAME_DESC,
        BY_DATE_NEWEST,
        BY_DATE_OLDEST,
        BY_GARMENT_COUNT
    }

    @Inject
    public OutfitMenuViewModel(OutfitRepository outfitRepository) {
        this.outfitRepository = outfitRepository;
        fetchOutfits();
    }

    public void fetchOutfits() {
        Log.d(TAG, "=== FETCH OUTFITS ===");
        outfitRepository.getOutfits(new Callback<List<Outfit>>() {
            @Override
            public void onSuccess(List<Outfit> result) {
                Log.d(TAG, "✅ Ricevuti " + result.size() + " outfit");
                allOutfits = result;
                applyFiltersAndSort();
            }

            @Override
            public void onFailure(String error, Throwable t) {
                Log.e(TAG, "❌ Errore: " + error, t);
                errorMessage.postValue(error);
            }
        });
    }

    /**
     * Applica filtri e ordinamento sugli outfit
     */
    private void applyFiltersAndSort() {
        Log.d(TAG, "=== APPLY FILTERS AND SORT ===");

        List<Outfit> filteredOutfits = new ArrayList<>(allOutfits);
        Map<String, List<String>> filters = activeFilters.getValue();

        if (filters != null) {
            // Filtra per stili
            List<String> styleFilters = filters.get("style");
            if (styleFilters != null && !styleFilters.isEmpty()) {
                Log.d(TAG, "Filtraggio per stili: " + styleFilters);
                filteredOutfits = filterByStyles(filteredOutfits, styleFilters);
            }

            // Filtra per stagioni
            List<String> seasonFilters = filters.get("season");
            if (seasonFilters != null && !seasonFilters.isEmpty()) {
                Log.d(TAG, "Filtraggio per stagioni: " + seasonFilters);
                filteredOutfits = filterBySeasons(filteredOutfits, seasonFilters);
            }

            // Filtra per colori
            List<String> colorFilters = filters.get("color");
            if (colorFilters != null && !colorFilters.isEmpty()) {
                Log.d(TAG, "Filtraggio per colori: " + colorFilters);
                filteredOutfits = filterByColors(filteredOutfits, colorFilters);
            }
        }

        // Ordina
        sortOutfits(filteredOutfits, currentSortOrder);

        Log.d(TAG, "Outfit filtrati e ordinati: " + filteredOutfits.size());
        outfits.postValue(filteredOutfits);
    }

    /**
     * Filtra outfit che contengono almeno un capo con uno degli stili selezionati
     */
    private List<Outfit> filterByStyles(List<Outfit> outfitList, List<String> styles) {
        List<Outfit> result = new ArrayList<>();
        for (Outfit outfit : outfitList) {
            if (outfit.getGarments() != null) {
                for (Garment garment : outfit.getGarments()) {
                    if (garment.getStyle() != null) {
                        for (String style : styles) {
                            if (garment.getStyle().contains(style)) {
                                result.add(outfit);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Filtra outfit che contengono almeno un capo con una delle stagioni selezionate
     */
    private List<Outfit> filterBySeasons(List<Outfit> outfitList, List<String> seasons) {
        List<Outfit> result = new ArrayList<>();
        for (Outfit outfit : outfitList) {
            String outfitSeason = outfit.getSeason(); // ← Usa la stagione dell'outfit
            if (outfitSeason != null && seasons.contains(outfitSeason)) {
                result.add(outfit);
            }
        }
        return result;
    }

    /**
     * Filtra outfit che contengono almeno un capo con uno dei colori selezionati
     */
    private List<Outfit> filterByColors(List<Outfit> outfitList, List<String> colors) {
        List<Outfit> result = new ArrayList<>();
        for (Outfit outfit : outfitList) {
            if (outfit.getGarments() != null) {
                for (Garment garment : outfit.getGarments()) {
                    if (garment.getColor() != null) {
                        for (String color : colors) {
                            if (garment.getColor().contains(color)) {
                                result.add(outfit);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Ordina gli outfit secondo il criterio specificato
     */
    private void sortOutfits(List<Outfit> outfitList, SortOrder order) {
        switch (order) {
            case BY_NAME_ASC:
                Collections.sort(outfitList, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
                break;
            case BY_NAME_DESC:
                Collections.sort(outfitList, (o1, o2) -> o2.getName().compareToIgnoreCase(o1.getName()));
                break;
            case BY_DATE_NEWEST:
                Collections.sort(outfitList, (o1, o2) -> {
                    if (o1.getCreatedAt() == null) return 1;
                    if (o2.getCreatedAt() == null) return -1;
                    return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                });
                break;
            case BY_DATE_OLDEST:
                Collections.sort(outfitList, (o1, o2) -> {
                    if (o1.getCreatedAt() == null) return 1;
                    if (o2.getCreatedAt() == null) return -1;
                    return o1.getCreatedAt().compareTo(o2.getCreatedAt());
                });
                break;
            case BY_GARMENT_COUNT:
                Collections.sort(outfitList, (o1, o2) -> {
                    int count1 = o1.getGarments() != null ? o1.getGarments().size() : 0;
                    int count2 = o2.getGarments() != null ? o2.getGarments().size() : 0;
                    return Integer.compare(count2, count1); // Decrescente
                });
                break;
        }
    }

    // =========================================================================
    // Metodi pubblici per filtri
    // =========================================================================

    public void filterByStyle(List<String> styles) {
        Log.d(TAG, "Filtro per stili: " + styles);
        Map<String, List<String>> filters = activeFilters.getValue();
        if (filters == null) filters = new HashMap<>();
        filters.put("style", styles);
        activeFilters.setValue(filters);
        applyFiltersAndSort();
    }

    public void filterBySeason(List<String> seasons) {
        Log.d(TAG, "Filtro per stagioni: " + seasons);
        Map<String, List<String>> filters = activeFilters.getValue();
        if (filters == null) filters = new HashMap<>();
        filters.put("season", seasons);
        activeFilters.setValue(filters);
        applyFiltersAndSort();
    }

    public void filterByColor(List<String> colors) {
        Log.d(TAG, "Filtro per colori: " + colors);
        Map<String, List<String>> filters = activeFilters.getValue();
        if (filters == null) filters = new HashMap<>();
        filters.put("color", colors);
        activeFilters.setValue(filters);
        applyFiltersAndSort();
    }

    public void setSortOrder(SortOrder order) {
        Log.d(TAG, "Ordinamento: " + order);
        this.currentSortOrder = order;
        applyFiltersAndSort();
    }

    public void clearFilters() {
        Log.d(TAG, "Cancello tutti i filtri");
        activeFilters.setValue(new HashMap<>());
        applyFiltersAndSort();
    }

    // =========================================================================
    // LiveData getters
    // =========================================================================

    public LiveData<List<Outfit>> getOutfits() {
        return outfits;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Map<String, List<String>>> getActiveFilters() {
        return activeFilters;
    }

    // =========================================================================
    // Metodi per ottenere tutte le opzioni disponibili (per i dialog)
    // =========================================================================

    public List<String> getAllStyles() {
        return List.of("Casual", "Elegante", "Sportivo", "Formale", "Streetwear", "Business", "Boho", "Vintage");
    }

    public List<String> getAllSeasons() {
        return List.of("Tutte le stagioni", "Primavera", "Estate", "Autunno", "Inverno",
                "Inverno - Autunno", "Primavera - Estate", "Primavera - Autunno");
    }

    public List<String> getAllColors() {
        return List.of("Nero", "Bianco", "Grigio", "Blu", "Rosso", "Verde", "Beige", "Marrone", "Rosa",
                "Giallo", "Arancione", "Viola", "Blu scuro");
    }
}