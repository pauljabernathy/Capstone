/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package capstone;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    
    private int ngramLength;
    
    private double[] genome;
    private int genomeLength = 8;
    
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
	
	
	//From the words in it, try to predict the last word.
	//=>
	//ngram prediction
	//Get the ngram at the lastIndexToUse minus the last word (2, 3, 4 etc. specified by genome)
	//based on that, find the most likely next work.

	//
	
	
	return "";
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
	List<HistogramEntry<String>> m = this.ngrams.queryAll(word -> word.startsWith(ngram)).stream().sorted((a, b) -> {
	    if(a.count > b.count) {
		return -1;
	    } else if (a.count == b.count) {
		return 0;
	    } else {
		return 1;
	    }
	}).collect(toList());
	
	//System.out.println("\n" + matchingNGrams);
	System.out.println("\nm = " + m);
	Optional<HistogramEntry<String>> he = this.ngrams.findFirst(p);
	if(he.isPresent()) {
	    System.out.println("he = " + he.get());
	    return he.get().item.split(" ")[this.ngramLength];	    //TODO: proper computation of this; and coherent handling of ngram length
	}
	//return m.get(0).item.split(" ")[2];
	return "";	    //TODO: random word from the all word histogram
    }
    
    public String doWordAssociationPrediction(List<String> sentence) {
	WeightedBinaryTree<String> scoresTree = this.getWordAssociationPrediction(sentence);
	List<WeightedBinaryTree> orderedScores = scoresTree.getAsList(WeightedBinaryTree.SortType.WEIGHT).stream().limit(5).collect(toList());
	if(orderedScores != null && orderedScores.size() > 1) {
	    return orderedScores.get(1).getKey().toString();
	} else {
	    return "";
	}
    }
    
    public WeightedBinaryTree<String> getWordAssociationPrediction(List<String> sentence) {
	sentence = sentence.stream().distinct().collect(toList());
	//System.out.println(sentence);
	//for each word in the sentence
	//For each association, use probAInSentenceWithB to find the prob of each word the given word is in a sentence with.
	//This gives a probability distribution.
	//After all dists have been determined, try Naive Bayes (yes, it's very naive, but may work OK)
	Map<String, ProbDist> dists = new HashMap<>();
	Set<String> associatedWords = new HashSet<>();   //to keep track of words that are associated with any word in this sentence and could be candiates for the predicted word; will hold just a small portion of the total words
	Map<String, Double> scores = new HashMap<>();
	WeightedBinaryTree<String> scoresTree = new WeightedBinaryTree<String>("null", 1);
	for(String word : sentence) {
	    //System.out.println("\n" + word);
	    List<WordPairAssociation> associations = binaryMatrix.getAllAssociationsFor(word);
	    //System.out.println(associations);
	    for(WordPairAssociation wpa : associations) {
		String other = wpa.getOther(word).get();
		double prob = (double)wpa.getCount() / (double)this.getOncePerSentenceWordHist().get(word).get().count;
		double probWithBeta = this.probAInSentenceGivenB(wpa.getOther(word).get(), word);
		//System.out.println("this.getOncePerSentenceWordHist().get(word) == " + this.getOncePerSentenceWordHist().get(word));
		//System.out.println("(double)this.getOncePerSentenceWordHist().get(word).get().count == " + (double)this.getOncePerSentenceWordHist().get(word).get().count);
		//System.out.println(wpa.getOther(word).get() + " " + prob + " " + this.probAInSentenceGivenB(wpa.getOther(word).get(), word));
		//p.add(wpa.getOther(word).get(), prob);	//some clunky syntax
		//P(A|B) = P(A & B) / P(B)    P(A & B) = #sentences with A and B / # sentences	    P(B) = #sentences with B / #sentences
		//=> = #sentences with A and B / # sentences with B
		associatedWords.add(wpa.getOther(word).get());
		if(scores.containsKey(other)) {
		    scores.put(other, scores.get(other) + probWithBeta);
		} else {
		    scores.put(other, probWithBeta);
		}
		scoresTree.insert(other, probWithBeta, DuplicateEntryOption.UPDATE);
	    }
	}
	//instead of doing a regular Naive Bayes, doing something slightly different in coming up with a probability for each associated word for each
	//word in the sentence, and then adding that to a WeightedBinaryTree.  Each time we encounter that associated word for another word in the
	//sentence, we update the weight in the tree.  It creates a final score for each word.  Not exactly Naive Bayes I suppose, but effectively is
	//about the same, I think.
	/*System.out.println("\nscores");
	for(String other : scores.keySet()) {
	    System.out.println(other + " " + scores.get(other) + " " + scoresTree.get(other).getWeight());
	}*/
	
	//System.out.println("\nordered scores");
	//scoresTree.getAsList(WeightedBinaryTree.SortType.WEIGHT).stream().limit(25).forEach(System.out::println);
	//List<WeightedBinaryTree> orderedScores = scoresTree.getAsList(WeightedBinaryTree.SortType.WEIGHT).stream().limit(5).collect(toList());
	//TODO: possibly randomly select from the distribution
	return scoresTree;
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
}
