package it.unimib.yourwardrobe.ui.main.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.UncontainedCarouselStrategy;

import java.util.ArrayList;
import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.adapter.ClothesAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OutfitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OutfitFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public OutfitFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OutfitFragment.
     */
    public static OutfitFragment newInstance() {
        OutfitFragment fragment = new OutfitFragment();
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
        return inflater.inflate(R.layout.fragment_outfit, container, false);
    }
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerViewOutfit = view.findViewById(R.id.outfit_recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerViewOutfit.setLayoutManager(gridLayoutManager);
        recyclerViewOutfit.setNestedScrollingEnabled(false);
        List<Integer> i = new ArrayList<>();
        i.add(R.drawable.cloudy);
        i.add(R.drawable.ic_email);
        i.add(R.drawable.ic_password);
        i.add(R.drawable.ic_google);
        i.add(R.drawable.ic_bot_filled);
        i.add(R.drawable.ic_email);
        i.add(R.drawable.ic_password);
        i.add(R.drawable.ic_google);
        i.add(R.drawable.ic_bot_filled);
        i.add(R.drawable.ic_email);
        i.add(R.drawable.ic_password);
        i.add(R.drawable.ic_google);
        i.add(R.drawable.ic_bot_filled);
        recyclerViewOutfit.setAdapter(new ClothesAdapter(i, R.layout.item_outfit_grid));

    }
}