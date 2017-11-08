/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package capstone;

import static capstone.Capstone.DEFAULT_BREAKS_BETWEEN_WORDS;
import static capstone.Capstone.findWordMatrix;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import toolbox.stats.HistogramEntry;
import toolbox.stats.TreeHistogram;

/**
 *
 * @author paul
 */
public class Wanderer {
    
    public static void main(String[] args) {
	Date start = new Date();
	System.out.println();
	/*try {
	    WordMatrix matrix = Capstone.findWordMatrixFromFile("through_the_looking_glass.txt");
	    System.out.println(matrix.getAllAssociationsFor("knight"));
	    System.out.println(matrix.getAllAssociationsFor("looking"));
	    System.out.println(matrix.getAllAssociationsFor("glass"));
	} catch(IOException e) {
	    System.err.println(e.getClass() + " trying to get the WordMatrix:  " + e.getMessage());
	}
	System.out.println("got the word matrix?");*/
	
	try {
	    String filename = "les_miserables.txt";
	    filename = "through_the_looking_glass.txt";
	    /*WordMatrix matrix = Capstone.findWordMatrixFromFile(filename);
	    //System.out.println(matrix.getAllAssociationsFor("cosette"));
	    //System.out.println(matrix.getAllAssociationsFor("jean"));
	    matrix.getTopAssociationsFor("cosette", 5).stream().forEach(a -> System.out.println(a));
	    matrix.getTopAssociationsFor("cosette", 5).stream().forEach(a -> System.out.println(a.toStringExclude("cosette")));
	    matrix.getTopAssociationsFor("jean", 5).stream().forEach(a -> System.out.println(a));
	    matrix.getTopAssociationsFor("jean", 5).stream().forEach(a -> System.out.println(a.toStringExclude("jean")));*/
	    
	    List<String> sentences = Capstone.readSentencesFromFile(filename);
	    List<String> sampleSentences = toolbox.random.Random.sample(sentences, 2, true);
	    sampleSentences.forEach(System.out::println);
	    
	    TreeHistogram<String> ngrams = NGrams.readNGramsFromFile(filename);
	    
	    WordMatrix matrix = Capstone.findWordMatrixFromSentenceList(sentences, new Request("").setRemoveStopWords(true));
	    
	    for(String sentence : sampleSentences) {
		System.out.println();
		String[] words = sentence.split(" ");
		if(words.length < 4) {
		    continue;
		}
		//List<String> tokens = Capstone.tokenize(sentence, new Request(null).setRemoveStopWords(true));
		//String[] words = tokens.toArray(new String[] {});
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
		WordMatrix matrixOfSentence = Capstone.findWordMatrix(tokens.toArray(new String[] {}));
		for(String token: tokens) {
		    System.out.println(token);
		    System.out.println(matrixOfSentence.getAllAssociationsFor(token));
		}
	    }
	} catch(IOException e) {
	    System.err.println(e.getClass() + " trying to get the WordMatrix:  " + e.getMessage());
	} catch(Exception e) {
	    System.err.println(e.getClass() + " trying to get the WordMatrix:  " + e.getMessage());
	}
	
	Date end = new Date();
	System.out.println(end.getTime() - start.getTime());
    }
    
}
