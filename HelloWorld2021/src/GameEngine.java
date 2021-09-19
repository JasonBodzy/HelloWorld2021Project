import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;

public class GameEngine extends Canvas implements MouseListener, MouseMotionListener, KeyListener{

    public static int WIDTH = 2000;
    public static int HEIGHT = 1000;

    boolean running = false;

    char mode;
    boolean help = false;


    public static ArrayList<Ground> groundArray = new ArrayList<Ground>();
    public static ArrayList<Enemy> enemyArray = new ArrayList<>();

    public static Player player;
    public static boolean playerExists = false;
    public static boolean enemyExists = false;
    long t1;

    public static Flag flag;
    public static boolean flagExists = false;

    Image groundImage, dirtImage, playerImage, enemyImage, flagImage;

    //UI Stuff
    public static JFrame frame = new JFrame();
    public static JFrame subframe = new JFrame();
    public static JPanel panel = new JPanel();
    public static JTextField textField = new JTextField();
    public static JLabel label = new JLabel();
    public static Canvas canvas;
    public static JButton button;

    public static boolean loadNeeded = false;
    public static boolean saveNeeded = false;
    public static String levelName;

    public GameEngine(){
        addMouseListener(this);
        addKeyListener(this);
        addMouseMotionListener(this);

        try{
            groundImage = ImageIO.read(this.getClass().getResource("/Assets/ground.png"));
            dirtImage = ImageIO.read(this.getClass().getResource("/Assets/dirt.png"));
            playerImage = ImageIO.read(this.getClass().getResource("/Assets/player.png"));
            enemyImage = ImageIO.read(this.getClass().getResource("/Assets/enemy.png"));
            flagImage = ImageIO.read(this.getClass().getResource("/Assets/flag.png"));

        } catch (IOException e) {
            System.out.println("image load fail");
        }
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
                if((player.y == g.y || player.y + 40 == g.y) && (player.x + 40 >= g.x && !(player.x >= g.x + 40 ))){
                    player.x -= player.xVel;
                }
                if((player.y == g.y || player.y + 40 == g.y) && (player.x <= g.x + 40 && !(player.x + 40 <= g.x ))){
                    player.x += -player.xVel;
                }

                //Player-Flag Collision
                if((player.y == flag.y) && (player.x + 40 >= flag.x && !(player.x >= flag.x + 40))){
                    System.out.println("Win!");
                }
                if(player.y == flag.y && player.x <= flag.x + 40 && !(player.x + 40 <= flag.x)){
                    System.out.println("Win!");
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
                            if(player.y + 80 < e.y - 30 || player.y + 80 > e.y - 40 && !player.onGround){
                                e.x = -1000;
                                player.yVel = -10;
                                player.onGround = false;
                                t1 = System.currentTimeMillis();
                            } else {
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


        frame = new JFrame("Hello World 2021 Game Engine");

        canvas = new GameEngine();
        canvas.setSize(WIDTH, HEIGHT);


        textField = new JTextField();
        panel = new JPanel();
        panel.setSize(WIDTH / 4, HEIGHT / 10);
        label = new JLabel();
        label.setText("Enter a level name (levelName.txt). \n");
        panel.add(label);
        textField.setText("levelName.txt");
        textField.setSize(100, 20);
        textField.setBounds(WIDTH / 2, 100, WIDTH / 4, HEIGHT / 15);
        panel.add(textField);
        button = new JButton();
        button.setText("Load");
        button.setSize(100, 20);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if(loadNeeded){
                    GameEngine.loadLevel();
                }
                if(saveNeeded){
                    GameEngine.writeLevel();
                }

            }
        });
        panel.add(button);
        panel.doLayout();

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
            for(Ground grnd2 : groundArray){
                if(grnd.x == grnd2.x && grnd.y == grnd2.y + 40){
                    grnd.isDirt = true;
                }
            }
            if(grnd.isDirt){
                g.drawImage(dirtImage, grnd.x, grnd.y, 40, 40,this);
            } else {
                g.drawImage(groundImage, grnd.x, grnd.y, 40, 40,this);
            }
        }

        for(Enemy e : enemyArray){
            g.drawImage(enemyImage, e.x, e.y, this);
        }

        if(playerExists){
            g.drawImage(playerImage, player.x, player.y, this);
        }

        if(flagExists){
            g.drawImage(flagImage, flag.x, flag.y, this);
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
            if(e.getKeyCode() == KeyEvent.VK_F){
                mode = 'F';
            }
            //Writes level
            if(e.getKeyCode() == KeyEvent.VK_S){
                subframe = new JFrame("Save Level");
                button.setText("Save");
                label.setText("Enter a level name (levelName.txt). \n");
                subframe.add(panel);
                subframe.setVisible(true);
                subframe.pack();
                loadNeeded = false;
                saveNeeded = true;
            }
            //Reads written level
            if(e.getKeyCode() == KeyEvent.VK_L){

                subframe = new JFrame("Load Level");
                button.setText("Load");
                label.setText("Enter a level name (levelName.txt). \n");
                subframe.add(panel);
                subframe.setVisible(true);
                subframe.pack();
                saveNeeded = false;
                loadNeeded = true;
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
        if(e.getButton() == 1) {
            switch (mode) {
                case 'G':
                    int newXPos = e.getX();
                    while (newXPos % 40 != 0) {
                        newXPos--;
                    }
                    int newYPos = e.getY();
                    while (newYPos % 40 != 0) {
                        newYPos--;
                    }
                    groundArray.add(new Ground(newXPos, newYPos));
                    break;
                case 'F':
                    int newFlagXPos = e.getX();
                    while (newFlagXPos % 40 != 0) {
                        newFlagXPos--;
                    }
                    int newFlagYPos = e.getY();
                    while (newFlagYPos % 40 != 0) {
                        newFlagYPos--;
                    }
                    flag = new Flag(newFlagXPos, newFlagYPos);
                    flagExists = true;
                    break;
                case 'P':
                    int newPlayerXPos = e.getX();
                    while (newPlayerXPos % 40 != 0) {
                        newPlayerXPos--;
                    }
                    int newPlayerYPos = e.getY();
                    while (newPlayerYPos % 40 != 0) {
                        newPlayerYPos--;
                    }
                    player = new Player(newPlayerXPos, newPlayerYPos);
                    player.yVel = 9;
                    playerExists = true;
                    break;
                case 'E':
                    int newEnemyXPos = e.getX();
                    while (newEnemyXPos % 40 != 0) {
                        newEnemyXPos--;
                    }
                    int newEnemyYPos = e.getY();
                    while (newEnemyYPos % 40 != 0) {
                        newEnemyYPos--;
                    }
                    enemyArray.add(new Enemy(newEnemyXPos, newEnemyYPos));
                    enemyArray.get(enemyArray.size() - 1).xVel = 1;
                    enemyArray.get(enemyArray.size() - 1).yVel = 9;
                    enemyExists = true;
                    break;
            }

        }

        if(e.getButton() == 3){
            for(Ground g : groundArray){
                if(e.getX() >= g.x && e.getX() <= g.x + 40 && e.getY() >= g.y && e.getY() <= g.y + 40){
                    g.x = -1000;
                }
            }
            for(Enemy v : enemyArray){
                if(e.getX() >= v.x && e.getX() <= v.x + 40 && e.getY() >= v.y && e.getY() <= v.y + 40){
                    v.x = -1000;
                }
            }
            if(playerExists) {
                if (e.getX() > +player.x && e.getX() <= player.x + 40 && e.getY() >= player.y && e.getY() <= player.y + 80) {
                    player.x = 10000;
                }
            }
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
        if(e.getButton() == 1) {
            switch (mode) {
                case 'G':
                    int newXPos = e.getX();
                    while (newXPos % 40 != 0) {
                        newXPos--;
                    }
                    int newYPos = e.getY();
                    while (newYPos % 40 != 0) {
                        newYPos--;
                    }
                    groundArray.add(new Ground(newXPos, newYPos));
                    break;
                case 'E':
                    int newEnemyXPos = e.getX();
                    while (newEnemyXPos % 40 != 0) {
                        newEnemyXPos--;
                    }
                    int newEnemyYPos = e.getY();
                    while (newEnemyYPos % 40 != 0) {
                        newEnemyYPos--;
                    }
                    enemyArray.add(new Enemy(newEnemyXPos, newEnemyYPos));
                    enemyArray.get(enemyArray.size() - 1).xVel = 1;
                    enemyArray.get(enemyArray.size() - 1).yVel = 9;
                    enemyExists = true;
                    break;
            }
        }

        if(e.getButton() == 3){
            for(Ground g : groundArray){
                if(e.getX() >= g.x && e.getX() <= g.x + 40 && e.getY() >= g.y && e.getY() <= g.y + 40){
                    g.x = -1000;
                }
            }
            for(Enemy v : enemyArray){
                if(e.getX() >= v.x && e.getX() <= v.x + 40 && e.getY() >= v.y && e.getY() <= v.y + 40){
                    v.x = -1000;
                }
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    //Reading/Writing Levels

    public static void loadLevel(){
        if(textField.getText().contains(".txt")){
            subframe.setVisible(false);
            try {

                levelName = textField.getText();

                File myObj = new File(levelName);
                Scanner reader = new Scanner(myObj);

                int ex = 0;
                int ey = 0;
                int gx = 0;
                int gy = 0;

                while (reader.hasNextLine()) {
                    String data = reader.nextLine();
                    if(!playerExists){
                        int playerX = 0;
                        int playerY = 0;
                        if (data.substring(0,2).equals("px")){
                            playerX = Integer.parseInt(data.substring(2, data.length()));
                        }
                        if (data.substring(0,2).equals("py")){
                            playerY = Integer.parseInt(data.substring(2, data.length()));
                        }
                        player = new Player(playerX, playerY);
                        player.yVel = 9;
                        playerExists = true;
                    }
                    if (data.substring(0,2).equals("px") && playerExists){
                        player.x = Integer.parseInt(data.substring(2, data.length()));
                    }
                    if (data.substring(0,2).equals("py") && playerExists){
                        player.y = Integer.parseInt(data.substring(2, data.length()));
                    }
                    if(!flagExists){
                        int flagX = 0;
                        int flagY = 0;
                        if (data.substring(0,2).equals("px")){
                            flagX = Integer.parseInt(data.substring(2, data.length()));
                        }
                        if (data.substring(0,2).equals("py")){
                            flagY = Integer.parseInt(data.substring(2, data.length()));
                        }
                        flag = new Flag(flagX, flagX);
                        flagExists = true;
                    }
                    if (data.substring(0,2).equals("fx") && flagExists){
                        flag.x = Integer.parseInt(data.substring(2, data.length()));
                    }
                    if (data.substring(0,2).equals("fy") && flagExists){
                        flag.y = Integer.parseInt(data.substring(2, data.length()));
                    }

                    if(data.substring(0,2).equals("gx")){
                        gx = Integer.parseInt(data.substring(2, data.length()));
                    }
                    if(data.substring(0,2).equals("gy")){
                        gy = Integer.parseInt(data.substring(2, data.length()));
                        groundArray.add(new Ground(gx, gy));
                    }

                    if(data.substring(0,2).equals("ex")){
                        ex = Integer.parseInt(data.substring(2, data.length()));
                    }
                    if(data.substring(0,2).equals("ey")){
                        ey = Integer.parseInt(data.substring(2, data.length()));
                        enemyArray.add(new Enemy(ex, ey));
                        enemyArray.get(enemyArray.size() - 1).xVel = 1;
                        enemyArray.get(enemyArray.size() - 1).yVel = 9;
                        enemyExists = true;
                    }


                }
                reader.close();
            } catch (FileNotFoundException zz) {
                System.out.println("An error occurred.");
                zz.printStackTrace();
            }
        } else {
            label.setText("Error: File does not exist or is not .txt");
            subframe.pack();
        }
    }
    public static void writeLevel(){
        if(textField.getText().contains(".txt")) {
            subframe.setVisible(false);
            try {
                levelName = textField.getText();
                FileWriter writer = new FileWriter(levelName);
                if (playerExists) {
                    writer.write("px" + player.x + "\n");
                    writer.write("py" + player.y + "\n");
                }
                if (flagExists) {
                    writer.write("fx" + flag.x + "\n");
                    writer.write("fy" + flag.y + "\n");
                }
                for (Ground g : groundArray) {
                    writer.write("gx" + g.x + "\n");
                    writer.write("gy" + g.y + "\n");
                }
                for (Enemy v : enemyArray) {
                    writer.write("ex" + v.x + "\n");
                    writer.write("ey" + v.y + "\n");
                }
                writer.close();
            } catch (IOException z) {
                System.out.println(levelName + " already exists.");
                z.printStackTrace();
            }
        } else {
            label.setText("Error: Please conclude your level name with .txt");
            subframe.pack();
        }
    }
}
