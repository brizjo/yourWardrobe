package it.unimib.yourwardrobe.ui.main.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.model.Outfit;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.domain.repository.OutfitRepository;
import it.unimib.yourwardrobe.utils.Callback;

@HiltViewModel
public class OutfitMenuViewModel extends ViewModel {

    private static final String TAG = "OutfitMenuViewModel";
    private final Context context;
    private final OutfitRepository outfitRepository;
    private final GarmentRepository garmentRepository;
    private final MutableLiveData<Map<String, List<String>>> activeFilters = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<List<Outfit>> outfits = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<UiState> uiState = new MutableLiveData<>(UiState.LOADING);
    private List<Outfit> allOutfits = new ArrayList<>();
    private SortOrder currentSortOrder = SortOrder.BY_NAME_ASC;
    @Inject
    public OutfitMenuViewModel(@ApplicationContext Context context, OutfitRepository outfitRepository, GarmentRepository garmentRepository) {
        this.context = context;
        this.outfitRepository = outfitRepository;
        this.garmentRepository = garmentRepository;
        checkDataAndFetchOutfits();
    }

    private void checkDataAndFetchOutfits() {
        uiState.setValue(UiState.LOADING);

        garmentRepository.getGarments(new Callback<List<Garment>>() {
            @Override
            public void onSuccess(List<Garment> garments) {
                boolean hasTop = garments.stream().anyMatch(g -> "Parte superiore".equals(g.getCategory()));
                boolean hasBottom = garments.stream().anyMatch(g -> "Parte inferiore".equals(g.getCategory()));

                if (hasTop && hasBottom) {
                    fetchOutfits();
                } else {
                    uiState.postValue(UiState.NOT_ENOUGH_GARMENTS);
                }
            }

            @Override
            public void onFailure(String error, Throwable t) {
                errorMessage.postValue(error);
                uiState.postValue(UiState.NOT_ENOUGH_GARMENTS); // Gestisci l'errore come se non ci fossero capi
            }
        });
    }

    public void fetchOutfits() {
        Log.d(TAG, "=== FETCH OUTFITS ===");
        outfitRepository.getOutfits(new Callback<List<Outfit>>() {
            @Override
            public void onSuccess(List<Outfit> result) {
                outfits.postValue(result);
                if (result == null || result.isEmpty()) {
                    uiState.postValue(UiState.NO_OUTFITS);
                } else {
                    uiState.postValue(UiState.HAS_OUTFITS);
                    allOutfits = result;
                    applyFiltersAndSort();
                }
            }

            @Override
            public void onFailure(String error, Throwable t) {
                Log.e(TAG, "❌ Errore: " + error, t);
                errorMessage.postValue(error);
                uiState.postValue(UiState.NO_OUTFITS);
            }
        });
    }

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

    // =========================================================================
    // Metodi pubblici per filtri
    // =========================================================================

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

    public LiveData<List<Outfit>> getOutfits() {
        return outfits;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    public LiveData<Map<String, List<String>>> getActiveFilters() {
        return activeFilters;
    }

    public List<String> getAllStyles() {
        //return List.of("Casual", "Elegante", "Sportivo", "Formale", "Streetwear", "Business", "Boho", "Vintage");
        return Arrays.asList(context.getResources().getStringArray(R.array.garment_styles));
    }

    public List<String> getAllSeasons() {
        return Arrays.asList(context.getResources().getStringArray(R.array.seasons));
    }

    // =========================================================================
    // Metodi per ottenere tutte le opzioni disponibili (per i dialog)
    // =========================================================================

    public List<String> getAllColors() {
        return Arrays.asList(context.getResources().getStringArray(R.array.garment_color));
    }

    public enum UiState {
        LOADING,
        NOT_ENOUGH_GARMENTS,
        NO_OUTFITS,
        HAS_OUTFITS
    }

    public enum SortOrder {
        BY_NAME_ASC,
        BY_NAME_DESC,
        BY_DATE_NEWEST,
        BY_DATE_OLDEST,
        BY_GARMENT_COUNT
    }
}
