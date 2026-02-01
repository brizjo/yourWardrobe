package it.unimib.yourwardrobe.ui.main.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;

public class GarmentViewModel extends ViewModel {

    private final GarmentRepository garmentRepository;
    private final MutableLiveData<Garment> garment = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isDeleted = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public GarmentViewModel(GarmentRepository garmentRepository) {
        this.garmentRepository = garmentRepository;
    }

    public LiveData<Garment> getGarment(){return garment;}
    public LiveData<Boolean> getIsDeleted(){return isDeleted;}

    public void setGarment(Garment garment){
        this.garment.postValue(garment);
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



}
