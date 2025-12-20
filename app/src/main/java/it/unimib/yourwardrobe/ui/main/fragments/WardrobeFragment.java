package it.unimib.yourwardrobe.ui.main.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.main.components.CardMenu;

public class WardrobeFragment extends Fragment {

    public static WardrobeFragment newInstance(String param1, String param2) {
        WardrobeFragment fragment = new WardrobeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wardrobe, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CardMenu outfitCard = view.findViewById(R.id.outfitCard);
        outfitCard.setCardStroke(R.color.md_theme_onPrimaryContainer, 3);
        outfitCard.setCardText("outfit");
        outfitCard.setCardTextStyle(R.style.TextAppearance_YourWardrobe_Body);
        outfitCard.setOnCardClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_wardrobeFragment_to_clothesFragment);
            }
        });
    }
}