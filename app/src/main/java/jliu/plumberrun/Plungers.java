package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

import java.util.ArrayList;

class Plungers {
    private final Bitmap plungerSprite;
    private final Player player;
    private ArrayList<Plunger> plungers;
    private static final int plungerHeight = Constants.plungerHeight;
    private final double plungerMaxSpeed = Constants.plungerSpeed;
    private final double gravity = Constants.projectileGravity;
    private final Paint white, opacity;

    Plungers(Bitmap plungerSprite, Player player) {
        this.plungerSprite = plungerSprite;
        this.player = player;
        plungers = new ArrayList<>();
        white = new Paint();
        white.setColor(Color.WHITE);
        opacity = new Paint();
        opacity.setAlpha(80);
    }

    void drawPlungers(Canvas canvas) {
        //do not use for each loop (concurrent modification exception)
        for (int i = 0; i < plungers.size(); i++) {
            plungers.get(i).draw(canvas);
        }
    }

    void updatePlungers() {
        for (int i = 0; i < plungers.size(); i++) {
            if (plungers.get(i).outOfPlay())
                plungers.remove(i--);
            else {
                plungers.get(i).update();
            }
        }
    }

    void addPlunger(float touchX, float touchY) {
        plungers.add(0, new Plunger(touchX, touchY));
    }

    void clearList() {
        plungers.clear();
    }

    boolean reloaded() {
        return plungers.size() == 0 || plungers.get(0).hasFired();
    }

    ArrayList<Plunger> getList() {
        return plungers;
    }

    class Plunger extends CollisionObject {
        private final float startX, startY;
        private float endX, endY;
        private double velX, velY;
        private Rect plungerPosition;
        private Point plungerOffset;   //offset plunger to player's hand
        private double plungerSpeed = 0;   //current plunger speed
        private float aim;
        private float angle;
        private float snapToAngle;
        private double power = 1;   //pull back more for more power; [.5, 1]
        private int airTime = 0;
        private boolean canFire = true, fired = false, sticking = false, collided = false, falling = false;
        private float pivotX, pivotY;  //image rotation pivot coordinates
        private float[] bounds;  //bounding points

        Plunger(float touchX, float touchY) {
            startX = endX = touchX;
            startY = endY = touchY;
            plungerOffset = new Point(-plungerHeight / 3, -plungerHeight / 5);
            plungerPosition = new Rect(player.getPosition().left, player.getPosition().top,
                    player.getPosition().left + plungerHeight, player.getPosition().top + plungerHeight);
            plungerPosition.offset(plungerOffset.x, plungerOffset.y);
            pivotX = plungerPosition.centerX();
            pivotY = plungerPosition.centerY();
            setBounds();
        }

        void draw(Canvas canvas) {
            canvas.rotate((float) Math.toDegrees(-angle), pivotX, pivotY);
            if (canFire || fired) canvas.drawBitmap(plungerSprite, null, plungerPosition, null);
            else canvas.drawBitmap(plungerSprite, null, plungerPosition, opacity);
            canvas.rotate((float) Math.toDegrees(angle), pivotX, pivotY);

            //draw arc
            if (!fired) {
                for (double i = 5.0 / power; i < 50; i += 5) {
                    double trailRadius = Constants.plungerTrailRadius;
                    canvas.drawCircle((float) (pivotX + power * plungerMaxSpeed * i * Math.cos(angle)),
                            (float) (pivotY - (power * plungerMaxSpeed * i * Math.sin(angle) + gravity * Math.pow(i, 2) / 2.0)),
                            (float) (trailRadius * (50 - i) / 5.0), white);
                }
            }
        }

        void update() {
            float prevAngle = angle;
            if (!fired) {
                velX = (player.getPosition().left + plungerOffset.x) - plungerPosition.left;
                velY = plungerPosition.top - (player.getPosition().top + plungerOffset.y);

                if (startX == endX) aim = 0;
                else aim = (float) Math.atan((endY - startY) / (startX - endX));
                aim = (float) Math.min(aim, Math.PI / 2.1);
                aim = (float) Math.max(aim, -Math.PI / 2.1);
                if (startX < endX) {
                    aim += Math.PI;
                    player.flip(true);
                    plungerOffset.x = Math.abs(plungerOffset.x);
                } else {
                    player.flip(false);
                    plungerOffset.x = -Math.abs(plungerOffset.x);
                }
                angle = aim;

                power = Math.hypot(startX - endX, startY - endY) / (Game.cameraFrame.right / 8.0);
                power = Math.min(power, 1);
                power = Math.max(power, .5);
            } else {
                if (sticking) {
                    double dTheta = snapToAngle - angle;
                    dTheta = dTheta > Math.PI ? dTheta - 2 * Math.PI : dTheta;
                    dTheta = dTheta < -Math.PI ? dTheta + 2 * Math.PI : dTheta;

                    angle += dTheta / 3.0;
                    if (Math.abs(dTheta) < .05) angle = snapToAngle;
                } else {
                    velX = plungerSpeed * Math.cos(aim);
                    velY = Math.max((plungerSpeed * Math.sin(aim) + gravity * airTime++), -plungerMaxSpeed / 2);
                    //angle unchanged when falling
                    angle = plungerSpeed == 0 ? angle : (float) Math.atan(velY / velX);
                    if (velX < 0) angle += Math.PI;
                }
            }

            canFire = true;

            offSetPosition((int) velX, (int) -velY);
            rotate(angle - prevAngle);
        }

        @Override
        void setBounds() {
            bounds = new float[]{plungerPosition.right - 5, plungerPosition.centerY() - plungerHeight * .125f,
                    plungerPosition.right - 5, plungerPosition.centerY() + plungerHeight * .125f,
                    plungerPosition.right - plungerHeight * .333f, plungerPosition.centerY()};
        }

        @Override
        float[] getBounds() {
            return bounds;
        }

        @Override
        void offSetPosition(int dX, int dY) {
            plungerPosition.offset(dX, dY);
            for (int i = 0; i < bounds.length; i++) {
                if (i % 2 == 0) bounds[i] += dX;
                else bounds[i] += dY;
            }
            pivotX += dX;
            pivotY += dY;
        }

        @Override
        Rect getPosition() {
            return plungerPosition;
        }

        private void rotate(float dTheta) {
            Matrix rotation = new Matrix();
            rotation.setRotate((float) Math.toDegrees(-dTheta), pivotX, pivotY);
            rotation.mapPoints(bounds);
        }

        @Override
        void collide(PointF normal) {
            if (fired) {
                offSetPosition((int) normal.x, (int) -normal.y);
                if (collided)
                    snapToAngle = angle;  //secondary collision
                else {
                    sticking = true;

                    if (normal.x == 0 && normal.y > 0) snapToAngle = (float) (-Math.PI / 2);
                    else if (normal.x == 0 && normal.y < 0) snapToAngle = (float) (Math.PI / 2);
                    else this.snapToAngle = (float) Math.atan(normal.y / normal.x);

                    if (normal.x > 0) snapToAngle += Math.PI;

                    float tempAngle = angle;
                    if (tempAngle < snapToAngle) tempAngle += 2 * Math.PI;
                    else if (tempAngle > snapToAngle + 2 * Math.PI) tempAngle -= 2 * Math.PI;

                    if (tempAngle > snapToAngle + Math.PI) changePivot(bounds[0], bounds[1]); //CCW
                    else changePivot(bounds[2], bounds[3]); //CW

                    velX = velY = 0;
                }
            } else {
                canFire = false;
            }
        }

        void fire() {
            if (canFire) {
                fired = true;
                plungerSpeed = power * plungerMaxSpeed;
            } else fall();
        }

        private void fall() {
            fired = true;
            sticking = false;
            falling = true;
            plungerSpeed = 0;
        }

        void setEnd(float endX, float endY) {
            this.endX = endX;
            this.endY = endY;
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

        boolean outOfPlay() {
            return plungerPosition.centerY() > Game.cameraFrame.bottom + plungerHeight
                    || plungerPosition.centerX() < Game.cameraFrame.left - plungerHeight;
        }

        boolean tileCollisionsEnabled() {
            return !falling && !(sticking && angle == snapToAngle);
        }

        boolean isSticking() {
            return sticking;
        }

        void hasCollided() {
            collided = true;
        }

        boolean hasFired() {
            return fired;
        }
    }
}