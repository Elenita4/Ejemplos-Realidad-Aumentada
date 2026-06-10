package com.movil.realidadvirtual4;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class GraphicOverlay extends View {
    private Paint framePaint;      // marco
    private Paint bridgePaint;     // puente
    private PointF leftEye;
    private PointF rightEye;
    private float eyeDistance;

    public GraphicOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Marco de las gafas (solo borde)
        framePaint = new Paint();
        framePaint.setColor(Color.BLACK);
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(12f);
        framePaint.setAntiAlias(true);

        // Puente (conecta ambos lentes)
        bridgePaint = new Paint();
        bridgePaint.setColor(Color.BLACK);
        bridgePaint.setStyle(Paint.Style.STROKE);
        bridgePaint.setStrokeWidth(10f);
        bridgePaint.setAntiAlias(true);
    }

    public void updateEyes(PointF leftEye, PointF rightEye) {
        this.leftEye = leftEye;
        this.rightEye = rightEye;
        if (leftEye != null && rightEye != null) {
            eyeDistance = (float) Math.hypot(rightEye.x - leftEye.x, rightEye.y - leftEye.y);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (leftEye == null || rightEye == null || eyeDistance <= 0) return;

        // Centro entre los ojos y ángulo de inclinación
        float centerX = (leftEye.x + rightEye.x) / 2f;
        float centerY = (leftEye.y + rightEye.y) / 2f;
        double angleRad = Math.atan2(rightEye.y - leftEye.y, rightEye.x - leftEye.x);
        float angleDeg = (float) Math.toDegrees(angleRad);

        canvas.save();
        canvas.translate(centerX, centerY);
        canvas.rotate(angleDeg);

        // Tamaño de los lentes (solo el marco)
        float lensWidth = eyeDistance * 1.4f;     // ancho de cada lente
        float lensHeight = lensWidth * 0.8f;      // alto
        float bridgeWidth = eyeDistance * 0.3f;   // ancho del puente
        float gapBetweenLenses = bridgeWidth;

        float halfTotalWidth = (lensWidth * 2 + gapBetweenLenses) / 2f;
        float leftLensLeft = -halfTotalWidth;
        float rightLensLeft = -halfTotalWidth + lensWidth + gapBetweenLenses;

        // Ajuste vertical (subir/bajar las gafas)
        float verticalOffset = -lensHeight * 0.15f;

        // Lente izquierdo (solo marco)
        RectF leftLensRect = new RectF(leftLensLeft, verticalOffset - lensHeight/2,
                leftLensLeft + lensWidth, verticalOffset + lensHeight/2);
        canvas.drawRoundRect(leftLensRect, 30f, 30f, framePaint);

        // Lente derecho (solo marco)
        RectF rightLensRect = new RectF(rightLensLeft, verticalOffset - lensHeight/2,
                rightLensLeft + lensWidth, verticalOffset + lensHeight/2);
        canvas.drawRoundRect(rightLensRect, 30f, 30f, framePaint);

        // Puente
        float bridgeStartX = leftLensLeft + lensWidth;
        float bridgeEndX = rightLensLeft;
        canvas.drawLine(bridgeStartX, verticalOffset, bridgeEndX, verticalOffset, bridgePaint);

        canvas.restore();
    }
}