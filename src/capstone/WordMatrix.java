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
import static java.util.stream.Collectors.toList;

//TODO: Important question I just though of:  should the association of words be directional?  Right now it is not.  But let's say that the word "money" appears in a sentence three times
//and the word "fiduciary" appears once, should we say that money and fiduciary are linked three times (as currently), or that fiduciary "sees" monney three times
//but money only "sees" fiducidary once?  That seems like an important question that could affect the way text is analyzed.
//Something to do for later maybe because it will take some extra work.
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
    //or if adding the same pair again should update the count;  currently it updates the count
    public WordMatrix add(String a, String b) {
	return this.add(a, b, 1);
    }
    
    public WordMatrix add(String a, String b, int count) {
	if(a.compareTo(b) < 0) {
	    this.addToMap(forward, a, b, count);
	    this.addToMap(reverse, b, a, count);
	} else if(a.compareTo(b) > 0) {
	    this.addToMap(forward, b, a, count);
	    this.addToMap(reverse, a, b, count);
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
    
    //TODO:  Get the stack trace and verify that this is being called by another WordMatrix object.
    /**
     * gets the two maps used to store internal data; for use in addAll(WordMatrix)
     * @return a List of the two maps
     */
    protected List<Map<String, Map<String, Integer>>> getRawData() {
	List<Map<String, Map<String, Integer>>> maps = new ArrayList<>();
	maps.add(forward);
	maps.add(reverse);
	return maps;
    }
    
    /**
     * So we can use the same code to add to both forward and reverse.
     * @param map
     * @param a
     * @param b
     * @return 
     */
    protected Map<String, Map<String, Integer>> addToMap(Map<String, Map<String, Integer>> map, String a, String b) {
	return this.addToMap(map, a, b, 1);
    }
    
    protected Map<String, Map<String, Integer>> addToMap(Map<String, Map<String, Integer>> map, String a, String b, int count) {
	if(count < 1) {
	    //Don't add 0 or negative values.
	    return map;
	}
	if(map == null) {
	    map = new HashMap<>();
	}
	if(!map.containsKey(a)) {
	    map.put(a, new HashMap<String, Integer>());
	}
	Map<String, Integer> aMap = map.get(a);
	if(!aMap.containsKey(b)){
	    aMap.put(b, count);
	} else {
	    aMap.put(b, aMap.get(b) + count);
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
    
    public List<WordPairAssociation> getTopAssociationsFor(String word, int limit) {
	return this.getAllAssociationsFor(word).stream().limit(limit).collect(toList());
    }
    
    public List<WordPairAssociation> getAllAssociationsFor(String word) {
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
	//TODO: a sorting option parameter
	result.sort((a, b) -> {
	    if(a.getCount() < b.getCount()) {
		return 1;
	    } else if(a.getCount() == b.getCount()) {
		return 0;
	    } else {
		return -1;
	    }
	 });
	return result;
    }
    
    protected WordMatrix addAll(Map<String, Map<String, Integer>> map) {
	for(String word : map.keySet()) {
	    for(String pairedWord : map.get(word).keySet()) {
		this.add(word, pairedWord, map.get(word).get(pairedWord));
	    }
	}
	return this;
    }
    
    /**
     * Adds all the contents of the other WordMatrix to this one.
     * @param other
     * @return 
     */
    public WordMatrix addAll(WordMatrix other) {
	if(other == null) {
	    return this;
	}
	//List<Map<String, Map<String, Integer>>> maps = other.getRawData();
	//Map<String, Map<String, Integer>> forward = other.getRawData().get(0);
	this.addAll(other.getRawData().get(0));
	return this;
    }
}
