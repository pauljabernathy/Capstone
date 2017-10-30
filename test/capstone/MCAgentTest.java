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
	try {
	    List<String> sentences = Capstone.readSentencesFromFile(filename);
	    TreeHistogram<String> ngrams = NGrams.readNGramsFromFile(filename);
	    instance = new MCAgent(sentences, ngrams, null);
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
    }
    
}
