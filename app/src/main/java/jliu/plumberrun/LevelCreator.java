package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;

import java.util.ArrayList;

class LevelCreator {
    private final Game game;
    private final ArrayList<Integer[]> level;
    private int levelIndexOffset;
    private final Bitmap tileSprites, toilet_sprites;
    private Rect tilePosition, spriteFrame;
    private final int spriteSize; //tile spriteWidth = spriteHeight

    LevelCreator(Game game, Bitmap tileSprites, Bitmap toilet_sprites) {
        this.game = game;
        level = new ArrayList<>();
        this.tileSprites = tileSprites;
        this.toilet_sprites = toilet_sprites;
        spriteSize = tileSprites.getWidth() / 5;
        spriteFrame = new Rect(0, 0, spriteSize, spriteSize);
        tilePosition = new Rect(0, 0, Constants.tileSize, Constants.tileSize);
    }

    void initializeLevel(ArrayList<Integer[]> levelFragment) {
        level.clear();
        level.addAll(levelFragment);
        levelIndexOffset = 0;
    }

    void draw(Canvas canvas) {
        int framePosX, framePosY;

        for (int col = Game.cameraFrame.left / Constants.tileSize; col <= Game.cameraFrame.right / Constants.tileSize; col++) {
            if (col >= level.size() + levelIndexOffset) {
                addFragment();
                break;
            }
            for (int row = 0; row < level.get(col - levelIndexOffset).length; row++) {
                int id = level.get(col - levelIndexOffset)[row];
                if (0 < id && id < 16) {
                    framePosX = spriteSize * (--id % 5);
                    framePosY = spriteSize * (id / 5);
                    spriteFrame.offsetTo(framePosX, framePosY);
                    tilePosition.offsetTo(col * Constants.tileSize, row * Constants.tileSize);
                    canvas.drawBitmap(tileSprites, spriteFrame, tilePosition, null);
                } else if (id == 50) {
                    boolean create = true;
                    for (Enemy e : game.getInitializedEnemies()) {
                        if (e.getInitializedPosition().equals(col * Constants.tileSize, row * Constants.tileSize)) {
                            create = false;
                            break;
                        }
                    }
                    if (create) {
                        game.addEnemy(new Enemy(toilet_sprites, col * Constants.tileSize, row * Constants.tileSize));
                    }
                }
            }
        }
    }

    private void addFragment() {
        ArrayList<Integer[]> levelFragment = game.newFragment();
        if (level.size() + levelFragment.size() >= 128) {    //clean list
            level.subList(0, Game.cameraFrame.left / Constants.tileSize - levelIndexOffset).clear();
            levelIndexOffset = Game.cameraFrame.left / Constants.tileSize;
        }
        level.addAll(levelFragment);
    }

    boolean updateCollisions(CollisionObject collisionObject1, CollisionObject collisionObject2, boolean offsetObject) {
        float[] bounds1 = collisionObject1.getBounds();
        float[] bounds2 = collisionObject2.getBounds();
        PointF offset1 = getProjectionOffset(bounds2, bounds1);
        PointF offset2 = getProjectionOffset(bounds1, bounds2);

        if (offset1 != null && offset2 != null) {
            if ((collisionObject1.getPosition().centerX() - collisionObject2.getPosition().centerX()) * offset1.x < 0) {
                offset1.negate();
            } else if ((collisionObject1.getPosition().centerY() - collisionObject2.getPosition().centerY()) * offset1.y < 0) {
                offset1.negate();
            }

            if (offset1.x != 0)
                offset1.x = (int) (Math.ceil(Math.abs(offset1.x))) * (offset1.x / Math.abs(offset1.x));
            if (offset1.y != 0)
                offset1.y = (int) (Math.ceil(Math.abs(offset1.y))) * (offset1.y / Math.abs(offset1.y));

            offset1.y *= -1;    //invert y component for consistency

            if (collisionObject2 instanceof Tile) {
                if (tileCollisionInvalid((Tile) collisionObject2, offset1)) {
                    offsetObject = false;
                }
            }

            if (offsetObject) collisionObject1.collide(offset1);
            return true;
        }
        return false;
    }

    //projection onto bounds1 polygon; minimum offset returned as a Points vector
    private PointF getProjectionOffset(float[] bounds1, float[] bounds2) {
        PointF ret = new PointF();
        PointF edge = new PointF();
        float overlap, globalOverlap = Float.POSITIVE_INFINITY;
        float dotProjection;

        for (int i = 0; i < bounds1.length; i += 2) {
            float minProjection1 = Float.POSITIVE_INFINITY;
            float maxProjection1 = Float.NEGATIVE_INFINITY;
            float minProjection2 = Float.POSITIVE_INFINITY;
            float maxProjection2 = Float.NEGATIVE_INFINITY;

            edge.set(bounds1[(i + 2) % bounds1.length] - bounds1[i], bounds1[(i + 3) % bounds1.length] - bounds1[i + 1]);
            double hypotenuse = Math.hypot(edge.x, edge.y);
            edge.x /= hypotenuse;
            edge.y /= hypotenuse;

            for (int j = 0; j < bounds1.length; j += 2) {
                dotProjection = -edge.y * bounds1[j] + edge.x * bounds1[j + 1];
                minProjection1 = Math.min(minProjection1, dotProjection);
                maxProjection1 = Math.max(maxProjection1, dotProjection);
            }

            for (int j = 0; j < bounds2.length; j += 2) {
                dotProjection = -edge.y * bounds2[j] + edge.x * bounds2[j + 1];
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

    private boolean tileCollisionInvalid(Tile tile, PointF offset) {
        int coordinateX = (int) Math.copySign(Constants.tileSize, offset.x) + tile.getPosition().centerX();
        int coordinateY = (int) Math.copySign(Constants.tileSize, -offset.y) + tile.getPosition().centerY();

        if (offset.x != 0) {
            try {
                int tileID = level.get(coordinateX / Constants.tileSize - levelIndexOffset)[tile.getPosition().centerY() / Constants.tileSize];
                if (Tile.isTile(tileID)) {
                    int index = 1;
                    if (offset.x < 0) index = 3;
                    if (Tile.getFlushAttribute(tile.getTileID()).charAt(index) != '0'
                            && Tile.getFlushAttribute(tileID).charAt((index + 2) % 4) == '1')
                        return true;
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }

        if (offset.y != 0) {
            try {
                int tileID = level.get(tile.getPosition().centerX() / Constants.tileSize - levelIndexOffset)[coordinateY / Constants.tileSize];
                if (Tile.isTile(tileID)) {
                    int index = 0;
                    if (offset.y < 0) index = 2;
                    if (Tile.getFlushAttribute(tile.getTileID()).charAt(index) != '0'
                            && Tile.getFlushAttribute(tileID).charAt((index + 2) % 4) == '1')
                        return true;
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }

        return false;
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
                int tileID = level.get(coordinateX / Constants.tileSize - levelIndexOffset)[coordinateY / Constants.tileSize];
                if (Tile.isTile(tileID))
                    surroundingTiles.add(new Tile(tileID, coordinateX, coordinateY));
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }

            if (coordinateX < maxX && coordinateY == minY) coordinateX += Constants.tileSize;
            else if (coordinateX >= maxX && coordinateY < maxY) coordinateY += Constants.tileSize;
            else if (coordinateX > minX) coordinateX -= Constants.tileSize;
            else coordinateY -= Constants.tileSize;
        } while (coordinateX != minX || coordinateY != minY);

        return surroundingTiles;
    }

    void setEnemyMovement(Enemy enemy) {
        try {
            int coordinateX = (int) (enemy.getPosition().centerX() + Math.copySign(Constants.tileSize, enemy.getVelX()));
            int coordinateY = enemy.getPosition().top;

            int tileID = level.get(coordinateX / Constants.tileSize - levelIndexOffset)[coordinateY / Constants.tileSize];
            if (Tile.isTile(tileID)) enemy.flip(enemy.getVelX() < 0);

            tileID = level.get(coordinateX / Constants.tileSize - levelIndexOffset)[coordinateY / Constants.tileSize + 1];
            if (Tile.isTile(tileID)) enemy.flip(enemy.getVelX() < 0);

            tileID = level.get(coordinateX / Constants.tileSize - levelIndexOffset)[coordinateY / Constants.tileSize + 2];
            if (tileID == 0) enemy.flip(enemy.getVelX() < 0);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
}
