package jliu.plumberrun;

import android.graphics.Point;
import android.graphics.Rect;

abstract class CollisionObject {

    abstract void setPoints();

    abstract float[] getBounds();

    abstract Rect getPosition();

    abstract void offSetPosition(int dX, int dY, float dTheta);

    abstract void collide(Point offset);

}
