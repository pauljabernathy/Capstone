/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package capstone;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import toolbox.util.ListArrayUtil;
import org.apache.logging.log4j.*;
import java.util.List;

/**
 *
 * @author paul
 */
public class StopWordsTest {
    
    private static Logger logger;
    
    public StopWordsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
	logger = ListArrayUtil.getLogger(StopWordsTest.class, Level.INFO);
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
    public void testConstructor() {
	logger.info("\ntesting constructor()");
	StopWords instance = new StopWords(null); 
	instance = new StopWords("stopwords.csv");
    }

    @Test
    public void testLoadStopWords() throws Exception {
	logger.info("\ntesting loadStopWords");
	StopWords instance = new StopWords(null);
	List<String> words = null;
	assertEquals(0, instance.loadStopWords(null).size());
	assertEquals(0, instance.loadStopWords("").size());
	assertEquals(0, instance.loadStopWords("somedumbfilename").size());
	
	words = instance.loadStopWords("stopwords.csv");
	System.out.println(words.size());
	words.forEach(System.out::println);
	assertTrue(words.size() > 100);
    }

    @Test
    public void testIsStopWord() {
	logger.info("\ntesting isStopWord()");
	StopWords instance = new StopWords("stopwords.csv");
	assertTrue(instance.isStopWord("a"));
	assertTrue(instance.isStopWord("across"));
	assertTrue(instance.isStopWord("them"));
	assertTrue(instance.isStopWord("the"));
	assertTrue(instance.isStopWord("it"));
	//assertTrue(instance.isStopWord("a"));
	assertTrue(instance.isStopWord("an"));
	
	//assertFalse(instance.isStopWord(""));
	assertFalse(instance.isStopWord("ameliorate"));
	assertFalse(instance.isStopWord("desert"));
	assertFalse(instance.isStopWord("Roman"));
	assertFalse(instance.isStopWord("legion"));
	assertFalse(instance.isStopWord("cockney"));
	assertFalse(instance.isStopWord("taxicab"));
	assertFalse(instance.isStopWord("monte carlo"));
    }
    
    //@Test
    public void testRemoveStopWords() {
	logger.info("\ntesting removeStopWords()");
	StopWords instance = new StopWords();
	String line = null;
	line = instance.removeStopWords(line);
	assertNull(line);
	line = instance.removeStopWords("");
	assertTrue(line.isEmpty());
	
	line = instance.removeStopWords("nevermind that we instantiated the variable altruistically");
	assertEquals("nevermind  we instantiated  variable altruistically", line);
    }
}
