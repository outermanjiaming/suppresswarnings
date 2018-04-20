/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.osgi.neuralnetwork;

public class Train {

	public static void train(String serializeTo, Data data, AI network, int echoStep, int shuffleStep, int saveStep, int max) {
		int step = 0;
		Clock clock = new Clock();
		clock.start("All");
		while(step ++ < max) {
			double err = 0;
			clock.start("Train");
			for(int i =0;i<data.size();i++) {
				Row r = data.get(i);
				err += network.train(r.feature, r.target);
			}
			clock.end("Train");
			if(step % echoStep == 0) System.out.println(String.format("%s\t%s/%s Err: %s", serializeTo, step, max, err));
			if(step % shuffleStep == 0) data.shuffle();
			if(step % saveStep == 0) Util.serialize(network, serializeTo); 
		}
		clock.end("All");
		clock.listAll();
	}
}
