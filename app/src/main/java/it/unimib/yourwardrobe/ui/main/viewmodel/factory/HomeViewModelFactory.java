package it.unimib.yourwardrobe.ui.main.viewmodel.factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import it.unimib.yourwardrobe.domain.repository.WeatherRepository;
import it.unimib.yourwardrobe.repository.UserRepository;
import it.unimib.yourwardrobe.ui.main.viewmodel.HomeViewModel;

public class HomeViewModelFactory implements ViewModelProvider.Factory {
    private final WeatherRepository weatherRepository;
    private final UserRepository userRepository;

    public HomeViewModelFactory(WeatherRepository weatherRepository, UserRepository userRepository) {
        this.weatherRepository = weatherRepository;
        this.userRepository = userRepository;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(weatherRepository, userRepository);
        }

        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
