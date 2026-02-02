package it.unimib.yourwardrobe.ui.main.viewmodel.factory;
import android.app.Application;

import androidx.annotation.NonNull;import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.ui.main.viewmodel.ClothesViewModel;
import it.unimib.yourwardrobe.ui.main.viewmodel.GarmentViewModel;

public class ClothesViewModelFactory implements ViewModelProvider.Factory {

    private final GarmentRepository garmentRepository;
    private final Application application;

    public ClothesViewModelFactory( Application application, GarmentRepository garmentRepository) {
        this.application = application;
        this.garmentRepository = garmentRepository;

    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ClothesViewModel.class)) {
            return (T) new ClothesViewModel(application, garmentRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }


}
