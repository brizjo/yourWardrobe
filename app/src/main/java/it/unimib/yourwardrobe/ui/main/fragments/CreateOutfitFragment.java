package it.unimib.yourwardrobe.ui.main.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.main.viewmodel.CreateOutfitViewModel;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateOutfitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
public class CreateOutfitFragment extends Fragment {

    private CreateOutfitViewModel viewModel;
    private AutoCompleteTextView seasonTextView;

    public CreateOutfitFragment() {
        // Required empty public constructor
    }

    public static CreateOutfitFragment newInstance(String param1, String param2) {
        CreateOutfitFragment fragment = new CreateOutfitFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_outfit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CreateOutfitViewModel.class);
        seasonTextView = view.findViewById(R.id.season_text_view);
        viewModel.getAllSeasons().observe(getViewLifecycleOwner(), seasons -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, seasons);
            seasonTextView.setAdapter(adapter);
        });
    }
}