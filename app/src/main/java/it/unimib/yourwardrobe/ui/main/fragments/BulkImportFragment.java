package it.unimib.yourwardrobe.ui.main.fragments;

import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.adapter.BulkImportAdapter;
import it.unimib.yourwardrobe.ui.main.viewmodel.BulkImportViewModel;

@AndroidEntryPoint
public class BulkImportFragment extends Fragment {

    private BulkImportViewModel viewModel;
    private RecyclerView recyclerView;
    private BulkImportAdapter adapter;
    private MaterialButton saveButton;
    private ProgressBar progressBar;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMediaLauncher =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(10), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    processSelectedImages(uris);
                } else {
                    Navigation.findNavController(requireView()).navigateUp();
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickMultipleMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bulk_import, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.bulk_import_recycler_view);
        saveButton = view.findViewById(R.id.save_all_button);
        progressBar = view.findViewById(R.id.bulk_import_progress_bar);

        viewModel = new ViewModelProvider(this).get(BulkImportViewModel.class);

        setupRecyclerView();
        setupObservers();

        saveButton.setOnClickListener(v -> viewModel.saveAllGarments());
    }

    private void setupRecyclerView() {
        adapter = new BulkImportAdapter(
                viewModel::updateGarmentName,
                viewModel::updateGarmentCategory,
                viewModel::updateGarmentSeason,
                viewModel::updateGarmentColor,
                viewModel::removeGarment
        );
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getGarmentItems().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(new ArrayList<>(items));
            saveButton.setEnabled(items != null && !items.isEmpty());
        });

        viewModel.getIsProcessing().observe(getViewLifecycleOwner(), isProcessing -> {
            progressBar.setVisibility(isProcessing ? View.VISIBLE : View.GONE);
        });

        viewModel.getSaveSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Snackbar.make(requireView(), "Capi salvati con successo!", Snackbar.LENGTH_SHORT)
                        .show();
                Navigation.findNavController(requireView()).navigateUp();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(requireView(), error, Snackbar.LENGTH_LONG)
                        .show();
            }
        });

        viewModel.getRejectedCount().observe(getViewLifecycleOwner(), rejectedCount -> {
            if (rejectedCount != null && rejectedCount > 0) {
                String message = rejectedCount == 1
                        ? "1 foto è stata scartata perché non sembra essere un capo di abbigliamento"
                        : rejectedCount + " foto sono state scartate perché non sembrano essere capi di abbigliamento";

                Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
                        .setAction("OK", v -> {})
                        .show();
            }
        });
    }

    private void processSelectedImages(List<Uri> uris) {
        List<Bitmap> bitmaps = new ArrayList<>();

        for (Uri uri : uris) {
            try {
                ImageDecoder.Source source = ImageDecoder.createSource(
                        requireActivity().getContentResolver(), uri);
                Bitmap bitmap = ImageDecoder.decodeBitmap(source);
                bitmaps.add(bitmap);
            } catch (IOException e) {
                Snackbar.make(requireView(), "Errore nel caricare alcune immagini", Snackbar.LENGTH_LONG)
                        .show();
            }
        }

        if (!bitmaps.isEmpty()) {
            viewModel.processImages(bitmaps);
        } else {
            Navigation.findNavController(requireView()).navigateUp();
        }
    }
}