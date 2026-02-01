package it.unimib.yourwardrobe.ui.main.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.UncontainedCarouselStrategy;

import java.util.ArrayList;
import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.adapter.ClothesAdapter;
import it.unimib.yourwardrobe.core.di.ServiceLocator;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.ui.main.viewmodel.ClothesViewModel;
import it.unimib.yourwardrobe.ui.main.viewmodel.factory.ClothesViewModelFactory;
import it.unimib.yourwardrobe.utils.ToastHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ClothesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClothesFragment extends Fragment {


    private ClothesViewModel clothesViewModel;
    private RecyclerView recyclerViewMagliette, recyclerViewFelpe, recyclerViewPantaloni, recyclerViewScarpe;
    private ClothesAdapter.OnItemClickListener listener;


    public ClothesFragment() {
        // Required empty public constructor
    }


    public static ClothesFragment newInstance(String param1, String param2) {
        ClothesFragment fragment = new ClothesFragment();
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
        return inflater.inflate(R.layout.fragment_clothes, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ClothesAdapter.OnItemClickListener listener = (v, item) -> {

            Bundle bundle = new Bundle();
            bundle.putSerializable("garment", item);
            Navigation.findNavController(v).navigate(R.id.action_clothesFragment_to_garmentFragment, bundle);
        };

        RecyclerView recyclerViewMagliette = view.findViewById(R.id.magliette_recycler_view);
        recyclerViewMagliette.setLayoutManager(new CarouselLayoutManager(new UncontainedCarouselStrategy()));
        recyclerViewMagliette.setNestedScrollingEnabled(false);



        GarmentRepository repository = ServiceLocator.getInstance().getGarmentRepository();
        ClothesViewModelFactory factory = new ClothesViewModelFactory(repository);

        clothesViewModel = new ViewModelProvider(this, factory).get(ClothesViewModel.class);

        clothesViewModel.getAllGarments().observe(getViewLifecycleOwner(), garments ->{
            if (garments != null) {
                recyclerViewMagliette.setAdapter(new ClothesAdapter(garments, listener));

               if(garments.isEmpty())
                   //TODO; CORRETTA GESTIONE DI UN ARMADIO ANCORA VUOTO
                   Log.d("ClothesFragment", "L'utente non ha ancora caricato vestiti.");
            }

        });


//        RecyclerView recyclerViewFelpe = view.findViewById(R.id.felpe_recycler_view);
//        recyclerViewFelpe.setLayoutManager(new CarouselLayoutManager(new UncontainedCarouselStrategy()));
//        recyclerViewFelpe.setAdapter(new ClothesAdapter(listagarmentProva, listener));
//        recyclerViewFelpe.setNestedScrollingEnabled(false);
//
//        RecyclerView recyclerViewPantaloni = view.findViewById(R.id.pantaloni_recycler_view);
//        recyclerViewPantaloni.setLayoutManager(new CarouselLayoutManager(new UncontainedCarouselStrategy()));
//        recyclerViewPantaloni.setAdapter(new ClothesAdapter(listagarmentProva, listener));
//        recyclerViewPantaloni.setNestedScrollingEnabled(false);
//
//        RecyclerView recyclerViewScarpe = view.findViewById(R.id.scarpe_recycler_view);
//        recyclerViewScarpe.setLayoutManager(new CarouselLayoutManager(new UncontainedCarouselStrategy()));
//        recyclerViewScarpe.setAdapter(new ClothesAdapter(listagarmentProva, listener));
//        recyclerViewScarpe.setNestedScrollingEnabled(false);

        clothesViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error->{
            if (error != null){
                ToastHelper.show(getContext(), error, false);
            }

        });
        Button addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_clothesFragment_to_addGarmentFragment));
    }
}