package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;

class Player extends CollisionObject {
    private final Bitmap playerSprite;
    private final int spriteSize;
    private Rect spriteFrame;
    private Rect playerPosition;
    private float[] points; //bounding points
    private int frameCount;
    private static final int imageSize = Tile.tileSize * 2;
    private double maxSpeedX = 10;
    private double velX = maxSpeedX, velY = 0;
    private double jumpVelocity = 35;
    private boolean running = true, jumping = false, landing = false, windUp = false, throwing = false;
    private boolean slowMotion = false;
    private final double gravity = -1.5;

    Player(Bitmap playerSprite) {
        this.playerSprite = playerSprite;
        spriteSize = playerSprite.getWidth() / 5;
        spriteFrame = new Rect(0, 0, spriteSize, spriteSize);
        playerPosition = new Rect(-imageSize, Game.getCanvasDimensions().bottom / 2,
                0, Game.getCanvasDimensions().bottom / 2 + imageSize);
        setPoints();
    }

    void draw(Canvas canvas) {
        canvas.drawBitmap(playerSprite, spriteFrame, Game.scaleRect(playerPosition), null);
    }

    void update() {
        if (velY < 0) jumping = true;   //free fall without jumping
        if (jumping) landing = true;

        boolean incrementFrame = true;
        //running [0, 9], jumping up [10], falling down [11, 12], landing [13, 14], wind up [15, 19], throwing [20, 24]
        if (running && !jumping && !landing && !windUp && !throwing) {
            if (frameCount > 9) frameCount = 0;
        } else if (jumping && !windUp && !throwing) {
            if (frameCount < 10 || frameCount > 12 || velY > 0) frameCount = 10;
            if (frameCount == 12) incrementFrame = false;
        } else if (landing && !windUp && !throwing) {
            if (frameCount < 13 || frameCount > 14) frameCount = 13;
            if (frameCount == 14) {
                landing = false;
                jumping = true;
                frameCount = 9; //transitions with running animation
            }
        } else if (windUp) {
            if (frameCount < 15 || frameCount > 19) frameCount = 15;
            if (frameCount == 19) incrementFrame = false;
        } else if (throwing) {
            if (frameCount < 20 || frameCount > 24) frameCount = 20;
            if (frameCount == 24) {
                throwing = false;
                running = true;
                frameCount = 8; //transitions with running animation
                if (jumping) frameCount = 11;
            }
        }
        spriteFrame.offsetTo(spriteSize * (frameCount % 5), spriteSize * (frameCount / 5));
        if (incrementFrame) frameCount++;

        if (!slowMotion) {
            if (velX < maxSpeedX) velX++;
            velY = Math.max(velY + gravity, -20);
        } else {
            velX = 1;
            velY = Math.min(0, velY - .1);
        }

        offSetPosition((int) velX, (int) -velY);
    }

    void jump() {
        if (!jumping) {
            jumping = true;
            velY = jumpVelocity;
        }
    }

    void windUp() {
        windUp = true;
        slowMotion = true;
    }

    void throwPlunger() {
        throwing = true;
        windUp = false;
        slowMotion = false;
    }

    @Override
    Rect getPosition() {
        return playerPosition;
    }

    @Override
    void setPoints() {
        points = new float[]{playerPosition.left + imageSize / 4.0f, playerPosition.top,
                playerPosition.left + 3 * imageSize / 4.0f, playerPosition.top,
                playerPosition.left + 3 * imageSize / 4.0f, playerPosition.top + 7 * imageSize / 8.0f,
                playerPosition.left + imageSize / 4.0f, playerPosition.top + 7 * imageSize / 8.0f};
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
