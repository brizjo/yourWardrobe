package it.unimib.yourwardrobe.source.remote;
import android.graphics.Bitmap;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.model.User;

public class GarmentRemoteDataSource {

    private final ImageLabeler classifier; //usato per controllare se foto è un vestito

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final AuthRemoteDataSource auth = new AuthRemoteDataSource();

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
    public GarmentRemoteDataSource(){
        //todo: commentare
        this.classifier = ImageLabeling.getClient(new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.6f)
                .build());

    }

    public void isGarment(Bitmap bitmap, Callback<Boolean> callback) {


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



        public void uploadImage(Bitmap image, Callback<String> callback) {

        User currentUser = auth.getCurrentUser();
        String uid = currentUser.getUid();
        if (uid == null){
            callback.onFailure("Problemi con autenticazione", new IllegalStateException("Utente non autenticato"));
            return;
        }


            String fileName = currentUser.getUid()+"_"+System.currentTimeMillis()+".jpg";
            StorageReference storageRef = storage.getReference()
                    .child("users")
                    .child(uid)
                    .child("garments")
                    .child(fileName);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 95, baos);
            byte [] byteFoto = baos.toByteArray();

            storageRef.putBytes(byteFoto).addOnSuccessListener(taskSnapshot->{

                storageRef.getDownloadUrl()
                        .addOnFailureListener(e -> callback.onFailure("errore nella creazione url foto", e))
                        .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()));

            })
                    .addOnFailureListener(e -> callback.onFailure("errore durante l'upload della foto", e));


    }


    public void saveGarmentDocument(Garment garment, Callback<Boolean> callback){

        User currentUser = auth.getCurrentUser();
        String uid = currentUser.getUid();
        if ( uid == null){
            callback.onFailure("Problemi con autenticazione", new IllegalStateException("Utente non autenticato"));
            return;
        }

        DocumentReference newDoc = db.collection("user")
                .document(uid)
                .collection("garments")
                .document(); // Genera l'ID


        garment.setId(newDoc.getId());



        newDoc.set(garment)
                .addOnCompleteListener(aVoid ->callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onFailure("errore durante l'upload del documento su firestore", e));








    }
}
