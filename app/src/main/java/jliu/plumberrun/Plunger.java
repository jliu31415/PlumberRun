package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

class Plunger {
    private final Bitmap plunger;
    private final Player player;
    private final LevelCreator levelCreator;
    private final double startX, startY;
    private double endX, endY;
    private int velX, velY;
    private Rect spritePosition;
    private final int plungerOffsetX = -80;   //have plunger in player's hand
    private final int imageSize = 138;  //same size as player; sprite not cropped
    private final int plungerMaxSpeed = 35;
    private int plungerSpeed = 0;   //current plunger speed
    private double initialAngle, angle; //angle follows parabolic arc when fired
    private final double gravity = -.5;
    private int airTime = 0;
    private boolean fired = false, stick = false, fall = false;
    private float[] points, rotatedPoints;  //used for hitBox

    public Plunger(Bitmap plunger, Player player, LevelCreator levelCreator, double startX, double startY) {
        this.plunger = plunger;
        this.player = player;
        this.levelCreator = levelCreator;
        this.startX = startX;
        this.startY = startY;
        spritePosition = new Rect(player.getPosX() + plungerOffsetX, player.getPosY(),
                player.getPosX() + plungerOffsetX + imageSize, player.getPosY() + imageSize);
        points = new float[6];
        rotatedPoints = new float[6];
        setPoints();
    }

    public void draw(Canvas canvas) {
        angle = fired ? Math.atan(Math.tan(initialAngle) + (gravity * airTime) / (plungerSpeed * Math.cos(initialAngle))) : initialAngle;
        canvas.rotate((float) Math.toDegrees(-angle), spritePosition.centerX(), spritePosition.centerY());
        canvas.drawBitmap(plunger, null, Game.scale(spritePosition), null);
        canvas.rotate((float) Math.toDegrees(angle), spritePosition.centerX(), spritePosition.centerY());
        setBoundingPoints(canvas);
    }

    public void update() {
        levelCreator.updateCollisions(this);
        if (!fired)
            spritePosition.offsetTo(player.getPosX() + plungerOffsetX, player.getPosY());
        else {
            if (stick) {

            } else if (fall && false) {

            } else {
                velX = (int) (plungerSpeed * Math.cos(initialAngle));
                velY = -Math.min((int) (plungerSpeed * Math.sin(initialAngle) + gravity * airTime++), 30);
                spritePosition.offset(velX, velY);
            }
        }
    }

    public void fire() {
        fired = true;
        plungerSpeed = plungerMaxSpeed;
    }

    public void drawArc(Canvas canvas, Paint white) {
        if (startX <= endX) {
            if (startY < endY) initialAngle = Math.PI / 2.0;
            else initialAngle = -Math.PI / 2.0;
        } else {
            initialAngle = Math.atan((endY - startY) / (startX - endX));
        }

        for (int i = 5; i <= 50; i += 5) {
            canvas.drawCircle((float) (spritePosition.centerX() + plungerMaxSpeed * i * Math.cos(initialAngle)),
                    (float) (spritePosition.centerY() - (plungerMaxSpeed * i * Math.sin(initialAngle) + gravity * Math.pow(i, 2) / 2.0)),
                    (55 - i) / 5, white);
        }
    }

    public void stick() {
        stick = true;
    }

    public boolean getStick() {
        return stick;
    }

    public void fall() {
        stick = false;
        fall = true;
    }

    public void setEnd(double endX, double endY) {
        this.endX = endX;
        this.endY = endY;
    }

    public void setBoundingPoints(Canvas canvas) {
        Matrix rotation = new Matrix();
        rotation.setRotate((float) -Math.toDegrees(angle), points[0], points[1]);   //represents center of image
        rotation.postTranslate(spritePosition.centerX() - points[0], spritePosition.centerY() - points[1]);
        rotation.mapPoints(rotatedPoints, points);
        Paint black = new Paint();
        black.setColor(Color.BLACK);
        for (int i = 0; i < points.length; i += 2) {
            canvas.drawCircle(rotatedPoints[i], rotatedPoints[i + 1], 10, black);
        }
    }

    public void setPoints() {
        points[0] = spritePosition.centerX();
        points[2] = spritePosition.right;
        points[4] = spritePosition.right;

        points[1] = spritePosition.centerY();
        points[3] = spritePosition.centerY() - imageSize / 8;
        points[5] = spritePosition.centerY() + imageSize / 8;
    }

    public float[] getBoundingPoints() {
        return rotatedPoints;
    }

    public Rect getSpritePosition() {
        return spritePosition;
    }
}
