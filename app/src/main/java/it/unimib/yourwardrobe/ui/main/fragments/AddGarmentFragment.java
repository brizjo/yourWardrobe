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
import android.widget.TextView;
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
    private AutoCompleteTextView categoryTextView;
    private ChipGroup colorChipGroup;
    private ChipGroup styleChipGroup;
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
        categoryTextView = view.findViewById(R.id.category_text_view);
        colorChipGroup = view.findViewById(R.id.chip_group_color);
        styleChipGroup = view.findViewById(R.id.chip_group_style);
        garmentNameEditText = view.findViewById(R.id.garmentName);
        addGarmentButton = view.findViewById(R.id.add_garment_button);

        addGarmentImageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_add_photo));

        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
            categoryTextView.setAdapter(adapter);
        });

        viewModel.getSelectedColors().observe(getViewLifecycleOwner(), colors -> {
            updateMainChipGroup(colorChipGroup, colors, selected ->viewModel.updateSelectedColors(colors));
        });

        viewModel.getSelectedStyles().observe(getViewLifecycleOwner(), styles -> {
            updateMainChipGroup(styleChipGroup, styles, selected ->viewModel.updateSelectedStyles(selected));
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

        setupAddChip(colorChipGroup, "color");
        setupAddChip(styleChipGroup, "style");

        // Opzionale: listener per sapere quando l'utente seleziona un'opzione
        categoryTextView.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedItem = (String) parent.getItemAtPosition(position);
            Toast.makeText(getContext(), "Selezionato: " + selectedItem, Toast.LENGTH_SHORT).show();
            viewModel.setSelectedCategory(selectedItem);
        });
    }

    private void launchCamera() {
        takePictureLauncher.launch(null);
    }

    private void setupAddChip(ChipGroup chipGroup, final String type) {
        Chip addChip = new Chip(requireContext());
        addChip.setText("+");
        addChip.setOnClickListener(v -> {
            List<String> allOptions = null;
            List<String> currentSelection = null;
            String dialogTitle = "";

            if ("color".equals(type)) {
                allOptions = viewModel.getAllColors().getValue();
                currentSelection = viewModel.getSelectedColors().getValue();
                dialogTitle = "Seleziona Colori";
            } else if ("style".equals(type)) {
                allOptions = viewModel.getAllStyles().getValue();
                currentSelection = viewModel.getSelectedStyles().getValue();
                dialogTitle = "Seleziona Stili";
            }

            if (allOptions != null && currentSelection != null) {
                showChipSelectionDialog(dialogTitle, allOptions, currentSelection, newSelection -> {
                    if ("color".equals(type)) {
                        viewModel.updateSelectedColors(newSelection);
                    } else if ("style".equals(type)) {
                        viewModel.updateSelectedStyles(newSelection);
                    }
                });
            }
        });
        chipGroup.addView(addChip, 0);
    }

    interface OnSelectionConfirmedListener {
        void onConfirmed(List<String> newSelection);
    }

    private void showChipSelectionDialog(String title, List<String> allOptions, List<String> currentSelection, OnSelectionConfirmedListener listener) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.chip_selector, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        ChipGroup dialogChipGroup = dialogView.findViewById(R.id.dialog_chip_group);
        Button okButton = dialogView.findViewById(R.id.dialog_ok_button);

        dialogTitle.setText(title);

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
            // Usa il listener per notificare il ViewModel
            listener.onConfirmed(newSelection);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateMainChipGroup(ChipGroup chipGroup, List<String> selectedItems, OnSelectionConfirmedListener listener) {
        // Rimuovi tutte le chip tranne quella "+"
        for (int i = chipGroup.getChildCount() - 1; i >= 0; i--) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip && !((Chip) child).getText().toString().equals("+")) {
                chipGroup.removeView(child);
            }
        }

        // Aggiungi le nuove chip
        for (String item : selectedItems) {
            Chip chip = new Chip(requireContext());
            chip.setText(item);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                // Notifica il ViewModel della rimozione di un elemento
                List<String> currentSelection = new ArrayList<>(selectedItems);
                currentSelection.remove(chip.getText().toString());
                listener.onConfirmed(currentSelection);
            });
            chipGroup.addView(chip);
        }
    }

}