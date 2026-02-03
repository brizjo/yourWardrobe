package it.unimib.yourwardrobe.ui.main.viewmodel.factory;

import androidx.annotation.NonNull;import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.ui.main.viewmodel.GarmentViewModel;
import android.app.Application;

public class GarmentViewModelFactory implements ViewModelProvider.Factory {

    private final GarmentRepository garmentRepository;
    private final Application application;

    public GarmentViewModelFactory(Application application, GarmentRepository garmentRepository) {
        this.garmentRepository = garmentRepository;
        this.application = application;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(GarmentViewModel.class)) {
            return (T) new GarmentViewModel(application, garmentRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}