package jliu.plumberrun;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.ContextCompat;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

@SuppressLint("ViewConstructor")
class Game extends SurfaceView implements SurfaceHolder.Callback {
    private static final int canvasX = 1794, canvasY = 1080;    //landscape reference
    private static double canvasScaleX = 1, canvasScaleY = 1;
    private static Rect cameraFrame;
    private final ArrayList<Integer[]> level;
    private final LevelCreator levelCreator;
    private final Player player;
    private final GameLoop gameLoop;
    private final Bitmap plunger_sprite;
    private ArrayList<Plunger> plungers;
    private ArrayList<Enemy> enemies;
    private Rect jumpButton;
    private Rect slowMotionBar;
    private Paint white;
    private long startTime = 0;
    private double slowDuration = 1500;
    private boolean slowMotion = false;
    private boolean levelStarted = false;

    Game(Context context, int levelID) {
        super(context);
        setFocusable(true);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        Bitmap plumber_sprites = BitmapFactory.decodeResource(getResources(), R.drawable.plumber_sprites);
        Bitmap tile_sprites = BitmapFactory.decodeResource(getResources(), R.drawable.tile_sprites);
        Bitmap flag_sprites = BitmapFactory.decodeResource(getResources(), R.drawable.flag_sprites);
        Bitmap toilet_sprites = BitmapFactory.decodeResource(getResources(), R.drawable.toilet_sprites);
        plunger_sprite = BitmapFactory.decodeResource(getResources(), R.drawable.plunger_sprite);

        level = parseLevel(levelID);
        cameraFrame = new Rect(0, 0, canvasX, canvasY);
        levelCreator = new LevelCreator(this, level, tile_sprites, flag_sprites, toilet_sprites);
        player = new Player(plumber_sprites);
        gameLoop = new GameLoop(this, surfaceHolder);
        plungers = new ArrayList<>();
        enemies = new ArrayList<>();
        jumpButton = new Rect(canvasX / 2, canvasY / 2, canvasX, canvasY);
        slowMotionBar = new Rect(100, 100, 600, 150);

        white = new Paint();
        white.setColor(Color.WHITE);
    }

    private ArrayList<Integer[]> parseLevel(int levelID) {
        InputStream is = new InputStream() {
            @Override
            public int read() {
                return 0;
            }
        };

        if (levelID == 0) {
            is = this.getResources().openRawResource(R.raw.test_level);
        }

        Scanner scan = new Scanner(is);
        ArrayList<Integer[]> level = new ArrayList<>();
        int numRows = (int) Math.ceil((double) canvasY / Tile.tileSize);
        int id = 0;
        boolean autoFill;

        while (scan.hasNext()) {
            if (!scan.hasNextInt()) {
                if (scan.next().equals("*")) {
                    int repeat = scan.nextInt();
                    while (repeat-- > 0) level.add(level.get(level.size() - 1));
                } else scan.nextLine(); //ignore text comments
            } else {
                Integer[] column = new Integer[numRows];
                autoFill = false;
                for (int i = numRows - 1; i >= 0; i--) {
                    if (!autoFill) id = scan.nextInt();
                    if (id == -1) autoFill = true;
                    if (!autoFill) column[i] = id;
                    else column[i] = column[i + 1];
                }
                level.add(column);
            }
        }

        scan.close();
        return level;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!levelStarted) levelStarted = true;
                else if (Game.scaleRect(jumpButton).contains((int) event.getX(), (int) event.getY())) {
                    player.setJumpLatch(true);
                } else if (player.getPosition().left > 0) {
                    if (plungers.size() == 0 || plungers.get(0).hasFired()) {
                        player.windUp();
                        plungers.add(0, new Plunger(plunger_sprite, player, event.getX(), event.getY()));
                        startTime = System.currentTimeMillis();
                        slowMotion = true;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (player.isWindingUp() && plungers.size() > 0)
                    plungers.get(0).setEnd(event.getX(), event.getY());
                break;

            case MotionEvent.ACTION_UP:
                if (player.isWindingUp() && plungers.size() > 0) {
                    player.throwPlunger();
                    plungers.get(0).fire();
                    slowMotion = false;
                } else {
                    player.setJumpLatch(false); //reset jump latch
                }
                break;
        }
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        gameLoop.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.translate(-cameraFrame.left, 0);
        super.draw(canvas);
        canvas.drawColor(ContextCompat.getColor(getContext(), R.color.primary_light));
        levelCreator.draw(canvas);
        player.draw(canvas);
        //do not use for each loop (concurrent modification exception)
        for (int i = 0; i < plungers.size(); i++) {
            plungers.get(i).draw(canvas);
        }
        for (int i = 0; i < enemies.size(); i++) {
            enemies.get(i).draw(canvas);
        }
        canvas.translate(cameraFrame.left, 0);

        if (slowMotion) {
            canvas.translate(slowMotionBar.left, slowMotionBar.top);
            double ratio = 1 - (System.currentTimeMillis() - startTime) / slowDuration;
            Rect bar = new Rect(0, 0, (int) (slowMotionBar.width() * ratio), slowMotionBar.height());
            canvas.drawRect(scaleRect(bar), white);
            canvas.translate(-slowMotionBar.left, -slowMotionBar.top);
        }
    }

    void update() {
        int cameraOffsetX = -300;
        int totalOffsetX = player.getPosition().left + cameraOffsetX;
        totalOffsetX = Math.min(totalOffsetX, level.size() * Tile.tileSize - canvasX);
        totalOffsetX = Math.max(totalOffsetX, 0);
        cameraFrame.offset((totalOffsetX - cameraFrame.left) / 5, 0);

        levelCreator.update();

        if (levelStarted) {
            player.update();
            for (Tile tile : levelCreator.getSurroundingTiles(player.getBounds())) {
                levelCreator.updateCollisions(player, tile, true);
            }
            if (levelCreator.checkLevelComplete(player)) gameOver(true);
            else if (checkPlayerDeath()) gameOver(false);

            for (int i = 0; i < plungers.size(); i++) {
                if (plungers.get(i).outOfPlay())
                    plungers.remove(i--);
                else {
                    plungers.get(i).update();
                    if (plungers.get(i).tileCollisionsEnabled()) {
                        for (Tile tile : levelCreator.getSurroundingTiles(plungers.get(i).getBounds())) {
                            levelCreator.updateCollisions(plungers.get(i), tile, true);
                        }
                        if (plungers.get(i).isSticking()) plungers.get(i).hasCollided();
                        else {
                            for (int j = 0; j < enemies.size(); j++) {
                                if (plungers.get(i).hasFired() &&
                                        levelCreator.updateCollisions(plungers.get(i), enemies.get(j), false)) {
                                    enemies.remove(j--);
                                }
                            }
                        }
                    }
                }
            }

            //automatically release plunger
            if (player.isWindingUp() && System.currentTimeMillis() - startTime > slowDuration) {
                onTouchEvent(MotionEvent.obtain(10, 10, MotionEvent.ACTION_UP, 0, 0, 0));
            }

            for (int i = 0; i < enemies.size(); i++) {
                enemies.get(i).update();
                levelCreator.setEnemyMovement(enemies.get(i));
            }
        }
    }

    private void gameOver(boolean levelComplete) {
        //release plunger
        onTouchEvent(MotionEvent.obtain(10, 10, MotionEvent.ACTION_UP, 0, 0, 0));
        if (levelComplete) {

        } else {
            levelStarted = false;
            player.initialize();
            plungers.clear();
            enemies.clear();
            levelCreator.reset();
        }
    }

    boolean checkPlayerDeath() {
        if (player.getPosition().left > 0 && !Rect.intersects(Game.getCameraFrame(), player.getPosition())) {
            return true;
        } else {
            for (Enemy e : enemies) {
                if (levelCreator.updateCollisions(player, e, false)) {
                    return true;
                }

            }
        }
        return false;
    }

    void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    //scale sprite positions for other canvas dimensions unequal to 1080 x 1794
    static Rect scaleRect(Rect position) {
        return new Rect((int) (Game.canvasScaleX * position.left), (int) (Game.canvasScaleY * position.top),
                (int) (Game.canvasScaleX * position.right), (int) (Game.canvasScaleY * position.bottom));
    }

    static double scaleX(double pointX) {
        return canvasScaleX * pointX;
    }

    static double scaleY(double pointY) {
        return canvasScaleX * pointY;
    }

    static Rect getCanvasDimensions() {
        return new Rect(0, 0, canvasX, canvasY);
    }

    static Rect getCameraFrame() {
        return cameraFrame;
    }
}
