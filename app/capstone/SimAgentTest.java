/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package capstone;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.*;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author paul
 */
public class SimAgentTest {
    
    private static Logger logger;
    
    public SimAgentTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
	logger = toolbox.util.ListArrayUtil.getLogger(SimAgentTest.class, Level.DEBUG);
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
    public void testDoOneRun() {
    }

    @Test
    public void testGenerateCoefficients() {
    }

    @Test
    public void testNextGuess() {
    }
    
    @Test
    public void cleanBeowulf() {
	logger.info("\ncleanBeowulf()");
	
	try {
	    BufferedReader reader = new BufferedReader(new FileReader("beowulf i to xxii.txt"));
	    PrintWriter writer = new PrintWriter(new FileWriter("b2"));
	    Pattern p = Pattern.compile("\\d");
	    Matcher m = null;
	    String line = null;
	    while(reader.ready()) {
		line = reader.readLine();
		m = p.matcher(line);
		/*if(m.find()) {
		    logger.debug(line);
		    line = line.replaceAll("\\d", "").trim();
		    //logger.debug(line.replaceAll("\\d", ""));
		    logger.info(line);
		}*/
		line = line.replaceAll("\\d", "").trim();
		writer.println(line);
	    }
	    writer.flush();
	} catch(IOException e) {
	    logger.error(e.getClass() + " " + e.getMessage());
	}
    }
}
