package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;

class Enemy extends CollisionObject {
    private final Bitmap toiletSprites;
    private Bitmap mirroredSprites;
    private float[] bounds;
    private int enemySize = 2 * Tile.tileSize;
    private Rect enemyPosition, spriteFrame;
    private final int spriteSize;
    private int frameCount = 0, frameIncrement = 1, pauseCount = 1;
    private int velX = -5;
    private boolean flipped = false;

    Enemy(Bitmap toiletSprites, double posX, double posY) {
        this.toiletSprites = toiletSprites;
        Matrix reflection = new Matrix();
        reflection.setScale(-1, 1, toiletSprites.getWidth() / 2.0f, toiletSprites.getHeight() / 2.0f);
        mirroredSprites = Bitmap.createBitmap(toiletSprites, 0, 0, toiletSprites.getWidth(), toiletSprites.getHeight(),
                reflection, true);
        enemyPosition = new Rect((int) posX, (int) posY, (int) posX + enemySize, (int) posY + enemySize);
        spriteSize = toiletSprites.getWidth() / 5;
        spriteFrame = new Rect(0, 0, spriteSize, spriteSize);
        setBounds();
    }

    void draw(Canvas canvas) {
        if (flipped)
            canvas.drawBitmap(mirroredSprites, spriteFrame, Game.scaleRect(enemyPosition), null);
        else {
            canvas.drawBitmap(toiletSprites, spriteFrame, Game.scaleRect(enemyPosition), null);
        }
    }

    void update() {
        if (pauseCount++ == 1) {    //update every other frame
            pauseCount = 0;
            frameCount += frameIncrement;
            if (frameCount == 0 || frameCount == 4) frameIncrement *= -1;
        }

        if (flipped) {
            spriteFrame.offsetTo((4 - frameCount) * spriteSize, 0);
        } else {
            spriteFrame.offsetTo(frameCount * spriteSize, 0);
        }

        offSetPosition(velX, 0);
    }

    @Override
    void setBounds() {
        bounds = new float[]{enemyPosition.left + enemySize * .6f, enemyPosition.top + enemySize * .1f,
                enemyPosition.left + enemySize * .9f, enemyPosition.top + enemySize * .1f,
                enemyPosition.left + enemySize * .9f, enemyPosition.bottom,
                enemyPosition.left + enemySize * .1f, enemyPosition.bottom,
                enemyPosition.left + enemySize * .1f, enemyPosition.bottom - enemySize * .3f};
    }

    @Override
    float[] getBounds() {
        return bounds;
    }

    @Override
    void offSetPosition(int dX, int dY) {
        enemyPosition.offset(dX, dY);
        for (int i = 0; i < bounds.length; i++) {
            if (i % 2 == 0) bounds[i] += dX;
            else bounds[i] += dY;
        }
    }

    @Override
    Rect getPosition() {
        return enemyPosition;
    }

    @Override
    void collide(PointF normal) {
    }

    void flip(boolean flip) {
        if (!flipped == flip) {
            flipped = !flipped;
            for (int i = 0; i < bounds.length; i += 2) {
                bounds[i] = 2 * enemyPosition.centerX() - bounds[i];
            }
            if (flipped) velX = Math.abs(velX);
            else velX = -Math.abs(velX);
        }
    }

    double getVelX() {
        return velX;
    }
}
