package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;

class Plunger {
    private final Bitmap plunger;
    private final Player player;
    private final LevelCreator levelCreator;
    private final double startX, startY;
    private double endX, endY;
    private double velX, velY;  //velY += gravity, velY must be double
    private Rect spritePosition;
    private final int plungerOffsetX = -80;   //have plunger in player's hand
    private final int imageSize = Tile.tileSize * 2;
    private final double plungerMaxSpeed = 30;
    private double plungerSpeed = 0;   //current plunger speed
    private double initialAngle, angle, angularVel, snapToAngle; //angle follows parabolic arc when fired; snapToAngle {0, PI/2, -PI/2}
    private double power = 1;   //pull back more for more power; [.5, 1]
    private final double gravity = -.5;
    private int airTime = 0;
    private boolean fired = false, sticking = false, falling = false;
    private float pivotX, pivotY;  //image rotation pivot coordinates
    private float[] points;  //used for hitBox
    private final Paint white;

    public Plunger(Bitmap plunger, Player player, LevelCreator levelCreator, double startX, double startY) {
        this.plunger = plunger;
        this.player = player;
        this.levelCreator = levelCreator;
        this.startX = startX;
        this.startY = startY;
        spritePosition = new Rect(player.getPosX() + plungerOffsetX, player.getPosY(),
                player.getPosX() + plungerOffsetX + imageSize, player.getPosY() + imageSize);
        points = new float[8];
        pivotX = spritePosition.centerX();
        pivotY = spritePosition.centerY();
        setPoints();
        white = new Paint();
        white.setColor(Color.WHITE);
    }

    public void draw(Canvas canvas) {
        canvas.rotate((float) Math.toDegrees(-angle), (float) (pivotX * Game.canvasScaleX), (float) (pivotY * Game.canvasScaleY));
        canvas.drawBitmap(plunger, null, Game.scale(spritePosition), null);
        canvas.rotate((float) Math.toDegrees(angle), (float) (pivotX * Game.canvasScaleX), (float) (pivotY * Game.canvasScaleY));

        //draw arc
        if (!fired) {
            for (int i = 10; i <= 50; i += 5) {
                if (i == 10 && power < .7) i += 5;
                canvas.drawCircle((float) (pivotX + power * plungerMaxSpeed * i * Math.cos(angle)),
                        (float) (pivotY - (power * plungerMaxSpeed * i * Math.sin(angle) + gravity * Math.pow(i, 2) / 2.0)),
                        (55 - i) / 5, white);
            }
        }
    }

    public void update() {
        if (!fired) {
            velX = player.getPosX() - spritePosition.left + plungerOffsetX;
            velY = -(player.getPosY() - spritePosition.top);

            angularVel = angle;
            if (startX <= endX) {
                if (startY < endY) initialAngle = Math.PI / 2.0;
                else initialAngle = -Math.PI / 2.0;
            } else {
                initialAngle = Math.atan((endY - startY) / (startX - endX));
            }
            angle = initialAngle;
            angularVel -= angle;

            power = Math.hypot(startX - endX, Math.abs(startY - endY)) / 200;
            power = Math.min(power, 1);
            power = Math.max(power, .5);
        } else {
            if (sticking) {
                angularVel = (snapToAngle - angle) / 3.0;
                angle += angularVel;
            } else if (falling) {
                angle += angularVel;
                velY += gravity;
            } else {
                angularVel = angle;
                angle = Math.atan(Math.tan(initialAngle) + (gravity * airTime) / (plungerSpeed * Math.cos(initialAngle)));
                angularVel -= angle;
                velX = plungerSpeed * Math.cos(initialAngle);
                velY = Math.max((plungerSpeed * Math.sin(initialAngle) + gravity * airTime++), -20);
            }
        }

        spritePosition.offset((int) velX, -(int) velY);
        updatePoints((int) velX, -(int) velY);

        if (fired && !sticking && !falling)
            levelCreator.updateCollisions(this);
    }

    public void fire() {
        fired = true;
        plungerSpeed = power * plungerMaxSpeed;
    }

    public void stick(ArrayList<Tile> collisionTiles) {
        sticking = true;
        velX = velY = 0;
        angularVel = 0;
        int index = -1;
        int count = 0;
        boolean pointIntersect;
        double offsetX = -Math.cos(angle);        //divide by five to increase precision
        double offsetY = Math.sin(angle);

        do {
            pointIntersect = false;
            outer:
            for (int i = points.length; i < points.length; i += 2) {
                for (Tile tile : collisionTiles) {
                    if (tile.getBounds().contains((int) points[i], (int) points[i + 1])) {
                        pointIntersect = true;
                        index = i;
                        break outer;
                    }
                }
            }
            if (pointIntersect) {
                updatePoints(offsetX, offsetY);
                count++;
            }
        } while (pointIntersect);

        spritePosition.offset((int) (offsetX * count), (int) (offsetY * count));
        //adjust for spritePosition truncation
        updatePoints((int) (offsetX * count) - (offsetX * count), (int) (offsetY * count) - (offsetY * count));

        if (Math.abs(angle) == Math.PI / 2.0)
            snapToAngle = angle;
        else {
            if (index == 0) snapToAngle = angle > 0 ? Math.PI / 2.0 : 0;
            if (index == 2) snapToAngle = angle < 0 ? -Math.PI / 2.0 : 0;
        }

        if (angle < snapToAngle) changePivot(points[0], points[1]);
        else changePivot(points[2], points[3]);
    }

    public void fall(double angularVel) {
        sticking = false;
        falling = true;
        this.angularVel = angularVel;
    }

    public void setEnd(double endX, double endY) {
        this.endX = endX;
        this.endY = endY;
    }

    public void updatePoints(double dX, double dY) {
        for (int i = 0; i < points.length; i++) {
            if (i % 2 == 0) points[i] += dX;
            else points[i] += dY;
        }
        pivotX += dX;
        pivotY += dY;

        Matrix rotation = new Matrix();
        rotation.setRotate((float) Math.toDegrees(angularVel), pivotX, pivotY);   //represents center of image plus offset
        rotation.mapPoints(points);
    }

    public void setPoints() {
        points[0] = spritePosition.right;
        points[2] = spritePosition.right;
        points[4] = spritePosition.right - imageSize / 3.0f;
        points[6] = spritePosition.right - imageSize / 6.0f;

        points[1] = spritePosition.centerY() - imageSize / 8.0f;
        points[3] = spritePosition.centerY() + imageSize / 8.0f;
        points[5] = spritePosition.centerY();
        points[7] = spritePosition.centerY();
    }

    public void changePivot(float pivotX, float pivotY) {
        float[] translate = new float[]{spritePosition.left, spritePosition.top};
        Matrix rotation = new Matrix();
        rotation.setRotate((float) Math.toDegrees(-angle), this.pivotX, this.pivotY);   //represents center of image plus offset
        rotation.postRotate((float) Math.toDegrees(angle), pivotX, pivotY);
        rotation.mapPoints(translate);

        spritePosition.offsetTo((int) translate[0], (int) translate[1]);
        this.pivotX = pivotX;
        this.pivotY = pivotY;
    }

    public float[] getBoundingPoints() {
        return points;
    }

    public Rect getSpritePosition() {
        return spritePosition;
    }
}
