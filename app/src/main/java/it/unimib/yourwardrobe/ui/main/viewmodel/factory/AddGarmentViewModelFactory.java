package it.unimib.yourwardrobe.ui.main.viewmodel.factory;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.ui.main.viewmodel.AddGarmentViewModel;

public class AddGarmentViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;
    private final GarmentRepository garmentRepository;

    public AddGarmentViewModelFactory(Application application, GarmentRepository garmentRepository) {
        this.application = application;
        this.garmentRepository = garmentRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AddGarmentViewModel.class)) {
            return (T) new AddGarmentViewModel(application, garmentRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}