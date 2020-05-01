package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

class Player {
    private final Bitmap playerSprite;
    private final LevelCreator levelCreator;
    private final int spriteWidth, spriteHeight;
    private Rect spriteFrame;   //frame of Bitmap to extract
    private Rect spritePosition;
    private long frameCount = 0;
    private int imageSize = 138;   //twice tile size
    private int posX, posY, velX, velY; //velY positive going up
    private int playerSpeed = 15;
    private int playerAcceleration = 1;
    private int jumpVelocity = 30;
    private boolean canJump = true;
    private int gravity = -2;

    public Player(Bitmap playerSprite, LevelCreator levelCreator) {
        this.playerSprite = playerSprite;
        this.levelCreator = levelCreator;
        spriteWidth = playerSprite.getWidth() / 4;
        spriteHeight = playerSprite.getHeight() / 3;
        spritePosition = new Rect(posX, posY, posX + imageSize, posY + imageSize);
        posX = -imageSize * 2;
        posY = 15 * 69 - imageSize;
        velX = playerSpeed;
        velY = 0;
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
        if (velX < playerSpeed) playerAcceleration = Math.abs(playerAcceleration);
        else if (velX > playerSpeed) playerAcceleration = -Math.abs(playerAcceleration);
        velX += playerAcceleration;
        velY += gravity;
        //collisions handled by LevelCreator class, updatePosRect() called in LevelCreator
        levelCreator.checkCollisionsAndUpdate(this);
    }

    public void jump() {
        if (canJump) velY = jumpVelocity;
        canJump = false;
    }

    public void resetJump() {
        canJump = true;
    }

    public void speedUp(int playerSpeed, int playerAcceleration) {
        this.playerSpeed = playerSpeed;
        this.playerAcceleration = playerAcceleration;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public int getVelX() {
        return velX;
    }

    public int getVelY() {
        return velY;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public void setVelX(int velX) {
        this.velX = velX;
    }

    public void setVelY(int velY) {
        this.velY = velY;
    }

    public void updatePosRect() {
        spritePosition.offsetTo(posX, posY);
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
