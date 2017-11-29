/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package capstone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
public class MCAgentTest {
    
    private static Logger logger;
    private MCAgent instance;
    
    private double EPSILON = 0.0001;
    
    public MCAgentTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
	logger = toolbox.util.ListArrayUtil.getLogger(MCAgentTest.class, Level.INFO);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
	String filename = "les_miserables.txt";
	filename = "through_the_looking_glass.txt";
	filename = "beowulf i to xxii.txt";
	//TODO: use a small test file with known stats
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
	    
	    instance = new MCAgent(totalAllWordHist, sentences, ngrams, matrix);
	    instance.setTotalNonStopWordHist(totalNonStopWordHist);
	    instance.setOncePerSentenceWordHist(oncePerSentenceWordHist);
	    instance.setBinaryMatrix(binaryMatrix);
	    sentences.stream().forEach(s -> {
		if(s.contains(" win ")) {
		    System.out.println("\n-" + s);
		}
	    });
	} catch(IOException e) {
	    System.err.println(e.getClass() + " " + e.getMessage());
	}
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testDoOneRunDemo() {
	logger.info("testing doOneRunDemo");
	instance.doOneRunDemo();
    }
    
    
    @Test
    public void testDoOneRun() {
	logger.info("\ntesting doOneRun()");
    }
    
    @Test
    public void testMakeOnePrediction() {
	logger.info("\ntesting makeOnePrediction()");
    }
    
    @Test
    public void testDoNGramPrediction() {
	logger.info("\ntesting doNGramPrediction()");
	String text = "I will win Hrothgar said to the";
	text = "Healfdene the high, and long while he held it";
	text = "Mickle wrack was it soothly for the friend of the Scyldings";
	List<String> sentence = Capstone.tokenize(text, new Request("").setRemoveStopWords(false));
	String result = null;
	result = instance.doNgramPrediction(sentence);
	assertEquals("scyldings", result);
    }
    
    @Test
    public void testDoWordAssociationPrediction() {
	logger.info("\ntesting doWordAssociationPrediction()");
	List<String> sentence = Capstone.tokenize("I will win Hrothgar said to the", new Request(""));
	//sentence.forEach(logger::debug);
	assertEquals(3, sentence.size());
	assertEquals("shall", instance.doWordAssociationPrediction(sentence));
	
	//logger.debug("\nMickle wrack was it soothly for the friend of the Scyldings");
	String text = "Mickle wrack was it soothly for the friend of the";
	sentence = Capstone.tokenize(text, new Request(""));
	String prediction = instance.doWordAssociationPrediction(sentence);
	logger.debug(prediction);
    }
    
    @Test
    public void testProbAandB() {
	logger.info("\ntesting probAandB");
	
	//An easy way to test is by inserting words that presumably don't appear anywhere else in the document.
	//One scenario is to put them in the same sentence, so both only appear when the other does.
	String wordA = "wordA";
	String wordB = "wordB";
	/*instance.getWordHist().insert(wordA, 1);
	instance.getWordHist().insert(wordB, 1);
	instance.getSentences().add(wordA + " " + wordB);
	instance.getNgrams().insert(wordA + " " + wordB, 1);
	instance.getWeightedMatrix().add(wordA, wordB);
	instance.setBeta(0.0);*/
	instance = this.insertAandBInSameSentence(instance, wordA, wordB, 1);
	/*logger.debug(instance.getWeightedMatrix().get(wordA, wordB));
	logger.debug(instance.getWordHist().queryFromFirst(w -> w.equals(wordA)).size());
	logger.debug(instance.getWordHist().queryFromFirst(w -> w.equals(wordB)).size());
	instance = this.insertAandBInSameSentence(instance, wordA, wordB, 2);*/
	
	int numAandB = instance.getWeightedMatrix().get(wordA, wordB);
	int numSentences = instance.getSentences().size();
	double prob = (double)numAandB / (numSentences * (1.0 + instance.getBeta()));
	assertEquals(prob, instance.probAInSentenceWithB(wordA, wordB), EPSILON);
	instance.setBeta(1.0);
	assertEquals(prob / 2.0, instance.probAInSentenceWithB(wordA, wordB), EPSILON);
	instance.setBeta(3.0);
	assertEquals(prob / 4.0, instance.probAInSentenceWithB(wordA, wordB), EPSILON);
	instance.setBeta(10.0);
	assertEquals(prob / 11.0, instance.probAInSentenceWithB(wordA, wordB), EPSILON);
	
	//TODO: more unit testing
    }
    
    @Test
    public void testGetFractionOfWords() {
	logger.info("\ntesting getFractionOfWords()");
	String wordA = "wordA";
	String wordB = "wordB";
	instance = this.insertAandBInSameSentence(instance, wordA, wordB, 1);
	int numWords = instance.getTotalAllWordHist().getTotalCount();
	assertEquals(1.0 / (double)numWords, instance.getFractionOfWords(wordB), EPSILON);
	assertEquals(1.0 / (double)numWords, instance.getFractionOfWords(wordA), EPSILON);
	instance = this.insertAandBInSameSentence(instance, wordA, wordB, 2);
	assertEquals(2.0 / (double)numWords, instance.getFractionOfWords(wordB), EPSILON);
	assertEquals(2.0 / (double)numWords, instance.getFractionOfWords(wordA), EPSILON);
    }
    
    /**
     * Inject test words into the data by inserting wordA and wordB together into the same sentence a given number of times.
     * The words should be words that do not appear in the data already.
     * If they are already present and in the same sentence the specified number of times, return.  If they are present more than that, 
     * or present but not in the same sentence, fail on an assertion.  Otherwise, insert a dummy sentence at the end of the sentence list
     * with just those two words, and adjust the ngrams and WordMatrix.
     * You can add more, but not subtract.
     * @param instance
     * @param wordA
     * @param wordB
     * @param n
     * @return 
     */
    private MCAgent insertAandBInSameSentence(MCAgent instance, String wordA, String wordB, int n) {
	
	//It is possible they were already inserted, so check.
	int numA = instance.getTotalAllWordHist().queryFromFirst(w -> w.equals(wordA)).size();
	int numB = instance.getTotalAllWordHist().queryFromFirst(w -> w.equals(wordB)).size();
	if(numA != 0) {
	     numA = instance.getTotalAllWordHist().queryAll(w -> w.equals(wordA)).get(0).count;
	}
	if(numB != 0) {
	    numB = instance.getTotalAllWordHist().queryAll(w -> w.equals(wordB)).get(0).count;
	}
	assertTrue(numA <= n);
	assertTrue(numB <= n);
	assertEquals(numA, numB);
	
	int numAandB = instance.getWeightedMatrix().get(wordA, wordB);
	assertTrue(numAandB <= n);
	if(numAandB > 0) {
	    //They are already there.
	    //Make sure they only occur in the same sentence.
	    assertEquals(numAandB, numA);
	    assertEquals(numAandB, numB);
	}
	//At this point, all the numbers should match up.
	if(numAandB < n) {
	    //already there, but not n times yet
	    for(int i = n; i > numAandB; i--) {
		addOnce(instance, wordA, wordB);
	    }
	}
	
	/*if(numAandB == n) {
	    //they are already there
	    assertEquals(n, numA);
	    assertEquals(n, numB);
	    return instance;
	} else if(numAandB == 0) {
	    assertEquals(0, numA + numB);
	}*/
	/*logger.debug(instance.getWeightedMatrix().get(wordA, wordB));
	logger.debug(instance.getTotalAllWordHist().queryFromFirst(w -> w.equals(wordA)).size());
	logger.debug(instance.getTotalAllWordHist().queryFromFirst(w -> w.equals(wordB)).size());*/
	return instance;
    }
    
    private MCAgent addOnce(MCAgent instance, String wordA, String wordB) {
	//logger.debug("addOnce");
	//logger.debug(instance.getTotalAllWordHist().queryFromFirst(w -> w.equals(wordA)).size());
	//instance.getTotalAllWordHist().queryFromFirst(w -> w.equals(wordB)).get(0);
	//logger.debug(instance.getTotalAllWordHist().queryFromFirst(w -> w.equals(wordB)));
	/*logger.debug(instance.getTotalAllWordHist().queryAll(w -> w.equals(wordA)));
	logger.debug(instance.getTotalAllWordHist().queryAll(w -> w.equals(wordB)));*/
	instance.getTotalAllWordHist().insert(wordA, 1);
	instance.getTotalAllWordHist().insert(wordB, 1);
	instance.getOncePerSentenceWordHist().insert(wordA, 1);
	instance.getOncePerSentenceWordHist().insert(wordB, 1);
	//logger.debug(instance.getTotalAllWordHist().queryFromFirst(w -> w.equals(wordA)).get(0));
	//logger.debug(instance.getTotalAllWordHist().queryFromFirst(w -> w.equals(wordB)).size());
	/*logger.debug(instance.getTotalAllWordHist().queryAll(w -> w.equals(wordA)));
	logger.debug(instance.getTotalAllWordHist().queryAll(w -> w.equals(wordB)));*/
	instance.getSentences().add(wordA + " " + wordB);
	instance.getNgrams().insert(wordA + " " + wordB, 1);
	instance.getWeightedMatrix().add(wordA, wordB);
	instance.getBinaryMatrix().add(wordA, wordB);
	instance.setBeta(0.0);
	return instance;
    }
    
    @Test
    public void testGetFractionOfSentencesWith() {
	logger.info("\ntesting getFractionOfSentencesWith");
	String wordA = "wordA";
	String wordB = "wordB";
	instance = this.insertAandBInSameSentence(instance, wordA, wordB, 1);
	
	logger.info(instance.getSentences().size());
	logger.info(instance.getTotalAllWordHist().get(wordB).get());
	logger.info(instance.getOncePerSentenceWordHist().get(wordB).get());
	assertEquals(1.0 / 384, instance.getFractionOfSentencesWith(wordB), EPSILON);
	//will need to change the above of course when the test file changes
    }
    
    @Test
    public void testGenerateGenome() {
	logger.info("\ntesting generateGenome");
	double[] genome = instance.generateRandomGenome();
	assertEquals(instance.getGenomeLength(), genome.length);
	logger.debug(toolbox.util.ListArrayUtil.arrayToString(genome));
	logger.debug(toolbox.util.MathUtil.mean(genome));
	assertEquals(2.5, toolbox.util.MathUtil.mean(genome), 1.0);
    }
    
    @Test
    public void testConstructNGram() {
	logger.info("\ntesting constructNGram()");
	List<String> sentence = null;
	assertEquals("", instance.constructNgram(sentence, 3, 1));
	
	sentence = new ArrayList<>();
	assertEquals("", instance.constructNgram(sentence, 3, 1));
	
	sentence = Arrays.asList("something", "of", "the", "subject", "we", "are", "talking", "about");
	sentence = Capstone.tokenize("something of the subject we are talking about", new Request("").setRemoveStopWords(false));
	logger.debug(sentence);
	assertEquals("we are talking", instance.constructNgram(sentence, 3, 1));
	assertEquals("subject we are", instance.constructNgram(sentence, 3, 2));
	assertEquals("subject we are talking", instance.constructNgram(sentence, 4, 1));
	assertEquals("are talking", instance.constructNgram(sentence, 2, 1));
	assertEquals("we are talking about", instance.constructNgram(sentence, 4, 0));
	
	assertEquals("", instance.constructNgram(sentence, 3, 8));
	//assertEquals("something", instance.constructNgram(sentence, 3, 7));
	//TODO:  more rigor on when the numToLeaveOff is high; generally it will only be 1 so a low priority
	
	
	sentence = Capstone.tokenize("I will win Hrothgar said to the", new Request("").setRemoveStopWords(false));
	assertEquals("hrothgar said to", instance.constructNgram(sentence, 3));
	logger.debug(sentence);
    }
}
