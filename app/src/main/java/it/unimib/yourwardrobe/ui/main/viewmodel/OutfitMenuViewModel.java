package it.unimib.yourwardrobe.ui.main.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Outfit;
import it.unimib.yourwardrobe.domain.repository.OutfitRepository;

@HiltViewModel
public class OutfitMenuViewModel extends ViewModel {

    private final OutfitRepository outfitRepository;

    private final MutableLiveData<List<Outfit>> outfits = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    @Inject
    public OutfitMenuViewModel(OutfitRepository outfitRepository) {
        this.outfitRepository = outfitRepository;
        fetchOutfits();
    }

    public void fetchOutfits() {
        outfitRepository.getOutfits(new Callback<List<Outfit>>() {
            @Override
            public void onSuccess(List<Outfit> result) {
                outfits.postValue(result);
            }

            @Override
            public void onFailure(String error, Throwable t) {
                errorMessage.postValue(error);
            }
        });
    }

    public LiveData<List<Outfit>> getOutfits() { return outfits; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
