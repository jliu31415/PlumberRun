package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;

class Plunger extends CollisionObject {
    private final Bitmap plungerSprites;
    private final Player player;
    private final float startX, startY;
    private float endX, endY;
    private double velX, velY;
    private Rect plungerPosition;
    private final int plungerOffsetX = -80;   //offset plunger to player's hand
    private static final int plungerSize = Tile.tileSize * 2;
    private final double plungerMaxSpeed = 35;
    private double plungerSpeed = 0;   //current plunger speed
    private float aim, prevAngle, angle, snapToAngle; //angle follows parabolic arc when fired; [-PI/2, 3PI/2]
    private double power = 1;   //pull back more for more power; [.5, 1]
    private final double gravity = -.5;
    private int airTime = 0;
    private boolean fired = false, sticking = false, collided = false;
    private float pivotX, pivotY;  //image rotation pivot coordinates
    private float[] points;  //bounding points
    private final Paint white;

    Plunger(Bitmap plungerSprites, Player player, float touchX, float touchY) {
        this.plungerSprites = plungerSprites;
        this.player = player;
        startX = endX = touchX;
        startY = endY = touchY;
        plungerPosition = new Rect(player.getPosition().left + plungerOffsetX, player.getPosition().top,
                player.getPosition().left + plungerOffsetX + plungerSize, player.getPosition().top + plungerSize);
        pivotX = plungerPosition.centerX();
        pivotY = plungerPosition.centerY();
        setPoints();
        white = new Paint();
        white.setColor(Color.WHITE);
    }

    void draw(Canvas canvas) {
        canvas.rotate((float) Math.toDegrees(-angle), (float) (Game.scaleX(pivotX)), (float) (Game.scaleY(pivotY)));
        canvas.drawBitmap(plungerSprites, null, Game.scaleRect(plungerPosition), null);
        canvas.rotate((float) Math.toDegrees(angle), (float) (Game.scaleX(pivotX)), (float) (Game.scaleY(pivotY)));

        //draw arc
        if (!fired) {
            for (double i = 5.0 / power; i < 50; i += 5) {
                canvas.drawCircle((float) (pivotX + power * plungerMaxSpeed * i * Math.cos(angle)),
                        (float) (pivotY - (power * plungerMaxSpeed * i * Math.sin(angle) + gravity * Math.pow(i, 2) / 2.0)),
                        (float) ((70 - i) / 5.0), white);
            }
        }
    }

    void update() {
        offSetPosition((int) velX, (int) -velY);
        rotate(angle - prevAngle);

        prevAngle = angle;
        if (!fired) {
            velX = player.getPosition().left - plungerPosition.left + plungerOffsetX;
            velY = plungerPosition.top - player.getPosition().top;

            if (startX == endX) aim = 0;
            else aim = (float) Math.atan((endY - startY) / (startX - endX));
            aim = (float) Math.min(aim, Math.PI / 2.1);
            aim = (float) Math.max(aim, -Math.PI / 2.1);
            if (startX < endX) aim += Math.PI;
            angle = aim;

            power = Math.hypot(startX - endX, startY - endY) / 200;
            power = Math.min(power, 1);
            power = Math.max(power, .5);
        } else {
            if (sticking) {
                double dTheta = snapToAngle - angle;
                dTheta = dTheta > Math.PI ? dTheta - 2 * Math.PI : dTheta;
                dTheta = dTheta < -Math.PI ? dTheta + 2 * Math.PI : dTheta;

                angle += dTheta / 3.0;
                if (Math.abs(dTheta) < 1E-3) angle = snapToAngle;
            } else {
                velX = plungerSpeed * Math.cos(aim);
                velY = Math.max((plungerSpeed * Math.sin(aim) + gravity * airTime++), -20);
                angle = (float) Math.atan(velY / velX);
                if (velX < 0) angle += Math.PI;
            }
        }
    }

    void fire() {
        fired = true;
        plungerSpeed = power * plungerMaxSpeed;
    }

    void setEnd(float endX, float endY) {
        this.endX = endX;
        this.endY = endY;
    }

    @Override
    Rect getPosition() {
        return plungerPosition;
    }

    @Override
    void offSetPosition(int dX, int dY) {
        plungerPosition.offset(dX, dY);
        for (int i = 0; i < points.length; i++) {
            if (i % 2 == 0) points[i] += dX;
            else points[i] += dY;
        }
        pivotX += dX;
        pivotY += dY;
    }

    private void rotate(float dTheta) {
        Matrix rotation = new Matrix();
        rotation.setRotate((float) Math.toDegrees(-dTheta), pivotX, pivotY);
        rotation.mapPoints(points);
    }

    @Override
    void collide(PointF normal) {
        if (collided) snapToAngle = angle;  //secondary collision
        else {
            sticking = true;

            if (normal.x == 0 && normal.y < 0) snapToAngle = (float) (-Math.PI / 2);
            else if (normal.x == 0 && normal.y > 0) snapToAngle = (float) (Math.PI / 2);
            else this.snapToAngle = (float) -Math.atan(normal.y / normal.x);

            if (normal.x > 0) snapToAngle += Math.PI;

            float tempAngle = angle;
            if (tempAngle < snapToAngle) tempAngle += 2 * Math.PI;
            else if (tempAngle > snapToAngle + 2 * Math.PI) tempAngle -= 2 * Math.PI;

            if (tempAngle > snapToAngle + Math.PI) changePivot(points[0], points[1]); //CCW
            else changePivot(points[2], points[3]); //CW

            velX = velY = 0;
        }
    }

    @Override
    void setPoints() {
        points = new float[]{plungerPosition.right, plungerPosition.centerY() - plungerSize / 8.0f,
                plungerPosition.right, plungerPosition.centerY() + plungerSize / 8.0f,
                plungerPosition.right - plungerSize / 3.0f, plungerPosition.centerY()};
    }

    private void changePivot(float pivotX, float pivotY) {
        if (this.pivotX != pivotX || this.pivotY != pivotY) {
            float[] translate = new float[]{plungerPosition.left, plungerPosition.top};
            Matrix rotation = new Matrix();
            rotation.setRotate((float) Math.toDegrees(-angle), this.pivotX, this.pivotY);
            rotation.postRotate((float) Math.toDegrees(angle), pivotX, pivotY);
            rotation.mapPoints(translate);

            plungerPosition.offsetTo((int) translate[0], (int) translate[1]);
            this.pivotX = pivotX;
            this.pivotY = pivotY;
        }
    }

    @Override
    float[] getBounds() {
        return points;
    }

    boolean outOfPlay() {
        return plungerPosition.top > Game.cameraFrame.bottom || plungerPosition.right < Game.cameraFrame.left;
    }

    boolean collisionsEnabled() {
        return fired && !(sticking && angle == snapToAngle);
    }

    boolean isSticking() {
        return sticking;
    }

    void hasCollided() {
        collided = true;
    }
}
