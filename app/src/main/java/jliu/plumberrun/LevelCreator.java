package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;

import java.util.ArrayList;

class LevelCreator {
    private final ArrayList<ArrayList<Integer[]>> allLevels;
    private ArrayList<Integer[]> level;
    private int levelID;
    private final Bitmap tileSpriteSheet;
    private Rect tileSprite;
    private Rect tilePosition;
    private final int tileSpriteSheetWidth, tileSpriteSheetHeight;
    private static final int tileSize = Tile.tileSize;

    LevelCreator(ArrayList<ArrayList<Integer[]>> allLevels, Bitmap tiles_platform) {
        this.allLevels = allLevels;
        tileSpriteSheet = tiles_platform;
        tileSpriteSheetWidth = tileSpriteSheet.getWidth() / 5;
        tileSpriteSheetHeight = tileSpriteSheet.getHeight() / 3;
        tileSprite = new Rect(0, 0, tileSpriteSheetWidth, tileSpriteSheetHeight);
        tilePosition = new Rect(0, 0, tileSize, tileSize);
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
                    framePosX = tileSpriteSheetWidth * (tileID % 5);
                    framePosY = tileSpriteSheetHeight * (tileID / 5);
                    tileSprite.offsetTo(framePosX, framePosY);
                    tilePosition.offsetTo(col * tileSize, row * tileSize);
                    canvas.drawBitmap(tileSpriteSheet, tileSprite, Game.scaleRect(tilePosition), null);
                }
            }
        }
    }

    void updateCollisions(CollisionObject collisionObject1, CollisionObject collisionObject2) {
        float[] points1 = collisionObject1.getBounds();
        float[] points2 = collisionObject2.getBounds();
        PointF offset1 = getProjectionOffset(points2, points1);
        PointF offset2 = getProjectionOffset(points1, points2);

        if (offset1 != null && offset2 != null) {
            if ((collisionObject1.getPosition().centerX() - collisionObject2.getPosition().centerX()) * offset1.x < 0) {
                offset1.negate();
            } else if ((collisionObject1.getPosition().centerY() - collisionObject2.getPosition().centerY()) * offset1.y > 0) {
                offset1.negate();
            }

            if (offset1.x != 0)
                offset1.x = (int) (Math.ceil(Math.abs(offset1.x))) * (offset1.x / Math.abs(offset1.x));
            if (offset1.y != 0)
                offset1.y = (int) (Math.ceil(Math.abs(offset1.y))) * (offset1.y / Math.abs(offset1.y));

            collisionObject1.collide(offset1);
            collisionObject1.offSetPosition((int) offset1.x, (int) -offset1.y);
        }
    }

    //projection onto points1 polygon; minimum offset returned as a Points vector
    private PointF getProjectionOffset(float[] points1, float[] points2) {
        PointF ret = new PointF();
        PointF edge = new PointF();
        float overlap, globalOverlap = Float.POSITIVE_INFINITY;
        float dotProjection;

        for (int i = 0; i < points1.length; i += 2) {
            float minProjection1 = Float.POSITIVE_INFINITY;
            float maxProjection1 = Float.NEGATIVE_INFINITY;
            float minProjection2 = Float.POSITIVE_INFINITY;
            float maxProjection2 = Float.NEGATIVE_INFINITY;

            edge.set(points1[(i + 2) % points1.length] - points1[i], points1[(i + 3) % points1.length] - points1[i + 1]);
            edge.x /= Math.hypot(edge.x, edge.y);
            edge.y /= Math.hypot(edge.x, edge.y);

            for (int j = 0; j < points1.length; j += 2) {
                dotProjection = -edge.y * points1[j] + edge.x * points1[j + 1];
                minProjection1 = Math.min(minProjection1, dotProjection);
                maxProjection1 = Math.max(maxProjection1, dotProjection);
            }

            for (int j = 0; j < points2.length; j += 2) {
                dotProjection = -edge.y * points2[j] + edge.x * points2[j + 1];
                minProjection2 = Math.min(minProjection2, dotProjection);
                maxProjection2 = Math.max(maxProjection2, dotProjection);
            }

            overlap = Math.min(maxProjection1, maxProjection2) - Math.max(minProjection1, minProjection2);
            if (overlap <= 0) return null;
            if (overlap < globalOverlap) {
                ret.set(overlap * -edge.y, overlap * edge.x);
                globalOverlap = overlap;
            }
        }

        return ret;
    }

    ArrayList<Tile> getSurroundingTiles(float[] points) {
        ArrayList<Tile> surroundingTiles = new ArrayList<>();
        int minX = (int) points[0], maxX = (int) points[0], minY = (int) points[1], maxY = (int) points[1];
        for (int i = 2; i < points.length; i++) {
            if (i % 2 == 0) {
                minX = (int) Math.min(minX, points[i]);
                maxX = (int) Math.max(maxX, points[i]);
            } else {
                minY = (int) Math.min(minY, points[i]);
                maxY = (int) Math.max(maxY, points[i]);
            }
        }

        int coordinateX = minX, coordinateY = minY;   //border coordinates
        do {
            try {
                int tileID = level.get(coordinateX / tileSize)[coordinateY / tileSize];
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
