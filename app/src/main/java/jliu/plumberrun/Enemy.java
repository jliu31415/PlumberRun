package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

class Enemy extends CollisionObject {
    private final Bitmap toiletSprites;
    private final Bitmap mirroredSprites;
    private Rect enemyPosition, spriteFrame;
    private Point initializedPosition;
    private float[] bounds;
    private final int spriteWidth, spriteHeight;
    private final int enemySize = Constants.enemySize;
    private double velX = -Constants.enemySpeed, velY = 0;    //enemy starts by moving left
    private int frameCount = 0, frameIncrement = 1, pauseCount = 1;
    private boolean groundEnemy;
    private boolean flipped = false;
    private boolean fading = false;
    private Paint opacity;

    Enemy(Bitmap toiletSprites, boolean groundEnemy, double posX, double posY) {
        this.toiletSprites = toiletSprites;
        this.groundEnemy = groundEnemy;
        Matrix reflection = new Matrix();
        reflection.setScale(-1, 1, toiletSprites.getWidth() / 2.0f, toiletSprites.getHeight() / 2.0f);
        mirroredSprites = Bitmap.createBitmap(toiletSprites, 0, 0, toiletSprites.getWidth(), toiletSprites.getHeight(),
                reflection, true);
        initializedPosition = new Point((int) posX, (int) posY);
        posY -= enemySize - Constants.tileSize; //align bottom left corner of sprite
        enemyPosition = new Rect((int) posX, (int) posY, (int) posX + enemySize, (int) posY + enemySize);
        spriteWidth = toiletSprites.getWidth() / 5;
        spriteHeight = toiletSprites.getHeight() / 2;
        spriteFrame = new Rect(0, 0, spriteWidth, spriteHeight);
        if (!groundEnemy) spriteFrame.offset(0, spriteHeight);
        setBounds();
        opacity = new Paint();
        opacity.setAlpha(255);
    }

    void draw(Canvas canvas) {
        if (flipped)
            if (fading) canvas.drawBitmap(mirroredSprites, spriteFrame, enemyPosition, opacity);
            else canvas.drawBitmap(mirroredSprites, spriteFrame, enemyPosition, null);
        else {
            if (fading) canvas.drawBitmap(toiletSprites, spriteFrame, enemyPosition, opacity);
            else canvas.drawBitmap(toiletSprites, spriteFrame, enemyPosition, null);
        }
    }

    void update() {
        if (pauseCount++ == 1) {    //update every other frame
            pauseCount = 0;
            frameCount += frameIncrement;
            if (frameCount == 0 || frameCount == 4) frameIncrement *= -1;
        }

        if (flipped) {
            spriteFrame.offsetTo((4 - frameCount) * spriteWidth, 0);
        } else {
            spriteFrame.offsetTo(frameCount * spriteWidth, 0);
        }
        if (!groundEnemy) spriteFrame.offset(0, spriteHeight);

        if (fading && opacity.getAlpha() > 0)
            opacity.setAlpha(Math.max(0, opacity.getAlpha() - Constants.fade));

        if (groundEnemy) velY = Math.max(velY + Constants.playerGravity, -Constants.playerJumpVel);
        offSetPosition((int) velX, (int) -velY);
    }

    @Override
    void setBounds() {
        bounds = new float[]{enemyPosition.left + enemySize * .6f, enemyPosition.top + enemySize * .45f,
                enemyPosition.left + enemySize * .8f, enemyPosition.top + enemySize * .45f,
                enemyPosition.left + enemySize * .6f, enemyPosition.top + enemySize,
                enemyPosition.left + enemySize * .3f, enemyPosition.top + enemySize};

        if (!groundEnemy) {
            Matrix transform = new Matrix();
            transform.setRotate(-15, enemyPosition.centerX(), enemyPosition.centerY());
            transform.postTranslate(-enemySize * .1f, -enemySize * .1f);
            transform.mapPoints(bounds);
        }
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

    Point getInitializedPosition() {
        return initializedPosition;
    }

    @Override
    void collide(PointF normal) {
        offSetPosition((int) normal.x, (int) -normal.y);
        velY = 0;

        if (normal.x * velX < 0) {
            flipped = !flipped;
            //reflect bounds
            for (int i = 0; i < bounds.length; i += 2) {
                bounds[i] = 2 * enemyPosition.centerX() - bounds[i];
            }
            velX *= -1;
        }
    }

    void fade() {
        fading = true;
        velX = 0;
        frameIncrement = 0;
    }

    boolean isDead() {
        return fading;
    }

    boolean canRemove() {
        return opacity.getAlpha() == 0;
    }
}
