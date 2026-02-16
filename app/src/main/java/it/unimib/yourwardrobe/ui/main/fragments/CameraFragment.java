package it.unimib.yourwardrobe.ui.main.fragments;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.databinding.FragmentCameraBinding;
import it.unimib.yourwardrobe.ui.main.viewmodel.AddGarmentViewModel;
import it.unimib.yourwardrobe.utils.ImageProcessor;

@AndroidEntryPoint
public class CameraFragment extends Fragment {

    private FragmentCameraBinding binding;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private ImageProcessor imageProcessor;
    private AddGarmentViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCameraBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageProcessor = new ImageProcessor();
        // Usiamo il ViewModel dell'Activity per passare l'immagine a AddGarmentFragment
        viewModel = new ViewModelProvider(requireActivity()).get(AddGarmentViewModel.class);
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 10);
        }

        binding.btnCapture.setOnClickListener(v -> takePhoto());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Nascondi le barre dell'app
        View topBar = requireActivity().findViewById(R.id.top_bar);
        View bottomNav = requireActivity().findViewById(R.id.bottom_nav);
        if (topBar != null) topBar.setVisibility(View.GONE);
        if (bottomNav != null) bottomNav.setVisibility(View.GONE);

        // FIX: Nasconde la barra di navigazione di Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.view.WindowInsetsController controller = requireActivity().getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(android.view.WindowInsets.Type.statusBars() | android.view.WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            // Versioni vecchie
            requireActivity().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Show App Bars
        View topBar = requireActivity().findViewById(R.id.top_bar);
        View bottomNav = requireActivity().findViewById(R.id.bottom_nav);
        if (topBar != null) topBar.setVisibility(View.VISIBLE);
        if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);

        // Show Android System Bars
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            android.view.WindowInsetsController controller = requireActivity().getWindow().getInsetsController();
            if (controller != null) {
                controller.show(android.view.WindowInsets.Type.statusBars() | android.view.WindowInsets.Type.navigationBars());
            }
        } else {
            requireActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    private void startCamera() {
        // CORRETTO: Ora usa l'import di Guava standard
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        // 1. Scatto della foto
        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        // Convertiamo ImageProxy in Bitmap e ruotiamolo correttamente
                        Bitmap originalBitmap = imageProxyToBitmap(image);
                        image.close();

                        // Mostriamo il "Freeze Frame" (l'immagine bloccata sopra la camera)
                        requireActivity().runOnUiThread(() -> {
                            binding.imgFreezeFrame.setImageBitmap(originalBitmap);
                            binding.imgFreezeFrame.setVisibility(View.VISIBLE);
                            binding.processingOverlay.setVisibility(View.VISIBLE);
                        });

                        // 2. Avviamo lo scontornamento AI (ML Kit)
                        imageProcessor.removeBackground(originalBitmap).addOnSuccessListener(cutoutBitmap -> {
                            requireActivity().runOnUiThread(() -> {
                                binding.processingOverlay.setVisibility(View.GONE);
                                // 3. Eseguiamo l'effetto scenico della linea di scansione
                                runScanAnimation(originalBitmap, cutoutBitmap);
                            });
                        }).addOnFailureListener(e -> {
                            // Fallback: se l'AI fallisce, passiamo l'originale
                            returnResult(originalBitmap);
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraFragment", "Errore cattura: " + exception.getMessage());
                    }
                });
    }

    private void runScanAnimation(Bitmap original, Bitmap cutout) {
        binding.scanLine.setVisibility(View.VISIBLE);

        ObjectAnimator animator = ObjectAnimator.ofFloat(binding.scanLine, "translationY", 0f, binding.getRoot().getHeight());
        animator.setDuration(1200);

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // Passa il bitmap scontornato al ViewModel e torna al fragment di aggiunta
                viewModel.setGarmentImage(cutout);
                Navigation.findNavController(requireView()).popBackStack();
            }
        });

        animator.start();
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        Matrix matrix = new Matrix();
        matrix.postRotate(image.getImageInfo().getRotationDegrees());
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cameraExecutor.shutdown();
        binding = null;
    }

    private void returnResult(Bitmap resultBitmap) {
        // Usiamo postValue per essere sicuri di essere thread-safe
        // o forziamo il thread principale per la navigazione
        requireActivity().runOnUiThread(() -> {
            if (viewModel != null) {
                viewModel.setGarmentImage(resultBitmap);
            }
            // Navigazione DEVE avvenire sul thread principale
            Navigation.findNavController(requireView()).popBackStack();
        });
    }
}