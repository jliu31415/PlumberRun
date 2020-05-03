package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

class Player {
    private final Bitmap playerRunningSprite, playerThrowingSprite;
    private final LevelCreator levelCreator;
    private final double runningSpriteWidth, runningSpriteHeight, throwingSpriteWidth, throwingSpriteHeight;
    private Rect spriteFrame;   //frame of Bitmap to extract
    private Rect playerPosition, spritePosition;    //sprite drawn larger to make collisions more realistic
    private long frameCount;
    private int framePosX, framePosY;
    private final int imageSize = 138;   //twice tile size
    private final int hitBoxBufferLR = 30;    //hit box padding
    private final int boundsBuffer = 3; //prevent getBounds functions to overlap
    private int playerSpeed = 10;
    private int playerAcceleration = -1;
    private int velX = playerSpeed, velY = 0;
    private int jumpVelocity = 30;
    private boolean canJump = true;
    private boolean throwCock = false;
    private boolean throwRelease = false;
    private boolean slowMotion = false;
    private int gravity = -2;

    public Player(Bitmap playerRunningSprite, Bitmap playerThrowingSprite, LevelCreator levelCreator) {
        this.playerRunningSprite = playerRunningSprite;
        this.playerThrowingSprite = playerThrowingSprite;
        this.levelCreator = levelCreator;
        runningSpriteWidth = playerRunningSprite.getWidth() / 4.0;
        runningSpriteHeight = playerRunningSprite.getHeight() / 3.0;
        throwingSpriteWidth = playerThrowingSprite.getWidth() / 4.0;
        throwingSpriteHeight = playerThrowingSprite.getHeight() / 3.0;
        playerPosition = new Rect(-imageSize + hitBoxBufferLR, 15 * 69 - imageSize,
                -hitBoxBufferLR, 15 * 69);
        spritePosition = new Rect(-imageSize, 15 * 69 - imageSize, 0, 15 * 69);
    }

    public void draw(Canvas canvas) {
        spritePosition.offsetTo(playerPosition.left - hitBoxBufferLR, playerPosition.top);
        if (throwCock || throwRelease)
            canvas.drawBitmap(playerThrowingSprite, spriteFrame, Game.scale(spritePosition), null);
        else
            canvas.drawBitmap(playerRunningSprite, spriteFrame, Game.scale(spritePosition), null);
    }

    public void update() {
        if (throwCock || throwRelease) {
            if (throwCock && frameCount == 5) frameCount--;
            if (frameCount == 12) throwRelease = false;
            framePosX = (int) (throwingSpriteWidth * (frameCount % 4));
            framePosY = (int) (throwingSpriteHeight * (frameCount / 4 % 3));
            spriteFrame = new Rect(framePosX, framePosY, framePosX + (int) throwingSpriteWidth, framePosY + (int) throwingSpriteHeight);
        } else {
            framePosX = (int) (runningSpriteWidth * (frameCount % 4));
            framePosY = (int) (runningSpriteHeight * (frameCount / 4 % 3));
            spriteFrame = new Rect(framePosX, framePosY, framePosX + (int) runningSpriteWidth, framePosY + (int) runningSpriteHeight);
        }
        frameCount++;   //frameCount reset in Game when transitioning between animations

        playerPosition.offset(velX, -velY);
        if (velX < playerSpeed) playerAcceleration = Math.abs(playerAcceleration);
        else if (velX > playerSpeed) playerAcceleration = -Math.abs(playerAcceleration);
        velX += playerAcceleration;
        velY += gravity;
        if (slowMotion) velY = Math.max(velY, -1);
        else velY = Math.max(velY, -30);
        //collisions handled by LevelCreator class, updatePos() called in LevelCreator
        levelCreator.checkCollisionsAndUpdate(this);
    }

    public void slowMotion(boolean slow) {
        if (slow) {
            slowMotion = true;
            playerSpeed = 1;
            gravity = -1;
            //velY handled in update; set floor to -1
        } else {
            slowMotion = false;
            playerSpeed = 10;
            gravity = -2;
        }
    }

    public void jump() {
        if (canJump) velY = jumpVelocity;
        canJump = false;
    }

    public void resetJump() {
        canJump = true;
    }

    public void setFrameCount(int frameCount) {
        this.frameCount = frameCount;
    }

    public void setThrowing(boolean throwCock, boolean throwRelease) {
        this.throwCock = throwCock;
        this.throwRelease = throwRelease;
    }

    public int getPosX() {
        return playerPosition.left;
    }

    public int getPosY() {
        return playerPosition.top;
    }

    public int getVelX() {
        return velX;
    }

    public int getVelY() {
        return velY;
    }

    public void setPosX(int posX) {
        playerPosition.offsetTo(posX, playerPosition.top);
    }

    public void setPosY(int posY) {
        playerPosition.offsetTo(playerPosition.left, posY);
    }

    public void setVelX(int velX) {
        this.velX = velX;
    }

    public void setVelY(int velY) {
        this.velY = velY;
    }

    public Rect getBounds() {
        return playerPosition;
    }

    public Rect getBoundTop() {
        return new Rect(playerPosition.left + boundsBuffer, playerPosition.top,
                playerPosition.right - boundsBuffer, playerPosition.top);
    }

    public Rect getBoundBottom() {
        return new Rect(playerPosition.left + boundsBuffer, playerPosition.bottom,
                playerPosition.right - boundsBuffer, playerPosition.bottom);
    }

    public Rect getBoundLeft() {
        return new Rect(playerPosition.left, playerPosition.top + boundsBuffer, playerPosition.left,
                playerPosition.bottom - boundsBuffer);
    }

    public Rect getBoundRight() {
        return new Rect(playerPosition.right, playerPosition.top + boundsBuffer, playerPosition.right,
                playerPosition.bottom - boundsBuffer);
    }
}
