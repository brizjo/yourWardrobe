package it.unimib.yourwardrobe.ui.main.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.utils.Callback;

@RunWith(MockitoJUnitRunner.class)
public class AddGarmentViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Context mockContext;
    @Mock
    private Resources mockResources;
    @Mock
    private GarmentRepository mockGarmentRepository;

    @Mock
    private Bitmap mockBitmap;

    @Mock
    private Observer<Boolean> isButtonEnabledObserver;
    @Mock
    private Observer<List<String>> subcategoriesObserver;

    @Captor
    private ArgumentCaptor<Boolean> booleanCaptor;

    private AddGarmentViewModel addGarmentViewModel;

    @Before
    public void setUp() {
        when(mockContext.getResources()).thenReturn(mockResources);
        when(mockResources.getStringArray(any(Integer.class))).thenReturn(new String[0]);
        addGarmentViewModel = new AddGarmentViewModel(mockContext, mockGarmentRepository);

        doAnswer(invocation -> {
            Callback<Boolean> callback = invocation.getArgument(1);
            callback.onSuccess(true); // Simula che l'immagine sia sempre valida
            return null;
        }).when(mockGarmentRepository).validateGarment(any(Bitmap.class), any(Callback.class));
    }

    @Test
    public void isButtonEnabled_onInit_isFalse() {
        // Verifica che il pulsante sia disabilitato all'inizio

        Boolean initialState = addGarmentViewModel.isButtonEnabled().getValue();
        assertNotNull(initialState);
        assertFalse("Il pulsante deve essere disabilitato all'inizio", initialState);
    }

    @Test
    public void isButtonEnabled_whenAllFieldsAreValid_isTrue() {

        addGarmentViewModel.isButtonEnabled().observeForever(isButtonEnabledObserver);

        // compila tutti i campi richiesti
        addGarmentViewModel.setGarmentImage(mockBitmap);
        addGarmentViewModel.setGarmentName("Nuova Maglia");
        addGarmentViewModel.setSelectedCategory("Parte superiore");
        addGarmentViewModel.updateSelectedColors(Collections.singletonList("Blu"));
        addGarmentViewModel.updateSelectedStyles(Collections.singletonList("Casual"));
        addGarmentViewModel.setSelectedSeason("Primavera");
        addGarmentViewModel.setSelectedSubCategory("T-shirt");

        verify(isButtonEnabledObserver, org.mockito.Mockito.atLeastOnce()).onChanged(booleanCaptor.capture());
        assertTrue("Il pulsante deve essere abilitato quando tutti i campi sono validi", booleanCaptor.getValue());
    }

    @Test
    public void isButtonEnabled_whenOneFieldIsMissing_isFalse() {

        addGarmentViewModel.isButtonEnabled().observeForever(isButtonEnabledObserver);

        //compila tutti i campi tranne uno
        addGarmentViewModel.setGarmentImage(mockBitmap);
        addGarmentViewModel.setSelectedCategory("Parte superiore");
        addGarmentViewModel.updateSelectedColors(Collections.singletonList("Blu"));
        addGarmentViewModel.updateSelectedStyles(Collections.singletonList("Casual"));
        addGarmentViewModel.setSelectedSeason("Primavera");
        addGarmentViewModel.setSelectedSubCategory("T-shirt");

        verify(isButtonEnabledObserver, org.mockito.Mockito.atLeastOnce()).onChanged(booleanCaptor.capture());
        assertFalse("Il pulsante deve rimanere disabilitato se un campo è mancante", booleanCaptor.getValue());
    }

    @Test
    public void setSelectedCategory_updatesSubCategoryList() {

        String[] topSubcategories = new String[]{"T-shirt", "Camicia"};
        String topCategory = "Parte superiore";
        when(mockContext.getString(R.string.top_garment)).thenReturn(topCategory);
        when(mockResources.getStringArray(R.array.subcategories_top)).thenReturn(topSubcategories);

        addGarmentViewModel = new AddGarmentViewModel(mockContext, mockGarmentRepository);
        addGarmentViewModel.getAllSubCategories().observeForever(subcategoriesObserver);

        addGarmentViewModel.setSelectedCategory(topCategory);

        ArgumentCaptor<List<String>> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(subcategoriesObserver).onChanged(listCaptor.capture());
        List<String> result = listCaptor.getValue();

        assertNotNull(result);
        assertEquals("La lista di sottocategorie deve contenere 2 elementi", 2, result.size());
        assertEquals("Il primo elemento deve essere 'T-shirt'", "T-shirt", result.get(0));
    }
}