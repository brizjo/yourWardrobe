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

    @Inject
    public GarmentViewModel(@NonNull Application application, GarmentRepository garmentRepository) {
        super(application);
        this.garmentRepository = garmentRepository;
    }

    public LiveData<Garment> getGarment(){return garment;}
    public LiveData<Boolean> getIsDeleted(){return isDeleted;}
    public LiveData<Boolean> getIsEditMode() {
        return isEditMode;
    }

    public void enterEditMode() {
        isEditMode.setValue(true);
    }
    public void exitEditMode() {
        isEditMode.setValue(false);
    }
    public void setGarment(Garment garment){
        this.garment.postValue(garment);
        this.originalGarment = new Garment(garment);
    }

    public void deleteGarment(){
        Garment currentGarment = garment.getValue();
        if(currentGarment != null) {
            garmentRepository.deleteGarment(currentGarment, new Callback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    isDeleted.postValue(true);
                }

                @Override
                public void onFailure(String error, Throwable t) {
                    GarmentViewModel.this.error.postValue(error);
                }
            });
        }
    }

    public LiveData<Boolean> getGarmentUpdatedSuccessfully() {
        return garmentUpdatedSuccessfully;
    }

    public void updateGarment() {
        Garment currentGarment = garment.getValue();
        if (currentGarment != null && currentGarment.equals(originalGarment)) {
            Log.i("GarmentViewModel", "Nessuna modifica rilevata. Aggiornamento saltato.");
            exitEditMode();
            garmentUpdatedSuccessfully.postValue(true);
            return; // Interrompi l'esecuzione qui
        }
        garmentRepository.updateGarment(currentGarment, new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                garmentUpdatedSuccessfully.postValue(true);
                // Esci dalla modalità modifica dopo aver salvato
                exitEditMode();
            }

            @Override
            public void onFailure(String error, Throwable t) {
                // Qui potresti voler notificare un errore specifico per l'update
                Log.e("GarmentViewModel", "Update fallito: " + error, t);
                garmentUpdatedSuccessfully.postValue(false);
            }
        });
    }

    public void cancelChanges() {
        // Ripristina il LiveData con la copia originale non modificata
        if (originalGarment != null) {
            garment.setValue(new Garment(originalGarment));
        }
        exitEditMode();
    }

    public void setGarmentName(String name) {
        Garment currentGarment = garment.getValue();
        if (currentGarment != null && !name.equals(currentGarment.getName())) {
            currentGarment.setName(name);
            garment.setValue(currentGarment);
        }
    }
    public List<String> getAllColors() {
        // recupera i dati da array di risorse
        return Arrays.asList(getApplication().getResources().getStringArray(R.array.garment_color));
    }
    public List<String> getAllStyles() {
        return Arrays.asList(getApplication().getResources().getStringArray(R.array.garment_styles));
    }
    public List<String> getAllFabrics() {
        return Arrays.asList(getApplication().getResources().getStringArray(R.array.fabric_types));
    }

    public void addColors(List<String> colorsToAdd) {
        Garment currentGarment = garment.getValue();
        if (currentGarment != null) {
            Garment newGarment = new Garment(currentGarment);
            if (newGarment.getColor() == null) {
                newGarment.setColor(new ArrayList<>());
            }
            // Aggiunge solo gli elementi non già presenti per sicurezza
            for (String color : colorsToAdd) {
                if (!newGarment.getColor().contains(color)) {
                    newGarment.getColor().add(color);
                }
            }
            garment.setValue(newGarment); // Notifica l'aggiornamento alla UI
        }
    }
    public void addStyles(List<String> stylesToAdd) {
        Garment currentGarment = garment.getValue();
        if (currentGarment != null) {
            Garment newGarment = new Garment(currentGarment);
            if (newGarment.getStyle() == null) {
                newGarment.setStyle(new ArrayList<>());
            }
            for (String style : stylesToAdd) {
                if (!newGarment.getStyle().contains(style)) {
                    newGarment.getStyle().add(style);
                }
            }
            garment.setValue(newGarment);
        }
    }
    public void addFabrics(List<String> fabricsToAdd) {
        Garment currentGarment = garment.getValue();
        if (currentGarment != null) {
            Garment newGarment = new Garment(currentGarment);
            if (newGarment.getFabric() == null) {
                newGarment.setFabric(new ArrayList<>());
            }
            for (String fabric : fabricsToAdd) {
                if (!newGarment.getFabric().contains(fabric)) {
                    newGarment.getFabric().add(fabric);
                }
            }
            garment.setValue(newGarment);
        }
    }
    public void removeColor(String colorToRemove) {
        Garment currentGarment = this.garment.getValue();
        if (currentGarment != null && currentGarment.getColor() != null) {
            Garment newGarment = new Garment(currentGarment);
            newGarment.getColor().remove(colorToRemove);
            // Notifica il cambiamento al LiveData per aggiornare la UI
            this.garment.setValue(newGarment);
        }
    }

    public void removeStyle(String styleToRemove) {
        Garment currentGarment = garment.getValue();
        if (currentGarment != null && currentGarment.getStyle() != null) {
            Garment newGarment = new Garment(currentGarment);
            newGarment.getStyle().remove(styleToRemove);
            this.garment.setValue(newGarment);
        }
    }

    public void removeFabric(String fabricToRemove) {
        Garment currentGarment = garment.getValue();
        if (currentGarment != null && currentGarment.getFabric() != null) {
            Garment newGarment = new Garment(currentGarment);
            newGarment.getFabric().remove(fabricToRemove);
            this.garment.setValue(newGarment);
        }
    }
}
