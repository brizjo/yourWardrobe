package it.unimib.yourwardrobe.ui.main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.Outfit;
import it.unimib.yourwardrobe.ui.main.components.CardOutfit;

public class OutfitGridAdapter extends RecyclerView.Adapter<OutfitGridAdapter.OutfitGridViewHolder> {

    private final int layoutId;
    private final OnItemClickListener listener;
    private final List<Outfit> outfits;

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

    public void updateOutfits(List<Outfit> newOutfits) {
        this.outfits.clear();
        this.outfits.addAll(newOutfits);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, Outfit outfit);
    }

    public static class OutfitGridViewHolder extends RecyclerView.ViewHolder {
        private final CardOutfit cardOutfit;
        private final TextView tvOutfitName;

        public OutfitGridViewHolder(@NonNull View itemView) {
            super(itemView);
            cardOutfit = itemView.findViewById(R.id.card_outfit_component);
            tvOutfitName = itemView.findViewById(R.id.tv_outfit_name_grid);
        }

        public void bind(Outfit outfit, OnItemClickListener listener) {
            if (outfit != null) {
                if (cardOutfit != null && outfit.getGarments() != null) {
                    cardOutfit.setGarments(outfit.getGarments());
                }

                // Rimuovi corner radius, bordo ed elevazione della card interna
                if (cardOutfit != null) {
                    MaterialCardView innerCard = cardOutfit.findViewById(R.id.card_outfit_root);
                    if (innerCard != null) {
                        innerCard.setStrokeWidth(0);
                        innerCard.setCardElevation(0f);
                    }
                }

                if (tvOutfitName != null) {
                    tvOutfitName.setText(outfit.getName());
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(v, outfit);
                }
            });
        }
    }
}