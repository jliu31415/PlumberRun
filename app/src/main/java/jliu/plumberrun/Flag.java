package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;

class Flag extends CollisionObject {
    private final Bitmap flagSprites;
    private float[] points;
    private Rect flagPosition, spriteFrame;
    private final int spriteWidth;
    private int frameCount = 0, frameIncrement = 1, pauseCount = 0;

    Flag(Bitmap flagSprites, double posX, double posY) {
        this.flagSprites = flagSprites;
        flagPosition = new Rect((int) posX, (int) posY, (int) posX + Tile.tileSize, (int) (posY + 1.5 * Tile.tileSize));
        spriteWidth = flagSprites.getWidth() / 5;
        spriteFrame = new Rect(0, 0, spriteWidth, flagSprites.getHeight());
        setPoints();
    }

    void updateFrame() {
        if (pauseCount++ == 1) {    //update every other frame
            pauseCount = 0;
            frameCount += frameIncrement;
            if (frameCount == 0 || frameCount == 4) frameIncrement *= -1;
        }
        spriteFrame.offsetTo(frameCount * spriteWidth, 0);
    }

    void draw(Canvas canvas) {
        canvas.drawBitmap(flagSprites, spriteFrame, Game.scaleRect(flagPosition), null);
    }

    @Override
    void setPoints() {
        points = new float[]{flagPosition.left, flagPosition.top, flagPosition.right, flagPosition.top,
                flagPosition.right, flagPosition.bottom, flagPosition.left, flagPosition.bottom};
    }

    @Override
    float[] getBounds() {
        return points;
    }

    @Override
    Rect getPosition() {
        return flagPosition;
    }

    @Override
    void offSetPosition(int dX, int dY) {

    }

    @Override
    void collide(PointF normal) {

    }
}
