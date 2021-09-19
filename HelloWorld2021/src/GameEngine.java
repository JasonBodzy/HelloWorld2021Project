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
import java.util.Random;
import java.util.Scanner;

public class GameEngine extends Canvas implements MouseListener, MouseMotionListener, KeyListener{

    public static int WIDTH = 2000;
    public static int HEIGHT = 1000;

    boolean running = false;

    char mode;
    boolean help = false;


    public static ArrayList<Ground> groundArray = new ArrayList<Ground>();
    public static ArrayList<Enemy> enemyArray = new ArrayList<>();
    public static ArrayList<Platform> platformArray = new ArrayList<>();
    public static ArrayList<FlyingEnemy> flyingEnemyArray = new ArrayList<>();

    public static Player player;
    public static boolean playerExists = false;
    public static boolean enemyExists = false;
    public static boolean flyingEnemyExists = false;
    long t1;

    public static Flag flag;
    public static boolean flagExists = false;

    Image groundImage, dirtImage, playerImage, playerImageBK, enemyImage, flagImage, platformImage, flyingEnemyImage, flyingEnemyImageBK, helpImage;

    //UI Stuff
    public static JFrame frame = new JFrame();
    public static JFrame subframe = new JFrame();
    public static JPanel panel = new JPanel();
    public static JTextField textField = new JTextField();
    public static JLabel label = new JLabel();

    public static String modeStatus = "Construct Mode";
    private static Font serifFont = new Font("SanSerif", Font.BOLD, 34);

    public static Canvas canvas;
    public static JButton button;

    public static boolean loadNeeded = false;
    public static boolean saveNeeded = false;
    public static String levelName;

    int cloudX = 0;

    public GameEngine(){
        addMouseListener(this);
        addKeyListener(this);
        addMouseMotionListener(this);

        try{
            groundImage = ImageIO.read(this.getClass().getResource("/Assets/ground.png"));
            dirtImage = ImageIO.read(this.getClass().getResource("/Assets/dirt.png"));
            playerImage = ImageIO.read(this.getClass().getResource("/Assets/player.png"));
            playerImageBK = ImageIO.read(this.getClass().getResource("/Assets/playerBK.png"));
            enemyImage = ImageIO.read(this.getClass().getResource("/Assets/enemy.png"));
            flagImage = ImageIO.read(this.getClass().getResource("/Assets/flag.png"));
            platformImage = ImageIO.read(this.getClass().getResource("/Assets/platform.png"));
            flyingEnemyImage = ImageIO.read(this.getClass().getResource("/Assets/flyingEnemy.png"));
            flyingEnemyImageBK = ImageIO.read(this.getClass().getResource("/Assets/flyingEnemyBK.png"));
            helpImage = ImageIO.read(this.getClass().getResource("/Assets/help.png"));




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
            if(player.y > HEIGHT){
                mode = 'C';
                modeStatus = "Player Killed, Construct Mode";
                running = false;
            }
            if(enemyExists) {
                for (Enemy e : enemyArray) {
                    e.x += e.xVel;
                    e.y += e.yVel;
                    e.yVel = 9;
                }
            }

            if(flyingEnemyExists){
                for (FlyingEnemy b : flyingEnemyArray){
                    b.x += b.xVel;
                }
                for(FlyingEnemy b : flyingEnemyArray) {
                    // Flying enemy bounce
                    for (Ground g : groundArray) {
                        if ((b.y == g.y ) && (b.x + 40 >= g.x && !(b.x >= g.x + 40)) && b.xVel > 0){
                            b.xVel = -b.xVel;
                        } else if((b.y == g.y ) && (b.x <= g.x + 40 && !(b.x + 40 <= g.x)) && b.xVel < 0){
                            b.xVel = -b.xVel;
                        }
                    }
                    // Flying enemy collisions
                    if(player.x + 40 >= b.x && player.x <= b.x + 40 && player.y + 80 >= b.y && player.y <= b.y + 40){
                        //Kill time
                        if((player.y + 80 >= b.y && player.y + 80 <= b.y + 10) && !(player.onGround)){
                            b.x = -1000;
                            b.y = -1000;
                            player.yVel = -10;
                            player.onGround = false;
                            t1 = System.currentTimeMillis();
                        } else {
                            mode = 'C';
                            modeStatus = "Player Killed, Construct Mode";
                            running = false;
                        }

                    }
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
            //Player Platform interactions
            for(Platform p: platformArray){
                if(player.y + 80 >= p.y && player.y <= p.y + 20 && player.x + 40 >= p.x  && player.x <= p.x + 40){
                    player.onGround = true;
                    if(player.y + 80 - p.y <= 10){
                        player.y = p.y - 80;
                    }
                }
                if(player.y <= p.y + 40 && player.y + 80 >= p.y && player.x + 40 >= p.x  && player.x <= p.x + 40 && player.yVel < 9){
                    player.yVel = player.yVel;
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
                if((player.y >= g.y && player.y <= g.y + 40) && (player.x + 40 >= g.x && !(player.x >= g.x + 40 ))){
                    player.x -= player.xVel;
                }
                if((player.y + 40 >= g.y && player.y + 40 <= g.y + 40) && (player.x <= g.x + 40 && !(player.x + 40 <= g.x ))){
                    player.x += -player.xVel;
                }

                //Player-Flag Collision
                if(flagExists) {
                    if ((player.y == flag.y) && (player.x + 40 >= flag.x && !(player.x >= flag.x + 40))) {
                        modeStatus = "Level Completed";
                    }
                    if (player.y == flag.y && player.x <= flag.x + 40 && !(player.x + 40 <= flag.x)) {
                        modeStatus = "Level Completed";
                    }
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
                                e.speed = 0;
                                player.yVel = -10;
                                player.onGround = false;
                                t1 = System.currentTimeMillis();
                            } else {
                                mode = 'C';
                                modeStatus = "Player Killed, Construct Mode";
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
        //Draws Mode Status
        g.setColor(Color.black);
        g.setFont(serifFont);
        g.drawString(modeStatus, WIDTH / 2 + 450, HEIGHT  / 2 + 450);

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


        if(running){
            g.setColor(Color.blue);
            g.fillRect(0,0,WIDTH,HEIGHT);

            g.setColor(Color.black);
            g.setFont(serifFont);
            g.drawString(modeStatus, WIDTH / 2 + 450, HEIGHT  / 2 + 450);
        }
        if(help){
            g.drawImage(helpImage, (WIDTH / 2) - helpImage.getWidth(this) / 2 , (HEIGHT / 2) - helpImage.getHeight(this) / 2 ,  this);
        }

        for(Platform p : platformArray){
            g.drawImage(platformImage, p.x, p.y, 40, 20, this);
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

        for(FlyingEnemy b: flyingEnemyArray){
            if(b.xVel >= 0) {
                g.drawImage(flyingEnemyImage, b.x, b.y, this);
            } else{
                g.drawImage(flyingEnemyImageBK, b.x, b.y, this);
            }

        }

        if(playerExists){
            if(player.xVel > 0) {
                if(player.y < 0){
                    player.y = 0;
                }
                g.drawImage(playerImageBK, player.x, player.y, this);
            } else {
                if(player.y < 0){
                    player.y = 0;
                }
                g.drawImage(playerImage, player.x, player.y, this);
            }

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
            if(e.getKeyCode() == KeyEvent.VK_DOWN){
                for(Platform p : platformArray){
                    if(player.y + 80 >= p.y && player.y <= p.y + 20 && player.x + 40 >= p.x  && player.x <= p.x + 40){
                        player.onGround = true;
                        player.y += 4;
                        if(player.y + 80 - p.y <= 10){
                            player.y = p.y - 80;
                        }
                    }
                    if(player.y <= p.y + 40 && player.y + 80 >= p.y && player.x + 40 >= p.x  && player.x <= p.x + 40 && player.yVel < 9){
                        player.yVel = player.yVel;
                    }
                }
            }
        }

        //Changes Toggled Edit Modes
        if(e.getKeyCode() == KeyEvent.VK_R){
            mode = 'R'; // Run mode
            running = true;
            modeStatus = "Run";
        }
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
            mode = 'C'; // Construct Mode
            running = false;
            modeStatus = "Construct Mode";
        }
        if(mode != 'R') {
            if (e.getKeyCode() == KeyEvent.VK_G) {
                mode = 'G'; // Ground Mode
                modeStatus = "Ground Mode";
            }
            if (e.getKeyCode() == KeyEvent.VK_P) {
                mode = 'P'; // Player Mode
                modeStatus = "Player Mode";
            }
            if (e.getKeyCode() == KeyEvent.VK_H) {
                mode = 'H'; // Help Mode
                help = true;
                modeStatus = "Help Mode";
            }
            if(e.getKeyCode() == KeyEvent.VK_E){
                mode = 'E'; // Enemy Mode
                modeStatus = "Enemy Mode";
            }
            if(e.getKeyCode() == KeyEvent.VK_F){
                mode = 'F'; // Flag Mode
                modeStatus = "Finish Mode";
            }
            if(e.getKeyCode() == KeyEvent.VK_MINUS){
                mode = '-'; // Platform Mode
                modeStatus = "Platform Mode";
            }
            if(e.getKeyCode() == KeyEvent.VK_B){
                mode = 'B'; // Flying Enemy Mode
                modeStatus = "Bee Mode";
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
                modeStatus = "Write Level";
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
                modeStatus = "Load Level";
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_H){
            help = false;
            mode = 'C';
            modeStatus = "Construct Mode";
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
                case '-':
                    int newPlatformXPos = e.getX();
                    while (newPlatformXPos % 40 != 0) {
                        newPlatformXPos--;
                    }
                    int newPlatformYPos = e.getY();
                    while (newPlatformYPos % 40 != 0) {
                        newPlatformYPos--;
                    }
                    platformArray.add(new Platform(newPlatformXPos, newPlatformYPos));
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
                    enemyArray.get(enemyArray.size() - 1).xVel = enemyArray.get(enemyArray.size() - 1).speed;
                    enemyArray.get(enemyArray.size() - 1).yVel = 9;
                    enemyExists = true;
                    break;
                case 'B':
                    int newFlyingEnemyXPos = e.getX();
                    while (newFlyingEnemyXPos % 40 != 0) {
                        newFlyingEnemyXPos--;
                    }
                    int newFlyingEnemyYPos = e.getY();
                    while (newFlyingEnemyYPos % 40 != 0) {
                        newFlyingEnemyYPos--;
                    }
                    flyingEnemyArray.add(new FlyingEnemy(newFlyingEnemyXPos, newFlyingEnemyYPos));
                    flyingEnemyArray.get(flyingEnemyArray.size() - 1).xVel = flyingEnemyArray.get(flyingEnemyArray.size() - 1).speed;
                    flyingEnemyArray.get(flyingEnemyArray.size() - 1).yVel = 9;
                    flyingEnemyExists = true;
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

            for(FlyingEnemy b : flyingEnemyArray){
                if(e.getX() >= b.x && e.getX() <= b.x + 40 && e.getY() >= b.y && e.getY() <= b.y + 40){
                    b.x = -1000;
                    b.y = -1000;
                }
            }

            for(Platform p : platformArray){
                if(e.getX() >= p.x && e.getX() <= p.x + 40 && e.getY() >= p.y && e.getY() <= p.y + 20){
                    p.x = -1000;
                }
            }
            if(playerExists) {
                if (e.getX() > +player.x && e.getX() <= player.x + 40 && e.getY() >= player.y && e.getY() <= player.y + 80) {
                    player.x = 10000;
                }
            }
            if(flagExists) {
                if (e.getX() > +flag.x && e.getX() <= flag.x + 40 && e.getY() >= flag.y && e.getY() <= flag.y + 80) {
                    flag.x = -10000;
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
                case '-':
                    int newPlatformXPos = e.getX();
                    while (newPlatformXPos % 40 != 0) {
                        newPlatformXPos--;
                    }
                    int newPlatformYPos = e.getY();
                    while (newPlatformYPos % 40 != 0) {
                        newPlatformYPos--;
                    }
                    platformArray.add(new Platform(newPlatformXPos, newPlatformYPos));
                    break;
                case 'B':
                    int newFlyingEnemyXPos = e.getX();
                    while (newFlyingEnemyXPos % 40 != 0) {
                        newFlyingEnemyXPos--;
                    }
                    int newFlyingEnemyYPos = e.getY();
                    while (newFlyingEnemyYPos % 40 != 0) {
                        newFlyingEnemyYPos--;
                    }
                    flyingEnemyArray.add(new FlyingEnemy(newFlyingEnemyXPos, newFlyingEnemyYPos));
                    flyingEnemyArray.get(flyingEnemyArray.size() - 1).xVel = flyingEnemyArray.get(flyingEnemyArray.size() - 1).speed;
                    flyingEnemyArray.get(flyingEnemyArray.size() - 1).yVel = 9;
                    flyingEnemyExists = true;
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

            for(FlyingEnemy b : flyingEnemyArray){
                if(e.getX() >= b.x && e.getX() <= b.x + 40 && e.getY() >= b.y && e.getY() <= b.y + 40){
                    b.x = -1000;
                    b.y = -1000;
                }
            }

            for(Platform p : platformArray){
                if(e.getX() >= p.x && e.getX() <= p.x + 40 && e.getY() >= p.y && e.getY() <= p.y + 20){
                    p.x = -1000;
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
                int plx = 0;
                int ply = 0;
                int bx = 0;
                int by = 0;

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
                        enemyArray.get(enemyArray.size() - 1).xVel = enemyArray.get(enemyArray.size() - 1).speed;
                        enemyArray.get(enemyArray.size() - 1).yVel = 9;
                        enemyExists = true;
                    }
                    if(data.substring(0,2).equals("-x")){
                        plx = Integer.parseInt(data.substring(2, data.length()));
                    }
                    if(data.substring(0,2).equals("-y")){
                        ply = Integer.parseInt(data.substring(2, data.length()));
                        platformArray.add(new Platform(plx, ply));
                    }
                    if(data.substring(0,2).equals("bx")){
                        bx = Integer.parseInt(data.substring(2, data.length()));
                    }
                    if(data.substring(0,2).equals("by")){
                        by = Integer.parseInt(data.substring(2, data.length()));
                        flyingEnemyArray.add(new FlyingEnemy(bx, by));
                        flyingEnemyArray.get(flyingEnemyArray.size() - 1).xVel = flyingEnemyArray.get(flyingEnemyArray.size() - 1).speed;
                        flyingEnemyArray.get(flyingEnemyArray.size() - 1).yVel = 9;
                        flyingEnemyExists = true;
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
                for (Platform p : platformArray){
                    writer.write("-x" + p.x + "\n");
                    writer.write("-y" + p.y + "\n");
                }
                for (FlyingEnemy b : flyingEnemyArray){
                    writer.write("bx" + b.x + "\n");
                    writer.write("by" + b.y + "\n");
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
