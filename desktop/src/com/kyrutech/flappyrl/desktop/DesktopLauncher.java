package com.kyrutech.flappyrl.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.kyrutech.flappyrl.FlappyBirdRL;
import com.kyrutech.flappyrl.FlappyMDP;
import org.deeplearning4j.rl4j.learning.HistoryProcessor;
import org.deeplearning4j.rl4j.learning.async.a3c.discrete.A3CDiscrete;
import org.deeplearning4j.rl4j.learning.async.a3c.discrete.A3CDiscreteConv;
import org.deeplearning4j.rl4j.learning.sync.qlearning.QLearning;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteConv;
import org.deeplearning4j.rl4j.network.ac.ActorCriticFactoryCompGraphStdConv;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdConv;
import org.deeplearning4j.rl4j.policy.DQNPolicy;
import org.deeplearning4j.rl4j.util.DataManager;
import org.nd4j.linalg.learning.config.Adam;

import java.io.IOException;

public class DesktopLauncher {

	public static QLearning.QLConfiguration FLAPPY_QL = new QLearning.QLConfiguration(123, //Random seed
			500, //Max step By epoch
			50000, //Max step
			50000, //Max size of experience replay
			32, //size of batches
			500, //target update (hard)
			10, //num step noop warmup
			0.01, //reward scaling
			0.99, //gamma
			1.0, //td-error clipping
			0.1f, //min epsilon
			10000, //num step for eps greedy anneal
			true //double DQN
	);

	public static DQNFactoryStdConv.Configuration FLAPPY_NET = new DQNFactoryStdConv.Configuration(
			0.01, //learning rate
			0.00, //l2 regularization
			null, // updater
			null // Listeners
	);

	public static A3CDiscrete.A3CConfiguration FLAPPY_AC3 =
			new A3CDiscrete.A3CConfiguration(
					123,            	//Random seed
					10000,   	//Max step By epoch
					8000000,        //Max step
					8,         	//Number of threads
					32,             	//t_max
					5000,         	//num step noop warmup
					0.1,       	//reward scaling
					0.99,           	//gamma
					10.0        	//td-error clipping
			);

	public static final ActorCriticFactoryCompGraphStdConv.Configuration FLAPPY_NET_AC3 =
			new ActorCriticFactoryCompGraphStdConv.Configuration(
					0.000,   //l2 regularization
					new Adam(0.00025), //learning rate
					null, false
			);


	public static HistoryProcessor.Configuration FLAPPY_HP =
			new HistoryProcessor.Configuration(
					4,       //History length
					160,      //resize width
					160,     //resize height
					160,      //crop width
					160,      //crop height
					0,       //cropping x offset
					0,       //cropping y offset
					1        //skip mod (one frame is picked every x
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


//		A3CDiscreteConv<FlappyMDP.Screen> a3c = new A3CDiscreteConv<FlappyMDP.Screen>(mdp, FLAPPY_NET_AC3, FLAPPY_HP, FLAPPY_AC3, manager);

		game.startGame();

		//train
		dql.train();

		//get the final policy
		//serialize and save (serialization showcase, but not required)
		dql.getPolicy().save("flappy-dql.model");

//		DQNPolicy dqnPolicy = DQNPolicy.load("flappy-dql.model");

		//close the mdp (close http)
		mdp.close();
	}
}
