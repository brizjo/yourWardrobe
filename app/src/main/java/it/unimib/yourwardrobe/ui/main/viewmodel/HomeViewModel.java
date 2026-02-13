package it.unimib.yourwardrobe.ui.main.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.core.functional.Result;
import it.unimib.yourwardrobe.domain.model.User;
import it.unimib.yourwardrobe.domain.model.WeatherInfo;
import it.unimib.yourwardrobe.domain.repository.AuthRepository;
import it.unimib.yourwardrobe.domain.repository.WeatherRepository;

@HiltViewModel
public class HomeViewModel extends ViewModel {
    private static final String TAG = HomeViewModel.class.getSimpleName();
    private final WeatherRepository weatherRepository;
    private final AuthRepository authRepository;
    private final MutableLiveData<Result<WeatherInfo>> _currentWeatherResult = new MutableLiveData<>();
    public final LiveData<Result<WeatherInfo>> currentWeatherResult = _currentWeatherResult;

    private final MutableLiveData<Result<User>> _currentUser = new MutableLiveData<>();
    public final LiveData<Result<User>> currentUser = _currentUser;


    @Inject
    public HomeViewModel(WeatherRepository weatherRepository, AuthRepository authRepository) {
        this.weatherRepository = weatherRepository;
        this.authRepository = authRepository;
    }

    public void getCurrentUser() {
        _currentUser.setValue(Result.loading(null));
        var user = this.authRepository.getCurrentUser();
        if (user != null) {
            _currentUser.setValue(Result.success(user));
        }
    }

    public void getCurrentWeather(double lat, double lon) {
        _currentWeatherResult.setValue(Result.loading(null));
        this.weatherRepository
                .getCurrentWeather(
                        lat,
                        lon,
                        new Callback<>() {
                            @Override
                            public void onSuccess(WeatherInfo data) {
                                _currentWeatherResult.setValue(Result.success(data));
                            }

                            @Override
                            public void onFailure(String errorMessage, Throwable t) {
                                Log.e(TAG, errorMessage, t);
                                _currentWeatherResult.setValue(Result.error(errorMessage, null));
                            }
                        });
    }

}
