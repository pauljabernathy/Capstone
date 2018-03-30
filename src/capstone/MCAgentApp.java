/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package capstone;

import capstone.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import static java.util.stream.Collectors.toList;
import org.apache.logging.log4j.*;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import toolbox.stats.HistogramEntry;
import toolbox.stats.TreeHistogram;

/**
 *
 * @author paul
 */
public class MCAgentApp {
    
    private static Logger logger;
    private static MCAgent instance;
    
    private static TreeHistogram<String> ngrams;
    private static List<String> matchingNGrams;
    private static List<HistogramEntry<String>> m;
    private static WordMatrix matrix;
    private static WordMatrix binaryMatrix;
    
    private double EPSILON = 0.0001;
    
    private static SimpleDateFormat format;
    
    @BeforeClass
    public static void setUpClass() {
	logger = toolbox.util.ListArrayUtil.getLogger(MCAgentApp.class, Level.INFO);
	format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Calendar start = Calendar.getInstance();
	
	String filename = "les_miserables.txt";
	//filename = "through_the_looking_glass.txt";
	//filename = "beowulf i to xxii.txt";
	//TODO: use a small test file with known stats
	/*if(instance == null) {
	    try {
		//TODO:  Refactor Capstone to be able to make a word histogram from the sentences list, to cut down on how many times it has to read the file.
		Request request = new Request(filename).setRemoveStopWords(true);
		List<String> sentences = Capstone.readSentencesFromFile(filename);
		TreeHistogram<String> ngrams = NGrams.getNGramsOfSentences(sentences, 3);	//TODO:  have the MCAgent compute this based on it's object variable, which should be specified in the genome
		String ngram = "while he";
		List<String> matchingNGrams = ngrams.queryFromFirst(word -> word.startsWith(ngram));
		List<HistogramEntry<String>> m = ngrams.queryAll(word -> word.startsWith(ngram));
		System.out.println(ngrams.getAsList(TreeHistogram.Sort.COUNT).stream().limit(50).collect(toList()));
		System.out.println("\n" + matchingNGrams);
		System.out.println("\n" + m);

		WordMatrix matrix = Capstone.findWordMatrixFromSentenceList(sentences, request);
		WordMatrix binaryMatrix = Capstone.findWordMatrixFromSentenceList(sentences, new Request(filename).setRemoveStopWords(true).setBinaryAssociationsOnly(true));

		TreeHistogram<String> totalAllWordHist = Capstone.fileSummaryTreeHistogram(request.setRemoveStopWords(false));
		//logger.info("\ntotalAllWordHist");
		//totalAllWordHist.getAsList(TreeHistogram.Sort.COUNT).stream().limit(50).forEach(System.out::println);
		TreeHistogram<String> totalNonStopWordHist = Capstone.fileSummaryTreeHistogram(request.setRemoveStopWords(true).setBinaryAssociationsOnly(true));
		//logger.info("\ntotalNonStopWordHist");
		//totalNonStopWordHist.getAsList(TreeHistogram.Sort.COUNT).stream().limit(50).forEach(System.out::println);
		TreeHistogram<String> oncePerSentenceWordHist = Capstone.fileSummaryTreeHistogram(request.setBinaryAssociationsOnly(true));
		//logger.info("\noncePerSentence");
		//oncePerSentenceWordHist.getAsList(TreeHistogram.Sort.COUNT).stream().limit(50).forEach(System.out::println);

		//MCAgent(List<String> sentences, TreeHistogram<String> totalAllWordHist, TreeHistogram<String> totalNonStopWordHist, TreeHistogram<String> oncePerSentenceWordHist, 
	//TreeHistogram<String> ngrams, WordMatrix weightedMatrix, WordMatrix binaryMatrix) {
		//instance = new MCAgent(totalAllWordHist, sentences, ngrams, matrix);
		instance = new MCAgent(sentences);
		instance.setTotalAllWordHist(totalAllWordHist)
		.setTotalNonStopWordHist(totalNonStopWordHist)    
		.setOncePerSentenceWordHist(oncePerSentenceWordHist)
		.setNgrams(ngrams).setWeightedMatrix(matrix)
		.setBinaryMatrix(binaryMatrix);
		sentences.stream().forEach(s -> {
		    if(s.contains(" win ")) {
			System.out.println("\n-" + s);
		    }
		});
		
		//logger.debug(instance.getNGramProbDist());
	    } catch(IOException e) {
		System.err.println(e.getClass() + " " + e.getMessage());
	    }
	}*/
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
	
    }
    
    @After
    public void tearDown() {
    }
    
    private MCAgent instantiateAgent(String filename) {
	logger.info("instantiating agent from file " + filename);
	try {
	    Calendar start = Calendar.getInstance();
	    //TODO:  Refactor Capstone to be able to make a word histogram from the sentences list, to cut down on how many times it has to read the file.
	    Request request = new Request(filename).setRemoveStopWords(true);
	    List<String> sentences = Capstone.readSentencesFromFile(filename);
	    TreeHistogram<String> ngrams = NGrams.getNGramsOfSentences(sentences, 3);	//TODO:  have the MCAgent compute this based on it's object variable, which should be specified in the genome
	    

	    WordMatrix matrix = Capstone.findWordMatrixFromSentenceList(sentences, request);
	    WordMatrix binaryMatrix = Capstone.findWordMatrixFromSentenceList(sentences, new Request(filename).setRemoveStopWords(true).setBinaryAssociationsOnly(true));

	    TreeHistogram<String> totalAllWordHist = Capstone.fileSummaryTreeHistogram(request.setRemoveStopWords(false));
	    //logger.info("\ntotalAllWordHist");
	    //totalAllWordHist.getAsList(TreeHistogram.Sort.COUNT).stream().limit(50).forEach(System.out::println);
	    TreeHistogram<String> totalNonStopWordHist = Capstone.fileSummaryTreeHistogram(request.setRemoveStopWords(true).setBinaryAssociationsOnly(true));
	    //logger.info("\ntotalNonStopWordHist");
	    //totalNonStopWordHist.getAsList(TreeHistogram.Sort.COUNT).stream().limit(50).forEach(System.out::println);
	    TreeHistogram<String> oncePerSentenceWordHist = Capstone.fileSummaryTreeHistogram(request.setBinaryAssociationsOnly(true));
	    //logger.info("\noncePerSentence");
	    //oncePerSentenceWordHist.getAsList(TreeHistogram.Sort.COUNT).stream().limit(50).forEach(System.out::println);

	    //MCAgent(List<String> sentences, TreeHistogram<String> totalAllWordHist, TreeHistogram<String> totalNonStopWordHist, TreeHistogram<String> oncePerSentenceWordHist, 
    //TreeHistogram<String> ngrams, WordMatrix weightedMatrix, WordMatrix binaryMatrix) {
	    //instance = new MCAgent(totalAllWordHist, sentences, ngrams, matrix);
	    MCAgent agent = new MCAgent(sentences);
	    agent.setTotalAllWordHist(totalAllWordHist)
	    .setTotalNonStopWordHist(totalNonStopWordHist)    
	    .setOncePerSentenceWordHist(oncePerSentenceWordHist)
	    .setNgrams(ngrams).setWeightedMatrix(matrix)
	    .setBinaryMatrix(binaryMatrix);

	    //logger.debug(instance.getNGramProbDist());
	    Calendar end = Calendar.getInstance();
	    logger.info(format.format(start.getTime()));
	    logger.info(format.format(end.getTime()));
	    logger.info(end.getTimeInMillis() - start.getTimeInMillis() + " millils to instantiate agent");
	    return agent;
	} catch(IOException e) {
	    System.err.println(e.getClass() + " " + e.getMessage());
	    return new MCAgent(null);
	}
    }
    
    @Test
    public void doPredictionsOnLesMisNoMC() {
	logger.info("\ndoPredictionsOnLesMisNoMC()");
	instance = this.instantiateAgent("les_miserables.txt");
	instance.setGenome(new double[] { 1.150221459310199, 2.853131659824399, 0.08473017271422967, 4.3904694260382735, 4.444581619967423, 0.5530100314467706, 1.142990210003586, 3.839286815451411 });
	instance.doPredictions(10);
    }
    
    @Test
    public void doPredictionsOnBlogsSample() {
	logger.info("\ndoPredictionsOnBlogsSample()");
	instance = this.instantiateAgent("blogsSample_1percent.txt");
	instance.setGenome(new double[] { 1.150221459310199, 2.853131659824399, 0.08473017271422967, 4.3904694260382735, 4.444581619967423, 0.5530100314467706, 1.142990210003586, 3.839286815451411 });
	instance.doPredictions(10);
    }
    
    @Test
    public void testDoPredictions() {
	logger.info("\ndoPredictions");
	Calendar start = Calendar.getInstance();
	instance.setGenome(new double[] { 1.150221459310199, 2.853131659824399, 0.08473017271422967, 4.3904694260382735, 4.444581619967423, 0.5530100314467706, 1.142990210003586, 3.839286815451411 });
	instance.doPredictions(10);
	Calendar end = Calendar.getInstance();
	logger.info(format.format(start.getTime()));
	logger.info(format.format(end.getTime()));
	logger.info(end.getTimeInMillis() - start.getTimeInMillis());
    }
    
    @Test
    public void doPredictionsQuiz2() {
	logger.info("\ndoPredictionsQuiz2()");
	String filename = "les_miserables.txt";
	filename = "blogsSample_5percent.txt";
	filename = "blogsSample1.txt";
	instance = this.instantiateAgent(filename);
	
	Calendar start = Calendar.getInstance();
	instance.setGenome(new double[] { 1.541870530631559, 2.853131659824399, 4.4919195414649025, 4.3904694260382735, 4.444581619967423, 0.5530100314467706, 1.142990210003586, 3.839286815451411 });
	List<String> sentences = new ArrayList<>();
	sentences.add("The guy in front of me just bought a pound of bacon, a bouquet, and a case of");
	//sentences.add("You're the reason why I smile everyday. Can you follow me please? It would mean the");
	/*sentences.add("Hey sunshine, can you follow me and make me the");
	sentences.add("Very early observations on the Bills game: Offense still struggling but the");
	sentences.add("Go on a romantic date at the");
	sentences.add("Well I'm pretty sure my granny has some old bagpipes in her garage I'll dust them off and be on my");
	sentences.add("Ohhhhh #PointBreak is on tomorrow. Love that film and haven't seen it in quite some");
	sentences.add("After the ice bucket challenge Louis will push his long wet hair out of his eyes with his little");
	sentences.add("Be grateful for the good times and keep the faith during the");
	sentences.add("If this isn't the cutest thing you've ever seen, then you must be");*/
	//sentences.add("");
	//sentences.add("");
	
	List<List<String>> tokenizedSentences = new ArrayList<>();
	sentences.forEach(s -> tokenizedSentences.add(Capstone.tokenize(s, new Request("").setRemoveStopWords(false))));
	
	tokenizedSentences.forEach(s -> { 
	    try {
		logger.info(s + ":  " + instance.makeOnePrediction(s));
	    } catch(Exception e) {
		System.err.println(e.getClass() + " for sentence " + s + ":  " + e.getMessage());
	    }
	});
	Calendar end = Calendar.getInstance();
	logger.info(format.format(start.getTime()));
	logger.info(format.format(end.getTime()));
	logger.info(end.getTimeInMillis() - start.getTimeInMillis() + " millis to do the predictions");
    }
    
    public static void main(String[] args) {
	MCAgentApp.setUpClass();
	MCAgentApp m = new MCAgentApp();
	m.doPredictionsQuiz2();
    }
}
