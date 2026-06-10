package com.movil.aplicacionqr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class QrGraphicOverlay extends View {

    private Rect qrBoundingBox;
    private int sourceWidth = 0;
    private int sourceHeight = 0;

    private final Paint rectPaint;
    private final Paint textPaint;
    private final Paint backgroundPaint;

    public QrGraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Pincel para el recuadro verde (Concepto AR)
        rectPaint = new Paint();
        rectPaint.setColor(Color.GREEN);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(8f);

        // Pincel para el texto descriptivo
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(45f);
        textPaint.setFakeBoldText(true);

        // Fondo para que el texto se vea mejor
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.argb(150, 0, 100, 0)); // Verde oscuro semitransparente
    }

    public void updateQrPosition(Rect boundingBox, int width, int height) {
        this.qrBoundingBox = boundingBox;
        this.sourceWidth = width;
        this.sourceHeight = height;
        postInvalidate();
    }

    public void clearOverlay() {
        qrBoundingBox = null;
        postInvalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (qrBoundingBox == null || sourceWidth == 0 || sourceHeight == 0) {
            return;
        }

        // Ajustar las coordenadas del QR al tamaño real de la pantalla del celular
        float scaleX = (float) getWidth() / sourceWidth;
        float scaleY = (float) getHeight() / sourceHeight;

        Rect box = new Rect(
                (int) (qrBoundingBox.left * scaleX),
                (int) (qrBoundingBox.top * scaleY),
                (int) (qrBoundingBox.right * scaleX),
                (int) (qrBoundingBox.bottom * scaleY)
        );

        // 1. Dibujar el recuadro verde sobre el QR
        canvas.drawRect(box, rectPaint);

        // 2. Dibujar etiqueta de Realidad Aumentada
        String label = "NIVEL: ACTIVO";
        float textWidth = textPaint.measureText(label);
        
        // Dibujar un pequeño fondo para el texto
        canvas.drawRect(
                box.left, 
                box.top - 60, 
                box.left + textWidth + 20, 
                box.top, 
                backgroundPaint
        );

        // Dibujar el texto justo encima del QR
        canvas.drawText(label, box.left + 10, box.top - 15, textPaint);
    }
}