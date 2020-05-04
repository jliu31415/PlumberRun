package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

class Plunger {
    private final Bitmap plunger;
    private final Player player;
    private final float startX, startY;
    private float endX, endY;
    private Rect spritePosition;
    private final int plungerOffsetX = -80;   //have plunger in player's hand
    private final int imageSize = 138;  //same size as player; sprite not cropped
    private final int plungerMaxSpeed = 50;
    private int plungerSpeed = 0;   //current plunger speed
    private double initialAngle, angle; //angle follows parabolic arc when fired
    private double gravity = -1.5;
    private int airTime = 0;
    private boolean fired = false;

    public Plunger(Bitmap plunger, Player player, float startX, float startY) {
        this.plunger = plunger;
        this.player = player;
        this.startX = startX;
        this.startY = startY;
        spritePosition = new Rect(player.getPosX() + plungerOffsetX, player.getPosY(),
                player.getPosX() + plungerOffsetX + imageSize, player.getPosY() + imageSize);
    }

    public void draw(Canvas canvas) {
        angle = fired ? Math.atan(Math.tan(initialAngle) + (gravity * airTime) / (plungerSpeed * Math.cos(initialAngle))) : initialAngle;
        canvas.rotate((float) Math.toDegrees(-angle), spritePosition.centerX(), spritePosition.centerY());
        canvas.drawBitmap(plunger, null, Game.scale(spritePosition), null);
        canvas.rotate((float) Math.toDegrees(angle), spritePosition.centerX(), spritePosition.centerY());
    }

    public void update() {
        if (!fired)
            spritePosition.offsetTo(player.getPosX() + plungerOffsetX, player.getPosY());
        else {
            spritePosition.offset((int) (plungerSpeed * Math.cos(initialAngle)),
                    -(int) (plungerSpeed * Math.sin(initialAngle) + gravity * airTime++));
        }
    }

    public void fire() {
        fired = true;
        plungerSpeed = plungerMaxSpeed;
    }

    public double drawArc(Canvas canvas, Paint white) {
        if (startX != endX) initialAngle = Math.atan((endY - startY) / (startX - endX));
        else initialAngle = 90;
        for (int i = 3; i < 18; i += 2) {
            canvas.drawCircle((float) (spritePosition.centerX() + plungerMaxSpeed * i * Math.cos(initialAngle)),
                    (float) (spritePosition.centerY() - (plungerMaxSpeed * i * Math.sin(initialAngle) + gravity * Math.pow(i, 2) / 2.0)),
                    10, white);
        }
        return initialAngle;
    }

    public void setEnd(float endX, float endY) {
        this.endX = endX;
        this.endY = endY;
    }
}
