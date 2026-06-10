package com.movil.turismoarbolivia

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage

class TurismoActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var imgLugar: ImageView
    private lateinit var txtTitulo: TextView
    private lateinit var txtInfo: TextView

    private val recognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val cameraExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_turismo)

        previewView = findViewById(R.id.previewView)
        imgLugar = findViewById(R.id.imgLugar)
        txtTitulo = findViewById(R.id.txtTitulo)
        txtInfo = findViewById(R.id.txtInfo)

        startCamera()
        loadLugar("uyuni")
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->

                val mediaImage = imageProxy.image

                if (mediaImage != null) {

                    val image = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )

                    recognizer.process(image)
                        .addOnSuccessListener { result ->

                            val text = result.text.lowercase()

                            when {
                                text.contains("salar") || text.contains("uyuni") ->
                                    runOnUiThread { loadLugar("uyuni") }

                                text.contains("titicaca") ->
                                    runOnUiThread { loadLugar("titicaca") }

                                text.contains("madidi") ->
                                    runOnUiThread { loadLugar("madidi") }

                                text.contains("potosi") ->
                                    runOnUiThread { loadLugar("potosi") }
                            }
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalysis
            )

        }, ContextCompat.getMainExecutor(this))
    }

    private fun loadLugar(lugar: String) {

        when (lugar) {

            "uyuni" -> {
                imgLugar.setImageResource(R.drawable.uyuni)
                txtTitulo.text = "🏜 Salar de Uyuni"
                txtInfo.text = "El desierto de sal más grande del mundo."
            }

            "titicaca" -> {
                imgLugar.setImageResource(R.drawable.titicaca)
                txtTitulo.text = "🌊 Lago Titicaca"
                txtInfo.text = "El lago navegable más alto del mundo."
            }

            "madidi" -> {
                imgLugar.setImageResource(R.drawable.madidi)
                txtTitulo.text = "🌿 Madidi"
                txtInfo.text = "Una de las reservas más biodiversas del planeta."
            }

            "potosi" -> {
                imgLugar.setImageResource(R.drawable.potosi)
                txtTitulo.text = "⛏ Potosí"
                txtInfo.text = "Ciudad histórica minera de Bolivia."
            }
        }
    }
}