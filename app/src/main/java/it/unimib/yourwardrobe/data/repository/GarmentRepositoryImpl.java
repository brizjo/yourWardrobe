package it.unimib.yourwardrobe.data.repository;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import androidx.palette.graphics.Palette;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import it.unimib.yourwardrobe.data.remote.GarmentRemoteDataSource;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.utils.Callback;

public class GarmentRepositoryImpl implements GarmentRepository {

    private static final String TAG = "GarmentRepositoryImpl";
    private final GarmentRemoteDataSource dataSource;

    @Inject
    public GarmentRepositoryImpl(GarmentRemoteDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void validateGarment(Bitmap garmentBitmap, Callback<Boolean> callback) {
        dataSource.isGarment(garmentBitmap, callback);
    }

    @Override
    public void addGarment(Bitmap image, Garment garment, Callback<Boolean> callback) {
        dataSource.uploadImage(image, new Callback<String>() {
            @Override
            public void onSuccess(String imageUrl) {
                garment.setImageUrl(imageUrl);
                dataSource.saveGarmentDocument(garment, new Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Log.d(TAG, "Documento salvato con successo.");
                        callback.onSuccess(true);
                    }

                    @Override
                    public void onFailure(String errorMessage, Throwable t) {
                        callback.onFailure("Errore salvataggio dati: " + errorMessage, t);
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage, Throwable t) {
                callback.onFailure("Errore caricamento immagine: " + errorMessage, t);
            }
        });
    }

    @Override
    public void updateGarmentImage(Bitmap newImage, Garment garment, Callback<Boolean> callback) {
        dataSource.uploadImage(newImage, new Callback<String>() {
            @Override
            public void onSuccess(String newImageUrl) {
                garment.setImageUrl(newImageUrl);
                dataSource.updateGarment(garment, new Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Log.d(TAG, "Immagine aggiornata con successo.");
                        callback.onSuccess(true);
                    }

                    @Override
                    public void onFailure(String errorMessage, Throwable t) {
                        callback.onFailure("Errore aggiornamento documento: " + errorMessage, t);
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage, Throwable t) {
                callback.onFailure("Errore caricamento nuova immagine: " + errorMessage, t);
            }
        });
    }

    @Override
    public void deleteGarment(Garment garment, Callback<Boolean> callback) {
        dataSource.deleteGarment(garment, callback);
    }

    @Override
    public void updateGarment(Garment garment, Callback<Boolean> callback) {
        dataSource.updateGarment(garment, callback);
    }

    @Override
    public void getGarmentsByCategory(String category, Callback<List<Garment>> callback) {
        dataSource.getGarmentsByCategory(category, callback);
    }

    @Override
    public void getGarments(Callback<List<Garment>> callback) {
        dataSource.getGarments(callback);
    }

    /**
     * Rileva categoria, stagione e colori del capo con ML Kit e Palette API
     */
    @Override
    public void detectGarmentAttributes(Bitmap bitmap, Callback<GarmentAttributes> callback) {
        try {
            Log.d(TAG, "=== RILEVAMENTO ATTRIBUTI CAPO ===");

            // Delega il rilevamento di categoria e stagione al DataSource (ML Kit)
            dataSource.detectGarmentAttributes(bitmap, new Callback<GarmentAttributes>() {
                @Override
                public void onSuccess(GarmentAttributes attributes) {
                    // Aggiungi il rilevamento dei colori con Palette API
                    List<String> detectedColors = detectDominantColors(bitmap);

                    // Crea nuovi attributi con i colori rilevati
                    GarmentAttributes enrichedAttributes = new GarmentAttributes(
                            attributes.getCategory(),
                            attributes.getSeason(),
                            detectedColors
                    );

                    Log.d(TAG, "✅ Attributi rilevati - Categoria: " + enrichedAttributes.getCategory() +
                            ", Stagione: " + enrichedAttributes.getSeason() +
                            ", Colori: " + enrichedAttributes.getColors());

                    callback.onSuccess(enrichedAttributes);
                }

                @Override
                public void onFailure(String error, Throwable t) {
                    Log.e(TAG, "❌ Errore rilevamento ML Kit, uso fallback", t);

                    // Fallback: rileva solo i colori
                    List<String> colors = detectDominantColors(bitmap);
                    GarmentAttributes fallbackAttributes = new GarmentAttributes(
                            "Parte superiore",  // Default
                            "Tutte le stagioni", // Default
                            colors
                    );
                    callback.onSuccess(fallbackAttributes);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Errore nel rilevamento attributi", e);
            callback.onFailure("Errore nel rilevamento attributi", e);
        }
    }

    /**
     * Rileva i colori dominanti nell'immagine usando Palette API
     */
    private List<String> detectDominantColors(Bitmap bitmap) {
        try {
            Log.d(TAG, "Rilevamento colori dominanti...");

            Palette palette = Palette.from(bitmap).generate();
            List<String> colors = new ArrayList<>();

            // Colore dominante
            if (palette.getDominantSwatch() != null) {
                int rgb = palette.getDominantSwatch().getRgb();
                String colorName = mapRgbToColorName(rgb);
                colors.add(colorName);
                Log.d(TAG, "  - Colore dominante: " + colorName);
            }

            // Colore vibrante (se diverso dal dominante)
            if (palette.getVibrantSwatch() != null) {
                int rgb = palette.getVibrantSwatch().getRgb();
                String colorName = mapRgbToColorName(rgb);
                if (!colors.contains(colorName)) {
                    colors.add(colorName);
                    Log.d(TAG, "  - Colore vibrante: " + colorName);
                }
            }

            // Colore muted (se diverso dai precedenti)
            if (palette.getMutedSwatch() != null && colors.size() < 2) {
                int rgb = palette.getMutedSwatch().getRgb();
                String colorName = mapRgbToColorName(rgb);
                if (!colors.contains(colorName)) {
                    colors.add(colorName);
                    Log.d(TAG, "  - Colore muted: " + colorName);
                }
            }

            // Se non abbiamo trovato colori, usa default
            if (colors.isEmpty()) {
                colors.add("Nero");
                Log.d(TAG, "  - Nessun colore rilevato, uso default: Nero");
            }

            return colors;

        } catch (Exception e) {
            Log.e(TAG, "Errore nel rilevamento colori", e);
            return List.of("Nero"); // Default in caso di errore
        }
    }

    /**
     * Mappa un colore RGB al nome del colore più vicino
     */
    private String mapRgbToColorName(int rgb) {
        int red = Color.red(rgb);
        int green = Color.green(rgb);
        int blue = Color.blue(rgb);

        // Calcola luminosità
        double brightness = (red * 0.299 + green * 0.587 + blue * 0.114);

        // Bianco/Nero/Grigio (colori acromatici)
        if (brightness > 220) return "Bianco";
        if (brightness < 50) return "Nero";

        // Grigio (quando i canali RGB sono simili)
        if (Math.abs(red - green) < 30 && Math.abs(green - blue) < 30 && Math.abs(red - blue) < 30) {
            return "Grigio";
        }

        // Trova il canale dominante
        int max = Math.max(red, Math.max(green, blue));
        int min = Math.min(red, Math.min(green, blue));
        int delta = max - min;

        // Se la saturazione è bassa, è grigio/beige
        if (delta < 40) {
            if (brightness > 150) return "Beige";
            return "Grigio";
        }

        // Colori primari e secondari
        if (red == max) {
            if (green > 150 && blue < 100) {
                // Rosso + Verde = Giallo/Arancione
                if (green > 200) return "Giallo";
                return "Arancione";
            } else if (blue > 150) {
                // Rosso + Blu = Rosa/Viola
                if (red > 200) return "Rosa";
                return "Viola";
            } else {
                return "Rosso";
            }
        } else if (green == max) {
            if (red > 150) {
                // Verde + Rosso = Giallo
                return "Giallo";
            } else if (blue > 150) {
                // Verde + Blu = Blu/Verde (dipende dalla dominanza)
                if (green > blue + 30) return "Verde";
                return "Blu";
            } else {
                return "Verde";
            }
        } else if (blue == max) {
            if (red > 150) {
                // Blu + Rosso = Viola
                return "Viola";
            } else {
                return "Blu";
            }
        }

        // Colori specifici per marroni
        if (red > 100 && green > 60 && green < red && blue < 80) {
            return "Marrone";
        }

        // Default
        return "Grigio";
    }

    @Override
    public void saveGarmentWithImage(Bitmap image, Garment garment, Callback<Boolean> callback) {
        // Riusa il metodo addGarment esistente
        addGarment(image, garment, callback);
    }
}