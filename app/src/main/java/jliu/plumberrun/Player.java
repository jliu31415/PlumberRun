package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

class Player {
    private Bitmap runningSprite;
    private int runningSpriteWidth, runningSpriteHeight;
    private Rect imageFrame;
    private int imagePosX, imagePosY;
    private Rect imagePosition;
    private long frameCount = 0;

    public Player(Bitmap runningSprite){
        this.runningSprite = runningSprite;
        runningSpriteWidth = runningSprite.getWidth()/4;
        runningSpriteHeight = runningSprite.getHeight()/3;
        imagePosX = 100;
        imagePosY = 500;
        imagePosition = new Rect(imagePosX, imagePosY, imagePosX + 256, imagePosY + 256);
    }

    public void draw(Canvas canvas) {
        int framePosX = runningSpriteWidth * ((int)frameCount % 4);
        int framePosY = runningSpriteHeight * ((int)frameCount/4 % 3);
        imageFrame = new Rect(framePosX, framePosY, framePosX + runningSpriteWidth, framePosY + runningSpriteHeight);
        frameCount++;
        canvas.drawBitmap(runningSprite, imageFrame, imagePosition, null);
    }

    public void update() {
    }
}
