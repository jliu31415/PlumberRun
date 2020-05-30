package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;

class Player extends CollisionObject {
    private final Bitmap plumberSprites;
    private final int spriteSize;   //spriteWidth = spriteHeight
    private Rect spriteFrame;
    private Rect playerPosition;
    private float[] points; //bounding points
    private int frameCount = 0;
    private static final int playerSize = Tile.tileSize * 2;
    private double maxSpeedX = 10, maxSpeedY = 30;
    private double velX = maxSpeedX, velY = 0;
    private int freeFallCounter = 0;
    private boolean airborne = false, windUp = false, throwing = false;
    private boolean slowMotion = false;
    private final double gravity = -1.5;

    Player(Bitmap plumberSprites) {
        this.plumberSprites = plumberSprites;
        spriteSize = plumberSprites.getWidth() / 5;
        spriteFrame = new Rect(0, 0, spriteSize, spriteSize);
        playerPosition = new Rect(-playerSize, 800, 0, 800 + playerSize);
        setPoints();
    }

    void draw(Canvas canvas) {
        canvas.drawBitmap(plumberSprites, spriteFrame, Game.scaleRect(playerPosition), null);
    }

    void update() {
        offSetPosition((int) velX, (int) -velY);    //offset after checking collisions (before next update)

        if (!throwing) {
            frameCount = frameCount % 10;
        } else {
            if (frameCount == 16 && windUp) frameCount--;
            if (frameCount == 19) throwing = false;
        }

        spriteFrame.offsetTo(spriteSize * (frameCount % 5), spriteSize * (frameCount / 5));
        frameCount++;

        if (!slowMotion) {
            if (velX < maxSpeedX) velX++;
            velY = Math.max(velY + gravity, -maxSpeedY);
        } else {
            velX = 1;
            velY = Math.min(0, velY - .1);
        }

        if (freeFallCounter++ > 5) airborne = true; //cannot jump when in free fall
    }

    void jump() {
        if (!airborne) {
            airborne = true;
            velY = maxSpeedY;
        }
    }

    void windUp() {
        throwing = true;
        windUp = true;
        slowMotion = true;
        frameCount = 10;
    }

    void throwPlunger() {
        windUp = false;
        slowMotion = false;
    }

    @Override
    Rect getPosition() {
        return playerPosition;
    }

    @Override
    void setPoints() {
        points = new float[]{playerPosition.left + playerSize / 4.0f, playerPosition.top,
                playerPosition.left + 3 * playerSize / 4.0f, playerPosition.top,
                playerPosition.left + 3 * playerSize / 4.0f, playerPosition.top + 7 * playerSize / 8.0f,
                playerPosition.left + playerSize / 4.0f, playerPosition.top + 7 * playerSize / 8.0f};
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
        if (normal.y < 0) {
            airborne = false;
            freeFallCounter = 0;
        }
        velX = maxSpeedX * -normal.y / Math.hypot(normal.x, normal.y);   //normal y is already inverted
        if (!airborne) velY = velX * -normal.x / Math.hypot(normal.x, normal.y);
    }

    @Override
    float[] getBounds() {
        return points;
    }
}
