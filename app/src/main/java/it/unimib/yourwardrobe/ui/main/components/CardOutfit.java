package it.unimib.yourwardrobe.ui.main.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import java.util.List;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.utils.GlideLoader;

public class CardOutfit extends MaterialCardView {
    private LinearLayout container;

    public CardOutfit(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.card_outfit_grid, this, true);
        container = findViewById(R.id.outfit_images_container);
    }

    public void setGarments(List<Garment> garments) {
        container.removeAllViews();
        if (garments == null || garments.isEmpty()) return;

        int size = Math.min(garments.size(), 6);

        if (size == 2) {
            // 2 Vestiti: Uno sopra l'altro (Verticale)
            container.setOrientation(LinearLayout.VERTICAL);
            addImagesToContainer(garments.subList(0, 2), container);
        } else {
            // Da 3 a 6: Griglia a due colonne
            container.setOrientation(LinearLayout.HORIZONTAL);

            // Colonna Sinistra
            LinearLayout leftCol = createColumn();
            container.addView(leftCol);

            // Colonna Destra
            LinearLayout rightCol = createColumn();
            container.addView(rightCol);

            int leftCount = (size + 1) / 2;
            addImagesToContainer(garments.subList(0, leftCount), leftCol);
            addImagesToContainer(garments.subList(leftCount, size), rightCol);
        }
    }

    private LinearLayout createColumn() {
        LinearLayout col = new LinearLayout(getContext());
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
        return col;
    }

    private void addImagesToContainer(List<Garment> list, LinearLayout target) {
        for (Garment g : list) {
            ImageView iv = new ImageView(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, 0, 1f);
            iv.setLayoutParams(lp);
            iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            iv.setPadding(4, 4, 4, 4);

            // FIX: Imposta lo sfondo trasparente iniziale per evitare il flash verde
            iv.setBackgroundColor(android.graphics.Color.TRANSPARENT);

            target.addView(iv);

            // Carica l'immagine usando la utility ottimizzata
            GlideLoader.loadImage(getContext(), g.getImageUrl(), iv);
        }
    }
}