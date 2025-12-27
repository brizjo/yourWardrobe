package it.unimib.yourwardrobe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.main.components.CardWardrobe;

public class ClothesAdapter extends RecyclerView.Adapter<ClothesAdapter.ClothesViewHolder> {

    private final List<Integer> clothesList; // Lista di ID immagini
    private final int layoutId; // ID del layout da utilizzare


    public ClothesAdapter(List<Integer> clothesList, int layoutId) {
        this.clothesList = clothesList;
        this.layoutId = layoutId;
    }

    public ClothesAdapter(List<Integer> clothesList) {
        this(clothesList, R.layout.item_clothes_carousel);
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
        Integer imageResId = clothesList.get(position);
        holder.bind(imageResId);
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

        public void bind(Integer imageResId) {
            if (cardWardrobe != null && imageResId != null) {
                cardWardrobe.setCardImage(ContextCompat.getDrawable(itemView.getContext(), imageResId));
            }
        }
    }
}

