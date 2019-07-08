package com.kyrutech.flappyrl;

import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.space.ArrayObservationSpace;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.Encodable;
import org.deeplearning4j.rl4j.space.ObservationSpace;

import java.nio.ByteBuffer;

import static javax.swing.UIManager.get;

public class FlappyMDP implements MDP<FlappyMDP.Screen, Integer, DiscreteSpace> {

    private FlappyBirdRL game;
    final protected DiscreteSpace discreteSpace;
    final protected ObservationSpace<Screen> observationSpace;

    final protected boolean render;

    protected double scaleFactor = 1;


    public FlappyMDP() {
        this(false, new FlappyBirdRL());
    }

    public FlappyMDP(boolean render, FlappyBirdRL game) {
        this.render = render;
        this.game = game;
        discreteSpace = new DiscreteSpace(2);
        observationSpace = new ArrayObservationSpace<>(new int[] {160, 160, 3});

    }

    @Override
    public ObservationSpace<Screen> getObservationSpace() {
        return observationSpace;
    }

    @Override
    public DiscreteSpace getActionSpace() {
        return discreteSpace;
    }

    @Override
    public FlappyMDP.Screen reset() {
        game.handleInput(FlappyBirdRL.Inputs.RESTART);
        byte[] buffer = new byte[game.getPixels().remaining()];
        game.getPixels().get(buffer);
        return new Screen(new byte[160*160*3]);
    }

    @Override
    public void close() {
        System.out.println("Finished Training");
    }

    @Override
    public StepReply<FlappyMDP.Screen> step(Integer action) {
        switch(action) {
            case 0:
                game.handleInput(FlappyBirdRL.Inputs.NOTHING);
                break;
            case 1:
                game.handleInput(FlappyBirdRL.Inputs.FLAP);
                break;
        }
        double r = game.getReward() * scaleFactor;

        ByteBuffer byteBuffer = (ByteBuffer) game.getPixels().position(0);

        byte[] buffer = new byte[byteBuffer.remaining()];
        byteBuffer.get(buffer);

//        Screen screen = new Screen(buffer);
        Screen screen = new Screen(buffer.length == 0 ? new byte[160*160*3] : buffer);
        return new StepReply<>(screen, r, game.isDone(), null);
    }

    @Override
    public boolean isDone() {
        return game.isDone();
    }

    @Override
    public MDP<Screen, Integer, DiscreteSpace> newInstance() {
        return new FlappyMDP(true, game);
    }

    public static class Screen implements Encodable {

        double[] array;

        public Screen(byte[] screen) {
            array = new double[screen.length];
            for (int i = 0; i < screen.length; i++) {
                array[i] = (screen[i] & 0xFF);// / 255.0;
            }
        }


        @Override
        public double[] toArray() {
            return array;
        }
    }
}
