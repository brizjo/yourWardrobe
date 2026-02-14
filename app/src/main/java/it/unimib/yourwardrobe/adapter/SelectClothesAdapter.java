package it.unimib.yourwardrobe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.ui.main.components.CardGarment;

public class SelectClothesAdapter extends RecyclerView.Adapter<SelectClothesAdapter.ViewHolder> {
    private final List<Garment> items;
    private final OnItemClickListener listener;private int selectedPosition = -1;

    public interface OnItemClickListener {
        void onItemClick(Garment garment);
    }

    public SelectClothesAdapter(List<Garment> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_clothes_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Garment garment = items.get(position);
        Glide.with(holder.itemView.getContext())
                .load(garment.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.card.getImageView()); // Assicurati che CardGarment esponga l'ImageView

        holder.bind(garment, position == selectedPosition);
        holder.itemView.setAlpha(position == selectedPosition ? 0.5f : 1.0f);
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getBindingAdapterPosition();
            if (selectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousPosition);
                notifyItemChanged(selectedPosition);
                listener.onItemClick(garment);
            }
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardGarment card;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.clothesCard);
        }
        public void bind(Garment garment, boolean isSelected) {
            // Qui carichi l'immagine nel tuo componente custom
            // Esempio: card.setImage(garment.getImageUrl());
            itemView.setAlpha(isSelected ? 0.5f : 1.0f);
        }
    }
}
