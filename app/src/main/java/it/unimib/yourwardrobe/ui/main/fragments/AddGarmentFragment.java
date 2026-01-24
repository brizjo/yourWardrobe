package it.unimib.yourwardrobe.ui.main.fragments;

import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.main.components.CardMenu;
import it.unimib.yourwardrobe.ui.main.viewmodel.AddGarmentViewModel;
/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddGarmentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddGarmentFragment extends Fragment {

    private ImageView addGarmentImageView;
    private AutoCompleteTextView autoCompleteTextView;
    private ChipGroup colorChipGroup;
    private AddGarmentViewModel viewModel;
    private TextInputEditText garmentNameEditText;
    private Button addGarmentButton;


    // 1. Launcher per la richiesta dei permessi
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permesso concesso, lancia la fotocamera
                    launchCamera();
                } else {
                    // Permesso negato, informa l'utente
                    Toast.makeText(getContext(), "Permesso fotocamera necessario per scattare una foto", Toast.LENGTH_SHORT).show();
                }
            });

    // 2. Launcher per ricevere il risultato dalla fotocamera
    private final ActivityResultLauncher<Void> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    // Foto scattata, mostrala nell'ImageView
                    addGarmentImageView.setImageBitmap(bitmap);
                    // Cambia lo scaleType per riempire l'area
                    addGarmentImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    viewModel.setGarmentImage(bitmap);
                }
            });

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
        viewModel = new ViewModelProvider(this).get(AddGarmentViewModel.class);
        addGarmentImageView = view.findViewById(R.id.addGarmentImage);
        autoCompleteTextView = view.findViewById(R.id.auto_complete_text_view);
        colorChipGroup = view.findViewById(R.id.chip_group_color);
        garmentNameEditText = view.findViewById(R.id.garmentName);
        addGarmentButton = view.findViewById(R.id.add_garment_button);

        addGarmentImageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_add_photo));

        viewModel.getAllSeasons().observe(getViewLifecycleOwner(), seasons -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, seasons);
            autoCompleteTextView.setAdapter(adapter);
        });

        viewModel.getSelectedColors().observe(getViewLifecycleOwner(), colors -> {
            updateMainChipGroup(colors);
        });

        viewModel.isButtonEnabled().observe(getViewLifecycleOwner(), isEnabled -> {
            addGarmentButton.setEnabled(isEnabled);
        });

        addGarmentImageView.setOnClickListener(v -> {
            // Controlla se il permesso è già stato concesso
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                // Se sì, lancia la fotocamera
                launchCamera();
            } else {
                // Altrimenti, richiedi il permesso
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        garmentNameEditText.addTextChangedListener(new TextWatcher() { //controllo inserimento testo
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                // Notifica il ViewModel del nuovo nome
                viewModel.setGarmentName(s.toString());
            }
        });

        setupAddChip();
        // Opzionale: listener per sapere quando l'utente seleziona un'opzione
        autoCompleteTextView.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedItem = (String) parent.getItemAtPosition(position);
            Toast.makeText(getContext(), "Selezionato: " + selectedItem, Toast.LENGTH_SHORT).show();
            viewModel.setSelectedSeason(selectedItem);
        });
    }

    private void launchCamera() {
        takePictureLauncher.launch(null);
    }

    private void setupAddChip() {
        Chip addChip = new Chip(requireContext());
        addChip.setText("+");
        addChip.setOnClickListener(v -> {
            // Chiedi al ViewModel la lista completa dei colori per mostrarla nel dialog
            List<String> allColors = viewModel.getAllColors().getValue();
            // Chiedi al ViewModel quali colori sono già selezionati
            List<String> currentSelection = viewModel.getSelectedColors().getValue();
            if (allColors != null && currentSelection != null) {
                showChipSelectionDialog(allColors, currentSelection);
            }
        });
        colorChipGroup.addView(addChip, 0);
    }

    private void showChipSelectionDialog(List<String> allOptions, List<String> currentSelection) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.chip_selector, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        ChipGroup dialogChipGroup = dialogView.findViewById(R.id.dialog_chip_group);
        Button okButton = dialogView.findViewById(R.id.dialog_ok_button);

        // Popola il dialog
        for (String option : allOptions) {
            Chip chip = new Chip(requireContext());
            chip.setText(option);
            chip.setCheckable(true);
            if (currentSelection.contains(option)) {
                chip.setChecked(true);
            }
            dialogChipGroup.addView(chip);
        }

        AlertDialog dialog = builder.create();

        dialogChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            okButton.setEnabled(!checkedIds.isEmpty());
        });
        okButton.setEnabled(!dialogChipGroup.getCheckedChipIds().isEmpty());

        okButton.setOnClickListener(v -> {
            List<String> newSelection = new ArrayList<>();
            for (int id : dialogChipGroup.getCheckedChipIds()) {
                Chip selectedChip = dialogChipGroup.findViewById(id);
                newSelection.add(selectedChip.getText().toString());
            }
            // --- NOTIFICA IL VIEWMODEL ---
            // Invece di modificare la UI direttamente, aggiorna il ViewModel.
            // Sarà l'observer a chiamare updateMainChipGroup.
            viewModel.updateSelectedColors(newSelection);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateMainChipGroup(List<String> selectedItems) {
        // Rimuovi tutte le chip tranne quella "+"
        for (int i = colorChipGroup.getChildCount() - 1; i >= 0; i--) {
            View child = colorChipGroup.getChildAt(i);
            if (child instanceof Chip && !((Chip) child).getText().toString().equals("+")) {
                colorChipGroup.removeView(child);
            }
        }

        // Aggiungi le nuove chip
        for (String item : selectedItems) {
            Chip chip = new Chip(requireContext());
            chip.setText(item);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                // Notifica il ViewModel della rimozione di un elemento
                List<String> currentSelection = new ArrayList<>(viewModel.getSelectedColors().getValue());
                currentSelection.remove(chip.getText().toString());
                viewModel.updateSelectedColors(currentSelection);
            });
            colorChipGroup.addView(chip);
        }
    }

}