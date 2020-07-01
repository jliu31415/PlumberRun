package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;

class Player extends CollisionObject {
    private final Bitmap plumberSprites;
    private Bitmap mirroredSprites;
    private final int spriteSize;   //spriteWidth = spriteHeight
    private Rect spriteFrame;
    private Rect playerPosition;
    private float[] bounds; //bounding points
    private int frameCount = 0, pauseCount = 0;
    private static final int playerSize = Tile.tileSize * 2;
    private double maxSpeedX = 15, maxSpeedY = 35;
    private double velX, velY;
    private int freeFallCounter = 0, jumpCounter = 0;
    private boolean initialized = false;
    private boolean airborne = true, jumpLatch = false;
    private boolean windUp = false, throwing = false;
    private boolean slowMotion = false;
    private boolean flipped = false;
    private double gravity;

    Player(Bitmap plumberSprites) {
        this.plumberSprites = plumberSprites;
        Matrix reflection = new Matrix();
        reflection.setScale(-1, 1, plumberSprites.getWidth() / 2.0f, plumberSprites.getHeight() / 2.0f);
        mirroredSprites = Bitmap.createBitmap(plumberSprites, 0, 0, plumberSprites.getWidth(), plumberSprites.getHeight(),
                reflection, true);
        spriteSize = plumberSprites.getWidth() / 5;
        spriteFrame = new Rect(0, 0, spriteSize, spriteSize);
        initialize();
    }

    void draw(Canvas canvas) {
        if (flipped)
            canvas.drawBitmap(mirroredSprites, spriteFrame, Game.scaleRect(playerPosition), null);
        else {
            canvas.drawBitmap(plumberSprites, spriteFrame, Game.scaleRect(playerPosition), null);
        }
    }

    void update() {
        if (initialized) {
            if (!throwing) {
                frameCount = frameCount % 10;
            } else {
                if (frameCount == 16 && windUp) frameCount--;
                else if (!windUp && pauseCount++ == 1) {   //slow down release animation
                    frameCount--;
                    pauseCount = 0;
                }
                if (frameCount == 19) throwing = false;
            }

            if (flipped) {
                spriteFrame.offsetTo(spriteSize * (4 - frameCount % 5), spriteSize * (frameCount / 5));
            } else {
                spriteFrame.offsetTo(spriteSize * (frameCount % 5), spriteSize * (frameCount / 5));
            }
            frameCount++;

            if (velX < maxSpeedX) velX++;
            velY = Math.max(velY + gravity, -maxSpeedY);

            if (freeFallCounter++ > 5) airborne = true; //cannot jump when in free fall

            if (playerPosition.centerX() < 0) gravity = 0;
            else gravity = -2;

            if (!throwing) flip(false);

            if (jumpLatch) jump();
            if (jumpCounter++ < 5 && !airborne) jump(); //delayed user input

            if (!slowMotion)
                offSetPosition((int) velX, (int) -velY);
            else
                offSetPosition((int) (velX / maxSpeedX), (int) (-velY / maxSpeedX));   //normalize with maxSpeedX
        }
    }

    private void jump() {
        jumpCounter = 0;
        if (!airborne) {
            airborne = true;
            velY = maxSpeedY;
        }
    }

    void setJumpLatch(boolean jumpLatch) {
        this.jumpLatch = jumpLatch;
    }

    void windUp() {
        throwing = true;
        windUp = true;
        slowMotion = true;
        frameCount = 10;
    }

    boolean isWindingUp() {
        return windUp;
    }

    void throwPlunger() {
        windUp = false;
        slowMotion = false;
    }

    @Override
    void setBounds() {
        bounds = new float[]{playerPosition.left + playerSize * .25f, playerPosition.top,
                playerPosition.left + playerSize * .75f, playerPosition.top,
                playerPosition.left + playerSize * .75f, playerPosition.top + playerSize * .875f,
                playerPosition.left + playerSize * .25f, playerPosition.top + playerSize * .875f};
    }

    @Override
    float[] getBounds() {
        return bounds;
    }

    @Override
    void offSetPosition(int dX, int dY) {
        playerPosition.offset(dX, dY);
        for (int i = 0; i < bounds.length; i++) {
            if (i % 2 == 0) bounds[i] += dX;
            else bounds[i] += dY;
        }
    }

    @Override
    Rect getPosition() {
        return playerPosition;
    }

    @Override
    void collide(PointF normal) {
        offSetPosition((int) normal.x, (int) -normal.y);

        velX = maxSpeedX * normal.y / Math.hypot(normal.x, normal.y);
        if (velY * normal.y < 0) velY = velX * -normal.x / Math.hypot(normal.x, normal.y);

        if (normal.y > 0) {
            airborne = false;
            freeFallCounter = 0;
        }
    }

    //flip does not change bounds array; bounds is symmetric
    void flip(boolean flip) {
        if (!flipped == flip) {
            flipped = !flipped;
        }
    }

    void initialize() {
        initialized = true;
        playerPosition = new Rect(-2 * playerSize, 500, -playerSize, 500 + playerSize);
        velX = maxSpeedX;
        velY = 0;
        setBounds();
    }
}
