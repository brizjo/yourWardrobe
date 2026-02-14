package it.unimib.yourwardrobe.ui.main.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.R;

@HiltViewModel
public class GarmentViewModel extends AndroidViewModel {

    private final GarmentRepository garmentRepository;
    private Garment originalGarment;
    private final MutableLiveData<Garment> garment = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isDeleted = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEditMode = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> garmentUpdatedSuccessfully = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    @Inject
    public GarmentViewModel(@NonNull Application application, GarmentRepository garmentRepository) {
        super(application);
        this.garmentRepository = garmentRepository;
    }

    public LiveData<Garment> getGarment() { return garment; }
    public LiveData<Boolean> getIsDeleted() { return isDeleted; }
    public LiveData<Boolean> getIsEditMode() { return isEditMode; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getGarmentUpdatedSuccessfully() { return garmentUpdatedSuccessfully; }

    public void enterEditMode() { isEditMode.setValue(true); }
    public void exitEditMode() { isEditMode.setValue(false); }

    public void setGarment(Garment garment) {
        this.garment.postValue(garment);
        this.originalGarment = new Garment(garment);
    }

    public void deleteGarment() {
        Garment currentGarment = garment.getValue();
        if (currentGarment != null) {
            garmentRepository.deleteGarment(currentGarment, new Callback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    isDeleted.postValue(true);
                }
                @Override
                public void onFailure(String error, Throwable t) {
                    isDeleted.postValue(false);
                    GarmentViewModel.this.error.postValue(error);
                }
            });
        }
    }

    public void updateGarment() {
        Garment currentGarment = garment.getValue();

        Log.d("GarmentViewModel", "========== UPDATE GARMENT ==========");
        Log.d("GarmentViewModel", "Current: " + (currentGarment != null ? currentGarment.getName() : "null"));
        Log.d("GarmentViewModel", "Original: " + (originalGarment != null ? originalGarment.getName() : "null"));

        if (currentGarment != null && !hasChanges(currentGarment, originalGarment)) {
            Log.i("GarmentViewModel", "⚠️ Nessuna modifica rilevata. Aggiornamento saltato.");
            exitEditMode();
            garmentUpdatedSuccessfully.postValue(true);
            return;
        }

        Log.d("GarmentViewModel", "✅ Modifiche rilevate, procedo con update");

        garmentRepository.updateGarment(currentGarment, new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Log.d("GarmentViewModel", "✅✅✅ UPDATE RIUSCITO!");
                garmentUpdatedSuccessfully.postValue(true);
                // Aggiorna l'originalGarment con i nuovi valori
                originalGarment = new Garment(currentGarment);
                exitEditMode();
            }
            @Override
            public void onFailure(String error, Throwable t) {
                Log.e("GarmentViewModel", "❌❌❌ ERRORE UPDATE: " + error, t);
                garmentUpdatedSuccessfully.postValue(false);
            }
        });
    }

    /**
     * Verifica se ci sono modifiche tra il garment corrente e quello originale
     */
    private boolean hasChanges(Garment current, Garment original) {
        if (original == null) return true;
        if (current == null) return false;

        boolean nameChanged = !Objects.equals(current.getName(), original.getName());
        boolean seasonChanged = !Objects.equals(current.getSeason(), original.getSeason());
        boolean subCategoryChanged = !Objects.equals(current.getSubCategory(), original.getSubCategory());
        boolean colorsChanged = !Objects.equals(current.getColor(), original.getColor());
        boolean stylesChanged = !Objects.equals(current.getStyle(), original.getStyle());
        boolean fabricsChanged = !Objects.equals(current.getFabric(), original.getFabric());

        Log.d("GarmentViewModel", "Name changed: " + nameChanged);
        Log.d("GarmentViewModel", "Season changed: " + seasonChanged);
        Log.d("GarmentViewModel", "SubCategory changed: " + subCategoryChanged);
        Log.d("GarmentViewModel", "Colors changed: " + colorsChanged);
        Log.d("GarmentViewModel", "Styles changed: " + stylesChanged);
        Log.d("GarmentViewModel", "Fabrics changed: " + fabricsChanged);

        return nameChanged || seasonChanged || subCategoryChanged ||
                colorsChanged || stylesChanged || fabricsChanged;
    }

    public void cancelChanges() {
        if (originalGarment != null) {
            garment.setValue(new Garment(originalGarment));
        }
        exitEditMode();
    }

    public void setGarmentName(String name) {
        Garment currentGarment = garment.getValue();
        if (currentGarment != null && !name.equals(currentGarment.getName())) {
            Garment newGarment = new Garment(currentGarment);
            newGarment.setName(name);
            garment.setValue(newGarment);
        }
    }

    // -------------------------------------------------------------------------
    // Getter liste opzioni
    // -------------------------------------------------------------------------

    public List<String> getAllColors() {
        return Arrays.asList(getApplication().getResources().getStringArray(R.array.garment_color));
    }

    public List<String> getAllStyles() {
        return Arrays.asList(getApplication().getResources().getStringArray(R.array.garment_styles));
    }

    public List<String> getAllFabrics() {
        return Arrays.asList(getApplication().getResources().getStringArray(R.array.fabric_types));
    }

    public List<String> getAllSeasons() {
        return Arrays.asList(
                "Tutte le stagioni",
                "Primavera",
                "Estate",
                "Autunno",
                "Inverno",
                "Inverno - Autunno",
                "Primavera - Estate",
                "Primavera - Autunno"
        );
    }

    public List<String> getAllSubCategories() {
        return Arrays.asList(getApplication().getResources().getStringArray(R.array.subcategories));
    }

    // -------------------------------------------------------------------------
    // Season
    // -------------------------------------------------------------------------

    public void setSelectedSeason(String season) {
        Garment currentGarment = garment.getValue();
        if (currentGarment != null) {
            Garment newGarment = new Garment(currentGarment);
            newGarment.setSeason(season);
            garment.setValue(newGarment);
        }
    }

    public void clearSeason() {
        Garment currentGarment = garment.getValue();
        if (currentGarment != null) {
            Garment newGarment = new Garment(currentGarment);
            newGarment.setSeason(null);
            garment.setValue(newGarment);
        }
    }

    // -------------------------------------------------------------------------
    // SubCategory
    // -------------------------------------------------------------------------

    public void setSubCategory(String subCategory) {
        Garment currentGarment = garment.getValue();
        if (currentGarment != null) {
            Garment newGarment = new Garment(currentGarment);
            newGarment.setSubCategory(subCategory);
            garment.setValue(newGarment);
        }
    }

    // -------------------------------------------------------------------------
    // Add
    // -------------------------------------------------------------------------

    public void addColors(List<String> colorsToAdd) {
        Garment currentGarment = garment.getValue();
        if (currentGarment != null) {
            Garment newGarment = new Garment(currentGarment);
            if (newGarment.getColor() == null) newGarment.setColor(new ArrayList<>());
            for (String color : colorsToAdd) {
                if (!newGarment.getColor().contains(color)) newGarment.getColor().add(color);
            }
            garment.setValue(newGarment);
        }
    }

    public void addStyles(List<String> stylesToAdd) {
        Garment currentGarment = garment.getValue();
        if (currentGarment != null) {
            Garment newGarment = new Garment(currentGarment);
            if (newGarment.getStyle() == null) newGarment.setStyle(new ArrayList<>());
            for (String style : stylesToAdd) {
                if (!newGarment.getStyle().contains(style)) newGarment.getStyle().add(style);
            }
            garment.setValue(newGarment);
        }
    }

    public void addFabrics(List<String> fabricsToAdd) {
        Garment currentGarment = garment.getValue();
        if (currentGarment != null) {
            Garment newGarment = new Garment(currentGarment);
            if (newGarment.getFabric() == null) newGarment.setFabric(new ArrayList<>());
            for (String fabric : fabricsToAdd) {
                if (!newGarment.getFabric().contains(fabric)) newGarment.getFabric().add(fabric);
            }
            garment.setValue(newGarment);
        }
    }

    // -------------------------------------------------------------------------
    // Remove
    // -------------------------------------------------------------------------

    public void removeColor(String colorToRemove) {
        Garment currentGarment = garment.getValue();
        if (currentGarment != null && currentGarment.getColor() != null) {
            Garment newGarment = new Garment(currentGarment);
            newGarment.getColor().remove(colorToRemove);
            garment.setValue(newGarment);
        }
    }

    public void removeStyle(String styleToRemove) {
        Garment currentGarment = garment.getValue();
        if (currentGarment != null && currentGarment.getStyle() != null) {
            Garment newGarment = new Garment(currentGarment);
            newGarment.getStyle().remove(styleToRemove);
            garment.setValue(newGarment);
        }
    }

    public void removeFabric(String fabricToRemove) {
        Garment currentGarment = garment.getValue();
        if (currentGarment != null && currentGarment.getFabric() != null) {
            Garment newGarment = new Garment(currentGarment);
            newGarment.getFabric().remove(fabricToRemove);
            garment.setValue(newGarment);
        }
    }
}