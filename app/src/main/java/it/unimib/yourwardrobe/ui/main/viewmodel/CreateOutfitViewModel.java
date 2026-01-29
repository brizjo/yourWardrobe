package it.unimib.yourwardrobe.ui.main.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.unimib.yourwardrobe.R;

public class CreateOutfitViewModel extends AndroidViewModel {
    private final MutableLiveData<List<String>> allSeasons = new MutableLiveData<>();

    private final MutableLiveData<String> selectedSeason = new MutableLiveData<>();

    public CreateOutfitViewModel(@NonNull Application application) {
        super(application);
        loadInitialData();
    }

    private void loadInitialData() {
        allSeasons.setValue(Arrays.asList(getApplication().getResources().getStringArray(R.array.seasons)));
    }

    public void setSelectedSeason(String season) {
        selectedSeason.setValue(season);
    }

    public LiveData<List<String>> getAllSeasons() {
        return allSeasons;
    }
}
