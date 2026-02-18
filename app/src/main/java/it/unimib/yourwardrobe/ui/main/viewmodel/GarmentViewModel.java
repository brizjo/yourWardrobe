package it.unimib.yourwardrobe.ui.main.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.ui.common.ImageValidationState;
import it.unimib.yourwardrobe.utils.Callback;

@HiltViewModel
public class GarmentViewModel extends AndroidViewModel {

    private final GarmentRepository garmentRepository;
    private final Map<String, List<String>> subcategoryMap = new HashMap<>();
    private final MutableLiveData<Garment> garment = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isDeleted = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEditMode = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> garmentUpdatedSuccessfully = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<ImageValidationState> imageValidationState = new MutableLiveData<>(ImageValidationState.UNCHECKED);
    private Garment originalGarment;
    private Bitmap pendingNewImage = null;

    @Inject
    public GarmentViewModel(@NonNull Application application, GarmentRepository garmentRepository) {
        super(application);
        this.garmentRepository = garmentRepository;
        loadSubcategoryOptions();
    }

    private void loadSubcategoryOptions() {
        Application app = getApplication();
        subcategoryMap.put(app.getString(R.string.top_garment), Arrays.asList(app.getResources().getStringArray(R.array.subcategories_top)));
        subcategoryMap.put(app.getString(R.string.bottom_garment), Arrays.asList(app.getResources().getStringArray(R.array.subcategories_bottom)));
        subcategoryMap.put(app.getString(R.string.footwear), Arrays.asList(app.getResources().getStringArray(R.array.subcategories_footwear)));
        subcategoryMap.put(app.getString(R.string.accessory), Arrays.asList(app.getResources().getStringArray(R.array.subcategories_accessories)));
    }

    public LiveData<Garment> getGarment() {
        return garment;
    }

    public void setGarment(Garment garment) {
        this.garment.postValue(garment);
        this.originalGarment = new Garment(garment);
    }

    public LiveData<Boolean> getIsDeleted() {
        return isDeleted;
    }

    public LiveData<Boolean> getIsEditMode() {
        return isEditMode;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getGarmentUpdatedSuccessfully() {
        return garmentUpdatedSuccessfully;
    }

    public LiveData<ImageValidationState> getImageValidationState() {
        return imageValidationState;
    }

    public void enterEditMode() {
        isEditMode.setValue(true);
    }

    public void exitEditMode() {
        isEditMode.setValue(false);
    }

    // -------------------------------------------------------------------------
    // Image change
    // -------------------------------------------------------------------------

    /**
     * Chiamato quando l'utente sceglie una nuova foto.
     * Valida prima tramite ML Kit, poi salva solo se valida.
     */
    public void onNewImageSelected(Bitmap bitmap) {
        imageValidationState.setValue(ImageValidationState.UNCHECKED);
        pendingNewImage = bitmap;

        // IMPORTANTE: Entra in modalità edit se non lo sei già
        if (Boolean.FALSE.equals(isEditMode.getValue())) {
            isEditMode.setValue(true);
        }

        isLoading.setValue(true);

        garmentRepository.validateGarment(bitmap, new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isLoading.postValue(false);
                if (result) {
                    imageValidationState.postValue(ImageValidationState.VALID);
                } else {
                    pendingNewImage = null;
                    imageValidationState.postValue(ImageValidationState.INVALID_CONFIRMATION_NEEDED);
                }
            }

            @Override
            public void onFailure(String error, Throwable t) {
                isLoading.postValue(false);
                pendingNewImage = null;
                imageValidationState.postValue(ImageValidationState.ERROR);
                GarmentViewModel.this.error.postValue("Errore validazione immagine: " + error);
            }
        });
    }

    /**
     * Forza l'immagine come valida (es. l'utente conferma nel dialog).
     */
    public void forceImageAsValid(Bitmap bitmap) {
        pendingNewImage = bitmap;
        imageValidationState.setValue(ImageValidationState.VALID);
    }

    /**
     * Annulla la selezione della nuova immagine.
     */
    public void cancelImageChange() {
        pendingNewImage = null;
        imageValidationState.setValue(ImageValidationState.UNCHECKED);
    }

    // -------------------------------------------------------------------------
    // Delete / Update
    // -------------------------------------------------------------------------

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

        if (currentGarment != null && !hasChanges(currentGarment, originalGarment)
                && pendingNewImage == null) {
            Log.i("GarmentViewModel", "Nessuna modifica rilevata. Aggiornamento saltato.");
            exitEditMode();
            garmentUpdatedSuccessfully.postValue(true);
            return;
        }

        isLoading.setValue(true);

        // Se c'è una nuova immagine, prima aggiorna l'immagine poi i dati
        if (pendingNewImage != null) {
            garmentRepository.updateGarmentImage(pendingNewImage, currentGarment, new Callback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    pendingNewImage = null;
                    // Dopo aver aggiornato l'immagine, aggiorna anche i dati testuali
                    updateGarmentData(currentGarment);
                }

                @Override
                public void onFailure(String error, Throwable t) {
                    isLoading.postValue(false);
                    Log.e("GarmentViewModel", "Errore aggiornamento immagine: " + error, t);
                    GarmentViewModel.this.error.postValue("Errore aggiornamento immagine: " + error);
                }
            });
        } else {
            updateGarmentData(currentGarment);
        }
    }

    private void updateGarmentData(Garment currentGarment) {
        if (!hasChanges(currentGarment, originalGarment)) {
            isLoading.postValue(false);
            originalGarment = new Garment(currentGarment);
            garmentUpdatedSuccessfully.postValue(true);
            exitEditMode();
            return;
        }
        garmentRepository.updateGarment(currentGarment, new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Log.d("GarmentViewModel", "Update riuscito");
                originalGarment = new Garment(currentGarment);
                isLoading.postValue(false);
                garmentUpdatedSuccessfully.postValue(true);
                exitEditMode();
            }

            @Override
            public void onFailure(String error, Throwable t) {
                Log.e("GarmentViewModel", "Errore update: " + error, t);
                isLoading.postValue(false);
                garmentUpdatedSuccessfully.postValue(false);
            }
        });
    }

    private boolean hasChanges(Garment current, Garment original) {
        if (original == null) return true;
        if (current == null) return false;

        boolean nameChanged = !Objects.equals(current.getName(), original.getName());
        boolean seasonChanged = !Objects.equals(current.getSeason(), original.getSeason());
        boolean subCategoryChanged = !Objects.equals(current.getSubCategory(), original.getSubCategory());
        boolean colorsChanged = !Objects.equals(current.getColor(), original.getColor());
        boolean stylesChanged = !Objects.equals(current.getStyle(), original.getStyle());
        boolean fabricsChanged = !Objects.equals(current.getFabric(), original.getFabric());

        return nameChanged || seasonChanged || subCategoryChanged ||
                colorsChanged || stylesChanged || fabricsChanged;
    }

    public void cancelChanges() {
        if (originalGarment != null) {
            // Crea una nuova istanza per forzare l'observer a reagire
            Garment restoredGarment = new Garment(originalGarment);
            garment.setValue(restoredGarment);
        }
        pendingNewImage = null;
        imageValidationState.setValue(ImageValidationState.UNCHECKED);
        exitEditMode();

        // Forza il refresh per far sì che l'UI ricarichi l'immagine originale
        if (originalGarment != null) {
            garment.postValue(new Garment(originalGarment));
        }
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
                "Tutte le stagioni", "Primavera", "Estate", "Autunno", "Inverno",
                "Inverno - Autunno", "Primavera - Estate", "Primavera - Autunno"
        );
    }

    public List<String> getAvailableSubcategoriesForGarment() {
        Garment currentGarment = garment.getValue();
        if (currentGarment != null && currentGarment.getCategory() != null
                && !currentGarment.getCategory().isEmpty()) {
            List<String> subcategories = subcategoryMap.get(currentGarment.getCategory());
            return subcategories != null ? subcategories : new ArrayList<>();
        }
        return new ArrayList<>();
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