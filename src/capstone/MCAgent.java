/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package capstone;

import java.io.IOException;
import java.util.List;
import toolbox.stats.HistogramEntry;
import toolbox.stats.TreeHistogram;

/**
 *
 * @author paul
 */
public class MCAgent {
    
    private List<String> sentences;
    private TreeHistogram<String> ngrams;
    private WordMatrix matrix;
    
    public MCAgent(List<String> sentences, TreeHistogram<String> ngrams, WordMatrix matrix) {
	this.sentences = sentences;
	this.ngrams = ngrams;
	this.matrix = matrix;
    }

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
		    System.out.println(token);
		    System.out.println(sentenceWordMatrix.getAllAssociationsFor(token));
		}
	    }
	} catch(IOException e) {
	    System.err.println(e.getClass() + " trying to get the WordMatrix:  " + e.getMessage());
	} catch(Exception e) {
	    System.err.println(e.getClass() + " trying to get the WordMatrix:  " + e.getMessage());
	}
    }
}
