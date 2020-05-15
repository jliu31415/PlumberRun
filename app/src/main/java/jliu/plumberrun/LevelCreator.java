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
        Point offset1 = getProjectionOffset(points1, points2);
        Point offset2 = getProjectionOffset(points2, points1);

        if (offset1 != null && offset2 != null) {
            Point offset = Math.hypot(offset1.x, offset1.y) < Math.hypot(offset2.x, offset2.y) ? offset1 : offset2;
            if ((collisionObject1.getPosition().centerX() - collisionObject2.getPosition().centerX()) * offset.x < 0) {
                offset.negate();
            } else if ((collisionObject1.getPosition().centerY() - collisionObject2.getPosition().centerY()) * offset.y > 0) {
                offset.negate();
            }
            collisionObject1.collide(offset);
            collisionObject1.offSetPosition(offset.x, -offset.y, 0);
        }
    }

    //projection onto points1 polygon; minimum offset returned as a Points vector
    private Point getProjectionOffset(float[] points1, float[] points2) {
        Point ret = new Point();
        float overlap, globalOverlap = Float.POSITIVE_INFINITY;

        for (int i = 0; i < points1.length; i += 2) {
            float dotProjection;
            float minProjection1 = Float.POSITIVE_INFINITY;
            float maxProjection1 = Float.NEGATIVE_INFINITY;
            float minProjection2 = Float.POSITIVE_INFINITY;
            float maxProjection2 = Float.NEGATIVE_INFINITY;

            float dX = points1[(i + 2) % points1.length] - points1[i];
            float dY = points1[(i + 3) % points1.length] - points1[i + 1];
            dX /= Math.hypot(dX, dY);
            dY /= Math.hypot(dX, dY);
            float normalX = -dY;
            float normalY = dX;

            for (int j = 0; j < points1.length; j += 2) {
                dotProjection = normalX * points1[j] + normalY * points1[j + 1];
                minProjection1 = Math.min(minProjection1, dotProjection);
                maxProjection1 = Math.max(maxProjection1, dotProjection);
            }

            for (int j = 0; j < points2.length; j += 2) {
                dotProjection = normalX * points2[j] + normalY * points2[j + 1];
                minProjection2 = Math.min(minProjection2, dotProjection);
                maxProjection2 = Math.max(maxProjection2, dotProjection);
            }

            overlap = Math.min(maxProjection1, maxProjection2) - Math.max(minProjection1, minProjection2);
            if (overlap <= 0) return null;
            if (overlap < globalOverlap) {
                ret.set((int) (overlap * normalX), (int) (overlap * normalY));
                globalOverlap = overlap;
            }
        }

        return ret;
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
