/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package capstone;

/**
 *
 * @author paul
 */
public class Constants {
    
    public static final boolean DEFAULT_SHOULD_REMOVE_STOP_WORDS = true;
    public static final boolean DEFAULT_BINARY_ASSOCIATIONS_ONLY = false;
    public static final boolean TOKENIZE_ON_SENTENCE_BREAKS = false;
    
    public static final double DEFAULT_BETA = 2.0;
    public static final int DEFAULT_NGRAM_LENGTH = 2;
    public static final int DEFAULT_NUM_RUNS_PER_BATCH = 100;
    public static final int DEFAULT_NUM_BATCHES = 40;
    public static final String SPACE = " ";
    
    /*
    this.genome = this.generateRandomGenome();
	this.beta = Constants.DEFAULT_BETA;
	this.ngramLength = 2;
	this.numRunsPerBatch = 100;
	this.numBatches = 40;
	this.name = UNKNOWN;
    */
}
