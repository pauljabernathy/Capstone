/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package capstone;

import static capstone.Capstone.DEFAULT_BREAKS_BETWEEN_WORDS;
import static capstone.Capstone.findWordMatrix;
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
    public void testAddToMap_Map_Word_Word() {
	logger.info("\ntesting addToMap(Map, String, String)");
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
	
	//currently enforcing this in add(), not addToMap()
	//result = instance.addToMap(result, "a", "a");
	//assertEquals(Integer.valueOf(0), result.get("a").get("a"));
    }
    
    @Test
    public void testAddToMap_Map_Word_Word_Count() {
	logger.info("\ntesting addToMap(Map, String, String, int)");
	WordMatrix instance = new WordMatrix();
	Map<String, Map<String, Integer>> result = null;
	result = instance.addToMap(null, "a", "b", 2);
	assertEquals(Integer.valueOf(2), result.get("a").get("b"));
	result = instance.addToMap(result, "a", "b", 1);
	assertEquals(Integer.valueOf(3), result.get("a").get("b"));
	
	result = instance.addToMap(result, "c", "d", 10);
	assertEquals(Integer.valueOf(10), result.get("c").get("d"));
	
	result = instance.addToMap(result, "a", "e", -1);
	assertEquals(null, result.get("a").get("e"));
	
	result = instance.addToMap(result, "a", "b", 27);
	assertEquals(Integer.valueOf(30), result.get("a").get("b"));
	
	result = instance.addToMap(result, "a", "b", -1);
	assertEquals(Integer.valueOf(30), result.get("a").get("b"));
	
	result = instance.addToMap(result, "a", "b", 0);
	assertEquals(Integer.valueOf(30), result.get("a").get("b"));
    }
    
    @Test
    public void testAddAndGet() {
	logger.info("\ntesting add()");
	WordMatrix instance = new WordMatrix();
	assertEquals(0, instance.get("hi", "there"));
	instance.add("hi", "there");
	assertEquals(1, instance.get("hi", "there"));
	assertEquals(1, instance.get("there", "hi"));
	instance.add("there", "hi");
	assertEquals(2, instance.get("hi", "there"));
	assertEquals(2, instance.get("there", "hi"));
	instance.add("hi", "there");
	assertEquals(3, instance.get("hi", "there"));
	assertEquals(3, instance.get("there", "hi"));
	
	assertEquals(0, instance.get("hi", "bye"));
	instance.add("hi", "bye");
	assertEquals(1, instance.get("hi", "bye"));
	assertEquals(1, instance.get("bye", "hi"));
	
	assertEquals(3, instance.get("hi", "there"));
	assertEquals(3, instance.get("there", "hi"));
	
	instance.add("hi", "hi");
	assertEquals(0, instance.get("hi", "hi"));
    }
    
    @Test
    public void testAddWithCount() {
	logger.info("\nesting add(String, String, int)");
	WordMatrix instance = new WordMatrix();
	instance.add("a", "b", 27);
	assertEquals(27, instance.get("a", "b"));
	instance.add("a", "b");
	assertEquals(28, instance.get("a", "b"));
	instance.add("a", "b", 0);
	assertEquals(28, instance.get("a", "b"));
	instance.add("a", "b", -9);
	assertEquals(28, instance.get("a", "b"));
    }
    
    //@Test
    public void testGet() {
	logger.info("\ntesting get()");
	List<String> words = java.util.Arrays.asList("I wish to preach not the doctrine of ignoble ease but the doctrine of the strenuous life".split(" "));
	words.stream().sorted().forEach(System.out::println);
    }
    
    @Test
    public void testGetAllAssociationsForSpecificWord() {
	logger.info("\ntesting getAll");
	WordMatrix instance = new WordMatrix();
	//preach the docrine of the
	instance.add("preach", "the").add("preach", "doctrine").add("preach", "of");//.add("preach", "the");
	instance.add("the", "doctrine").add("the", "of").add("the", "the"); //the last one should have no effect
	instance.add("doctrine", "of");//
	
	//the one zebra
	instance.add("the", "one").add("one", "zebra").add("the", "zebra").add("zebra", "the");
	
	List<WordPairAssociation> result = instance.getAllAssociationsFor("the");
	assertEquals(5, result.size());
	result.stream().forEach(System.out::println);
	assertTrue(result.contains(new WordPairAssociation("the", "zebra", 2)));
    }
    
    @Test
    public void testAddAllFromMap() {
	logger.info("\ntesting addAll(map)");
	WordMatrix matrix = new WordMatrix();
	matrix.add("kidney", "stone", 3);
	matrix.add("geologist", "stone", 8);
	matrix.add("kidney", "bean", 2);
	
	Map<String, Map<String, Integer>> map = new HashMap<>();
	Map<String, Integer> bean = new HashMap<>();
	bean.put("black", 3);
	bean.put("kidney", 1);
	Map<String, Integer> rock = new HashMap<>();
	rock.put("stone", 9);
	rock.put("band", 4);
	map.put("bean", bean);
	map.put("rock", rock);
	
	matrix = matrix.addAll(map);
	matrix.getAllAssociationsFor("stone").forEach(System.out::println);
	assertEquals(3, matrix.getAllAssociationsFor("stone").size());
	assertEquals(20, this.getAssociationCount(matrix, "stone"));
	
	matrix.getAllAssociationsFor("kidney").forEach(System.out::println);
	assertEquals(2, matrix.getAllAssociationsFor("kidney").size());
	assertEquals(6, this.getAssociationCount(matrix, "kidney"));
	
	/*System.out.println();
	String sentence = "one two three four";
	String[] wordsInSentence = Capstone.tokenize(sentence, DEFAULT_BREAKS_BETWEEN_WORDS);
	//System.out.println(wordsInSentence);
	for(String word : wordsInSentence) {
	    //System.out.println(word);
	}
	Capstone.findWordMatrix(wordsInSentence).getAllAssociationsFor("one").forEach(System.out::println);
	matrix = matrix.addAll(Capstone.findWordMatrix(wordsInSentence));
	//assertEquals(3, matrix.getAllAssociationsFor("one").size());
	//matrix.getAllAssociationsFor("bean").forEach(System.out::println);
	//matrix.getAllAssociationsFor("one").forEach(System.out::println);
	
	System.out.println();
	WordMatrix result = Capstone.findWordMatrix(wordsInSentence);
	result.getAllAssociationsFor("one").forEach(System.out::println);
	assertEquals(3, result.getAllAssociationsFor("one").size());*/
    }
    
    private int getAssociationCount(WordMatrix matrix, String word) {
	//matrix.getAllAssociationsFor("the").stream().map(a -> a.getCount()).reduce((a, b) -> a + b) returns an Optional, 
	//hence the get() at the end of the line below
	return matrix.getAllAssociationsFor(word).stream().map(a -> a.getCount()).reduce((a, b) -> a + b).get();
    }
    
    @Test
    public void testAddAllFromWordMatrix() {
	logger.info("\ntesting addAll(WordMatrix)");
	
	WordMatrix matrix = new WordMatrix();
	matrix.add("kidney", "stone", 3);
	matrix.add("geologist", "stone", 8);
	matrix.add("kidney", "bean", 2);
	
	WordMatrix other = new WordMatrix();
	other.add("bean", "black", 3);
	other.add("bean", "kidney", 1);
	other.add("stone", "rock", 9);
	other.add("rock", "band", 4);
	
	matrix = matrix.addAll(other);
	matrix.getAllAssociationsFor("stone").forEach(System.out::println);
	assertEquals(3, matrix.getAllAssociationsFor("stone").size());
	assertEquals(20, this.getAssociationCount(matrix, "stone"));
	
	matrix.getAllAssociationsFor("kidney").forEach(System.out::println);
	assertEquals(2, matrix.getAllAssociationsFor("kidney").size());
	assertEquals(6, this.getAssociationCount(matrix, "kidney"));
	
	System.out.println();
	String sentence = "one two three four";
	String[] wordsInSentence = Capstone.tokenize(sentence, DEFAULT_BREAKS_BETWEEN_WORDS);
	//System.out.println(wordsInSentence);
	for(String word : wordsInSentence) {
	    //System.out.println(word);
	}
	Capstone.findWordMatrix(wordsInSentence).getAllAssociationsFor("one").forEach(System.out::println);
	matrix = matrix.addAll(Capstone.findWordMatrix(wordsInSentence));
	//assertEquals(3, matrix.getAllAssociationsFor("one").size());
	//matrix.getAllAssociationsFor("bean").forEach(System.out::println);
	//matrix.getAllAssociationsFor("one").forEach(System.out::println);
	
	System.out.println();
	WordMatrix result = Capstone.findWordMatrix(wordsInSentence);
	result.getAllAssociationsFor("one").forEach(System.out::println);
	assertEquals(3, result.getAllAssociationsFor("one").size());
	
	other = null;
	matrix = matrix.addAll(other);
	assertEquals(3, result.getAllAssociationsFor("one").size());
    }
}
