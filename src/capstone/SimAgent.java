/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package capstone;

import toolbox.random.Random;

/**
 *
 * @author paul
 */
public class SimAgent implements Runnable {
    
    public static final double TARGET = -80;
    public static final double ERROR_RANGE = 5.0;
    
    private double[] genome;
    private double[] currentCoefs;
    private double[] mus;
    private double[] sigmas;
    
    private int numGuessesToMake;
    private int numGuessesDone;
    private int numCorrectGuesses;
    private double bestGenomeSuccessRate;
    private int runsPerBatch = 100;
    
    //TODO:  make it generte its own genome here
    public SimAgent(double[] genome, double[] mus, double[] sigmas, int numGuesses) {
	assert(genome.length == mus.length) : "lengths must be equals";
	assert(genome.length == sigmas.length) : "lengths must be equals";
	this.genome = genome;
	this.mus = mus;
	this.sigmas = sigmas;
	this.currentCoefs = this.generateCoefficients();
	this.numCorrectGuesses = 0;
	this.numGuessesToMake = numGuesses;
	this.numGuessesDone = 0;
	this.bestGenomeSuccessRate = 0;
    }
    
    public void run() {
	/*for(int count = 0; count < this.numGuessesToMake; count++) {
	    this.doOneRun();
	}*/
	
	int numBatches = this.numGuessesToMake / this.runsPerBatch;
	for(int i = 0; i < numBatches; i++) {
	    this.doOneBatch(this.runsPerBatch);
	}
    }
    
    public void doOneBatch(int numRuns) {
	double[] previousGenome = this.genome;
	//double currentSuccessRate = (double)this.getNumCorrectGuesses() / (double)this.numGuessesDone;
	
	//generate new genome
	double[] newGenome = this.generateGenome(this.genome.length);
	this.setGenome(newGenome);
	
	//run the specified number of times
	int successes = 0;
	for(int i = 0; i < numRuns; i++) {
	    //TODO:  pass a genome into makeGuess so it does not use this.genome, and then only change this.genome if the result is better
	    if(this.makeGuess()) {
		successes++;
		this.numCorrectGuesses++;
	    }
	}
	
	//if success rate > bestSuccessRate, use the genome
	double currentSuccessRate = (double)successes / (double)numRuns;
	if(currentSuccessRate > this.bestGenomeSuccessRate) {
	    this.bestGenomeSuccessRate = currentSuccessRate;
	} else {
	    //otherwise, revert to the old one
	    this.genome = previousGenome;
	}
    }
    
    public void doOneRun() {
	boolean goodGuess = this.makeGuess();
	if(goodGuess) {
	    this.numCorrectGuesses++;
	}
    }
    
    public boolean makeGuess() {
	double num = this.nextGuess(this.generateCoefficients());
	//System.out.println(num);
	this.numGuessesDone++;
	if((num > TARGET - ERROR_RANGE) && num < (TARGET + ERROR_RANGE)) {
	    //System.out.println("was correct");
	    return true;
	}
	else {
	    return false;
	}
    }
    
    public double[] generateCoefficients() {
	double[] coefs = new double[this.genome.length];
	for(int i = 0; i < coefs.length; i++) {
	    coefs[i] = Random.rnorm(1, mus[i], sigmas[i])[0];
	}
	return coefs;
    }
    
    //TODO: ability to do small mutations instead of just a whole new genome
    public double[] generateGenome(int length) {
	return Random.getUniformDoubles(length, 0.0, 10.0);
    }
    
    public double nextGuess(double[] coefs) {
	return coefs[0] * this.genome[0] + coefs[1] * this.genome[1] + coefs[2] * this.genome[2];
    }

    public double[] getGenome() {
	return genome;
    }

    public void setGenome(double[] genome) {
	this.genome = genome;
    }

    public double[] getCurrentCoefs() {
	return currentCoefs;
    }

    public void setCurrentCoefs(double[] currentCoefs) {
	this.currentCoefs = currentCoefs;
    }

    public double[] getMus() {
	return mus;
    }

    public void setMus(double[] mus) {
	this.mus = mus;
    }

    public double[] getSigmas() {
	return sigmas;
    }

    public void setSigmas(double[] sigmas) {
	this.sigmas = sigmas;
    }

    public int getNumGuessesToMake() {
	return numGuessesToMake;
    }

    public void setNumGuessesToMake(int numGuesses) {
	this.numGuessesToMake = numGuesses;
    }

    public int getNumCorrectGuesses() {
	return numCorrectGuesses;
    }

    private void setNumCorrectGuesses(int numCorrectGuesses) {
	this.numCorrectGuesses = numCorrectGuesses;
    }

    public int getNumGuessesDone() {
	return numGuessesDone;
    }

    private void setNumGuessesDone(int numGuessesDone) {
	this.numGuessesDone = numGuessesDone;
    }
    
    public double getBestGenomeSuccessRate() {
	return this.bestGenomeSuccessRate;
    }
    
}
