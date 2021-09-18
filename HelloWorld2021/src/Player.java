import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class Player {

    static Image image;
    int x,y, width, height, xVel, yVel;

    Player (int x, int y) {
        this.x = x;
        this.y = y;

        this.image = image;

        this.width = 40;
        this.height = 80;
        this.xVel = xVel;
        this.yVel = yVel;
        try{
            image = ImageIO.read(this.getClass().getResource("/Assets/player.png"));

        } catch (IOException e) {
            System.out.println("image load fail");
        }
    }
}
