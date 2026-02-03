package it.unimib.yourwardrobe.ui.main.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.main.viewmodel.AddGarmentViewModel;
import it.unimib.yourwardrobe.utils.ToastHelper;

public class AddGarmentFragment extends Fragment {

    private ImageView addGarmentImageView;
    private AutoCompleteTextView categoryTextView;
    private ChipGroup colorChipGroup;
    private ChipGroup styleChipGroup;
    private ChipGroup fabricChipGroup;
    private AddGarmentViewModel viewModel;

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
    private TextInputEditText garmentNameEditText;
    private Button addGarmentButton;


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
        // 1. Recupera il repository dal ServiceLocator

        // 2. Crea la Factory passando Application e il Repository

        viewModel = new ViewModelProvider(this).get(AddGarmentViewModel.class);
        addGarmentImageView = view.findViewById(R.id.addGarmentImage);
        categoryTextView = view.findViewById(R.id.category_text_view);
        colorChipGroup = view.findViewById(R.id.chip_group_color);
        styleChipGroup = view.findViewById(R.id.chip_group_style);
        fabricChipGroup = view.findViewById(R.id.chip_group_fabric);
        garmentNameEditText = view.findViewById(R.id.garmentName);
        addGarmentButton = view.findViewById(R.id.add_garment_button);

        addGarmentImageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_add_photo));

        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
            categoryTextView.setAdapter(adapter);
        });

        viewModel.getSelectedColors().observe(getViewLifecycleOwner(), colors -> {
            updateMainChipGroup(colorChipGroup, colors, selected -> viewModel.updateSelectedColors(selected));
        });

        viewModel.getSelectedStyles().observe(getViewLifecycleOwner(), styles -> {
            updateMainChipGroup(styleChipGroup, styles, selected -> viewModel.updateSelectedStyles(selected));
        });

        viewModel.getSelectedFabrics().observe(getViewLifecycleOwner(), fabrics -> {
            updateMainChipGroup(fabricChipGroup, fabrics, selected -> viewModel.updateSelectedFabrics(selected));
        });

        //todo: vedere geterrormessage
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                ToastHelper.show(getContext(), error, false);
            }
        });

        viewModel.isButtonEnabled().observe(getViewLifecycleOwner(), isEnabled -> {
            addGarmentButton.setEnabled(isEnabled);
        });

        viewModel.getGarmentAddedSuccessfully().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                ToastHelper.show(getContext(), "Capo aggiunto con successo!", false);
                // Torna al fragment precedente (es. WardrobeFragment)
                Navigation.findNavController(view).navigateUp();
            }
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Notifica il ViewModel del nuovo nome
                viewModel.setGarmentName(s.toString());
            }
        });

        setupAddChip(colorChipGroup, "color");
        setupAddChip(styleChipGroup, "style");
        setupAddChip(fabricChipGroup, "fabric");

        addGarmentButton.setOnClickListener(v -> {
            // Chiama il metodo nel ViewModel
            viewModel.saveGarment();
        });

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
        addChip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.md_theme_primary)));
        addChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary));
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
            } else if ("fabric".equals(type)) {
                allOptions = viewModel.getAllFabrics().getValue();
                currentSelection = viewModel.getSelectedFabrics().getValue();
                dialogTitle = "Seleziona Tessuti";
            }

            if (allOptions != null && currentSelection != null) {
                showChipSelectionDialog(dialogTitle, allOptions, currentSelection, newSelection -> {
                    if ("color".equals(type)) {
                        viewModel.updateSelectedColors(newSelection);
                    } else if ("style".equals(type)) {
                        viewModel.updateSelectedStyles(newSelection);
                    } else if ("fabric".equals(type)) {
                        viewModel.updateSelectedFabrics(newSelection);
                    }
                });
            }
        });
        chipGroup.addView(addChip, 0);
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
        int colorSelected = ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimaryContainer);
        int colorDefault = ContextCompat.getColor(requireContext(), R.color.md_theme_primaryContainer);
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked}, // Stato: selezionato (checked)
                new int[]{-android.R.attr.state_checked}  // Stato: non selezionato
        };
        int[] colors = new int[]{
                colorSelected,
                colorDefault
        };
        ColorStateList colorStateList = new ColorStateList(states, colors);

        int textColorSelected = ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary); // Bianco quando selezionato
        int textColorDefault = ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimaryContainer);   // Nero/scuro quando non selezionato

        int[][] textStates = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };
        int[] textColors = new int[]{
                textColorSelected,
                textColorDefault
        };
        ColorStateList chipTextColorStateList = new ColorStateList(textStates, textColors);

        // Popola il dialog
        for (String option : allOptions) {
            Chip chip = new Chip(requireContext());
            chip.setText(option);
            chip.setCheckable(true);
            chip.setChipBackgroundColor(colorStateList);
            chip.setTextColor(chipTextColorStateList);

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
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.md_theme_primary)));
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimary));
            chip.setOnCloseIconClickListener(v -> {
                // Notifica il ViewModel della rimozione di un elemento
                List<String> currentSelection = new ArrayList<>(selectedItems);
                currentSelection.remove(chip.getText().toString());
                listener.onConfirmed(currentSelection);
            });
            chipGroup.addView(chip);
        }
    }

    interface OnSelectionConfirmedListener {
        void onConfirmed(List<String> newSelection);
    }

}