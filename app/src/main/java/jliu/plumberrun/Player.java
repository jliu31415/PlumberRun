package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;

class Player extends CollisionObject {
    private final Bitmap runningSprite, throwingSprite;
    private final int runningSpriteWidth, runningSpriteHeight, throwingSpriteWidth, throwingSpriteHeight;
    private Rect playerPosition;
    private float[] points; //bounding points
    private Rect runningSpriteFrame, throwingSpriteFrame;   //frame of Bitmap to extract
    private int frameCount;
    private static final int imageSize = Tile.tileSize * 2;
    private double maxSpeedX = 10;
    private double velX = maxSpeedX, velY = 0;
    private double jumpVelocity = 30;
    private boolean running = true, windUp = false, throwing = false, jumping = false;
    private boolean slowMotion = false;
    private final double gravity = -1.2;

    Player(Bitmap runningSprite, Bitmap throwingSprite) {
        this.runningSprite = runningSprite;
        this.throwingSprite = throwingSprite;
        runningSpriteWidth = runningSprite.getWidth() / 4;
        runningSpriteHeight = runningSprite.getHeight() / 3;
        throwingSpriteWidth = throwingSprite.getWidth() / 4;
        throwingSpriteHeight = throwingSprite.getHeight() / 3;
        runningSpriteFrame = new Rect(0, 0, runningSpriteWidth, runningSpriteHeight);
        throwingSpriteFrame = new Rect(0, 0, throwingSpriteWidth, throwingSpriteHeight);
        playerPosition = new Rect(0, Game.getCanvasDimensions().bottom / Tile.tileSize * Tile.tileSize - imageSize,
                imageSize, Game.getCanvasDimensions().bottom / Tile.tileSize * Tile.tileSize);
        setPoints();
    }

    void draw(Canvas canvas) {
        if (running && !jumping && !windUp && !throwing)
            canvas.drawBitmap(runningSprite, runningSpriteFrame, Game.scaleRect(playerPosition), null);
        else
            canvas.drawBitmap(throwingSprite, throwingSpriteFrame, Game.scaleRect(playerPosition), null);
    }

    void update() {
        int framePosX, framePosY;

        if (frameCount == 13) {
            throwing = false;
            running = true;
        }

        if (running && !jumping && !windUp && !throwing) {
            framePosX = runningSpriteWidth * (frameCount % 4);
            framePosY = runningSpriteHeight * (frameCount / 4 % 3);
            runningSpriteFrame.offsetTo(framePosX, framePosY);
        } else if (jumping && !windUp) {
            //jumping animation
        } else if (windUp || throwing) {
            if (windUp && frameCount == 5) frameCount--;
            framePosX = throwingSpriteWidth * (frameCount % 4);
            framePosY = throwingSpriteHeight * (frameCount / 4 % 3);
            throwingSpriteFrame.offsetTo(framePosX, framePosY);
        }
        frameCount++;

        if (!slowMotion) {
            if (velX < maxSpeedX) velX++;
            velY = Math.max(velY + gravity, -20);
        } else {
            velX = 1;
            velY = Math.max(velY + gravity, -1);
        }

        if (velY < 0) jumping = true;    //free fall without jumping

        offSetPosition((int) velX, (int) -velY);
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

    @Override
    Rect getPosition() {
        return playerPosition;
    }

    @Override
    void setPoints() {
        points = new float[]{playerPosition.left, playerPosition.top,
                playerPosition.left + imageSize, playerPosition.top,
                playerPosition.left + imageSize, playerPosition.top + imageSize,
                playerPosition.left, playerPosition.top + imageSize};
    }

    @Override
    void offSetPosition(int dX, int dY) {
        playerPosition.offset(dX, dY);
        for (int i = 0; i < points.length; i++) {
            if (i % 2 == 0) points[i] += dX;
            else points[i] += dY;
        }
    }

    @Override
    void collide(PointF normal) {
        velX = maxSpeedX * (1 - Math.abs(normal.x / Math.hypot(normal.x, normal.y)));
        if (normal.y != 0) velY = 0;
        if (normal.y > 0) jumping = false;
    }

    @Override
    float[] getBounds() {
        return points;
    }
}
