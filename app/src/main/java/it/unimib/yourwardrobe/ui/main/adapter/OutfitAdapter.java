package it.unimib.yourwardrobe.ui.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.Outfit;

public class OutfitAdapter extends RecyclerView.Adapter<OutfitAdapter.OutfitViewHolder> {

    private final List<Outfit> outfitList;
    private final Context context;

    public OutfitAdapter(Context context, List<Outfit> outfitList) {
        this.context = context;
        this.outfitList = outfitList;
    }

    @NonNull
    @Override
    public OutfitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the XML card layout
        View view = LayoutInflater.from(context).inflate(R.layout.card_outfit, parent, false);
        return new OutfitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OutfitViewHolder holder, int position) {
        Outfit outfit = outfitList.get(position);
        holder.bind(outfit);
    }

    @Override
    public int getItemCount() {
        return outfitList.size();
    }

    public class OutfitViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivSweater, ivJacket, ivPants, ivGlasses, ivBoots, ivEarrings;
        private final MaterialButton btnLike, btnDislike;

        public OutfitViewHolder(@NonNull View itemView) {
            super(itemView);

            // Bind Views
            ivSweater = itemView.findViewById(R.id.iv_top_left);
            ivJacket = itemView.findViewById(R.id.iv_top_right);
            ivPants = itemView.findViewById(R.id.iv_mid_left);
            ivGlasses = itemView.findViewById(R.id.iv_mid_right);
            ivBoots = itemView.findViewById(R.id.iv_bot_left);
            ivEarrings = itemView.findViewById(R.id.iv_bot_right);

            btnLike = itemView.findViewById(R.id.btn_like);
            btnDislike = itemView.findViewById(R.id.btn_dislike);
        }

        public void bind(Outfit outfit) {
            // Load images using helper method
            loadImage(ivSweater, outfit.sweaterUrl);
            loadImage(ivJacket, outfit.jacketUrl);
            loadImage(ivPants, outfit.pantsUrl);
            loadImage(ivGlasses, outfit.glassesUrl);
            loadImage(ivBoots, outfit.bootsUrl);
            loadImage(ivEarrings, outfit.earringsUrl);

            // Handle Clicks
            btnLike.setOnClickListener(v -> {
                //TODO: Handle Like
            });
            btnLike.setOnClickListener(v -> {
                // TODO: Handle Like
            });

        }

        private void loadImage(ImageView view, String url) {
            if (url == null || url.isEmpty()) {
                // Hide ImageView if no URL prevents empty space gaps
                view.setVisibility(View.INVISIBLE);
                return;
            }
            view.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(url)
                    .fitCenter() // Ensures full image is visible
                    .transition(DrawableTransitionOptions.withCrossFade()) // Smooth fade-in
                    .into(view);
        }
    }
}