package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;

class LevelCreator {
    private final Bitmap tileSprite;
    private final int spriteWidth, spriteHeight;
    private static ArrayList<Tile> tiles;

    public LevelCreator(Bitmap tiles_platform) {
        tileSprite = tiles_platform;
        spriteWidth = tileSprite.getWidth() / 5;
        spriteHeight = tileSprite.getHeight() / 3;
        tiles = new ArrayList<>();
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

    public static void checkCollisionsAndUpdate(Player player) {
        for (Tile t : tiles) {
            if (Rect.intersects(player.getBounds(), t.getBounds())) {
                if (Rect.intersects(player.getBoundTop(), t.getBounds())) {
                    player.setPosY(t.getBounds().bottom);
                    player.updatePosRect();
                    if (player.getVelY() > 0) player.setVelY(0);
                }
                if (Rect.intersects(player.getBoundBottom(), t.getBounds())) {
                    player.setPosY(t.getBounds().top - player.getBounds().height());
                    player.updatePosRect();
                    if (player.getVelY() < 0) player.setVelY(0);
                }
                if (Rect.intersects(player.getBoundLeft(), t.getBounds())) {
                    player.setPosX(t.getBounds().right);
                    player.updatePosRect();
                    if (player.getVelX() < 0) player.setVelX(0);
                }
                if (Rect.intersects(player.getBoundRight(), t.getBounds())) {
                    player.setPosX(t.getBounds().left - player.getBounds().width());
                    player.updatePosRect();
                    if (player.getVelX() > 0) player.setVelX(0);
                }
            }
        }
        player.updatePosRect(); //if no collisions, update position as normal
    }
}
