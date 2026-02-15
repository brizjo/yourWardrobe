package it.unimib.yourwardrobe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.Outfit;
import it.unimib.yourwardrobe.ui.main.components.CardOutfit;

public class OutfitGridAdapter extends RecyclerView.Adapter<OutfitGridAdapter.OutfitGridViewHolder> {

    private final List<Outfit> outfits;
    private final int layoutId;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View view, Outfit outfit);
    }

    public OutfitGridAdapter(List<Outfit> outfits, int layoutId, OnItemClickListener listener) {
        this.outfits = outfits;
        this.layoutId = layoutId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OutfitGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new OutfitGridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OutfitGridViewHolder holder, int position) {
        Outfit outfit = outfits.get(position);
        holder.bind(outfit, listener);
    }

    @Override
    public int getItemCount() {
        return outfits != null ? outfits.size() : 0;
    }

    public static class OutfitGridViewHolder extends RecyclerView.ViewHolder {
        private final CardOutfit cardOutfit;

        public OutfitGridViewHolder(@NonNull View itemView) {
            super(itemView);
            // Questo ID deve essere presente in item_outfit_grid.xml
            cardOutfit = itemView.findViewById(R.id.card_outfit_component);
        }

        public void bind(Outfit outfit, OnItemClickListener listener) {
            if (cardOutfit != null && outfit != null) {
                // Passiamo la lista dei vestiti al componente custom
                // Assicurati che il metodo in Outfit.java si chiami getGarments() o getComponents()
                cardOutfit.setGarments(outfit.getGarments());
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(v, outfit);
                }
            });
        }
    }
}