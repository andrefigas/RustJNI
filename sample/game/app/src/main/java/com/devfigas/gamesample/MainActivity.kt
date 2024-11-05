package com.devfigas.gamesample
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), GameListener {

    private lateinit var fab : FloatingActionButton
    private lateinit var gameSurfaceView : GameSurfaceView
    private lateinit var swHitBox : SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fab = findViewById(R.id.fab)
        gameSurfaceView = findViewById(R.id.gameSurfaceView)
        swHitBox = findViewById(R.id.swHitBox)
        gameSurfaceView.gameListener = this
        swHitBox.setOnCheckedChangeListener { _, isChecked ->
            gameSurfaceView.hitBox = isChecked
        }
        enableStartButton()
    }

    private fun enableStartButton(){
        fab.setImageResource(R.drawable.baseline_play_arrow_24)
        fab.setOnClickListener {
            enableJumpButton()
            NativeLib.actionStart()
        }
    }

    private fun enableJumpButton(){
        fab.setImageResource(R.drawable.baseline_file_upload_24)
        fab.setOnClickListener {
            NativeLib.actionJump()
        }
    }

    override fun onGameEnd() {
        Toast.makeText(this, "Game Over", Toast.LENGTH_SHORT).show()
        enableStartButton()
    }

}
