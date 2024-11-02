package com.devfigas.rustjni.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    //<RustJNI>
    // auto-generated code

    private external fun sayHello(): String

    init { System.loadLibrary("my_rust_lib") }

    //</RustJNI>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}