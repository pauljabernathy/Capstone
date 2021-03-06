/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capstone;

import capstone.*;
import static capstone.Capstone.DEFAULT_BREAKS_BETWEEN_WORDS;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
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
import java.util.regex.*;

/**
 *
 * @author pabernathy
 */
public class CapstoneApp {
    
    private static Capstone instance;
    private static Logger logger;
    
    public CapstoneApp() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        //instance = new Capstone();
        logger = ListArrayUtil.getLogger(CapstoneApp.class, Level.INFO);
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
            sentences.stream().limit(20).forEach(System.out::println);
            assertEquals(8, sentences.size());
            
            filename = "word_pair_test2.txt";
            sentences = Capstone.readSentencesFromFile(filename);
            sentences.stream().limit(20).forEach(System.out::println);
            assertEquals(3, sentences.size());
	    
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
        } catch(IOException e) {
            System.err.println(e.getClass() + " " + e.getMessage());
        }
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
    
    private void testRegularSplitting() {
        doOneSplit("one", " ");
        doOneSplit("one", "");
        doOneSplit("one two", " ");
        doOneSplit("one two", "");
    }
    
    private void doOneSplit(String text, String sep) {
        String[] words = text.split(sep);
        System.out.println("\n_" + text + "_   _" + sep + "_");
        System.out.println(words.length);
        Arrays.asList(words).stream().forEach(System.out::println);
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
    
    //@Test
    public void testGetWordPairSepHistogramsFromLargeFile() {
        logger.info("\ntesting getWordPairSepHistogramsFromFile(), from one of the larger data files");
        Map<Integer, TreeHistogram<WordPairSeparation>> result = null;
        String filename = "blogsSample1.txt";
        try {
            result = Capstone.getWordPairSepHistogramsFromFile(filename);
            logger.info(result.size());
            logger.info(result.get(1).getTotalCount());
            logger.info(result.get(2).getTotalCount());
            logger.info(result.get(3).getTotalCount());
            logger.info(result.get(4).getTotalCount());
            logger.info(result.get(5).getTotalCount());
        } catch(IOException e) {
            fail(e.getClass() + " trying to read " + filename + ":  " + e.getMessage());
        }
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
    
    @Test
    public void testampleLinesFromFile() {
        logger.info("\ntesting sampleLinesFromFile()");
        //instance.sampleLinesFromFile("C:/Users/paul/Coursera/datascience/capstone/en_US.blogs.txt", "blogsSample1.txt", .001);
        //instance.sampleLinesFromFile("/Users/pabernathy/coursera/datascience/final/en_US/en_US.news.txt", "/Users/pabernathy/coursera/datascience/final/en_US/newsSample1.txt", .001);
        //instance.sampleLinesFromFile("C:/Users/paul/Coursera/datascience/capstone/en_US.twitter.txt", "twitterSample5.txt", .75);
        //instance.sampleLinesFromFile("/Users/pabernathy/coursera/datascience/final/en_US/en_US.twitter.txt", "/Users/pabernathy/coursera/datascience/final/en_US/twitterSample2.txt", .0001);
        
	Capstone.sampleLinesFromFile("C:/Users/paul/Coursera/datascience/capstone/en_US.blogs.txt", "blogsSample_1percent.txt", .01);
	Capstone.sampleLinesFromFile("C:/Users/paul/Coursera/datascience/capstone/en_US.blogs.txt", "blogsSample_5percent.txt", .05);
        //Capstone.sampleLinesFromFile("C:/Users/paul/Coursera/datascience/capstone/en_US.blogs.txt", "blogsSample1.txt", .1);
        /*Capstone.sampleLinesFromFile("C:/Users/paul/Coursera/datascience/capstone/en_US.blogs.txt", "blogsSample2.txt", .33);
        Capstone.sampleLinesFromFile("C:/Users/paul/Coursera/datascience/capstone/en_US.blogs.txt", "blogsSample3.txt", .5);
        Capstone.sampleLinesFromFile("C:/Users/paul/Coursera/datascience/capstone/en_US.blogs.txt", "blogsSample4.txt", .66);
        Capstone.sampleLinesFromFile("C:/Users/paul/Coursera/datascience/capstone/en_US.blogs.txt", "blogsSample5.txt", .75);
        
        Capstone.sampleLinesFromFile("C:/Users/paul/Coursera/datascience/capstone/en_US.news.txt", "newsSample1.txt", .1);
        Capstone.sampleLinesFromFile("C:/Users/paul/Coursera/datascience/capstone/en_US.news.txt", "newsSample2.txt", .33);
        Capstone.sampleLinesFromFile("C:/Users/paul/Coursera/datascience/capstone/en_US.news.txt", "newsSample3.txt", .5);
        Capstone.sampleLinesFromFile("C:/Users/paul/Coursera/datascience/capstone/en_US.news.txt", "newsSample4.txt", .66);
        Capstone.sampleLinesFromFile("C:/Users/paul/Coursera/datascience/capstone/en_US.news.txt", "newsSample5.txt", .75);*/
    }
    
    /**
     * Test of findLongestLine method, of class Capstone.
     */
    //@Test
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
        String filename = "/Users/pabernathy/coursera/datascience/final/en_US/twitterSample1.txt";
        String pattern = " the";
        int expResult = 895;
        int result = Capstone.findNumOccurrences(filename, pattern);
        assertEquals(expResult, result);
    }

    /**
     * Test of findLinesThatContain method, of class Capstone.
     */
    @Test
    public void testFindLinesThatContain() {
        logger.info("findLinesThatContain");
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
        
        filename = "blogsSample5_5grams.txt";
        filename = "newsSample5_5grams.txt";
        result = Capstone.findLinesThatMatch(filename, s -> s.startsWith("a case of"));
        //result = Capstone.findLinesThatMatch(filename, s -> s.startsWith("it would mean the"));
        //result = Capstone.findLinesThatMatch(filename, s -> s.startsWith("make me the"));
        //result = Capstone.findLinesThatMatch(filename, s -> s.startsWith("struggling but the"));  //NPE - empty results
        //result = Capstone.findLinesThatMatch(filename, s -> s.startsWith("romantic date at the"));
        //result = Capstone.findLinesThatMatch(filename, s -> s.startsWith("and be on my"));
        //result = Capstone.findLinesThatMatch(filename, s -> s.startsWith("it in quite some"));      //no results
        //result = Capstone.findLinesThatMatch(filename, s -> s.startsWith("eyes with his little"));
        //result = Capstone.findLinesThatMatch(filename, s -> s.startsWith("the faith during the"));      //empty results
        //result = Capstone.findLinesThatMatch(filename, s -> s.startsWith("then you must be"));
        
        result = result.stream().sorted().collect(java.util.stream.Collectors.toList());
        StringHistogram hist = new StringHistogram();
        result.stream().forEach(s -> hist.insert(s));
        logger.debug(result);
        hist.display();
    }

    /**
     * Test of wordCould method, of class Capstone.
     */
    @Test
    public void testWordCount() {
        logger.info("wordCount");
        String filename = "/Users/pabernathy/coursera/datascience/final/en_US/twitterSample1.txt";
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
	
        /*result = Capstone.readFileAsStrings("test1.txt", " ");
        assertEquals(5, result.size());
        result.stream().forEach(System.out::println);
        
        result = Capstone.readFileAsStrings("/Users/pabernathy/coursera/datascience/final/en_US/twitterSample1.txt", " ");
        System.out.println("twitterSample1.txt:  " + result.size());
        assertEquals(895, result.stream().filter(word -> word.equals("the")).count());
        assertEquals(9696, result.stream().distinct().count());
        
        System.out.println("the:  " + result.stream().filter(word -> word.equals("the")).count());
        System.out.println("The:  " + result.stream().filter(word -> word.equals("The")).count());
        System.out.println("to:  " + result.stream().filter(word -> word.equals("to")).count());
        System.out.println("I:  " + result.stream().filter(word -> word.equals("I")).count());
        System.out.println("a:  " + result.stream().filter(word -> word.equals("a")).count());
        System.out.println("for:  " + result.stream().filter(word -> word.equals("for")).count());
        
        System.out.println();
        //result = result.stream().forEach(n -> n.toLowerCase()).collect(Collectors.toList());  //forEach returns void, so you have to use map instead
        result.stream().limit(5).forEach(System.out::println);
        result = result.stream().map(n -> n.toLowerCase()).collect(Collectors.toList());
        System.out.println(result.stream().distinct().count());
        System.out.println("the:  " + result.stream().filter(word -> word.equals("the")).count());
        System.out.println("The:  " + result.stream().filter(word -> word.equals("The")).count());
        System.out.println("to:  " + result.stream().filter(word -> word.equals("to")).count());
        System.out.println("i:  " + result.stream().filter(word -> word.equals("i")).count());
        System.out.println("I:  " + result.stream().filter(word -> word.equals("I")).count());
        System.out.println("a:  " + result.stream().filter(word -> word.equals("a")).count());
        System.out.println("for:  " + result.stream().filter(word -> word.equals("for")).count());*/
        
        /*result = Capstone.readFileAsStrings("/Users/pabernathy/coursera/datascience/final/en_US/en_US.blogs.txt", " ");
        System.out.println("en_US.blogs.txt:  " + result.size());
        result = Capstone.readFileAsStrings("/Users/pabernathy/coursera/datascience/final/en_US/en_US.news.txt", " ");
        System.out.println("en_US.news.txt:  " + result.size());
        result = Capstone.readFileAsStrings("/Users/pabernathy/coursera/datascience/final/en_US/en_US.twitter.txt", " ");
        System.out.println("en_US.twitter.txt:  " + result.size());/**/
        
        /**result = Capstone.readFileAsStrings("/Users/pabernathy/twitterSample2.txt", " ");
        System.out.println("the:  " + result.stream().filter(word -> word.equals("the")).count());
        System.out.println("The:  " + result.stream().filter(word -> word.equals("The")).count());
        System.out.println("to:  " + result.stream().filter(word -> word.equals("to")).count());
        System.out.println("i:  " + result.stream().filter(word -> word.equals("i")).count());
        System.out.println("I:  " + result.stream().filter(word -> word.equals("I")).count());
        System.out.println("a:  " + result.stream().filter(word -> word.equals("a")).count());
        System.out.println("for:  " + result.stream().filter(word -> word.equals("for")).count());
        
        System.out.println();
        result = result.stream().map(n -> n.toLowerCase()).collect(Collectors.toList());/**/
        /*System.out.println("the:  " + result.stream().filter(word -> word.equals("the")).count());
        System.out.println("The:  " + result.stream().filter(word -> word.equals("The")).count());
        System.out.println("to:  " + result.stream().filter(word -> word.equals("to")).count());
        System.out.println("i:  " + result.stream().filter(word -> word.equals("i")).count());
        System.out.println("I:  " + result.stream().filter(word -> word.equals("I")).count());
        System.out.println("a:  " + result.stream().filter(word -> word.equals("a")).count());
        System.out.println("for:  " + result.stream().filter(word -> word.equals("for")).count());*/
        /*System.out.println("the:  " + result.stream().filter(word -> word.equals("the")).count());
        System.out.println(showWordTotal(result, "the"));
        System.out.println("The:  " + result.stream().filter(word -> word.equals("The")).count());
        System.out.println(showWordTotal(result, "The"));
        System.out.println("to:  " + result.stream().filter(word -> word.equals("to")).count());
        System.out.println(showWordTotal(result, "to"));
        System.out.println("i:  " + result.stream().filter(word -> word.equals("i")).count());
        System.out.println(showWordTotal(result, "i"));
        System.out.println("I:  " + result.stream().filter(word -> word.equals("I")).count());
        System.out.println(showWordTotal(result, "I"));
        System.out.println("a:  " + result.stream().filter(word -> word.equals("a")).count());
        System.out.println(showWordTotal(result, "a"));
        System.out.println("for:  " + result.stream().filter(word -> word.equals("for")).count());
        System.out.println(showWordTotal(result, "for"));*/
        
        /*System.out.println("the:  " + result.stream().filter(word -> word.equals("the")).count());
        System.out.println("to:  " + result.stream().filter(word -> word.equals("to")).count());
        System.out.println("i:  " + result.stream().filter(word -> word.equals("i")).count());
        System.out.println("I:  " + result.stream().filter(word -> word.equals("you")).count());
        System.out.println("a:  " + result.stream().filter(word -> word.equals("a")).count());
        System.out.println("for:  " + result.stream().filter(word -> word.equals("in")).count());
        System.out.println("The:  " + result.stream().filter(word -> word.equals("for")).count());
        System.out.println("to:  " + result.stream().filter(word -> word.equals("of")).count());
        System.out.println("i:  " + result.stream().filter(word -> word.equals("and")).count());
        System.out.println("I:  " + result.stream().filter(word -> word.equals("is")).count());
        System.out.println("a:  " + result.stream().filter(word -> word.equals(" ")).count());
        System.out.println("for:  " + result.stream().filter(word -> word.equals("that")).count());*/
        
        /**System.out.println(showWordTotal(result, "the"));
        System.out.println(showWordTotal(result, "to"));
        System.out.println(showWordTotal(result, "i"));
        System.out.println(showWordTotal(result, "you"));
        System.out.println(showWordTotal(result, "a"));
        System.out.println(showWordTotal(result, "in"));
        System.out.println(showWordTotal(result, "for"));
        System.out.println(showWordTotal(result, "of"));
        System.out.println(showWordTotal(result, "and"));
        System.out.println(showWordTotal(result, "is"));
        System.out.println(showWordTotal(result, " "));
        System.out.println(showWordTotal(result, "...in"));
        System.out.println(showWordTotal(result, "in."));
        System.out.println(result.stream().distinct().count());/**/
    }
    
    public String showWordTotal(List<String> result, String word) {
        return new StringBuilder().append(word).append(":  ").append(result.stream().filter(n -> n.equals(word)).count()).toString();
    }
    
    @Test
    public void testFileSummaryHistogram() {
        logger.info("\nfileSummaryHistogram");
        Histogram result = null;
        result = Capstone.fileSummaryHistogram("/Users/pabernathy/coursera/datascience/final/en_US/en_US.blogs.txt", " ");
        System.out.println(result.getLabel());
        System.out.println(result);
        
        result = Capstone.fileSummaryHistogram("/Users/pabernathy/coursera/datascience/final/en_US/en_US.news.txt", " ");
        System.out.println(result.getLabel());
        System.out.println(result);
        
        result = Capstone.fileSummaryHistogram("/Users/pabernathy/coursera/datascience/final/en_US/en_US.twitter.txt", " ");
        System.out.println(result.getLabel());
        System.out.println(result);
        
        result = Capstone.fileSummaryHistogram("/Users/pabernathy/coursera/datascience/final/en_US/twitterSample1.txt", " ");
        System.out.println(result.getLabel());
        System.out.println(result);
    }
    
    @Test
    public void testFileSummary() {
        logger.info("\nfileSummary");
        List<String> result = null;
        /**result = Capstone.fileSummary("/Users/pabernathy/coursera/datascience/final/en_US/en_US.blogs.txt", " ");
        System.out.println("en_US.blogs.txt:  " + result);
        
        result = Capstone.fileSummary("/Users/pabernathy/coursera/datascience/final/en_US/blogsSample1.txt", " ");
        System.out.println("blogsSample1.txt:  " + result);
        
        result = Capstone.fileSummary("/Users/pabernathy/coursera/datascience/final/en_US/en_US.news.txt", " ");
        System.out.println("en_US.news.txt:  " + result);
        
        result = Capstone.fileSummary("/Users/pabernathy/coursera/datascience/final/en_US/newsSample1.txt", " ");
        System.out.println("newsSample1.txt:  " + result);
        
        result = Capstone.fileSummary("/Users/pabernathy/coursera/datascience/final/en_US/en_US.twitter.txt", " ");
        System.out.println("en_US.twitter.txt:  " + result);
        /**/
        //result = Capstone.fileSummary("/Users/pabernathy/coursera/datascience/final/en_US/twitterSample1.txt", " ");
        //System.out.println("twitterSample1.txt:  " + result);
        result = Capstone.fileSummary("/Users/pabernathy/coursera/datascience/final/en_US/twitterSample2.txt", " ");
        System.out.println("twitterSample2.txt:  " + result);
    }
    
    @Test
    public void testTreeHistogram() {
        logger.info("\ntestTreeHistogram()");
        TreeHistogram<String> hist = null;
        /*hist = Capstone.fileSummaryTreeHistogram("../Toolbox/jabberwocky.txt", " ");
        hist.getAsList(TreeHistogram.Sort.COUNT).stream().limit(10).forEach(System.out::println);
        System.out.println("\n Les Mis");
        hist = Capstone.fileSummaryTreeHistogram("../Toolbox/les_miserables.txt", " ");
        hist.getAsList(TreeHistogram.Sort.COUNT).stream().limit(10).forEach(System.out::println);*/
        
        //this.doTreeHistogram("twitterSample5.txt");
        //this.doTreeHistogram("en_US.twitter.txt");
        
        //this.doTreeHistogram("newsSample1.txt");
        //this.doTreeHistogram("newsSample2.txt");
        //this.doTreeHistogram("newsSample3.txt");
        //this.doTreeHistogram("newsSample4.txt");
        //this.doTreeHistogram("newsSample5.txt");
        
        /*this.doTreeHistogram("blogsSample1.txt");
        this.doTreeHistogram("blogsSample2.txt");
        this.doTreeHistogram("blogsSample3.txt");
        this.doTreeHistogram("blogsSample4.txt");
        this.doTreeHistogram("blogsSample5.txt");*/
	
	this.doTreeHistogram("through_the_looking_glass.txt");
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
	System.out.println(matrix.getAllAssociationsFor("two"));
	assertEquals(2, matrix.getAllAssociationsFor("two").size());
	assertEquals(8, this.getAssociationCount(matrix, "two"));
	System.out.println(matrix.getAllAssociationsFor("three"));
	assertEquals(2, matrix.getAllAssociationsFor("three").size());
	assertEquals(9, this.getAssociationCount(matrix, "three"));
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
	
	/**/try {
	    result = Capstone.findWordMatrixFromFile(new Request("word_pair_test2.txt").setRemoveStopWords(false));
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
	
	sentences.add("one two three four five six");
	sentences.add("seven eight");
	sentences.add("one two three");
	result = Capstone.findWordMatrixFromSentenceList(sentences, new Request("").setRemoveStopWords(false));
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
