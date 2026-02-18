package it.unimib.yourwardrobe.ui.main.viewmodel;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.utils.Callback;

@HiltViewModel
public class BulkImportViewModel extends ViewModel {

    private final GarmentRepository garmentRepository;

    private final MutableLiveData<List<GarmentImportItem>> garmentItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isProcessing = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Integer> rejectedCount = new MutableLiveData<>();  // ← NUOVO

    @Inject
    public BulkImportViewModel(GarmentRepository garmentRepository) {
        this.garmentRepository = garmentRepository;
    }

    public LiveData<List<GarmentImportItem>> getGarmentItems() {
        return garmentItems;
    }

    public LiveData<Boolean> getIsProcessing() {
        return isProcessing;
    }

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Integer> getRejectedCount() {  // ← NUOVO
        return rejectedCount;
    }

    /**
     * Processa le immagini selezionate con ML Kit
     */
    public void processImages(List<Bitmap> bitmaps) {
        isProcessing.setValue(true);
        List<GarmentImportItem> items = new ArrayList<>();
        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger rejectedImagesCount = new AtomicInteger(0);  // ← NUOVO
        int totalImages = bitmaps.size();

        for (int i = 0; i < bitmaps.size(); i++) {
            final int index = i;
            Bitmap bitmap = bitmaps.get(i);

            // Valida prima se è un capo
            garmentRepository.validateGarment(bitmap, new Callback<Boolean>() {
                @Override
                public void onSuccess(Boolean isValid) {
                    if (isValid) {
                        // Riconosci categoria, stagione e colore con ML Kit
                        detectGarmentAttributes(bitmap, index, items, processedCount, totalImages, rejectedImagesCount);
                    } else {
                        // Immagine rifiutata
                        rejectedImagesCount.incrementAndGet();  // ← NUOVO
                        int completed = processedCount.incrementAndGet();
                        if (completed == totalImages) {
                            garmentItems.postValue(items);
                            isProcessing.postValue(false);

                            // Notifica foto rifiutate
                            int rejected = rejectedImagesCount.get();
                            if (rejected > 0) {
                                rejectedCount.postValue(rejected);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(String error, Throwable t) {
                    rejectedImagesCount.incrementAndGet();  // ← NUOVO
                    int completed = processedCount.incrementAndGet();
                    if (completed == totalImages) {
                        garmentItems.postValue(items);
                        isProcessing.postValue(false);

                        int rejected = rejectedImagesCount.get();
                        if (rejected > 0) {
                            rejectedCount.postValue(rejected);
                        }
                    }
                }
            });
        }
    }

    /**
     * Rileva attributi del capo con ML Kit (categoria, stagione, colore)
     */
    private void detectGarmentAttributes(Bitmap bitmap, int index,
                                         List<GarmentImportItem> items,
                                         AtomicInteger processedCount,
                                         int totalImages,
                                         AtomicInteger rejectedImagesCount) {

        garmentRepository.detectGarmentAttributes(bitmap, new Callback<GarmentRepository.GarmentAttributes>() {
            @Override
            public void onSuccess(GarmentRepository.GarmentAttributes attributes) {
                GarmentImportItem item = new GarmentImportItem(
                        index,
                        bitmap,
                        attributes.getCategory(),
                        attributes.getSeason(),
                        attributes.getColors()
                );
                items.add(item);

                int completed = processedCount.incrementAndGet();
                if (completed == totalImages) {
                    garmentItems.postValue(items);
                    isProcessing.postValue(false);

                    int rejected = rejectedImagesCount.get();
                    if (rejected > 0) {
                        rejectedCount.postValue(rejected);
                    }
                }
            }

            @Override
            public void onFailure(String error, Throwable t) {
                // Aggiungi comunque con valori di default
                GarmentImportItem item = new GarmentImportItem(
                        index,
                        bitmap,
                        "Parte superiore",
                        "Tutte le stagioni",
                        new ArrayList<>()
                );
                items.add(item);

                int completed = processedCount.incrementAndGet();
                if (completed == totalImages) {
                    garmentItems.postValue(items);
                    isProcessing.postValue(false);

                    int rejected = rejectedImagesCount.get();
                    if (rejected > 0) {
                        rejectedCount.postValue(rejected);
                    }
                }
            }
        });
    }

    public void updateGarmentName(int index, String name) {
        List<GarmentImportItem> currentItems = garmentItems.getValue();
        if (currentItems != null) {
            for (GarmentImportItem item : currentItems) {
                if (item.getId() == index) {
                    item.setName(name);
                    break;
                }
            }
        }
    }

    public void updateGarmentCategory(int index, String category) {
        List<GarmentImportItem> currentItems = garmentItems.getValue();
        if (currentItems != null) {
            for (GarmentImportItem item : currentItems) {
                if (item.getId() == index) {
                    item.setCategory(category);
                    break;
                }
            }
            garmentItems.setValue(currentItems);
        }
    }

    public void updateGarmentSeason(int index, String season) {
        List<GarmentImportItem> currentItems = garmentItems.getValue();
        if (currentItems != null) {
            for (GarmentImportItem item : currentItems) {
                if (item.getId() == index) {
                    item.setSeason(season);
                    break;
                }
            }
            garmentItems.setValue(currentItems);
        }
    }

    public void updateGarmentColor(int index, List<String> colors) {
        List<GarmentImportItem> currentItems = garmentItems.getValue();
        if (currentItems != null) {
            for (GarmentImportItem item : currentItems) {
                if (item.getId() == index) {
                    item.setColors(new ArrayList<>(colors));  // Crea nuova lista
                    break;
                }
            }
            // IMPORTANTE: Crea una NUOVA lista per triggerare l'observer
            List<GarmentImportItem> newList = new ArrayList<>(currentItems);
            garmentItems.setValue(newList);
        }
    }

    public void removeGarment(int index) {
        List<GarmentImportItem> currentItems = garmentItems.getValue();
        if (currentItems != null) {
            currentItems.removeIf(item -> item.getId() == index);
            garmentItems.setValue(currentItems);
        }
    }

    /**
     * Salva tutti i capi nel repository
     */
    public void saveAllGarments() {
        List<GarmentImportItem> items = garmentItems.getValue();
        if (items == null || items.isEmpty()) {
            errorMessage.setValue("Nessun capo da salvare");
            return;
        }

        isProcessing.setValue(true);
        AtomicInteger savedCount = new AtomicInteger(0);
        int totalItems = items.size();

        for (GarmentImportItem item : items) {
            Garment garment = new Garment();
            garment.setName(item.getName());
            garment.setCategory(item.getCategory());
            garment.setSeason(item.getSeason());
            garment.setColor(item.getColors());

            garmentRepository.saveGarmentWithImage(item.getBitmap(), garment, new Callback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    int completed = savedCount.incrementAndGet();
                    if (completed == totalItems) {
                        isProcessing.postValue(false);
                        saveSuccess.postValue(true);
                    }
                }

                @Override
                public void onFailure(String error, Throwable t) {
                    isProcessing.postValue(false);
                    errorMessage.postValue("Errore nel salvataggio: " + error);
                }
            });
        }
    }

    /**
     * Classe per rappresentare un capo in fase di importazione
     */
    public static class GarmentImportItem {
        private final int id;
        private final Bitmap bitmap;
        private String name;
        private String category;
        private String season;
        private List<String> colors;

        public GarmentImportItem(int id, Bitmap bitmap, String category,
                                 String season, List<String> colors) {
            this.id = id;
            this.bitmap = bitmap;
            this.name = "Capo " + (id + 1);
            this.category = category;
            this.season = season;
            this.colors = new ArrayList<>(colors);  // Crea copia
        }

        public int getId() {
            return id;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getSeason() {
            return season;
        }

        public void setSeason(String season) {
            this.season = season;
        }

        public List<String> getColors() {
            return new ArrayList<>(colors);
        }  // Ritorna copia

        public void setColors(List<String> colors) {
            this.colors = new ArrayList<>(colors);
        }
    }
}