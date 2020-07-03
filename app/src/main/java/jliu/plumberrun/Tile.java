package jliu.plumberrun;

import android.graphics.PointF;
import android.graphics.Rect;


class Tile extends CollisionObject {
    private float[] bounds;
    private final int tileSize = Constants.tileSize;
    private int tileID;
    private int posX, posY;

    Tile(int tileID, int posX, int posY) {
        this.tileID = tileID;
        this.posX = posX / tileSize * tileSize;
        this.posY = posY / tileSize * tileSize;
        setBounds();
    }

    @Override
    void setBounds() {
        if (tileID > 0 && tileID <= 7)  //full tile
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
        else throw new IllegalArgumentException("Invalid Tile ID");
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

    int getTileID() {
        return tileID;
    }

    static String getFlushAttribute(int tileID) {
        //encoding starting from top edge, CW
        //0 = no edge, 1 = full edge, 2 = incomplete edge
        if (tileID > 0 && tileID <= 7)  //full tile
            return "1111";
        else if (tileID == 8) //slope up
            return "0110";
        else if (tileID == 9)   //slope down
            return "0011";
        else if (tileID == 10)   //rounded left
            return "1100";
        else if (tileID == 11)   //rounded right
            return "1001";
        else if (tileID <= 15)  //half tile
            return "0202";
        return "";
    }

    static boolean isTile(int tileID) {
        return 0 < tileID && tileID < 16;
    }
}
