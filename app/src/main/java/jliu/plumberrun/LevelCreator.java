package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.ArrayList;

class LevelCreator {
    private Bitmap tileSprite;
    private int spriteWidth, spriteHeight;
    private ArrayList<Tile> tiles;

    public LevelCreator(Bitmap tiles_platform) {
        tileSprite = tiles_platform;
        spriteWidth = tileSprite.getWidth()/4;
        spriteHeight = tileSprite.getHeight()/2;

        //tiles will be read from n x 26 png
        tiles = new ArrayList<>();
        for(int i = 0; i < 26; i++){
            tiles.add(new Tile(Bitmap.createBitmap(tiles_platform, 0, 0, spriteWidth, spriteHeight), i, 0));
        }
    }

    public void draw(Canvas canvas) {
        for (Tile t: tiles) {
            t.drawTile(canvas);
        }
    }
}
