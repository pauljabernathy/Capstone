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
import org.apache.logging.log4j.*;

/**
 *
 * @author paul
 */
public class WordPairAssociationTest {
    
    private static Logger logger;
    
    public WordPairAssociationTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
	logger = toolbox.util.ListArrayUtil.getLogger(WordPairAssociationTest.class, Level.INFO);
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
    public void testInstantiateAndEquals() {
	logger.info("\ntesting constructor");
	WordPairAssociation instance = new WordPairAssociation("absent", "minded", 2);
	assertEquals("absent", instance.getFirst());
	assertEquals("minded", instance.getSecond());
	assertEquals(2, instance.getCount());
	
	instance = new WordPairAssociation("absent", "minded");
	assertEquals("absent", instance.getFirst());
	assertEquals("minded", instance.getSecond());
	assertEquals(1, instance.getCount());
	
	WordPairAssociation instance2 = new WordPairAssociation("roman", "legion");
	assertEquals("roman", instance2.getSecond());
	assertEquals("legion", instance2.getFirst());
	assertEquals(1, instance.getCount());
	
	WordPairAssociation instance3 = new WordPairAssociation("minded", "absent");
	assertEquals("absent", instance3.getFirst());
	assertEquals("minded", instance3.getSecond());
	assertEquals(1, instance3.getCount());
	
	assertTrue(instance.equals(instance3));
	assertFalse(instance.equals(instance2));
    }   
}
