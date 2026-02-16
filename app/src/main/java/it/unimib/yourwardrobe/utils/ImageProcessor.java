package it.unimib.yourwardrobe.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation;
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenter;
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions;

public class ImageProcessor {

    private final SubjectSegmenter segmenter;

    public ImageProcessor() {
        SubjectSegmenterOptions options = new SubjectSegmenterOptions.Builder()
                .enableForegroundBitmap() // Ci serve il bitmap del soggetto
                .build();
        this.segmenter = SubjectSegmentation.getClient(options);
    }

    public Task<Bitmap> removeBackground(Bitmap sourceBitmap) {
        InputImage image = InputImage.fromBitmap(sourceBitmap, 0);

        return segmenter.process(image).continueWith(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().getForegroundBitmap() != null) {
                // Ritorna il bitmap del solo soggetto (scontornato)
                return task.getResult().getForegroundBitmap();
            } else {
                // Se fallisce, ritorna l'originale (fallback sicuro)
                return sourceBitmap;
            }
        });
    }
}