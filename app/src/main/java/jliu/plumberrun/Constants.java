package jliu.plumberrun;

final class Constants {
    static final int fade = 30;

    static final int tileSize = 90;

    static final int playerSize = tileSize * 2;
    static final double playerMaxSpeedX = tileSize / 7.0;
    static final double playerJumpVel = tileSize / 2.3;
    static final double playerGravity = -tileSize / 38.0;

    static final int plungerHeight = tileSize * 2;
    static final double plungerSpeed = tileSize / 2.6;
    static final double plungerTrailRadius = tileSize / 50.0;
    static final double projectileGravity = -tileSize / 80.0;

    static final int enemySize = (int) (tileSize * 2.5);  //extra space in bitmap needed for propeller
    static final double enemySpeed = tileSize / 20.0;
}
