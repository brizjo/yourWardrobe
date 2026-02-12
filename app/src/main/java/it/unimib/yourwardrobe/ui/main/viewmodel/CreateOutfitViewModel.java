package it.unimib.yourwardrobe.ui.main.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.model.Outfit;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.domain.repository.OutfitRepository;

@HiltViewModel
public class CreateOutfitViewModel extends ViewModel {

    public static final int MAX_TOPS = 3;
    public static final int MAX_BOTTOMS = 1;
    public static final int MAX_ACCESSORIES = 4;

    private final GarmentRepository garmentRepository;
    private final OutfitRepository outfitRepository;

    // --- Stagioni ---
    private final MutableLiveData<List<String>> allSeasons = new MutableLiveData<>();
    private final MutableLiveData<String> selectedSeason = new MutableLiveData<>();

    // --- Garments dal guardaroba divisi per categoria ---
    private final MutableLiveData<List<Garment>> topGarments = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Garment>> bottomGarments = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Garment>> accessoryGarments = new MutableLiveData<>(new ArrayList<>());

    // --- Selezioni dell'utente ---
    private final MutableLiveData<List<Garment>> selectedTops = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Garment>> selectedBottoms = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Garment>> selectedAccessories = new MutableLiveData<>(new ArrayList<>());

    // --- Nome outfit ---
    private final MutableLiveData<String> outfitName = new MutableLiveData<>("");

    // --- Stato UI ---
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> outfitSavedSuccessfully = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSaveEnabled = new MutableLiveData<>(false);

    @Inject
    public CreateOutfitViewModel(GarmentRepository garmentRepository,
                                 OutfitRepository outfitRepository) {
        this.garmentRepository = garmentRepository;
        this.outfitRepository = outfitRepository;
        loadSeasons();
        fetchGarments();
    }

    private void loadSeasons() {
        allSeasons.setValue(Arrays.asList("Primavera", "Estate", "Autunno", "Inverno"));
    }

    public void fetchGarments() {
        garmentRepository.getGarments(new Callback<List<Garment>>() {
            @Override
            public void onSuccess(List<Garment> garments) {
                List<Garment> tops = new ArrayList<>();
                List<Garment> bottoms = new ArrayList<>();
                List<Garment> accessories = new ArrayList<>();

                for (Garment g : garments) {
                    String cat = g.getCategory();
                    if (cat == null) continue;
                    switch (cat.toLowerCase()) {
                        case "top":
                        case "maglietta":
                        case "camicia":
                        case "maglione":
                            tops.add(g);
                            break;
                        case "bottom":
                        case "pantaloni":
                        case "gonna":
                        case "jeans":
                            bottoms.add(g);
                            break;
                        case "accessorio":
                        case "accessori":
                        case "scarpe":
                        case "borsa":
                        case "cappello":
                            accessories.add(g);
                            break;
                    }
                }
                topGarments.postValue(tops);
                bottomGarments.postValue(bottoms);
                accessoryGarments.postValue(accessories);
            }

            @Override
            public void onFailure(String error, Throwable t) {
                errorMessage.postValue(error);
            }
        });
    }

    public void toggleTopSelection(Garment garment) {
        List<Garment> current = new ArrayList<>(selectedTops.getValue());
        if (current.contains(garment)) {
            current.remove(garment);
        } else if (current.size() < MAX_TOPS) {
            current.add(garment);
        } else {
            errorMessage.setValue("Puoi selezionare al massimo " + MAX_TOPS + " capi top.");
            return;
        }
        selectedTops.setValue(current);
        updateSaveButtonState();
    }

    public void toggleBottomSelection(Garment garment) {
        List<Garment> current = new ArrayList<>(selectedBottoms.getValue());
        if (current.contains(garment)) {
            current.remove(garment);
        } else {
            current.clear();
            current.add(garment);
        }
        selectedBottoms.setValue(current);
        updateSaveButtonState();
    }

    public void toggleAccessorySelection(Garment garment) {
        List<Garment> current = new ArrayList<>(selectedAccessories.getValue());
        if (current.contains(garment)) {
            current.remove(garment);
        } else if (current.size() < MAX_ACCESSORIES) {
            current.add(garment);
        } else {
            errorMessage.setValue("Puoi selezionare al massimo " + MAX_ACCESSORIES + " accessori.");
            return;
        }
        selectedAccessories.setValue(current);
        updateSaveButtonState();
    }

    public void setOutfitName(String name) {
        outfitName.setValue(name);
        updateSaveButtonState();
    }

    public void setSelectedSeason(String season) {
        selectedSeason.setValue(season);
    }

    private void updateSaveButtonState() {
        boolean hasItems = !selectedTops.getValue().isEmpty()
                || !selectedBottoms.getValue().isEmpty();
        boolean hasName = outfitName.getValue() != null
                && !outfitName.getValue().trim().isEmpty();
        isSaveEnabled.setValue(hasItems && hasName);
    }

    public void saveOutfit() {
        if (!Boolean.TRUE.equals(isSaveEnabled.getValue())) return;

        isLoading.setValue(true);

        List<Garment> allGarments = new ArrayList<>();
        allGarments.addAll(selectedTops.getValue());
        allGarments.addAll(selectedBottoms.getValue());
        allGarments.addAll(selectedAccessories.getValue());

        Outfit outfit = new Outfit(outfitName.getValue(), selectedSeason.getValue(), allGarments);

        outfitRepository.saveOutfit(outfit, new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isLoading.postValue(false);
                outfitSavedSuccessfully.postValue(true);
            }

            @Override
            public void onFailure(String error, Throwable t) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    public LiveData<List<String>> getAllSeasons() { return allSeasons; }
    public LiveData<String> getSelectedSeason() { return selectedSeason; }
    public LiveData<List<Garment>> getTopGarments() { return topGarments; }
    public LiveData<List<Garment>> getBottomGarments() { return bottomGarments; }
    public LiveData<List<Garment>> getAccessoryGarments() { return accessoryGarments; }
    public LiveData<List<Garment>> getSelectedTops() { return selectedTops; }
    public LiveData<List<Garment>> getSelectedBottoms() { return selectedBottoms; }
    public LiveData<List<Garment>> getSelectedAccessories() { return selectedAccessories; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getOutfitSavedSuccessfully() { return outfitSavedSuccessfully; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsSaveEnabled() { return isSaveEnabled; }
}