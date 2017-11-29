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

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.util.Arrays;
import java.util.TreeMap;
import toolbox.util.ListArrayUtil;
import toolbox.stats.*;

/**
 *
 * @author pabernathy
 */
public class NGramsTest {
    
    private static NGrams instance;
    
    public NGramsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
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
    public void testGetNGrams_List() {
        System.out.println("\ntesting getNGrams(List<String> words, int n, String delimiter, boolean allLowerCase)");
        List<String> words = null;
        TreeMap<String, Integer> result = null;
        result = NGrams.getNGrams(words, 1, null, true);
        assertEquals(0, result.size());
        words = new ArrayList<>();
        assertEquals(0, result.size());
        
        words.add("a");
        int n = 1;
        String delimiter = " ";
        //System.out.println(ListArrayUtil.listToString(words.subList(0, 0 + n), delimiter, "", ""));
        result = NGrams.getNGrams(words, 1, " ", true);
        assertEquals(1, result.size());
        
        words.add("b");
        words.add("c");
        result = NGrams.getNGrams(words, 1, " ", true);
        assertEquals(3, result.size());
        words.add("a");
        result = NGrams.getNGrams(words, 1, " ", true);
        assertEquals(3, result.size());
        
        result = NGrams.getNGrams(words, 2, " ", true);
        System.out.println(result);
        assertEquals(3, result.size());
        
        result = NGrams.getNGrams(words, 3, " ", true);
        System.out.println(result);
        assertEquals(2, result.size());
        
        result = NGrams.getNGrams(words, 4, " ", true);
        System.out.println(result);
        assertEquals(1, result.size());
        
        result = NGrams.getNGrams(words, 5, " ", true);
        System.out.println(result);
        assertEquals(0, result.size());
    }
    
    @Test
    public void testGetNGrams() {
        System.out.println("\ntesting getNGrams()");
        try {
            
            /*assertEquals(0, NGrams.getNGrams(null, 0, null, true).size());
            assertEquals(0, NGrams.getNGrams(null, 0, null, false).size());
            assertEquals(0, NGrams.getNGrams("", 0, null, true).size());
            assertEquals(0, NGrams.getNGrams("", 0, null, false).size());
            assertEquals(0, NGrams.getNGrams(null, 0, "", true).size());
            assertEquals(0, NGrams.getNGrams(null, 0, "", false).size());
            assertEquals(0, NGrams.getNGrams("", 0, "", true).size());
            assertEquals(0, NGrams.getNGrams("", 0, "", false).size());
            
            assertEquals(0, NGrams.getNGrams(null, 1, null, true).size());
            assertEquals(0, NGrams.getNGrams(null, 1, null, false).size());
            assertEquals(0, NGrams.getNGrams("", 1, null, true).size());
            assertEquals(0, NGrams.getNGrams("", 1, null, false).size());
            assertEquals(0, NGrams.getNGrams(null, 1, "", true).size());
            assertEquals(0, NGrams.getNGrams(null, 1, "", false).size());
            assertEquals(0, NGrams.getNGrams("", 1, "", true).size());
            assertEquals(0, NGrams.getNGrams("", 1, "", false).size());*/
            
            TreeMap<String, Integer> result = null;
            
            
            result = NGrams.getNGrams("scantest1.txt", 1, " ", true);
            System.out.println(result);
            assertEquals(4, result.size());
            
            result = NGrams.getNGrams("scantest2.txt", 2, " ", true);
            System.out.println(result);
            assertEquals(5, result.size());
            
        } catch(IOException e) {
            System.err.println(e.getClass() + " trying to get the ngrams:  " + e.getMessage());
        }
                
    }
    
    @Test
    public void testGetNGramsTree() {
        System.out.println("\ntesting getNGramsTree()");
        TreeHistogram<String> result = null;
        try {
            result = NGrams.getNGramsTree("scantest1.txt", 1, " ", true);
            System.out.println(result.getAsList(TreeHistogram.Sort.COUNT));
            assertEquals(4, result.getTotalCount());

            result = NGrams.getNGramsTree("scantest2.txt", 2, " ", true);
            System.out.println(result.getAsList(TreeHistogram.Sort.COUNT));
            assertEquals(6, result.getTotalCount());
            assertEquals(5, result.getNumEntries());
            
            result = NGrams.getNGramsTree("blogsSample2.txt", 4, " ", true);
            List<HistogramEntry<String>> words = result.getAsList(TreeHistogram.Sort.COUNT);
            words.stream().limit(10).forEach(System.out::println);
            System.out.println("\nand a case of:");
            words.stream().filter(entry -> entry.toString().startsWith("a case of")).forEach(System.out::println);
            
        } catch(IOException e) {
            System.err.println(e.getClass() + " trying to get the ngrams:  " + e.getMessage());
        }
    }
    
    @Test
    public void compareMapAndTree() {
        System.out.println("\ncompareMapAndTree()");
        //compareMapAndTreeForFile("../Toolbox/jabberwocky.txt");
        //compareMapAndTreeForFile("../Toolbox/beowulf.txt");
        compareMapAndTreeForFile("../Toolbox/les_miserables.txt");
    }
    
    private void compareMapAndTreeForFile(String filename) {
        TreeHistogram<String> treeResult = null;
        TreeMap<String, Integer> mapResult = null;
        //try {
            compareMapAndTreeForFile(filename, 1);
            compareMapAndTreeForFile(filename, 2);
            compareMapAndTreeForFile(filename, 3);
            compareMapAndTreeForFile(filename, 4);
        //} catch(IOException e) {
        //    System.err.println(e.getClass() + " trying to get the ngrams:  " + e.getMessage());
        //}
    }
    
    private void compareMapAndTreeForFile(String filename, int n) {
        System.out.println("\ncomputing " + n + " grams for " + filename);
        TreeHistogram<String> treeResult = null;
        TreeMap<String, Integer> mapResult = null;
        try {
            treeResult = NGrams.getNGramsTree(filename, n, " ", true);
            mapResult = NGrams.getNGrams(filename, n, " ", true);
            System.out.println("tree:");
            System.out.println("total entries:  " + treeResult.getTotalCount());
            System.out.println("unique entries:  " + treeResult.getAsList().size());
            
            System.out.println("\nmap:");
            int total = 0;
            total = mapResult.values().stream().reduce(0, (a, b) -> a + b);// forEach(i -> total += i);
            System.out.println("total entries:  " + total);
            System.out.println("unique entries:  " + mapResult.size());
            
            assertEquals(treeResult.getNumEntries(), mapResult.size());
            treeResult.getAsList(TreeHistogram.Sort.COUNT).stream().limit(20).forEach(System.out::println);
            
        } catch(IOException e) {
            System.err.println(e.getClass() + " trying to get the ngrams:  " + e.getMessage());
        }
        
    }
    
    @Test
    public void testGetNGramsOfSentences() {
	System.out.println("\ntesting getNGramsOfSentences");
	String sentence1 = "one two three four five one two three";
	String sentence2 = "a b c d";
	String sentence3 = "one two three four five six seven eight a b";
	List<String> sentences = new ArrayList<String>();
	sentences.add(sentence1);
	sentences.add(sentence2);
	sentences.add(sentence3);
	
	TreeHistogram<String> ngrams = NGrams.getNGramsOfSentences(sentences, 3);
	List<HistogramEntry<String>> list = ngrams.getAsList(TreeHistogram.Sort.COUNT);
	list.forEach(System.out::println);
    }
    
    @Test
    public void testExtractNGrams() {
        System.out.println("\ntesting extractNGrams()");
        String filename = "scantest2.txt";
        List<String> words = Capstone.readFileAsStrings(filename, " ");
        List<String> ngrams = NGrams.extractNGrams(words, 1);
        System.out.println(ngrams);
        assertEquals(7, ngrams.size());
        
        ngrams = NGrams.extractNGrams(words, 2);
        System.out.println(ngrams);
        assertEquals(6, ngrams.size());
        
        ngrams = NGrams.extractNGrams(words, 3);
        System.out.println(ngrams);
        assertEquals(5, ngrams.size());
        
        ngrams = NGrams.extractNGrams(words, 4);
        System.out.println(ngrams);
        assertEquals(4, ngrams.size());
    }
    
    @Test
    public void testWriteNGrams() {
        try {
            List<String> words = Capstone.readFileAsStrings("newsSample5.txt", " ");
            NGrams.writeNGrams(words, 5, "newsSample5_5grams.txt");
        } catch(IOException e) {
            System.err.println(e.getClass() + " trying to write n grams:  " + e.getMessage());
        }
    }
    
    @Test
    public void testReadNGramsFromFile() {
        System.out.println("\ntesting readNGramsFromFile()");
        try {
            TreeHistogram<String> histogram = NGrams.readNGramsFromFile("histTest1.txt");
            assertEquals(6, histogram.getTotalCount());
            assertEquals(3, histogram.getNumEntries());
            //System.out.println(histogram.getAsList());
            
            /*histogram = NGrams.readNGramsFromFile("blogsSample2_2grams.txt");
            histogram.getAsList().stream().limit(20).forEach(System.out::println);
            System.out.println();
            histogram.getAsList(TreeHistogram.Sort.COUNT).stream().limit(20).forEach(System.out::println);*/
            
            histogram = NGrams.readNGramsFromFile("blogsSample2_4grams.txt");
            //histogram.getAsList().stream().limit(20).forEach(System.out::println);
            //System.out.println();
            histogram.getAsList(TreeHistogram.Sort.COUNT).stream().limit(20).forEach(System.out::println);
        } catch(IOException e) {
            fail(e.getClass() + " trying to write n grams:  " + e.getMessage());
        }
    }
    
    @Test
    public void testGetAsString() {
        
        assertEquals("a b", NGrams.getAsString(Arrays.asList(new String[] { "a", "b"}), " "));
    }
    
}
