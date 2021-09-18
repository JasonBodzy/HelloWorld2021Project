import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Ground {
    static Image image;
    int x,y, width, height;
    Toolkit t = Toolkit.getDefaultToolkit();

    Ground (int x, int y) {
        this.x = x;
        this.y = y;
        this.image = image;

        this.width = 40;
        this.height = 40;



        try{
            image = ImageIO.read(this.getClass().getResource("/Assets/ground.png"));

        } catch (IOException e) {
            System.out.println("image load fail");
        }
    }

}
