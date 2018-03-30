/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package capstone;

import toolbox.random.Random;
import java.util.List;
import java.util.ArrayList;


/**
 *
 * @author paul
 */
public class SimAgentRunner {
    
    private int numAgents;
    private int numCoefs;
    private int numGuesses;
    private double[] mus;
    private double[] sigmas;
    
    public static void main(String... args) {
	SimAgentRunner sim = new SimAgentRunner();
	sim.run();
    }
    
    public SimAgentRunner() {
	this.numCoefs = 3;
	this.mus = new double[] { 3.0, -14.0, 5.0 };
	this.sigmas = new double[] { 1.0, 1.5, 2.0 }; 
	
	this.numAgents = 10000;
	this.numGuesses = 1000;
    }
    
    public double[] generateGenome(int length) {
	return Random.getUniformDoubles(length, 0.0, 10.0);
    }
    
    protected List<SimAgent> instantiateAgents(int numAgents) {
	List<SimAgent> agents = new ArrayList<>();
	for(int i = 0; i < numAgents; i++) {
	    agents.add(new SimAgent(this.generateGenome(this.numCoefs), this.mus, this.sigmas, this.numGuesses));
	}
	return agents;
    }
    
    public void run() {
	List<SimAgent> agents = this.instantiateAgents(this.numAgents);
	List<Thread> threads = new ArrayList<>();
	agents.forEach(agent -> { 
	    Thread t = new Thread(agent);
	    threads.add(t);
	    t.start();
	});
	while(this.stillRunning(threads)) {
	    //continue to run
	}
	//agents.forEach(agent -> System.out.println(agent.getNumCorrectGuesses()));
	int bestIndex = 0;
	for(int i = 0; i < agents.size(); i++) {
	    /*int numCorrect = agents.get(i).getNumCorrectGuesses();
	    //System.out.println(numCorrect);
	    if(numCorrect > agents.get(bestIndex).getNumCorrectGuesses()) {
		bestIndex = i;
		//System.out.println("new best");
	    }*/
	    double successRate = agents.get(i).getBestGenomeSuccessRate();
	    if(successRate > agents.get(bestIndex).getBestGenomeSuccessRate()) {
		bestIndex = i;
	    }
	}
	double[] bestGenome = agents.get(bestIndex).getGenome();
	System.out.println(toolbox.util.ListArrayUtil.arrayToString(bestGenome));
	System.out.println(agents.get(bestIndex).getNumCorrectGuesses());
	System.out.println(bestGenome[0] * mus[0] + bestGenome[1] * mus[1] + bestGenome[2] * mus[2]);
    }
    
    public boolean stillRunning(List<Thread> threads) {
	for(Thread t : threads) {
	    if(t.isAlive()) {
		return true;
	    }
	}
	return false;
    }
}
