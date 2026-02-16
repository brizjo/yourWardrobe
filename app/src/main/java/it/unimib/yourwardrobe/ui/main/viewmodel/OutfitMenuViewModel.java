package it.unimib.yourwardrobe.ui.main.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.model.Outfit;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.domain.repository.OutfitRepository;

@HiltViewModel
public class OutfitMenuViewModel extends ViewModel {

    private final OutfitRepository outfitRepository;
    private final GarmentRepository garmentRepository;
    public enum UiState {
        LOADING,
        NOT_ENOUGH_GARMENTS,
        NO_OUTFITS,
        HAS_OUTFITS
    }

    private final MutableLiveData<List<Outfit>> outfits = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<UiState> uiState = new MutableLiveData<>(UiState.LOADING);

    @Inject
    public OutfitMenuViewModel(OutfitRepository outfitRepository, GarmentRepository garmentRepository) {
        this.outfitRepository = outfitRepository;
        this.garmentRepository = garmentRepository;
        checkDataAndFetchOutfits();
    }

    private void checkDataAndFetchOutfits() {
        uiState.setValue(UiState.LOADING);

        garmentRepository.getGarments(new Callback<List<Garment>>() {
            @Override
            public void onSuccess(List<Garment> garments) {
                boolean hasTop = garments.stream().anyMatch(g -> "Parte superiore".equals(g.getCategory()));
                boolean hasBottom = garments.stream().anyMatch(g -> "Parte inferiore".equals(g.getCategory()));

                if (hasTop && hasBottom) {
                    fetchOutfits();
                } else {
                    uiState.postValue(UiState.NOT_ENOUGH_GARMENTS);
                }
            }

            @Override
            public void onFailure(String error, Throwable t) {
                errorMessage.postValue(error);
                uiState.postValue(UiState.NOT_ENOUGH_GARMENTS); // Gestisci l'errore come se non ci fossero capi
            }
        });
    }

    public void fetchOutfits() {
        outfitRepository.getOutfits(new Callback<List<Outfit>>() {
            @Override
            public void onSuccess(List<Outfit> result) {
                outfits.postValue(result);
                if (result == null || result.isEmpty()) {
                    uiState.postValue(UiState.NO_OUTFITS);
                } else {
                    uiState.postValue(UiState.HAS_OUTFITS);
                }
            }

            @Override
            public void onFailure(String error, Throwable t) {
                errorMessage.postValue(error);
                uiState.postValue(UiState.NO_OUTFITS);
            }
        });
    }

    public LiveData<List<Outfit>> getOutfits() { return outfits; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<UiState> getUiState() {
        return uiState;
    }
}
