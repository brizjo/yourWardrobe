package it.unimib.yourwardrobe.ui.main.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;

public class ClothesViewModel extends ViewModel {

    private final GarmentRepository garmentRepository;

    // LiveData per la lista completa e lo stato
    private final MutableLiveData<List<Garment>> allGarments = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public ClothesViewModel(GarmentRepository garmentRepository) {
        this.garmentRepository = garmentRepository;
        fetchGarments(); // Avvia il recupero dati alla creazione
    }

    /**
     * Recupera i vestiti dal repository (che usa un real-time listener su Firestore)
     */
    public void fetchGarments() {
        isLoading.setValue(true);
        garmentRepository.getGarments(new Callback<List<Garment>>() {
            @Override
            public void onSuccess(List<Garment> data) {
                allGarments.postValue(data);
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(String error, Throwable t) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });
    }



    public LiveData<List<Garment>> getAllGarments() {
        return allGarments;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }



    /**
     * Helper per filtrare i vestiti per categoria specifica.
     * Utile per popolare le diverse RecyclerView del ClothesFragment.
     */
//    public List<Garment> filterByCategory(List<Garment> fullList, String category) {
//        List<Garment> filtered = new ArrayList<>();
//        if (fullList == null) return filtered;
//
//        for (Garment g : fullList) {
//            if (category.equalsIgnoreCase(g.getCategory())) {
//                filtered.add(g);
//            }
//        }
//        return filtered;
//    }

}