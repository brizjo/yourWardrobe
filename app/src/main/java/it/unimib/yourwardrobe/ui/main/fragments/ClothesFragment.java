package it.unimib.yourwardrobe.ui.main.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import it.unimib.yourwardrobe.R;
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
        CardWardrobe clothesCard = view.findViewById(R.id.clothesCard);
        clothesCard.setCardImage(ContextCompat.getDrawable(getContext(), R.drawable.cloudy));
        Button button = view.findViewById(R.id.back_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_clothesFragment_to_wardrobeFragment);
            }
        });
    }
}