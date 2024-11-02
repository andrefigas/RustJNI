package com.devfigas.rustjni.sample;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    //<RustJNI>
    // auto-generated code

    private static native String sayHello();

    static { System.loadLibrary("my_rust_lib"); }

    //</RustJNI>

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

}