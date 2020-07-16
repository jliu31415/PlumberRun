package jliu.plumberrun;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

@SuppressLint("ViewConstructor")
class Game extends SurfaceView implements SurfaceHolder.Callback {
    private final GameLoop gameLoop;
    private ArrayList<Integer[]> level;
    private final LevelCreator levelCreator;
    private final Player player;
    private final PlungerType plunger;
    private ArrayList<Plunger> plungers;
    private ArrayList<Enemy> enemies;
    static Rect cameraFrame;
    private Rect jumpButton;
    private RectF slowMotionBar;
    private final Object lock;
    private Paint white;
    private Paint typeFace;
    private long startTime = 0;
    private double slowDuration = 1000;
    private boolean slowMotion = false;
    private boolean levelLoading = true, levelStarted = false, gameInProgress = true;

    //must be public for xml file
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Game(Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        setFocusable(true);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        Bitmap plumber_sprites = BitmapFactory.decodeResource(getResources(), R.drawable.plumber_sprites);
        Bitmap grass_tile_sprites = BitmapFactory.decodeResource(getResources(), R.drawable.grass_tile_sprites);
        Bitmap flag_sprites = BitmapFactory.decodeResource(getResources(), R.drawable.flag_sprites);
        Bitmap toilet_sprites = BitmapFactory.decodeResource(getResources(), R.drawable.toilet_sprites);
        Bitmap plunger_sprite = BitmapFactory.decodeResource(getResources(), R.drawable.plunger_sprite);

        gameLoop = new GameLoop(this, surfaceHolder);
        levelCreator = new LevelCreator(this, grass_tile_sprites, flag_sprites, toilet_sprites);
        player = new Player(plumber_sprites);
        plunger = new PlungerType(plunger_sprite, player);
        plungers = new ArrayList<>();
        enemies = new ArrayList<>();

        lock = new Object();
        white = new Paint();
        white.setColor(Color.WHITE);
        typeFace = new Paint();
        typeFace.setTypeface(getResources().getFont(R.font.chelsea_market));
        typeFace.setTextSize(100);
        typeFace.setTextAlign(Paint.Align.CENTER);
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
                        if (plungers.size() == 0 || plungers.get(0).hasFired()) {
                            player.windUp();
                            plungers.add(0, plunger.createPlunger(event.getX(), event.getY()));
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
        //landscape reference
        int canvasX = holder.getSurfaceFrame().right;
        int canvasY = holder.getSurfaceFrame().bottom;
        cameraFrame = new Rect(0, 0, canvasX, canvasY);
        jumpButton = new Rect(canvasX / 2, canvasY / 2, canvasX, canvasY);
        slowMotionBar = new RectF(canvasX / 20.0f, canvasY / 20.0f, canvasX / 3.0f, canvasY / 10.0f);

        synchronized (lock) {
            lock.notify();   //allow parseLevel() when cameraFrame is initialized;
        }
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
        //do not use for each loop (concurrent modification exception)
        for (int i = 0; i < plungers.size(); i++) {
            plungers.get(i).draw(canvas);
        }
        for (int i = 0; i < enemies.size(); i++) {
            enemies.get(i).draw(canvas);
        }
        canvas.translate(cameraFrame.left, cameraFrame.top);

        if (slowMotion) {
            canvas.translate(slowMotionBar.left, slowMotionBar.top);
            float ratio = (float) (1 - (System.currentTimeMillis() - startTime) / slowDuration);
            RectF bar = new RectF(0, 0, slowMotionBar.width() * ratio, slowMotionBar.height());
            canvas.drawRoundRect(bar, bar.height() / 2, bar.height() / 2, white);
            canvas.translate(-slowMotionBar.left, -slowMotionBar.top);
        }

        if (typeFace.getAlpha() > 0) {
            canvas.drawText("Touch to Start", cameraFrame.centerX(), cameraFrame.centerY(), typeFace);
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
            if (levelCreator.checkLevelComplete(player)) {
                synchronized (this) {
                    gameInProgress = false;
                    notify();   //notify MainActivity that level is complete
                }
            } else if (checkPlayerDeath()) {
                resetLevel();
            }

            for (int i = 0; i < plungers.size(); i++) {
                if (plungers.get(i).outOfPlay()) {
                    plungers.remove(i--);
                } else {
                    plungers.get(i).update();
                    if (plungers.get(i).tileCollisionsEnabled()) {
                        for (Tile tile : levelCreator.getSurroundingTiles(plungers.get(i).getBounds())) {
                            levelCreator.updateCollisions(plungers.get(i), tile, true);
                        }
                        if (plungers.get(i).isSticking()) {
                            plungers.get(i).hasCollided();
                        } else {
                            for (int j = 0; j < enemies.size(); j++) {
                                if (plungers.get(i).hasFired() &&
                                        levelCreator.updateCollisions(plungers.get(i), enemies.get(j), false)) {
                                    plungers.get(i).fade();
                                    enemies.get(j).fade();
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
                if (enemies.get(i).canRemove()) {
                    enemies.remove(i--);
                } else {
                    enemies.get(i).update();
                    levelCreator.setEnemyMovement(enemies.get(i));
                }
            }

            typeFace.setAlpha(Math.max(typeFace.getAlpha() - Constants.fade, 0));
        }
    }

    void loadLevel(final int levelID) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (cameraFrame == null) {
                    synchronized (lock) {
                        try {
                            lock.wait();    //wait for cameraFrame initialization
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                level = parseLevel(levelID);
                levelCreator.initializeLevel(level);
                player.initialize();
                levelLoading = false;
            }
        }).start();
    }

    void resetLevel() {
        //release current plunger
        onTouchEvent(MotionEvent.obtain(10, 10, MotionEvent.ACTION_UP, 0, 0, 0));

        levelStarted = false;
        gameInProgress = true;
        player.initialize();
        plungers.clear();
        enemies.clear();
        levelCreator.reset();
        typeFace.setAlpha(255);
    }

    boolean checkPlayerDeath() {
        if (player.getPosition().left > 0 && !Rect.intersects(cameraFrame, player.getPosition())) {
            return true;
        } else {
            for (Enemy e : enemies) {
                if (!e.isDead() && levelCreator.updateCollisions(player, e, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    boolean levelLoading() {
        return levelLoading;
    }

    boolean gameInProgress() {
        return gameInProgress;
    }

    void startGameLoop() {
        gameLoop.start();
    }

    void endGameLoop() {
        gameLoop.endLoop();
    }
}
