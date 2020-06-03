package jliu.plumberrun;

import android.graphics.PointF;
import android.graphics.Rect;

class Tile extends CollisionObject {
    private int tileID;
    private int posX, posY;
    private float[] bounds;
    static final int tileSize = 80;

    Tile(int tileID, int posX, int posY) {
        this.tileID = tileID;
        this.posX = posX / tileSize * tileSize;
        this.posY = posY / tileSize * tileSize;
        setBounds();
    }

    @Override
    void setBounds() {
        if (tileID <= 7)  //full tile
            bounds = new float[]{posX, posY, posX + tileSize, posY, posX + tileSize, posY + tileSize, posX, posY + tileSize};
        else if (tileID == 8) //slope up
            bounds = new float[]{posX, posY + tileSize, posX + tileSize, posY, posX + tileSize, posY + tileSize};
        else if (tileID == 9)   //slope down
            bounds = new float[]{posX, posY, posX + tileSize, posY + tileSize, posX, posY + tileSize};
        else if (tileID == 10)   //rounded left
            bounds = new float[]{posX, posY, posX + tileSize, posY, posX + tileSize, posY + tileSize,
                    posX + .25f * tileSize, posY + .75f * tileSize};
        else if (tileID == 11)   //rounded right
            bounds = new float[]{posX, posY, posX + tileSize, posY, posX + .75f * tileSize, posY + .75f * tileSize,
                    posX + tileSize, posY + tileSize};
        else if (tileID <= 15)  //half tile
            bounds = new float[]{posX, posY, posX + tileSize, posY, posX + tileSize, posY + .5f * tileSize, posX, posY + .5f * tileSize};
    }

    @Override
    float[] getBounds() {
        return bounds;
    }

    @Override
    void offSetPosition(int dX, int dY) {

    }

    @Override
    Rect getPosition() {
        return new Rect(posX, posY, posX + tileSize, posY + tileSize);
    }

    @Override
    void collide(PointF normal) {

    }
}
