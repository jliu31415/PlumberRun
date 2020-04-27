package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Tile {
    private Bitmap tileSprite;
    private Rect position;
    private int posX, posY;
    private int tileSize; //26 tiles to fill width of canvas (landscape), 69 x 69 pixels

    public Tile(Bitmap tileSprite, int posX, int posY){
        this.tileSprite = tileSprite;
        this.posX = posX;
        this.posY = posY;
    }

    public void drawTile(Canvas canvas){
        tileSize = (int)(69*Game.canvasScaleY);
        //(posX, posY) = (0 0) at bottom left corner of canvas
        int adjustedPosX = (tileSize*posX);
        int adjustedPosY = (int)(1080*Game.canvasScaleY - tileSize*posY);
        //adjustedPosX, adjustedPosY gives bottom left corner of spritePosition rectangle
        position = new Rect(adjustedPosX, adjustedPosY - tileSize, adjustedPosX + tileSize, adjustedPosY);
        canvas.drawBitmap(tileSprite, null, position, null);
    }
}
