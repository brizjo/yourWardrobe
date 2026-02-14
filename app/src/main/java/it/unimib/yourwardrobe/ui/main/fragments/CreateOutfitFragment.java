package it.unimib.yourwardrobe.ui.main.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.ui.main.viewmodel.CreateOutfitViewModel;
import it.unimib.yourwardrobe.utils.ToastHelper;

@AndroidEntryPoint
public class CreateOutfitFragment extends Fragment {

    private CreateOutfitViewModel viewModel;
    private AutoCompleteTextView seasonTextView;
    private TextInputEditText nameEditText;
    private Button btnSave;

    // Slot Views
    private View btnAddTop, btnAddBottom, btnAddShoes, btnAddAccessory;
    private ImageView imgTop, imgBottom, imgShoes, imgAccessory;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_outfit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CreateOutfitViewModel.class);

        initViews(view);
        setupListeners();
        observeViewModel();
    }

    private void initViews(View view) {
        seasonTextView = view.findViewById(R.id.season_text_view);
        nameEditText = view.findViewById(R.id.outfit_name_edit_text);
        btnSave = view.findViewById(R.id.btn_save_outfit);

        // Inizializzazione Slot
        btnAddTop = view.findViewById(R.id.btn_add_top);
        imgTop = view.findViewById(R.id.img_top_slot);

        btnAddBottom = view.findViewById(R.id.btn_add_bottom);
        imgBottom = view.findViewById(R.id.img_bottom_slot);

        btnAddShoes = view.findViewById(R.id.btn_add_shoes);
        imgShoes = view.findViewById(R.id.img_shoes_slot);

        btnAddAccessory = view.findViewById(R.id.btn_add_accessory);
        imgAccessory = view.findViewById(R.id.img_accessory_slot);
    }

    private void setupListeners() {
        // Listener per il nome
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { viewModel.setOutfitName(s.toString()); }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Listener per la stagione
        seasonTextView.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            viewModel.setSelectedSeason(selected);
        });

        // Click sui tasti "+" per aprire il Dialog di selezione
        btnAddTop.setOnClickListener(v -> openSelectionDialog( 1));
        btnAddBottom.setOnClickListener(v -> openSelectionDialog(2));
        btnAddShoes.setOnClickListener(v -> openSelectionDialog(3));
        btnAddAccessory.setOnClickListener(v -> openSelectionDialog( 4));

        btnSave.setOnClickListener(v -> viewModel.saveOutfit());
    }

    private void openSelectionDialog(int type) {
        // Passiamo solo il numero (1, 2, 3 o 4)
        SelectClothesDialogFragment.newInstance(type, selected -> {
            if (!selected.isEmpty()) {
                Garment g = selected.get(0);
                if (type == 1) viewModel.toggleTopSelection(g);
                else if (type == 2) viewModel.toggleBottomSelection(g);
                else if (type == 3) viewModel.toggleShoesSelection(g);
                else if (type == 4) viewModel.toggleAccessorySelection(g);
            }
        }).show(getChildFragmentManager(), "select_garment");
    }

    private void observeViewModel() {
        // Stagioni
        viewModel.getAllSeasons().observe(getViewLifecycleOwner(), seasons -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, seasons);
            seasonTextView.setAdapter(adapter);
        });

        // Abilitazione bottone Salva
        viewModel.getIsSaveEnabled().observe(getViewLifecycleOwner(), btnSave::setEnabled);

        // Osservatori per gli Slot (Aggiornano l'immagine o mostrano il "+")
        viewModel.getSelectedTops().observe(getViewLifecycleOwner(), list -> updateSlotUI(list, imgTop, btnAddTop));
        viewModel.getSelectedBottoms().observe(getViewLifecycleOwner(), list -> updateSlotUI(list, imgBottom, btnAddBottom));
        viewModel.getSelectedShoes().observe(getViewLifecycleOwner(), list -> updateSlotUI(list, imgShoes, btnAddShoes));
        viewModel.getSelectedAccessories().observe(getViewLifecycleOwner(), list -> updateSlotUI(list, imgAccessory, btnAddAccessory));

        // Successo Salvataggio
        viewModel.getOutfitSavedSuccessfully().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                ToastHelper.show(getContext(), "Outfit salvato!", false);
                Navigation.findNavController(requireView()).navigateUp();
            }
        });
    }

    private void updateSlotUI(List<Garment> list, ImageView img, View btn) {
        if (list != null && !list.isEmpty()) {
            img.setVisibility(View.VISIBLE);
            btn.setVisibility(View.GONE);
            Glide.with(this).load(list.get(0).getImageUrl()).into(img);

            // Permetti di cambiare cliccando sull'immagine
            img.setOnClickListener(v -> btn.performClick());
        } else {
            img.setVisibility(View.GONE);
            btn.setVisibility(View.VISIBLE);
        }
    }
}