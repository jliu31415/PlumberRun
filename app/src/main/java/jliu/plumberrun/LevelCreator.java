package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

import java.util.ArrayList;

class LevelCreator {
    private final Game game;
    private ArrayList<Integer[]> level;
    private final Bitmap tileSprites, flagSprites, toilet_sprites;
    private Flag flag;
    private Rect spriteFrame;
    private Rect tilePosition;
    private final int spriteSize; //tile spriteWidth = spriteHeight
    private static final int tileSize = Tile.tileSize;
    private boolean levelComplete;
    private ArrayList<FireworkParticle> fireworks;
    private ArrayList<Point> enemiesInstantiated;    //keeps track of instantiated enemies

    LevelCreator(Game game, ArrayList<Integer[]> level, Bitmap tileSprites, Bitmap flagSprites, Bitmap toilet_sprites) {
        this.game = game;
        this.level = level;
        this.tileSprites = tileSprites;
        this.flagSprites = flagSprites;
        this.toilet_sprites = toilet_sprites;
        spriteSize = tileSprites.getWidth() / 5;
        spriteFrame = new Rect(0, 0, spriteSize, spriteSize);
        tilePosition = new Rect(0, 0, tileSize + 1, tileSize + 1);  //allow tiles to overlap to get rid of border
        enemiesInstantiated = new ArrayList<>();
    }

    void draw(Canvas canvas) {
        int framePosX, framePosY;

        for (int col = Game.getCameraFrame().left / tileSize; col <= Game.getCameraFrame().right / tileSize; col++) {
            if (col == level.size()) break;
            for (int row = 0; row < level.get(col).length; row++) {
                int id = level.get(col)[row];
                if (0 < id && id < 16) {
                    framePosX = spriteSize * (--id % 5);
                    framePosY = spriteSize * (id / 5);
                    spriteFrame.offsetTo(framePosX, framePosY);
                    tilePosition.offsetTo(col * tileSize, row * tileSize);
                    canvas.drawBitmap(tileSprites, spriteFrame, Game.scaleRect(tilePosition), null);
                } else if (id == 50) {
                    boolean create = true;
                    for (Point p : enemiesInstantiated) {
                        if (p.equals(col, row)) {
                            create = false;
                            break;
                        }
                    }
                    if (create) {
                        game.addEnemy(new Enemy(toilet_sprites, col * tileSize, (row - 1) * tileSize));
                        enemiesInstantiated.add(new Point(col, row));
                    }
                } else if (id == 99) {
                    if (flag == null)
                        flag = new Flag(flagSprites, col * tileSize, (row - 2) * tileSize);
                    flag.draw(canvas);
                }
            }
        }

        //draw fireworks
        if (levelComplete) {
            if (fireworks == null) {
                fireworks = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    Paint color = new Paint();
                    color.setColor(Color.rgb((int) (255 * Math.random()), (int) (255 * Math.random()), (int) (255 * Math.random())));
                    fireworks.add(new FireworkParticle(Game.getCameraFrame().left + Game.getCanvasDimensions().width() / 5.0 * (i + Math.random()),
                            Game.getCanvasDimensions().height(), true, color));
                }
            }
            for (FireworkParticle f : fireworks) {
                f.draw(canvas);
            }
        }
    }

    void update() {
        if (flag != null) flag.update();
        if (fireworks != null) {
            for (int i = 0; i < fireworks.size(); i++) {
                fireworks.get(i).update();
                if (fireworks.get(i).canRemove()) fireworks.remove(i--);
            }
        }
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
                if (0 < tileID && tileID < 16)
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

    boolean checkLevelComplete(Player player) {
        if (flag != null && updateCollisions(player, flag, false)) {
            levelComplete = true;
        }
        return levelComplete;
    }

    void setEnemyMovement(Enemy enemy) {
        try {
            int coordinateX = (int) (enemy.getPosition().centerX() + Math.copySign(tileSize, enemy.getVelX()));
            int coordinateY = enemy.getPosition().top;

            int tileID = level.get(coordinateX / tileSize)[coordinateY / tileSize];
            if (0 < tileID && tileID < 16) enemy.flip(enemy.getVelX() < 0);

            tileID = level.get(coordinateX / tileSize)[coordinateY / tileSize + 1];
            if (0 < tileID && tileID < 16) enemy.flip(enemy.getVelX() < 0);

            tileID = level.get(coordinateX / tileSize)[coordinateY / tileSize + 2];
            if (tileID == 0) enemy.flip(enemy.getVelX() < 0);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    boolean checkPlayerDeath(Player player, ArrayList<Enemy> enemies) {
        if (Game.getCameraFrame().left > 0 && !Rect.intersects(Game.getCameraFrame(), player.getPosition())) {
            player.initialize();    //reset when player falls out of frame
            return true;
        } else {
            for (Enemy e : enemies) {
                if (updateCollisions(player, e, false)) {
                    player.initialize();
                    return true;
                }

            }
        }
        return false;
    }

    static class FireworkParticle {
        private double posX, posY;
        private double velX, velY;
        ArrayList<FireworkParticle> fragments;
        private Paint color;
        private boolean parent, exploded;

        FireworkParticle(double posX, double posY, boolean parent, Paint color) {
            this.parent = parent;   //parent objects differ from exploded fragments
            this.posX = posX;
            this.posY = posY;
            if (parent) {
                velX = 0;
                velY = 30 + 15 * Math.random();
            } else {
                velX = 10 * Math.random() - 5;
                velY = 10 * Math.random() - 5;
            }

            this.color = color;
        }

        void update() {
            if (parent && !exploded && velY < 0) explode();
            if (fragments != null) {
                for (int i = 0; i < fragments.size(); i++) {
                    fragments.get(i).update();
                    if (fragments.get(i).canRemove()) fragments.remove(i--);
                }
            }

            posX += velX;
            posY -= velY;
            velY--;
        }

        private void explode() {
            exploded = true;
            fragments = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                fragments.add(new FireworkParticle(this.posX, this.posY, false, this.color));
            }
        }

        void draw(Canvas canvas) {
            if (!exploded)  //stop drawing parent after explosion
                canvas.drawCircle((float) Game.scaleX(posX), (float) Game.scaleY(posY), (float) Game.scaleX(10), color);
            if (fragments != null) {
                for (FireworkParticle f : fragments) f.draw(canvas);
            }
        }

        boolean canRemove() {
            if (exploded && fragments.size() == 0)
                return true;    //condition for removing parent
            return !parent && posY > Game.getCanvasDimensions().height();  //condition for removing fragment
        }
    }
}
