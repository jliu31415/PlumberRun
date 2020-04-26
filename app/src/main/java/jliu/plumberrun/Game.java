package jliu.plumberrun;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.core.content.ContextCompat;

class Game extends SurfaceView implements SurfaceHolder.Callback {
    private GameLoop gameLoop;
    private final Player player;

    public Game(Context context) {
        super(context);
        setFocusable(true);

        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        gameLoop = new GameLoop(this, surfaceHolder);
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.plumber_running));
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
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
        super.draw(canvas);
        canvas.drawColor(ContextCompat.getColor(getContext(), R.color.primary_light));
        drawUPS(canvas);
        drawFPS(canvas);
        player.draw(canvas);
    }

    public void drawUPS(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(ContextCompat.getColor(getContext(), R.color.primary_dark));
        paint.setTextSize(50);
        String averageUPS = Double.toString(gameLoop.getAverageUPS());
        canvas.drawText("UPS: " + averageUPS, 100, 100, paint);
    }

    public void drawFPS(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(ContextCompat.getColor(getContext(), R.color.primary_dark));
        paint.setTextSize(50);
        String averageFPS = Double.toString(gameLoop.getAverageFPS());
        canvas.drawText("FPS: " + averageFPS, 100, 200, paint);
    }

    public void update() {
        player.update();
    }
}
