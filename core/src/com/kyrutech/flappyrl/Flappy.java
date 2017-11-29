package com.kyrutech.flappyrl;


public class Flappy {

    private int x, y;

    private int height;

    private double fallingSpeed = 1.0;
    private final double FLAP = 16.0;

    private boolean alive = true;

    public Flappy(int x, int y, int height) {
     this.x = x;
     this.y = y;
     this.height = height;
    }

    public void act(double gravity) {
        applyGravity(gravity);
        normalizeFallingSpeed();
        move();
        if(y > height) {
            y = height;
        }
    }

    private void applyGravity(double gravity) {
        fallingSpeed += gravity;
    }

    private void normalizeFallingSpeed() {
        if(fallingSpeed > 20) {
            fallingSpeed = 20;
        }
        if(fallingSpeed < -20) {
            fallingSpeed = -20;
        }
    }

    private void move() {
        y = (int) (y - fallingSpeed);
    }

    public void flap() {
        if(isAlive()) {
            fallingSpeed -= FLAP;
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getFallingSpeed() {
        return fallingSpeed;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        if(alive == false) {
            fallingSpeed = 0;
        }
        this.alive = alive;
    }
}
