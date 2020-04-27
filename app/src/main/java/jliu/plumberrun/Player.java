package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

class Player {
    private Bitmap playerSprite;
    private Rect spriteFrame; //frame of Bitmap to extract
    private Rect spritePosition;  //position of sprite
    private int spriteWidth, spriteHeight;
    private int posX = 0, posY = 1011;    //player starts on bottom tile layer
    private int velX = 10, velY = 0;
    private long frameCount = 0;
    private int imageSize;   //player size = 2x tile size; 138 x 138 pixels
    private int jumpVelocity = 0;
    private int gravity = 0;

    public Player(Bitmap playerSprite){
        this.playerSprite = playerSprite;
        spriteWidth = playerSprite.getWidth()/4;
        spriteHeight = playerSprite.getHeight()/3;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(playerSprite, spriteFrame, spritePosition, null);
    }

    public void update() {
        int framePosX = spriteWidth * ((int)frameCount % 4);
        int framePosY = spriteHeight * ((int)frameCount/4 % 3);
        spriteFrame = new Rect(framePosX, framePosY, framePosX + spriteWidth, framePosY + spriteHeight);
        frameCount++;

        posX += velX;
        posY -= velY;
        velY += gravity;
        //check collisions!!!

        imageSize = (int)(138 * Game.canvasScaleY); //image size depends on width of canvas
        //posX, posY gives bottom left corner of spritePosition rectangle
        spritePosition = new Rect((int)(Game.canvasScaleX*posX), (int)(Game.canvasScaleY*posY - imageSize),
                (int)(Game.canvasScaleX*posX + imageSize), (int)(Game.canvasScaleY*posY));
    }

    public void jump() {
        velY = jumpVelocity;
    }
}
