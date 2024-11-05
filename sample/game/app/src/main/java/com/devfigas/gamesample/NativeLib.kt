package com.devfigas.gamesample

object NativeLib {

    //<RustJNI>
    // auto-generated code
            
    public external fun loadCanvas(width: Int, height: Int): Unit

    public external fun loadEnemySpriteTable(size: Int, height: Int, width: Int): Unit

    public external fun loadPlayerSpriteTable(size: Int, height: Int, width: Int): Unit

    public external fun actionJump(): Unit

    public external fun actionStart(): Unit

    public external fun isDead(): Boolean

    public external fun getPlayerX(): Float

    public external fun getPlayerY(): Float

    public external fun getEnemyX(): Float

    public external fun getEnemyY(): Float

    public external fun getPlayerSprite(): Int

    public external fun getEnemySprite(): Int
            
    init { System.loadLibrary("my_rust_lib") }
            
    //</RustJNI>

}