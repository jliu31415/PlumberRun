package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;

class Player {
    private Rect screenDimensions;
    private Bitmap runningSprite;
    private Rect imageFrame;
    private Rect imagePosition;
    private int runningSpriteWidth, runningSpriteHeight;
    private double relativeImagePosX = .1, relativeImagePosY = .5; //[0, 1]
    private int imagePosX, imagePosY;
    private double relativeImageSize = .2;  //[0, 1]
    private int imageSize;
    private long frameCount = 0;

    public Player(Bitmap runningSprite){
        this.runningSprite = runningSprite;
        runningSpriteWidth = runningSprite.getWidth()/4;
        runningSpriteHeight = runningSprite.getHeight()/3;
    }

    //called after surface is created
    public void setScreenDimensions(SurfaceHolder holder) {
        screenDimensions = holder.getSurfaceFrame();
        //width and height switched due to landscape orientation
        imagePosX = (int)(screenDimensions.width() * relativeImagePosX);
        imagePosY = (int)(screenDimensions.height() * relativeImagePosY);
        imageSize = (int)(relativeImageSize * screenDimensions.height());
    }

    public void draw(Canvas canvas) {
        imagePosition = new Rect(imagePosX, imagePosY, imagePosX + imageSize, imagePosY + imageSize);
        int framePosX = runningSpriteWidth * ((int)frameCount % 4);
        int framePosY = runningSpriteHeight * ((int)frameCount/4 % 3);
        imageFrame = new Rect(framePosX, framePosY, framePosX + runningSpriteWidth, framePosY + runningSpriteHeight);
        frameCount++;
        canvas.drawBitmap(runningSprite, imageFrame, imagePosition, null);
    }

    public void update() {
    }
}
