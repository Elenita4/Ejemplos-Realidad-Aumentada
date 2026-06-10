package com.movil.sistemasolarar

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.node.ModelNode

class MainActivity : AppCompatActivity() {

    private lateinit var arSceneView: ARSceneView
    private lateinit var txtInfo: TextView
    private lateinit var modelLoader: ModelLoader

    private lateinit var modelNode: ModelNode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arSceneView = findViewById(R.id.arSceneView)

        // Configuración de iluminación
        arSceneView.planeRenderer.isVisible = true

        arSceneView.lightEstimator?.environmentalHdrReflections = true
        arSceneView.lightEstimator?.environmentalHdrMainLightIntensity = true
        arSceneView.lightEstimator?.environmentalHdrSphericalHarmonics = true

        arSceneView.mainLightNode?.isVisible = true

        // Descomenta si Android Studio reconoce estas propiedades

        arSceneView.mainLightNode?.intensity = 100000f
        //arSceneView.mainLightNode?.color = android.graphics.Color.WHITE

        txtInfo = findViewById(R.id.txtInfo)

        val btnMas = findViewById<Button>(R.id.btnMas)
        val btnMenos = findViewById<Button>(R.id.btnMenos)
        val btnIzq = findViewById<Button>(R.id.btnIzq)
        val btnDer = findViewById<Button>(R.id.btnDer)

        modelLoader = ModelLoader(
            arSceneView.engine,
            this
        )

        arSceneView.onSessionCreated = {
            txtInfo.text = "📷 ARCore iniciado"
        }

        arSceneView.onSessionFailed = {
            txtInfo.text = "❌ Error AR: ${it.message}"
        }

        arSceneView.onSessionUpdated = { session, _ ->

            val planeDetected =
                session.getAllTrackables(Plane::class.java)
                    .any { it.trackingState == TrackingState.TRACKING }

            if (planeDetected) {
                txtInfo.text = """
🤖 CARL

Robot Explorador XR-01

🔋 Batería: 87%
📡 Estado: Activo
⚙️ Sistema: Operativo
🚀 Misión: Exploración AR

Utiliza los botones para:
➕ Ampliar
➖ Reducir
⟲ Girar Izquierda
⟳ Girar Derecha
""".trimIndent()
            } else {
                txtInfo.text = "🔍 Busca una superficie plana"
            }
        }

        try {

            val modelInstance =
                modelLoader.createModelInstance("carl.glb")

            modelNode = ModelNode(
                modelInstance = modelInstance,
                scaleToUnits = 0.002f
            )

            // Sombras
            modelNode.isShadowCaster = true
            modelNode.isShadowReceiver = true

            arSceneView.addChildNode(modelNode)

            btnMas.setOnClickListener {
                modelNode.scale *= 1.2f
            }

            btnMenos.setOnClickListener {
                modelNode.scale *= 0.8f
            }

            btnIzq.setOnClickListener {
                modelNode.rotation =
                    modelNode.rotation.copy(
                        y = modelNode.rotation.y - 10f
                    )
            }

            btnDer.setOnClickListener {
                modelNode.rotation =
                    modelNode.rotation.copy(
                        y = modelNode.rotation.y + 10f
                    )
            }

        } catch (e: Exception) {

            txtInfo.text = "❌ ${e.message}"
        }
    }
}