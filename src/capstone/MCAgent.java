/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package capstone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import static java.util.stream.Collectors.toList;
import org.apache.logging.log4j.*;
import toolbox.random.Random;
import toolbox.stats.*;
import toolbox.trees.DuplicateEntryOption;
import toolbox.trees.WeightedBinaryTree;
import toolbox.util.ListArrayUtil;

/*parameters to be tuned:
-weight of prediction methods (ngrams, word associations, random selection, other algorithms) relative to each other
-should predictions like word association prediction use the most likely or randomly select?
-beta
-how often genome changes

*/


/**
 *
 * @author paul
 */
public class MCAgent implements Runnable {
    
    //TODO: inject a genome to code for parameters
    private double beta;    //deals with prior probability of one word given another; parameter specifies how many times we are pretending we found the same word histogram before but with no words together in a sentence
    //ex. beta = 2 => There are 1000 sentences. Word A and word B occur once each, in the same sentence.  P(A|B) really 1?  If beta = 2, we pretend we have 2000 prior sentences and have found A and B twice each, but not in the same sentence.
    //=> P(A|B) = 1/3.  If we then find A and B together in the same sentence 10 times, and always together, P(A|B) = 10/12.
    
    private TreeHistogram<String> totalAllWordHist;
    private TreeHistogram<String> totalNonStopWordHist;
    private TreeHistogram<String> oncePerSentenceWordHist;
    private List<String> sentences;
    private TreeHistogram<String> ngrams;
    private WordMatrix weightedMatrix;
    private WordMatrix binaryMatrix;
    
    private ProbDist<String> wordProbDist;
    private ProbDist<String> ngramsProbDist;
    
    private int ngramLength;
    private int numRunsPerBatch;
    private int numBatches;
    
    private double[] genome;
    private final int NGRAM_WEIGHT_POSITION = 0;
    private final int WORD_ASSOCIATION_WEIGHT_POSITION = 1;
    private final int RANDOM_WEIGHT_POSITION = 2;
    private final int NGRAM_LENGTH_POSITION = 3;
    
    private int genomeLength = 8;
    
    private String name;
    private double latestRatioCorrect;
    
    private static final String UNKNOWN = "UNKNOWN";
    
    private Logger logger;   
    
    public MCAgent(List<String> sentences) {
	this.sentences = sentences;
	//sentences is actually the only true information.  The rest can be calculated from it and are only for speed.
	//The other objects should be passed in because the MCAgentRunner can calculate it all one for all of the agents.
	//If this constructor is used, the calling code will either need to use the setters or this class will need to call the appropriate methods in Capstone.
	
	this.setDefaultParameters();
    }
    
    //TODO: remove - use either the default (with just senteces) or the full
    /**
     * @deprecated 
     * @param wordHist
     * @param sentences
     * @param ngrams
     * @param matrix 
     */
    public MCAgent(TreeHistogram<String> wordHist, List<String> sentences, TreeHistogram<String> ngrams, WordMatrix matrix) {
	this(sentences, wordHist, null, null, ngrams, matrix, null);
    }
    
    public MCAgent(List<String> sentences, TreeHistogram<String> totalAllWordHist, TreeHistogram<String> totalNonStopWordHist, TreeHistogram<String> oncePerSentenceWordHist, 
	TreeHistogram<String> ngrams, WordMatrix weightedMatrix, WordMatrix binaryMatrix) {
	this(sentences);
	this.sentences = sentences;
	this.totalAllWordHist = totalAllWordHist;
	this.totalNonStopWordHist = totalNonStopWordHist;
	this.oncePerSentenceWordHist = oncePerSentenceWordHist;
	this.ngrams = ngrams;
	this.weightedMatrix = weightedMatrix;
	this.binaryMatrix = binaryMatrix;
    }

    public void setDefaultParameters() {
	this.genome = this.generateRandomGenome();
	this.beta = Constants.DEFAULT_BETA;
	this.ngramLength = 2;
	this.numRunsPerBatch = 100;
	this.numBatches = 40;
	this.name = UNKNOWN;
	this.logger = LogManager.getLogger(this.getClass());
    }
    
    public double getBeta() {
	return beta;
    }

    public MCAgent setBeta(double beta) {
	this.beta = beta;
	return this;
    }

    public TreeHistogram<String> getTotalAllWordHist() {
	return totalAllWordHist;
    }

    public MCAgent setTotalAllWordHist(TreeHistogram<String> wordHist) {
	this.totalAllWordHist = wordHist;
	return this;
    }

    public TreeHistogram<String> getTotalNonStopWordHist() {
	return totalNonStopWordHist;
    }

    public MCAgent setTotalNonStopWordHist(TreeHistogram<String> totalNonStopWordHist) {
	this.totalNonStopWordHist = totalNonStopWordHist;
	return this;
    }

    public TreeHistogram<String> getOncePerSentenceWordHist() {
	return oncePerSentenceWordHist;
    }

    public MCAgent setOncePerSentenceWordHist(TreeHistogram<String> oncePerSentenceWordHist) {
	this.oncePerSentenceWordHist = oncePerSentenceWordHist;
	return this;
    }

    public List<String> getSentences() {
	return sentences;
    }

    public MCAgent setSentences(List<String> sentences) {
	this.sentences = sentences;
	return this;
    }

    public TreeHistogram<String> getNgrams() {
	return ngrams;
    }

    public MCAgent setNgrams(TreeHistogram<String> ngrams) {
	this.ngrams = ngrams;
	return this;
    }

    public WordMatrix getWeightedMatrix() {
	return weightedMatrix;
    }

    public MCAgent setWeightedMatrix(WordMatrix matrix) {
	this.weightedMatrix = matrix;
	return this;
    }

    public WordMatrix getBinaryMatrix() {
	return binaryMatrix;
    }

    public MCAgent setBinaryMatrix(WordMatrix binaryMatrix) {
	this.binaryMatrix = binaryMatrix;
	return this;
    }

    public ProbDist<String> getWordProbDist() {
	return wordProbDist;
    }

    public MCAgent setWordProbDist(ProbDist<String> wordProbDist) {
	this.wordProbDist = wordProbDist;
	return this;
    }

    public ProbDist<String> getNgramsProbDist() {
	return ngramsProbDist;
    }

    public MCAgent setNgramsProbDist(ProbDist<String> ngramsProbDist) {
	this.ngramsProbDist = ngramsProbDist;
	return this;
    }

    public double[] getGenome() {
	return genome;
    }

    public MCAgent setGenome(double[] genome) {
	this.genome = genome;
	return this;
    }

    public int getGenomeLength() {
	return genomeLength;
    }

    public MCAgent setGenomeLength(int genomeLength) {
	this.genomeLength = genomeLength;
	return this;
    }
    
    public int getNgramLength() {
	return ngramLength;
    }

    public MCAgent setNgramLength(int ngramLength) {
	this.ngramLength = ngramLength;
	return this;
    }
    
    public int getNumRunsPerBatch() {
	return this.numRunsPerBatch;
    }
    
    public MCAgent setNumRunsPerBatch(int numRuns) {
	this.numRunsPerBatch = numRuns;
	return this;
    }
    
    public int getNumBatches() {
	return this.numBatches;
    }
    
    public MCAgent setNumBatches(int numBatches) {
	this.numBatches = numBatches;
	return this;
    }
    
    protected double getNgramWeight() {
	return this.genome[NGRAM_WEIGHT_POSITION];
    }
    
    protected MCAgent setWordNGramWeight(double weight) {
	this.genome[NGRAM_WEIGHT_POSITION] = weight;
	return this;
    }
    
    protected double getWordAssocationWeight() {
	return this.genome[WORD_ASSOCIATION_WEIGHT_POSITION];
    }
    
    protected MCAgent setWordAssoctionWeight(double weight) {
	this.genome[WORD_ASSOCIATION_WEIGHT_POSITION] = weight;
	return this;
    }
    
    protected double getRandomWeight() {
	return this.genome[RANDOM_WEIGHT_POSITION];
    }
    
    protected MCAgent setRandomWeight(double weight) {
	this.genome[RANDOM_WEIGHT_POSITION] = weight;
	return this;
    }
    
    public String getName() {
	return this.name;
    }
    
    public MCAgent setName(String name) {
	this.name = name;
	return this;
    }

    public double getLatestRatioCorrect() {
	return this.latestRatioCorrect;
    }

    public Logger getLogger() {
	return logger;
    }

    public MCAgent setLogger(Logger logger) {
	this.logger = logger;
	return this;
    }
    
    
    public static void main(String[] args) {
    
    }
    
    public void run() {
	int currentNumCorrect = 0;
	int bestNumCorrect = 0;
	double[] bestGenome = this.genome.clone();
	
	for(int i = 0; i < numBatches; i++) {
	    logger.debug("genome is " + ListArrayUtil.arrayToString(this.genome));
	    currentNumCorrect = this.doOneBatch();
	    logger.debug("currentNumCorrect = " + currentNumCorrect);
	    if(currentNumCorrect > bestNumCorrect) {
		logger.info(ListArrayUtil.arrayToString(this.genome) + " was better than " + ListArrayUtil.arrayToString(bestGenome));
		bestNumCorrect = currentNumCorrect;
		bestGenome = this.genome.clone();
	    } else {
		this.genome = bestGenome.clone();
	    }
	    System.out.flush();
	    this.mutateGenome();
	}
	this.genome = bestGenome;
	this.latestRatioCorrect = (double)bestNumCorrect / (double)this.numRunsPerBatch;
	StringBuilder report = new StringBuilder();
	report.append("\n").append(this.name).append(":  final genome is ").append(ListArrayUtil.arrayToString(this.genome))
	    .append(" with ").append(this.latestRatioCorrect * 100.0).append("% correct");
	//logger.info(this.name + ":  final genome is " + ListArrayUtil.arrayToString(this.genome) + " with " + this.latestRatioCorrect * 100.0 + "% correct");
	logger.info("\n");
	logger.info(report);
	
	//System.out.println("now try a few predictions");
	//this.doPredictions(100);
    }
    
    /**
     * To actually do a specified number of random predictions.  Generally you would call this after training.
     * @param howMany 
     */
    public void doPredictions(int howMany) {
	
	int numCorrect = 0;
	for(int i = 0; i < howMany; i++) {
	    String sentence = sentences.get(Random.uniformInts(1, 0, sentences.size() - 1)[0]);
	    logger.debug("\n" + sentence);
	    if(sentence == null) {
		logger.error("randomly chosen sentence was null; returning false");
	    }
	    List<String> words = Capstone.tokenize(sentence, new Request("").setRemoveStopWords(false));
	    String lastWord = words.get(words.size() - 1);
	    if(lastWord == null) {
		System.err.println("lastWord was null:  " + sentence + "; return ing false");
	    }
	    System.out.println("last word = " + lastWord);
	    words.remove(words.size() - 1);
	    
	    //make a prediction on what the last word is
	    String prediction = this.makeOnePrediction(words);
	    System.out.println("prediction = " + prediction);
	    System.out.println(lastWord.equals(prediction));
	    if(lastWord.equals(prediction)) {
		numCorrect++;
	    }
	}
	System.out.println("\nnumber correct was " + numCorrect);
    }
    
    public int doOneBatch() {
	//make a series of runs making a prediction each time
	//count the number correct predictions
	int numCorrect = 0;
	for(int i = 0; i < numRunsPerBatch; i++) {
	    if(this.doOneRun()) {
		//System.out.println("was correct");
		numCorrect++;
	    }
	}
	return numCorrect;
    }
    
    //TODO:  possible return some result object that contains the true/false value and an error message
    public boolean doOneRun() {
	
	//Find a random sentence
	String sentence = sentences.get(Random.uniformInts(1, 0, sentences.size() - 1)[0]);
	if(sentence == null) {
	    System.err.println("randomly chosen sentence was null; returning false");
	    return false;
	}
	List<String> words = Capstone.tokenize(sentence, new Request("").setRemoveStopWords(false));
	String lastWord = words.get(words.size() - 1);
	if(lastWord == null) {
	    System.err.println("lastWord was null:  " + sentence + "; return ing false");
	    return false;
	}
	
	//make a prediction on what the last word is
	words.remove(words.size() - 1);	    //The input to makeOnePrediction no longer includes the last word, which is to be predicted.
	//TODO:  length check
	String prediction = this.makeOnePrediction(words);
	
	//return if it was correct or not
	if(prediction == null) {
	    System.err.println("prediction was null, returning false");
	    return false;
	}
	return prediction.equals(lastWord);
    }
    
    /**
     * 
     * @param sentence, not including the last word
     * @return 
     */
    public String makeOnePrediction(String sentence) {
	
	//Token the sentence.  Should we remove the stop words here?  Should that be part of the genome?
	//I think the stop words stay because they are used in the ngrams.  But they are not used in the associations.
	//Maybe tokenize twice - once for the ngrams and another for the associations.
	
	List<String> words = Capstone.tokenize(sentence, new Request("").setRemoveStopWords(false));
	//TODO:  length check
	words.remove(words.size() - 1);
	return this.makeOnePrediction(words);
    }
    
    /**
     * 
     * @param sentence
     * @return 
     */
    public String makeOnePrediction(List<String> sentence) {
	//System.out.println("makeOnePrediction(" + sentence + ")");
	//From the sentence in it, try to predict the last word.
	//=>
	//ngram prediction
	//Get the ngram at the lastIndexToUse minus the last word (2, 3, 4 etc. specified by genome)
	//based on that, find the most likely next work.
	
	//Check for null here where they are about to be used because they might not have been initiated in the construstor.
	if(this.ngramsProbDist == null) {
	    this.ngramsProbDist = this.ngrams.computeProbDist();
	}
	if(this.wordProbDist == null) {
	    this.wordProbDist = this.totalAllWordHist.computeProbDist();
	}
	try {
	    ProbDist<String> associations = this.getWordAssociationScores(sentence);
	    List<ProbDist<String>> dists = new ArrayList<>();
	    dists.add(this.getNgramPredictionProbDist(sentence));
	    dists.add(associations);
	    /**/ //**/dists.add(this.wordProbDist);
	    ProbDist<String> totalProbDist = this.combineProbDists(dists);
	    //System.out.println(totalProbDist);

	    return totalProbDist.getValue(0);   //TODO:  some sort of error checking
	} catch(Exception e) {
	    System.err.println(e.getClass() + " for sentence " + sentence + ":  " + e.getMessage());
	    return MCAgent.UNKNOWN;
	}
    }
    
    public ProbDist<String> getNgramPredictionProbDist(List<String> sentence) {
	//look at the histogram of ngrams to see which ones firstIndexToUse with the given ngram
	//find the most common one and return it
	//TODO:  an element of randomness?  maybe choose a random one, with the probability being equal to its proportion in the histogram?
	//maybe specify the randomness in the genome?
	
	String ngram = this.constructNgram(sentence, 2);
	//System.out.println(ngram);
	Predicate<String> p = word -> word.startsWith(ngram);
	if(this.ngramsProbDist == null) {
	    this.ngramsProbDist = this.ngrams.computeProbDist();
	}
	ProbDist<String> probsGiven = this.ngramsProbDist.given(p);
	if(probsGiven == null) {
	    System.out.println("probsGiven was null");
	}
	//System.out.println("matching ngrams are:");
	//probsGiven.getValues().stream().forEach(System.out::println);
	//Extract just the last word of the ngram, so we return the predicted words and not the entire ngrams.
	probsGiven.setValues(probsGiven.getValues().stream().parallel().map(word -> this.mapWord(ngram, word)).collect(toList()));  //parallel
	return probsGiven;
    }
    
    private String mapWord(String ngram, String word) {
	if(word != null) {
	    word = word.replaceAll(ngram, "").trim();
	} else {
	    word = "UNKNOWN";
	}
	return word; 
    }
    
    public ProbDist<String> getNGramProbDist() {
	/*String ngram = this.constructNgram(sentence, 2);
	System.out.println(ngram);
	Predicate<String> p = word -> word.startsWith(ngram);
	List<String> matchingNGrams = this.ngrams.queryFromFirst(p);	    //TODO:  find out why this gives an error (problem in TreeHistogram and/or WeightedBinaryTree)
	
	return null;*/
	return this.ngramsProbDist;
    }
    
    public String doWordAssociationPrediction(List<String> sentence) {
	ProbDist<String> scores = this.getWordAssociationScores(sentence);
	//TODO:  possibly return a random value; whether or not we return the most likely value
	//or a random value could be specified in the genome
	return scores.getValue(0);/**/
    }
    
    public ProbDist<String> getWordAssociationScores(List<String> sentence) {
	sentence = sentence.stream().distinct().collect(toList());  //distinct => can't do parallel
	//System.out.println(sentence);
	//for each word in the sentence
	//For each association, use probAInSentenceWithB to find the prob of each word the given word is in a sentence with.
	//This gives a probability distribution.
	//After all dists have been determined, try Naive Bayes (yes, it's very naive, but may work OK)
	Map<String, ProbDist> dists = new HashMap<>();
	Map<String, Double> scores = new HashMap<>();
	WeightedBinaryTree<String> scoresTree = new WeightedBinaryTree<>(UNKNOWN, 1);	//TODO: stop this as soon as the defect with the WBT constructor is fixed
	for(String word : sentence) {
	    //System.out.println("\n" + word);
	    List<WordPairAssociation> associations = binaryMatrix.getAllAssociationsFor(word);
	    //System.out.println(associations);
	    for(WordPairAssociation wpa : associations) {
		String other = wpa.getOther(word).get();
		//double prob = (double)wpa.getCount() / (double)this.getOncePerSentenceWordHist().get(word).get().count;
		double probWithBeta = this.probAInSentenceGivenB(wpa.getOther(word).get(), word);
		if(scores.containsKey(other)) {
		    scores.put(other, scores.get(other) + probWithBeta);
		} else {
		    scores.put(other, probWithBeta);
		}
		scoresTree.insert(other, probWithBeta, DuplicateEntryOption.UPDATE);
	    }
	}
	scoresTree.insert(UNKNOWN, .00000001, DuplicateEntryOption.REPLACE);	//hack to deal with the hack of initializing the WBT with a dummy root with weight 1
	List<WeightedBinaryTree<String>> orderedScores = scoresTree.getAsList(WeightedBinaryTree.SortType.WEIGHT).stream().filter(t -> !t.getKey().equals(UNKNOWN)).collect(toList());
	double totalWeight = scoresTree.getTreeWeight();
	ProbDist<String> scoresProbs = new ProbDist<>();
	orderedScores.stream().forEach(t -> {	
	    scoresProbs.add(t.getKey(), t.getWeight() / totalWeight);
	});/**/
	
	
	//instead of doing a regular Naive Bayes, doing something slightly different in coming up with a probability for each associated word for each
	//word in the sentence, and then adding that to a WeightedBinaryTree.  Each time we encounter that associated word for another word in the
	//sentence, we update the weight in the tree.  It creates a final score for each word.  Not exactly Naive Bayes I suppose, but effectively is
	//about the same, I think.
	return scoresProbs;
    }
    
    public List<String> tokenizeSentence(String sentence) {
	return Capstone.tokenize(sentence, new Request("").setRemoveStopWords(false));
    }
    
    /**
     * returns the adjusted probability that a and b appear in the same sentence, taking into account the prior belief specified by beta
     * @param a
     * @param b
     * @return the probability
     */
    protected double probAInSentenceWithB(String a, String b) {
	//TODO:  The line below currently gives the number of times A and B were found to occur together in the same sentence.
	//So if they occur once in the document in the same sentence, it is 1.0.  If they occur twice, but two times within that same sentence,
	//you get 2.0.  If you are looking at just the probability that they occur in the same sentence, this seems incorrect.  Maybe.
	//But for now let's try it.  Again, our probability of A given B is sortof a best guess anyway.
	int numAandB = this.binaryMatrix.get(a, b);
	return (double)numAandB / (double)(this.sentences.size() * (1.0 + this.beta));
    }
    
    /**
     * returns the probability that a will be in a given sentence, given that b has been found in that sentence
     * @param a
     * @param b
     * @return 
     */
    protected double probAInSentenceGivenB(String a, String b) {
	//TODO: some data structure to keep track of how often a word appears in a sentence?  For now, just estimate with 
	//#occurences of B divided by total number of words.
	return this.probAInSentenceWithB(a, b) / this.getFractionOfSentencesWith(b);	//this.getFractionOfWords(b) represent prob(b)
    }
    
    //TODO: some probability distribution for the number of occurances of a given an occurence of b, or given a given number
    //of occurences of b
    
    
    protected double getFractionOfWords(String b) {
	//return this.totalAllWordHist.queryFromFirst(word -> word.equals(b)).size() / this.totalAllWordHist.getTotalCount();
	/*List<HistogramEntry<String>> entries = this.totalAllWordHist.queryAll(word -> word.equals(b));
	if(entries.isEmpty()) {
	    return 0.0;
	} else {
	    return (double) entries.get(0).count / (double) this.totalAllWordHist.getTotalCount();
	}*/
	Optional<HistogramEntry<String>> entry = this.totalAllWordHist.get(b);
	if(entry.isPresent()) {
	    return (double)entry.get().count / (double) this.totalAllWordHist.getTotalCount();
	} else {
	    return 0.0;
	}
    }
    
    protected double getFractionOfSentencesWith(String b) {
	Optional<HistogramEntry<String>> entry = this.oncePerSentenceWordHist.get(b);
	if(entry.isPresent()) {
	    return (double)entry.get().count / (double) sentences.size();
	} else {
	    return 0.0;
	}
    }
    
    
    protected double[] generateRandomGenome() {
	//double[] newGenome = new double[this.genomeLength];
	return Random.getUniformDoubles(genomeLength, 0.0, 5.0);
    }
    
    protected double[] mutateGenome() {
	//TODO:  fill in; use SecureRandom
	double rand = Math.random();
	int index = 0;
	if(rand < .1) {
	    this.genome = this.generateRandomGenome();
	} else {
	    rand = Math.random();
	    index = (int)(rand * (double)3);
	    double newValue = Random.getUniformDoubles(1, 0.0, 5.0)[0];
	    this.genome[index] = newValue;
	    //System.out.println(index + " " + newValue);
	}
	return this.genome;
    }
    
    protected String constructNgram(List<String> sentence, int length) {
	return this.constructNgram(sentence, length, 0);
    }
    
    protected String constructNgram(List<String> sentence, int length, int numToLeaveOff) {
	//"something of the subject we are talking about"
	String result = "";
	if(sentence == null || sentence.isEmpty() || numToLeaveOff >= sentence.size()) {
	    return result;
	}
	StringBuilder s = new StringBuilder();
	int lastIndexToUse = (sentence.size() - 1) - numToLeaveOff;
	int firstIndexToUse = lastIndexToUse - (length - 1);
	if(firstIndexToUse < 0) {
	    return result;
	}
	for(int i = firstIndexToUse; i < lastIndexToUse; i++) {
	    s.append(sentence.get(i)).append(Constants.SPACE);
	}
	s.append(sentence.get(lastIndexToUse));
	return s.toString();
    }
    
    private ProbDist<String> combineProbDists(List<ProbDist<String>> dists) {
	return this.combineProbDists(dists, new double[] { this.genome[this.NGRAM_WEIGHT_POSITION], this.genome[this.WORD_ASSOCIATION_WEIGHT_POSITION], this.genome[this.RANDOM_WEIGHT_POSITION] });
    }
    
    protected ProbDist<String> combineProbDists(List<ProbDist<String>> dists, double[] weights) {
	//skip check of sizes matching for now...
	
	ProbDist<String> result = new ProbDist<>();
	double totalWeight = toolbox.util.MathUtil.sum(weights);
	double weight = 0.0;
	ProbDist<String> currentDist = null;
	List<String> words = null;
	List<Double> probs = null;
	String word = null;
	TreeMap<String, Double> resultMap = new TreeMap<>();
	WeightedBinaryTree<String> t = new WeightedBinaryTree<>(UNKNOWN, 1);
	for(int currentDistIndex = 0; currentDistIndex < dists.size(); currentDistIndex++) {
	    currentDist = dists.get(currentDistIndex);
	    weight = weights[currentDistIndex] / totalWeight;
	    words = currentDist.getValues();
	    probs = currentDist.getProbabilities();
	    for(int currentProbIndex = 0; currentProbIndex < currentDist.getValues().size(); currentProbIndex++) {
		word = words.get(currentProbIndex);
		if(word == null) {
		    continue;
		}
		/**if(resultMap.keySet().contains(word)) {
		    resultMap.put(word, resultMap.get(word) + probs.get(currentProbIndex) * weight);
		} else {
		    resultMap.put(word, probs.get(currentProbIndex) * weight);
		}/**/
		t.insert(word, probs.get(currentProbIndex) * weight, DuplicateEntryOption.UPDATE);
	    }
	}
	//resultMap.keySet().stream().forEach(k -> result.add(k, resultMap.get(k)));
	t.insert(UNKNOWN, .00000001, DuplicateEntryOption.REPLACE);
	t.getAsList(WeightedBinaryTree.SortType.WEIGHT).stream().forEach(node -> {
	    if(!node.getKey().equals(UNKNOWN)) {
		result.add(node.getKey(), node.getWeight());
	    }
	});
	return result;
    }
}
