package jliu.plumberrun;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

class GameLoop extends Thread{
    private Game game;
    private SurfaceHolder surfaceHolder;
    private boolean running = false;
    private double averageUPS, averageFPS;
    private final double TARGET_UPS = 36;

    public GameLoop(Game game, SurfaceHolder surfaceHolder) {
        this.game = game;
        this.surfaceHolder = surfaceHolder;
    }

    public void startLoop() {
        running = true;
        start();
    }

    @Override
    public void run() {
        super.run();
        Canvas canvas = null;

        int updateCount = 0, frameCount = 0;
        long startTime, elapsedTime, sleepTime;
        startTime = System.currentTimeMillis();

        while(running){
            try{
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    game.update();
                    updateCount++;
                    game.draw(canvas);
                }
            } catch (IllegalArgumentException e){
                e.printStackTrace();
            } finally {
                if(canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                        frameCount++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            elapsedTime = System.currentTimeMillis() - startTime;
            sleepTime = (long)(updateCount * (1E+3/TARGET_UPS) - elapsedTime);
            if(sleepTime > 0){
                try{
                    sleep(sleepTime);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
            while(sleepTime < 0) {
                game.update();
                updateCount++;
                elapsedTime = System.currentTimeMillis() - startTime;
                sleepTime = (long) (updateCount * (1E+3 / TARGET_UPS) - elapsedTime);
            }

            if(elapsedTime > 1000) {
                averageUPS = updateCount * elapsedTime * 1E-3;
                averageFPS = frameCount * elapsedTime * 1E-3;
                updateCount = frameCount = 0;
                startTime = System.currentTimeMillis();
            }
        }
    }

    public double getAverageUPS() {
        return averageUPS;
    }

    public double getAverageFPS() {
        return averageFPS;
    }
}
