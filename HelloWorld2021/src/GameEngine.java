import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GameEngine extends Canvas implements MouseListener, MouseMotionListener, KeyListener{

    public static int WIDTH = 2000;
    public static int HEIGHT = 1000;

    boolean running = false;

    char mode;
    boolean help = false;

    ArrayList<Ground> groundArray = new ArrayList<Ground>();

    Player player;

    boolean playerExists = false;


    public GameEngine(){
        addMouseListener(this);
        addKeyListener(this);
        addMouseMotionListener(this);
    }

    void update() throws InterruptedException{

        if(running && playerExists) {
            player.x += player.xVel;
            player.y += player.yVel;
            player.yVel = 9;
            for(Ground g: groundArray){
                if(player.y + 80 >= g.y && player.y <= g.y - 40 && player.x + 40 >= g.x && player.x <= g.x + 40){
                    System.out.println("onGround");
                    player.yVel = 0;
                    player.y = g.y - 80;
                }
            }
        }





        repaint();
    }

    public static void main(String[] args) {


        JFrame frame = new JFrame("Hello World 2021 Game Engine");

        Canvas canvas = new GameEngine();
        canvas.setSize(WIDTH, HEIGHT);
        frame.add(canvas);

        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);



    }

    public void paint(Graphics g) {

        //Draws Tile Grid
        if(!running) {
            int i = 0;
            while (i < WIDTH) {
                if (i % 40 == 0) {
                    g.drawLine(i, 0, i, HEIGHT);
                }
                i++;
            }

            i = 0;
            while (i < HEIGHT) {
                if (i % 40 == 0) {
                    g.drawLine(0, i, WIDTH, i);
                }
                i++;
            }
        }

        if(help){
            System.out.println("Instructions add here");
        }

        for(Ground grnd : groundArray){
            g.drawImage(grnd.image, grnd.x, grnd.y, this);
        }

        if(playerExists){
            g.drawImage(player.image, player.x, player.y, this);
        }





        try {
            update();
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    // Key Listeners

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

        //Player Controls
        if(playerExists) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                player.xVel = -4;
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                player.xVel = 4;
            }
            if(e.getKeyCode() == KeyEvent.VK_SPACE){
                player.y -= 120;
            }
        }

        //Changes Toggled Edit Modes
        if(e.getKeyCode() == KeyEvent.VK_R){
            mode = 'R';
            running = true;
        }
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
            mode = 'E';
            running = false;
        }
        if(mode != 'R') {
            if (e.getKeyCode() == KeyEvent.VK_G) {
                mode = 'G';
            }
            if (e.getKeyCode() == KeyEvent.VK_P) {
                mode = 'P';
            }
            if (e.getKeyCode() == KeyEvent.VK_H) {
                mode = 'H';
                help = true;

            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_H){
            help = false;
        }

        if(playerExists) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                player.xVel = 0;
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                player.xVel = 0;
            }
        }

    }

    // Mouse Listener

    @Override
    public void mouseClicked(MouseEvent e){
    }

    @Override
    public void mousePressed(MouseEvent e) {

        switch(mode){
            case 'G':
                int newXPos = e.getX();
                while(newXPos % 40 != 0){
                    newXPos --;
                }
                int newYPos = e.getY();
                while(newYPos % 40 != 0){
                    newYPos --;
                }
                groundArray.add(new Ground(newXPos, newYPos));
                break;
            case 'P':
                int newPlayerXPos = e.getX();
                while(newPlayerXPos % 40 != 0){
                    newPlayerXPos --;
                }
                int newPlayerYPos = e.getY();
                while(newPlayerYPos % 40 != 0){
                    newPlayerYPos --;
                }
                player = new Player(newPlayerXPos, newPlayerYPos);
                playerExists = true;
                break;
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        switch(mode){
            case 'G':
                int newXPos = e.getX();
                while(newXPos % 40 != 0){
                    newXPos --;
                }
                int newYPos = e.getY();
                while(newYPos % 40 != 0){
                    newYPos --;
                }
                groundArray.add(new Ground(newXPos, newYPos));
                break;

        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
