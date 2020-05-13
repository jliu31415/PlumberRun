package jliu.plumberrun;

public class Tile extends CollisionObject {
    private int tileID;
    private int posX, posY;
    private float[] points;
    static final int tileSize = 80;

    Tile(int tileID, double posX, double posY) {
        this.tileID = tileID;
        this.posX = (int) posX / tileSize * tileSize;
        this.posY = (int) posY / tileSize * tileSize;
        setPoints();
    }

    @Override
    public void setPoints() {
        if (tileID < 6 || true)  //full tile
            points = new float[]{posX, posY, posX + tileSize, posY, posX + tileSize, posY + tileSize, posX, posY + tileSize, posX, posY};
        else if (tileID < 8) //rounded tile
            points = new float[0];
        else if (tileID < 10)   //slope
            points = new float[0];
        else if (tileID < 12)   //slope connector
            points = new float[0];
        else if (tileID < 16)    //half tile
            points = new float[0];
    }

    @Override
    public float[] getBounds() {
        return points;
    }

    @Override
    public void offSetPosition(int dX, int dY, float dTheta) {

    }

    @Override
    public double getVelX() {
        return 0;
    }

    @Override
    public double getVelY() {
        return 0;
    }
}
