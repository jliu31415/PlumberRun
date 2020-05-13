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
    public static Rect cameraFrame;
    private int totalOffsetX;   //camera frame offset
    private Bitmap plunger_horizontal = BitmapFactory.decodeResource(getResources(), R.drawable.plunger_horizontal);
    private final ArrayList<ArrayList<Integer[]>> allLevels;
    private final LevelCreator levelCreator;
    private final Player player;
    private final GameLoop gameLoop;
    private ArrayList<Plunger> plungers;
    private boolean aiming = false;   //true if user swipes to shoot plunger

    public Game(Context context) {
        super(context);
        setFocusable(true);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        Bitmap tiles_platform = BitmapFactory.decodeResource(getResources(), R.drawable.tiles_platform);
        Bitmap plumber_running = BitmapFactory.decodeResource(getResources(), R.drawable.plumber_running);
        Bitmap plumber_throwing = BitmapFactory.decodeResource(getResources(), R.drawable.plumber_throwing);

        allLevels = parseAllLevels();
        levelCreator = new LevelCreator(allLevels, tiles_platform);
        player = new Player(plumber_running, plumber_throwing);
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
                    plungers.add(0, new Plunger(plunger_horizontal, player, event.getX(), event.getY()));
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
        for (int i = 0; i < plungers.size(); i++)
            plungers.get(i).draw(canvas);

        player.drawPoints(canvas);
        for (Tile tile : levelCreator.getSurroundingTiles(player.getBounds())) {
            tile.drawPoints(canvas);
        }

        canvas.translate(totalOffsetX, 0);
    }

    public void update() {
        player.update();
        for (Tile tile : levelCreator.getSurroundingTiles(player.getBounds())) {
            levelCreator.updateCollisions(player, tile);
        }

        for (int i = 0; i < plungers.size(); i++) {
            if (plungers.get(i).outOfPlay())
                plungers.remove(i--);
            else {
                plungers.get(i).update();
                //levelCreator.updateCollisions(plungers.get(i));
            }
        }

        int cameraOffsetX = -300;
        totalOffsetX = player.getPosition().left + cameraOffsetX;
        totalOffsetX = Math.min(totalOffsetX, allLevels.get(levelCreator.getCurrentLevel()).size() * Tile.tileSize - canvasX);
        totalOffsetX = Math.max(totalOffsetX, 0);
        cameraFrame.offsetTo(totalOffsetX, 0);
    }

    //scale sprite positions for other canvas dimensions unequal to 1080 x 1794
    public static Rect scaleRect(Rect position) {
        return new Rect((int) (Game.canvasScaleX * position.left), (int) (Game.canvasScaleY * position.top),
                (int) (Game.canvasScaleX * position.right), (int) (Game.canvasScaleY * position.bottom));
    }

    public static double scaleX(double pointX) {
        return canvasScaleX * pointX;
    }

    public static double scaleY(double pointY) {
        return canvasScaleX * pointY;
    }
}
