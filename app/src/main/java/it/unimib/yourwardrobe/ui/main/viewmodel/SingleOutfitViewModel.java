package it.unimib.yourwardrobe.ui.main.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.model.Outfit;
import it.unimib.yourwardrobe.domain.repository.OutfitRepository;

@HiltViewModel
public class SingleOutfitViewModel extends ViewModel {
    private final OutfitRepository outfitRepository;

    private final MutableLiveData<Outfit> outfit = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEditMode = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> outfitDeleted = new MutableLiveData<>();
    private final MutableLiveData<Boolean> outfitUpdated = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    @Inject
    public SingleOutfitViewModel(OutfitRepository outfitRepository) {
        this.outfitRepository = outfitRepository;
    }

    public void setOutfit(Outfit outfitData) { outfit.setValue(outfitData); }
    public LiveData<Outfit> getOutfit() { return outfit; }
    public LiveData<Boolean> getIsEditMode() { return isEditMode; }
    public LiveData<Boolean> getOutfitDeleted() { return outfitDeleted; }
    public LiveData<Boolean> getOutfitUpdated() { return outfitUpdated; }

    public void enterEditMode() { isEditMode.setValue(true); }
    public void cancelEdit() { isEditMode.setValue(false); }

    public void removeGarment(Garment garment) {
        Outfit current = outfit.getValue();
        if (current != null && current.getGarments() != null) {
            List<Garment> updatedList = new ArrayList<>(current.getGarments());
            updatedList.remove(garment);
            current.setGarments(updatedList);
            outfit.setValue(current); // Notifica la UI per aggiornare il collage
        }
    }

    public void saveChanges(String newName) {
        Outfit current = outfit.getValue();
        if (current != null) {
            current.setName(newName);
            outfitRepository.updateOutfit(current, new Callback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    isEditMode.postValue(false);
                    outfitUpdated.postValue(true);
                }
                @Override
                public void onFailure(String error, Throwable t) { errorMessage.postValue(error); }
            });
        }
    }

    public void deleteOutfit() {
        Outfit current = outfit.getValue();
        if (current != null) {
            outfitRepository.deleteOutfit(current, new Callback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) { outfitDeleted.postValue(true); }
                @Override
                public void onFailure(String error, Throwable t) { errorMessage.postValue(error); }
            });
        }
    }


}