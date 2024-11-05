package com.devfigas.gamesample

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.concurrent.thread

class GameSurfaceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    companion object{
        private val hitBoxPaint = Paint().apply {
            color = Color.argb(128, 255, 0, 0)
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }
    }

    private val mainHandler: Handler = Handler(Looper.getMainLooper())

    private var playerSprites: Array<Bitmap>
    private var enemySprites: Array<Bitmap>

    private var updateThread: Thread? = null
    private var isRunning = false

    var gameListener: GameListener? = null
    var hitBox : Boolean = false

    init {
        holder.addCallback(this)
        playerSprites = Sprite.PLAYER.table(context).let { (dim, sprites) ->
            NativeLib.loadPlayerSpriteTable(sprites.size, dim.height(), dim.width())
            sprites
        }

        enemySprites = Sprite.ENEMY.table(context).let { (dim, sprites) ->
            NativeLib.loadEnemySpriteTable(sprites.size, dim.height(), dim.width())
            sprites
        }

    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isRunning = true
        updateThread = thread {
            loadCanvas()
            while (isRunning) {
                if (NativeLib.isDead()) {
                    onGameEnd()
                }

                val canvas = holder.lockCanvas()
                canvas.drawColor(Color.WHITE)

                if (canvas != null) {
                    drawPlayer(canvas)
                    drawEnemy(canvas)
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }
    }

    private fun loadCanvas(){
        val canvas = holder.lockCanvas()
        NativeLib.loadCanvas(canvas.width, canvas.height)
        canvas.drawColor(Color.WHITE)
        holder.unlockCanvasAndPost(canvas)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRunning = false
        updateThread?.join()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    private fun drawPlayer(canvas: Canvas) {
        val sprite = playerSprites[NativeLib.getPlayerSprite()]
        val left = NativeLib.getPlayerX()
        val top = NativeLib.getPlayerY()
        drawHitBox(canvas, left, top, sprite)
        canvas.drawBitmap(sprite, left, top, null)
    }

    private fun drawEnemy(canvas: Canvas) {
        val sprite = enemySprites[NativeLib.getEnemySprite()]
        val left = NativeLib.getEnemyX()
        val top = NativeLib.getEnemyY()
        drawHitBox(canvas, left, top, sprite)
        canvas.drawBitmap(sprite, left, top, null)
    }

    private fun onGameEnd() {
        mainHandler.post {
            gameListener?.onGameEnd()
        }
    }

    private fun drawHitBox(canvas: Canvas, left: Float, top: Float, bitmap: Bitmap) {
        if(!hitBox) return
        canvas.drawRect(left, top, left + bitmap.width, top + bitmap.height, hitBoxPaint)
    }

}
