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
import java.util.HashMap;

/**
 *
 * @author pabernathy
 */
public class MapReduceSimTest {
    
    public MapReduceSimTest() {
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

    /**
     * Test of doSim method, of class MapReduceSim.
     */
    @Test
    public void testDoSim() {
        System.out.println("\nrunning doSim");
        MapReduceSim.doSim1();
    }
    
    @Test
    public void testMixRandomly() {
        System.out.println("\ntesting mixRandomly()");
        List<String> a = new ArrayList<>();
        a.add("a"); a.add("b"); a.add("c");
        List<String> b = new ArrayList<>();
        b.add("d"); b.add("e"); b.add("f");
        try {
            List<String> result = MapReduceSim.mixRandomly(a, b);
            System.out.println(result);
        } catch(Exception e) {
            System.err.println(e.getClass() + " " + e.getMessage());
        }
    }
    
    @Test
    public void testFlatten() {
        System.out.println("\ntesting flatten");
        HashMap<String, ArrayList<String>> input = new HashMap<>();
        List<String> result = null;
        result = MapReduceSim.flatten(input, null);
        assertEquals(0, result.size());
        
        ArrayList<String> cooking = new ArrayList<>();
        cooking.add("DEF"); cooking.add("XYZ"); cooking.add("ABC");
        ArrayList<String> talking = new ArrayList<>();
        talking.add("XYZ"); talking.add("DEF"); talking.add("CNO");
        input.put("cooking", cooking);
        input.put("talking", talking);
        result = MapReduceSim.flatten(input, " ");
        assertEquals(6, result.size());
        assertEquals("cooking DEF", result.get(0));
        result.forEach(System.out::println);
    }
}
