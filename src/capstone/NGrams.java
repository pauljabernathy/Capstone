/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capstone;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.util.TreeMap;
import toolbox.util.ListArrayUtil;
import toolbox.stats.TreeHistogram;

/**
 *
 * @author pabernathy
 */
public class NGrams {
    
    public static final String SPACE = " ";
    
    public static TreeMap<String, Integer> getNGrams(String filename, int n, String delimiter, boolean allLowerCase) throws IOException {
        List<String> words = Capstone.readFileAsStrings(filename, delimiter);
        return getNGrams(words, n, delimiter, allLowerCase);
    }
    
    /**
     * 
     * @param words
     * @param n
     * @param delimiter what to use to separate words in each entry; not necessarily the same as how the words are separated in an input file
     * @param allLowerCase
     * @return 
     */
    public static TreeMap<String, Integer> getNGrams(List<String> words, int n, String delimiter, boolean allLowerCase) {
        TreeMap<String, Integer> ngrams = new TreeMap<String, Integer>();
        if(words == null) {
            return ngrams;
        }
        //words.stream().forEach(word -> ngrams.add(word));//will only work for 1grams
        String ngram = null;
        for(int i = 0; i < words.size() - n + 1; i++) {
            ngram = ListArrayUtil.listToString(words.subList(i, i + n), delimiter, "", "");
            if(ngrams.containsKey(ngram)) {
                ngrams.put(ngram, ngrams.get(ngram) + 1);
            } else {
                ngrams.put(ngram, 1);
            }
        }
        return ngrams;
    }
    
    public static TreeHistogram<String> getNGramsTree(String filename, int n, String delimiter, boolean allLowerCase) throws IOException {
        List<String> words = Capstone.readFileAsStrings(filename, delimiter);
        return getNGramsTree(words, n, delimiter, allLowerCase);
    }
    
    public static TreeHistogram<String> getNGramsTree(List<String> words, int n, String delimiter, boolean allLowerCase) {
        TreeHistogram<String> ngrams = new TreeHistogram<>();
        
         if(words == null) {
            return ngrams;
        }
        //words.stream().forEach(word -> ngrams.add(word));//will only work for 1grams
        /*String ngram = null;
        for(int i = 0; i < words.size() - n + 1; i++) {
            ngram = ListArrayUtil.listToString(words.subList(i, i + n), delimiter, "", "");
            ngrams.insert(ngram, 1);
        }*/
        List<String> nGramsList = extractNGrams(words, n);
        nGramsList.forEach(gram -> ngrams.insert(gram, 1));
        return ngrams;
    }
    
    public static List<String> extractNGrams(List<String> words, int n) {
        List<String> grams = new ArrayList<>();
        for(int i = 0; i < words.size() - n + 1; i++) {
            grams.add(ListArrayUtil.listToString(words.subList(i, i + n), SPACE, "", ""));
        }
        return grams;
    }
    
    public static void writeNGrams(List<String> words, int n, String filename) throws IOException {
        List<String> grams = new ArrayList<>();
        PrintWriter writer = new PrintWriter(new FileWriter(filename));
        for(int i = 0; i < words.size() - n + 1; i++) {
            writer.println(ListArrayUtil.listToString(words.subList(i, i + n), SPACE, "", ""));
        }
    }
    
    //Really, just creates a histogram of lines of the file.  This is assuming that the file is in the format was you would get from writeNGrams()
    public static TreeHistogram<String> readNGramsFromFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        TreeHistogram<String> histogram = new TreeHistogram<>();
        String line = null;
        while(reader.ready()) {
            line = reader.readLine();
            histogram.insert(line, 1);
        }
        return histogram;
    }
    
    //surly this function already exists
    public static String getAsString(List<String> words, String delimiter) {
        /*StringBuilder result = new StringBuilder();
        for(int i = 0; i < words.size() - 1; i++) {
            result.append(words.get(i)).append(delimiter);
        }
        result.append(words.get(words.size() - 1));
        return result.toString();*/
        return ListArrayUtil.listToString(words, delimiter, "", "");
    }
}
