package it.unimib.yourwardrobe.ui.main.fragments;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import androidx.test.filters.SmallTest;


import it.unimib.yourwardrobe.ui.main.viewmodel.ClothesViewModel;

import static org.mockito.Mockito.verify;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class ClothesFragmentTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private ClothesViewModel mockViewModel;


    private ClothesFragment clothesFragmentSpy;


    @Before
    public void setUp() {
        ClothesFragment realFragment = new ClothesFragment();
        clothesFragmentSpy = Mockito.spy(realFragment);
        clothesFragmentSpy.setViewModel(mockViewModel);
    }

    @Test
    public void onResume_shouldFetchGarments() {
        clothesFragmentSpy.onResume();

        verify(mockViewModel).fetchGarments();
    }
}
