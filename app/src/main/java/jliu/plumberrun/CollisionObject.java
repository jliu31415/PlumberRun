package jliu.plumberrun;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;

abstract class CollisionObject {

    abstract void setPoints();

    abstract float[] getBounds();

    abstract Rect getPosition();

    abstract void offSetPosition(int dX, int dY);

    abstract void collide(PointF normal);

    void drawPoints(Canvas canvas) {
        Paint white = new Paint();
        white.setColor(Color.WHITE);
        for (int i = 0; i < this.getBounds().length; i += 2) {
            canvas.drawCircle(this.getBounds()[i], this.getBounds()[i + 1], 10, white);
        }
    }

}
