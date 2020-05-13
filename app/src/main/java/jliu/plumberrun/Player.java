package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

class Player extends CollisionObject {
    private final Bitmap runningSprite, throwingSprite;
    private final int runningSpriteWidth, runningSpriteHeight, throwingSpriteWidth, throwingSpriteHeight;
    private Rect playerPosition;
    private float[] points; //bounding points
    private Rect spriteFrame;   //frame of Bitmap to extract
    private int frameCount;
    private static final int imageSize = Tile.tileSize * 2;
    private double maxSpeed = 10;
    private double velX = maxSpeed, velY = 0;
    private double jumpVelocity = 30;
    private boolean windUp = false, throwing = false, jumping = false;
    private boolean slowMotion = false;
    private final double gravity = 0;

    Player(Bitmap runningSprite, Bitmap throwingSprite) {
        this.runningSprite = runningSprite;
        this.throwingSprite = throwingSprite;
        runningSpriteWidth = runningSprite.getWidth() / 4;
        runningSpriteHeight = runningSprite.getHeight() / 3;
        throwingSpriteWidth = throwingSprite.getWidth() / 4;
        throwingSpriteHeight = throwingSprite.getHeight() / 3;
        playerPosition = new Rect(0, 15 * 69 - imageSize, imageSize, 15 * 69);
        setPoints();
    }

    void draw(Canvas canvas) {
        if (windUp || throwing)
            canvas.drawBitmap(throwingSprite, spriteFrame, Game.scaleRect(playerPosition), null);
        else
            canvas.drawBitmap(runningSprite, spriteFrame, Game.scaleRect(playerPosition), null);
    }

    void update() {
        int framePosX, framePosY;
        if (windUp || throwing) {
            if (windUp && frameCount == 5) frameCount--;
            if (frameCount == 12) throwing = false;
            framePosX = throwingSpriteWidth * (frameCount % 4);
            framePosY = throwingSpriteHeight * (frameCount / 4 % 3);
            spriteFrame = new Rect(framePosX, framePosY, framePosX + throwingSpriteWidth, framePosY + throwingSpriteHeight);
        } else {
            if (jumping) {
                //jump animation
            }
            framePosX = runningSpriteWidth * (frameCount % 4);
            framePosY = runningSpriteHeight * (frameCount / 4 % 3);
            spriteFrame = new Rect(framePosX, framePosY, framePosX + runningSpriteWidth, framePosY + runningSpriteHeight);
        }
        frameCount++;

        if (!slowMotion) {
            if (velX < maxSpeed) velX++;
            velY = Math.max(velY + gravity, -20);
        } else {
            velX = 1;
            velY = Math.max(velY, -1);
        }
        offSetPosition((int) velX, (int) -velY, 0);
    }

    void windUp() {
        windUp = true;
        slowMotion = true;
        frameCount = 0;
    }

    void throwPlunger() {
        throwing = true;
        windUp = false;
        slowMotion = false;
    }

    void jump() {
        if (!jumping) {
            velY = jumpVelocity;
            jumping = true;
        }
    }

    Rect getPosition() {
        return playerPosition;
    }

    @Override
    public void setPoints() {
        float posX = playerPosition.left;
        float posY = playerPosition.top;
        points = new float[]{posX, posY, posX + imageSize, posY, posX + imageSize, posY + imageSize,
                posX, posY + imageSize, posX, posY};
    }

    @Override
    public void offSetPosition(int dX, int dY, float dTheta) {
        playerPosition.offset(dX, dY);
        for (int i = 0; i < points.length; i++) {
            if (i % 2 == 0) points[i] += dX;
            else points[i] += dY;
        }
    }

    @Override
    public float[] getBounds() {
        return points;
    }

    @Override
    public double getVelX() {
        return velX;
    }

    @Override
    public double getVelY() {
        return velY;
    }
}
