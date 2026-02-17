package it.unimib.yourwardrobe.ui.main.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.model.Outfit;
import it.unimib.yourwardrobe.domain.model.UserPreferences;
import it.unimib.yourwardrobe.domain.model.WeatherInfo;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.domain.repository.OutfitRepository;
import it.unimib.yourwardrobe.domain.repository.ProfileRepository;
import it.unimib.yourwardrobe.domain.repository.WeatherRepository;
import it.unimib.yourwardrobe.utils.WeatherUtil;

@HiltViewModel
public class PersonalStylistViewModel extends ViewModel {

    private static final String NOT_SELECTED = "Non selezionato";
    private static final double PREFERENCE_BOOST = 2.5; // Moltiplicatore per outfit con preferenze

    private final GarmentRepository garmentRepository;
    private final OutfitRepository outfitRepository;
    private final WeatherRepository weatherRepository;
    private final ProfileRepository profileRepository;

    private final MutableLiveData<WeatherInfo> currentWeather = new MutableLiveData<>();
    private final MutableLiveData<String> suggestedSeason = new MutableLiveData<>();
    private final MutableLiveData<List<String>> availableColors = new MutableLiveData<>();
    private final MutableLiveData<List<String>> availableStyles = new MutableLiveData<>();
    private final MutableLiveData<List<String>> availableSeasons = new MutableLiveData<>();
    private final MutableLiveData<List<Garment>> generatedOutfitGarments = new MutableLiveData<>();
    private final MutableLiveData<Boolean> outfitSaved = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private List<Garment> allGarments = new ArrayList<>();
    private double currentTemperature = 20.0;

    // Preferenze utente
    private List<String> userFavoriteColors = new ArrayList<>();
    private List<String> userFavoriteStyles = new ArrayList<>();

    private final Map<String, Set<String>> colorHarmony = new HashMap<>();
    private final Map<String, Set<String>> styleCompatibility = new HashMap<>();
    private final Map<String, Set<String>> seasonCompatibility = new HashMap<>();

    @Inject
    public PersonalStylistViewModel(
            GarmentRepository garmentRepository,
            OutfitRepository outfitRepository,
            WeatherRepository weatherRepository,
            ProfileRepository profileRepository) {
        this.garmentRepository = garmentRepository;
        this.outfitRepository = outfitRepository;
        this.weatherRepository = weatherRepository;
        this.profileRepository = profileRepository;

        initializeColorHarmony();
        initializeStyleCompatibility();
        initializeSeasonCompatibility();
        loadFilters();
        loadGarments();
        loadUserPreferences();
    }

    // -------------------------------------------------------------------------
    // User Preferences
    // -------------------------------------------------------------------------

    private void loadUserPreferences() {
        profileRepository.getUserPreferences().observeForever(prefs -> {
            if (prefs != null) {
                userFavoriteColors = prefs.getFavoriteColors() != null
                        ? new ArrayList<>(prefs.getFavoriteColors())
                        : new ArrayList<>();
                userFavoriteStyles = prefs.getFavoriteStyles() != null
                        ? new ArrayList<>(prefs.getFavoriteStyles())
                        : new ArrayList<>();

                android.util.Log.d("PersonalStylistVM",
                        "Preferenze caricate - Colori: " + userFavoriteColors.size() +
                                ", Stili: " + userFavoriteStyles.size());
            }
        });
    }

    /**
     * Calcola un punteggio per l'outfit in base alle preferenze dell'utente.
     * Più colori/stili preferiti sono presenti, più alto è il punteggio.
     */
    private double calculatePreferenceScore(List<Garment> outfit) {
        double score = 1.0;
        int matchingColors = 0;
        int matchingStyles = 0;

        // Conta quanti colori preferiti sono nell'outfit
        for (Garment garment : outfit) {
            if (garment.getColor() != null) {
                for (String color : garment.getColor()) {
                    if (userFavoriteColors.contains(color)) {
                        matchingColors++;
                    }
                }
            }
            if (garment.getStyle() != null) {
                for (String style : garment.getStyle()) {
                    if (userFavoriteStyles.contains(style)) {
                        matchingStyles++;
                    }
                }
            }
        }

        // Boost del punteggio in base ai match
        if (matchingColors > 0) {
            score *= (1.0 + (matchingColors * 0.3)); // +30% per ogni colore preferito
        }
        if (matchingStyles > 0) {
            score *= (1.0 + (matchingStyles * 0.4)); // +40% per ogni stile preferito
        }

        return score;
    }

    // -------------------------------------------------------------------------
    // Initialization
    // -------------------------------------------------------------------------

    private void initializeColorHarmony() {
        colorHarmony.put("Nero",    new HashSet<>(Arrays.asList("Bianco", "Grigio", "Rosso", "Blu", "Beige", "Nero")));
        colorHarmony.put("Bianco",  new HashSet<>(Arrays.asList("Nero", "Blu", "Grigio", "Rosso", "Verde", "Beige", "Bianco")));
        colorHarmony.put("Grigio",  new HashSet<>(Arrays.asList("Nero", "Bianco", "Blu", "Rosso", "Rosa", "Grigio")));
        colorHarmony.put("Blu",     new HashSet<>(Arrays.asList("Bianco", "Grigio", "Beige", "Nero", "Marrone", "Blu")));
        colorHarmony.put("Rosso",   new HashSet<>(Arrays.asList("Nero", "Bianco", "Grigio", "Blu scuro", "Rosso")));
        colorHarmony.put("Verde",   new HashSet<>(Arrays.asList("Bianco", "Beige", "Marrone", "Nero", "Verde")));
        colorHarmony.put("Beige",   new HashSet<>(Arrays.asList("Bianco", "Marrone", "Blu", "Verde", "Nero", "Beige")));
        colorHarmony.put("Marrone", new HashSet<>(Arrays.asList("Beige", "Verde", "Bianco", "Blu", "Marrone")));
        colorHarmony.put("Rosa",    new HashSet<>(Arrays.asList("Grigio", "Bianco", "Nero", "Blu", "Rosa")));
    }

    private void initializeStyleCompatibility() {
        styleCompatibility.put("Casual",     new HashSet<>(Arrays.asList("Casual", "Sportivo", "Streetwear")));
        styleCompatibility.put("Elegante",   new HashSet<>(Arrays.asList("Elegante", "Formale", "Business")));
        styleCompatibility.put("Sportivo",   new HashSet<>(Arrays.asList("Sportivo", "Casual", "Streetwear")));
        styleCompatibility.put("Formale",    new HashSet<>(Arrays.asList("Formale", "Elegante", "Business")));
        styleCompatibility.put("Streetwear", new HashSet<>(Arrays.asList("Streetwear", "Casual", "Sportivo")));
        styleCompatibility.put("Business",   new HashSet<>(Arrays.asList("Business", "Formale", "Elegante")));
        styleCompatibility.put("Boho",       new HashSet<>(Arrays.asList("Boho", "Casual")));
        styleCompatibility.put("Vintage",    new HashSet<>(Arrays.asList("Vintage", "Casual", "Elegante")));
    }

    private void initializeSeasonCompatibility() {
        seasonCompatibility.put("Primavera", new HashSet<>(Arrays.asList(
                "Primavera", "Primavera - Estate", "Primavera - Autunno", "Tutte le stagioni")));
        seasonCompatibility.put("Estate", new HashSet<>(Arrays.asList(
                "Estate", "Primavera - Estate", "Tutte le stagioni")));
        seasonCompatibility.put("Autunno", new HashSet<>(Arrays.asList(
                "Autunno", "Inverno - Autunno", "Primavera - Autunno", "Tutte le stagioni")));
        seasonCompatibility.put("Inverno", new HashSet<>(Arrays.asList(
                "Inverno", "Inverno - Autunno", "Tutte le stagioni")));
    }

    private void loadFilters() {
        List<String> colors = new ArrayList<>();
        colors.add(NOT_SELECTED);
        colors.addAll(Arrays.asList("Rosso", "Blu", "Verde", "Nero", "Bianco", "Grigio", "Marrone", "Beige", "Rosa"));
        availableColors.setValue(colors);

        List<String> styles = new ArrayList<>();
        styles.add(NOT_SELECTED);
        styles.addAll(Arrays.asList("Casual", "Elegante", "Sportivo", "Formale", "Streetwear", "Business", "Boho", "Vintage"));
        availableStyles.setValue(styles);

        availableSeasons.setValue(Arrays.asList("Primavera", "Estate", "Autunno", "Inverno"));
    }

    private void loadGarments() {
        garmentRepository.getGarments(new Callback<List<Garment>>() {
            @Override
            public void onSuccess(List<Garment> garments) {
                allGarments = garments;
                android.util.Log.d("PersonalStylistVM", "Caricati " + garments.size() + " capi");
            }
            @Override
            public void onFailure(String error, Throwable t) {
                errorMessage.postValue("Errore caricamento capi: " + error);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Weather
    // -------------------------------------------------------------------------

    public void fetchWeather(double lat, double lon) {
        isLoading.setValue(true);
        weatherRepository.getCurrentWeather(lat, lon, new Callback<WeatherInfo>() {
            @Override
            public void onSuccess(WeatherInfo weather) {
                currentWeather.postValue(weather);
                String tempStr = weather.getTemperature().replace("°C", "");
                try {
                    currentTemperature = Double.parseDouble(tempStr);
                    String season = WeatherUtil.getSeasonFromTemperature(currentTemperature);
                    suggestedSeason.postValue(season);
                } catch (NumberFormatException e) {
                    currentTemperature = 20.0;
                }
                isLoading.postValue(false);
            }
            @Override
            public void onFailure(String error, Throwable t) {
                errorMessage.postValue("Errore meteo: " + error);
                suggestedSeason.postValue(WeatherUtil.getSeasonFromTemperature(20.0));
                isLoading.postValue(false);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Season helpers
    // -------------------------------------------------------------------------

    private boolean garmentMatchesSeason(Garment garment, String filterSeason) {
        if (filterSeason == null || filterSeason.isEmpty()) return true;
        String garmentSeason = garment.getSeason();
        if (garmentSeason == null || garmentSeason.isEmpty()) return true;

        Set<String> compatible = seasonCompatibility.get(filterSeason);
        if (compatible == null) return true;

        return compatible.contains(garmentSeason);
    }

    private boolean isWarmSeason(String season) {
        return "Estate".equals(season) || "Primavera".equals(season);
    }

    // -------------------------------------------------------------------------
    // Outfit generation
    // -------------------------------------------------------------------------

    public void generateOutfit(String filterSeason, String filterColor, String filterStyle) {
        android.util.Log.d("PersonalStylistVM", "=== GENERAZIONE OUTFIT ===");
        android.util.Log.d("PersonalStylistVM", "Stagione: " + filterSeason);
        android.util.Log.d("PersonalStylistVM", "Colore: " + filterColor);
        android.util.Log.d("PersonalStylistVM", "Stile: " + filterStyle);
        android.util.Log.d("PersonalStylistVM", "Preferenze - Colori: " + userFavoriteColors + ", Stili: " + userFavoriteStyles);

        if (allGarments.isEmpty()) {
            errorMessage.setValue("Nessun capo disponibile nel guardaroba");
            return;
        }

        String actualColor = NOT_SELECTED.equals(filterColor) ? null : filterColor;
        String actualStyle = NOT_SELECTED.equals(filterStyle) ? null : filterStyle;

        List<Garment> tops = filterByCategoryAndSeason(allGarments, "Parte superiore", filterSeason);
        List<Garment> bottoms = filterByCategoryAndSeason(allGarments, "Parte inferiore", filterSeason);

        List<Garment> shoes;
        if (isWarmSeason(filterSeason)) {
            shoes = filterByCategoryAndSeason(allGarments, "Calzature", filterSeason);
        } else {
            shoes = filterByCategory(allGarments, "Calzature");
        }

        List<Garment> accessories;
        if (isWarmSeason(filterSeason)) {
            accessories = filterByCategoryAndSeason(allGarments, "Accessori", filterSeason);
        } else {
            accessories = filterByCategory(allGarments, "Accessorio");
        }

        android.util.Log.d("PersonalStylistVM", "Pool - Top: " + tops.size() + ", Bottom: " + bottoms.size() +
                ", Scarpe: " + shoes.size() + ", Accessori: " + accessories.size());

        if (tops.isEmpty() || bottoms.isEmpty()) {
            errorMessage.setValue("Non hai abbastanza capi per generare un outfit (servono almeno 1 top e 1 bottom)");
            return;
        }

        List<List<Garment>> allPossibleOutfits = generateAllPossibleOutfits(tops, bottoms, shoes, accessories);
        android.util.Log.d("PersonalStylistVM", "Outfit possibili: " + allPossibleOutfits.size());

        List<List<Garment>> validOutfits = filterOutfitsByConstraints(allPossibleOutfits, actualColor, actualStyle);
        android.util.Log.d("PersonalStylistVM", "Outfit validi dopo filtri: " + validOutfits.size());

        if (validOutfits.isEmpty()) {
            errorMessage.setValue("Nessun outfit armonioso trovato con i filtri selezionati. Prova a rimuovere qualche filtro.");
            return;
        }

        // SELEZIONE OUTFIT CON PREFERENZE
        List<Garment> selectedOutfit = selectOutfitWithPreferences(validOutfits, actualColor, actualStyle);

        android.util.Log.d("PersonalStylistVM", "Outfit selezionato con " + selectedOutfit.size() + " capi");
        generatedOutfitGarments.setValue(selectedOutfit);
    }

    /**
     * Seleziona un outfit dando priorità a quelli con colori/stili preferiti
     * quando non ci sono filtri specifici.
     */
    private List<Garment> selectOutfitWithPreferences(List<List<Garment>> validOutfits,
                                                      String filterColor, String filterStyle) {
        // Se ci sono filtri espliciti, selezione casuale normale
        if (filterColor != null || filterStyle != null) {
            Random random = new Random();
            return validOutfits.get(random.nextInt(validOutfits.size()));
        }

        // Nessun filtro → usa preferenze per pesare la selezione
        if (userFavoriteColors.isEmpty() && userFavoriteStyles.isEmpty()) {
            // Nessuna preferenza salvata, selezione casuale
            Random random = new Random();
            return validOutfits.get(random.nextInt(validOutfits.size()));
        }

        // Calcola punteggi per tutti gli outfit
        List<OutfitScore> scoredOutfits = new ArrayList<>();
        for (List<Garment> outfit : validOutfits) {
            double score = calculatePreferenceScore(outfit);
            scoredOutfits.add(new OutfitScore(outfit, score));
        }

        // Ordina per punteggio decrescente
        scoredOutfits.sort((a, b) -> Double.compare(b.score, a.score));

        // Log top 5 outfit
        android.util.Log.d("PersonalStylistVM", "=== TOP 5 OUTFIT PER PUNTEGGIO PREFERENZE ===");
        for (int i = 0; i < Math.min(5, scoredOutfits.size()); i++) {
            android.util.Log.d("PersonalStylistVM", "#" + (i+1) + " Score: " + scoredOutfits.get(i).score);
        }

        // Selezione pesata: 70% top 20%, 30% resto
        Random random = new Random();
        int topCount = Math.max(1, (int)(validOutfits.size() * 0.2));

        if (random.nextDouble() < 0.7) {
            // Scegli da top 20%
            int index = random.nextInt(Math.min(topCount, scoredOutfits.size()));
            android.util.Log.d("PersonalStylistVM", "Selezionato outfit da TOP 20% (index " + index + ")");
            return scoredOutfits.get(index).outfit;
        } else {
            // Scegli dal resto
            int index = random.nextInt(scoredOutfits.size());
            android.util.Log.d("PersonalStylistVM", "Selezionato outfit casuale (index " + index + ")");
            return scoredOutfits.get(index).outfit;
        }
    }

    /**
     * Classe helper per associare outfit e punteggio
     */
    private static class OutfitScore {
        List<Garment> outfit;
        double score;

        OutfitScore(List<Garment> outfit, double score) {
            this.outfit = outfit;
            this.score = score;
        }
    }

    // -------------------------------------------------------------------------
    // Combinazioni outfit (resto del codice invariato)
    // -------------------------------------------------------------------------

    private List<List<Garment>> generateAllPossibleOutfits(
            List<Garment> tops, List<Garment> bottoms,
            List<Garment> shoes, List<Garment> accessories) {

        List<List<Garment>> allOutfits = new ArrayList<>();

        List<List<Garment>> topCombinations = new ArrayList<>();
        for (int i = 0; i < tops.size(); i++) {
            List<Garment> singleTop = new ArrayList<>();
            singleTop.add(tops.get(i));
            topCombinations.add(singleTop);
            for (int j = i + 1; j < tops.size() && j < i + 5; j++) {
                List<Garment> doubleTops = new ArrayList<>();
                doubleTops.add(tops.get(i));
                doubleTops.add(tops.get(j));
                topCombinations.add(doubleTops);
            }
        }

        List<List<Garment>> bottomCombinations = new ArrayList<>();
        for (Garment bottom : bottoms) {
            List<Garment> singleBottom = new ArrayList<>();
            singleBottom.add(bottom);
            bottomCombinations.add(singleBottom);
        }

        List<List<Garment>> shoesCombinations = new ArrayList<>();
        shoesCombinations.add(new ArrayList<>());
        for (Garment shoe : shoes) {
            List<Garment> singleShoe = new ArrayList<>();
            singleShoe.add(shoe);
            shoesCombinations.add(singleShoe);
        }

        List<List<Garment>> accessoryCombinations = new ArrayList<>();

        for (int i = 0; i < accessories.size(); i++) {
            List<Garment> single = new ArrayList<>();
            single.add(accessories.get(i));
            accessoryCombinations.add(single);

            for (int j = i + 1; j < accessories.size() && j < i + 4; j++) {
                List<Garment> pair = new ArrayList<>();
                pair.add(accessories.get(i));
                pair.add(accessories.get(j));
                accessoryCombinations.add(pair);
            }

            for (int j = i + 1; j < accessories.size() && j < i + 3; j++) {
                for (int k = j + 1; k < accessories.size() && k < j + 2; k++) {
                    List<Garment> triple = new ArrayList<>();
                    triple.add(accessories.get(i));
                    triple.add(accessories.get(j));
                    triple.add(accessories.get(k));
                    accessoryCombinations.add(triple);
                }
            }

            for (int j = i + 1; j < accessories.size() && j < i + 2; j++) {
                for (int k = j + 1; k < accessories.size() && k < j + 2; k++) {
                    for (int l = k + 1; l < accessories.size() && l < k + 2; l++) {
                        List<Garment> quad = new ArrayList<>();
                        quad.add(accessories.get(i));
                        quad.add(accessories.get(j));
                        quad.add(accessories.get(k));
                        quad.add(accessories.get(l));
                        accessoryCombinations.add(quad);
                    }
                }
            }
        }

        accessoryCombinations.add(new ArrayList<>());

        for (List<Garment> topCombo : topCombinations) {
            if (topCombo.size() > 2) continue;
            for (List<Garment> bottomCombo : bottomCombinations) {
                if (bottomCombo.size() > 1) continue;
                for (List<Garment> shoesCombo : shoesCombinations) {
                    if (shoesCombo.size() > 1) continue;
                    for (List<Garment> accessoryCombo : accessoryCombinations) {
                        if (accessoryCombo.size() > 4) continue;
                        List<Garment> outfit = new ArrayList<>();
                        outfit.addAll(topCombo);
                        outfit.addAll(bottomCombo);
                        outfit.addAll(shoesCombo);
                        outfit.addAll(accessoryCombo);
                        allOutfits.add(outfit);
                    }
                }
            }
        }

        return allOutfits;
    }

    private List<List<Garment>> filterOutfitsByConstraints(
            List<List<Garment>> outfits, String filterColor, String filterStyle) {

        List<List<Garment>> validOutfits = new ArrayList<>();
        for (List<Garment> outfit : outfits) {
            if (!isColorHarmonious(outfit)) continue;
            if (!isStyleCompatible(outfit)) continue;
            if (!hasUniqueSubCategories(outfit)) continue;

            boolean meetsColor = filterColor == null || filterColor.isEmpty() || outfitHasColor(outfit, filterColor);
            boolean meetsStyle = filterStyle == null || filterStyle.isEmpty() || outfitHasStyle(outfit, filterStyle);

            if (meetsColor && meetsStyle) validOutfits.add(outfit);
        }
        return validOutfits;
    }

    private List<Garment> filterByCategory(List<Garment> garments, String category) {
        List<Garment> result = new ArrayList<>();
        for (Garment g : garments) {
            if (category.equals(g.getCategory())) result.add(g);
        }
        return result;
    }

    private List<Garment> filterByCategoryAndSeason(List<Garment> garments, String category, String filterSeason) {
        List<Garment> result = new ArrayList<>();
        for (Garment g : garments) {
            if (category.equals(g.getCategory()) && garmentMatchesSeason(g, filterSeason)) {
                result.add(g);
            }
        }
        return result;
    }

    private boolean isColorHarmonious(List<Garment> outfit) {
        List<String> outfitColors = new ArrayList<>();
        for (Garment garment : outfit) {
            if (garment.getColor() != null) outfitColors.addAll(garment.getColor());
        }
        if (outfitColors.isEmpty()) return true;
        for (int i = 0; i < outfitColors.size(); i++) {
            for (int j = i + 1; j < outfitColors.size(); j++) {
                String c1 = outfitColors.get(i);
                String c2 = outfitColors.get(j);
                Set<String> h1 = colorHarmony.get(c1);
                Set<String> h2 = colorHarmony.get(c2);
                if (h1 == null || h2 == null) continue;
                if (!h1.contains(c2) && !h2.contains(c1)) return false;
            }
        }
        return true;
    }

    private boolean isStyleCompatible(List<Garment> outfit) {
        List<String> outfitStyles = new ArrayList<>();
        for (Garment garment : outfit) {
            if (garment.getStyle() != null) outfitStyles.addAll(garment.getStyle());
        }
        if (outfitStyles.isEmpty()) return true;
        for (int i = 0; i < outfitStyles.size(); i++) {
            for (int j = i + 1; j < outfitStyles.size(); j++) {
                String s1 = outfitStyles.get(i);
                String s2 = outfitStyles.get(j);
                Set<String> c1 = styleCompatibility.get(s1);
                Set<String> c2 = styleCompatibility.get(s2);
                if (c1 == null || c2 == null) continue;
                if (!c1.contains(s2) && !c2.contains(s1)) return false;
            }
        }
        return true;
    }

    private boolean hasUniqueSubCategories(List<Garment> outfit) {
        Set<String> seenSubCategories = new HashSet<>();
        for (Garment garment : outfit) {
            String subCategory = garment.getSubCategory();
            if (subCategory == null || subCategory.trim().isEmpty()) continue;
            if (seenSubCategories.contains(subCategory)) return false;
            seenSubCategories.add(subCategory);
        }
        return true;
    }

    private boolean outfitHasColor(List<Garment> outfit, String color) {
        for (Garment garment : outfit) {
            if (garment.getColor() != null && garment.getColor().contains(color)) return true;
        }
        return false;
    }

    private boolean outfitHasStyle(List<Garment> outfit, String style) {
        for (Garment garment : outfit) {
            if (garment.getStyle() != null && garment.getStyle().contains(style)) return true;
        }
        return false;
    }

    public void saveGeneratedOutfit(String outfitName) {
        List<Garment> garments = generatedOutfitGarments.getValue();
        if (garments == null || garments.isEmpty()) {
            errorMessage.setValue("Nessun outfit da salvare");
            return;
        }
        if (outfitName == null || outfitName.trim().isEmpty()) {
            errorMessage.setValue("Inserisci un nome per l'outfit");
            return;
        }
        String season = suggestedSeason.getValue();
        Outfit outfit = new Outfit(outfitName, season, garments);
        isLoading.setValue(true);
        outfitRepository.saveOutfit(outfit, new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isLoading.postValue(false);
                outfitSaved.postValue(true);
            }
            @Override
            public void onFailure(String error, Throwable t) {
                isLoading.postValue(false);
                errorMessage.postValue("Errore salvataggio: " + error);
            }
        });
    }

    public LiveData<WeatherInfo> getCurrentWeather() { return currentWeather; }
    public LiveData<String> getSuggestedSeason() { return suggestedSeason; }
    public LiveData<List<String>> getAvailableColors() { return availableColors; }
    public LiveData<List<String>> getAvailableStyles() { return availableStyles; }
    public LiveData<List<String>> getAvailableSeasons() { return availableSeasons; }
    public LiveData<List<Garment>> getGeneratedOutfitGarments() { return generatedOutfitGarments; }
    public LiveData<Boolean> getOutfitSaved() { return outfitSaved; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
}