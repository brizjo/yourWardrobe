package it.unimib.yourwardrobe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.ui.main.components.CardWardrobe;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import android.graphics.drawable.Drawable;

public class ClothesAdapter extends RecyclerView.Adapter<ClothesAdapter.ClothesViewHolder> {

    private final List<Garment> clothesList; // Lista di ID immagini
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

        private final CardWardrobe cardWardrobe;

        public ClothesViewHolder(@NonNull View itemView) {
            super(itemView);
            cardWardrobe = itemView.findViewById(R.id.clothesCard);
        }

        public void bind(Garment garment, OnItemClickListener listener) {
            if (cardWardrobe != null && garment != null) {// Caricamento immagine tramite Glide nell'ImageView del componente custom
                Glide.with(itemView.getContext())
                        .load(garment.getImageUrl())
                        .placeholder(R.drawable.ic_launcher_background)
                        .centerCrop()
                        .into(new CustomTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                cardWardrobe.setCardImage(resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                cardWardrobe.setCardImage(placeholder);
                            }
                        });

                cardWardrobe.setOnCardClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, garment);
                    }
                });
            }
        }
    }
}

