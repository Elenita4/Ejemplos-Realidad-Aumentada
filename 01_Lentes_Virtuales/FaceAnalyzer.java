package com.movil.realidadvirtual4;

import android.graphics.PointF;
import android.media.Image;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.PreviewView;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FaceAnalyzer implements ImageAnalysis.Analyzer {
    private final FaceDetector detector;
    private final FaceDetectionListener listener;
    private final PreviewView previewView;
    private final int imageWidth;
    private final int imageHeight;

    // Suavizado de coordenadas (opcional pero recomendado)
    private final Queue<PointF> leftEyeHistory = new LinkedList<>();
    private final Queue<PointF> rightEyeHistory = new LinkedList<>();
    private static final int SMOOTHING_WINDOW = 5;

    public interface FaceDetectionListener {
        void onEyesDetected(PointF leftEye, PointF rightEye);
    }

    public FaceAnalyzer(FaceDetectionListener listener, PreviewView previewView, int imageWidth, int imageHeight) {
        this.listener = listener;
        this.previewView = previewView;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                .build();
        detector = FaceDetection.getClient(options);
    }

    @ExperimentalGetImage
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            detector.process(image)
                    .addOnSuccessListener(faces -> processFaces(faces, imageProxy))
                    .addOnFailureListener(e -> imageProxy.close());
        } else {
            imageProxy.close();
        }
    }

    private void processFaces(List<Face> faces, ImageProxy imageProxy) {
        if (!faces.isEmpty()) {
            Face face = faces.get(0);
            FaceLandmark leftEyeLandmark = face.getLandmark(FaceLandmark.LEFT_EYE);
            FaceLandmark rightEyeLandmark = face.getLandmark(FaceLandmark.RIGHT_EYE);

            if (leftEyeLandmark != null && rightEyeLandmark != null) {
                PointF leftEyeRaw = leftEyeLandmark.getPosition();
                PointF rightEyeRaw = rightEyeLandmark.getPosition();

                // 1. Transformar coordenadas de imagen (640x480) a pantalla
                PointF leftEyeScreen = transformToScreenCoordinates(leftEyeRaw);
                PointF rightEyeScreen = transformToScreenCoordinates(rightEyeRaw);

                // 2. Suavizar para evitar temblor
                PointF leftEyeSmooth = smooth(leftEyeHistory, leftEyeScreen);
                PointF rightEyeSmooth = smooth(rightEyeHistory, rightEyeScreen);

                // 3. Enviar al listener (el overlay dibujará los lentes)
                listener.onEyesDetected(leftEyeSmooth, rightEyeSmooth);
            }
        }
        imageProxy.close();
    }

    /**
     * Convierte coordenadas del espacio de la imagen de análisis (imageWidth x imageHeight)
     * al espacio de la pantalla (PreviewView), aplicando escala, centrado y espejo horizontal.
     */
    private PointF transformToScreenCoordinates(PointF point) {
        if (previewView.getWidth() == 0 || previewView.getHeight() == 0) {
            return point; // todavía no hay medidas
        }

        float previewW = previewView.getWidth();
        float previewH = previewView.getHeight();

        float previewAspect = previewW / previewH;
        float imageAspect = (float) imageWidth / imageHeight;

        float scaleX, scaleY, offsetX, offsetY;

        if (previewAspect > imageAspect) {
            // La pantalla es más ancha que la imagen: la imagen se escala para ajustar la altura
            scaleY = previewH / imageHeight;
            scaleX = scaleY;
            offsetX = (previewW - imageWidth * scaleX) / 2f;
            offsetY = 0;
        } else {
            // La pantalla es más alta que la imagen: la imagen se escala para ajustar el ancho
            scaleX = previewW / imageWidth;
            scaleY = scaleX;
            offsetX = 0;
            offsetY = (previewH - imageHeight * scaleY) / 2f;
        }

        float x = point.x * scaleX + offsetX;
        float y = point.y * scaleY + offsetY;

        // Espejo horizontal (porque la cámara frontal muestra la imagen reflejada)
        x = previewW - x;

        // Limitar al área visible (por seguridad)
        x = Math.max(0, Math.min(previewW, x));
        y = Math.max(0, Math.min(previewH, y));

        return new PointF(x, y);
    }

    private PointF smooth(Queue<PointF> history, PointF newPoint) {
        history.add(newPoint);
        while (history.size() > SMOOTHING_WINDOW) {
            history.poll();
        }
        float sumX = 0, sumY = 0;
        for (PointF p : history) {
            sumX += p.x;
            sumY += p.y;
        }
        return new PointF(sumX / history.size(), sumY / history.size());
    }
}

