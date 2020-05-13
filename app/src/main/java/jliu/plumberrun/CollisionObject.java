package jliu.plumberrun;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

abstract class CollisionObject {

    abstract void setPoints();

    abstract float[] getBounds();

    abstract void offSetPosition(int dX, int dY, float dTheta);

    abstract double getVelX();

    abstract double getVelY();

    void drawPoints(Canvas canvas) {
        Paint white = new Paint();
        white.setColor(Color.WHITE);
        for (int i = 0; i < getBounds().length; i += 2)
            canvas.drawCircle(getBounds()[i], getBounds()[i + 1], 10, white);
    }
}
