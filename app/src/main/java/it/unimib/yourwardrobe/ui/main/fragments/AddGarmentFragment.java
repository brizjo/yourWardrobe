package it.unimib.yourwardrobe.ui.main.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.main.components.CardMenu;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddGarmentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddGarmentFragment extends Fragment {


    public AddGarmentFragment() {
        // Required empty public constructor
    }


    public static AddGarmentFragment newInstance(String param1, String param2) {
        AddGarmentFragment fragment = new AddGarmentFragment();
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
        return inflater.inflate(R.layout.fragment_add_garment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView imageView = view.findViewById(R.id.addGarmentImage);
        imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_add));

    }
}