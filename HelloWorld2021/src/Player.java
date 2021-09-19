import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class Player {

    int x,y, width, height, xVel, yVel, speed;
    boolean onGround;

    Player (int x, int y) {
        this.x = x;
        this.y = y;

        this.width = 40;
        this.height = 80;
        this.xVel = xVel;
        this.yVel = yVel;
        this.onGround = onGround;

        this.speed = 4;

    }
}
