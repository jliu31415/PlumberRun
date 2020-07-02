package jliu.plumberrun;

final class Constants {
    static final int tileSize = 90;

    static final int playerSize = tileSize * 2;
    static final double playerMaxSpeedX = tileSize / 5.0;
    static final double playerJumpVel = tileSize / 3.0;
    static final double playerGravity = -tileSize / 50.0;

    static final int plungerHeight = tileSize * 2;
    static final double plungerSpeed = tileSize / 2.0;
    static final double plungerTrailRadius = tileSize / 50.0;
    static final double projectileGravity = -tileSize / 80.0;

    static final int enemySize = tileSize * 2;
    static final double enemySpeed = tileSize / 15.0;

    static final int flagHeight = tileSize * 3;
    static final double fragmentSize = tileSize / 10.0;   //firework fragments
    static final double fireworkLaunchVel = tileSize / 3.0;
}
