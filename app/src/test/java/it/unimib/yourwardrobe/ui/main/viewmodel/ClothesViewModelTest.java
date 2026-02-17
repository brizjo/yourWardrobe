package it.unimib.yourwardrobe.ui.main.viewmodel;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import android.app.Application;
import android.content.res.Resources;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import androidx.lifecycle.Observer;
import it.unimib.yourwardrobe.core.functional.Callback;

import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.R;

@RunWith(MockitoJUnitRunner.class)
public class ClothesViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Resources mockResources;
    @Mock
    private Application mockApplication;
    @Mock
    private GarmentRepository mockGarmentRepository;

    @Mock
    private Observer<List<Garment>> garmentsObserver;
    @Mock
    private Observer<Boolean> isEmptyObserver;

    @Captor
    private ArgumentCaptor<List<Garment>> garmentListCaptor;

    private ClothesViewModel clothesViewModel;
    private List<Garment> fakeGarments;

    @Before
    public void setUp() {
        when(mockApplication.getResources()).thenReturn(mockResources);
        when(mockResources.getStringArray(any(Integer.class))).thenReturn(new String[0]);
        when(mockApplication.getString(R.string.top_garment)).thenReturn("Parte superiore");
        when(mockApplication.getString(R.string.bottom_garment)).thenReturn("Parte inferiore");
        when(mockApplication.getString(R.string.footwear)).thenReturn("Calzature");
        when(mockApplication.getString(R.string.accessory)).thenReturn("Accessorio");

        Garment tShirt = new Garment();
        tShirt.setName("T-shirt");
        tShirt.setCategory("Parte superiore");
        tShirt.setColor(Collections.singletonList("Rosso"));

        Garment jeans = new Garment();
        jeans.setName("Jeans");
        jeans.setCategory("Parte inferiore");
        jeans.setColor(Collections.singletonList("Blu"));

        Garment camicia = new Garment();
        camicia.setName("Camicia");
        camicia.setCategory("Parte superiore");
        camicia.setColor(Collections.singletonList("Bianco"));
        fakeGarments = Arrays.asList(tShirt, jeans, camicia);

        doAnswer(invocation -> {
            // 1. Usa il tuo Callback, non quello di Chromium
            Callback<List<Garment>> callback = invocation.getArgument(0);
            callback.onSuccess(new ArrayList<>(fakeGarments));
            return null;
        }).when(mockGarmentRepository).getGarments(any(Callback.class)); // 2. Usa il tuo Callback anche qui

        clothesViewModel = new ClothesViewModel(mockApplication, mockGarmentRepository);
    }

    @Test
    public void fetchGarments_onInit_loadsAndCategorizesGarments() {
        // Test per verificare che i dati vengano caricati e categorizzati correttamente all'avvio.

        clothesViewModel.getTopGarments().observeForever(garmentsObserver);
        clothesViewModel.getBottomGarments().observeForever(garmentsObserver);

        List<Garment> topGarments = clothesViewModel.getTopGarments().getValue();
        List<Garment> bottomGarments = clothesViewModel.getBottomGarments().getValue();

        assertNotNull(topGarments);
        assertNotNull(bottomGarments);
        assertEquals("Dovrebbero esserci 2 capi nella lista top", 2, topGarments.size());
        assertEquals("Dovrebbe esserci 1 capo nella lista bottom", 1, bottomGarments.size());
        assertEquals("Il capo bottom dovrebbe essere 'Jeans'", "Jeans", bottomGarments.get(0).getName());
    }


    @Test
    public void filterByColor_withValidColor_returnsFilteredList() {
        // Test per verificare che il filtro per colore funzioni.

        clothesViewModel.getGridGarments().observeForever(garmentsObserver);

        clothesViewModel.setDisplayMode(ClothesViewModel.DisplayMode.GRID_ALPHABETICAL);
        clothesViewModel.filterByColor(Collections.singletonList("Rosso"));
        clothesViewModel.fetchGarments();
        verify(garmentsObserver, org.mockito.Mockito.atLeastOnce()).onChanged(garmentListCaptor.capture());
        List<Garment> filteredList = garmentListCaptor.getValue();

        assertNotNull(filteredList);
        assertEquals("La lista filtrata dovrebbe contenere 1 solo capo", 1, filteredList.size());
        assertEquals("Il capo filtrato dovrebbe essere 'T-shirt'", "T-shirt", filteredList.get(0).getName());
    }


    @Test
    public void getIsWardrobeEmpty_whenRepositoryIsEmpty_returnsTrue() {
        // Test per lo stato "guardaroba vuoto".

        doAnswer(invocation -> {
            Callback<List<Garment>> callback = invocation.getArgument(0);
            callback.onSuccess(new ArrayList<>()); // Lista vuota
            return null;
        }).when(mockGarmentRepository).getGarments(any(Callback.class));

        clothesViewModel.getIsWardrobeEmpty().observeForever(isEmptyObserver);

        clothesViewModel.fetchGarments();

        // Verifica
        ArgumentCaptor<Boolean> captor = ArgumentCaptor.forClass(Boolean.class);
        verify(isEmptyObserver, org.mockito.Mockito.atLeastOnce()).onChanged(captor.capture());
        assertTrue("isWardrobeEmpty deve essere true se il repository è vuoto", captor.getValue());
    }


    @Test
    public void getGridGarments_whenSortedAlphabetically_returnsSortedList() {
        // Test per l'ordinamento alfabetico.

        clothesViewModel.getGridGarments().observeForever(garmentsObserver);

        clothesViewModel.setDisplayMode(ClothesViewModel.DisplayMode.GRID_ALPHABETICAL);

        verify(garmentsObserver, org.mockito.Mockito.atLeastOnce()).onChanged(garmentListCaptor.capture());
        List<Garment> sortedList = garmentListCaptor.getValue();

        assertNotNull(sortedList);
        assertFalse(sortedList.isEmpty());

        assertEquals("Il primo elemento dovrebbe essere 'Camicia'", "Camicia", sortedList.get(0).getName());
        assertEquals("Il secondo elemento dovrebbe essere 'Jeans'", "Jeans", sortedList.get(1).getName());
        assertEquals("Il terzo elemento dovrebbe essere 'T-shirt'", "T-shirt", sortedList.get(2).getName());
    }
}