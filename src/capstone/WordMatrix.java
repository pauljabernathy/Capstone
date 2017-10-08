/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package capstone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author paul
 */
public class WordMatrix {
    
    //private ArrayList<String> ordered;
    //private ArrayList<String> reverseOrder;
    /*
    There are two different maps so that we can grad all pairs involving a given word by only getting that word's Map, 
    instead of having to search for all entries involving that word.
    I suppose another solution would be a single Map/Array of Histograms, in which every pair involves an entry for both words.
    I don't know if that would get us anything more.
    Either way, as far as I can tell, the functionality to be able to get all pairs for a given word in O(1) time involves some sort of double entry.
    */
    Map<String, Map<String, Integer>> forward;
    Map<String, Map<String, Integer>> reverse;
    
    public WordMatrix() {
	this.forward = new HashMap<String, Map<String, Integer>>();
	this.reverse = new HashMap<String, Map<String, Integer>>();
    }
    
    //TODO:  the ability to specifiy if things should be unique (don't add if it's already there)
    //or if adding the same pair again should update the cound
    public WordMatrix add(String a, String b) {
	if(a.compareTo(b) < 0) {
	    this.addToMap(forward, a, b);
	    this.addToMap(reverse, b, a);
	} else if(a.compareTo(b) > 0) {
	    this.addToMap(forward, b, a);
	    this.addToMap(reverse, a, b);
	}
	//If a and b are the same, do nothing because it should not be added.
	return this;
    }
    
    public int get(String a, String b) {
	if(a.compareTo(b) < 0) {
	    return this.getFromMap(forward, a, b);
	} else if(a.compareTo(b) > 0) {
	    return this.getFromMap(reverse, a, b);
	} else {
	    return 0;
	}
    }
    
    /**
     * So we can use the same code to add to both forward and reverse.
     * @param map
     * @param a
     * @param b
     * @return 
     */
    protected Map<String, Map<String, Integer>> addToMap(Map<String, Map<String, Integer>> map, String a, String b) {
	if(map == null) {
	    map = new HashMap<>();
	}
	if(!map.containsKey(a)) {
	    map.put(a, new HashMap<String, Integer>());
	}
	Map<String, Integer> aMap = map.get(a);
	if(!aMap.containsKey(b)){
	    aMap.put(b, 1);
	} else {
	    aMap.put(b, aMap.get(b) + 1);
	}
	return map;
    }
    
    /**
     * Just made this so I could test getting from forward and reverse.
     * @param map
     * @param a
     * @param b
     * @return 
     */
    protected int getFromMap(Map<String, Map<String, Integer>> map, String a, String b) {
	if(map.containsKey(a) && map.get(a).containsKey(b)) {
	    return map.get(a).get(b);
	} else {
	    return 0;
	}
    }
    
    public List<WordPairAssociation> getAll(String word) {
	List<WordPairAssociation> result = new ArrayList<>();
	if(forward.containsKey(word)) {
	    for(String key : forward.get(word).keySet()) {
		result.add(new WordPairAssociation(word, key, forward.get(word).get(key)));
	    }
	}
	if(reverse.containsKey(word)) {
	    for(String key : reverse.get(word).keySet()) {
		result.add(new WordPairAssociation(word, key, reverse.get(word).get(key)));
	    }
	}
	return result;
    }
}
