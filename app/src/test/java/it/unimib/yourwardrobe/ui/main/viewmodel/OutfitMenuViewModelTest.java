package it.unimib.yourwardrobe.ui.main.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import it.unimib.yourwardrobe.core.functional.Callback;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.model.Outfit;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.domain.repository.OutfitRepository;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {28})
public class OutfitMenuViewModelTest {

    // Regola per eseguire i LiveData in modo sincrono
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private GarmentRepository mockGarmentRepository;
    @Mock
    private OutfitRepository mockOutfitRepository;

    @Mock
    private Observer<OutfitMenuViewModel.UiState> uiStateObserver;
    @Mock
    private Observer<List<Outfit>> outfitsObserver;

    @Captor
    private ArgumentCaptor<OutfitMenuViewModel.UiState> uiStateCaptor;
    @Captor
    private ArgumentCaptor<List<Outfit>> outfitListCaptor;

    // Oggetto da testare
    private OutfitMenuViewModel outfitMenuViewModel;
    private Application application;

    @Before
    public void setUp() {

        MockitoAnnotations.openMocks(this);

        // Ottieni un contesto reale di Application grazie a Robolectric
        application = ApplicationProvider.getApplicationContext();

        // Inizializza il ViewModel con l'Application reale e i repository mock
        outfitMenuViewModel = new OutfitMenuViewModel(application, mockOutfitRepository, mockGarmentRepository);
    }

    @Test
    public void fetchAllData_whenNotEnoughGarments_setsUiStateToNotEnoughGarments() {
        // Prepara i dati di test: un solo capo, insufficiente per creare un outfit
        Garment tShirt = new Garment();
        tShirt.setName("T-shirt");
        tShirt.setCategory("Parte superiore");

        // Simula la risposta del GarmentRepository
        doAnswer(invocation -> {
            Callback<List<Garment>> callback = invocation.getArgument(0);
            callback.onSuccess(Collections.singletonList(tShirt));
            return null;
        }).when(mockGarmentRepository).getGarments(any(Callback.class));

        // Simula la risposta vuota dell'OutfitRepository
        doAnswer(invocation -> {
            Callback<List<Outfit>> callback = invocation.getArgument(0);
            callback.onSuccess(new ArrayList<>());
            return null;
        }).when(mockOutfitRepository).getOutfits(any(Callback.class));

        outfitMenuViewModel = new OutfitMenuViewModel(application, mockOutfitRepository, mockGarmentRepository);

        // Osserva il LiveData dello stato UI
        outfitMenuViewModel.getUiState().observeForever(uiStateObserver);

        // Verifica: cattura tutti i valori emessi da uiState
        verify(uiStateObserver, org.mockito.Mockito.atLeastOnce()).onChanged(uiStateCaptor.capture());
        List<OutfitMenuViewModel.UiState> capturedStates = uiStateCaptor.getAllValues();

        // L'ultimo stato deve essere NOT_ENOUGH_GARMENTS
        assertEquals(OutfitMenuViewModel.UiState.NOT_ENOUGH_GARMENTS, capturedStates.get(capturedStates.size() - 1));
    }

    @Test
    public void fetchAllData_withEnoughGarmentsButNoOutfits_setsUiStateToNoOutfits() {
        // Dati di test: capi sufficienti
        Garment tShirt = new Garment();
        tShirt.setCategory("Parte superiore");
        Garment jeans = new Garment();
        jeans.setCategory("Parte inferiore");

        // Simula la risposta del GarmentRepository
        doAnswer(invocation -> {
            Callback<List<Garment>> callback = invocation.getArgument(0);
            callback.onSuccess(Arrays.asList(tShirt, jeans));
            return null;
        }).when(mockGarmentRepository).getGarments(any(Callback.class));

        // Simula la risposta vuota dell'OutfitRepository
        doAnswer(invocation -> {
            Callback<List<Outfit>> callback = invocation.getArgument(0);
            callback.onSuccess(new ArrayList<>());
            return null;
        }).when(mockOutfitRepository).getOutfits(any(Callback.class));

        // Osserva il LiveData
        outfitMenuViewModel.getUiState().observeForever(uiStateObserver);

        // Azione
        outfitMenuViewModel.fetchOutfits();


        // Verifica
        verify(uiStateObserver, org.mockito.Mockito.atLeastOnce()).onChanged(uiStateCaptor.capture());
        List<OutfitMenuViewModel.UiState> capturedStates = uiStateCaptor.getAllValues();
        assertEquals(OutfitMenuViewModel.UiState.NO_OUTFITS, capturedStates.get(capturedStates.size() - 1));
    }

    @Test
    public void fetchAllData_withExistingOutfits_setsUiStateToHasOutfits() {
        // Dati di test: capi sufficienti
        Garment tShirt = new Garment();
        tShirt.setCategory("Parte superiore");
        Garment jeans = new Garment();
        jeans.setCategory("Parte inferiore");

        // Dati di test: un outfit esistente
        Outfit summerOutfit = new Outfit("Outfit Estivo", "Primavera", Arrays.asList(tShirt, jeans) );

        // Simula la risposta del GarmentRepository
        doAnswer(invocation -> {
            Callback<List<Garment>> callback = invocation.getArgument(0);
            callback.onSuccess(Arrays.asList(tShirt, jeans));
            return null;
        }).when(mockGarmentRepository).getGarments(any(Callback.class));

        // Simula la risposta dell'OutfitRepository con un outfit
        doAnswer(invocation -> {
            Callback<List<Outfit>> callback = invocation.getArgument(0);
            callback.onSuccess(Collections.singletonList(summerOutfit));
            return null;
        }).when(mockOutfitRepository).getOutfits(any(Callback.class));

        // Osserva i LiveData
        outfitMenuViewModel.getUiState().observeForever(uiStateObserver);
        outfitMenuViewModel.getOutfits().observeForever(outfitsObserver);

        // Azione
        outfitMenuViewModel.fetchOutfits();

        // Verifica lo stato UI
        verify(uiStateObserver, org.mockito.Mockito.atLeastOnce()).onChanged(uiStateCaptor.capture());
        List<OutfitMenuViewModel.UiState> capturedStates = uiStateCaptor.getAllValues();
        assertEquals(OutfitMenuViewModel.UiState.HAS_OUTFITS, capturedStates.get(capturedStates.size() - 1));

        // Verifica la lista di outfit
        verify(outfitsObserver, org.mockito.Mockito.atLeastOnce()).onChanged(outfitListCaptor.capture());
        List<Outfit> capturedOutfits = outfitListCaptor.getValue();
        assertNotNull(capturedOutfits);
        assertEquals(1, capturedOutfits.size());
        assertEquals("Outfit Estivo", capturedOutfits.get(0).getName());
    }
}