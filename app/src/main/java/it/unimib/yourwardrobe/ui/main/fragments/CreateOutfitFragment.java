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
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
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

    // Containers e bottoni
    private LinearLayout containerTops, containerAccessories;
    private View btnAddTop, btnAddBottom, btnAddShoes, btnAddAccessory;
    private ImageView imgBottom, imgShoes;

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

        // Containers per selezione multipla
        containerTops = view.findViewById(R.id.container_tops);
        containerAccessories = view.findViewById(R.id.container_accessories);

        // Bottoni +
        btnAddTop = view.findViewById(R.id.btn_add_top);
        btnAddBottom = view.findViewById(R.id.btn_add_bottom);
        btnAddShoes = view.findViewById(R.id.btn_add_shoes);
        btnAddAccessory = view.findViewById(R.id.btn_add_accessory);

        // Slot singoli
        imgBottom = view.findViewById(R.id.img_bottom_slot);
        imgShoes = view.findViewById(R.id.img_shoes_slot);
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

        // Click sui tasti "+"
        btnAddTop.setOnClickListener(v -> openSelectionDialog(1));
        btnAddBottom.setOnClickListener(v -> openSelectionDialog(2));
        btnAddShoes.setOnClickListener(v -> openSelectionDialog(3));
        btnAddAccessory.setOnClickListener(v -> openSelectionDialog(4));

        btnSave.setOnClickListener(v -> viewModel.saveOutfit());
    }

    private void openSelectionDialog(int type) {
        SelectClothesDialogFragment.newInstance(type, selected -> {
            if (!selected.isEmpty()) {
                // Pulisci le selezioni precedenti
                if (type == 1) {
                    List<Garment> currentTops = viewModel.getSelectedTops().getValue();
                    if (currentTops != null) {
                        for (Garment g : currentTops) {
                            viewModel.toggleTopSelection(g); // Rimuovi tutti
                        }
                    }
                } else if (type == 4) {
                    List<Garment> currentAccessories = viewModel.getSelectedAccessories().getValue();
                    if (currentAccessories != null) {
                        for (Garment g : currentAccessories) {
                            viewModel.toggleAccessorySelection(g); // Rimuovi tutti
                        }
                    }
                }

                // Aggiungi le nuove selezioni
                for (Garment garment : selected) {
                    if (type == 1) viewModel.toggleTopSelection(garment);
                    else if (type == 2) viewModel.toggleBottomSelection(garment);
                    else if (type == 3) viewModel.toggleShoesSelection(garment);
                    else if (type == 4) viewModel.toggleAccessorySelection(garment);
                }
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

        // Osservatori per gli Slot MULTIPLI
        viewModel.getSelectedTops().observe(getViewLifecycleOwner(), this::updateMultiSlotUI_Tops);
        viewModel.getSelectedAccessories().observe(getViewLifecycleOwner(), this::updateMultiSlotUI_Accessories);

        // Osservatori per gli Slot SINGOLI
        viewModel.getSelectedBottoms().observe(getViewLifecycleOwner(), list -> updateSingleSlotUI(list, imgBottom, btnAddBottom));
        viewModel.getSelectedShoes().observe(getViewLifecycleOwner(), list -> updateSingleSlotUI(list, imgShoes, btnAddShoes));

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                android.util.Log.e("CreateOutfitFragment", "ERRORE: " + error);
                ToastHelper.show(getContext(), "Errore: " + error, false);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            android.util.Log.d("CreateOutfitFragment", "Loading: " + isLoading);
        });
        // Successo Salvataggio
        viewModel.getOutfitSavedSuccessfully().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                ToastHelper.show(getContext(), "Outfit salvato!", false);
                Navigation.findNavController(requireView()).navigateUp();
            }
        });
    }

    // ========== AGGIORNAMENTO UI PER SELEZIONE MULTIPLA (TOPS) ==========
    private void updateMultiSlotUI_Tops(List<Garment> garments) {
        // Rimuovi tutte le immagini precedenti (tranne il bottone +)
        containerTops.removeAllViews();

        if (garments != null && !garments.isEmpty()) {
            // Aggiungi le immagini dei capi selezionati
            for (Garment garment : garments) {
                ShapeableImageView imageView = createGarmentImageView(garment);
                imageView.setOnClickListener(v -> openSelectionDialog(1)); // Permetti di modificare
                containerTops.addView(imageView);
            }
        }

        // Aggiungi sempre il bottone + alla fine
        containerTops.addView(btnAddTop);
    }

    // ========== AGGIORNAMENTO UI PER SELEZIONE MULTIPLA (ACCESSORI) ==========
    private void updateMultiSlotUI_Accessories(List<Garment> garments) {
        // Rimuovi tutte le immagini precedenti (tranne il bottone +)
        containerAccessories.removeAllViews();

        if (garments != null && !garments.isEmpty()) {
            // Aggiungi le immagini dei capi selezionati
            for (Garment garment : garments) {
                ShapeableImageView imageView = createGarmentImageView(garment);
                imageView.setOnClickListener(v -> openSelectionDialog(4)); // Permetti di modificare
                containerAccessories.addView(imageView);
            }
        }

        // Aggiungi sempre il bottone + alla fine
        containerAccessories.addView(btnAddAccessory);
    }

    // ========== CREA IMAGEVIEW PER UN CAPO ==========
    private ShapeableImageView createGarmentImageView(Garment garment) {
        ShapeableImageView imageView = new ShapeableImageView(requireContext());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int) (100 * getResources().getDisplayMetrics().density), // 100dp
                (int) (100 * getResources().getDisplayMetrics().density)  // 100dp
        );
        params.setMarginEnd((int) (8 * getResources().getDisplayMetrics().density)); // 8dp margin
        imageView.setLayoutParams(params);

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setShapeAppearanceModel(
                imageView.getShapeAppearanceModel()
                        .toBuilder()
                        .setAllCornerSizes(12 * getResources().getDisplayMetrics().density)
                        .build()
        );

        Glide.with(this)
                .load(garment.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(imageView);

        return imageView;
    }

    // ========== AGGIORNAMENTO UI PER SELEZIONE SINGOLA ==========
    private void updateSingleSlotUI(List<Garment> list, ImageView img, View btn) {
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