import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class Enemy {

    static Image image;
    int x,y, width, height, xVel, yVel, speed;

    Enemy (int x, int y) {
        this.x = x;
        this.y = y;

        this.image = image;

        this.width = 40;
        this.height = 80;
        this.xVel = xVel;
        this.yVel = yVel;

        this.speed = 4;
        try{
            image = ImageIO.read(this.getClass().getResource("/Assets/enemy.png"));

        } catch (IOException e) {
            System.out.println("image load fail");
        }
    }
}