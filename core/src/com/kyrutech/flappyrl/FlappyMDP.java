package com.kyrutech.flappyrl;

import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.space.ArrayObservationSpace;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.Encodable;
import org.deeplearning4j.rl4j.space.ObservationSpace;

public class FlappyMDP implements MDP<FlappyMDP.Screen, Integer, DiscreteSpace> {

    private FlappyBirdRL game;
    final protected DiscreteSpace discreteSpace;
    final protected ObservationSpace<Screen> observationSpace;

    final protected boolean render;

    protected double scaleFactor = 100;

    public FlappyMDP() {
        this(false, new FlappyBirdRL());
    }

    public FlappyMDP(boolean render, FlappyBirdRL game) {
        this.render = render;
        this.game = game;
        discreteSpace = new DiscreteSpace(2);
        observationSpace = new ArrayObservationSpace<>(new int[] {160, 120, 3});

    }

    @Override
    public ObservationSpace<FlappyMDP.Screen> getObservationSpace() {
        return observationSpace;
    }

    @Override
    public DiscreteSpace getActionSpace() {
        return discreteSpace;
    }

    @Override
    public FlappyMDP.Screen reset() {
        game.handleInput(FlappyBirdRL.Inputs.RESTART);
        return new Screen(game.getPixels());
    }

    @Override
    public void close() {
        System.out.println("Finished Training");
    }

    @Override
    public StepReply<FlappyMDP.Screen> step(Integer action) {
        double r = game.getReward() * scaleFactor;
        switch(action) {
            case 0:
                game.handleInput(FlappyBirdRL.Inputs.NOTHING);
                break;
            case 1:
                game.handleInput(FlappyBirdRL.Inputs.FLAP);
                break;
        }
        Screen screen = new Screen(game.isDone() ? new byte[160*120*3] : game.getPixels());
        return new StepReply<>(screen, r, game.isDone(), null);
    }

    @Override
    public boolean isDone() {
        return game.isDone();
    }

    @Override
    public MDP<FlappyMDP.Screen, Integer, DiscreteSpace> newInstance() {
        return new FlappyMDP(true, game);
    }

    public static class Screen implements Encodable {

        double[] array;

        public Screen(byte[] screen) {
            array = new double[screen.length];
            for (int i = 0; i < screen.length; i++) {
                array[i] = (screen[i] & 0xFF) / 255.0;
            }
        }

        @Override
        public double[] toArray() {
            return array;
        }
    }
}
