package com.santi.organizame

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AyudaParserActividad : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ayuda_parser)

        findViewById<Button>(R.id.btnVolverAyuda).setOnClickListener {
            finish()
        }
    }
}
