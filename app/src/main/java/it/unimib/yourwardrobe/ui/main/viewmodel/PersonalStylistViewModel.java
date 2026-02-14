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
import it.unimib.yourwardrobe.domain.model.WeatherInfo;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.domain.repository.OutfitRepository;
import it.unimib.yourwardrobe.domain.repository.WeatherRepository;
import it.unimib.yourwardrobe.utils.WeatherUtil;

@HiltViewModel
public class PersonalStylistViewModel extends ViewModel {

    private static final String NOT_SELECTED = "Non selezionato";

    private final GarmentRepository garmentRepository;
    private final OutfitRepository outfitRepository;
    private final WeatherRepository weatherRepository;

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

    private final Map<String, Set<String>> colorHarmony = new HashMap<>();
    private final Map<String, Set<String>> styleCompatibility = new HashMap<>();

    @Inject
    public PersonalStylistViewModel(
            GarmentRepository garmentRepository,
            OutfitRepository outfitRepository,
            WeatherRepository weatherRepository) {
        this.garmentRepository = garmentRepository;
        this.outfitRepository = outfitRepository;
        this.weatherRepository = weatherRepository;

        initializeColorHarmony();
        initializeStyleCompatibility();
        loadFilters();
        loadGarments();
    }

    private void initializeColorHarmony() {
        colorHarmony.put("Nero", new HashSet<>(Arrays.asList("Bianco", "Grigio", "Rosso", "Blu", "Beige", "Nero")));
        colorHarmony.put("Bianco", new HashSet<>(Arrays.asList("Nero", "Blu", "Grigio", "Rosso", "Verde", "Beige", "Bianco")));
        colorHarmony.put("Grigio", new HashSet<>(Arrays.asList("Nero", "Bianco", "Blu", "Rosso", "Rosa", "Grigio")));
        colorHarmony.put("Blu", new HashSet<>(Arrays.asList("Bianco", "Grigio", "Beige", "Nero", "Marrone", "Blu")));
        colorHarmony.put("Rosso", new HashSet<>(Arrays.asList("Nero", "Bianco", "Grigio", "Blu scuro", "Rosso")));
        colorHarmony.put("Verde", new HashSet<>(Arrays.asList("Bianco", "Beige", "Marrone", "Nero", "Verde")));
        colorHarmony.put("Beige", new HashSet<>(Arrays.asList("Bianco", "Marrone", "Blu", "Verde", "Nero", "Beige")));
        colorHarmony.put("Marrone", new HashSet<>(Arrays.asList("Beige", "Verde", "Bianco", "Blu", "Marrone")));
        colorHarmony.put("Rosa", new HashSet<>(Arrays.asList("Grigio", "Bianco", "Nero", "Blu", "Rosa")));
    }

    private void initializeStyleCompatibility() {
        styleCompatibility.put("Casual", new HashSet<>(Arrays.asList("Casual", "Sportivo", "Streetwear")));
        styleCompatibility.put("Elegante", new HashSet<>(Arrays.asList("Elegante", "Formale", "Business")));
        styleCompatibility.put("Sportivo", new HashSet<>(Arrays.asList("Sportivo", "Casual", "Streetwear")));
        styleCompatibility.put("Formale", new HashSet<>(Arrays.asList("Formale", "Elegante", "Business")));
        styleCompatibility.put("Streetwear", new HashSet<>(Arrays.asList("Streetwear", "Casual", "Sportivo")));
        styleCompatibility.put("Business", new HashSet<>(Arrays.asList("Business", "Formale", "Elegante")));
        styleCompatibility.put("Boho", new HashSet<>(Arrays.asList("Boho", "Casual")));
        styleCompatibility.put("Vintage", new HashSet<>(Arrays.asList("Vintage", "Casual", "Elegante")));
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

    public void generateOutfit(String filterSeason, String filterColor, String filterStyle) {
        android.util.Log.d("PersonalStylistVM", "=== GENERAZIONE OUTFIT ===");
        android.util.Log.d("PersonalStylistVM", "Stagione: " + filterSeason);
        android.util.Log.d("PersonalStylistVM", "Colore: " + filterColor);
        android.util.Log.d("PersonalStylistVM", "Stile: " + filterStyle);

        if (allGarments.isEmpty()) {
            errorMessage.setValue("Nessun capo disponibile nel guardaroba");
            return;
        }

        String actualColor = NOT_SELECTED.equals(filterColor) ? null : filterColor;
        String actualStyle = NOT_SELECTED.equals(filterStyle) ? null : filterStyle;

        List<Garment> tops = filterByCategory(allGarments, "Parte superiore");
        List<Garment> bottoms = filterByCategory(allGarments, "Parte inferiore");
        List<Garment> shoes = filterByCategory(allGarments, "Calzature");
        List<Garment> accessories = filterByCategory(allGarments, "Accessori");

        android.util.Log.d("PersonalStylistVM", "Pool - Top: " + tops.size() + ", Bottom: " + bottoms.size() + ", Scarpe: " + shoes.size() + ", Accessori: " + accessories.size());

        if (tops.isEmpty() || bottoms.isEmpty()) {
            errorMessage.setValue("Non hai abbastanza capi per generare un outfit (servono almeno 1 top e 1 bottom)");
            return;
        }

        List<List<Garment>> allPossibleOutfits = generateAllPossibleOutfits(tops, bottoms, shoes, accessories);
        android.util.Log.d("PersonalStylistVM", "Outfit possibili: " + allPossibleOutfits.size());

        List<List<Garment>> validOutfits = filterOutfitsByConstraints(
                allPossibleOutfits,
                actualColor,
                actualStyle
        );

        android.util.Log.d("PersonalStylistVM", "Outfit validi dopo filtri: " + validOutfits.size());

        if (validOutfits.isEmpty()) {
            errorMessage.setValue("Nessun outfit armonioso trovato con i filtri selezionati. Prova a rimuovere qualche filtro.");
            return;
        }

        Random random = new Random();
        List<Garment> selectedOutfit = validOutfits.get(random.nextInt(validOutfits.size()));

        android.util.Log.d("PersonalStylistVM", "Outfit selezionato con " + selectedOutfit.size() + " capi");
        generatedOutfitGarments.setValue(selectedOutfit);
    }

    private List<List<Garment>> generateAllPossibleOutfits(
            List<Garment> tops,
            List<Garment> bottoms,
            List<Garment> shoes,
            List<Garment> accessories) {

        List<List<Garment>> allOutfits = new ArrayList<>();
        Random random = new Random();

        // Genera combinazioni di top (1 o 2)
        List<List<Garment>> topCombinations = new ArrayList<>();
        for (int i = 0; i < tops.size(); i++) {
            List<Garment> singleTop = new ArrayList<>();
            singleTop.add(tops.get(i));
            topCombinations.add(singleTop);

            // Combinazioni di 2 top
            for (int j = i + 1; j < tops.size() && j < i + 5; j++) { // Limita per performance
                List<Garment> doubleTops = new ArrayList<>();
                doubleTops.add(tops.get(i));
                doubleTops.add(tops.get(j));
                topCombinations.add(doubleTops);
            }
        }

        // Genera combinazioni di bottom (solo 1)
        List<List<Garment>> bottomCombinations = new ArrayList<>();
        for (Garment bottom : bottoms) {
            List<Garment> singleBottom = new ArrayList<>();
            singleBottom.add(bottom);
            bottomCombinations.add(singleBottom);
        }

        // Genera combinazioni di scarpe (solo 1)
        List<List<Garment>> shoesCombinations = new ArrayList<>();
        shoesCombinations.add(new ArrayList<>()); // Opzione: nessuna scarpa
        for (Garment shoe : shoes) {
            List<Garment> singleShoe = new ArrayList<>();
            singleShoe.add(shoe);
            shoesCombinations.add(singleShoe);
        }

        // Genera combinazioni di accessori (0 a 4)
        List<List<Garment>> accessoryCombinations = new ArrayList<>();
        accessoryCombinations.add(new ArrayList<>()); // Opzione: nessun accessorio

        // Singoli accessori
        for (int i = 0; i < accessories.size(); i++) {
            List<Garment> single = new ArrayList<>();
            single.add(accessories.get(i));
            accessoryCombinations.add(single);

            // Coppie di accessori
            for (int j = i + 1; j < accessories.size() && j < i + 4; j++) {
                List<Garment> pair = new ArrayList<>();
                pair.add(accessories.get(i));
                pair.add(accessories.get(j));
                accessoryCombinations.add(pair);
            }

            // Terzetti di accessori
            for (int j = i + 1; j < accessories.size() && j < i + 3; j++) {
                for (int k = j + 1; k < accessories.size() && k < j + 2; k++) {
                    List<Garment> triple = new ArrayList<>();
                    triple.add(accessories.get(i));
                    triple.add(accessories.get(j));
                    triple.add(accessories.get(k));
                    accessoryCombinations.add(triple);
                }
            }

            // Quartetti di accessori
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

        android.util.Log.d("PersonalStylistVM", "Combinazioni - Top: " + topCombinations.size() +
                ", Bottom: " + bottomCombinations.size() +
                ", Scarpe: " + shoesCombinations.size() +
                ", Accessori: " + accessoryCombinations.size());

        // Combina tutto
        for (List<Garment> topCombo : topCombinations) {
            // Verifica threshold top (max 2)
            if (topCombo.size() > 2) continue;

            for (List<Garment> bottomCombo : bottomCombinations) {
                // Verifica threshold bottom (max 1)
                if (bottomCombo.size() > 1) continue;

                for (List<Garment> shoesCombo : shoesCombinations) {
                    // Verifica threshold scarpe (max 1)
                    if (shoesCombo.size() > 1) continue;

                    for (List<Garment> accessoryCombo : accessoryCombinations) {
                        // Verifica threshold accessori (max 4)
                        if (accessoryCombo.size() > 4) continue;

                        List<Garment> outfit = new ArrayList<>();
                        outfit.addAll(topCombo);
                        outfit.addAll(bottomCombo);
                        outfit.addAll(shoesCombo);
                        outfit.addAll(accessoryCombo);

                        // Aggiungi solo se ha almeno top e bottom
                        if (!topCombo.isEmpty() && !bottomCombo.isEmpty()) {
                            allOutfits.add(outfit);
                        }
                    }
                }
            }
        }

        android.util.Log.d("PersonalStylistVM", "Outfit totali generati: " + allOutfits.size());
        return allOutfits;
    }

    private List<List<Garment>> filterOutfitsByConstraints(
            List<List<Garment>> outfits,
            String filterColor,
            String filterStyle) {

        List<List<Garment>> validOutfits = new ArrayList<>();

        for (List<Garment> outfit : outfits) {
            if (!isColorHarmonious(outfit)) {
                continue;
            }

            if (!isStyleCompatible(outfit)) {
                continue;
            }

            boolean meetsColorConstraint = true;
            boolean meetsStyleConstraint = true;

            if (filterColor != null && !filterColor.isEmpty()) {
                meetsColorConstraint = outfitHasColor(outfit, filterColor);
            }

            if (filterStyle != null && !filterStyle.isEmpty()) {
                meetsStyleConstraint = outfitHasStyle(outfit, filterStyle);
            }

            if (meetsColorConstraint && meetsStyleConstraint) {
                validOutfits.add(outfit);
            }
        }

        return validOutfits;
    }

    private boolean isColorHarmonious(List<Garment> outfit) {
        List<String> outfitColors = new ArrayList<>();
        for (Garment garment : outfit) {
            if (garment.getColor() != null && !garment.getColor().isEmpty()) {
                outfitColors.addAll(garment.getColor());
            }
        }

        if (outfitColors.isEmpty()) {
            return true;
        }

        for (int i = 0; i < outfitColors.size(); i++) {
            for (int j = i + 1; j < outfitColors.size(); j++) {
                String color1 = outfitColors.get(i);
                String color2 = outfitColors.get(j);

                Set<String> harmonious1 = colorHarmony.get(color1);
                Set<String> harmonious2 = colorHarmony.get(color2);

                if (harmonious1 == null || harmonious2 == null) {
                    continue;
                }

                if (!harmonious1.contains(color2) && !harmonious2.contains(color1)) {
                    android.util.Log.d("PersonalStylistVM", "Colori non armonici: " + color1 + " + " + color2);
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isStyleCompatible(List<Garment> outfit) {
        List<String> outfitStyles = new ArrayList<>();
        for (Garment garment : outfit) {
            if (garment.getStyle() != null && !garment.getStyle().isEmpty()) {
                outfitStyles.addAll(garment.getStyle());
            }
        }

        if (outfitStyles.isEmpty()) {
            return true;
        }

        for (int i = 0; i < outfitStyles.size(); i++) {
            for (int j = i + 1; j < outfitStyles.size(); j++) {
                String style1 = outfitStyles.get(i);
                String style2 = outfitStyles.get(j);

                Set<String> compatible1 = styleCompatibility.get(style1);
                Set<String> compatible2 = styleCompatibility.get(style2);

                if (compatible1 == null || compatible2 == null) {
                    continue;
                }

                if (!compatible1.contains(style2) && !compatible2.contains(style1)) {
                    android.util.Log.d("PersonalStylistVM", "Stili incompatibili: " + style1 + " + " + style2);
                    return false;
                }
            }
        }

        return true;
    }

    private boolean outfitHasColor(List<Garment> outfit, String color) {
        for (Garment garment : outfit) {
            if (garment.getColor() != null && garment.getColor().contains(color)) {
                return true;
            }
        }
        return false;
    }

    private boolean outfitHasStyle(List<Garment> outfit, String style) {
        for (Garment garment : outfit) {
            if (garment.getStyle() != null && garment.getStyle().contains(style)) {
                return true;
            }
        }
        return false;
    }

    private List<Garment> filterByCategory(List<Garment> garments, String category) {
        List<Garment> result = new ArrayList<>();
        for (Garment g : garments) {
            if (category.equals(g.getCategory())) {
                result.add(g);
            }
        }
        return result;
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
                android.util.Log.d("PersonalStylistVM", "Outfit salvato con successo");
                isLoading.postValue(false);
                outfitSaved.postValue(true);
            }

            @Override
            public void onFailure(String error, Throwable t) {
                android.util.Log.e("PersonalStylistVM", "Errore salvataggio: " + error);
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