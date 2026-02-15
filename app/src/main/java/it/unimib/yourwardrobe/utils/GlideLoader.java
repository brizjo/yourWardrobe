package it.unimib.yourwardrobe.utils;

import android.content.Context;
import android.widget.ImageView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import it.unimib.yourwardrobe.R;

public class GlideLoader {

    // utils/GlideLoader.java
    public static void loadImage(Context context, String url, ImageView imageView) {
        CircularProgressDrawable progressDrawable = new CircularProgressDrawable(context);
        progressDrawable.setStrokeWidth(5f);
        progressDrawable.setCenterRadius(30f);
        progressDrawable.setColorSchemeColors(context.getColor(R.color.md_theme_primary));
        progressDrawable.start();

        Glide.with(context)
                .load(url)
                .placeholder(progressDrawable)
                .error(R.drawable.ic_launcher_background) // Assicurati che non sia verde!
                .transition(DrawableTransitionOptions.withCrossFade()) // <--- AGGIUNGI: Transizione fluida
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerInside() // Importante per non sformare i vestiti
                .into(imageView);
    }
}