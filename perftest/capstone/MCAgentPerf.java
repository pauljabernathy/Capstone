/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capstone;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import static java.util.stream.Collectors.toList;
import java.util.stream.IntStream;
import org.apache.logging.log4j.*;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import toolbox.random.Random;
import toolbox.stats.HistogramEntry;
import toolbox.stats.ProbDist;
import toolbox.stats.TreeHistogram;

/**
 *
 * @author paul
 */
public class MCAgentPerf {

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
	format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Calendar start = Calendar.getInstance();
	logger = toolbox.util.ListArrayUtil.getLogger(MCAgentApp.class, Level.INFO);
	
	String filename = "les_miserables.txt";
	//filename = "through_the_looking_glass.txt";
	//filename = "beowulf i to xxii.txt";
	//TODO: use a small test file with known stats
	if(instance == null) {
	    try {
		//TODO:  Refactor Capstone to be able to make a word histogram from the sentences list, to cut down on how many times it has to read the file.
		Request request = new Request(filename).setRemoveStopWords(true);
		List<String> sentences = Capstone.readSentencesFromFile(filename);
		sentences = sentences.stream().map(s -> s.replaceAll("\\[", "")).collect(toList());
		sentences = sentences.stream().map(s -> s.replaceAll("\\]", "")).collect(toList());
		TreeHistogram<String> ngrams = NGrams.getNGramsOfSentences(sentences, 3);	//TODO:  have the MCAgent compute this based on it's object variable, which should be specified in the genome
		//String ngram = "while he";
		//List<String> matchingNGrams = ngrams.queryFromFirst(word -> word.startsWith(ngram));
		//List<HistogramEntry<String>> m = ngrams.queryAll(word -> word.startsWith(ngram));
		//System.out.println(ngrams.getAsList(TreeHistogram.Sort.COUNT).stream().limit(50).collect(toList()));
		//System.out.println("\n" + matchingNGrams);
		//System.out.println("\n" + m);

		matrix = Capstone.findWordMatrixFromSentenceList(sentences, request);
		binaryMatrix = Capstone.findWordMatrixFromSentenceList(sentences, new Request(filename).setRemoveStopWords(true).setBinaryAssociationsOnly(true));

		TreeHistogram<String> totalAllWordHist = Capstone.fileSummaryTreeHistogram(request.setRemoveStopWords(false));
		//logger.info("\ntotalAllWordHist");
		//totalAllWordHist.getAsList(TreeHistogram.Sort.COUNT).stream().limit(50).forEach(System.out::println);
		TreeHistogram<String> totalNonStopWordHist = Capstone.fileSummaryTreeHistogram(request.setRemoveStopWords(true).setBinaryAssociationsOnly(true));
		//logger.info("\ntotalNonStopWordHist");
		//totalNonStopWordHist.getAsList(TreeHistogram.Sort.COUNT).stream().limit(50).forEach(System.out::println);
		TreeHistogram<String> oncePerSentenceWordHist = Capstone.fileSummaryTreeHistogram(request.setBinaryAssociationsOnly(true));
		//logger.info("\noncePerSentence");
		//oncePerSentenceWordHist.getAsList(TreeHistogram.Sort.COUNT).stream().limit(50).forEach(System.out::println);
		
		instance = new MCAgent(sentences);
		instance.setTotalAllWordHist(totalAllWordHist)
		.setTotalNonStopWordHist(totalNonStopWordHist)    
		.setOncePerSentenceWordHist(oncePerSentenceWordHist)
		.setNgrams(ngrams).setWeightedMatrix(matrix)
		.setBinaryMatrix(binaryMatrix);
		/*sentences.stream().forEach(s -> {
		    if(s.contains(" win ")) {
			System.out.println("\n-" + s);
		    }
		});*/
		instance.setNgramsProbDist(instance.getNgrams().computeProbDist());
		if(instance.getWordProbDist() == null) {
		    instance.setWordProbDist(instance.getTotalAllWordHist().computeProbDist());
		}
		
		//logger.debug(instance.getNGramProbDist());
		Calendar end = Calendar.getInstance();
		System.out.println(format.format(start.getTime()));
		System.out.println(format.format(end.getTime()));
		System.out.println(end.getTimeInMillis() - start.getTimeInMillis());
		System.out.flush();
	    } catch(IOException e) {
		System.err.println(e.getClass() + " " + e.getMessage());
	    }
	}
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
    
    //private
    @Test
    public void testGetNgramPredictionProbDist() {
	logger.info("\ntesting getNgramPredictionProbDist()");
	Calendar start = Calendar.getInstance();
	List<String> sampleSentences = new ArrayList<>();
	int howMany = 1000;
	sampleSentences = instance.getSentences().stream().limit(howMany).collect(toList());
	sampleSentences.stream().forEach(s -> instance.getNgramPredictionProbDist(Capstone.tokenize(s, new Request("").setRemoveStopWords(false))));
	
	Calendar end = Calendar.getInstance();
	System.out.println(format.format(start.getTime()));
	System.out.println(format.format(end.getTime()));
	logger.info(end.getTimeInMillis() - start.getTimeInMillis());
    }
    
    @Test
    public void testGetWordAssociationScores() {
	logger.info("\ntesting getWordAssociationScores()");
	Calendar start = Calendar.getInstance();
	List<String> sampleSentences = new ArrayList<>();
	int howMany = 100;
	/*//Integer[] indeces = Random.uniformInts(howMany, 0, instance.getSentences().size() - 1);
	for(int i = 0; i < howMany; i++) {
	    sampleSentences.add(instance.getSentences().get(i));
	}
	IntStream.rangeClosed(0, howMany - 1).forEach(i -> instance.getWordAssociationScores(Capstone.tokenize(sampleSentences.get(i), new Request("").setRemoveStopWords(false))));*/
	sampleSentences = instance.getSentences().stream().limit(howMany).collect(toList());
	sampleSentences.stream().forEach(s -> instance.getWordAssociationScores(Capstone.tokenize(s, new Request("").setRemoveStopWords(false))));
	//instance.getWordAssociationScores(instance.getSentences());
	Calendar end = Calendar.getInstance();
	System.out.println(format.format(start.getTime()));
	System.out.println(format.format(end.getTime()));
	logger.info(end.getTimeInMillis() - start.getTimeInMillis());
    }
    
    @Test
    public void testCombineProbDist() {
	logger.info("\ntesting combineProbDist()");
	Calendar start = Calendar.getInstance();
	int howMany = 1;
	List<String> sampleSentences = instance.getSentences().stream().limit(howMany).collect(toList());
	List<ProbDist<String>> dists = new ArrayList<>();
	for(int i = 0; i < howMany; i++) {
	    List<String> sentence = Capstone.tokenize(sampleSentences.get(i), new Request("").setRemoveStopWords(false));
	    logger.debug(i + " " + sentence);
	    //String ngram = instance.constructNgram(sentence, 2);
	    ProbDist<String> ng = instance.getNgramPredictionProbDist(sentence);
	    ProbDist<String> wa = instance.getWordAssociationScores(sentence);
	    dists = new ArrayList<>();
	    dists.add(ng);
	    logger.debug(ng);
	    logger.debug(ng.getValues().size());
	    dists.add(wa);
	    //logger.debug(wa);
	    logger.debug(wa.getValues().size());
	    //dists.add(instance.getWordProbDist());
	    //logger.debug(instance.getWordProbDist());
	    logger.debug(instance.getWordProbDist().getValues().size());
	    long totalDifference = 0l;
	    for(int j = 0; j < 10; j++) {
		Date before = new Date();
		logger.info("before: " + format.format(before));
		instance.combineProbDists(dists, instance.getGenome());
		Date after = new Date();
		logger.info("after: " + format.format(after));
		long difference = after.getTime() - before.getTime();
		totalDifference += difference;
		logger.info(difference + " millis");
	    }
	    logger.info("average difference:  " + totalDifference / 10);
	}
	Calendar end = Calendar.getInstance();
	logger.info(format.format(start.getTime()));
	logger.info(format.format(end.getTime()));
	logger.info(end.getTimeInMillis() - start.getTimeInMillis());
    }
    
    @Test
    public void testMakeOnePrediction() {
	logger.info("\ntesting makeOnePrediction()");
	Calendar start = Calendar.getInstance();
	try {
	    //logger.debug(Random.sample(instance.getSentences(), 1, true).getClass());
	    List<String> sampleSentences = Random.sample(instance.getSentences(), 1, true);
	    sampleSentences.stream().forEach(s -> logger.debug(this.makeOnePredictionOneSentence(s)));
	} catch(Exception e) {
	    logger.error(e.getClass() + " " + e.getMessage());
	}
	Calendar end = Calendar.getInstance();
	logger.info(format.format(start.getTime()));
	logger.info(format.format(end.getTime()));
	logger.info(end.getTimeInMillis() - start.getTimeInMillis());
    }
	
    private int makeOnePredictionOneSentence(String sentence) {
	logger.debug(sentence);
	int numCorrect = 0;
	List<String> words = Capstone.tokenize(sentence, new Request("").setRemoveStopWords(false));
	String lastWord = words.get(words.size() - 1);
	logger.debug("word to predict is " + lastWord + "\n");
	String current = null;
	for(int i = 0; i < 50; i++) {
	    current = instance.makeOnePrediction(sentence);
	    //logger.debug(current + " " + ListArrayUtil.arrayToString(instance.getGenome()));
	    //numCorrect = lastWord.equals(current) ? numCorrect++ : numCorrect;
	    if(lastWord.equals(current)) {
		numCorrect++;
	    }
	    instance.mutateGenome();
	    //instance.getGenome()[2] = 0.0;
	}
	return numCorrect;
    }
}
