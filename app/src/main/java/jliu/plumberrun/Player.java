package jliu.plumberrun;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

class Player {
    private final Bitmap playerRunningSprite, playerThrowingSprite;
    private final LevelCreator levelCreator;
    private final double runningSpriteWidth, runningSpriteHeight, throwingSpriteWidth, throwingSpriteHeight;
    private Rect spriteFrame;   //frame of Bitmap to extract
    private Rect spritePosition;
    private long frameCount;
    private int framePosX, framePosY;
    private int imageSize = 138;   //twice tile size
    private int velX, velY; //velY positive going up
    private int playerSpeed = 10;
    private int playerAcceleration = 1;
    private int jumpVelocity = 30;
    private boolean canJump = true;
    private boolean throwCock = false;
    private boolean throwRelease = false;
    private int gravity = -2;

    public Player(Bitmap playerRunningSprite, Bitmap playerThrowingSprite, LevelCreator levelCreator) {
        this.playerRunningSprite = playerRunningSprite;
        this.playerThrowingSprite = playerThrowingSprite;
        this.levelCreator = levelCreator;
        runningSpriteWidth = playerRunningSprite.getWidth() / 4.0;
        runningSpriteHeight = playerRunningSprite.getHeight() / 3.0;
        throwingSpriteWidth = playerThrowingSprite.getWidth() / 4.0;
        throwingSpriteHeight = playerThrowingSprite.getHeight() / 3.0;
        spritePosition = new Rect(-imageSize, 15 * 69 - imageSize, 0, 15 * 69);
        velX = playerSpeed;
        velY = 0;
    }

    public void draw(Canvas canvas) {
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

        spritePosition.offset(velX, -velY);
        if (velX < playerSpeed) playerAcceleration = Math.abs(playerAcceleration);
        else if (velX > playerSpeed) playerAcceleration = -Math.abs(playerAcceleration);
        velX += playerAcceleration;
        velY += gravity;
        //collisions handled by LevelCreator class, updatePos() called in LevelCreator
        levelCreator.checkCollisionsAndUpdate(this);
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
        return spritePosition.left;
    }

    public int getPosY() {
        return spritePosition.top;
    }

    public int getVelX() {
        return velX;
    }

    public int getVelY() {
        return velY;
    }

    public void setPosX(int posX) {
        spritePosition.offsetTo(posX, spritePosition.top);
    }

    public void setPosY(int posY) {
        spritePosition.offsetTo(spritePosition.left, posY);
    }

    public void setVelX(int velX) {
        this.velX = velX;
    }

    public void setVelY(int velY) {
        this.velY = velY;
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
