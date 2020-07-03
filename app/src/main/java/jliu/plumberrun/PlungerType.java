package jliu.plumberrun;

import android.graphics.Bitmap;

class PlungerType {
    private final Bitmap plungerSprite;
    private final Player player;

    PlungerType(Bitmap plungerSprite, Player player) {
        this.plungerSprite = plungerSprite;
        this.player = player;
    }

    Plunger createPlunger(float touchX, float touchY) {
        return new Plunger(plungerSprite, player, touchX, touchY);
    }
}
