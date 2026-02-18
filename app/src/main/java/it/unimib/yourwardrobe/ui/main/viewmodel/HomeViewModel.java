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
import java.util.Calendar;
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
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.model.Outfit;
import it.unimib.yourwardrobe.domain.model.User;
import it.unimib.yourwardrobe.domain.model.WeatherInfo;
import it.unimib.yourwardrobe.domain.repository.AuthRepository;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.domain.repository.OutfitRepository;
import it.unimib.yourwardrobe.domain.repository.WeatherRepository;
import it.unimib.yourwardrobe.utils.Callback;
import it.unimib.yourwardrobe.utils.Resource;
import it.unimib.yourwardrobe.utils.WeatherUtil;

@HiltViewModel
public class HomeViewModel extends ViewModel {
    private static final String TAG = HomeViewModel.class.getSimpleName();
    private static final String PREF_OUTFIT_KEY = "daily_outfit";
    private static final String PREF_OUTFIT_DATE_KEY = "outfit_date";

    private final WeatherRepository weatherRepository;
    private final AuthRepository authRepository;
    private final GarmentRepository garmentRepository;
    private final OutfitRepository outfitRepository;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    // --- LiveData pubblici ---
    private final MutableLiveData<Resource<WeatherInfo>> _currentWeatherResult = new MutableLiveData<>();
    public final LiveData<Resource<WeatherInfo>> currentWeatherResult = _currentWeatherResult;

    private final MutableLiveData<Resource<User>> _currentUser = new MutableLiveData<>();
    public final LiveData<Resource<User>> currentUser = _currentUser;

    private final MutableLiveData<List<Garment>> _outfitOfTheDay = new MutableLiveData<>();
    public final LiveData<List<Garment>> outfitOfTheDay = _outfitOfTheDay;

    private final MutableLiveData<Boolean> _isGeneratingOutfit = new MutableLiveData<>(false);
    public final LiveData<Boolean> isGeneratingOutfit = _isGeneratingOutfit;

    // Planned outfit
    private final MutableLiveData<Resource<WeatherInfo>> _plannedWeather = new MutableLiveData<>();
    public final LiveData<Resource<WeatherInfo>> plannedWeather = _plannedWeather;

    private final MutableLiveData<List<Garment>> _plannedOutfit = new MutableLiveData<>();
    public final LiveData<List<Garment>> plannedOutfit = _plannedOutfit;

    private final MutableLiveData<Boolean> _isGeneratingPlanned = new MutableLiveData<>(false);
    public final LiveData<Boolean> isGeneratingPlanned = _isGeneratingPlanned;

    // Save outfit
    private final MutableLiveData<Resource<Boolean>> _saveOutfitResult = new MutableLiveData<>();
    public final LiveData<Resource<Boolean>> saveOutfitResult = _saveOutfitResult;
    // Compatibility maps
    private final Map<String, Set<String>> colorHarmony = new HashMap<>();
    private final Map<String, Set<String>> styleCompatibility = new HashMap<>();
    private final Map<String, Set<String>> seasonCompatibility = new HashMap<>();
    private final Map<String, List<String>> occasionToStyles = new HashMap<>();
    // Cache
    private WeatherInfo cachedWeatherInfo;

    @Inject
    public HomeViewModel(WeatherRepository weatherRepository,
                         AuthRepository authRepository,
                         GarmentRepository garmentRepository,
                         OutfitRepository outfitRepository,
                         SharedPreferences sharedPreferences) {
        this.weatherRepository = weatherRepository;
        this.authRepository = authRepository;
        this.garmentRepository = garmentRepository;
        this.outfitRepository = outfitRepository;
        this.sharedPreferences = sharedPreferences;
        this.gson = new Gson();

        initializeColorHarmony();
        initializeStyleCompatibility();
        initializeSeasonCompatibility();
        initializeOccasionToStyles();
    }

    // -------------------------------------------------------------------------
    // Init maps
    // -------------------------------------------------------------------------

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

    private void initializeOccasionToStyles() {
        occasionToStyles.put("Casual", Arrays.asList("Casual", "Streetwear", "Boho", "Vintage"));
        occasionToStyles.put("Business", Arrays.asList("Business", "Formale", "Elegante"));
        occasionToStyles.put("Elegant", Arrays.asList("Elegante", "Formale"));
        occasionToStyles.put("Sport", List.of("Sportivo"));
    }

    // -------------------------------------------------------------------------
    // Outfit of the day
    // -------------------------------------------------------------------------

    public void getCurrentUser() {
        _currentUser.setValue(Resource.loading(null));
        var user = authRepository.getCurrentUser();
        if (user != null) _currentUser.setValue(Resource.success(user));
    }

    public void getCurrentWeather(double lat, double lon) {
        _currentWeatherResult.setValue(Resource.loading(null));
        weatherRepository.getCurrentWeather(lat, lon, new Callback<>() {
            @Override
            public void onSuccess(WeatherInfo data) {
                _currentWeatherResult.setValue(Resource.success(data));
                cachedWeatherInfo = data;
                loadOrGenerateOutfit(data);
            }

            @Override
            public void onFailure(String errorMessage, Throwable t) {
                Log.e(TAG, errorMessage, t);
                _currentWeatherResult.setValue(Resource.error(errorMessage, null));
                loadOrGenerateOutfitFallback();
            }
        });
    }

    public void regenerateOutfit() {
        if (cachedWeatherInfo != null) {
            generateOutfitOfTheDay(cachedWeatherInfo);
        } else {
            generateOutfitOfTheDayForSeason("Primavera");
        }
    }

    private void loadOrGenerateOutfit(WeatherInfo weatherInfo) {
        String today = getTodayDate();
        String savedDate = sharedPreferences.getString(PREF_OUTFIT_DATE_KEY, "");
        if (today.equals(savedDate)) {
            List<Garment> saved = loadSavedOutfit();
            if (saved != null && !saved.isEmpty()) {
                _outfitOfTheDay.setValue(saved);
                return;
            }
        }
        generateOutfitOfTheDay(weatherInfo);
    }

    private void loadOrGenerateOutfitFallback() {
        String today = getTodayDate();
        String savedDate = sharedPreferences.getString(PREF_OUTFIT_DATE_KEY, "");
        if (today.equals(savedDate)) {
            List<Garment> saved = loadSavedOutfit();
            if (saved != null && !saved.isEmpty()) {
                _outfitOfTheDay.setValue(saved);
                return;
            }
        }
        generateOutfitOfTheDayForSeason("Primavera");
    }

    private void generateOutfitOfTheDay(WeatherInfo weatherInfo) {
        generateOutfitOfTheDayForSeason(seasonFromWeatherInfo(weatherInfo));
    }

    private void generateOutfitOfTheDayForSeason(String season) {
        _isGeneratingOutfit.setValue(true);
        garmentRepository.getGarments(new Callback<List<Garment>>() {
            @Override
            public void onSuccess(List<Garment> garments) {
                List<Garment> outfit = garments.isEmpty()
                        ? new ArrayList<>()
                        : generateSmartOutfit(garments, season);
                saveOutfitToPrefs(outfit);
                _outfitOfTheDay.postValue(outfit.isEmpty() ? null : outfit);
                _isGeneratingOutfit.postValue(false);
            }

            @Override
            public void onFailure(String error, Throwable t) {
                Log.e(TAG, error, t);
                _outfitOfTheDay.postValue(null);
                _isGeneratingOutfit.postValue(false);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Planned outfit
    // -------------------------------------------------------------------------

    public void generatePlannedOutfit(double lat, double lon, long dateMillis,
                                      int targetHour, String occasion) {
        _plannedWeather.setValue(Resource.loading(null));
        _isGeneratingPlanned.setValue(true);

        // Imposta l'ora selezionata nel timestamp
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateMillis);
        cal.set(Calendar.HOUR_OF_DAY, targetHour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long adjustedMillis = cal.getTimeInMillis();

        weatherRepository.getForecastWeather(lat, lon, adjustedMillis, new Callback<>() {
            @Override
            public void onSuccess(WeatherInfo data) {
                _plannedWeather.setValue(Resource.success(data));
                String season = seasonFromWeatherInfo(data);
                List<String> allowedStyles = occasionToStyles.getOrDefault(
                        occasion, Arrays.asList("Casual", "Streetwear"));
                fetchAndGeneratePlanned(season, allowedStyles);
            }

            @Override
            public void onFailure(String errorMessage, Throwable t) {
                Log.e(TAG, errorMessage, t);
                _plannedWeather.setValue(Resource.error(errorMessage, null));
                _isGeneratingPlanned.setValue(false);
            }
        });
    }

    private void fetchAndGeneratePlanned(String season, List<String> allowedStyles) {
        garmentRepository.getGarments(new Callback<List<Garment>>() {
            @Override
            public void onSuccess(List<Garment> garments) {
                if (garments.isEmpty()) {
                    _plannedOutfit.postValue(null);
                    _isGeneratingPlanned.postValue(false);
                    return;
                }
                List<Garment> outfit = generateSmartOutfitWithOccasion(garments, season, allowedStyles);
                _plannedOutfit.postValue(outfit.isEmpty() ? null : outfit);
                _isGeneratingPlanned.postValue(false);
            }

            @Override
            public void onFailure(String error, Throwable t) {
                Log.e(TAG, error, t);
                _plannedOutfit.postValue(null);
                _isGeneratingPlanned.postValue(false);
            }
        });
    }

    public void savePlannedOutfit(String name, List<Garment> garments, String season) {
        if (name == null || name.trim().isEmpty()) {
            _saveOutfitResult.setValue(Resource.error("Inserisci un nome per l'outfit", null));
            return;
        }
        _saveOutfitResult.setValue(Resource.loading(null));
        Outfit outfit = new Outfit(name.trim(), season, garments);
        outfitRepository.saveOutfit(outfit, new Callback<>() {
            @Override
            public void onSuccess(Boolean result) {
                _saveOutfitResult.postValue(Resource.success(true));
            }

            @Override
            public void onFailure(String error, Throwable t) {
                Log.e(TAG, error, t);
                _saveOutfitResult.postValue(Resource.error(error, null));
            }
        });
    }

    public void resetPlannedOutfit() {
        _plannedWeather.setValue(null);
        _plannedOutfit.setValue(null);
        _isGeneratingPlanned.setValue(false);
    }

    public void resetSaveOutfitResult() {
        _saveOutfitResult.setValue(null);
    }

    // -------------------------------------------------------------------------
    // Smart outfit generation
    // -------------------------------------------------------------------------

    private List<Garment> generateSmartOutfit(List<Garment> allGarments, String filterSeason) {
        return generateSmartOutfitWithOccasion(allGarments, filterSeason, null);
    }

    private List<Garment> generateSmartOutfitWithOccasion(List<Garment> allGarments,
                                                          String filterSeason,
                                                          List<String> allowedStyles) {
        boolean isWarm = "Estate".equals(filterSeason) || "Primavera".equals(filterSeason);

        List<Garment> pool = allowedStyles != null
                ? filterBySeasonAndOccasion(allGarments, filterSeason, allowedStyles)
                : allGarments;

        if (pool.size() < 3) pool = filterBySeason(allGarments, filterSeason);

        List<Garment> tops = filterByCategory(pool, "Parte superiore", filterSeason);
        List<Garment> bottoms = filterByCategory(pool, "Parte inferiore", filterSeason);
        List<Garment> shoes = isWarm
                ? filterByCategory(pool, "Calzature", filterSeason)
                : filterByCategoryNoSeason(allGarments, "Calzature");
        List<Garment> accessories = isWarm
                ? filterByCategory(pool, "Accessorio", filterSeason)
                : filterByCategoryNoSeason(allGarments, "Accessorio");

        if (tops.isEmpty() || bottoms.isEmpty()) return new ArrayList<>();

        List<List<Garment>> allOutfits = generateAllCombinations(tops, bottoms, shoes, accessories);
        List<List<Garment>> validOutfits = filterByConstraints(allOutfits);

        if (validOutfits.isEmpty()) return new ArrayList<>();

        return validOutfits.get(new Random(System.currentTimeMillis()).nextInt(validOutfits.size()));
    }

    private List<List<Garment>> generateAllCombinations(List<Garment> tops, List<Garment> bottoms,
                                                        List<Garment> shoes, List<Garment> accessories) {
        List<List<Garment>> result = new ArrayList<>();

        List<List<Garment>> topCombos = new ArrayList<>();
        for (int i = 0; i < tops.size(); i++) {
            topCombos.add(new ArrayList<>(List.of(tops.get(i))));
            for (int j = i + 1; j < Math.min(tops.size(), i + 5); j++) {
                topCombos.add(new ArrayList<>(Arrays.asList(tops.get(i), tops.get(j))));
            }
        }

        List<List<Garment>> shoesCombos = new ArrayList<>();
        shoesCombos.add(new ArrayList<>());
        for (Garment s : shoes) shoesCombos.add(new ArrayList<>(List.of(s)));

        List<List<Garment>> accCombos = buildAccessoryCombinations(accessories);

        for (List<Garment> top : topCombos) {
            for (Garment bottom : bottoms) {
                for (List<Garment> shoe : shoesCombos) {
                    for (List<Garment> acc : accCombos) {
                        List<Garment> outfit = new ArrayList<>();
                        outfit.addAll(top);
                        outfit.add(bottom);
                        outfit.addAll(shoe);
                        outfit.addAll(acc);
                        result.add(outfit);
                    }
                }
            }
        }
        return result;
    }

    private List<List<Garment>> buildAccessoryCombinations(List<Garment> accessories) {
        List<List<Garment>> combos = new ArrayList<>();
        for (int i = 0; i < accessories.size(); i++) {
            combos.add(new ArrayList<>(List.of(accessories.get(i))));
            for (int j = i + 1; j < accessories.size() && j < i + 4; j++) {
                combos.add(new ArrayList<>(Arrays.asList(accessories.get(i), accessories.get(j))));
                for (int k = j + 1; k < accessories.size() && k < j + 2; k++) {
                    combos.add(new ArrayList<>(Arrays.asList(
                            accessories.get(i), accessories.get(j), accessories.get(k))));
                }
            }
        }
        combos.add(new ArrayList<>());
        return combos;
    }

    private List<List<Garment>> filterByConstraints(List<List<Garment>> outfits) {
        List<List<Garment>> valid = new ArrayList<>();
        for (List<Garment> outfit : outfits) {
            if (isColorHarmonious(outfit) && isStyleCompatible(outfit) && hasUniqueSubCategories(outfit)) {
                valid.add(outfit);
            }
        }
        return valid;
    }

    // -------------------------------------------------------------------------
    // Filtering helpers
    // -------------------------------------------------------------------------

    private List<Garment> filterBySeasonAndOccasion(List<Garment> garments, String season,
                                                    List<String> allowedStyles) {
        List<Garment> result = new ArrayList<>();
        for (Garment g : garments) {
            if (!garmentMatchesSeason(g, season)) continue;
            if (g.getStyle() == null || g.getStyle().isEmpty()) {
                result.add(g);
                continue;
            }
            for (String s : g.getStyle()) {
                if (allowedStyles.contains(s)) {
                    result.add(g);
                    break;
                }
            }
        }
        return result;
    }

    private List<Garment> filterBySeason(List<Garment> garments, String season) {
        List<Garment> result = new ArrayList<>();
        for (Garment g : garments) {
            if (garmentMatchesSeason(g, season)) result.add(g);
        }
        return result;
    }

    private List<Garment> filterByCategory(List<Garment> garments, String category, String season) {
        List<Garment> result = new ArrayList<>();
        for (Garment g : garments) {
            if (category.equals(g.getCategory()) && garmentMatchesSeason(g, season)) result.add(g);
        }
        return result;
    }

    private List<Garment> filterByCategoryNoSeason(List<Garment> garments, String category) {
        List<Garment> result = new ArrayList<>();
        for (Garment g : garments) {
            if (category.equals(g.getCategory())) result.add(g);
        }
        return result;
    }

    private boolean garmentMatchesSeason(Garment garment, String filterSeason) {
        if (filterSeason == null || filterSeason.isEmpty()) return true;
        String gs = garment.getSeason();
        if (gs == null || gs.isEmpty()) return true;
        Set<String> compatible = seasonCompatibility.get(filterSeason);
        return compatible != null && compatible.contains(gs);
    }

    // -------------------------------------------------------------------------
    // Constraint checks
    // -------------------------------------------------------------------------

    private boolean isColorHarmonious(List<Garment> outfit) {
        List<String> colors = new ArrayList<>();
        for (Garment g : outfit) {
            if (g.getColor() != null) colors.addAll(g.getColor());
        }
        if (colors.isEmpty()) return true;
        for (int i = 0; i < colors.size(); i++) {
            for (int j = i + 1; j < colors.size(); j++) {
                Set<String> h1 = colorHarmony.get(colors.get(i));
                Set<String> h2 = colorHarmony.get(colors.get(j));
                if (h1 == null || h2 == null) continue;
                if (!h1.contains(colors.get(j)) && !h2.contains(colors.get(i))) return false;
            }
        }
        return true;
    }

    private boolean isStyleCompatible(List<Garment> outfit) {
        List<String> styles = new ArrayList<>();
        for (Garment g : outfit) {
            if (g.getStyle() != null) styles.addAll(g.getStyle());
        }
        if (styles.isEmpty()) return true;
        for (int i = 0; i < styles.size(); i++) {
            for (int j = i + 1; j < styles.size(); j++) {
                Set<String> c1 = styleCompatibility.get(styles.get(i));
                Set<String> c2 = styleCompatibility.get(styles.get(j));
                if (c1 == null || c2 == null) continue;
                if (!c1.contains(styles.get(j)) && !c2.contains(styles.get(i))) return false;
            }
        }
        return true;
    }

    private boolean hasUniqueSubCategories(List<Garment> outfit) {
        Set<String> seen = new HashSet<>();
        for (Garment g : outfit) {
            String sub = g.getSubCategory();
            if (sub == null || sub.trim().isEmpty()) continue;
            if (!seen.add(sub)) return false;
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // SharedPreferences helpers
    // -------------------------------------------------------------------------

    private void saveOutfitToPrefs(List<Garment> outfit) {
        if (outfit == null || outfit.isEmpty()) return;
        sharedPreferences.edit()
                .putString(PREF_OUTFIT_KEY, gson.toJson(outfit))
                .putString(PREF_OUTFIT_DATE_KEY, getTodayDate())
                .apply();
    }

    private List<Garment> loadSavedOutfit() {
        String json = sharedPreferences.getString(PREF_OUTFIT_KEY, null);
        if (json == null) return null;
        try {
            Type t = new TypeToken<List<Garment>>() {
            }.getType();
            return gson.fromJson(json, t);
        } catch (Exception e) {
            Log.e(TAG, "Errore caricamento outfit salvato", e);
            return null;
        }
    }

    public void clearSavedOutfit() {
        sharedPreferences.edit()
                .remove(PREF_OUTFIT_KEY)
                .remove(PREF_OUTFIT_DATE_KEY)
                .apply();
        _outfitOfTheDay.postValue(null);
        _plannedOutfit.postValue(null);
        cachedWeatherInfo = null;
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    private String seasonFromWeatherInfo(WeatherInfo info) {
        try {
            double temp = Double.parseDouble(info.getTemperature().replace("°C", "").trim());
            return WeatherUtil.getSeasonFromTemperature(temp);
        } catch (NumberFormatException e) {
            return "Primavera";
        }
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}