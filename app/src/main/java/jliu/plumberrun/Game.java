package jliu.plumberrun;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.ContextCompat;

class Game extends SurfaceView implements SurfaceHolder.Callback {
    //canvas broken as an n x 26 grid, to be rescaled with canvasScale variables
    public static double canvasScaleX, canvasScaleY;    //landscape reference
    private GameLoop gameLoop;
    private final Player player;
    private final LevelCreator levelCreator;

    public Game(Context context) {
        super(context);
        setFocusable(true);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        gameLoop = new GameLoop(this, surfaceHolder);
        Bitmap plumber_running = BitmapFactory.decodeResource(getResources(), R.drawable.plumber_running);
        Bitmap tiles_platform = BitmapFactory.decodeResource(getResources(), R.drawable.tiles_platform);
        levelCreator = new LevelCreator(tiles_platform);
        player = new Player(plumber_running);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                player.jump();
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        canvasScaleX = holder.getSurfaceFrame().width()/1794;   //dimensions of pixel 2 canvas
        canvasScaleY = holder.getSurfaceFrame().width()/1080;   //dimensions of pixel 2 canvas
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
        player.draw(canvas);
        levelCreator.draw(canvas);
    }

    public void update() {
        player.update();
    }
}
