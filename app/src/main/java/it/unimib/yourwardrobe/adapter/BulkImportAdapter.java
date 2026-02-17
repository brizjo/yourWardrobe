package it.unimib.yourwardrobe.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.main.viewmodel.BulkImportViewModel.GarmentImportItem;

public class BulkImportAdapter extends RecyclerView.Adapter<BulkImportAdapter.ViewHolder> {

    private static final String TAG = "BulkImportAdapter";

    public interface OnItemChangeListener {
        void onChange(int index, String value);
    }

    public interface OnColorChangeListener {
        void onChange(int index, List<String> colors);
    }

    public interface OnRemoveListener {
        void onRemove(int index);
    }

    private List<GarmentImportItem> items = new ArrayList<>();
    private final OnItemChangeListener nameListener;
    private final OnItemChangeListener categoryListener;
    private final OnItemChangeListener seasonListener;
    private final OnColorChangeListener colorListener;
    private final OnRemoveListener removeListener;

    // ✅ Typeface passati dal fragment
    private Typeface popstarTypeface;
    private Typeface changoTypeface;

    public BulkImportAdapter(OnItemChangeListener nameListener,
                             OnItemChangeListener categoryListener,
                             OnItemChangeListener seasonListener,
                             OnColorChangeListener colorListener,
                             OnRemoveListener removeListener) {
        this.nameListener = nameListener;
        this.categoryListener = categoryListener;
        this.seasonListener = seasonListener;
        this.colorListener = colorListener;
        this.removeListener = removeListener;
    }

    // ✅ Metodo chiamato dal fragment per passare i font
    public void setTypefaces(Typeface popstar, Typeface chango) {
        this.popstarTypeface = popstar;
        this.changoTypeface = chango;
    }

    public void submitList(List<GarmentImportItem> newItems) {
        if (newItems != null) {
            this.items = new ArrayList<>(newItems);
            notifyDataSetChanged();
            Log.d(TAG, "submitList: " + items.size() + " items");
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bulk_import_garment, parent, false);
        return new ViewHolder(view, popstarTypeface, changoTypeface);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GarmentImportItem item = items.get(position);
        Log.d(TAG, "onBindViewHolder position=" + position + ", id=" + item.getId() +
                ", colors=" + item.getColors());
        holder.bind(item, nameListener, categoryListener, seasonListener, colorListener, removeListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView garmentImage;
        private final TextInputEditText nameInput;
        private final AutoCompleteTextView categoryDropdown;
        private final AutoCompleteTextView seasonDropdown;
        private final TextInputEditText colorInput;
        private final TextInputLayout colorInputLayout;
        private final ChipGroup colorsChipGroup;
        private final MaterialButton removeButton;

        // ✅ font salvati nel ViewHolder
        private final Typeface popstar;
        private final Typeface chango;

        private TextWatcher nameTextWatcher;
        private boolean isUpdatingName = false;

        public ViewHolder(@NonNull View itemView, Typeface popstar, Typeface chango) {
            super(itemView);
            this.popstar = popstar;
            this.chango = chango;

            garmentImage = itemView.findViewById(R.id.garment_image);
            nameInput = itemView.findViewById(R.id.name_input);
            categoryDropdown = itemView.findViewById(R.id.category_dropdown);
            seasonDropdown = itemView.findViewById(R.id.season_dropdown);
            colorInput = itemView.findViewById(R.id.color_input);
            colorInputLayout = itemView.findViewById(R.id.color_input_layout);
            colorsChipGroup = itemView.findViewById(R.id.colors_chip_group);
            removeButton = itemView.findViewById(R.id.remove_button);

            // ✅ applica popstar a tutti i campi testo
            if (popstar != null) {
                nameInput.setTypeface(popstar);
                categoryDropdown.setTypeface(popstar);
                seasonDropdown.setTypeface(popstar);
                colorInput.setTypeface(popstar);
                removeButton.setTypeface(popstar);
            }
        }

        public void bind(GarmentImportItem item,
                         OnItemChangeListener nameListener,
                         OnItemChangeListener categoryListener,
                         OnItemChangeListener seasonListener,
                         OnColorChangeListener colorListener,
                         OnRemoveListener removeListener) {

            Log.d(TAG, "=== BIND Item " + item.getId() + " ===");
            Log.d(TAG, "  Colori: " + item.getColors());

            garmentImage.setImageBitmap(item.getBitmap());

            if (nameTextWatcher != null) {
                nameInput.removeTextChangedListener(nameTextWatcher);
            }

            isUpdatingName = true;
            nameInput.setText(item.getName());
            isUpdatingName = false;

            nameTextWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (!isUpdatingName) {
                        nameListener.onChange(item.getId(), s.toString());
                    }
                }
            };
            nameInput.addTextChangedListener(nameTextWatcher);

            // Setup categoria dropdown
            String[] categories = {"Parte superiore", "Parte inferiore", "Calzature", "Accessorio"};
            ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                    itemView.getContext(), android.R.layout.simple_dropdown_item_1line, categories);
            categoryDropdown.setAdapter(categoryAdapter);
            categoryDropdown.setText(item.getCategory(), false);
            categoryDropdown.setOnItemClickListener((parent, view, position, id) -> {
                String selected = (String) parent.getItemAtPosition(position);
                categoryListener.onChange(item.getId(), selected);
            });

            // Setup stagione dropdown
            String[] seasons = {"Tutte le stagioni", "Primavera", "Estate", "Autunno", "Inverno",
                    "Inverno - Autunno", "Primavera - Estate", "Primavera - Autunno"};
            ArrayAdapter<String> seasonAdapter = new ArrayAdapter<>(
                    itemView.getContext(), android.R.layout.simple_dropdown_item_1line, seasons);
            seasonDropdown.setAdapter(seasonAdapter);
            seasonDropdown.setText(item.getSeason(), false);
            seasonDropdown.setOnItemClickListener((parent, view, position, id) -> {
                String selected = (String) parent.getItemAtPosition(position);
                seasonListener.onChange(item.getId(), selected);
            });

            updateColorsDisplay(item);
            updateColorsChips(item, colorListener);

            colorInput.setOnClickListener(v -> showColorSelectionDialog(item, colorListener));
            colorInputLayout.setEndIconOnClickListener(v -> showColorSelectionDialog(item, colorListener));
            removeButton.setOnClickListener(v -> removeListener.onRemove(item.getId()));
        }

        private void updateColorsDisplay(GarmentImportItem item) {
            if (item.getColors().isEmpty()) {
                colorInput.setText("Nessun colore selezionato");
            } else {
                colorInput.setText(String.join(", ", item.getColors()));
            }
        }

        private void updateColorsChips(GarmentImportItem item, OnColorChangeListener colorListener) {
            Log.d(TAG, "  updateColorsChips: " + item.getColors().size() + " colori");
            colorsChipGroup.removeAllViews();

            for (String color : item.getColors()) {
                Log.d(TAG, "    Aggiungendo chip: " + color);

                Chip chip = new Chip(itemView.getContext());
                chip.setText(color);

                // ✅ font chango + sfondo nero
                if (chango != null) chip.setTypeface(chango);
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.BLACK));
                chip.setTextColor(Color.WHITE);
                chip.setCloseIconTint(ColorStateList.valueOf(Color.WHITE));
                chip.setCloseIconVisible(true);

                chip.setOnCloseIconClickListener(v -> {
                    List<String> updatedColors = new ArrayList<>(item.getColors());
                    updatedColors.remove(color);
                    Log.d(TAG, "  Rimosso colore: " + color + ", rimasti: " + updatedColors);
                    colorListener.onChange(item.getId(), updatedColors);
                });
                colorsChipGroup.addView(chip);
            }
        }

        private void showColorSelectionDialog(GarmentImportItem item, OnColorChangeListener colorListener) {
            String[] allColors = {"Rosso", "Blu", "Verde", "Nero", "Bianco", "Grigio",
                    "Marrone", "Beige", "Rosa", "Giallo", "Arancione", "Viola"};

            boolean[] checkedColors = new boolean[allColors.length];
            List<String> selectedColors = new ArrayList<>(item.getColors());

            for (int i = 0; i < allColors.length; i++) {
                checkedColors[i] = selectedColors.contains(allColors[i]);
            }

            new MaterialAlertDialogBuilder(itemView.getContext())
                    .setTitle("Seleziona colori")
                    .setMultiChoiceItems(allColors, checkedColors, (dialog, which, isChecked) -> {
                        if (isChecked) {
                            if (!selectedColors.contains(allColors[which]))
                                selectedColors.add(allColors[which]);
                        } else {
                            selectedColors.remove(allColors[which]);
                        }
                    })
                    .setPositiveButton("OK", (dialog, which) -> {
                        Log.d(TAG, "Dialog OK - Colori selezionati: " + selectedColors);
                        colorListener.onChange(item.getId(), selectedColors);
                    })
                    .setNegativeButton("Annulla", null)
                    .show();
        }
    }
}