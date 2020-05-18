package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

class Plunger extends CollisionObject {
    private final Bitmap plunger;
    private final Player player;
    private final float startX, startY;
    private float endX, endY;
    private double velX, velY;
    private Rect plungerPosition;
    private final int plungerOffsetX = -80;   //offset plunger to player's hand
    private static final int imageSize = Tile.tileSize * 2;
    private final double plungerMaxSpeed = 30;
    private double plungerSpeed = 0;   //current plunger speed
    private float aim, angle, prevAngle, snapToAngle; //angle follows parabolic arc when fired
    private double power = 1;   //pull back more for more power; [.5, 1]
    private final double gravity = -.3;
    private int airTime = 0;
    private boolean fired = false, sticking = false;
    private float pivotX, pivotY;  //image rotation pivot coordinates
    private float[] points;  //bounding points
    private final Paint white;

    Plunger(Bitmap plunger, Player player, float touchX, float touchY) {
        this.plunger = plunger;
        this.player = player;
        startX = endX = touchX;
        startY = endY = touchY;
        plungerPosition = new Rect(player.getPosition().left + plungerOffsetX, player.getPosition().top,
                player.getPosition().left + plungerOffsetX + imageSize, player.getPosition().top + imageSize);
        pivotX = plungerPosition.centerX();
        pivotY = plungerPosition.centerY();
        setPoints();
        white = new Paint();
        white.setColor(Color.WHITE);
    }

    void draw(Canvas canvas) {
        canvas.rotate((float) Math.toDegrees(-angle), (float) (Game.scaleX(pivotX)), (float) (Game.scaleY(pivotY)));
        canvas.drawBitmap(plunger, null, Game.scaleRect(plungerPosition), null);
        canvas.rotate((float) Math.toDegrees(angle), (float) (Game.scaleX(pivotX)), (float) (Game.scaleY(pivotY)));

        //draw arc
        if (!fired) {
            for (int i = 10; i < 70; i += 10) {
                canvas.drawCircle((float) (pivotX + power * plungerMaxSpeed * i * Math.cos(angle)),
                        (float) (pivotY - (power * plungerMaxSpeed * i * Math.sin(angle) + gravity * Math.pow(i, 2) / 2.0)),
                        (70 - i) / 5.0f, white);
            }
        }
    }

    void update() {
        prevAngle = angle;

        if (!fired) {
            velX = player.getPosition().left - plungerPosition.left + plungerOffsetX;
            velY = plungerPosition.top - player.getPosition().top;

            power = Math.hypot(startX - endX, startY - endY) / 200;
            power = Math.min(power, 1);
            power = Math.max(power, .5);

            if (power == .5)
                angle = aim = (float) (Math.PI / 3);
            else {
                aim = (float) Math.atan((endY - startY) / (startX - endX));
                aim = (float) Math.min(aim, Math.PI / 2.1);
                aim = (float) Math.max(aim, -Math.PI / 2.1);
                angle = aim;
            }
        } else {
            if (sticking) {
                angle += (snapToAngle - angle) / 3.0f;
                if (Math.abs(snapToAngle - angle) < 1E-3) angle = snapToAngle;
            } else {
                velX = plungerSpeed * Math.cos(aim);
                velY = Math.max((plungerSpeed * Math.sin(aim) + gravity * airTime++), -20);
                angle = (float) Math.atan(velY / velX);
            }
        }

        offSetPosition((int) velX, (int) -velY);
        rotate(angle - prevAngle);
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
        Log.d("debug", normal.toString());
        sticking = true;
        velX = velY = 0;

        if (normal.x == 0 && normal.y > 0) snapToAngle = (float) (-Math.PI / 2);
        else if (normal.x == 0 && normal.y < 0) snapToAngle = (float) (Math.PI / 2);
        else this.snapToAngle = (float) Math.atan(-normal.y / normal.x);

        if (angle < snapToAngle) changePivot(points[0], points[1]);
        else changePivot(points[2], points[3]);
    }

    @Override
    void setPoints() {
        points = new float[]{plungerPosition.right, plungerPosition.centerY() - imageSize / 8.0f,
                plungerPosition.right, plungerPosition.centerY() + imageSize / 8.0f,
                plungerPosition.right - imageSize / 3.0f, plungerPosition.centerY()};
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

    boolean tileCollisionsEnabled() {
        return fired && !(sticking && angle == snapToAngle);
    }
}
