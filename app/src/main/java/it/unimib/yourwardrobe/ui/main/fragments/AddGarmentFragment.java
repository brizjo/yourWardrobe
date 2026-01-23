package it.unimib.yourwardrobe.ui.main.fragments;

import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.main.components.CardMenu;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddGarmentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddGarmentFragment extends Fragment {

    private ImageView addGarmentImageView;

    // 1. Launcher per la richiesta dei permessi
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permesso concesso, lancia la fotocamera
                    launchCamera();
                } else {
                    // Permesso negato, informa l'utente
                    Toast.makeText(getContext(), "Permesso fotocamera necessario per scattare una foto", Toast.LENGTH_SHORT).show();
                }
            });

    // 2. Launcher per ricevere il risultato dalla fotocamera
    private final ActivityResultLauncher<Void> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    // Foto scattata, mostrala nell'ImageView
                    addGarmentImageView.setImageBitmap(bitmap);
                    // Cambia lo scaleType per riempire l'area
                    addGarmentImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            });

    public AddGarmentFragment() {
        // Required empty public constructor
    }


    public static AddGarmentFragment newInstance(String param1, String param2) {
        AddGarmentFragment fragment = new AddGarmentFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_garment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addGarmentImageView = view.findViewById(R.id.addGarmentImage);
        addGarmentImageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_add_photo));
        // 3. Imposta il listener per il click sull'immagine
        addGarmentImageView.setOnClickListener(v -> {
            // Controlla se il permesso è già stato concesso
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                // Se sì, lancia la fotocamera
                launchCamera();
            } else {
                // Altrimenti, richiedi il permesso
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });
    }
    private void launchCamera() {
        takePictureLauncher.launch(null);
    }
}