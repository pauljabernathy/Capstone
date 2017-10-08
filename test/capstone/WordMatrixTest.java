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
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author paul
 */
public class WordMatrixTest {
    
    private static Logger logger;
    
    public WordMatrixTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
	logger = toolbox.util.ListArrayUtil.getLogger(WordMatrixTest.class, Level.INFO);
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
    public void testAddToMap() {
	logger.info("\ntesting addToMap()");
	WordMatrix instance = new WordMatrix();
	Map<String, Map<String, Integer>> result = null;
	result = instance.addToMap(null, "a", "b");
	assertEquals(Integer.valueOf(1), result.get("a").get("b"));
	result = instance.addToMap(result, "a", "b");
	assertEquals(Integer.valueOf(2), result.get("a").get("b"));
	
	result = instance.addToMap(result, "c", "d");
	assertEquals(Integer.valueOf(1), result.get("c").get("d"));
	
	result = instance.addToMap(result, "a", "e");
	assertEquals(Integer.valueOf(1), result.get("a").get("e"));
	
	result = instance.addToMap(result, "a", "b");
	assertEquals(Integer.valueOf(3), result.get("a").get("b"));
    }
    
    @Test
    public void testAddAndGet() {
	logger.info("\ntesting add()");
	WordMatrix instance = new WordMatrix();
	assertEquals(0, instance.get("hi", "there"));
	instance.add("hi", "there");
	assertEquals(1, instance.get("hi", "there"));
	instance.add("hi", "there");
	assertEquals(2, instance.get("hi", "there"));
	
	assertEquals(0, instance.get("hi", "bye"));
	instance.add("hi", "bye");
	assertEquals(1, instance.get("hi", "bye"));
	assertEquals(2, instance.get("hi", "there"));
	
	assertEquals(2, instance.get("there", "hi"));
    }
    
    @Test
    public void testGet() {
	logger.info("\ntesting get()");
	List<String> words = java.util.Arrays.asList("I wish to preach not the doctrine of ignoble ease but the doctrine of the strenuous life".split(" "));
	words.stream().sorted().forEach(System.out::println);
    }
    
}
