package it.unimib.yourwardrobe.ui.main.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.adapter.ClothesAdapter;
import it.unimib.yourwardrobe.adapter.SelectClothesAdapter;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.ui.main.viewmodel.CreateOutfitViewModel;

public class SelectClothesDialogFragment extends BottomSheetDialogFragment {


    private OnGarmentSelectedListener listener;
    private SelectClothesAdapter adapter;

    private int categoryType;

    public interface OnGarmentSelectedListener {
        void onGarmentsConfirmed(List<Garment> selected);
    }

    public static SelectClothesDialogFragment newInstance(int categoryType, OnGarmentSelectedListener listener) {
        SelectClothesDialogFragment fragment = new SelectClothesDialogFragment();
        fragment.categoryType = categoryType;
        fragment.listener = listener;
        return fragment;
    }

    // All'interno di SelectClothesDialogFragment.java, metodo onCreateView
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_garment, container, false);

        RecyclerView rv = view.findViewById(R.id.rv_select_garments);

        rv.setLayoutManager(new GridLayoutManager(getContext(), 3));
        CreateOutfitViewModel viewModel = new ViewModelProvider(requireParentFragment()).get(CreateOutfitViewModel.class);

        LiveData<List<Garment>> liveData;
        if (categoryType == 1) liveData = viewModel.getTopGarments();
        else if (categoryType == 2) liveData = viewModel.getBottomGarments();
        else if (categoryType == 3) liveData = viewModel.getShoesGarments();
        else liveData = viewModel.getAccessoryGarments();

        List<Garment> temporarySelection = new ArrayList<>();

        liveData.observe(getViewLifecycleOwner(), garments -> {
            if (garments != null && !garments.isEmpty()) {
                android.util.Log.d("DIALOG_DEBUG", "Ricevuti " + garments.size() + " capi per categoria " + categoryType);

                // Creiamo l'adapter
                 adapter = new SelectClothesAdapter(garments, garment -> {
                    temporarySelection.clear();
                    temporarySelection.add(garment);
                });

                rv.setAdapter(adapter);
                // Forza il refresh immediato della UI
                adapter.notifyDataSetChanged();
            } else {
                android.util.Log.d("DIALOG_DEBUG", "Lista vuota o null per categoria " + categoryType);
            }
        });
        // Usiamo il ClothesAdapter che abbiamo già creato


        // Notifica l'adapter se i dati sono arrivati dopo

        view.findViewById(R.id.btn_confirm_selection).setOnClickListener(v -> {
            if (listener != null && !temporarySelection.isEmpty()) {
                listener.onGarmentsConfirmed(temporarySelection);
            }
            dismiss();
        });

        return view;
    }
}