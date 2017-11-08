/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package capstone;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import toolbox.stats.HistogramEntry;
import toolbox.stats.TreeHistogram;

/**
 *
 * @author paul
 */
public class MCAgent {
    
    //TODO: inject a genome to code for parameters
    private double beta;    //deals with prior probability of one word given another; parameter specifies how many times we are pretending we found the same word histogram before but with no words together in a sentence
    //ex. beta = 2 => There are 1000 sentences. Word A and word B occur once each, in the same sentence.  P(A|B) really 1?  If beta = 2, we pretend we have 2000 prior sentences and have found A and B twice each, but not in the same sentence.
    //=> P(A|B) = 1/3.  If we then find A and B together in the same sentence 10 times, and always together, P(A|B) = 10/12.
    
    private TreeHistogram<String> wordHist;
    private List<String> sentences;
    private TreeHistogram<String> ngrams;
    private WordMatrix matrix;
    
    public MCAgent(TreeHistogram<String> wordHist, List<String> sentences, TreeHistogram<String> ngrams, WordMatrix matrix) {
	this.wordHist = wordHist;
	this.sentences = sentences;
	this.ngrams = ngrams;
	this.matrix = matrix;
    }

    public double getBeta() {
	return beta;
    }

    public MCAgent setBeta(double beta) {
	this.beta = beta;
	return this;
    }

    public TreeHistogram<String> getWordHist() {
	return wordHist;
    }

    public MCAgent setWordHist(TreeHistogram<String> wordHist) {
	this.wordHist = wordHist;
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

    public WordMatrix getMatrix() {
	return matrix;
    }

    public MCAgent setMatrix(WordMatrix matrix) {
	this.matrix = matrix;
	return this;
    }

    /**
     * Does one run of the simulation
     */
    protected void doOneRun() {
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
		    List<WordPairAssociation> associations = matrix.getAllAssociationsFor(token);
		    System.out.println(matrix.getAllAssociationsFor(token));
		    
		    //Find the words with the highest probability to appear in the same sentence as word "token."
		    for(WordPairAssociation wpa : associations) {
			String other = wpa.getOther(token).get();
			System.out.println(other + " " + wpa.getCount() + " " + this.probAInSentenceWithB(other, token) + " " + this.probAInSentenceWithB(token, other));
			System.out.println(this.probAGivenB(other, token));
		    }
		}
	    }
	} catch(IOException e) {
	    System.err.println(e.getClass() + " trying to get the WordMatrix:  " + e.getMessage());
	} catch(Exception e) {
	    System.err.println(e.getClass() + " trying to get the WordMatrix:  " + e.getMessage());
	}
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
	int numAandB = this.matrix.get(a, b);
	return (double)numAandB / (double)(this.sentences.size() * (1.0 + this.beta));
    }
    
    /**
     * returns the probability that a will be in a given sentence, given that b has been found in that sentence
     * @param a
     * @param b
     * @return 
     */
    protected double probAGivenB(String a, String b) {
	//TODO: some data structure to keep track of how often a word appears in a sentence?  For now, just estimate with 
	//#occurences of B divided by total number of words.
	return this.probAInSentenceWithB(a, b) / this.getFractionOfWords(b);	//this.getFractionOfWords(b) represent prob(b)
    }
    
    //TODO: some probability distribution for the number of occurances of a given an occurence of b, or given a given number
    //of occurences of b
    
    
    protected double getFractionOfWords(String b) {
	//return this.wordHist.queryFromFirst(word -> word.equals(b)).size() / this.wordHist.getTotalCount();
	/*List<HistogramEntry<String>> entries = this.wordHist.queryAll(word -> word.equals(b));
	if(entries.isEmpty()) {
	    return 0.0;
	} else {
	    return (double) entries.get(0).count / (double) this.wordHist.getTotalCount();
	}*/
	Optional<HistogramEntry<String>> entry = this.wordHist.findFirst(word -> word.equals(b));
	if(!entry.isPresent()) {
	    return 0.0;
	} else {
	    return (double)entry.get().count / (double) this.wordHist.getTotalCount();
	}
    }
}
