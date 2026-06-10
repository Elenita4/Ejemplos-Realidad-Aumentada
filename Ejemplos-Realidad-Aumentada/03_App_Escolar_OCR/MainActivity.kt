package com.movil.turismoarbolivia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val btnIngresar =
            findViewById<Button>(R.id.btnIngresar)

        btnIngresar.setOnClickListener {

            val intent =
                Intent(
                    this,
                    TurismoActivity::class.java
                )

            startActivity(intent)
        }

        val btnSistema =
            findViewById<Button>(R.id.btnSistema)

        btnSistema.setOnClickListener {

            val intent =
                Intent(
                    this,
                    SistemaSolar::class.java
                )

            startActivity(intent)
        }
    }
}
