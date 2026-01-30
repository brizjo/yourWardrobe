package it.unimib.yourwardrobe.source.remote;
import android.graphics.Bitmap;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unimib.yourwardrobe.core.functional.Callback;

public class GarmentRecognitionDataSource {

    private final ImageLabeler classifier;

    private static final Set<String> KEYWORDS_GARMENT = new HashSet<>(Arrays.asList(
            // Categorie Generali (Rimosso "Fashion", "Textile", "Pattern" perché troppo generici)
            "Clothing", "Outerwear", "Top", "Apparel", "Garment",

            // Capi Specifici (Mantieni questi, sono molto precisi)
            "Shirt", "T-shirt", "Pants", "Dress", "Suit", "Jersey", "Trousers", "Jeans",
            "Shorts", "Skirt", "Coat", "Jacket", "Vest", "Sweater", "Cardigan", "Blouse",
            "Hoodie", "Uniform", "Activewear", "Sportswear",

            // Parti di vestiti (Rimosso "Button" perché ML Kit lo vede in ogni cerchio)
            "Sleeve", "Pocket", "Neck", "Collar", "Zipper", "Hem",

            // Materiali (Mantieni solo quelli univoci dei vestiti)
            "Wool", "Cotton", "Denim", "Leather", "Silk", "Woven", "Knitting",

            // Accessori
            "Scarf", "Tie", "Belt", "Gloves", "Hat"
    ));
    //TODO: SI POTREBBE FARE CIO PER OGNI CATEGORIA (ESEMPIO: SE L'UTETNTE INSERISCE UN VESTITO DA SEZIONE MAGLIETTE E NON è UNA MAGLIETTA ALLORA RIFIUTA)
    public GarmentRecognitionDataSource(){
        //todo: commentare
        this.classifier = ImageLabeling.getClient(new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.6f)
                .build());

    }

    public void isGarment(Bitmap bitmap, Callback<Boolean> callback){


        InputImage fotoGarment = InputImage.fromBitmap(bitmap, 0);
        classifier.process(fotoGarment).addOnCompleteListener(labels -> {
                boolean garmentCheck = false;
                for (ImageLabel label : labels.getResult()) {

                    if (KEYWORDS_GARMENT.contains(label.getText())) {
                        garmentCheck = true;
                        break;

                }
                }
                callback.onSuccess(garmentCheck);
            }
        ).addOnFailureListener(e -> callback.onFailure(e.getMessage(), e)); // nel caso non funzioni classifier



    }
}
