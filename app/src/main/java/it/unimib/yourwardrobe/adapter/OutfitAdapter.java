package it.unimib.yourwardrobe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.Outfit;
import it.unimib.yourwardrobe.ui.main.components.CardGarment;

public class OutfitAdapter extends RecyclerView.Adapter<OutfitAdapter.OutfitViewHolder> {

    private final List<Outfit> outfits;
    private final int layoutId;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View view, Outfit outfit);
    }

    public OutfitAdapter(List<Outfit> outfits, int layoutId, OnItemClickListener listener) {
        this.outfits = outfits;
        this.layoutId = layoutId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OutfitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new OutfitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OutfitViewHolder holder, int position) {
        holder.bind(outfits.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return outfits != null ? outfits.size() : 0;
    }

    public static class OutfitViewHolder extends RecyclerView.ViewHolder {
        private final CardGarment cardGarment;

        public OutfitViewHolder(@NonNull View itemView) {
            super(itemView);
            cardGarment = itemView.findViewById(R.id.clothesCard);
        }

        public void bind(Outfit outfit, OnItemClickListener listener) {
            if (cardGarment == null) return;

            // Carica l'immagine del primo capo come anteprima dell'outfit
            if (outfit.getGarments() != null && !outfit.getGarments().isEmpty()) {
                String imageUrl = outfit.getGarments().get(0).getImageUrl();
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(cardGarment.getImageView());
            }

            cardGarment.setOnCardClickListener(v -> {
                if (listener != null) listener.onItemClick(v, outfit);
            });
        }
    }
}