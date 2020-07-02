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
    private ArrayList<Integer[]> level;
    private final int levelID;
    private final LevelCreator levelCreator;
    private final Player player;
    private final Plungers plungers;
    private final GameLoop gameLoop;
    private ArrayList<Enemy> enemies;
    public static Rect cameraFrame;
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
        Bitmap plunger_sprite = BitmapFactory.decodeResource(getResources(), R.drawable.plunger_sprite);

        this.levelID = levelID;
        levelCreator = new LevelCreator(this, tile_sprites, flag_sprites, toilet_sprites);
        player = new Player(plumber_sprites);
        plungers = new Plungers(plunger_sprite, player);
        gameLoop = new GameLoop(this, surfaceHolder);
        enemies = new ArrayList<>();

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
        int numRows = (int) Math.ceil((double) cameraFrame.height() / Constants.tileSize);
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
                else if (jumpButton.contains((int) event.getX(), (int) event.getY())) {
                    player.setJumpLatch(true);
                } else if (player.getPosition().left > 0) {
                    if (plungers.reloaded()) {
                        player.windUp();
                        plungers.addPlunger(event.getX(), event.getY());
                        startTime = System.currentTimeMillis();
                        slowMotion = true;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (player.isWindingUp() && plungers.getList().size() > 0)
                    plungers.getList().get(0).setEnd(event.getX(), event.getY());
                break;

            case MotionEvent.ACTION_UP:
                if (player.isWindingUp() && plungers.getList().size() > 0) {
                    player.throwPlunger();
                    plungers.getList().get(0).fire();
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
        //landscape reference
        int canvasX = holder.getSurfaceFrame().right;
        int canvasY = holder.getSurfaceFrame().bottom;
        cameraFrame = new Rect(0, 0, canvasX, canvasY);
        jumpButton = new Rect(canvasX / 2, canvasY / 2, canvasX, canvasY);
        slowMotionBar = new Rect(canvasX / 20, canvasY / 20, canvasX / 5, canvasY / 12);
        level = parseLevel(levelID);
        levelCreator.initializeLevel(level);
        player.initialize();
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
        canvas.translate(-cameraFrame.left, -cameraFrame.top);
        super.draw(canvas);
        canvas.drawColor(ContextCompat.getColor(getContext(), R.color.primary_light));
        levelCreator.draw(canvas);
        player.draw(canvas);
        plungers.drawPlungers(canvas);
        for (int i = 0; i < enemies.size(); i++) {
            enemies.get(i).draw(canvas);
        }
        canvas.translate(cameraFrame.left, cameraFrame.top);

        if (slowMotion) {
            canvas.translate(slowMotionBar.left, slowMotionBar.top);
            double ratio = 1 - (System.currentTimeMillis() - startTime) / slowDuration;
            Rect bar = new Rect(0, 0, (int) (slowMotionBar.width() * ratio), slowMotionBar.height());
            canvas.drawRect(bar, white);
            canvas.translate(-slowMotionBar.left, -slowMotionBar.top);
        }
    }

    void update() {
        int totalOffsetX = player.getPosition().left - cameraFrame.width() / 5;
        totalOffsetX = Math.min(totalOffsetX, level.size() * Constants.tileSize - cameraFrame.width());
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

            plungers.updatePlungers();
            for (int i = 0; i < plungers.getList().size(); i++) {
                if (plungers.getList().get(i).tileCollisionsEnabled()) {
                    for (Tile tile : levelCreator.getSurroundingTiles(plungers.getList().get(i).getBounds())) {
                        levelCreator.updateCollisions(plungers.getList().get(i), tile, true);
                    }
                    if (plungers.getList().get(i).isSticking()) {
                        plungers.getList().get(i).hasCollided();
                    } else {
                        for (int j = 0; j < enemies.size(); j++) {
                            if (plungers.getList().get(i).hasFired() &&
                                    levelCreator.updateCollisions(plungers.getList().get(i), enemies.get(j), false))
                                enemies.remove(j--);
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
            plungers.clearList();
            enemies.clear();
            levelCreator.reset();
        }
    }

    boolean checkPlayerDeath() {
        if (player.getPosition().left > 0 && !Rect.intersects(cameraFrame, player.getPosition())) {
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
}
