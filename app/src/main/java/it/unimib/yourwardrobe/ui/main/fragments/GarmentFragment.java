package it.unimib.yourwardrobe.ui.main.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.unimib.yourwardrobe.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GarmentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GarmentFragment extends Fragment {

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
}