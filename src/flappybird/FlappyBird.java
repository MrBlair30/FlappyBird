package flappybird;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    int boardWidth = 360;
    int boardHeight = 640;

    //Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;
    Image fireBallImg;
    Image shieldImg;

    //Bird
    int birdX = boardWidth / 8;
    int birdY = boardHeight / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    //Pipe
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 62;
    int pipeHeight = 512;
    int randomYTopPipe;
    int randomBottomPipeY;

    //FireBall
    int fireBallY = 0;

    //Shield
    int shieldWidth = birdWidth + 20;
    int shieldHeight = birdHeight + 20;
    double shieldX = 0;
    double shieldY = 0;
    int incrementShieldX = 310;
    int incrementShieldY = -55;

    //Bird class
    class Bird {

        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    //Pipe class
    class Pipe {

        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    //GAME LOGIC
    File file;
    PrintWriter pw;
    BufferedReader br;

    Bird bird;
    double velocityY = 0;
    double velocityX = 0;
    double shieldVelocityX = -1;
    double gravity = 0;

    ArrayList<Pipe> pipes;

    Timer gameLoop;
    Timer placePipesTimer;
    Timer blinkTimer;

    boolean gameOver = false;
    boolean showStartMessage = true;
    boolean blinkMessage = true;
    double score = 0;
    double highScore = 0;
    boolean activateObstacle = false;
    boolean generateShield = false;
    boolean hasShield = false;

    FlappyBird() throws IOException {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        //Load images                
        backgroundImg = new ImageIcon(getClass().getResource("flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("bottompipe.png")).getImage();
        fireBallImg = new ImageIcon(getClass().getResource("fireball.png")).getImage();
        shieldImg = new ImageIcon(getClass().getResource("protection_shield.png")).getImage();

        //bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();
        createFile();

        //Blink message timer
        blinkTimer = new Timer(380, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                blinkMessage = !blinkMessage;
                repaint();
            }
        });
        blinkTimer.start();

        //Place pipes timer
        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipesTimer.start();

        //Game timer
        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        //background
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

        //Start game
        if (showStartMessage && blinkMessage) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Press ENTER to start the game...", 20, boardHeight / 2);
        }

        //bird
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        //Generate shield
        if (generateShield) {
            g.drawImage(shieldImg, (int) shieldX, (int) shieldY, shieldWidth, shieldHeight, null);
        }

        //Shield Active
        if (hasShield) {
            g.drawImage(shieldImg, bird.x - 10, bird.y - 10, shieldWidth, shieldHeight, null);
        }

        //pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipeDraw = pipes.get(i);
            g.drawImage(pipeDraw.img, pipeDraw.x, pipeDraw.y, pipeDraw.width, pipeDraw.height, null);
        }

        //Random Obstabcle
        if (activateObstacle) {
            g.drawImage(fireBallImg, bird.x - 12, fireBallY, 68, 46, null);
        }

        //Score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 45));
            g.drawString("Game Over: " + (int) score, 17, boardHeight / 2);
            g.setColor(Color.DARK_GRAY);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("Press SPACE to restart...", 80, 600);
        } else {
            g.drawString("Score: " + (int) score, 20, 40);
        }

        //MAX Record
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Record: " + (int) highScore, 225, 40);

    }

    public void placePipes() {
        randomYTopPipe = (int) -Math.floor(Math.random() * 270) + 1;
        int space = 150;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomYTopPipe;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        randomBottomPipeY = randomYTopPipe + topPipe.height + space;
        bottomPipe.y = randomBottomPipeY;
        pipes.add(bottomPipe);
    }

    public void move() {
        try {
            readFile();
        } catch (IOException ex) {
            Logger.getLogger(FlappyBird.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        //Pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipeDraw = pipes.get(i);
            pipeDraw.x += velocityX;
            int randomNumber = 0;
            if (!pipeDraw.passed && bird.x > pipeDraw.x + pipeDraw.width) {
                pipeDraw.passed = true;
                score += 0.5;
                randomNumber = (int) Math.floor(Math.random() * 50) + 1;
                System.out.println(randomNumber);
                if (randomNumber == 1) {
                    //gameOver = true;
                    if (!activateObstacle) {
                        activateObstacle = true;
                    }
                }

                //Shield generation
                if (randomNumber == 1) {
                    if (!generateShield) {
                        shieldX = pipeDraw.x + incrementShieldX;
                        shieldY = (randomYTopPipe + pipeDraw.height) + incrementShieldY;
                        generateShield = true;
                    }
                }

                try {
                    writeFile();
                } catch (IOException ex) {
                    System.out.println("Error: " + ex.getMessage());
                }
            }
            if (pipeDraw.x + pipeDraw.width < 0) {
                pipes.remove(i);
                i--;
            }

            if (collision(bird, pipeDraw)) {
                gameOver = true;
            }

            //Shield
            if (bird.x < shieldX + shieldWidth
                    && bird.x + bird.width > shieldX
                    && bird.y < shieldY + shieldHeight
                    && bird.y + bird.height > shieldY) {

                //System.out.println("Shield X: " + shieldX + " Shield Y: " + shieldY);
                //System.out.println("Bird X: " + bird.x + " Bird Y: " + bird.y);
                if (generateShield) {
                    hasShield = true;
                }
                generateShield = false;
            }

            if (generateShield) {
                shieldX += shieldVelocityX;
                //System.out.println("*********************************** SHIELD X VELOCITY: " + shieldVelocityX + " *****************************************");
                if (shieldX > boardWidth) {
                    generateShield = false;                 }
                }

        }//end for

        if (bird.y > boardHeight) {
            gameOver = true;
        }

        //Fireball
        if (!activateObstacle) {

        } else if (fireBallY != (bird.y - 32)) {
            fireBallY += 2;
            //System.out.println("Fire ball position: "+fireBallY+" Bird position: "+(bird.y-32));
            if (fireBallY > bird.y - 32) {
                if (!hasShield) {
                    activateObstacle = false;
                    //gameOver = true;
                    fireBallY = 0;
                    repaint();
                } else {
                    activateObstacle = false;
                    hasShield = false;
                    fireBallY = 0;
                }
            }
        }

        //Reverse generation
        if (score == 100) {
            pipeX = 0;
            bird.x = 290;
            velocityX = 4;
            shieldVelocityX = 0.5;
            incrementShieldX = 40;
            pipes.clear();
            activateObstacle = false;
            generateShield = false;
        }

    }

    public boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width
                && a.x + a.width > b.x
                && a.y < b.y + b.height
                && a.y + a.height > b.y;
    }

    //FIles
    public void createFile() throws IOException {
        file = new File("C:\\Users\\usuario\\Desktop\\Flappy Bird\\FlappyBirdScore.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    public void writeFile() throws IOException {
        pw = new PrintWriter(new FileWriter(file, true));
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = date.format(formatter);
        if (score > highScore) {
            pw.println(score + " Points on: " + formattedDate);
        }
        pw.close();
    }

    public void readFile() throws IOException {
        br = new BufferedReader(new FileReader(file));
        String recordDate = "";
        String line;
        while ((line = br.readLine()) != null) {
            String[] split = line.split(" ");
            double currentScore = Double.parseDouble(split[0]);
            if (currentScore > highScore) {
                highScore = currentScore;
                recordDate = split[3];
            }
        }

    }

    //Events
    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();

        if (gameOver) {
            placePipesTimer.stop();
            gameLoop.stop();

        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (showStartMessage) {
                velocityY = 0;
            } else {
                velocityY = -9;
            }
            if (gameOver) {
                pipeX = boardWidth;
                bird.x = birdX;
                bird.y = birdY;
                shieldX = 0;
                shieldY = 0;
                incrementShieldX = 310;
                incrementShieldY = -55;
                shieldVelocityX = -1;
                fireBallY = 0;
                score = 0;
                velocityY = 0;
                velocityX = 0;
                gravity = 0;
                pipes.clear();
                gameOver = false;
                showStartMessage = true;
                activateObstacle = false;
                generateShield = false;
                hasShield = false;
                blinkTimer.start();
                gameLoop.start();
                placePipesTimer.start();
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            pipes.clear();
            gravity = 1;
            velocityX = -4;
            velocityY = -9;

            showStartMessage = false;
            blinkTimer.stop();

        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

}
