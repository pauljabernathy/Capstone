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
public class WordPairSeparation implements Comparable {
    public String preceeding;
    public String ending;
    public int separation;
    
    public WordPairSeparation(String preceeding, String ending, int separation) {
        this.preceeding = preceeding;
        this.ending = ending;
        this.separation = separation;
    }
    
    public String toString() {
        return preceeding + " " + separation + " " + ending;
    }
    
    public int compareTo(Object o) {
        if(o == null || !(o instanceof WordPairSeparation)) {
            return -1;
        } else {
            WordPairSeparation other = (WordPairSeparation)o;
            return this.preceeding.compareTo(other.preceeding);
        }
    }
}
