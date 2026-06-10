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

class SistemaSolar : AppCompatActivity() {

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
        loadLugar("sol")
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

                                text.contains("sol") ->
                                    runOnUiThread { loadLugar("sol") }

                                text.contains("luna") ->
                                    runOnUiThread { loadLugar("luna") }

                                text.contains("mercurio") ->
                                    runOnUiThread { loadLugar("mercurio") }

                                text.contains("venus") ->
                                    runOnUiThread { loadLugar("venus") }

                                text.contains("tierra") ->
                                    runOnUiThread { loadLugar("tierra") }

                                text.contains("marte") ->
                                    runOnUiThread { loadLugar("marte") }

                                text.contains("jupiter") ->
                                    runOnUiThread { loadLugar("jupiter") }

                                text.contains("saturno") ->
                                    runOnUiThread { loadLugar("saturno") }

                                text.contains("urano") ->
                                    runOnUiThread { loadLugar("urano") }

                                text.contains("neptuno") ->
                                    runOnUiThread { loadLugar("neptuno") }
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

            "sol" -> {
                imgLugar.setImageResource(R.drawable.sol)
                txtTitulo.text = "☀️ Sol"
                txtInfo.text = "Estrella central del sistema solar."
            }
            "luna" -> {
                imgLugar.setImageResource(R.drawable.luna)
                txtTitulo.text = "🌙 Luna"
                txtInfo.text = "Satélite natural de la Tierra."
            }

            "mercurio" -> {
                imgLugar.setImageResource(R.drawable.mercurio)
                txtTitulo.text = "☿ Mercurio"
                txtInfo.text = "Planeta más cercano al Sol."
            }

            "venus" -> {
                imgLugar.setImageResource(R.drawable.venus)
                txtTitulo.text = "♀ Venus"
                txtInfo.text = "Planeta más caliente del sistema solar."
            }

            "tierra" -> {
                imgLugar.setImageResource(R.drawable.tierra)
                txtTitulo.text = "🌍 Tierra"
                txtInfo.text = "Nuestro planeta, único con vida conocida."
            }

            "marte" -> {
                imgLugar.setImageResource(R.drawable.marte)
                txtTitulo.text = "♂ Marte"
                txtInfo.text = "Planeta rojo, posible futuro hogar humano."
            }

            "jupiter" -> {
                imgLugar.setImageResource(R.drawable.jupiter)
                txtTitulo.text = "🪐 Júpiter"
                txtInfo.text = "El planeta más grande del sistema solar."
            }

            "saturno" -> {
                imgLugar.setImageResource(R.drawable.saturno)
                txtTitulo.text = "🪐 Saturno"
                txtInfo.text = "Famoso por sus anillos."
            }

            "urano" -> {
                imgLugar.setImageResource(R.drawable.urano)
                txtTitulo.text = "🔵 Urano"
                txtInfo.text = "Planeta helado con rotación inclinada."
            }

            "neptuno" -> {
                imgLugar.setImageResource(R.drawable.neptuno)
                txtTitulo.text = "🌊 Neptuno"
                txtInfo.text = "El planeta más lejano del sistema solar."
            }
        }
    }
}