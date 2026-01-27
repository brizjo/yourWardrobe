package it.unimib.yourwardrobe.ui.main.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.MediatorLiveData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import it.unimib.yourwardrobe.R;

public class AddGarmentViewModel extends AndroidViewModel {

    // LiveData per i dati "statici" (le liste complete)
    private final MutableLiveData<List<String>> allColors = new MutableLiveData<>();
    private final MutableLiveData<List<String>> allCategories = new MutableLiveData<>();
    private final MutableLiveData<List<String>> allStyles = new MutableLiveData<>();
    private final MutableLiveData<List<String>> allFabrics = new MutableLiveData<>();

    // LiveData per lo stato "dinamico" (le selezioni dell'utente)
    private final MutableLiveData<List<String>> selectedColors = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> garmentImage = new MutableLiveData<>();
    private final MutableLiveData<String> garmentName = new MutableLiveData<>();
    private final MutableLiveData<String> selectedCategory = new MutableLiveData<>();
    private final MutableLiveData<List<String>> selectedStyles = new MutableLiveData<>();
    private final MutableLiveData<List<String>> selectedFabrics = new MutableLiveData<>();
    private final MediatorLiveData<Boolean> isButtonEnabled = new MediatorLiveData<>();

    public AddGarmentViewModel(@NonNull Application application) {
        super(application);
        // Inizializza le liste di dati
        loadInitialData();
        // Inizializza la lista delle selezioni come vuota
        selectedColors.setValue(new ArrayList<>());
        selectedStyles.setValue(new ArrayList<>());
        selectedFabrics.setValue(new ArrayList<>());
        isButtonEnabled.setValue(false); // Inizialmente disabilitato

        // Aggiungi le sorgenti da osservare
        isButtonEnabled.addSource(garmentImage, value -> validateForm());
        isButtonEnabled.addSource(garmentName, value -> validateForm());
        isButtonEnabled.addSource(selectedCategory, value -> validateForm());
        isButtonEnabled.addSource(getSelectedColors(), value -> validateForm());
        isButtonEnabled.addSource(getSelectedStyles(), value -> validateForm());
        isButtonEnabled.addSource(getSelectedFabrics(), value -> validateForm());
    }

    // Metodo per caricare i dati dalle risorse (potrebbe venire da un Repository in futuro)
    private void loadInitialData() {
        allColors.setValue(Arrays.asList(getApplication().getResources().getStringArray(R.array.garment_color)));
        allCategories.setValue(Arrays.asList(getApplication().getResources().getStringArray(R.array.categories)));
        allStyles.setValue(Arrays.asList(getApplication().getResources().getStringArray(R.array.garment_styles)));
        allFabrics.setValue(Arrays.asList(getApplication().getResources().getStringArray(R.array.fabric_types)));
    }

    private void validateForm() {
        boolean hasImage = garmentImage.getValue() != null;
        boolean hasName = garmentName.getValue() != null && !garmentName.getValue().isEmpty();
        boolean hasSeason = selectedCategory.getValue() != null && !selectedCategory.getValue().isEmpty();
        boolean hasColors = getSelectedColors().getValue() != null && !getSelectedColors().getValue().isEmpty();
        boolean hasStyles = getSelectedStyles().getValue() != null && !getSelectedStyles().getValue().isEmpty();
        boolean hasFabrics = getSelectedFabrics().getValue() != null && !getSelectedFabrics().getValue().isEmpty();

        isButtonEnabled.setValue(hasImage && hasName && hasSeason && hasColors && hasStyles && hasFabrics);
    }

    public LiveData<Boolean> isButtonEnabled() {
        return isButtonEnabled;
    }

    public void setGarmentImage(Bitmap bitmap) {
        garmentImage.setValue(bitmap);
    }

    // Chiamato dal Fragment quando il testo del nome cambia
    public void setGarmentName(String name) {
        garmentName.setValue(name);
    }

    // Chiamato dal Fragment quando la categoria viene selezionata
    public void setSelectedCategory(String category) {
        selectedCategory.setValue(category);
    }
    // Espone i LiveData (sola lettura) al Fragment
    public LiveData<List<String>> getAllColors() {
        return allColors;
    }

    public LiveData<List<String>> getAllCategories() {
        return allCategories;
    }

    public LiveData<List<String>> getAllStyles() {
        return allStyles;
    }

    public LiveData<List<String>> getAllFabrics() {
        return allFabrics;
    }
    public LiveData<List<String>> getSelectedColors() {
        return selectedColors;
    }

    // Metodo chiamato dal Fragment quando l'utente conferma la selezione dei colori
    public void updateSelectedColors(List<String> newSelection) {
        selectedColors.setValue(newSelection);
    }

    public LiveData<List<String>> getSelectedStyles() {
        return selectedStyles;
    }
    public void updateSelectedStyles(List<String> newSelection) {
        selectedStyles.setValue(newSelection);
    }

    public LiveData<List<String>> getSelectedFabrics() {
        return selectedFabrics;
    }
    public void updateSelectedFabrics(List<String> newSelection) {
        selectedFabrics.setValue(newSelection);
    }
}