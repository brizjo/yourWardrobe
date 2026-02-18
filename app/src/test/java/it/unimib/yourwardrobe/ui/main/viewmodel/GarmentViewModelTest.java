package it.unimib.yourwardrobe.ui.main.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.domain.repository.GarmentRepository;
import it.unimib.yourwardrobe.utils.Callback;
import it.unimib.yourwardrobe.utils.ImageValidationState;

@RunWith(MockitoJUnitRunner.class)
public class GarmentViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Application mockApplication;

    @Mock
    private GarmentRepository mockGarmentRepository;

    @Mock
    private Observer<Boolean> booleanObserver;

    @Mock
    private Observer<ImageValidationState> imageStateObserver;

    @Mock
    private Bitmap mockBitmap;

    @Mock
    private Resources mockResources;

    private GarmentViewModel garmentViewModel;
    private MockedStatic<Log> mockedLog;

    @Before
    public void setUp() {
        mockedLog = Mockito.mockStatic(Log.class);
        when(mockApplication.getResources()).thenReturn(mockResources);
        when(mockResources.getStringArray(anyInt())).thenReturn(new String[0]);
        when(mockApplication.getString(anyInt())).thenReturn("Fake String");
        garmentViewModel = new GarmentViewModel(mockApplication, mockGarmentRepository);
    }

    @After
    public void tearDown() {
        mockedLog.close();
    }

    @Test
    public void deleteGarment_whenRepositorySucceeds_isDeletedLiveDataIsTrue() {
        Garment fakeGarment = new Garment();
        fakeGarment.setName("T-shirt");
        fakeGarment.setCategory("Parte superiore");
        fakeGarment.setColor(Collections.singletonList("Rosso"));
        garmentViewModel.setGarment(fakeGarment);

        doAnswer(invocation -> {
            Callback<Boolean> callback = invocation.getArgument(1); // Il callback è il secondo argomento
            callback.onSuccess(true);
            return null;
        }).when(mockGarmentRepository).deleteGarment(any(Garment.class), any(Callback.class));

        garmentViewModel.getIsDeleted().observeForever(booleanObserver);

        garmentViewModel.deleteGarment();

        verify(mockGarmentRepository).deleteGarment(eq(fakeGarment), any(Callback.class));
        verify(booleanObserver).onChanged(true);
    }

    @Test
    public void updateGarment_whenNoChangesMade_exitsEditModeWithoutCallingRepository() {
        Garment fakeGarment = new Garment();
        fakeGarment.setName("T-shirt");
        fakeGarment.setCategory("Parte superiore");
        fakeGarment.setColor(Collections.singletonList("Rosso"));
        garmentViewModel.setGarment(fakeGarment);
        garmentViewModel.enterEditMode();

        garmentViewModel.getIsEditMode().observeForever(booleanObserver);

        garmentViewModel.updateGarment();

        verify(mockGarmentRepository, never()).updateGarment(any(), any());
        verify(mockGarmentRepository, never()).updateGarmentImage(any(), any(), any());

        ArgumentCaptor<Boolean> captor = ArgumentCaptor.forClass(Boolean.class);
        verify(booleanObserver, times(2)).onChanged(captor.capture()); // Cattura sia true che false
        assertFalse("isEditMode deve essere false dopo l'aggiornamento", captor.getValue());
    }

    @Test
    public void onNewImageSelected_whenImageIsValid_setsValidationStateToValid() {
        doAnswer(invocation -> {
            Callback<Boolean> callback = invocation.getArgument(1);
            callback.onSuccess(true); // Immagine valida
            return null;
        }).when(mockGarmentRepository).validateGarment(any(Bitmap.class), any(Callback.class));

        garmentViewModel.getImageValidationState().observeForever(imageStateObserver);

        garmentViewModel.onNewImageSelected(mockBitmap);

        ArgumentCaptor<ImageValidationState> captor = ArgumentCaptor.forClass(ImageValidationState.class);

        verify(imageStateObserver, atLeastOnce()).onChanged(captor.capture());
        assertEquals("Lo stato di validazione dell'immagine dovrebbe essere VALID", ImageValidationState.VALID, captor.getValue());
        assertFalse(garmentViewModel.getIsLoading().getValue());
    }

    @Test
    public void cancelChanges_restoresOriginalGarmentState() {
        Garment originalGarment = new Garment();
        originalGarment.setName("Original Name");
        originalGarment.setCategory("Parte superiore");
        originalGarment.setColor(Collections.singletonList("Rosso"));
        garmentViewModel.setGarment(originalGarment);
        garmentViewModel.setGarmentName("New Name");
        assertNotEquals("Il nome del capo dovrebbe essere stato modificato", "Original Name", garmentViewModel.getGarment().getValue().getName());

        Observer<Garment> garmentObserver = mock(Observer.class);
        garmentViewModel.getGarment().observeForever(garmentObserver);

        garmentViewModel.cancelChanges();

        ArgumentCaptor<Garment> captor = ArgumentCaptor.forClass(Garment.class);
        verify(garmentObserver, atLeastOnce()).onChanged(captor.capture());


        assertEquals("Il nome del capo dovrebbe essere stato ripristinato a quello originale", "Original Name", captor.getValue().getName());
        assertFalse("isEditMode deve essere false dopo aver annullato", garmentViewModel.getIsEditMode().getValue());
    }
}
