package jliu.plumberrun;

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

class Game extends SurfaceView implements SurfaceHolder.Callback {
    public static double canvasScaleX, canvasScaleY;    //landscape reference
    public static Rect cameraFrame;
    private final int cameraOffsetX = -300;
    private int offset; //camera offset + player position
    private final LevelCreator levelCreator;
    private final Player player;
    private final GameLoop gameLoop;
    private ArrayList<Plunger> plungers;
    private final Paint white;
    private boolean dragSet = false, drawArc = false;   //true if user swipes to shoot

    public Game(Context context) {
        super(context);
        setFocusable(true);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        Bitmap tiles_platform = BitmapFactory.decodeResource(getResources(), R.drawable.tiles_platform);
        Bitmap plumber_running = BitmapFactory.decodeResource(getResources(), R.drawable.plumber_running);
        Bitmap plumber_throwing = BitmapFactory.decodeResource(getResources(), R.drawable.plumber_throwing);

        levelCreator = new LevelCreator(tiles_platform);
        player = new Player(plumber_running, plumber_throwing, levelCreator);
        gameLoop = new GameLoop(this, surfaceHolder, levelCreator, parseAllLevels());
        plungers = new ArrayList<>();

        white = new Paint();
        white.setColor(Color.WHITE);
    }

    private ArrayList<ArrayList<Integer[]>> parseAllLevels() {
        ArrayList<ArrayList<Integer[]>> allLevels = new ArrayList<>();
        InputStream is = this.getResources().openRawResource(R.raw.test_level);
        Scanner scan = new Scanner(is);
        ArrayList<Integer[]> level = new ArrayList<>();
        while (scan.hasNext()) {
            while (!scan.hasNextInt()) scan.nextLine();
            int counter = 0;
            Integer[] column = new Integer[16];
            for (int i = 0; i < 16; i++) column[counter++] = scan.nextInt();
            level.add(column);
        }
        allLevels.add(level);
        scan.close();
        return allLevels;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!gameLoop.isReady())
                    gameLoop.setReady(true);
                else if (event.getX() > 1794 * canvasScaleX * .6 && event.getY() > 1080 * canvasScaleY * .6)
                    player.jump();
                else {
                    dragSet = true;
                    player.setFrameCount(0);
                    player.slowMotion(true);
                    player.setThrowing(true, false);
                    plungers.add(0, new Plunger(BitmapFactory.decodeResource(getResources(), R.drawable.plunger),
                            player, event.getX(), event.getY()));
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (dragSet) {
                    drawArc = true;
                    plungers.get(0).setEnd(event.getX(), event.getY());
                }
                break;

            case MotionEvent.ACTION_UP:
                if (dragSet) {
                    player.slowMotion(false);
                    plungers.get(0).fire();
                    player.setThrowing(false, true);
                }
                dragSet = false;
                drawArc = false;
                break;
        }

        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //dimensions of Google Pixel 2
        canvasScaleX = holder.getSurfaceFrame().width() / 1794;
        canvasScaleY = holder.getSurfaceFrame().width() / 1080;
        cameraFrame = new Rect(cameraOffsetX, 0, (int) (1794 * Game.canvasScaleY), (int) (1080 * Game.canvasScaleX));
        gameLoop.startLoop();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void draw(Canvas canvas) {
        offset = player.getPosX() + cameraOffsetX;
        if (offset > 0)
            canvas.translate(-offset, 0);

        super.draw(canvas);
        canvas.drawColor(ContextCompat.getColor(getContext(), R.color.primary_light));
        levelCreator.draw(canvas);
        player.draw(canvas);

        if (drawArc)
            plungers.get(0).drawArc(canvas, white);
        for (int i = 0; i < plungers.size(); i++)
            plungers.get(i).draw(canvas);

        if (offset > 0)
            canvas.translate(offset, 0);
    }

    public void update() {
        player.update();
        for (Plunger p : plungers) {
            p.update();
        }
        if (player.getPosX() > -cameraOffsetX)
            cameraFrame.offsetTo(player.getPosX() + cameraOffsetX, 0);
    }

    //scale sprite positions for other canvas dimensions unequal to 1080 x 1794 (landscape)
    public static Rect scale(Rect position) {
        return new Rect((int) (Game.canvasScaleX * position.left), (int) (Game.canvasScaleY * position.top),
                (int) (Game.canvasScaleX * position.right), (int) (Game.canvasScaleY * position.bottom));
    }
}
