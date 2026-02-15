package it.unimib.yourwardrobe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.Garment;

public class OutfitComponentAdapter extends RecyclerView.Adapter<OutfitComponentAdapter.ViewHolder> {

    private final List<Garment> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Garment garment);
    }

    public OutfitComponentAdapter(List<Garment> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_outfit_component, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Garment g = items.get(position);
        Glide.with(holder.itemView.getContext())
                .load(g.getImageUrl())
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(g));
    }

    @Override
    public int getItemCount() { return items != null ? items.size() : 0; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.iv_component_image);
        }
    }
}