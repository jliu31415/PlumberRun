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
    private ArrayList<Tile> tiles;

    public LevelCreator(Bitmap tiles_platform) {
        tileSprite = tiles_platform;
        spriteWidth = tileSprite.getWidth() / 5;
        spriteHeight = tileSprite.getHeight() / 3;
        tiles = new ArrayList<>();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void createLevel(ArrayList<Integer[]> level) {
        for (int i = -10; i < 0; i++)    //initial buffer so player can run in frame
            tiles.add(new Tile(Bitmap.createBitmap(tileSprite, 0, 0, spriteWidth, spriteHeight), 69 * i, 69 * 15));
        for (int col = 0; col < level.size(); col++) {
            for (int row = 0; row < level.get(col).length; row++) {    //j < 16
                int tileID = level.get(col)[row] - 1;
                if (tileID != -1) {
                    Bitmap tile = Bitmap.createBitmap(tileSprite, spriteWidth * (tileID % 5), spriteHeight * (tileID / 3), spriteWidth, spriteHeight);
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
            if (tiles.get(i).getBounds().left > player.getBounds().right) break;
            if (Rect.intersects(player.getBounds(), tiles.get(i).getBounds())) {
                if (Rect.intersects(player.getBoundTop(), tiles.get(i).getBounds())) {
                    player.setPosY(tiles.get(i).getBounds().bottom);
                    player.updatePosRect();
                    if (player.getVelY() > 0) player.setVelY(0);
                }
                if (Rect.intersects(player.getBoundBottom(), tiles.get(i).getBounds())) {
                    player.setPosY(tiles.get(i).getBounds().top - player.getBounds().height());
                    player.updatePosRect();
                    if (player.getVelY() < 0) {
                        player.setVelY(0);
                        player.resetJump();
                    }
                }
                if (Rect.intersects(player.getBoundLeft(), tiles.get(i).getBounds())) {
                    player.setPosX(tiles.get(i).getBounds().right);
                    player.updatePosRect();
                    if (player.getVelX() < 0) player.setVelX(0);
                }
                if (Rect.intersects(player.getBoundRight(), tiles.get(i).getBounds())) {
                    player.setPosX(tiles.get(i).getBounds().left - player.getBounds().width());
                    player.updatePosRect();
                    if (player.getVelX() > 0) player.setVelX(0);
                }
            }
        }
        player.updatePosRect(); //if no collisions, update position as normal
    }
}
