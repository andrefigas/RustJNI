package com.example.flappybirdstandalone

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    companion object {
        const val BIRD_X = 80f
        const val BIRD_RADIUS = 15f
        const val PIPE_WIDTH = 52f
        const val PIPE_GAP = 150f
        const val GROUND_HEIGHT = 60f
    }

    private val wasm = (context as MainActivity)

    private var gameThread: Thread? = null
    @Volatile private var running = false
    @Volatile private var pendingInput = false
    private var scaleX = 1f
    private var scaleY = 1f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        val canvasW = wasm.gameGetCanvasWidth().toFloat()
        val canvasH = wasm.gameGetCanvasHeight().toFloat()
        scaleX = width.toFloat() / canvasW
        scaleY = height.toFloat() / canvasH

        running = true
        gameThread = Thread {
            var lastTime = System.nanoTime()
            while (running) {
                val now = System.nanoTime()
                val dt = ((now - lastTime) / 1_000_000_000.0).toFloat().coerceAtMost(0.05f)
                lastTime = now

                if (pendingInput) {
                    pendingInput = false
                    wasm.gameOnInput()
                }
                wasm.gameUpdate(dt.toDouble())

                val canvas = holder.lockCanvas() ?: continue
                try {
                    canvas.save()
                    canvas.scale(scaleX, scaleY)
                    drawGame(canvas)
                    canvas.restore()
                } finally {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }.also { it.start() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        val canvasW = wasm.gameGetCanvasWidth().toFloat()
        val canvasH = wasm.gameGetCanvasHeight().toFloat()
        scaleX = width.toFloat() / canvasW
        scaleY = height.toFloat() / canvasH
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        gameThread?.join()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            pendingInput = true
        }
        return true
    }

    private fun drawGame(canvas: Canvas) {
        val canvasW = wasm.gameGetCanvasWidth().toFloat()
        val canvasH = wasm.gameGetCanvasHeight().toFloat()
        val groundY = canvasH - GROUND_HEIGHT

        // Sky
        paint.color = 0xFF70C5CE.toInt()
        canvas.drawRect(0f, 0f, canvasW, canvasH, paint)

        // Pipes
        val count = wasm.gameGetPipeCount()
        paint.color = 0xFF73BF2E.toInt()
        for (i in 0 until count) {
            val px = wasm.gameGetPipeX(i).toFloat()
            val gapY = wasm.gameGetPipeGapY(i).toFloat()
            canvas.drawRect(px, 0f, px + PIPE_WIDTH, gapY, paint)
            val bottomTop = gapY + PIPE_GAP
            canvas.drawRect(px, bottomTop, px + PIPE_WIDTH, groundY, paint)
        }

        // Ground
        paint.color = 0xFF8B4513.toInt()
        canvas.drawRect(0f, groundY, canvasW, canvasH, paint)

        // Bird
        paint.color = 0xFFF5C842.toInt()
        canvas.drawCircle(BIRD_X, wasm.gameGetBirdY().toFloat(), BIRD_RADIUS, paint)
    }
}
