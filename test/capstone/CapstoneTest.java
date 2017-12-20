/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capstone;

import static capstone.Capstone.DEFAULT_BREAKS_BETWEEN_WORDS;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import org.apache.logging.log4j.*;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import toolbox.stats.*;
import toolbox.util.ListArrayUtil;

/**
 *
 * @author pabernathy
 */
public class CapstoneTest {
    
    private static Capstone instance;
    private static Logger logger;
    
    public CapstoneTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        //instance = new Capstone();
        logger = ListArrayUtil.getLogger(CapstoneTest.class, Level.INFO);
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

    /**
     * Test of readLinesFromFile method, of class Capstone.
     */
    @Test
    public void testReadLinesFromFile() throws Exception {
        logger.info("\ntestoing readLinesFromFile()");
        String filename = "sentenceSample1.txt";// "through_the_looking_glass.txt";
        try {
            List<String> result = Capstone.readLinesFromFile(filename);
            result = result.stream().limit(20).collect(toList());
            assertEquals(17, result.size());
            for(String s : result) {
                logger.debug(s);
            }
        } catch(IOException e) {
            System.err.println(e.getClass() + " " + e.getMessage());
        }
        
    }
    
    @Test
    public void testReadSentencesFromFile() {
        logger.info("\ntesting readSentencesFromFile()");
        List<String> result = null;
        try {
	    String filename = null;
            assertEquals(0, Capstone.readSentencesFromFile(filename).size());
            List<String> sentenceBreaks = Arrays.asList(".", "!", "?");
            filename = "through_the_looking_glass.txt";
            filename = "sentenceSample1.txt";
            List<String> sentences = Capstone.readSentencesFromFile(filename);
            //sentences.stream().limit(20).forEach(System.out::println);
            //assertEquals(8, sentences.size());
            
            filename = "word_pair_test2.txt";
            sentences = Capstone.readSentencesFromFile(filename);
            sentences.stream().limit(20).forEach(System.out::println);
            //assertEquals(3, sentences.size());
	    //System.out.println(sentences);
	    sentences.forEach(System.out::println);
	    for(int i = 0; i < sentences.size(); i++) {
		System.out.println(i + " " + sentences.get(i));
	    }
	    
	    filename = "word_pair_test1.txt";
	    sentences = Capstone.readSentencesFromFile(filename);
            sentences.stream().limit(20).forEach(System.out::println);
            assertEquals(1, sentences.size());
	    
	    filename = "sentenceSample2.txt";
	    logger.info("\n" + filename);
	    sentences = Capstone.readSentencesFromFile(filename);
            sentences.stream().limit(20).forEach(System.out::println);
	    
	    filename = "sentenceSample3.txt";
	    logger.info("\n" + filename);
	    sentences = Capstone.readSentencesFromFile(filename);
            sentences.stream().limit(20).forEach(s -> System.out.println("-" + s));
	    
	    filename = "beowulf i to xxii.txt";
	    logger.info("\n" + filename);
	    sentences = Capstone.readSentencesFromFile(filename);
            sentences.stream().limit(20).forEach(s -> System.out.println("-" + s));
        } catch(IOException e) {
            System.err.println(e.getClass() + " " + e.getMessage());
        }
    }
    
    @Test
    public void testTokenize() {
        logger.info("\ntesting split()");
        List<String> sentenceBreaks = Arrays.asList(".", "!", "?");
	Request request = new Request("").setSentenceBreaks(sentenceBreaks).setRemoveStopWords(false);
        String text = null;
        //String [] splits = null;
	List<String> splits = null;

        splits = Capstone.tokenize(text, request);
        if(splits == null) {
            fail("splits was null when it should have been empty but non null");
        }
        assertEquals(0, splits.size());

        text = "";
        splits = Capstone.tokenize(text, request);
        if(splits == null) {
            fail("splits was null when it should have been empty but non null");
        }
        Arrays.asList(splits).stream().forEach(System.out::println);
        assertEquals(0, splits.size());

        text = "And then I said no.  But why?  How dumb was that!  oh well";
        splits = Capstone.tokenize(text, request.setTokenizeOnSentenceBreaks(true));
	System.out.println(splits);
        //assertEquals(4, splits.size());
        //Arrays.asList(splits).stream().forEach(s -> s = s.replace("\t", ""));
        Arrays.asList(splits).stream().forEach(System.out::println);

        text = "There is only one sentence here so ha.";
        splits = Capstone.tokenize(text, request);
        assertEquals(1, splits.size());

        text = "There is only one sentence here so ha";
        splits = Capstone.tokenize(text, request);
        assertEquals(1, splits.size());

        request = null;
        splits = Capstone.tokenize(text, request);
        assertEquals(1, splits.size());

        request = new Request("").setSentenceBreaks(new ArrayList<>()).setRemoveStopWords(false).setTokenizeOnSentenceBreaks(true);
        splits = Capstone.tokenize(text, request);
	logger.debug(splits);
        assertEquals(1, splits.size());
        
        request.setSentenceBreaks(Capstone.DEFAULT_SENTENCE_BREAKS);
        text = "We need to handle the elipsis correctly...  Yes we do.";
        splits = Capstone.tokenize(text, request);
        Arrays.asList(splits).stream().forEach(System.out::println);
        assertEquals(2, splits.size());
	//TODO: a final determination on what to do with ... and implement it.
        
        text = "We need to handle the elipsis correctly....  Yes we do";
        splits = Capstone.tokenize(text, request);
        Arrays.asList(splits).stream().forEach(System.out::println);
        assertEquals(2, splits.size());
	
	text = "one two threefour five";
	request.setSentenceBreaks(new ArrayList<>());
	request.getSentenceBreaks().add(" ");
	request.setTokenizeOnSentenceBreaks(true);
	request.setRemoveStopWords(true);
	splits = Capstone.tokenize(text, request);
	logger.debug(toolbox.util.ListArrayUtil.listToString(splits));
	assertEquals(1, splits.size());
	splits = Capstone.tokenize(text, request.setRemoveStopWords(false));
	assertEquals(4, splits.size());
	
	text = "one two three four";
	List<String> tokens = Capstone.tokenize(text, new Request("").setRemoveStopWords(false));//Capstone.DEFAULT_BREAKS_BETWEEN_WORDS);
	logger.debug(tokens);
	assertEquals(4, tokens.size());
	
	text = "one two three four\n" +
	    "five six one.  seven eight.  one two three";
	System.out.println("\n" + text);
	tokens = Capstone.tokenize(text, new Request("").setWordBreaks(Capstone.DEFAULT_SENTENCE_BREAKS));
	System.out.println("\n" + tokens);
	//tokens.forEach(System.out::println);
    }
    
    @Test
    public void testGetWordPairSeparation() {
        logger.info("\ntesting getWordPairSeparation()");
        Optional<WordPairSeparation> result = null;// Optional.of(null);
        String text = null;
        result = Capstone.getWordPairSeparation(text, 1);
        if(result == null) {
            fail("result of getWordPairSeparation(null, 1) should not have been null");
        }
        logger.debug(result);
        
        text = "";
        result = Capstone.getWordPairSeparation(text, 1);
        if(result == null) {
            fail("result of getWordPairSeparation(<empty>, 1) should not have been null");
        }
        logger.debug(result);
        
    }
    
    @Test
    public void testGetWordPairs() {
        logger.info("\ntesting getWordPairSeparations()");
        Optional<List<WordPairSeparation>> result = null;
        result = Capstone.getWordPairSeparationss(null);
        if(result == null) {
            fail("null result for getWordPairSeparations(null)");
        }
        assertEquals(false, result.isPresent());
        
        result = Capstone.getWordPairSeparationss("");
        if(result == null) {
            fail("null result for getWordPairSeparations(\"\")");
        }
        assertEquals(false, result.isPresent());
        
        
        String text = "one two three four";
        result = Capstone.getWordPairSeparationss(text);
        assertEquals(6, result.get().size());
        result.get().stream().forEach(System.out::println);
        
        text = "one two three four five six";
        result = Capstone.getWordPairSeparationss(text);
        assertEquals(15, result.get().size());
    }
    
    @Test
    public void testGetWordPairSeparationsFromFile() {
        logger.info("\ntesting getWordPairSeparationsFromFile()");
        //Optional<List<WordPair>> result = null;
        List<WordPairSeparation> result = null;
        String filename = "word_pair_test1.txt";
        testGetWordPairSeparationsFromOneFile(filename, 6);
        result = testGetWordPairSeparationsFromOneFile("word_pair_test2.txt", 19);
        result.stream().filter(wp -> wp.ending.equals("four")).forEach(System.out::println);
        
        filename = "sentenceSample1.txt";
        try {
            result = Capstone.getWordPairSeparationsFromFile(filename);
            //result.get().stream().filter(wp -> wp.ending.equals("the")).forEach(System.out::println);
            //result.get().stream().filter(wp -> wp.preceeding.equals("the")).forEach(System.out::println);
            logger.debug(result.stream().filter(wp -> wp.ending.equals("the") && wp.separation == 6).count());
            logger.debug(result.stream().filter(wp -> wp.preceeding.equals("the") && wp.separation == 6).count());
        } catch(IOException e) {
            fail(e.getClass() + " trying to read " + filename + ":  " + e.getMessage());
        }
    }
    
    private List<WordPairSeparation> testGetWordPairSeparationsFromOneFile(String filename, int expectedCount) {
        System.out.println();
        List<WordPairSeparation> result = null;
        try {
            result = Capstone.getWordPairSeparationsFromFile(filename);
            //result.get().stream().forEach(System.out::println);
            assertEquals(expectedCount, result.size());
        } catch(IOException e) {
            fail(e.getClass() + " trying to read " + filename + ":  " + e.getMessage());
        }
        return result;
    }
    
    @Test
    public void testGetWordPairSepHistogramsFromFile() {
        logger.info("\ntesting getWordPairSepHistogramsFromFile()");
        Map<Integer, TreeHistogram<WordPairSeparation>> result = null;
        result = testGetWordPairSepHistogramsFromOneFile("word_pair_test1.txt", 3);
        TreeHistogram th = result.get(1);
        logger.debug(th.getAsList());
        assertEquals(3, th.getTotalCount());
        th = result.get(2);
        logger.debug(th.getAsList());
        assertEquals(2, th.getTotalCount());
        th = result.get(3);
        logger.debug(th.getAsList());
        assertEquals(1, th.getTotalCount());
        
        result = testGetWordPairSepHistogramsFromOneFile("word_pair_test2.txt", 5);
        logger.debug(result.get(1).getAsList(toolbox.stats.TreeHistogram.Sort.ITEM));
        logger.debug(result.get(2).getAsList(toolbox.stats.TreeHistogram.Sort.ITEM));
        logger.debug(result.get(3).getAsList(toolbox.stats.TreeHistogram.Sort.ITEM));
        logger.debug(result.get(4).getAsList(toolbox.stats.TreeHistogram.Sort.ITEM));
        logger.debug(result.get(5).getAsList(toolbox.stats.TreeHistogram.Sort.ITEM));
    }
    
    private Map<Integer, TreeHistogram<WordPairSeparation>> testGetWordPairSepHistogramsFromOneFile(String filename, int expectedCount) {
        System.out.println();
        Map<Integer, TreeHistogram<WordPairSeparation>> result = null;
        try {
            result = Capstone.getWordPairSepHistogramsFromFile(filename);
            //result.get().stream().forEach(System.out::println);
            assertEquals(expectedCount, result.size());
        } catch(IOException e) {
            fail(e.getClass() + " trying to read " + filename + ":  " + e.getMessage());
        }
        return result;
    }
    
    //@Test
    public void testampleLinesFromFile() {
        logger.info("\ntesting sampleLinesFromFile()");
        //TODO: fill in
    }
    
    /**
     * Test of findLongestLine method, of class Capstone.
     */
    //@Test
    //TODO
    public void testFindLongestLine() {
        logger.info("findLongestLine");
        String filename = "";
        String expResult = "";
        String result = Capstone.findLongestLine(filename);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findNumOccurrences method, of class Capstone.
     */
    @Test
    public void testFindNumOccurrences() {
        logger.info("findNumOccurrences");
        String filename = "word_pair_test2.txt";
        String pattern = "one";
        int expResult = 2;
        int result = Capstone.findNumOccurrences(filename, pattern);
        assertEquals(expResult, result);
	//TODO: determine why findNumOccurrences found ~300 less occurrences of "the" in beowolf than the file summary method
    }

    /**
     * Test of findLinesThatContain method, of class Capstone.
     */
    //@Test
    public void testFindLinesThatContain() {
        logger.info("findLinesThatContain");
	//TODO: a small file
        String filename = "blogsSample2_4grams.txt";
        String pattern = "a case of";
        List<String> expResult = null;
        List<String> result = Capstone.findLinesThatContain(filename, pattern);
        result = result.stream().sorted().collect(java.util.stream.Collectors.toList());
        logger.info(result);
    }
    
    /**
     * Test of findLinesThatMatch method, of class Capstone.
     */
    @Test
    public void testFindLinesThatMatch() {
        logger.info("findLinesThatMatch");
        String filename = "histTest2.txt";
        String pattern = "a case of";
        List<String> result = Capstone.findLinesThatMatch(filename, s -> s.contains(pattern));
        result = result.stream().sorted().collect(java.util.stream.Collectors.toList());
        logger.debug(result);
        
        result = Capstone.findLinesThatMatch(filename, s -> s.contains("lumber"));
        result = result.stream().sorted().collect(java.util.stream.Collectors.toList());
        logger.debug(result);
        
        result = Capstone.findLinesThatMatch(filename, s -> s.contains("a case of beer"));
        result = result.stream().sorted().collect(java.util.stream.Collectors.toList());
        logger.debug(result);
        
        result = result.stream().sorted().collect(java.util.stream.Collectors.toList());
        StringHistogram hist = new StringHistogram();
        result.stream().forEach(s -> hist.insert(s));
        logger.debug(result);
        hist.display();
    }

    /**
     * Test of wordCould method, of class Capstone.
     */
    //@Test
    //TODO: unit test with a small file
    public void testWordCount() {
        logger.info("wordCount");
        String filename = "twitterSample1.txt";
        Histogram expResult = null;
        Histogram result = Capstone.wordCount(filename);
        if(result == null) {
            fail("result was null");
        }
        assertEquals(9696, result.size());
        logger.info(result);  //getProbDist().toString());
    }
    
    //TODO:  Do not use unit test files for actual processing, especially something that takes a long time!
    @Test
    public void testReadFileAsStrings() {
        logger.info("\ntesting readFileAsStrings()");
        List<String> result = null;
        //assertEquals(0, Capstone.readFileAsStrings(null, null).size());
	Map<String, String> replacements = new HashMap<>();
	replacements.put(":", "XYZ");
	result = Capstone.readFileAsStrings("replacementTestFile1.txt", " ", replacements);
	result.forEach(System.out::println);
	assertTrue(result.contains("thisXYZ"));
	assertFalse(result.contains(":"));
	assertTrue(result.contains(","));
	assertTrue(result.contains("clear,\""));
	
	result = Capstone.readFileAsStrings("replacementTestFile1.txt", " ");
	assertFalse(result.contains("thisXYZ"));
	assertFalse(result.contains(":"));
	assertFalse(result.contains(","));
	assertFalse(result.contains("clear,\""));
	
    }
    
    public String showWordTotal(List<String> result, String word) {
        return new StringBuilder().append(word).append(":  ").append(result.stream().filter(n -> n.equals(word)).count()).toString();
    }
    
    //@Test
    public void testFileSummaryHistogram() {
        logger.info("\nfileSummaryHistogram");
        Histogram result = null;
        result = null;
	//TODO: a test file
    }
    
    //@Test
    public void testFileSummary() {
        logger.info("\nfileSummary");
        //TODO
    }
    
    @Test
    public void testTreeHistogram() {
        logger.info("\ntestTreeHistogram()");
        TreeHistogram<String> hist = null;
        /*hist = Capstone.fileSummaryTreeHistogram("../Toolbox/jabberwocky.txt", " ");
        hist.getAsList(TreeHistogram.Sort.COUNT).stream().limit(10).forEach(System.out::println);
        */
	//TODO
	hist = Capstone.fileSummaryTreeHistogram(new Request("word_pair_test2.txt").setRemoveStopWords(false));
	//logger.debug(hist.getAsList());
	hist.getAsList().forEach(System.out::println);
	assertEquals(3, hist.get("one").get().count);
	assertEquals(2, hist.get("two").get().count);
	assertEquals(2, hist.get("three").get().count);
	assertEquals(1, hist.get("four").get().count);
	assertEquals(1, hist.get("five").get().count);
	assertEquals(1, hist.get("six").get().count);
	assertEquals(1, hist.get("seven").get().count);
	assertEquals(1, hist.get("eight").get().count);
    }
    
    private void doTreeHistogram(String filename) {
        logger.info("\ndoing TreeHistogram for " + filename);
        System.out.flush();
        long startTime = Calendar.getInstance().getTimeInMillis();
        TreeHistogram<String> hist = Capstone.fileSummaryTreeHistogram(new Request(filename));
        List<HistogramEntry<String>> list = hist.getAsList(TreeHistogram.Sort.COUNT);
        long stopTime = Calendar.getInstance().getTimeInMillis();
        System.out.println("total words:  " + hist.getTotalCount());
        System.out.println("unique words:  " + list.size());
        System.out.println("time taken:  " + ((double)stopTime - (double)startTime)/1000.0);
        list.stream().limit(100).forEach(System.out::println);
        System.out.flush();
    }
    
    @Test
    public void testFindWordMatrixFromArray() {
	logger.info("\ntesting getWordMatrix(String[] array)");
	String[] input = null;
	WordMatrix result = null;
	result = Capstone.findWordMatrix(input);
	assertEquals(0, result.getAllAssociationsFor("a").size());
	
	input = new String[] { };
	result = Capstone.findWordMatrix(input);
	assertEquals(0, result.getAllAssociationsFor("a").size());
	
	List<String> breaks = new ArrayList<>();
	breaks.add(" ");
	input = "preach the docrine of the".split(" ");//Capstone.tokenize("preach the docrine of the", breaks);//the one zebra
	result = Capstone.findWordMatrix(input);
	assertEquals(3, result.getAllAssociationsFor("the").size());
	assertEquals(6, this.getAssociationCount(result, "the"));
	assertEquals(3, result.getAllAssociationsFor("preach").size());
	assertEquals(4, this.getAssociationCount(result, "preach"));
	
	result = Capstone.findWordMatrix(input, true);
	assertEquals(3, result.getAllAssociationsFor("the").size());
	assertEquals(3, this.getAssociationCount(result, "the"));
	assertEquals(3, result.getAllAssociationsFor("preach").size());
	assertEquals(3, this.getAssociationCount(result, "preach"));
	
	String sentence = "one two three four";
	String[] wordsInSentence = "one two three four".split(" ");//Capstone.tokenize(sentence, DEFAULT_BREAKS_BETWEEN_WORDS);
	System.out.println(wordsInSentence);
	for(String word : wordsInSentence) {
	    System.out.println(word);
	}
	result = Capstone.findWordMatrix(wordsInSentence);
	result.getAllAssociationsFor("one").forEach(System.out::println);
	assertEquals(3, result.getAllAssociationsFor("one").size());
	assertEquals(3, result.getAllAssociationsFor("two").size());
	assertEquals(3, result.getAllAssociationsFor("three").size());
	assertEquals(3, result.getAllAssociationsFor("four").size());
	assertEquals(0, result.getAllAssociationsFor("five").size());
    }
    
    @Test
    public void testFindWordMatrixFromList() {
	logger.info("\ntesting findWordMatrix(List<String>)");
	List<String> words = new ArrayList<>();
	words.add("one");
	words.add("two");
	words.add("two");
	words.add("three");
	words.add("three");
	words.add("three");
	WordMatrix matrix = Capstone.findWordMatrix(words);
	assertEquals(2, matrix.getAllAssociationsFor("one").size());
	assertEquals(5, this.getAssociationCount(matrix, "one"));
	assertEquals(2, matrix.getAllAssociationsFor("two").size());
	assertEquals(8, this.getAssociationCount(matrix, "two"));
	assertEquals(2, matrix.getAllAssociationsFor("three").size());
	assertEquals(9, this.getAssociationCount(matrix, "three"));
	
	//now with just one association per sentence
	matrix = Capstone.findWordMatrix(words, new Request("").setBinaryAssociationsOnly(true));
	assertEquals(2, matrix.getAllAssociationsFor("one").size());
	assertEquals(2, this.getAssociationCount(matrix, "one"));
	assertEquals(2, matrix.getAllAssociationsFor("two").size());
	assertEquals(2, this.getAssociationCount(matrix, "two"));
	assertEquals(2, matrix.getAllAssociationsFor("three").size());
	assertEquals(2, this.getAssociationCount(matrix, "three"));
    }
    
    private int getAssociationCount(WordMatrix matrix, String word) {
	//matrix.getAllAssociationsFor("the").stream().map(a -> a.getCount()).reduce((a, b) -> a + b) returns an Optional, 
	//hence the get() at the end of the line below
	if(matrix.getAllAssociationsFor(word).isEmpty()) {
	    return 0;
	}
	return matrix.getAllAssociationsFor(word).stream().map(a -> a.getCount()).reduce((a, b) -> a + b).get();
    }
    
    @Test
    public void testFindWordMatrixFromFile() {
	logger.info("\ntesting getWordMatrixFromFile()");
	WordMatrix result = null;
	try {
	    result = Capstone.findWordMatrixFromFile(null);
	    //fail("did not throw exception for null filename");
	} catch(IOException e) {
	    assertEquals(e.getClass(), java.io.IOException.class);
	}
	
	try {
	    result = Capstone.findWordMatrixFromFile(new Request(""));
	} catch(IOException e) {
	    assertEquals(e.getClass(), java.io.IOException.class);
	}
	
	try {
	    result = Capstone.findWordMatrixFromFile(new Request("nonExistentFile"));
	    fail("did not throw exception for null filename");
	} catch(IOException e) {
	    assertEquals(e.getClass(), java.io.FileNotFoundException.class);
	}
	
	/**/try {
	    result = Capstone.findWordMatrixFromFile(new Request("word_pair_test1.txt").setRemoveStopWords(false));
	    System.out.println(result.getAllAssociationsFor("one"));
	    System.out.println(result.getAllAssociationsFor("two"));
	    System.out.println(result.getAllAssociationsFor("three"));
	    System.out.println(result.getAllAssociationsFor("four"));
	    System.out.println(result.getAllAssociationsFor("five"));
	    assertEquals(3, result.getAllAssociationsFor("one").size());
	    assertEquals(3, this.getAssociationCount(result, "one"));
	    assertEquals(3, result.getAllAssociationsFor("two").size());
	    assertEquals(3, this.getAssociationCount(result, "two"));
	    assertEquals(3, result.getAllAssociationsFor("three").size());
	    assertEquals(3, this.getAssociationCount(result, "three"));
	    assertEquals(3, result.getAllAssociationsFor("four").size());
	    assertEquals(3, this.getAssociationCount(result, "four"));
	    assertEquals(0, result.getAllAssociationsFor("five").size());
	    assertEquals(0, this.getAssociationCount(result, "five"));
	} catch(IOException e) {
	    fail(e.getClass() + " trying to get WordMatrix for word_pair_test_1.txt");
	}/**/
	
	/**try {
	    result = Capstone.findWordMatrixFromFile(new Request("word_pair_test2.txt").setRemoveStopWords(false));
	    result.getAllAssociationsFor("one").forEach(System.out::println);
	    assertEquals(5, result.getAllAssociationsFor("one").size());
	    assertEquals(12, this.getAssociationCount(result, "one"));
	    
	    //result.getAllAssociationsFor("three").forEach(System.out::println);
	    assertEquals(5, result.getAllAssociationsFor("three").size());
	    assertEquals(8, this.getAssociationCount(result, "three"));
	    
	    //result.getAllAssociationsFor("five").forEach(System.out::println);
	    assertEquals(5, result.getAllAssociationsFor("five").size());
	    assertEquals(6, this.getAssociationCount(result, "five"));
	    
	    //result.getAllAssociationsFor("seven").forEach(System.out::println);
	    assertEquals(1, result.getAllAssociationsFor("seven").size());
	    assertEquals(1, this.getAssociationCount(result, "seven"));
	    //result.getAllAssociationsFor("eight").forEach(System.out::println);
	    assertEquals(1, result.getAllAssociationsFor("eight").size());
	    assertEquals(1, this.getAssociationCount(result, "eight"));
	} catch(IOException e) {
	    fail(e.getClass() + " trying to get WordMatrix for word_pair_test_2.txt");
	}/**/
	
	//now with one association per sentence
	/**try {
	    result = Capstone.findWordMatrixFromFile(new Request("word_pair_test2.txt").setRemoveStopWords(false).setBinaryAssociationsOnly(true));
	    //result.getAllAssociationsFor("one").forEach(System.out::println);
	    assertEquals(5, result.getAllAssociationsFor("one").size());
	    assertEquals(7, this.getAssociationCount(result, "one"));
	    
	    //result.getAllAssociationsFor("three").forEach(System.out::println);
	    assertEquals(5, result.getAllAssociationsFor("three").size());
	    assertEquals(7, this.getAssociationCount(result, "three"));
	    
	    //result.getAllAssociationsFor("five").forEach(System.out::println);
	    assertEquals(5, result.getAllAssociationsFor("five").size());
	    assertEquals(5, this.getAssociationCount(result, "five"));
	    
	    //result.getAllAssociationsFor("seven").forEach(System.out::println);
	    assertEquals(1, result.getAllAssociationsFor("seven").size());
	    assertEquals(1, this.getAssociationCount(result, "seven"));
	    //result.getAllAssociationsFor("eight").forEach(System.out::println);
	    assertEquals(1, result.getAllAssociationsFor("eight").size());
	    assertEquals(1, this.getAssociationCount(result, "eight"));
	} catch(IOException e) {
	    fail(e.getClass() + " trying to get WordMatrix for word_pair_test_2.txt");
	}/**/
	
	try {
	    result = Capstone.findWordMatrixFromFile(new Request("beowulf i to xxii.txt").setRemoveStopWords(true).setBinaryAssociationsOnly(true));
	    logger.debug(result.getTopAssociationsFor("beowulf", 20));
	    logger.debug(result.getTopAssociationsFor("man", 20));
	    logger.debug(result.getTopAssociationsFor("hrothgar", 20));
	    logger.debug(result.getTopAssociationsFor("he", 20));
	    logger.debug(result.getTopAssociationsFor("she", 20));
	    logger.debug(result.getTopAssociationsFor("grendel", 20));
	    logger.debug(result.getTopAssociationsFor("fear", 20));
	    logger.debug(result.getTopAssociationsFor("meade", 20));
	} catch(IOException e) {
	    fail(e.getClass() + " trying to get WordMatrix for word_pair_test_2.txt");
	}/**/
    }
    
    @Test
    public void testFindWordMatrixFromSentenceList() {
	logger.info("\ntesting findWordMatrixFromSentenceList()");
	List<String> sentences = null;
	WordMatrix result = Capstone.findWordMatrixFromSentenceList(sentences, new Request(null));
	if(result == null) {
	    fail("result should not have been null");
	}
	
	sentences = new ArrayList<>();
	result = Capstone.findWordMatrixFromSentenceList(sentences, new Request(""));
	if(result == null) {
	    fail("result should not have been null");
	}
	
	sentences.add("one two three four five six one");
	sentences.add("seven eight");
	sentences.add("one two three");
	result = Capstone.findWordMatrixFromSentenceList(sentences, new Request("").setRemoveStopWords(false));
	assertEquals(5, result.getAllAssociationsFor("one").size());
	assertEquals(12, this.getAssociationCount(result, "one"));

	//result.getAllAssociationsFor("three").forEach(System.out::println);
	assertEquals(5, result.getAllAssociationsFor("three").size());
	assertEquals(8, this.getAssociationCount(result, "three"));

	//result.getAllAssociationsFor("five").forEach(System.out::println);
	assertEquals(5, result.getAllAssociationsFor("five").size());
	assertEquals(6, this.getAssociationCount(result, "five"));

	//result.getAllAssociationsFor("seven").forEach(System.out::println);
	assertEquals(1, result.getAllAssociationsFor("seven").size());
	assertEquals(1, this.getAssociationCount(result, "seven"));
	//result.getAllAssociationsFor("eight").forEach(System.out::println);
	assertEquals(1, result.getAllAssociationsFor("eight").size());
	assertEquals(1, this.getAssociationCount(result, "eight"));
	
	//Now with one association per sentence
	result = Capstone.findWordMatrixFromSentenceList(sentences, new Request("").setRemoveStopWords(false).setBinaryAssociationsOnly(true));
	assertEquals(5, result.getAllAssociationsFor("one").size());
	assertEquals(7, this.getAssociationCount(result, "one"));

	//result.getAllAssociationsFor("three").forEach(System.out::println);
	assertEquals(5, result.getAllAssociationsFor("three").size());
	assertEquals(7, this.getAssociationCount(result, "three"));

	//result.getAllAssociationsFor("five").forEach(System.out::println);
	assertEquals(5, result.getAllAssociationsFor("five").size());
	assertEquals(5, this.getAssociationCount(result, "five"));

	//result.getAllAssociationsFor("seven").forEach(System.out::println);
	assertEquals(1, result.getAllAssociationsFor("seven").size());
	assertEquals(1, this.getAssociationCount(result, "seven"));
	//result.getAllAssociationsFor("eight").forEach(System.out::println);
	assertEquals(1, result.getAllAssociationsFor("eight").size());
	assertEquals(1, this.getAssociationCount(result, "eight"));
    }
}
