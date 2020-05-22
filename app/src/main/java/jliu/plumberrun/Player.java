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
    private int frameCount = 0;
    private static final int imageSize = Tile.tileSize * 2;
    private double maxSpeedX = 15, maxSpeedY = 30;
    private double velX = maxSpeedX, velY = 0;
    private boolean airBorne = false, windUp = false, throwing = false;
    private boolean slowMotion = false;
    private final double gravity = -2;

    Player(Bitmap playerSprite) {
        this.playerSprite = playerSprite;
        spriteSize = playerSprite.getWidth() / 5;
        spriteFrame = new Rect(0, 0, spriteSize, spriteSize);
        playerPosition = new Rect(-imageSize, 800, 0, 800 + imageSize);
        setPoints();
    }

    void draw(Canvas canvas) {
        canvas.drawBitmap(playerSprite, spriteFrame, Game.scaleRect(playerPosition), null);
    }

    void update() {
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

        offSetPosition((int) velX, (int) -velY);
        if (velY < 0) airBorne = true;   //free fall without jumping
    }

    void jump() {
        if (!airBorne) {
            airBorne = true;
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
        if (normal.y < 0) airBorne = false;
        velX = maxSpeedX * -normal.y / Math.hypot(normal.x, normal.y);   //normal y is already inverted
        if (!airBorne) velY = velX * -normal.x / Math.hypot(normal.x, normal.y);
    }

    @Override
    float[] getBounds() {
        return points;
    }
}
