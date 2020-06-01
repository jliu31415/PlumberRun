package jliu.plumberrun;

import android.graphics.PointF;
import android.graphics.Rect;

abstract class CollisionObject {

    abstract void setBounds();

    abstract float[] getBounds();

    abstract void offSetPosition(int dX, int dY);

    abstract Rect getPosition();

    abstract void collide(PointF normal);

}
