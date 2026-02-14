package it.unimib.yourwardrobe.ui.main.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.adapter.SelectClothesAdapter;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.ui.main.viewmodel.CreateOutfitViewModel;

public class SelectClothesDialogFragment extends BottomSheetDialogFragment {

    private OnGarmentSelectedListener listener;
    private SelectClothesAdapter adapter;
    private int categoryType;
    private TextView dialogTitle;

    public interface OnGarmentSelectedListener {
        void onGarmentsConfirmed(List<Garment> selected);
    }

    public static SelectClothesDialogFragment newInstance(int categoryType, OnGarmentSelectedListener listener) {
        SelectClothesDialogFragment fragment = new SelectClothesDialogFragment();
        fragment.categoryType = categoryType;
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_garment, container, false);

        RecyclerView rv = view.findViewById(R.id.rv_select_garments);
        dialogTitle = view.findViewById(R.id.dialog_title);
        MaterialButton btnConfirm = view.findViewById(R.id.btn_confirm_selection);

        rv.setLayoutManager(new GridLayoutManager(getContext(), 3));
        CreateOutfitViewModel viewModel = new ViewModelProvider(requireParentFragment()).get(CreateOutfitViewModel.class);

        // Determina il tipo di selezione in base alla categoria
        LiveData<List<Garment>> liveData;
        boolean isMultiSelect;
        int maxSelection;
        String title;

        switch (categoryType) {
            case 1: // TOP
                liveData = viewModel.getTopGarments();
                isMultiSelect = true;
                maxSelection = CreateOutfitViewModel.MAX_TOPS; // 2
                title = "Seleziona Top (max " + maxSelection + ")";
                break;
            case 2: // BOTTOM
                liveData = viewModel.getBottomGarments();
                isMultiSelect = false;
                maxSelection = 1;
                title = "Seleziona Bottom (max " + maxSelection + ")";
                break;
            case 3: // SCARPE
                liveData = viewModel.getShoesGarments();
                isMultiSelect = false;
                maxSelection = 1;
                title = "Seleziona Scarpe (max " + maxSelection + ")";
                break;
            case 4: // ACCESSORI
                liveData = viewModel.getAccessoryGarments();
                isMultiSelect = true;
                maxSelection = CreateOutfitViewModel.MAX_ACCESSORIES; // 4
                title = "Seleziona Accessori (max " + maxSelection + ")";
                break;
            default:
                liveData = viewModel.getTopGarments();
                isMultiSelect = false;
                maxSelection = 1;
                title = "Seleziona Capi";
        }

        dialogTitle.setText(title);

        liveData.observe(getViewLifecycleOwner(), garments -> {
            if (garments != null && !garments.isEmpty()) {
                android.util.Log.d("DIALOG_DEBUG", "Ricevuti " + garments.size() + " capi per categoria " + categoryType);

                // Crea l'adapter con selezione multipla/singola
                adapter = new SelectClothesAdapter(
                        garments,
                        isMultiSelect,
                        maxSelection,
                        (garment, isSelected) -> {
                            // Callback opzionale quando viene selezionato un capo
                            android.util.Log.d("DIALOG_DEBUG", "Capo " + (isSelected ? "selezionato" : "deselezionato") + ": " + garment.getName());
                        }
                );

                // Imposta i capi già selezionati (se presenti)
                List<Garment> alreadySelected = new ArrayList<>();
                switch (categoryType) {
                    case 1:
                        alreadySelected = viewModel.getSelectedTops().getValue();
                        break;
                    case 2:
                        alreadySelected = viewModel.getSelectedBottoms().getValue();
                        break;
                    case 3:
                        alreadySelected = viewModel.getSelectedShoes().getValue();
                        break;
                    case 4:
                        alreadySelected = viewModel.getSelectedAccessories().getValue();
                        break;
                }
                if (alreadySelected != null && !alreadySelected.isEmpty()) {
                    adapter.setSelectedGarments(alreadySelected);
                }

                rv.setAdapter(adapter);
            } else {
                android.util.Log.d("DIALOG_DEBUG", "Lista vuota o null per categoria " + categoryType);
            }
        });

        btnConfirm.setOnClickListener(v -> {
            if (listener != null && adapter != null) {
                List<Garment> selected = adapter.getSelectedGarments();
                if (!selected.isEmpty()) {
                    listener.onGarmentsConfirmed(selected);
                }
            }
            dismiss();
        });

        return view;
    }
}