package it.unimib.yourwardrobe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.ui.main.components.CardGarment;
import com.bumptech.glide.Glide;

import android.widget.ImageView;

public class ClothesAdapter extends RecyclerView.Adapter<ClothesAdapter.ClothesViewHolder> {

    private List<Garment> clothesList; // Lista di ID immagini
    private final int layoutId; // ID del layout da utilizzare
    private final OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Garment garment);
    }

    public ClothesAdapter(List<Garment> clothesList, int layoutId, OnItemClickListener onItemClickListener) {
        this.clothesList = clothesList;
        this.layoutId = layoutId;
        this.onItemClickListener = onItemClickListener;
    }

    public ClothesAdapter(List<Garment> clothesList, OnItemClickListener onItemClickListener) {
        this(clothesList, R.layout.item_clothes_carousel, onItemClickListener);
    }

    public void updateGarments(List<Garment> newGarments) {
        this.clothesList.clear();
        this.clothesList.addAll(newGarments);
        notifyDataSetChanged(); // Notifica al RecyclerView di ridisegnare tutto
    }

    @NonNull
    @Override
    public ClothesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(this.layoutId, parent, false);
        return new ClothesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClothesViewHolder holder, int position) {
        Garment garment = clothesList.get(position);
        holder.bind(garment, onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return clothesList != null ? clothesList.size() : 0;
    }
    public static class ClothesViewHolder extends RecyclerView.ViewHolder {

        private final CardGarment cardGarment;
        private final ImageView targetImageView;

        public ClothesViewHolder(@NonNull View itemView) {
            super(itemView);
            cardGarment = itemView.findViewById(R.id.clothesCard);
            targetImageView = cardGarment.findViewById(R.id.card_garment_image);
        }

        public void bind(Garment garment, OnItemClickListener listener) {
            if (cardGarment != null && garment != null) {// Caricamento immagine tramite Glide nell'ImageView del componente custom
                Glide.with(itemView.getContext())
                        .load(garment.getImageUrl())
                        .placeholder(R.drawable.ic_launcher_background)
                        .centerCrop()
                        .into(targetImageView);

                cardGarment.setOnCardClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, garment);
                    }
                });
            }
        }
    }
}

