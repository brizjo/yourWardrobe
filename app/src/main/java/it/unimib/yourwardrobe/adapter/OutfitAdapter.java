package it.unimib.yourwardrobe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.main.components.CardGarment;


//todo: questa è una classe temporanea di placeholder
public class OutfitAdapter extends RecyclerView.Adapter<OutfitAdapter.OutfitViewHolder> {

    private final List<Integer> outfitImages;
    private final int layoutId;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View view, Integer item);
    }

    public OutfitAdapter(List<Integer> outfitImages, int layoutId, OnItemClickListener listener) {
        this.outfitImages = outfitImages;
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
        Integer resId = outfitImages.get(position);
        holder.bind(resId, listener);
    }

    @Override
    public int getItemCount() {
        return outfitImages != null ? outfitImages.size() : 0;
    }

    public static class OutfitViewHolder extends RecyclerView.ViewHolder {
        private final CardGarment cardGarment;

        public OutfitViewHolder(@NonNull View itemView) {
            super(itemView);
            // Cerca l'ID del componente nel tuo item_outfit_grid.xml
            // Se nel layout si chiama diversamente, correggi R.id.clothesCard
            cardGarment = itemView.findViewById(R.id.clothesCard);
        }

        public void bind(Integer resId, OnItemClickListener listener) {
            if (cardGarment != null) {
                cardGarment.setCardImage(ContextCompat.getDrawable(itemView.getContext(), resId));
                cardGarment.setOnCardClickListener(v -> {
                    if (listener != null) listener.onItemClick(v, resId);
                });
            }
        }
    }
}