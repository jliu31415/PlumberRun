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
    private float[] bounds; //bounding points
    private int frameCount = 0;
    private static final int playerSize = Tile.tileSize * 2;
    private double maxSpeedX = 10, maxSpeedY = 30;
    private double velX = maxSpeedX, velY = 0;
    private int freeFallCounter = 0;
    private boolean airborne = false, windUp = false, throwing = false;
    private boolean slowMotion = false;
    private double gravity;

    Player(Bitmap plumberSprites) {
        this.plumberSprites = plumberSprites;
        spriteSize = plumberSprites.getWidth() / 5;
        spriteFrame = new Rect(0, 0, spriteSize, spriteSize);
        playerPosition = new Rect(-2 * playerSize, 800, -playerSize, 800 + playerSize);
        setBounds();
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

        if (playerPosition.centerX() < 0) gravity = 0;
        else gravity = -1.5;
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

    boolean isWindingUp() {
        return windUp;
    }

    void throwPlunger() {
        windUp = false;
        slowMotion = false;
    }

    @Override
    void setBounds() {
        bounds = new float[]{playerPosition.left + playerSize / 4.0f, playerPosition.top,
                playerPosition.left + 3 * playerSize / 4.0f, playerPosition.top,
                playerPosition.left + 3 * playerSize / 4.0f, playerPosition.top + 7 * playerSize / 8.0f,
                playerPosition.left + playerSize / 4.0f, playerPosition.top + 7 * playerSize / 8.0f};
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
        if (normal.y < 0) {
            airborne = false;
            freeFallCounter = 0;
        }
        velX = maxSpeedX * -normal.y / Math.hypot(normal.x, normal.y);   //normal y is already inverted
        if (!airborne) velY = velX * -normal.x / Math.hypot(normal.x, normal.y);
    }
}
