package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

class Plunger {
    private final Bitmap plunger;
    private final Player player;
    private final int startX, startY;
    private final int plungerLength = 69;
    private int plungerSpeed = 0;
    private boolean fired = false;
    private Rect spritePosition;

    public Plunger(Bitmap plunger, Player player, float startX, float startY) {
        this.plunger = plunger;
        this.player = player;
        this.startX = (int) startX;
        this.startY = (int) startY;
        spritePosition = new Rect(player.getPosX(), player.getPosY(), player.getPosX() + 2 * plungerLength, player.getPosY() + plungerLength);
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(plunger, null, Game.scale(spritePosition), null);
    }

    public void update() {
        if (!fired)
            spritePosition.offsetTo(player.getPosX(), player.getPosY());
        else
            spritePosition.offset(plungerSpeed, 0);
    }

    public void fire() {
        fired = true;
        plungerSpeed = 30;
        Log.d("debug", "fire");
    }

    public void drawArc() {
        Log.d("debug", "drawArc");
    }
}
