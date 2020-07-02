package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;

class Flag extends CollisionObject {
    private final Bitmap flagSprites;
    private Rect flagPosition, spriteFrame;
    private float[] bounds;
    private final int spriteWidth, spriteHeight;
    private int frameCount = 0, frameIncrement = 1;


    Flag(Bitmap flagSprites, double posX, double posY) {
        this.flagSprites = flagSprites;
        int flagHeight = Constants.flagHeight;
        posY -= flagHeight - Constants.tileSize;
        flagPosition = new Rect((int) posX, (int) posY, (int) (posX + flagHeight * 2 / 3.0), (int) (posY + flagHeight));
        spriteWidth = flagSprites.getWidth() / 5;
        spriteHeight = flagSprites.getHeight() / 2;
        spriteFrame = new Rect(0, 0, spriteWidth, spriteHeight);
        setBounds();
    }

    void draw(Canvas canvas) {
        canvas.drawBitmap(flagSprites, spriteFrame, flagPosition, null);
    }

    void update() {
        frameCount += frameIncrement;
        if (frameCount == 0 || frameCount == 9) frameIncrement *= -1;
        spriteFrame.offsetTo(frameCount % 5 * spriteWidth, frameCount / 5 * spriteHeight);
    }

    @Override
    void setBounds() {
        bounds = new float[]{flagPosition.left, flagPosition.top, flagPosition.right, flagPosition.top,
                flagPosition.right, flagPosition.bottom, flagPosition.left, flagPosition.bottom};
    }

    @Override
    float[] getBounds() {
        return bounds;
    }

    @Override
    void offSetPosition(int dX, int dY) {

    }

    @Override
    Rect getPosition() {
        return flagPosition;
    }

    @Override
    void collide(PointF normal) {

    }
}
