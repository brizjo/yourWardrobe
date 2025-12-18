package it.unimib.yourwardrobe.ui.main.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.main.components.CardComponent;
import it.unimib.yourwardrobe.ui.welcome.components.LoginButton;

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
        CardComponent outfitCard = view.findViewById(R.id.outfitCard);
        outfitCard.setCardStroke(R.color.md_theme_onPrimaryContainer, 3);
        outfitCard.setCardText("outfit");
        outfitCard.setCardTextStyle(R.style.TextAppearance_YourWardrobe_Body);
        outfitCard.setOnCardClickListener(view1 -> {
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.cloudy);
            outfitCard.setCardImage(drawable);
        });
    }
}