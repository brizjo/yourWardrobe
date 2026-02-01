package it.unimib.yourwardrobe.ui.main.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.core.di.ServiceLocator;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.ui.main.viewmodel.GarmentViewModel;
import it.unimib.yourwardrobe.ui.main.viewmodel.factory.GarmentViewModelFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GarmentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GarmentFragment extends Fragment {

    private GarmentViewModel viewModel;
    private ImageView garmentImageView;
    private TextView nameTextView;

    public GarmentFragment() {
        // Required empty public constructor
    }

    public static GarmentFragment newInstance() {
        GarmentFragment fragment = new GarmentFragment();
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
        return inflater.inflate(R.layout.fragment_garment, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        garmentImageView = view.findViewById(R.id.garmentImage); // Assicurati l'ID sia corretto nel layout
        nameTextView = view.findViewById(R.id.nameGarmentText);
        Button deleteButton = view.findViewById(R.id.delete_button);


        GarmentRepository repository = ServiceLocator.getInstance().getGarmentRepository();
        GarmentViewModelFactory factory = new GarmentViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(GarmentViewModel.class);

        if (getArguments() != null) {
            // GarmentFragmentArgs viene generato automaticamente dal plugin Navigation
            Garment garment = GarmentFragmentArgs.fromBundle(getArguments()).getGarment();
            if (garment != null) {
                viewModel.setGarment(garment);
            }
        }

        observeViewModel();
        if (deleteButton != null) {
            deleteButton.setOnClickListener(v -> viewModel.deleteGarment());
        }

    }



    private void observeViewModel() {
        viewModel.getGarment().observe(getViewLifecycleOwner(), garment -> {
            nameTextView.setText(garment.getName());

            // CARICAMENTO OTTIMIZZATO
            Glide.with(this)
                    .load(garment.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background) // Immagine temporanea mentre carica la prima volta
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache totale
                    .into(garmentImageView);
        });

        viewModel.getIsDeleted().observe(getViewLifecycleOwner(), deleted -> {
            if (deleted) {
                Navigation.findNavController(requireView()).navigateUp();
            }
        });
    }
}