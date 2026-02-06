package com.devfigas.rustjni.restapi

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    //<RustJNI>
    // auto-generated code
            
    private external fun getRandomDog(): String

    private external fun listBreeds(): String

    private external fun getBreedImage(breed: String): String
            
    init { System.loadLibrary("rust_rest_api") }
            
    //</RustJNI>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvResult = findViewById<TextView>(R.id.tvResult)
        val etBreed = findViewById<EditText>(R.id.etBreed)
        val btnRandomDog = findViewById<Button>(R.id.btnRandomDog)
        val btnListBreeds = findViewById<Button>(R.id.btnListBreeds)
        val btnBreedImage = findViewById<Button>(R.id.btnBreedImage)

        btnRandomDog.setOnClickListener {
            callRust(tvResult) { getRandomDog() }
        }

        btnListBreeds.setOnClickListener {
            callRust(tvResult) { listBreeds() }
        }

        btnBreedImage.setOnClickListener {
            val breed = etBreed.text.toString().trim()
            if (breed.isEmpty()) {
                tvResult.text = "Please enter a breed name"
                return@setOnClickListener
            }
            callRust(tvResult) { getBreedImage(breed) }
        }
    }

    private fun callRust(tvResult: TextView, block: () -> String) {
        tvResult.text = getString(R.string.loading)
        Thread {
            val result = block()
            runOnUiThread {
                tvResult.text = result
            }
        }.start()
    }
}
