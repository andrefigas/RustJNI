package com.devfigas.gamesample

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point

typealias SpriteData = Pair<Point, Array<Bitmap>>
fun Point.width() = this.x
fun Point.height() = this.y

enum class Sprite(
    private val resourceName : String,
    private val size : Int) {
    PLAYER("player.png", 6),
    ENEMY("enemy.png", 6);

    fun table(context: Context): SpriteData {
        return context.assets.open(resourceName).use { inputStream ->
            // Load the complete bitmap from the asset file
            val spriteSheet = BitmapFactory.decodeStream(inputStream)

            // Calculate the width and height of each sprite (1 row and 'size' columns)
            val spriteWidth = spriteSheet.width / size
            val spriteHeight = spriteSheet.height

            // Initialize the array of bitmaps
            Point(spriteHeight, spriteWidth) to Array(size) { index ->
                // Calculate the x position of the sprite in the row
                val x = index * spriteWidth
                // Extract and create a new bitmap for each sprite
                Bitmap.createBitmap(spriteSheet, x, 0, spriteWidth, spriteHeight)
            }
        }
    }

}
