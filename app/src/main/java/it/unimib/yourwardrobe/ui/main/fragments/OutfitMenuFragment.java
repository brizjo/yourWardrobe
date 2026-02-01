package it.unimib.yourwardrobe.ui.main.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.adapter.ClothesAdapter;
import it.unimib.yourwardrobe.adapter.OutfitAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OutfitMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OutfitMenuFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public OutfitMenuFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OutfitFragment.
     */
    public static OutfitMenuFragment newInstance() {
        OutfitMenuFragment fragment = new OutfitMenuFragment();
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
        return inflater.inflate(R.layout.fragment_outfit_menu, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerViewOutfit = view.findViewById(R.id.outfit_recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerViewOutfit.setLayoutManager(gridLayoutManager);
        recyclerViewOutfit.setNestedScrollingEnabled(false);
        List<Integer> i = new ArrayList<>();
        i.add(R.drawable.scattered_clouds_day);
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
        OutfitAdapter.OnItemClickListener listener = (v, item) -> {
            Navigation.findNavController(v).navigate(R.id.action_outfitFragment_to_singleOutfitFragment);
        };
        recyclerViewOutfit.setAdapter(new OutfitAdapter(i, R.layout.item_outfit_grid, listener));
        Button createOutfitButton = view.findViewById(R.id.create_outfit_button);
        createOutfitButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_outfitFragment_to_createOutfitFragment));
    }
}