package it.unimib.yourwardrobe.ui.main.viewmodel;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.core.functional.Result;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.model.User;
import it.unimib.yourwardrobe.domain.model.WeatherInfo;
import it.unimib.yourwardrobe.domain.repository.AuthRepository;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.domain.repository.WeatherRepository;
import it.unimib.yourwardrobe.utils.WeatherUtil;

@HiltViewModel
public class HomeViewModel extends ViewModel {
    private static final String TAG = HomeViewModel.class.getSimpleName();
    private static final String PREF_OUTFIT_KEY = "daily_outfit";
    private static final String PREF_OUTFIT_DATE_KEY = "outfit_date";

    private final WeatherRepository weatherRepository;
    private final AuthRepository authRepository;
    private final GarmentRepository garmentRepository;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    private final MutableLiveData<Result<WeatherInfo>> _currentWeatherResult = new MutableLiveData<>();
    public final LiveData<Result<WeatherInfo>> currentWeatherResult = _currentWeatherResult;

    private final MutableLiveData<Result<User>> _currentUser = new MutableLiveData<>();
    public final LiveData<Result<User>> currentUser = _currentUser;

    private final MutableLiveData<List<Garment>> _outfitOfTheDay = new MutableLiveData<>();
    public final LiveData<List<Garment>> outfitOfTheDay = _outfitOfTheDay;

    private final MutableLiveData<Boolean> _isGeneratingOutfit = new MutableLiveData<>(false);
    public final LiveData<Boolean> isGeneratingOutfit = _isGeneratingOutfit;

    private final Map<String, Set<String>> colorHarmony = new HashMap<>();
    private final Map<String, Set<String>> styleCompatibility = new HashMap<>();
    private final Map<String, Set<String>> seasonCompatibility = new HashMap<>();

    // Cached weather info per rigenerazione manuale
    private WeatherInfo cachedWeatherInfo;

    @Inject
    public HomeViewModel(WeatherRepository weatherRepository,
                         AuthRepository authRepository,
                         GarmentRepository garmentRepository,
                         SharedPreferences sharedPreferences) {
        this.weatherRepository = weatherRepository;
        this.authRepository = authRepository;
        this.garmentRepository = garmentRepository;
        this.sharedPreferences = sharedPreferences;
        this.gson = new Gson();

        initializeColorHarmony();
        initializeStyleCompatibility();
        initializeSeasonCompatibility();
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

    private void initializeSeasonCompatibility() {
        seasonCompatibility.put("Primavera", new HashSet<>(Arrays.asList("Primavera", "Primavera - Estate", "Primavera - Autunno", "Tutte le stagioni")));
        seasonCompatibility.put("Estate", new HashSet<>(Arrays.asList("Estate", "Primavera - Estate", "Tutte le stagioni")));
        seasonCompatibility.put("Autunno", new HashSet<>(Arrays.asList("Autunno", "Inverno - Autunno", "Primavera - Autunno", "Tutte le stagioni")));
        seasonCompatibility.put("Inverno", new HashSet<>(Arrays.asList("Inverno", "Inverno - Autunno", "Tutte le stagioni")));
    }

    public void getCurrentUser() {
        _currentUser.setValue(Result.loading(null));
        var user = this.authRepository.getCurrentUser();
        if (user != null) {
            _currentUser.setValue(Result.success(user));
        }
    }

    public void getCurrentWeather(double lat, double lon) {
        _currentWeatherResult.setValue(Result.loading(null));
        this.weatherRepository.getCurrentWeather(lat, lon, new Callback<>() {
            @Override
            public void onSuccess(WeatherInfo data) {
                _currentWeatherResult.setValue(Result.success(data));
                cachedWeatherInfo = data;
                // Carica outfit salvato o genera nuovo se necessario
                loadOrGenerateOutfit(data);
            }

            @Override
            public void onFailure(String errorMessage, Throwable t) {
                Log.e(TAG, errorMessage, t);
                _currentWeatherResult.setValue(Result.error(errorMessage, null));
                loadOrGenerateOutfitWithDefaultSeason();
            }
        });
    }

    /**
     * Carica outfit salvato se è di oggi, altrimenti genera nuovo
     */
    private void loadOrGenerateOutfit(WeatherInfo weatherInfo) {
        String today = getTodayDate();
        String savedDate = sharedPreferences.getString(PREF_OUTFIT_DATE_KEY, "");

        if (today.equals(savedDate)) {
            // Carica outfit salvato
            List<Garment> savedOutfit = loadSavedOutfit();
            if (savedOutfit != null && !savedOutfit.isEmpty()) {
                Log.d(TAG, "Caricato outfit salvato con " + savedOutfit.size() + " capi");
                _outfitOfTheDay.setValue(savedOutfit);
                return;
            }
        }

        // Genera nuovo outfit
        Log.d(TAG, "Generazione nuovo outfit del giorno");
        generateOutfitOfTheDay(weatherInfo);
    }

    private void loadOrGenerateOutfitWithDefaultSeason() {
        String today = getTodayDate();
        String savedDate = sharedPreferences.getString(PREF_OUTFIT_DATE_KEY, "");

        if (today.equals(savedDate)) {
            List<Garment> savedOutfit = loadSavedOutfit();
            if (savedOutfit != null && !savedOutfit.isEmpty()) {
                Log.d(TAG, "Caricato outfit salvato con " + savedOutfit.size() + " capi");
                _outfitOfTheDay.setValue(savedOutfit);
                return;
            }
        }

        generateOutfitOfTheDayWithDefaultSeason();
    }

    /**
     * Forza rigenerazione outfit (chiamato dal bottone)
     */
    public void regenerateOutfit() {
        Log.d(TAG, "Rigenerazione manuale outfit");
        if (cachedWeatherInfo != null) {
            generateOutfitOfTheDay(cachedWeatherInfo);
        } else {
            generateOutfitOfTheDayWithDefaultSeason();
        }
    }

    private void generateOutfitOfTheDay(WeatherInfo weatherInfo) {
        _isGeneratingOutfit.setValue(true);

        String tempStr = weatherInfo.getTemperature().replace("°C", "");
        double temperature = 20.0;
        try {
            temperature = Double.parseDouble(tempStr);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing temperature", e);
        }

        String season = WeatherUtil.getSeasonFromTemperature(temperature);
        Log.d(TAG, "Generazione outfit per stagione: " + season);

        garmentRepository.getGarments(new Callback<List<Garment>>() {
            @Override
            public void onSuccess(List<Garment> garments) {
                if (garments.isEmpty()) {
                    Log.d(TAG, "Nessun capo disponibile");
                    _outfitOfTheDay.postValue(null);
                    _isGeneratingOutfit.postValue(false);
                    return;
                }

                List<Garment> outfit = generateSmartOutfit(garments, season);

                // Salva outfit generato
                saveOutfit(outfit);

                _outfitOfTheDay.postValue(outfit);
                _isGeneratingOutfit.postValue(false);
            }

            @Override
            public void onFailure(String error, Throwable t) {
                Log.e(TAG, "Errore caricamento capi: " + error, t);
                _outfitOfTheDay.postValue(null);
                _isGeneratingOutfit.postValue(false);
            }
        });
    }

    private void generateOutfitOfTheDayWithDefaultSeason() {
        _isGeneratingOutfit.setValue(true);

        garmentRepository.getGarments(new Callback<List<Garment>>() {
            @Override
            public void onSuccess(List<Garment> garments) {
                if (garments.isEmpty()) {
                    _outfitOfTheDay.postValue(null);
                    _isGeneratingOutfit.postValue(false);
                    return;
                }

                List<Garment> outfit = generateSmartOutfit(garments, "Primavera");

                // Salva outfit generato
                saveOutfit(outfit);

                _outfitOfTheDay.postValue(outfit);
                _isGeneratingOutfit.postValue(false);
            }

            @Override
            public void onFailure(String error, Throwable t) {
                Log.e(TAG, "Errore caricamento capi: " + error, t);
                _outfitOfTheDay.postValue(null);
                _isGeneratingOutfit.postValue(false);
            }
        });
    }

    /**
     * Salva outfit in SharedPreferences
     */
    private void saveOutfit(List<Garment> outfit) {
        if (outfit == null || outfit.isEmpty()) {
            return;
        }

        String outfitJson = gson.toJson(outfit);
        String today = getTodayDate();

        sharedPreferences.edit()
                .putString(PREF_OUTFIT_KEY, outfitJson)
                .putString(PREF_OUTFIT_DATE_KEY, today)
                .apply();

        Log.d(TAG, "Outfit salvato per la data: " + today);
    }

    /**
     * Carica outfit salvato da SharedPreferences
     */
    private List<Garment> loadSavedOutfit() {
        String outfitJson = sharedPreferences.getString(PREF_OUTFIT_KEY, null);
        if (outfitJson == null) {
            return null;
        }

        try {
            Type listType = new TypeToken<List<Garment>>() {}.getType();
            return gson.fromJson(outfitJson, listType);
        } catch (Exception e) {
            Log.e(TAG, "Errore caricamento outfit salvato", e);
            return null;
        }
    }

    /**
     * Cancella outfit salvato (da chiamare al logout)
     */
    public void clearSavedOutfit() {
        sharedPreferences.edit()
                .remove(PREF_OUTFIT_KEY)
                .remove(PREF_OUTFIT_DATE_KEY)
                .apply();

        // Pulisci anche il LiveData e la cache
        _outfitOfTheDay.postValue(null);
        cachedWeatherInfo = null;

        Log.d(TAG, "Outfit salvato cancellato e cache pulita");
    }

    /**
     * Ottiene data di oggi in formato yyyy-MM-dd
     */
    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Genera outfit usando la logica completa del PersonalStylist
     */
    private List<Garment> generateSmartOutfit(List<Garment> allGarments, String filterSeason) {
        boolean isWarmSeason = "Estate".equals(filterSeason) || "Primavera".equals(filterSeason);

        List<Garment> tops = filterByCategoryAndSeason(allGarments, "Parte superiore", filterSeason);
        List<Garment> bottoms = filterByCategoryAndSeason(allGarments, "Parte inferiore", filterSeason);

        List<Garment> shoes;
        if (isWarmSeason) {
            shoes = filterByCategoryAndSeason(allGarments, "Calzature", filterSeason);
        } else {
            shoes = filterByCategory(allGarments, "Calzature");
        }

        List<Garment> accessories;
        if (isWarmSeason) {
            accessories = filterByCategoryAndSeason(allGarments, "Accessorio", filterSeason);
        } else {
            accessories = filterByCategory(allGarments, "Accessorio");
        }

        if (tops.isEmpty() || bottoms.isEmpty()) {
            Log.w(TAG, "Nessun top o bottom disponibile");
            return new ArrayList<>();
        }

        List<List<Garment>> allPossibleOutfits = generateAllPossibleOutfits(tops, bottoms, shoes, accessories);
        List<List<Garment>> validOutfits = filterOutfitsByConstraints(allPossibleOutfits);

        if (validOutfits.isEmpty()) {
            Log.w(TAG, "Nessun outfit valido trovato");
            return new ArrayList<>();
        }

        Random random = new Random(System.currentTimeMillis());
        List<Garment> selectedOutfit = validOutfits.get(random.nextInt(validOutfits.size()));
        Log.d(TAG, "Outfit generato con " + selectedOutfit.size() + " capi");
        return selectedOutfit;
    }

    private List<List<Garment>> generateAllPossibleOutfits(List<Garment> tops, List<Garment> bottoms,
                                                           List<Garment> shoes, List<Garment> accessories) {
        List<List<Garment>> allOutfits = new ArrayList<>();

        // Top combinations (max 2)
        List<List<Garment>> topCombinations = new ArrayList<>();
        for (int i = 0; i < tops.size(); i++) {
            List<Garment> single = new ArrayList<>();
            single.add(tops.get(i));
            topCombinations.add(single);

            for (int j = i + 1; j < Math.min(tops.size(), i + 5); j++) {
                List<Garment> pair = new ArrayList<>();
                pair.add(tops.get(i));
                pair.add(tops.get(j));
                topCombinations.add(pair);
            }
        }

        // Bottom combinations (max 1)
        List<List<Garment>> bottomCombinations = new ArrayList<>();
        for (Garment bottom : bottoms) {
            List<Garment> single = new ArrayList<>();
            single.add(bottom);
            bottomCombinations.add(single);
        }

        // Shoes combinations (0 or 1)
        List<List<Garment>> shoesCombinations = new ArrayList<>();
        shoesCombinations.add(new ArrayList<>());
        for (Garment shoe : shoes) {
            List<Garment> single = new ArrayList<>();
            single.add(shoe);
            shoesCombinations.add(single);
        }

        // Accessory combinations (0 to 4)
        List<List<Garment>> accessoryCombinations = generateAccessoryCombinations(accessories);

        // Combina tutto
        for (List<Garment> topCombo : topCombinations) {
            if (topCombo.size() > 2) continue;
            for (List<Garment> bottomCombo : bottomCombinations) {
                for (List<Garment> shoesCombo : shoesCombinations) {
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

    private List<List<Garment>> generateAccessoryCombinations(List<Garment> accessories) {
        List<List<Garment>> combinations = new ArrayList<>();
        combinations.add(new ArrayList<>());

        for (int i = 0; i < accessories.size(); i++) {
            List<Garment> single = new ArrayList<>();
            single.add(accessories.get(i));
            combinations.add(single);

            for (int j = i + 1; j < Math.min(accessories.size(), i + 4); j++) {
                List<Garment> pair = new ArrayList<>();
                pair.add(accessories.get(i));
                pair.add(accessories.get(j));
                combinations.add(pair);
            }
        }

        return combinations;
    }

    private List<List<Garment>> filterOutfitsByConstraints(List<List<Garment>> outfits) {
        List<List<Garment>> validOutfits = new ArrayList<>();
        for (List<Garment> outfit : outfits) {
            if (isColorHarmonious(outfit) && isStyleCompatible(outfit) && hasUniqueSubCategories(outfit)) {
                validOutfits.add(outfit);
            }
        }
        return validOutfits;
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

    private boolean garmentMatchesSeason(Garment garment, String filterSeason) {
        if (filterSeason == null || filterSeason.isEmpty()) return true;
        String garmentSeason = garment.getSeason();
        if (garmentSeason == null || garmentSeason.isEmpty()) return true;

        Set<String> compatible = seasonCompatibility.get(filterSeason);
        if (compatible == null) return true;

        return compatible.contains(garmentSeason);
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
}