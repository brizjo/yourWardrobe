package it.unimib.yourwardrobe.ui.main.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.model.Outfit;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.domain.repository.OutfitRepository;

@HiltViewModel
public class CreateOutfitViewModel extends ViewModel {

    public static final int MAX_TOPS = 2;
    public static final int MAX_ACCESSORIES = 4;
    private static final int SAVE_TIMEOUT_MS = 7000;

    private final GarmentRepository garmentRepository;
    private final OutfitRepository outfitRepository;

    private final MutableLiveData<List<String>> allSeasons = new MutableLiveData<>();
    private final MutableLiveData<String> selectedSeason = new MutableLiveData<>();
    private final MutableLiveData<List<Garment>> topGarments = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Garment>> bottomGarments = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Garment>> shoesGarments = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Garment>> accessoryGarments = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Garment>> selectedTops = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Garment>> selectedBottoms = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Garment>> selectedShoes = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Garment>> selectedAccessories = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> outfitName = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> outfitSavedSuccessfully = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSaveEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> offlineSaveScheduled = new MutableLiveData<>();

    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable;

    @Inject
    public CreateOutfitViewModel(GarmentRepository garmentRepository, OutfitRepository outfitRepository) {
        this.garmentRepository = garmentRepository;
        this.outfitRepository = outfitRepository;
        loadSeasons();
        fetchGarments();
    }

    private void loadSeasons() {
        allSeasons.setValue(Arrays.asList("Primavera", "Estate", "Autunno", "Inverno"));
    }

    public void fetchGarments() {
        isLoading.setValue(true);
        garmentRepository.getGarmentsByCategory("Parte superiore", new Callback<List<Garment>>() {
            @Override public void onSuccess(List<Garment> result) { topGarments.postValue(result); }
            @Override public void onFailure(String error, Throwable t) { errorMessage.postValue(error); }
        });
        garmentRepository.getGarmentsByCategory("Parte inferiore", new Callback<List<Garment>>() {
            @Override public void onSuccess(List<Garment> result) { bottomGarments.postValue(result); }
            @Override public void onFailure(String error, Throwable t) { errorMessage.postValue(error); }
        });
        garmentRepository.getGarmentsByCategory("Calzature", new Callback<List<Garment>>() {
            @Override public void onSuccess(List<Garment> result) { shoesGarments.postValue(result); }
            @Override public void onFailure(String error, Throwable t) { errorMessage.postValue(error); }
        });
        garmentRepository.getGarmentsByCategory("Accessorio", new Callback<List<Garment>>() {
            @Override public void onSuccess(List<Garment> result) { accessoryGarments.postValue(result); isLoading.postValue(false); }
            @Override public void onFailure(String error, Throwable t) { errorMessage.postValue(error); isLoading.postValue(false); }
        });
    }

    public void toggleTopSelection(Garment garment) {
        List<Garment> current = new ArrayList<>(selectedTops.getValue());
        if (current.contains(garment)) current.remove(garment);
        else if (current.size() < MAX_TOPS) current.add(garment);
        else { errorMessage.setValue("Puoi selezionare al massimo " + MAX_TOPS + " capi top."); return; }
        selectedTops.setValue(current);
        updateSaveButtonState();
    }

    public void toggleBottomSelection(Garment garment) {
        List<Garment> current = new ArrayList<>(selectedBottoms.getValue());
        if (current.contains(garment)) current.remove(garment);
        else { current.clear(); current.add(garment); }
        selectedBottoms.setValue(current);
        updateSaveButtonState();
    }

    public void toggleShoesSelection(Garment garment) {
        List<Garment> current = new ArrayList<>(selectedShoes.getValue());
        if (current.contains(garment)) current.remove(garment);
        else if (current.isEmpty()) current.add(garment);
        else { errorMessage.setValue("Puoi selezionare solo un paio di scarpe."); return; }
        selectedShoes.setValue(current);
        updateSaveButtonState();
    }

    public void toggleAccessorySelection(Garment garment) {
        List<Garment> current = new ArrayList<>(selectedAccessories.getValue());
        if (current.contains(garment)) current.remove(garment);
        else if (current.size() < MAX_ACCESSORIES) current.add(garment);
        else { errorMessage.setValue("Puoi selezionare al massimo " + MAX_ACCESSORIES + " accessori."); return; }
        selectedAccessories.setValue(current);
        updateSaveButtonState();
    }

    public void removeTop() { selectedTops.setValue(null); updateSaveButtonState(); }
    public void removeBottom() { selectedBottoms.setValue(null); updateSaveButtonState(); }
    public void removeShoes() { selectedShoes.setValue(null); updateSaveButtonState(); }
    public void removeAccessory() { selectedAccessories.setValue(null); updateSaveButtonState(); }

    public void setOutfitName(String name) { outfitName.setValue(name); updateSaveButtonState(); }
    public void setSelectedSeason(String season) { selectedSeason.setValue(season); updateSaveButtonState(); }

    private void updateSaveButtonState() {
        List<Garment> tops = selectedTops.getValue();
        List<Garment> bottoms = selectedBottoms.getValue();
        boolean hasItems = (tops != null && !tops.isEmpty()) && (bottoms != null && !bottoms.isEmpty());
        boolean hasName = outfitName.getValue() != null && !outfitName.getValue().trim().isEmpty();
        boolean hasSeason = selectedSeason.getValue() != null && !selectedSeason.getValue().trim().isEmpty();
        isSaveEnabled.setValue(hasItems && hasName && hasSeason);
    }

    public void saveOutfit() {
        if (!Boolean.TRUE.equals(isSaveEnabled.getValue())) return;

        isLoading.setValue(true);

        // Avvia timeout di 7 secondi
        timeoutRunnable = () -> {
            if (Boolean.TRUE.equals(isLoading.getValue())) {
                isLoading.postValue(false);
                offlineSaveScheduled.postValue(true);
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, SAVE_TIMEOUT_MS);

        List<Garment> allGarments = new ArrayList<>();
        if (selectedTops.getValue() != null) allGarments.addAll(selectedTops.getValue());
        if (selectedBottoms.getValue() != null) allGarments.addAll(selectedBottoms.getValue());
        if (selectedShoes.getValue() != null) allGarments.addAll(selectedShoes.getValue());
        if (selectedAccessories.getValue() != null) allGarments.addAll(selectedAccessories.getValue());

        Outfit outfit = new Outfit(outfitName.getValue(), selectedSeason.getValue(), allGarments);

        outfitRepository.saveOutfit(outfit, new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                cancelTimeout();
                isLoading.postValue(false);
                outfitSavedSuccessfully.postValue(true);
            }
            @Override
            public void onFailure(String error, Throwable t) {
                cancelTimeout();
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });
    }

    private void cancelTimeout() {
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cancelTimeout();
    }

    public LiveData<Boolean> getOfflineSaveScheduled() { return offlineSaveScheduled; }
    public LiveData<List<String>> getAllSeasons() { return allSeasons; }
    public LiveData<String> getSelectedSeason() { return selectedSeason; }
    public LiveData<List<Garment>> getTopGarments() { return topGarments; }
    public LiveData<List<Garment>> getBottomGarments() { return bottomGarments; }
    public LiveData<List<Garment>> getShoesGarments() { return shoesGarments; }
    public LiveData<List<Garment>> getAccessoryGarments() { return accessoryGarments; }
    public LiveData<List<Garment>> getSelectedTops() { return selectedTops; }
    public LiveData<List<Garment>> getSelectedBottoms() { return selectedBottoms; }
    public LiveData<List<Garment>> getSelectedShoes() { return selectedShoes; }
    public LiveData<List<Garment>> getSelectedAccessories() { return selectedAccessories; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getOutfitSavedSuccessfully() { return outfitSavedSuccessfully; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsSaveEnabled() { return isSaveEnabled; }
}