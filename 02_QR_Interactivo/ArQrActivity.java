package com.movil.aplicacionqr;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.util.Size;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ArQrActivity extends AppCompatActivity {

    private PreviewView previewView;
    private QrGraphicOverlay qrGraphicOverlay;
    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_qr);

        previewView = findViewById(R.id.previewView);
        qrGraphicOverlay = findViewById(R.id.qrGraphicOverlay);
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Configurar ML Kit para que solo busque códigos QR (optimiza rendimiento)
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);

        // Verificar permisos de cámara antes de iniciar
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // 1. Preview de la cámara
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // 2. Analizador de imagen para ML Kit
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
                    @Override
                    @OptIn(markerClass = ExperimentalGetImage.class)
                    public void analyze(@NonNull ImageProxy imageProxy) {
                        Image mediaImage = imageProxy.getImage();
                        if (mediaImage != null) {
                            InputImage image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.getImageInfo().getRotationDegrees()
                            );

                            int rotation = imageProxy.getImageInfo().getRotationDegrees();
                            int width = (rotation == 90 || rotation == 270) ? imageProxy.getHeight() : imageProxy.getWidth();
                            int height = (rotation == 90 || rotation == 270) ? imageProxy.getWidth() : imageProxy.getHeight();

                            barcodeScanner.process(image)
                                    .addOnSuccessListener(barcodes -> processBarcodes(barcodes, width, height))
                                    .addOnFailureListener(e -> e.printStackTrace())
                                    .addOnCompleteListener(task -> imageProxy.close());
                        } else {
                            imageProxy.close();
                        }
                    }
                });

                // Seleccionar cámara trasera
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Desvincular casos de uso previos y vincular los nuevos
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (Exception e) {
                Toast.makeText(this, "Error al iniciar la cámara: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void processBarcodes(List<Barcode> barcodes, int width, int height) {
        if (barcodes.isEmpty()) {
            qrGraphicOverlay.clearOverlay();
            return;
        }

        boolean foundTarget = false;

        for (Barcode barcode : barcodes) {
            String rawValue = barcode.getRawValue();

            // Log para ver en Android Studio qué lee el celu
            android.util.Log.d("QR_SCAN", "Leído: " + rawValue);

            // Filtro para el QR específico (puedes usar "HOLA" o "11")
            if (rawValue != null && (rawValue.equalsIgnoreCase("11") || rawValue.equalsIgnoreCase("HOLA"))) {
                Rect boundingBox = barcode.getBoundingBox();
                if (boundingBox != null) {
                    qrGraphicOverlay.updateQrPosition(boundingBox, width, height);
                    foundTarget = true;
                    break;
                }
            }
        }

        if (!foundTarget) {
            qrGraphicOverlay.clearOverlay();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        barcodeScanner.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Permiso de cámara denegado.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}