package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Pair;

class Player {
    private final Bitmap playerSprite;
    private final int spriteWidth, spriteHeight;
    private Rect spriteFrame;   //frame of Bitmap to extract
    private Rect spritePosition;
    private long frameCount = 0;
    private int imageSize = 138;   //twice tile size
    private int posX, posY, velX, velY;
    private boolean[] collisions;   //top, bottom, left, right; true if colliding
    private int playerSpeed = 5;
    private int jumpVelocity = 30;
    private int gravity = -2;

    public Player(Bitmap playerSprite) {
        this.playerSprite = playerSprite;
        spriteWidth = playerSprite.getWidth() / 4;
        spriteHeight = playerSprite.getHeight() / 3;
        spritePosition = new Rect(posX, posY, posX + imageSize, posY + imageSize);
        posX = imageSize;
        posY = 15 * 69 - imageSize;
        velX = playerSpeed;
        velY = 0;
        collisions = new boolean[]{false, false, false, false};
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(playerSprite, spriteFrame, Game.scale(spritePosition), null);
    }

    public void update() {
        int framePosX = spriteWidth * ((int) frameCount % 4);
        int framePosY = spriteHeight * ((int) frameCount / 4 % 3);
        spriteFrame = new Rect(framePosX, framePosY, framePosX + spriteWidth, framePosY + spriteHeight);
        frameCount++;

        posX += velX;   //posX, posY gives top left corner of spritePosition rectangle
        posY -= velY;
        collisions = LevelCreator.checkCollisionsAndUpdate(this);   //collisions handled by LevelCreator class
        if(!collisions[3])
            velX = playerSpeed;
        if(!collisions[1])
            velY += gravity;
        spritePosition.offsetTo(posX, posY);
    }

    public void jump() {
        velY = jumpVelocity;
    }

    public void setX(int posX, int velX) {
        this.posX = posX;
        this.velX = velX;
    }

    public void setY(int posY, int velY) {
        this.posY = posY;
        this.velY = velY;
    }

    public Pair getPos() {
        return new Pair(posX, posY);
    }

    public Rect getBounds() {
        return spritePosition;
    }

    public Rect getBoundTop() {
        return new Rect(spritePosition.left, spritePosition.top, spritePosition.right, spritePosition.top);
    }

    public Rect getBoundBottom() {
        return new Rect(spritePosition.left, spritePosition.bottom, spritePosition.right, spritePosition.bottom);
    }

    public Rect getBoundLeft() {
        return new Rect(spritePosition.left, spritePosition.top, spritePosition.left, spritePosition.bottom);
    }

    public Rect getBoundRight() {
        return new Rect(spritePosition.right, spritePosition.top, spritePosition.right, spritePosition.bottom);
    }
}
