package it.unimib.yourwardrobe.data.remote;

import android.graphics.Bitmap;
import android.util.Log;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.model.User;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.utils.Callback;

public class GarmentRemoteDataSource {

    private static final Set<String> KEYWORDS_GARMENT = new HashSet<>(Arrays.asList(
            "Clothing", "Outerwear", "Top", "Apparel", "Garment",
            "Shirt", "T-shirt", "Pants", "Dress", "Suit", "Jersey", "Trousers", "Jeans",
            "Shorts", "Skirt", "Coat", "Jacket", "Vest", "Sweater", "Cardigan", "Blouse",
            "Hoodie", "Uniform", "Activewear", "Sportswear",
            "Sleeve", "Pocket", "Neck", "Collar", "Zipper", "Hem",
            "Wool", "Cotton", "Denim", "Leather", "Silk", "Woven", "Knitting",
            "Scarf", "Tie", "Belt", "Gloves", "Hat", "Beanie", "Sunglasses", "Watch",
            "Shoe", "Footwear", "Sneakers", "Sandal", "Boot", "High heels", "Sport shoe",
            "Jewelry", "Necklace", "Ring", "Bracelet", "Earrings", "Pendant", "Gemstone", "Bling"
    ));

    private static final Map<String, Set<String>> CATEGORY_KEYWORDS = new HashMap<>();
    private static final Map<String, Set<String>> COLOR_KEYWORDS = new HashMap<>();
    private static final Map<String, Set<String>> SEASON_KEYWORDS = new HashMap<>();

    static {
        CATEGORY_KEYWORDS.put("Parte superiore", new HashSet<>(Arrays.asList(
                "Shirt", "T-shirt", "Blouse", "Top", "Sweater", "Cardigan", "Hoodie", "Jacket", "Coat", "Vest"
        )));
        CATEGORY_KEYWORDS.put("Parte inferiore", new HashSet<>(Arrays.asList(
                "Pants", "Trousers", "Jeans", "Shorts", "Skirt", "Leggings"
        )));
        CATEGORY_KEYWORDS.put("Calzature", new HashSet<>(Arrays.asList(
                "Shoe", "Footwear", "Sneakers", "Sandal", "Boot", "High heels", "Sport shoe"
        )));
        CATEGORY_KEYWORDS.put("Accessorio", new HashSet<>(Arrays.asList(
                "Scarf", "Tie", "Belt", "Gloves", "Hat", "Beanie", "Sunglasses", "Watch",
                "Jewelry", "Necklace", "Ring", "Bracelet", "Earrings"
        )));
    }

    static {
        COLOR_KEYWORDS.put("Rosso", new HashSet<>(Arrays.asList("Red", "Crimson", "Scarlet")));
        COLOR_KEYWORDS.put("Blu", new HashSet<>(Arrays.asList("Blue", "Navy", "Azure")));
        COLOR_KEYWORDS.put("Verde", new HashSet<>(Arrays.asList("Green", "Olive")));
        COLOR_KEYWORDS.put("Nero", new HashSet<>(List.of("Black")));
        COLOR_KEYWORDS.put("Bianco", new HashSet<>(List.of("White")));
        COLOR_KEYWORDS.put("Grigio", new HashSet<>(Arrays.asList("Gray", "Grey", "Silver")));
        COLOR_KEYWORDS.put("Marrone", new HashSet<>(Arrays.asList("Brown", "Tan", "Beige")));
        COLOR_KEYWORDS.put("Beige", new HashSet<>(Arrays.asList("Beige", "Cream", "Tan")));
        COLOR_KEYWORDS.put("Rosa", new HashSet<>(Arrays.asList("Pink", "Rose")));
        COLOR_KEYWORDS.put("Giallo", new HashSet<>(Arrays.asList("Yellow", "Gold")));
        COLOR_KEYWORDS.put("Arancione", new HashSet<>(List.of("Orange")));
        COLOR_KEYWORDS.put("Viola", new HashSet<>(Arrays.asList("Purple", "Violet")));
    }

    static {
        SEASON_KEYWORDS.put("Estate", new HashSet<>(Arrays.asList(
                "T-shirt", "Shorts", "Sandal", "Tank top", "Swimwear", "Light"
        )));
        SEASON_KEYWORDS.put("Inverno", new HashSet<>(Arrays.asList(
                "Coat", "Jacket", "Sweater", "Boot", "Wool", "Heavy", "Warm"
        )));
        SEASON_KEYWORDS.put("Primavera", new HashSet<>(Arrays.asList(
                "Light jacket", "Cardigan", "Dress"
        )));
        SEASON_KEYWORDS.put("Autunno", new HashSet<>(Arrays.asList(
                "Cardigan", "Vest", "Long sleeve"
        )));
    }

    private final ImageLabeler classifier;
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;
    private final AuthRemoteDataSource auth;

    @Inject
    public GarmentRemoteDataSource(AuthRemoteDataSource auth, FirebaseStorage storage, FirebaseFirestore firestore) {
        this.auth = auth;
        this.firestore = firestore;
        this.storage = storage;
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
        }).addOnFailureListener(e -> callback.onFailure(e.getMessage(), e));
    }

    public void detectGarmentAttributes(Bitmap bitmap, Callback<GarmentRepository.GarmentAttributes> callback) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        classifier.process(image).addOnSuccessListener(labels -> {
            String detectedCategory = "Parte superiore";
            String detectedSeason = "Tutte le stagioni";
            List<String> detectedColors = new ArrayList<>();

            for (ImageLabel label : labels) {
                Log.d("MLKit", "Label: " + label.getText() + " - Confidence: " + label.getConfidence());
            }

            for (Map.Entry<String, Set<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
                for (ImageLabel label : labels) {
                    if (entry.getValue().contains(label.getText())) {
                        detectedCategory = entry.getKey();
                        break;
                    }
                }
            }

            for (Map.Entry<String, Set<String>> entry : COLOR_KEYWORDS.entrySet()) {
                for (ImageLabel label : labels) {
                    if (entry.getValue().contains(label.getText())) {
                        if (!detectedColors.contains(entry.getKey())) {
                            detectedColors.add(entry.getKey());
                        }
                        break;
                    }
                }
            }

            int summerScore = 0, winterScore = 0, springScore = 0, autumnScore = 0;
            for (ImageLabel label : labels) {
                String labelText = label.getText();
                if (SEASON_KEYWORDS.get("Estate").contains(labelText)) summerScore++;
                if (SEASON_KEYWORDS.get("Inverno").contains(labelText)) winterScore++;
                if (SEASON_KEYWORDS.get("Primavera").contains(labelText)) springScore++;
                if (SEASON_KEYWORDS.get("Autunno").contains(labelText)) autumnScore++;
            }

            int maxScore = Math.max(Math.max(summerScore, winterScore), Math.max(springScore, autumnScore));
            if (maxScore > 0) {
                if (maxScore == summerScore) detectedSeason = "Estate";
                else if (maxScore == winterScore) detectedSeason = "Inverno";
                else if (maxScore == springScore) detectedSeason = "Primavera";
                else if (maxScore == autumnScore) detectedSeason = "Autunno";
            }

            Log.d("MLKit", "Categoria: " + detectedCategory + ", Stagione: " + detectedSeason + ", Colori: " + detectedColors);
            callback.onSuccess(new GarmentRepository.GarmentAttributes(detectedCategory, detectedSeason, detectedColors));

        }).addOnFailureListener(e -> {
            Log.e("MLKit", "Errore nel rilevamento attributi", e);
            callback.onSuccess(new GarmentRepository.GarmentAttributes("Parte superiore", "Tutte le stagioni", new ArrayList<>()));
        });
    }

    public void uploadImage(Bitmap image, Callback<String> callback) {
        User currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("Utente non autenticato", new IllegalStateException("User null"));
            return;
        }
        String uid = currentUser.getUid();

        String fileName = uid + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference storageRef = storage.getReference()
                .child("users").child(uid).child("garments").child(fileName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 95, baos);
        byte[] byteFoto = baos.toByteArray();

        storageRef.putBytes(byteFoto)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl()
                                .addOnFailureListener(e -> callback.onFailure("Errore nella creazione url foto", e))
                                .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                )
                .addOnFailureListener(e -> callback.onFailure("Errore durante l'upload della foto", e));
    }

    public void saveGarmentDocument(Garment garment, Callback<Boolean> callback) {
        User currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("Utente non autenticato", new IllegalStateException("User null"));
            return;
        }
        String uid = currentUser.getUid();

        DocumentReference newDoc = firestore.collection("user")
                .document(uid).collection("garments").document();

        garment.setId(newDoc.getId());
        garment.setCreatedAt(new Date());
        newDoc.set(garment)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FIRESTORE_SUCCESS", "Documento scritto correttamente");
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE_ERROR", "Errore durante la scrittura: " + e.getMessage());
                    callback.onFailure("Errore durante l'upload del documento su firestore", e);
                });
    }

    public void deleteGarment(Garment garment, Callback<Boolean> callback) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            callback.onFailure("Utente non autenticato", new IllegalStateException("User null"));
            return;
        }
        if (garment.getId() == null) {
            callback.onFailure("Vestito non trovato: dati mancanti", new IllegalStateException("ID non valido"));
            return;
        }

        firestore.collection("user").document(uid).collection("garments")
                .document(garment.getId())
                .delete()
                .addOnSuccessListener(x -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onFailure("Errore durante la cancellazione del documento", e));
    }

    public void updateGarment(Garment garment, Callback<Boolean> callback) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            callback.onFailure("Utente non autenticato", new IllegalStateException("User null"));
            return;
        }
        if (garment == null || garment.getId() == null) {
            callback.onFailure("Dati del capo non validi", new IllegalArgumentException("Garment o ID è null"));
            return;
        }

        firestore.collection("user").document(uid).collection("garments").document(garment.getId())
                .set(garment)
                .addOnSuccessListener(aVoid -> {
                    Log.i("GarmentDataSource", "Capo aggiornato con successo.");
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("GarmentDataSource", "Errore durante l'aggiornamento.", e);
                    callback.onFailure("Errore durante l'aggiornamento", e);
                });
    }

    public void getGarments(Callback<List<Garment>> callback) {
        User currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("Utente non autenticato", new IllegalStateException("User null"));
            return;
        }
        String uid = currentUser.getUid();

        firestore.collection("user").document(uid).collection("garments")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage(), error);
                        return;
                    }
                    if (value != null) {
                        callback.onSuccess(value.toObjects(Garment.class));
                    }
                });
    }

    public void getGarmentsByCategory(String category, Callback<List<Garment>> callback) {
        User currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("Utente non autenticato", new IllegalStateException("User null"));
            return;
        }
        String uid = currentUser.getUid();

        firestore.collection("user").document(uid).collection("garments")
                .whereEqualTo("category", category)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage(), error);
                        return;
                    }
                    if (value != null) {
                        callback.onSuccess(value.toObjects(Garment.class));
                    }
                });
    }
}