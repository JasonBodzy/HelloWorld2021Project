import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class GameEngine extends Canvas implements MouseListener, MouseMotionListener, KeyListener{

    public static int WIDTH = 2000;
    public static int HEIGHT = 1000;

    boolean running = false;

    char mode;
    boolean help = false;


    ArrayList<Ground> groundArray = new ArrayList<Ground>();
    ArrayList<Enemy> enemyArray = new ArrayList<>();

    Player player;
    boolean playerExists = false;
    boolean enemyExists = false;
    long t1;

    public GameEngine(){
        addMouseListener(this);
        addKeyListener(this);
        addMouseMotionListener(this);
    }


    //Variable Updates Called Every Frame
    void update() throws InterruptedException{

        //Player Physics
        if(running && playerExists) {
            player.x += player.xVel;
            player.y += player.yVel;
            if(enemyExists) {
                for (Enemy e : enemyArray) {
                    e.x += e.xVel;
                    e.y += e.yVel;
                    e.yVel = 9;
                }
            }

            //Jump -> Fall
            if(player.yVel == -10) {
                if(System.currentTimeMillis() - t1 >= 250) {
                    while(player.yVel < 9) {
                        player.yVel++;
                    }
                }
            }
            //Player Interactions with the ground
            for(Ground g: groundArray){
                if(player.y + 80 >= g.y && player.y <= g.y + 40 && player.x + 40 >= g.x  && player.x <= g.x + 40){
                    player.yVel = 0;
                    player.onGround = true;
                    if(player.y + 80 - g.y <= 10) {
                        player.y = g.y - 80;
                    }
                }
                //Head Bump
                if(player.y <= g.y + 40 && player.y + 80 >= g.y && player.x + 40 >= g.x  && player.x <= g.x + 40 && player.yVel < 9){
                    player.yVel = 9;
                }

                //Wall Interactions
                if((player.y == g.y || player.y + 40 >= g.y) && (player.x + 40 >= g.x && !(player.x >= g.x + 40 ))){
                    player.x -= player.xVel;
                }
                if((player.y == g.y || player.y + 40 >= g.y) && (player.x <= g.x + 40 && !(player.x + 40 <= g.x ))){
                    player.x += -player.xVel;
                }
                if(enemyExists) {
                    for (Enemy e : enemyArray) {

                        //Enemy Gravity
                        if(e.y + 40 >= g.y && e.y <= g.y + 40 && e.x + 40 >= g.x  && e.x <= g.x + 40){
                            e.yVel = 0;
                            if((g.y - e.y) > 0){
                                e.y = g.y - 40;
                            }
                        }

                        //Enemy Bounce
                        if ((e.y >= g.y ) && (e.x + 40 >= g.x && !(e.x >= g.x + 40)) && e.xVel > 0) {
                            e.xVel = -e.xVel;
                        } else if ((e.y >= g.y ) && (e.x <= g.x + 40 && !(e.x + 40 <= g.x)) && e.xVel < 0) {
                            e.xVel = -e.xVel;
                        }

                        //Enemy-Player Collisions
                        if((e.y >= player.y && e.y <= player.y + 80) && (player.x + 40 >= e.x && !(player.x > e.x + 40))){

                            //Kill time
                            if(player.y + 80 < e.y - 30 || player.y + 80 > e.y - 40 && player.onGround == false){
                                System.out.println("Kill!!!");
                                e.x = -1000;
                                player.yVel = -10;
                                player.onGround = false;
                                t1 = System.currentTimeMillis();
                            } else {
                                System.out.println((player.y + 80) + ", " + (e.y - 30));
                                mode = 'C';
                                running = false;
                            }

                        }

                    }
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

    //Drawing Method -> Called Every frame
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

        for(Enemy e : enemyArray){
            g.drawImage(e.image, e.x, e.y, this);
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
                player.xVel = -player.speed;
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                player.xVel = player.speed;
            }
            if(e.getKeyCode() == KeyEvent.VK_SPACE){
                if(player.onGround) {
                    player.yVel = -10;
                    player.onGround = false;
                    t1 = System.currentTimeMillis();
                }
            }
        }

        //Changes Toggled Edit Modes
        if(e.getKeyCode() == KeyEvent.VK_R){
            mode = 'R';
            running = true;
        }
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
            mode = 'C';
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
            if(e.getKeyCode() == KeyEvent.VK_E){
                mode = 'E';
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
                player.yVel = 9;
                playerExists = true;
                break;
            case 'E':
                int newEnemyXPos = e.getX();
                while(newEnemyXPos % 40 != 0){
                    newEnemyXPos --;
                }
                int newEnemyYPos = e.getY();
                while(newEnemyYPos % 40 != 0){
                    newEnemyYPos --;
                }
                enemyArray.add(new Enemy(newEnemyXPos, newEnemyYPos));
                enemyArray.get(enemyArray.size() - 1).xVel = 1;
                enemyArray.get(enemyArray.size() - 1).yVel = 9;
                enemyExists = true;
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
