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
import toolbox.random.Random;
import toolbox.stats.*;
import toolbox.trees.DuplicateEntryOption;
import toolbox.trees.WeightedBinaryTree;

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
public class MCAgent {
    
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
    
    private double[] genome;
    private final int NGRAM_WEIGHT_POSITION = 0;
    private final int WORD_ASSOCIATION_WEIGHT_POSITION = 1;
    private final int RANDOM_WEIGHT_POSITION = 2;
    private final int NGRAM_LENGTH_POSITION = 3;
    
    private int genomeLength = 8;
    
    private static final String UNKNOWN = "UNKNOWN";
    
    public MCAgent(List<String> sentences) {
	this.sentences = sentences;
	//sentences is actually the only true information.  The rest can be calculated from it and are only for speed.
	//The other objects should be passed in because the MCAgentRunner can calculate it all one for all of the agents.
	//If this constructor is used, the calling code will either need to use the setters or this class will need to call the appropriate methods in Capstone.
	
	this.beta = Constants.DEFAULT_BETA;
	this.ngramLength = 3;
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
	this.sentences = sentences;
	this.totalAllWordHist = totalAllWordHist;
	this.totalNonStopWordHist = totalNonStopWordHist;
	this.oncePerSentenceWordHist = oncePerSentenceWordHist;
	this.ngrams = ngrams;
	this.weightedMatrix = weightedMatrix;
	this.binaryMatrix = binaryMatrix;
	
	this.wordProbDist = this.totalAllWordHist.computeProbDist();
	this.ngramsProbDist = this.ngrams.computeProbDist();
	
	this.genome = this.generateRandomGenome();
	this.beta = Constants.DEFAULT_BETA;
	this.ngramLength = 2;
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

    public void setBinaryMatrix(WordMatrix binaryMatrix) {
	this.binaryMatrix = binaryMatrix;
    }

    public ProbDist<String> getWordProbDist() {
	return wordProbDist;
    }

    public void setWordProbDist(ProbDist<String> wordProbDist) {
	this.wordProbDist = wordProbDist;
    }

    public ProbDist<String> getNgramsProbDist() {
	return ngramsProbDist;
    }

    public void setNgramsProbDist(ProbDist<String> ngramsProbDist) {
	this.ngramsProbDist = ngramsProbDist;
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

    public void setNgramLength(int ngramLength) {
	this.ngramLength = ngramLength;
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

    /**
     * Does one run of the simulation
     */
    protected void doOneRunDemo() {
	try {
	    List<String> sampleSentences = toolbox.random.Random.sample(sentences, 2, true);
	    sampleSentences.forEach(System.out::println);
	    
	    for(String sentence : sampleSentences) {
		System.out.println();
		String[] words = sentence.split(" ");
		if(words.length < 4) {
		    continue;
		}
		int n = words.length;
		String threeGram = words[n - 4] + " " + words[n - 3] + " " + words[n - 2];
		//System.out.println(threeGram);
		String twoGram = words[n - 3] + " " + words[n - 2];
		System.out.println(twoGram);
		String toPredict = words[n - 1];
		List<HistogramEntry<String>> matches = ngrams.queryAll(ng -> ng.startsWith(twoGram));
		matches.forEach(m -> System.out.println("\t" + m));
		
		//tokenizing this separately because here we want to remove stop words and we do not want to on the ngrams
		List<String> tokens = Capstone.tokenize(sentence, new Request(null).setRemoveStopWords(true));
		WordMatrix sentenceWordMatrix = Capstone.findWordMatrix(tokens.toArray(new String[] {}));
		for(String token: tokens) {
		    System.out.println(token + " " + this.getFractionOfWords(token));
		    //System.out.println(sentenceWordMatrix.getAllAssociationsFor(token));
		    List<WordPairAssociation> associations = weightedMatrix.getAllAssociationsFor(token);
		    System.out.println(weightedMatrix.getAllAssociationsFor(token));
		    
		    //Find the words with the highest probability to appear in the same sentence as word "token."
		    for(WordPairAssociation wpa : associations) {
			String other = wpa.getOther(token).get();
			System.out.println(other + " " + wpa.getCount() + " " + this.probAInSentenceWithB(other, token) + " " + this.probAInSentenceWithB(token, other));
			System.out.println(this.probAInSentenceGivenB(other, token));
		    }
		}
	    }
	} catch(IOException e) {
	    System.err.println(e.getClass() + " trying to get the WordMatrix:  " + e.getMessage());
	} catch(Exception e) {
	    System.err.println(e.getClass() + " trying to get the WordMatrix:  " + e.getMessage());
	}
    }
    
    
    public void doOneBatch() {
	//make a series of runs (doOneRunDemo() may not be exactly the right function but it is the same idea), making a prediction each time
	
	//count the number correct predictions
    }
    
    public boolean doOneRun() {
	
	//Find a random sentence
	String sentence = sentences.get(Random.uniformInts(1, 0, sentences.size() - 1)[0]);
	
	
	//make a prediction on what the last word is
	
	//return if it was correct or not
	return false;
    }
    
    public String makeOnePrediction(String sentence) {
	
	//Token the sentence.  Should we remove the stop words here?  Should that be part of the genome?
	//I think the stop words stay because they are used in the ngrams.  But they are not used in the associations.
	//Maybe tokenize twice - once for the ngrams and another for the associations.
	List<String> allWords = Capstone.tokenize(sentence, new Request("").setRemoveStopWords(false));
	List<String> goWords = Capstone.tokenize(sentence, new Request("").setRemoveStopWords(true));
	
	List<String> words = Capstone.tokenize(sentence, new Request("").setRemoveStopWords(false));
	//String lastWord = words.get(words.size() - 1);
	
	//From the words in it, try to predict the last word.
	//=>
	//ngram prediction
	//Get the ngram at the lastIndexToUse minus the last word (2, 3, 4 etc. specified by genome)
	//based on that, find the most likely next work.

	ProbDist<String> associations = this.getWordAssociationScores(words);
	List<ProbDist<String>> dists = new ArrayList<>();
	dists.add(this.ngramsProbDist);
	dists.add(associations);
	dists.add(this.wordProbDist);
	ProbDist<String> totalProbDist = this.combineProbDists(dists);
	System.out.println(totalProbDist);
	
	return totalProbDist.getValue(0);
    }
    
    public String doNgramPrediction(List<String> sentence) {
	//look at the histogram of ngrams to see which ones firstIndexToUse with the given ngram
	//find the most common one and return it
	//TODO:  an element of randomness?  maybe choose a random one, with the probability being equal to its proportion in the histogram?
	//maybe specify the randomness in the genome?
	
	String ngram = this.constructNgram(sentence, 2);
	System.out.println(ngram);
	Predicate<String> p = word -> word.startsWith(ngram);
	//List<String> matchingNGrams = this.ngrams.queryFromFirst(p);	    //TODO:  find out why this gives an error (problem in TreeHistogram and/or WeightedBinaryTree)
	/*List<HistogramEntry<String>> m = this.ngrams.queryAll(p).stream().sorted((a, b) -> {
	    if(a.count > b.count) {
		return -1;
	    } else if (a.count == b.count) {
		return 0;
	    } else {
		return 1;
	    }
	}).collect(toList());*/
	
	//System.out.println("\n" + matchingNGrams);
	//System.out.println("\nm = " + m);
	Optional<HistogramEntry<String>> he = this.ngrams.findFirst(p);	    //faster than queryFromFirst, sort, and find the first element; O(logn) vs O(n) for queryAll and O(nlogn) for sort
	//works because the ngrams is a TreeHistogram, with the items of higher count higher up in the tree
	//If we were to change to a different histogram type or something else about the data structure or the way it is used were to change, this might now work.
	//Actually, I just remembered, in the WeightedBinaryTree, it has the capability of not rebalancing until the child trees are of a certain % weight
	//more than the parent tree.  Right now it always rebalances when the children are of higher weight, but if that changes then this also could cause
	//the assumption in the above line of code to be violated.
	if(he.isPresent()) {
	    //System.out.println("he = " + he.get());
	    return he.get().item.split(" ")[this.ngramLength];	    //TODO: proper computation of this; and coherent handling of ngram length
	}
	//return m.get(0).item.split(" ")[2];
	return "";	    //TODO: random word from the all word histogram
    }
    
    public ProbDist<String> getNGramProbDist(List<String> sentence) {
	String ngram = this.constructNgram(sentence, 2);
	System.out.println(ngram);
	Predicate<String> p = word -> word.startsWith(ngram);
	List<String> matchingNGrams = this.ngrams.queryFromFirst(p);	    //TODO:  find out why this gives an error (problem in TreeHistogram and/or WeightedBinaryTree)
	
	return null;
    }
    
    public String doWordAssociationPrediction(List<String> sentence) {
	ProbDist<String> scores = this.getWordAssociationScores(sentence);
	//TODO:  possibly return a random value; whether or not we return the most likely value
	//or a random value could be specified in the genome
	return scores.getValue(0);/**/
    }
    
    public ProbDist<String> getWordAssociationScores(List<String> sentence) {
	sentence = sentence.stream().distinct().collect(toList());
	//System.out.println(sentence);
	//for each word in the sentence
	//For each association, use probAInSentenceWithB to find the prob of each word the given word is in a sentence with.
	//This gives a probability distribution.
	//After all dists have been determined, try Naive Bayes (yes, it's very naive, but may work OK)
	Map<String, ProbDist> dists = new HashMap<>();
	Set<String> associatedWords = new HashSet<>();   //to keep track of words that are associated with any word in this sentence and could be candiates for the predicted word; will hold just a small portion of the total words
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
		associatedWords.add(wpa.getOther(word).get());
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
    
    protected double[] mutateGenome(double[] current) {
	//TODO:  fill in
	return current;
    }
    
    protected String constructNgram(List<String> sentence, int length) {
	return this.constructNgram(sentence, length, 1);
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
		if(resultMap.keySet().contains(word)) {
		    resultMap.put(word, resultMap.get(word) + probs.get(currentProbIndex) * weight);
		} else {
		    resultMap.put(word, probs.get(currentProbIndex) * weight);
		}
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
