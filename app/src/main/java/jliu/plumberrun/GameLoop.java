package jliu.plumberrun;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;

class GameLoop extends Thread {
    private final Game game;
    private final SurfaceHolder surfaceHolder;
    private final LevelCreator levelCreator;
    private final ArrayList<ArrayList<Integer[]>> levels;
    private final double TARGET_UPS = 36;
    private double averageUPS, averageFPS;
    private boolean running;
    private boolean ready = false;  //tap to start game

    public GameLoop(Game game, SurfaceHolder surfaceHolder, LevelCreator levelCreator, ArrayList<ArrayList<Integer[]>> levels) {
        this.game = game;
        this.surfaceHolder = surfaceHolder;
        this.levelCreator = levelCreator;
        this.levels = levels;
    }

    public void startLoop() {
        running = true;
        start();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void run() {
        super.run();
        Canvas canvas = null;
        levelCreator.createLevel(levels.get(0));
        int updateCount = 0, frameCount = 0;
        long startTime, elapsedTime, sleepTime;
        startTime = System.currentTimeMillis();

        while (running) {
            if (!ready) {
                //draw start screen
                startTime = System.currentTimeMillis();
            } else {
                try {
                    canvas = surfaceHolder.lockCanvas();
                    synchronized (surfaceHolder) {
                        game.update();
                        updateCount++;
                        game.draw(canvas);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } finally {
                    if (canvas != null) {
                        try {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                            frameCount++;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                elapsedTime = System.currentTimeMillis() - startTime;
                sleepTime = (long) (updateCount * (1E+3 / TARGET_UPS) - elapsedTime);
                if (sleepTime > 0) {
                    try {
                        sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                while (sleepTime < 0) {
                    game.update();
                    updateCount++;
                    elapsedTime = System.currentTimeMillis() - startTime;
                    sleepTime = (long) (updateCount * (1E+3 / TARGET_UPS) - elapsedTime);
                }

                if (elapsedTime > 1000) {
                    averageUPS = updateCount * elapsedTime * 1E-3;
                    averageFPS = frameCount * elapsedTime * 1E-3;
                    printUPSFPS();
                    updateCount = frameCount = 0;
                    startTime = System.currentTimeMillis();
                }
            }
        }
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isReady() {
        return ready;
    }

    public void printUPSFPS() {
        Log.i("UPS: ", Double.toString(averageUPS));
        Log.i("FPS: ", Double.toString(averageFPS));
    }
}
