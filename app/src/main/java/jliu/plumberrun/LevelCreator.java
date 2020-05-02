package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;

class LevelCreator {
    private final Bitmap tileSprite;
    private final double spriteWidth, spriteHeight;
    private int framePosX, framePosY;
    private ArrayList<Tile> tiles;

    public LevelCreator(Bitmap tiles_platform) {
        tileSprite = tiles_platform;
        spriteWidth = tileSprite.getWidth() / 5.0;
        spriteHeight = tileSprite.getHeight() / 3.0;
        tiles = new ArrayList<>();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void createLevel(ArrayList<Integer[]> level) {
        for (int col = 0; col < level.size(); col++) {
            for (int row = 0; row < level.get(col).length; row++) {    //j < 16
                int tileID = level.get(col)[row] - 1;
                if (tileID != -1) {
                    framePosX = (int) (spriteWidth * (tileID % 5));
                    framePosY = (int) (spriteHeight * (tileID / 5));
                    Bitmap tile = Bitmap.createBitmap(tileSprite, framePosX, framePosY, (int) spriteWidth, (int) spriteHeight);
                    tiles.add(new Tile(tile, 69 * col, 69 * row));
                }
            }
        }
    }

    public void draw(Canvas canvas) {
        for (Tile t : tiles) {
            if (Rect.intersects(t.getBounds(), Game.cameraFrame))
                t.drawTile(canvas);
        }
    }

    public void checkCollisionsAndUpdate(Player player) {
        for (int i = 0; i < tiles.size(); i++) {
            while (i < tiles.size() && tiles.get(i).getBounds().right < player.getBounds().left)
                i++;
            if (i == tiles.size() || tiles.get(i).getBounds().left > player.getBounds().right)
                break;

            if (Rect.intersects(player.getBounds(), tiles.get(i).getBounds())) {
                if (Rect.intersects(player.getBoundTop(), tiles.get(i).getBounds())) {
                    player.setPosY(tiles.get(i).getBounds().bottom);
                    if (player.getVelY() > 0)
                        player.setVelY(0);
                }
                if (Rect.intersects(player.getBoundBottom(), tiles.get(i).getBounds())) {
                    player.setPosY(tiles.get(i).getBounds().top - player.getBounds().height());
                    if (player.getVelY() < 0) {
                        player.setVelY(0);
                        player.resetJump();
                    }
                }
                if (Rect.intersects(player.getBoundLeft(), tiles.get(i).getBounds())) {
                    player.setPosX(tiles.get(i).getBounds().right);
                    if (player.getVelX() < 0)
                        player.setVelX(0);
                }
                if (Rect.intersects(player.getBoundRight(), tiles.get(i).getBounds())) {
                    player.setPosX(tiles.get(i).getBounds().left - player.getBounds().width());
                    if (player.getVelX() > 0)
                        player.setVelX(0);
                }
            }
        }
    }
}
