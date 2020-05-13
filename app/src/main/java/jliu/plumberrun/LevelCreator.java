package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;

import java.util.ArrayList;

class LevelCreator {
    private final ArrayList<ArrayList<Integer[]>> allLevels;
    private ArrayList<Integer[]> level;
    private int levelID;
    private final Bitmap tileSprite;
    private final int tileSpriteWidth, tileSpriteHeight;
    private static final int tileSize = Tile.tileSize;

    LevelCreator(ArrayList<ArrayList<Integer[]>> allLevels, Bitmap tiles_platform) {
        this.allLevels = allLevels;
        tileSprite = tiles_platform;
        tileSpriteWidth = tileSprite.getWidth() / 5;
        tileSpriteHeight = tileSprite.getHeight() / 3;
    }

    int getCurrentLevel() {
        return levelID;
    }

    void createLevel(int levelID) {
        this.levelID = levelID;
        level = allLevels.get(levelID);
    }

    void draw(Canvas canvas) {
        int framePosX, framePosY;
        for (int col = Game.cameraFrame.left / tileSize; col < Game.cameraFrame.left / tileSize + level.size(); col++) {
            for (int row = 0; row < level.get(col).length; row++) {
                int tileID = level.get(col)[row];
                if (tileID-- != 0) {
                    framePosX = tileSpriteWidth * (tileID % 5);
                    framePosY = tileSpriteHeight * (tileID / 5);
                    Bitmap tile = Bitmap.createBitmap(tileSprite, framePosX, framePosY, tileSpriteWidth, tileSpriteHeight);
                    Rect tilePosition = new Rect(col * tileSize, row * tileSize, (col + 1) * tileSize, (row + 1) * tileSize);
                    canvas.drawBitmap(tile, null, Game.scaleRect(tilePosition), null);
                }
            }
        }
    }

    void updateCollisions(CollisionObject collisionObject1, CollisionObject collisionObject2) {
        float[] points1 = collisionObject1.getBounds();
        float[] points2 = collisionObject2.getBounds();
        double collisionAngle;
        for (int i = 0; i < points1.length - 4; i += 2) {
            for (int j = 0; j < points2.length - 4; j += 2) {
                Point intersection = intersectingLines(points1[i], points1[i + 1], points1[i + 2], points1[i + 3],
                        points2[j], points2[j + 1], points2[j + 2], points2[j + 3]);
                if (intersection != null) {
                    //collisionObject1.offSetPosition((int) -collisionObject1.getVelX(), (int) collisionObject1.getVelY(), 0);
                }
            }
        }
    }

    //return point of intersection of AB and CD
    private Point intersectingLines(double aX, double aY, double bX, double bY, double cX, double cY, double dX, double dY) {
        // Line AB represented as a1x + b1y = c1
        double a1 = bY - aY;
        double b1 = aX - bX;
        double c1 = a1 * aX + b1 * aY;

        // Line CD represented as a2x + b2y = c2
        double a2 = dY - cY;
        double b2 = cX - dX;
        double c2 = a2 * cX + b2 * cY;

        double x, y;
        double determinant = a1 * b2 - a2 * b1;

        if (determinant == 0) return null;  //lines are parallel
        else {
            x = (b2 * c1 - b1 * c2) / determinant;
            y = (a1 * c2 - a2 * c1) / determinant;
        }

        if (Math.min(aX, bX) <= x && x <= Math.max(aX, bX)) {
            if (Math.min(aY, bY) <= y && y <= Math.max(aY, bY))
                return new Point((int) x, (int) y);
        }

        return null;
    }

    ArrayList<Tile> getSurroundingTiles(float[] points) {
        ArrayList<Tile> surroundingTiles = new ArrayList<>();
        float minX = points[0], maxX = points[0], minY = points[1], maxY = points[1];
        for (int i = 2; i < points.length; i++) {
            if (i % 2 == 0) {
                minX = Math.min(minX, points[i]);
                maxX = Math.max(maxX, points[i]);
            } else {
                minY = Math.min(minY, points[i]);
                maxY = Math.max(maxY, points[i]);
            }
        }
        int loop = 0;
        float coordinateX = minX, coordinateY = minY;   //border coordinates
        do {
            try {
                int tileID = level.get((int) coordinateX / tileSize)[(int) coordinateY / tileSize];
                if (tileID > 0)
                    surroundingTiles.add(new Tile(tileID, coordinateX, coordinateY));
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }

            if (coordinateX < maxX && coordinateY == minY) coordinateX += tileSize;
            else if (coordinateX >= maxX && coordinateY < maxY) coordinateY += tileSize;
            else if (coordinateX > minX) coordinateX -= tileSize;
            else coordinateY -= tileSize;
        } while (coordinateX != minX || coordinateY != minY);

        return surroundingTiles;
    }
}
