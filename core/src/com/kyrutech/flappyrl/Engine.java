package com.kyrutech.flappyrl;

import com.badlogic.gdx.math.Rectangle;

public class Engine {

    private int score = 0;
    private final double GRAVITY = 0.5;

    private Flappy flappy;

    private boolean flapped = false;

    private Rectangle topPipe, bottomPipe;

    private int width, height;

    private boolean scored;

    private GameState state;

    public enum GameState {
        START, RUNNING, GAMEOVER;
    }

    public Engine(int width, int height) {
        this.width = width;
        this.height = height;

        checkPipes();
        flappy = new Flappy(width/2, height/2, height);

        state = GameState.START;
    }

    public void act(double delta) {
        double pipeMovement = 200 * delta;
        topPipe.setX((float) (topPipe.getX()-pipeMovement));
        bottomPipe.setX((float) (bottomPipe.getX()-pipeMovement));

        checkPipes();
        checkForScore();
        if(flappy.isAlive()) {
            checkCollision();
        }

        if(flappy.getY() > 10) {
            flappy.act(GRAVITY);
        } else {
            flappy.setAlive(false);
            state = GameState.GAMEOVER;
        }

    }

    public void restart() {
        topPipe = null;
        bottomPipe = null;
        checkPipes();
        flappy = new Flappy(width/2, height/2, height);

        state = GameState.RUNNING;
        score = 0;
        scored = false;
    }

    public Rectangle getFlappyRect() {
        return new Rectangle(flappy.getX()-(FlappyBirdRL.FLAPPY_SIZE/2), flappy.getY()-(FlappyBirdRL.FLAPPY_SIZE/2), FlappyBirdRL.FLAPPY_SIZE, FlappyBirdRL.FLAPPY_SIZE);
    }

    private void checkCollision() {
        Rectangle flappyRect = getFlappyRect();

        if(flappyRect.overlaps(topPipe) || flappyRect.overlaps(bottomPipe)) {
            flappy.setAlive(false);
        }
    }

    public void flapFlappy() {
        if(!flapped) {
            flappy.flap();
            flapped = true;
        }
    }

    public void unFlapFlappy() {
        if(flapped) {
            flapped = false;
        }
    }

    private void checkForScore() {
        if(flappy.isAlive() && !scored) {
            if(topPipe.getX()+25 < width/2) {
                score++;
                scored = true;
            }
        }
    }


    private void checkPipes() {
        if(topPipe == null || topPipe.getX() < -25) {
            //removeObject(topPipe);
            //removeObject(bottomPipe);

            int centerY = (int) (Math.random()*200) + 200;
            int openingHeight = (int) (Math.random()*200) + 150;

            int bottomPHeight = centerY - (openingHeight/2);
            bottomPipe = new Rectangle(width-25, 0, 50, bottomPHeight);

            int topPHeight = height - (centerY + (openingHeight/2));
            topPipe = new Rectangle(width-25, height - topPHeight, 50, topPHeight);

            scored = false;
        }
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Flappy getFlappy() {
        return flappy;
    }

    public void setFlappy(Flappy flappy) {
        this.flappy = flappy;
    }

    public Rectangle getTopPipe() {
        return topPipe;
    }

    public void setTopPipe(Rectangle topPipe) {
        this.topPipe = topPipe;
    }

    public Rectangle getBottomPipe() {
        return bottomPipe;
    }

    public void setBottomPipe(Rectangle bottomPipe) {
        this.bottomPipe = bottomPipe;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }
}
