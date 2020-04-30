package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;

class LevelCreator {
    private final Bitmap tileSprite;
    private final int spriteWidth, spriteHeight;
    private ArrayList<Color[][]> levels;
    private static ArrayList<Tile> tiles;

    public LevelCreator(Bitmap tiles_platform) {
        tileSprite = tiles_platform;
        spriteWidth = tileSprite.getWidth() / 5;
        spriteHeight = tileSprite.getHeight() / 3;
        tiles = new ArrayList<>();
        levels = new ArrayList<>();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void createLevel(ArrayList<Integer[]> level) {
        for (int col = 0; col < level.size(); col++) {
            for (int row = 0; row < level.get(col).length; row++)    //j < 16
                if (level.get(col)[row] == 1)
                    tiles.add(new Tile(Bitmap.createBitmap(tileSprite, 0, 0, spriteWidth, spriteHeight), 69 * col, 69 * row));
        }
    }

    public void draw(Canvas canvas) {
        for (Tile t : tiles) {
            t.drawTile(canvas);
        }
    }

    public static boolean[] checkCollisionsAndUpdate(Player player) {
        boolean[] ret = new boolean[]{false, false, false, false};
        for (Tile t : tiles) {
            if (Rect.intersects(player.getBounds(), t.getBounds())) {
                if (Rect.intersects(player.getBoundTop(), t.getBounds())) {
                    player.setY(t.getBounds().bottom, 0);
                    ret[0] = true;
                } else if (Rect.intersects(player.getBoundBottom(), t.getBounds())) {
                    player.setY(t.getBounds().top - player.getBounds().height(), 0);
                    ret[1] = true;
                } else if (Rect.intersects(player.getBoundLeft(), t.getBounds())) {
                    player.setX(t.getBounds().right, 0);
                    ret[2] = true;
                } else if (Rect.intersects(player.getBoundRight(), t.getBounds())) {
                    player.setX(t.getBounds().left - player.getBounds().width(), 0);
                    ret[3] = true;
                }
            }
        }
        return ret;
    }
}
