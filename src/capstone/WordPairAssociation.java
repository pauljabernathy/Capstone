/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package capstone;

/**
 *
 * @author paul
 */
public class WordPairAssociation {
    
    public static final String ERROR = "ERROR";
    private String first;
    private String second;
    private int count;		//How often the words appear together in the same sentence, document, etc.  Could also be some other measure of similarity.
    
    public WordPairAssociation(String first, String second) {
	this(first, second, 1);
    }
    
    public WordPairAssociation(String first, String second, int count) {
	//TODO:  How to handle nulls?
	if(first == null || second == null) {
	    throw new RuntimeException("cannot give a null word");
	}
	if(first.compareTo(second) <= 0) {
	    this.first = first;
	    this.second = second;
	} else if(first.compareTo(second) > 0) {
	    this.first = second;
	    this.second = first;
	}
	this.count = count;
    }

    public String getFirst() {
	return first;
    }

    public WordPairAssociation setFirst(String first) {
	this.first = first;
	return this;
    }

    public String getSecond() {
	return second;
    }

    public WordPairAssociation setSecond(String second) {
	this.second = second;
	return this;
    }

    public int getCount() {
	return count;
    }

    public WordPairAssociation setCount(int count) {
	this.count = count;
	return this;
    }
    
    //TODO:  what about count?  The association is between two words, but if the count is different...
    public boolean equals(Object o) {
	if(o instanceof WordPairAssociation) {
	    WordPairAssociation other = (WordPairAssociation)o;
	    return this.first.equals(other.first) && this.second.equals(other.second);
	} else {
	    return false;
	}
    }
    
    public String toString() {
	return this.first + " " + this.second + " " + this.count;
    }
}
