package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Tile {
    private final Bitmap tileSprite;
    private Rect spritePosition;
    private int tileSize = 69; //26 tiles to fill width of landscape canvas (15.65 to fill height)

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
