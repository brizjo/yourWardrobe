package it.unimib.yourwardrobe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.utils.GlideLoader;

public class SelectClothesAdapter extends RecyclerView.Adapter<SelectClothesAdapter.ViewHolder> {

    private final List<Garment> items;
    private final OnItemClickListener listener;
    private final List<Garment> selectedGarments = new ArrayList<>();
    private final boolean isMultiSelect;  // ← NUOVO
    private final int maxSelection;       // ← NUOVO

    public interface OnItemClickListener {
        void onItemClick(Garment garment, boolean isSelected);
    }

    /**
     * @param items Lista di capi disponibili
     * @param isMultiSelect true per selezione multipla, false per singola
     * @param maxSelection Numero massimo di selezioni (usato solo se isMultiSelect=true)
     * @param listener Callback quando viene selezionato un capo
     */
    public SelectClothesAdapter(List<Garment> items, boolean isMultiSelect, int maxSelection, OnItemClickListener listener) {
        this.items = items;
        this.isMultiSelect = isMultiSelect;
        this.maxSelection = maxSelection;
        this.listener = listener;
    }

    /**
     * Imposta i capi già selezionati (per mostrare lo stato attuale)
     */
    public void setSelectedGarments(List<Garment> selected) {
        selectedGarments.clear();
        if (selected != null) {
            selectedGarments.addAll(selected);
        }
        notifyDataSetChanged();
    }

    /**
     * Ottieni la lista dei capi selezionati
     */
    public List<Garment> getSelectedGarments() {
        return new ArrayList<>(selectedGarments);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_garment_selectable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Garment garment = items.get(position);
        boolean isSelected = selectedGarments.contains(garment);
        holder.bind(garment, isSelected);

        holder.itemView.setOnClickListener(v -> {
            if (isMultiSelect) {
                // SELEZIONE MULTIPLA
                if (selectedGarments.contains(garment)) {
                    // Deseleziona
                    selectedGarments.remove(garment);
                    notifyItemChanged(position);
                    if (listener != null) {
                        listener.onItemClick(garment, false);
                    }
                } else {
                    // Seleziona (se non hai raggiunto il massimo)
                    if (selectedGarments.size() < maxSelection) {
                        selectedGarments.add(garment);
                        notifyItemChanged(position);
                        if (listener != null) {
                            listener.onItemClick(garment, true);
                        }
                    } else {
                        // Mostra messaggio che hai raggiunto il limite
                        android.widget.Toast.makeText(
                                v.getContext(),
                                "Puoi selezionare al massimo " + maxSelection + " capi",
                                android.widget.Toast.LENGTH_SHORT
                        ).show();
                    }
                }
            } else {
                // SELEZIONE SINGOLA
                selectedGarments.clear();
                selectedGarments.add(garment);
                notifyDataSetChanged();
                if (listener != null) {
                    listener.onItemClick(garment, true);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void clearSelection() {
        selectedGarments.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardGarment;
        private final ImageView ivGarment;
        private final View overlaySelected;
        private final ImageView ivCheck;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardGarment = itemView.findViewById(R.id.card_garment);
            ivGarment = itemView.findViewById(R.id.iv_garment);
            overlaySelected = itemView.findViewById(R.id.overlay_selected);
            ivCheck = itemView.findViewById(R.id.iv_check);
        }

        public void bind(Garment garment, boolean isSelected) {
            // Carica l'immagine con Glide
            GlideLoader.loadImage(itemView.getContext(), garment.getImageUrl(), ivGarment);


            // Aggiorna l'UI in base allo stato di selezione
            updateSelectionUI(isSelected);
        }

        private void updateSelectionUI(boolean isSelected) {
            if (isSelected) {
                // STATO SELEZIONATO
                cardGarment.setStrokeWidth(8);
                cardGarment.setStrokeColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.md_theme_primary)
                );
                cardGarment.setCardElevation(8f);
                overlaySelected.setVisibility(View.VISIBLE);
                ivCheck.setVisibility(View.VISIBLE);

                itemView.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(150)
                        .start();

            } else {
                // STATO NON SELEZIONATO
                cardGarment.setStrokeWidth(0);
                cardGarment.setCardElevation(2f);
                overlaySelected.setVisibility(View.GONE);
                ivCheck.setVisibility(View.GONE);

                itemView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start();
            }
        }
    }
}