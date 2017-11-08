/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package capstone;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.logging.log4j.*;
import toolbox.stats.TreeHistogram;

import java.io.IOException;

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
	try {
	    //TODO:  Refactor Capstone to be able to make a word histogram from the sentences list, to cut down on how many times it has to read the file.
	    Request request = new Request(filename).setRemoveStopWords(true);
	    List<String> sentences = Capstone.readSentencesFromFile(filename);
	    TreeHistogram<String> ngrams = NGrams.readNGramsFromFile(filename);
	    WordMatrix matrix = Capstone.findWordMatrixFromSentenceList(sentences, request);
	    TreeHistogram<String> wordHist = Capstone.fileSummaryTreeHistogram(request.setRemoveStopWords(true));
	    wordHist.getAsList(TreeHistogram.Sort.COUNT).stream().limit(50).forEach(System.out::println);
	    instance = new MCAgent(wordHist, sentences, ngrams, matrix);
	} catch(IOException e) {
	    System.err.println(e.getClass() + " " + e.getMessage());
	}
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testDoOneRun() {
	logger.info("testing doOneRun");
	instance.doOneRun();
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
	instance.getMatrix().add(wordA, wordB);
	instance.setBeta(0.0);*/
	instance = this.insertAandBInSameSentence(instance, wordA, wordB, 1);
	/*logger.debug(instance.getMatrix().get(wordA, wordB));
	logger.debug(instance.getWordHist().queryFromFirst(w -> w.equals(wordA)).size());
	logger.debug(instance.getWordHist().queryFromFirst(w -> w.equals(wordB)).size());
	instance = this.insertAandBInSameSentence(instance, wordA, wordB, 2);*/
	
	int numAandB = instance.getMatrix().get(wordA, wordB);
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
	int numWords = instance.getWordHist().getTotalCount();
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
	int numA = instance.getWordHist().queryFromFirst(w -> w.equals(wordA)).size();
	int numB = instance.getWordHist().queryFromFirst(w -> w.equals(wordB)).size();
	if(numA != 0) {
	     numA = instance.getWordHist().queryAll(w -> w.equals(wordA)).get(0).count;
	}
	if(numB != 0) {
	    numB = instance.getWordHist().queryAll(w -> w.equals(wordB)).get(0).count;
	}
	assertTrue(numA <= n);
	assertTrue(numB <= n);
	assertEquals(numA, numB);
	
	int numAandB = instance.getMatrix().get(wordA, wordB);
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
	/*logger.debug(instance.getMatrix().get(wordA, wordB));
	logger.debug(instance.getWordHist().queryFromFirst(w -> w.equals(wordA)).size());
	logger.debug(instance.getWordHist().queryFromFirst(w -> w.equals(wordB)).size());*/
	return instance;
    }
    
    private MCAgent addOnce(MCAgent instance, String wordA, String wordB) {
	//logger.debug("addOnce");
	//logger.debug(instance.getWordHist().queryFromFirst(w -> w.equals(wordA)).size());
	//instance.getWordHist().queryFromFirst(w -> w.equals(wordB)).get(0);
	//logger.debug(instance.getWordHist().queryFromFirst(w -> w.equals(wordB)));
	/*logger.debug(instance.getWordHist().queryAll(w -> w.equals(wordA)));
	logger.debug(instance.getWordHist().queryAll(w -> w.equals(wordB)));*/
	instance.getWordHist().insert(wordA, 1);
	instance.getWordHist().insert(wordB, 1);
	//logger.debug(instance.getWordHist().queryFromFirst(w -> w.equals(wordA)).get(0));
	//logger.debug(instance.getWordHist().queryFromFirst(w -> w.equals(wordB)).size());
	/*logger.debug(instance.getWordHist().queryAll(w -> w.equals(wordA)));
	logger.debug(instance.getWordHist().queryAll(w -> w.equals(wordB)));*/
	instance.getSentences().add(wordA + " " + wordB);
	instance.getNgrams().insert(wordA + " " + wordB, 1);
	instance.getMatrix().add(wordA, wordB);
	instance.setBeta(0.0);
	return instance;
    }
}
