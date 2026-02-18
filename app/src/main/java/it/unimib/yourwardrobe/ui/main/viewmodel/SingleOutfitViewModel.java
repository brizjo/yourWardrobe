package it.unimib.yourwardrobe.ui.main.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.model.Outfit;
import it.unimib.yourwardrobe.domain.repository.OutfitRepository;
import it.unimib.yourwardrobe.utils.Callback;

@HiltViewModel
public class SingleOutfitViewModel extends ViewModel {

    private final OutfitRepository outfitRepository;

    private final MutableLiveData<Outfit> outfit = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEditMode = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> outfitDeleted = new MutableLiveData<>();
    private final MutableLiveData<Boolean> outfitUpdated = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Copia dell'outfit originale per il ripristino in caso di annulla
    private Outfit originalOutfit;

    @Inject
    public SingleOutfitViewModel(OutfitRepository outfitRepository) {
        this.outfitRepository = outfitRepository;
    }

    public LiveData<Outfit> getOutfit() {
        return outfit;
    }

    public void setOutfit(Outfit outfitData) {
        outfit.setValue(outfitData);
        originalOutfit = deepCopyOutfit(outfitData); // salva copia originale
    }

    public LiveData<Boolean> getIsEditMode() {
        return isEditMode;
    }

    public LiveData<Boolean> getOutfitDeleted() {
        return outfitDeleted;
    }

    public LiveData<Boolean> getOutfitUpdated() {
        return outfitUpdated;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void enterEditMode() {
        // Aggiorna la copia originale ogni volta che si entra in edit mode
        originalOutfit = deepCopyOutfit(outfit.getValue());
        isEditMode.setValue(true);
    }

    public void cancelEdit() {
        // Ripristina l'outfit originale e notifica la UI
        if (originalOutfit != null) {
            outfit.setValue(deepCopyOutfit(originalOutfit));
        }
        isEditMode.setValue(false);
    }

    public void removeGarment(Garment garment) {
        Outfit current = outfit.getValue();
        if (current == null) return;

        List<Garment> list = new ArrayList<>(current.getGarments());

        String cat = garment.getCategory().toLowerCase();
        int tops = 0, bottoms = 0;
        for (Garment g : list) {
            if (g.getCategory().toLowerCase().contains("superiore")) tops++;
            if (g.getCategory().toLowerCase().contains("inferiore")) bottoms++;
        }

        if (cat.contains("superiore") && tops <= 1) {
            errorMessage.setValue("Un outfit deve avere almeno una parte superiore.");
        } else if (cat.contains("inferiore") && bottoms <= 1) {
            errorMessage.setValue("Un outfit deve avere almeno una parte inferiore.");
        } else {
            list.remove(garment);
            current.setGarments(list);
            outfit.setValue(current);
        }
    }

    public void saveChanges(String newName) {
        Outfit current = outfit.getValue();
        if (current != null) {
            current.setName(newName);
            outfitRepository.updateOutfit(current, new Callback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    // Aggiorna la copia originale con i nuovi dati salvati
                    originalOutfit = deepCopyOutfit(current);
                    isEditMode.postValue(false);
                    outfitUpdated.postValue(true);
                }

                @Override
                public void onFailure(String error, Throwable t) {
                    errorMessage.postValue(error);
                }
            });
        }
    }

    public void deleteOutfit() {
        Outfit current = outfit.getValue();
        if (current != null) {
            outfitRepository.deleteOutfit(current, new Callback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    outfitDeleted.postValue(true);
                }

                @Override
                public void onFailure(String error, Throwable t) {
                    errorMessage.postValue(error);
                }
            });
        }
    }

    public void updateSeason(String newSeason) {
        Outfit current = outfit.getValue();
        if (current != null) {
            current.setSeason(newSeason);
            outfit.setValue(current);
        }
    }

    public void addGarment(Garment newGarment) {
        Outfit current = outfit.getValue();
        if (current == null) return;

        List<Garment> list = new ArrayList<>(current.getGarments());

        if (list.size() >= 6) {
            errorMessage.setValue("La carta può mostrare massimo 6 capi.");
            return;
        }

        String cat = newGarment.getCategory().toLowerCase();
        int tops = 0, bottoms = 0, shoes = 0, accessories = 0;
        for (Garment g : list) {
            String c = g.getCategory().toLowerCase();
            if (c.contains("superiore")) tops++;
            else if (c.contains("inferiore")) bottoms++;
            else if (c.contains("scarpe") || c.contains("calzature")) shoes++;
            else accessories++;
        }

        if (cat.contains("superiore") && tops >= 3) {
            errorMessage.setValue("Massimo 3 parti superiori consentite.");
        } else if (cat.contains("inferiore") && bottoms >= 1) {
            errorMessage.setValue("Massimo 1 parte inferiore consentita.");
        } else if ((cat.contains("scarpe") || cat.contains("calzature")) && shoes >= 1) {
            errorMessage.setValue("Massimo 1 paio di scarpe consentito.");
        } else if (cat.contains("accessorio") && accessories >= 4) {
            errorMessage.setValue("Massimo 4 accessori consentiti.");
        } else {
            list.add(newGarment);
            current.setGarments(list);
            outfit.setValue(current);
        }
    }

    public void replaceGarment(Garment oldGarment, Garment newGarment) {
        Outfit current = outfit.getValue();
        if (current != null) {
            List<Garment> updatedList = new ArrayList<>(current.getGarments());
            int index = -1;
            for (int i = 0; i < updatedList.size(); i++) {
                if (updatedList.get(i).getId().equals(oldGarment.getId())) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                updatedList.set(index, newGarment);
                current.setGarments(updatedList);
                outfit.setValue(current);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Deep copy helper
    // -------------------------------------------------------------------------

    private Outfit deepCopyOutfit(Outfit source) {
        if (source == null) return null;
        List<Garment> garmentsCopy = new ArrayList<>(source.getGarments());
        Outfit copy = new Outfit(source.getName(), source.getSeason(), garmentsCopy);
        copy.setId(source.getId());
        return copy;
    }
}