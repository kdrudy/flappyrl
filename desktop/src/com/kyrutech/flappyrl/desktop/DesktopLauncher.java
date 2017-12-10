package com.kyrutech.flappyrl.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.kyrutech.flappyrl.FlappyBirdRL;
import com.kyrutech.flappyrl.FlappyMDP;
import org.deeplearning4j.rl4j.learning.HistoryProcessor;
import org.deeplearning4j.rl4j.learning.sync.qlearning.QLearning;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteConv;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdConv;
import org.deeplearning4j.rl4j.policy.DQNPolicy;
import org.deeplearning4j.rl4j.util.DataManager;

import java.io.IOException;

public class DesktopLauncher {

	public static QLearning.QLConfiguration FLAPPY_QL =
			new QLearning.QLConfiguration(
					123,      //Random seed
					10000,    //Max step By epoch
					8000000,  //Max step
					1000000,  //Max size of experience replay
					32,       //size of batches
					10000,    //target update (hard)
					500,      //num step noop warmup
					0.1,      //reward scaling
					0.99,     //gamma
					100.0,    //td-error clipping
					0.1f,     //min epsilon
					100000,   //num step for eps greedy anneal
					true      //double-dqn
			);

	public static HistoryProcessor.Configuration FLAPPY_HP =
			new HistoryProcessor.Configuration(
					4,       //History length
					120,      //resize width
					90,     //resize height
					90,      //crop width
					90,      //crop height
					0,       //cropping x offset
					0,       //cropping y offset
					4        //skip mod (one frame is picked every x
			);

	public static DQNFactoryStdConv.Configuration FLAPPY_NET =
			new DQNFactoryStdConv.Configuration(
					0.001, //learning rate
					0.001,   //l2 regularization
					null, null
			);


	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 800;
		config.height = 600;

		FlappyBirdRL game = new FlappyBirdRL();

		new LwjglApplication(game, config);

		try {
			flappy(game);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	public static void flappy(FlappyBirdRL game) throws IOException {
		System.out.println("Starting training");
		//record the training data in rl4j-data in a new folder (save)
		DataManager manager = new DataManager(true);

		//define the mdp from gym (name, render)
		FlappyMDP mdp = null;
		try {
			mdp = new FlappyMDP(true, game);
		} catch (RuntimeException e){
			e.printStackTrace();
			//System.out.print("To run this example, download and start the gym-http-api repo found at https://github.com/openai/gym-http-api.");
		}
		//define the training
		QLearningDiscreteConv<FlappyMDP.Screen> dql = new QLearningDiscreteConv(mdp, FLAPPY_NET, FLAPPY_HP, FLAPPY_QL, manager);

		game.startGame();

		//train
		dql.train();

		//get the final policy
		DQNPolicy<FlappyMDP.Screen> pol = dql.getPolicy();

		//serialize and save (serialization showcase, but not required)
		pol.save("/tmp/pol1");

		//close the mdp (close http)
		mdp.close();
	}
}
