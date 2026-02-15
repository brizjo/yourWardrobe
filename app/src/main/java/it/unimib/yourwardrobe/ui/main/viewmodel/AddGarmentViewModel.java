package it.unimib.yourwardrobe.ui.main.viewmodel;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.utils.ImageValidationState;

@HiltViewModel
public class AddGarmentViewModel extends ViewModel {

    private final Context context;
    private final GarmentRepository garmentRepository;
    private final MutableLiveData<ImageValidationState> imageValidationState = new MutableLiveData<>(ImageValidationState.UNCHECKED);

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> garmentAddedSuccessfully = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private final MutableLiveData<List<String>> allColors = new MutableLiveData<>();
    private final MutableLiveData<List<String>> allCategories = new MutableLiveData<>();
    private final MutableLiveData<List<String>> allStyles = new MutableLiveData<>();
    private final MutableLiveData<List<String>> allFabrics = new MutableLiveData<>();
    private final MutableLiveData<List<String>> allSeasons = new MutableLiveData<>();
    private final MutableLiveData<List<String>> allSubCategories = new MutableLiveData<>();
    private final Map<String, List<String>> subcategoryMap = new HashMap<>();

    private final MutableLiveData<List<String>> selectedColors = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> garmentImage = new MutableLiveData<>();
    private final MutableLiveData<String> garmentName = new MutableLiveData<>();
    private final MutableLiveData<String> selectedCategory = new MutableLiveData<>();
    private final MutableLiveData<List<String>> selectedStyles = new MutableLiveData<>();
    private final MutableLiveData<List<String>> selectedFabrics = new MutableLiveData<>();
    private final MutableLiveData<String> selectedSeason = new MutableLiveData<>();
    private final MutableLiveData<String> selectedSubCategory = new MutableLiveData<>(); // CAMBIATO: String invece di List
    private final MediatorLiveData<Boolean> isButtonEnabled = new MediatorLiveData<>();

    @Inject
    public AddGarmentViewModel(@ApplicationContext Context context, GarmentRepository garmentRepository) {
        this.context = context;
        this.garmentRepository = garmentRepository;
        loadInitialData();
        selectedColors.setValue(new ArrayList<>());
        selectedStyles.setValue(new ArrayList<>());
        selectedFabrics.setValue(new ArrayList<>());
        isButtonEnabled.setValue(false);
        isButtonEnabled.addSource(imageValidationState, this::validateForm);
        isButtonEnabled.addSource(garmentImage, value -> validateForm());
        isButtonEnabled.addSource(garmentName, value -> validateForm());
        isButtonEnabled.addSource(selectedCategory, value -> validateForm());
        isButtonEnabled.addSource(selectedColors, value -> validateForm());
        isButtonEnabled.addSource(selectedStyles, value -> validateForm());
        isButtonEnabled.addSource(selectedFabrics, value -> validateForm());
        isButtonEnabled.addSource(selectedSeason, value -> validateForm());
        isButtonEnabled.addSource(selectedSubCategory, value -> validateForm());
    }

    private void loadInitialData() {
        allColors.setValue(Arrays.asList(context.getResources().getStringArray(R.array.garment_color)));
        allCategories.setValue(Arrays.asList(context.getResources().getStringArray(R.array.categories)));
        allStyles.setValue(Arrays.asList(context.getResources().getStringArray(R.array.garment_styles)));
        allFabrics.setValue(Arrays.asList(context.getResources().getStringArray(R.array.fabric_types)));
        allSeasons.setValue(Arrays.asList(
                "Tutte le stagioni",
                "Primavera",
                "Estate",
                "Autunno",
                "Inverno",
                "Inverno - Autunno",
                "Primavera - Estate",
                "Primavera - Autunno"
        ));
        subcategoryMap.put(context.getString(R.string.top_garment), Arrays.asList(context.getResources().getStringArray(R.array.subcategories_top)));
        subcategoryMap.put(context.getString(R.string.bottom_garment), Arrays.asList(context.getResources().getStringArray(R.array.subcategories_bottom)));
        subcategoryMap.put(context.getString(R.string.footwear), Arrays.asList(context.getResources().getStringArray(R.array.subcategories_footwear)));
        subcategoryMap.put(context.getString(R.string.accessory), Arrays.asList(context.getResources().getStringArray(R.array.subcategories_accessories)));
    }

    private void validateForm() {
        ImageValidationState validation = imageValidationState.getValue();
        boolean imageOk = validation == ImageValidationState.VALID;
        boolean hasName = garmentName.getValue() != null && !garmentName.getValue().isEmpty();
        boolean hasCategory = selectedCategory.getValue() != null && !selectedCategory.getValue().isEmpty();
        boolean hasColors = selectedColors.getValue() != null && !selectedColors.getValue().isEmpty();
        boolean hasStyles = selectedStyles.getValue() != null && !selectedStyles.getValue().isEmpty();
        boolean hasSeason = selectedSeason.getValue() != null && !selectedSeason.getValue().isEmpty();
        boolean hasSubCategory = selectedSubCategory.getValue() != null && !selectedSubCategory.getValue().isEmpty(); // CAMBIATO

        isButtonEnabled.setValue(imageOk && hasName && hasCategory && hasColors && hasStyles && hasSeason && hasSubCategory);
    }

    private void validateForm(ImageValidationState state){
        validateForm();
    }

    public LiveData<Boolean> isButtonEnabled() { return isButtonEnabled; }

    public void setGarmentImage(Bitmap bitmap) {
        garmentImage.setValue(bitmap);
        garmentRepository.validateGarment(bitmap, new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //isImageValid.postValue(result);
                if (result) {
                    // L'immagine è valida
                    imageValidationState.postValue(ImageValidationState.VALID);
                } else {
                    // L'immagine NON è valida, notifica il Fragment per mostrare il dialog
                    imageValidationState.postValue(ImageValidationState.INVALID_CONFIRMATION_NEEDED);
                }
                /*if (!result) {
                    errorMessage.postValue("L'immagine non sembra un capo d'abbigliamento.");
                } else {
                    errorMessage.postValue(null);
                }*/
                //validateForm();
            }
            @Override
            public void onFailure(String error, Throwable t) {
                //isImageValid.postValue(false);
                imageValidationState.postValue(ImageValidationState.ERROR);
                errorMessage.postValue("Errore durante il riconoscimento: " + error);
                //validateForm();
            }
        });
    }

    public void forceImageAsValid() {
        imageValidationState.setValue(ImageValidationState.VALID);
    }

    public void resetImageSelection() {
        garmentImage.setValue(null);
        imageValidationState.setValue(ImageValidationState.UNCHECKED);
    }

    public void saveGarment() {
        Bitmap image = garmentImage.getValue();
        String name = garmentName.getValue();
        String category = selectedCategory.getValue();
        List<String> colors = selectedColors.getValue();
        List<String> styles = selectedStyles.getValue();
        List<String> fabrics = selectedFabrics.getValue();
        String season = selectedSeason.getValue();
        String subCategory = selectedSubCategory.getValue(); // CAMBIATO

        if (image == null || name == null || name.isEmpty() || category == null
                || colors == null || colors.isEmpty() || season == null
                || subCategory == null || subCategory.isEmpty()) { // CAMBIATO
            errorMessage.postValue("Dati mancanti per creare il capo.");
            return;
        }

        Garment garment = new Garment();
        garment.setName(name);
        garment.setCategory(category);
        garment.setColor(colors);
        garment.setStyle(styles);
        garment.setFabric(fabrics);
        garment.setSeason(season);
        garment.setSubCategory(subCategory); // CAMBIATO

        isLoading.postValue(true);
        garmentRepository.addGarment(image, garment, new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                garmentAddedSuccessfully.postValue(true);
                isLoading.postValue(false);
            }
            @Override
            public void onFailure(String error, Throwable t) {
                errorMessage.postValue("Errore durante il salvataggio: " + error);
                garmentAddedSuccessfully.postValue(false);
                isLoading.postValue(false);
            }
        });
    }

    public void setGarmentName(String name) { garmentName.setValue(name); }
    public void setSelectedCategory(String category) {
        if (this.selectedCategory.getValue() == null || !this.selectedCategory.getValue().equals(category)) {
            setSelectedSubCategory(null);
        }
        selectedCategory.setValue(category);
        List<String> availableSubcategories = subcategoryMap.get(category);
        allSubCategories.setValue(availableSubcategories != null ? availableSubcategories : new ArrayList<>());
    }
    public void setSelectedSeason(String season) { selectedSeason.setValue(season); }
    public void setSelectedSubCategory(String subCategory) { selectedSubCategory.setValue(subCategory); } // NUOVO

    public LiveData<ImageValidationState> getImageValidationState() {
        return imageValidationState;
    }
    public LiveData<Boolean> getGarmentAddedSuccessfully() { return garmentAddedSuccessfully; }
    public LiveData<List<String>> getAllColors() { return allColors; }
    public LiveData<List<String>> getAllCategories() { return allCategories; }
    public LiveData<List<String>> getAllStyles() { return allStyles; }
    public LiveData<List<String>> getAllFabrics() { return allFabrics; }
    public LiveData<List<String>> getAllSeasons() { return allSeasons; }
    public LiveData<List<String>> getAllSubCategories() { return allSubCategories; }
    public LiveData<List<String>> getAvailableSubCategories() {
        return allSubCategories;
    }
    public LiveData<List<String>> getSelectedColors() { return selectedColors; }
    public LiveData<List<String>> getSelectedStyles() { return selectedStyles; }
    public LiveData<List<String>> getSelectedFabrics() { return selectedFabrics; }
    public LiveData<String> getSelectedSeason() { return selectedSeason; }
    public LiveData<String> getSelectedCategory() { return selectedCategory; }
    public LiveData<String> getSelectedSubCategory() { return selectedSubCategory; }

    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void updateSelectedColors(List<String> newSelection) { selectedColors.setValue(newSelection); }
    public void updateSelectedStyles(List<String> newSelection) { selectedStyles.setValue(newSelection); }
    public void updateSelectedFabrics(List<String> newSelection) { selectedFabrics.setValue(newSelection); }
}