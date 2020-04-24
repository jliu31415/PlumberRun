package jliu.plumberrun;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.core.content.ContextCompat;

class Game extends SurfaceView implements SurfaceHolder.Callback {
    private GameLoop gameLoop;
    private Context context;

    public Game(Context context) {
        super(context);
        this.context = context;
        setFocusable(true);

        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        gameLoop = new GameLoop(this, surfaceHolder);
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
        drawUPS(canvas);
        drawFPS(canvas);
    }

    public void drawUPS(Canvas canvas){
        Paint paint = new Paint();
        int color = ContextCompat.getColor(context, R.color.colorPrimary);
        paint.setColor(color);
        paint.setTextSize(50);
        String averageUPS = Double.toString(gameLoop.getAverageUPS());
        canvas.drawText("UPS: " + averageUPS, 100, 100, paint);
    }

    public void drawFPS(Canvas canvas){
        Paint paint = new Paint();
        int color = ContextCompat.getColor(context, R.color.colorPrimary);
        paint.setColor(color);
        paint.setTextSize(50);
        String averageFPS = Double.toString(gameLoop.getAverageFPS());
        canvas.drawText("FPS: " + averageFPS, 100, 200, paint);
    }

    public void update() {
    }
}
