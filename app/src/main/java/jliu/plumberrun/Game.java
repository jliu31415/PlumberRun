package jliu.plumberrun;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.ContextCompat;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

class Game extends SurfaceView implements SurfaceHolder.Callback {
    private static final int canvasX = 1794, canvasY = 1080;    //landscape reference
    private static double canvasScaleX = 1, canvasScaleY = 1;
    static Rect cameraFrame;
    private int totalOffsetX;   //camera frame offset
    private Bitmap plunger_sprite;
    private final ArrayList<ArrayList<Integer[]>> allLevels;
    private final LevelCreator levelCreator;
    private final Player player;
    private final GameLoop gameLoop;
    private ArrayList<Plunger> plungers;
    private boolean aiming = false;   //true if user swipes to shoot plunger

    Game(Context context) {
        super(context);
        setFocusable(true);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        Bitmap tile_sprites = BitmapFactory.decodeResource(getResources(), R.drawable.tile_sprites);
        Bitmap plumber_sprites = BitmapFactory.decodeResource(getResources(), R.drawable.plumber_sprites);
        plunger_sprite = BitmapFactory.decodeResource(getResources(), R.drawable.plunger_sprite);

        allLevels = parseAllLevels();
        levelCreator = new LevelCreator(allLevels, tile_sprites);
        player = new Player(plumber_sprites);
        gameLoop = new GameLoop(this, surfaceHolder, levelCreator);
        plungers = new ArrayList<>();
    }

    private ArrayList<ArrayList<Integer[]>> parseAllLevels() {
        ArrayList<ArrayList<Integer[]>> allLevels = new ArrayList<>();
        InputStream is = this.getResources().openRawResource(R.raw.test_level);
        Scanner scan = new Scanner(is);
        ArrayList<Integer[]> level = new ArrayList<>();
        int numRows = 1 + canvasY / Tile.tileSize;
        int id = 0;
        boolean autoFill;
        while (scan.hasNext()) {
            while (!scan.hasNextInt()) scan.nextLine();
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
        allLevels.add(level);
        scan.close();
        return allLevels;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!gameLoop.levelStarted())
                    gameLoop.startLevel();
                else if (event.getX() > Game.scaleX(canvasX * .5) && event.getY() > Game.scaleY(canvasY * .5))
                    player.jump();
                else {
                    aiming = true;
                    player.windUp();
                    plungers.add(0, new Plunger(plunger_sprite, player, event.getX(), event.getY()));
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (aiming)
                    plungers.get(0).setEnd(event.getX(), event.getY());
                break;

            case MotionEvent.ACTION_UP:
                if (aiming) {
                    player.throwPlunger();
                    plungers.get(0).fire();
                    aiming = false;
                }
                break;
        }

        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        cameraFrame = new Rect(0, 0, canvasX, canvasY);
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
        canvas.translate(-totalOffsetX, 0);

        super.draw(canvas);
        canvas.drawColor(ContextCompat.getColor(getContext(), R.color.primary_light));
        levelCreator.draw(canvas);
        player.draw(canvas);
        for (int i = 0; i < plungers.size(); i++) { //don't use foreach loop
            plungers.get(i).draw(canvas);
        }

        canvas.translate(totalOffsetX, 0);
    }

    void update() {
        player.update();
        for (Tile tile : levelCreator.getSurroundingTiles(player.getBounds())) {
            levelCreator.updateCollisions(player, tile);
        }

        for (int i = 0; i < plungers.size(); i++) {
            if (plungers.get(i).outOfPlay())
                plungers.remove(i--);
            else {
                plungers.get(i).update();
                if (plungers.get(i).tileCollisionsEnabled()) {
                    for (Tile tile : levelCreator.getSurroundingTiles(plungers.get(i).getBounds())) {
                        levelCreator.updateCollisions(plungers.get(i), tile);
                    }
                }
            }
        }

        int cameraOffsetX = -300;
        totalOffsetX = player.getPosition().left + cameraOffsetX;
        totalOffsetX = Math.min(totalOffsetX, allLevels.get(levelCreator.getCurrentLevel()).size() * Tile.tileSize - canvasX);
        totalOffsetX = Math.max(totalOffsetX, 0);
        cameraFrame.offsetTo(totalOffsetX, 0);
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
}
