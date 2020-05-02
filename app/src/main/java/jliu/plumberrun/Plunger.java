package jliu.plumberrun;

import android.graphics.Bitmap;
import android.util.Log;

class Plunger {
    private final Bitmap plunger;
    private final Player player;
    private final int startX, startY;

    public Plunger(Bitmap plunger, Player player, float startX, float startY){
        this.plunger = plunger;
        this.player = player;
        this.startX = (int) startX;
        this.startY = (int) startY;
    }

    public void fire() {
        Log.d("debug", "fire");
    }

    public void drawArc() {
        Log.d("debug", "drawArc");
    }
}
