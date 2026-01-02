package it.unimib.yourwardrobe.ui.main.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.UncontainedCarouselStrategy;

import java.util.ArrayList;
import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.adapter.ClothesAdapter;
import it.unimib.yourwardrobe.ui.main.components.CardMenu;
import it.unimib.yourwardrobe.ui.main.components.CardWardrobe;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ClothesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClothesFragment extends Fragment {



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
            Navigation.findNavController(v).navigate(R.id.action_clothesFragment_to_garmentFragment);
        };
        RecyclerView recyclerViewMagliette = view.findViewById(R.id.magliette_recycler_view);
        recyclerViewMagliette.setLayoutManager(new CarouselLayoutManager(new UncontainedCarouselStrategy()));
        //dati di prova
        List<Integer> i = new ArrayList<>();
        i.add(R.drawable.cloudy);
        i.add(R.drawable.ic_email);
        i.add(R.drawable.ic_password);
        i.add(R.drawable.ic_google);
        i.add(R.drawable.ic_bot_filled);
        recyclerViewMagliette.setAdapter(new ClothesAdapter(i, listener));
        recyclerViewMagliette.setNestedScrollingEnabled(false);
        RecyclerView recyclerViewFelpe = view.findViewById(R.id.felpe_recycler_view);
        recyclerViewFelpe.setLayoutManager(new CarouselLayoutManager(new UncontainedCarouselStrategy()));
        recyclerViewFelpe.setAdapter(new ClothesAdapter(i, listener));
        recyclerViewFelpe.setNestedScrollingEnabled(false);
        RecyclerView recyclerViewPantaloni = view.findViewById(R.id.pantaloni_recycler_view);
        recyclerViewPantaloni.setLayoutManager(new CarouselLayoutManager(new UncontainedCarouselStrategy()));
        recyclerViewPantaloni.setAdapter(new ClothesAdapter(i, listener));
        recyclerViewPantaloni.setNestedScrollingEnabled(false);
        RecyclerView recyclerViewScarpe = view.findViewById(R.id.scarpe_recycler_view);
        recyclerViewScarpe.setLayoutManager(new CarouselLayoutManager(new UncontainedCarouselStrategy()));
        recyclerViewScarpe.setAdapter(new ClothesAdapter(i, listener));
        recyclerViewScarpe.setNestedScrollingEnabled(false);
        Button addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_clothesFragment_to_addGarmentFragment));

    }
}