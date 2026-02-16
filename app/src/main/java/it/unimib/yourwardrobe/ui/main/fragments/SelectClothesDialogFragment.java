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
    private boolean isReplacement;  // ← NUOVO
    private TextView dialogTitle;

    public interface OnGarmentSelectedListener {
        void onGarmentsConfirmed(List<Garment> selected);
    }

    public static SelectClothesDialogFragment newInstance(int categoryType, boolean isReplacement, OnGarmentSelectedListener listener) {
        SelectClothesDialogFragment fragment = new SelectClothesDialogFragment();
        fragment.categoryType = categoryType;
        fragment.isReplacement = isReplacement;  // ← NUOVO
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

        LiveData<List<Garment>> liveData;
        boolean isMultiSelect;
        int maxSelection;
        String title;

        switch (categoryType) {
            case 1:
                liveData = viewModel.getTopGarments();
                isMultiSelect = !isReplacement;           // ← se sostituzione, sempre singolo
                maxSelection = isReplacement ? 1 : CreateOutfitViewModel.MAX_TOPS;
                title = isReplacement ? "Sostituisci Top" : "Seleziona Top (max " + maxSelection + ")";
                break;
            case 2:
                liveData = viewModel.getBottomGarments();
                isMultiSelect = false;
                maxSelection = 1;
                title = isReplacement ? "Sostituisci Bottom" : "Seleziona Bottom (max " + maxSelection + ")";
                break;
            case 3:
                liveData = viewModel.getShoesGarments();
                isMultiSelect = false;
                maxSelection = 1;
                title = isReplacement ? "Sostituisci Scarpe" : "Seleziona Scarpe (max " + maxSelection + ")";
                break;
            case 4:
                liveData = viewModel.getAccessoryGarments();
                isMultiSelect = !isReplacement;           // ← se sostituzione, sempre singolo
                maxSelection = isReplacement ? 1 : CreateOutfitViewModel.MAX_ACCESSORIES;
                title = isReplacement ? "Sostituisci Accessorio" : "Seleziona Accessori (max " + maxSelection + ")";
                break;
            default:
                liveData = viewModel.getTopGarments();
                isMultiSelect = false;
                maxSelection = 1;
                title = "Seleziona Capi";
        }

        dialogTitle.setText(title);

        // Il resto rimane identico...
        final boolean finalIsMultiSelect = isMultiSelect;
        final int finalMaxSelection = maxSelection;

        liveData.observe(getViewLifecycleOwner(), garments -> {
            if (garments != null && !garments.isEmpty()) {
                adapter = new SelectClothesAdapter(
                        garments,
                        finalIsMultiSelect,
                        finalMaxSelection,
                        (garment, isSelected) -> {}
                );

                List<Garment> alreadySelected = new ArrayList<>();
                switch (categoryType) {
                    case 1: alreadySelected = viewModel.getSelectedTops().getValue(); break;
                    case 2: alreadySelected = viewModel.getSelectedBottoms().getValue(); break;
                    case 3: alreadySelected = viewModel.getSelectedShoes().getValue(); break;
                    case 4: alreadySelected = viewModel.getSelectedAccessories().getValue(); break;
                }
                if (alreadySelected != null && !alreadySelected.isEmpty()) {
                    adapter.setSelectedGarments(alreadySelected);
                }

                rv.setAdapter(adapter);
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