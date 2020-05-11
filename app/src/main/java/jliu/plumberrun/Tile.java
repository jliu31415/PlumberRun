package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Tile {
    private final Bitmap tileSprite;
    private Rect spritePosition;
    public static final int tileSize = 80;

    public Tile(Bitmap tileSprite, int posX, int posY) {
        this.tileSprite = tileSprite;
        spritePosition = new Rect(posX, posY, posX + tileSize, posY + tileSize);
    }

    public void drawTile(Canvas canvas) {
        canvas.drawBitmap(tileSprite, null, Game.scale(spritePosition), null);
    }

    public Rect getBounds() {
        return spritePosition;
    }
}
