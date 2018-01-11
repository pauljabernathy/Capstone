/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capstone;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static java.util.stream.Collectors.toList;
import toolbox.random.Random;
import toolbox.stats.*;
import java.util.function.Predicate;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import toolbox.util.ListArrayUtil;

//TODO:  might should return an IOException for null and empty file names
//Some inconsistency here, since giving a non empty name of a file that does not exist results in an IOException
//but a null or empty value for filename just returns empty results in several of these methods.

//TODO:  Consolidate file reading into one or two methods.

/**
 *
 * @author pabernathy
 */
public class Capstone {
    
    //line = line.toLowerCase().trim().replaceAll("\\.", "").replaceAll(":", "").replaceAll("\\-", "").replaceAll("\n", " ")
    //.replaceAll(",", "").replaceAll("\"", "");
    public static final Map<String, String> DEFAULT_PREPROCESS_REPLACEMENTS;
    public static final List<String> DEFAULT_BREAKS_BETWEEN_WORDS;
    public static final List<String> DEFAULT_SENTENCE_BREAKS;
    
    //TODO:  use regex for replacements, so you can do things like remove ' at the start and end but not in the middle of the word
    static {
	DEFAULT_PREPROCESS_REPLACEMENTS = new HashMap<>();
	//DEFAULT_PREPROCESS_REPLACEMENTS.put("\\.", "");
	DEFAULT_PREPROCESS_REPLACEMENTS.put(":", "");
	DEFAULT_PREPROCESS_REPLACEMENTS.put("\\-", "");
	DEFAULT_PREPROCESS_REPLACEMENTS.put("\n", " ");
	DEFAULT_PREPROCESS_REPLACEMENTS.put(",", "");
	DEFAULT_PREPROCESS_REPLACEMENTS.put("\"", "");
	DEFAULT_PREPROCESS_REPLACEMENTS.put("!", "");
	DEFAULT_PREPROCESS_REPLACEMENTS.put("\\[", "");
	DEFAULT_PREPROCESS_REPLACEMENTS.put("\\]", "");
	//DEFAULT_PREPROCESS_REPLACEMENTS.put("", "");
    
	DEFAULT_BREAKS_BETWEEN_WORDS = new ArrayList<>();
	DEFAULT_BREAKS_BETWEEN_WORDS.add(" ");
	
	DEFAULT_SENTENCE_BREAKS = Arrays.asList(".", "\\!", "?");
	
    }
    public static List<String> readLinesFromFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        List<String> lines = new ArrayList<>();
        while(reader.ready()) {
            lines.add(reader.readLine());
        }
        return lines;
    }
    
    public static List<String> readLinesFromFile(String filename, int maxLines) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        List<String> lines = new ArrayList<>();
        while(reader.ready()) {
            lines.add(reader.readLine());
        }
        return lines;
    }
    
    public static List<String> readSentencesFromFile(String filename) throws IOException {
	return readSentencesFromFile(new Request(filename));
    }
    
    public static List<String> readSentencesFromFile(Request request) throws IOException {
        String filename = request.getFilename();
        List<String> allSentences = new ArrayList<>();
        if(filename == null || filename.equals("")) {
            return allSentences;
        }
        //List<String> sentenceBreaks = Arrays.asList(".", "!", "?");
        String currentLine = null;
        String leftoverSentencePart = null;
        //String[] currentSentences = null;
	List<String> currentSentences = null;
        boolean splitSentenceLastLine = false;
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        while(reader.ready()) {
            currentLine = reader.readLine();
	    //TODO: unit test
	    if(currentLine != null) {
		for(String key : request.getReplacements().keySet()) {
		    currentLine.replaceAll(key, request.getReplacements().get(key));
		}
	    }
            currentSentences = Capstone.tokenize(currentLine, request.setWordBreaks(DEFAULT_SENTENCE_BREAKS));	//a hack to tell it to tokenize based on the sentence breaks
	    //TODO:  a better long term solution than the above hack
            if(currentSentences.size() == 0) {
                //I don't know why this would happen but check anyway.
                continue;
            }
            if(splitSentenceLastLine) {
                //currentSentences[0] = leftoverSentencePart + currentSentences[0];
                if(!allSentences.isEmpty()) {
                    //add the first sentence fragment to the last one recorded, because they are apparently the same sentence tokenize across two lines
                    allSentences.set(allSentences.size() - 1, allSentences.get(allSentences.size() - 1).replace("\n", "") + " " + currentSentences.get(0)); //The + " " before currentSentences[0] is so it won't combine the last word of the previous line with the first word of the next line into the same word.
                    //Now add the rest to allSentences.
                    for(int i = 1; i < currentSentences.size(); i++) {
                        if(currentSentences.get(i) != null && currentSentences.get(i).length() > 0) {
                            allSentences.add(currentSentences.get(i));
                        }
                    }
                } else {
                    //should never get here
                    allSentences.addAll(currentSentences);
                    System.err.println("in Capstone::readSentencesFromFile(): splitSentenceLastLine was true but allSentences was empty");
                }
            } else {
                //There is no leftover sentence fragment, so just add them all
                allSentences.addAll(currentSentences);
            }
            splitSentenceLastLine = true;
            for(String punct : request.getSentenceBreaks()) {
                if(currentLine.endsWith(punct)) {
                    splitSentenceLastLine = false;
                    break;
                }
            }
            
        }
        reader.close();
        return allSentences;
    }
    
    //TODO:  return List<String>; This appears in a number of places so it will take a few minutes to refactor.
    /**
     * Tokenize the given text, using the breaks list as a list of things separating each token.
     * @param text The text to tokenize
     * @param breaks The list of characters or string to use as separators.
     * @return 
     * @deprecated 
     */
    public static String[] tokenize(String text, List<String> breaks) {
        //return tokenize(text, breaks, Arrays.asList("...")).toArray(new String[] {});
	return tokenize(text, new Request("").setWordBreaks(breaks).setRemoveStopWords(true)).toArray(new String[] {});
    }
    
    //TODO:  Get that Arrays.asList("...") out of there.
    public static List<String> tokenize(String text, Request request) {
        List<String> result = new ArrayList<>();
        if(text == null || text.length() == 0) {
            return result;
        }
	if(request == null) {
	    result.add(text);
	    return result;
	}
	List<String> breaks = request.shouldTokenizeOnSentenceBreaks() ? request.getSentenceBreaks() : request.getWordBreaks();
        if(breaks == null || breaks.isEmpty()) {
            //result = new String[1];
            //result[0] = text;
	    result.add(text);
            return result;
        }
        
        //TODO:  a better way of handling it than removing
        /*for(String s : toRemove) {
            text = text.replace(s, "");
        }*/
	//TODO: unit test this
	for(String s : request.getReplacements().keySet()) {
	    text = text.replace(s, request.getReplacements().get(s));
	}
        String regex = "[";
        for(String s : breaks) {
            regex += s;
        }
        regex += "]";
	String[] words = text.split(regex);
	String word = null;
        for(int i = 0; i < words.length; i++) {
	    word = words[i].replace("\t", "").trim().toLowerCase();
	    //System.out.println(word);
	    if(word == null || word.isEmpty()) {
		continue;
	    }
	    if(!request.shouldRemoveStopWords()) {
		//System.out.println("adding " + word + ", not caring about stop words");
		result.add(word);
	    } else if(!request.getStopWords().isStopWord(word)) {
		//System.out.println("adding " + word + ", which is not a stop word");
		result.add(word);
	    } 
        }
        return result;
    }
    
    //unfinished, doing getWordPairSeparationss instead
    //TODO:  remove?
    public static Optional<WordPairSeparation> getWordPairSeparation(String text, int separation) {
        WordPairSeparation pair = null;
	if(text == null) {
            return Optional.ofNullable(null);
        }
        String[] words = text.split(" ");
        
        return Optional.ofNullable(pair);
    }
    
    public static Optional<List<WordPairSeparation>> getWordPairSeparationss(String text) {
        if(text == null || text.isEmpty()) {
            return Optional.ofNullable(null);
        }
        
        String[] words = text.split(" ");
        if(words == null || words.length == 0) {
            return Optional.ofNullable(null);
        }
        List<WordPairSeparation> pairs = new ArrayList<>();
        for(int i = 0; i < words.length; i++) {
            for(int j = i + 1; j < words.length; j++) {
                pairs.add(new WordPairSeparation(words[i], words[j], j - i));
            }
        }
        return Optional.ofNullable(pairs);
    }
    
    public static List<WordPairSeparation> getWordPairSeparationsFromFile(String filename) throws IOException {
        List<String> sentences = readSentencesFromFile(filename);
        List<WordPairSeparation> pairs = new ArrayList<>();
        Optional<List<WordPairSeparation>> current = null;
        for(String sentence : sentences) {
            current = getWordPairSeparationss(sentence);
            if(current.isPresent()) {
                pairs.addAll(current.get());
            }
        }
        return pairs;
    }
    
    public static Map<Integer, TreeHistogram<WordPairSeparation>> getWordPairSepHistogramsFromFile(String filename) throws IOException {
        Map<Integer, TreeHistogram<WordPairSeparation>> map = new HashMap();
        List<WordPairSeparation> wordPairs = getWordPairSeparationsFromFile(filename);
        //if(wordPairs.isPresent()) {
        for(WordPairSeparation wp : wordPairs) {
            //wordPairs.stream().forEach(wp -> map.get(wp.separation).insert(wp, 1));
            if(map.get(wp.separation) == null) {
                TreeHistogram<WordPairSeparation> th = new TreeHistogram<>();
                map.put(wp.separation, th);
            }
            map.get(wp.separation).insert(wp, 1);
        }
        //}
        return map;
    }
    
    
    
    
    
    public static void sampleLinesFromFile(String inputFile, String outputFile, double probability) {
        BufferedReader reader = null;
        PrintWriter writer = null;
        try {
           reader = new BufferedReader(new FileReader(inputFile));
           writer = new PrintWriter(new FileWriter(outputFile));
           String line = "";
           while(reader.ready()) {
               line = reader.readLine();
               if(Random.rbinom(1, probability)[0] == 1) {
                   writer.println(line);
               }
           }
           reader.close();
           writer.close();
        } catch(IOException e) {
            System.err.println(e.getClass() + " in sampleLinesFromFile(" + inputFile + ", " + outputFile + ", " + probability + "): " + e.getMessage());
        } finally {
            
        }
    }
    
    public static void main(String[] args) {
        //Capstone lf = new Capstone();
        //List<String> lines = null;        /**/
        //try {
            /**lines = lf.readLinesFromFile("/Users/pabernathy/workspace/webseries/Data/oracle/dgb_usergroup.sql").stream().filter((String s) -> s.contains("'FEDWIRE'")).collect(toList());
            System.out.println(lines.size());
            for(String s : lines) {
                System.out.println(s.replaceAll("FEDWIRE", "FEDTAX"));
            }
            for(String s : lines) {
                //System.out.println(s.replaceAll("FEDWIRE", "FEDTAX").replace("MAINT", ""));
            }/**/
            
            /**lines = lf.readLinesFromFile("/Users/pabernathy/workspace/webseries/Data/oracle/dgb_accounts.sql").stream().filter((String s) -> s.contains("'FEDWIRE'")).collect(toList());
            System.out.println();//lines.size());
            for(String s : lines) {
                //System.out.println(s.replaceAll("FEDWIRE", "FEDTAX"));
            }
            for(String s : lines) {
                System.out.println(s.replaceAll("FEDWIRE", "FEDTAX").replace("MAINT", ""));
            }/**/
            
            /*List<String> existingLines = lf.readLinesFromFile("/Users/pabernathy/desktop/dgb_usergroup.sql").stream().filter((String s) -> s.contains("'FEDTAX'")).collect(toList());
            System.out.println(existingLines.size());
            for(String s : existingLines) {
                System.out.println(s);
            }
            /**lines = lf.readLinesFromFile("/Users/pabernathy/desktop/dgb_accounts.sql").stream().filter((String s) -> s.contains("'FEDTAX'")).collect(toList());
            System.out.println(existingLines.size());
            for(String s : existingLines) {
                System.out.println(s);
            }/**/
            
            /*for(String s : existingLines) {
                if(lines.contains(s)) {
                    System.out.println("already have " + s);
                }
                lines.remove(s);
            }
            //lines.stream().s*/
        //} catch(IOException e) {
        //    System.err.println(e.getClass() + " " + e.getMessage());
        //}
        if(args != null && args.length > 0) {
            switch(args[0]) {
                case "WordPairHistogram":
                    if(args.length > 1) {
                        Map<Integer, TreeHistogram<WordPairSeparation>> result = null;
                        String filename = args[1];
                        try {
                            result = Capstone.getWordPairSepHistogramsFromFile(filename);
                            System.out.println(result);
                        }
                        catch(IOException e) {
                            System.err.println(e.getClass() + " trying to read " + filename + ":  " + e.getMessage());
                        }
                    }
                    break;
                default:
                    //do nothing
            }
        }
	//TreeHistogram hist = Capstone.fileSummaryTreeHistogram(new Request("through_the_looking_glass.txt"));
	//hist.getAsList(toolbox.stats.TreeHistogram.Sort.COUNT).forEach(System.out::println);
	try {
	    WordMatrix matrix = Capstone.findWordMatrixFromFile(new Request("through_the_looking_glass.txt"));
	    System.out.println(matrix.getAllAssociationsFor("knight"));
	    System.out.println(matrix.getAllAssociationsFor("looking"));
	    System.out.println(matrix.getAllAssociationsFor("glass"));
	} catch(IOException e) {
	    System.err.println(e.getClass() + " trying to get the WordMatrix:  " + e.getMessage());
	}
	System.out.println("got the word matrix?");
    }
    
    public static String findLongestLine(String filename) {
        String longestLine = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            int longestLength = 0;
            int currentIndex = 1;
            String currentLine = "";
            while(reader.ready()) {
                if(currentIndex % 100000 == 0) {
                    //System.out.println(currentIndex + ";  longest line so far is \"" + longestLine + "\"");
                }
                currentLine = reader.readLine();
                if(currentLine != null && currentLine.length() > longestLength) {
                    longestLength = currentLine.length();
                    longestLine = currentLine;
                }
                currentIndex++;
            }
            System.out.println("There are " + (currentIndex - 1) + " lines in this file.");
        } catch(IOException e) {
            System.err.println(e.getClass() + " in findLongestLine(" + filename + "):  " + e.getMessage());
        } finally {
            return longestLine;
        }
    }
    
    public static int findNumOccurrences(String filename, String pattern) {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            int longestLength = 0;
            int currentIndex = 1;
            String currentLine = "";
            while(reader.ready()) {
                if(currentIndex % 100000 == 0) {
                    //System.out.println(currentIndex + ";  count so far is \"" + count + "\"");
                }
                currentLine = reader.readLine();
                if(currentLine != null && currentLine.contains(pattern)) {
                    count++;
                }
                currentIndex++;
            }
            
        } catch(IOException e) {
            System.err.println(e.getClass() + " in findLongestLine(" + filename + "):  " + e.getMessage());
        } finally {
            return count;
        }
    }
    
    public static List<String> findLinesThatContain(String filename, String pattern) {
        List<String> results = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            int currentIndex = 1;
            String currentLine = "";
            while(reader.ready()) {
                if(currentIndex % 100000 == 0) {
                    //System.out.println(currentIndex + ";  count so far is \"" + count + "\"");
                }
                currentLine = reader.readLine();
                if(currentLine != null && currentLine.contains(pattern)) {
                    results.add(currentLine);
                }
                currentIndex++;
            }
            
        } catch(IOException e) {
            System.err.println(e.getClass() + " in findLongestLine(" + filename + "):  " + e.getMessage());
        } finally {
            return results;
        }
    }
    
    public static List<String> findLinesThatMatch(String filename, Predicate<String> pattern) {
        List<String> results = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            int currentIndex = 1;
            String currentLine = "";
            while(reader.ready()) {
                if(currentIndex % 100000 == 0) {
                    //System.out.println(currentIndex + ";  count so far is \"" + count + "\"");
                }
                currentLine = reader.readLine();
                if(pattern.test(currentLine)) {
                    results.add(currentLine);
                }
                currentIndex++;
            }
            
        } catch(IOException e) {
            System.err.println(e.getClass() + " in findLongestLine(" + filename + "):  " + e.getMessage());
        } finally {
            return results;
        }
    }
    
    public static Histogram wordCount(String filename) {
        Histogram h = new Histogram();
        List<String> allWords = readFileAsStrings(filename, " ");
        //h.setDataList(allWords);
        h = new Histogram(allWords);
        return h;
    }
    
    //TODO:  rethrow this Exception!
    public static List<String> readFileAsStrings(String filename, String delimiter) {
	return readFileAsStrings(filename, delimiter, DEFAULT_PREPROCESS_REPLACEMENTS);
    }
    
    //TODO:  Unit test after blacklist update!
    public static List<String> readFileAsStrings(String filename, String delimiter, Map<String, String> replacements) {
        List<String> result = new ArrayList<>();
        if(filename == null || filename.equals("") || delimiter == null) {
            return result;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = "";
            String[] words = { };
            
            while(reader.ready()) {
                line = reader.readLine();
		//line = line.toLowerCase().trim().replaceAll("\\.", "").replaceAll(":", "").replaceAll("\\-", "").replaceAll("\n", " ").replaceAll(",", "").replaceAll("\"", "");
		line = line.toLowerCase().trim();
		for(String toReplace : replacements.keySet()) {
		    line = line.replaceAll(toReplace, replacements.get(toReplace));
		}
                    
                if(line != null && !"".equals(line)) {
                    words = line.split(delimiter);
                    result.addAll(Arrays.asList(words));
                }
            }
        } catch(IOException e) {
            System.err.println(e.getClass() + " in readFileAsString(" + filename + "):  " + e.getMessage());
        }
        return result;
    }
    
    public static Histogram fileSummaryHistogram(String filename, String delimiter) {
        List<String> allWords = new ArrayList<>();
        int lineCount = 0;
        int wordCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = "";
            String[] words = { };
            
            while(reader.ready()) {
                line = reader.readLine();
                lineCount++;
                if(line != null && !"".equals(line)) {
                    words = line.split(delimiter);
                    if(words == null) {
                        continue;
                    }
                    allWords.addAll(Arrays.asList(words));
                    wordCount += words.length;
                }
            }
        } catch(IOException e) {
            System.err.println(e.getClass() + " in findLongestLine(" + filename + "):  " + e.getMessage());
        }
        return new Histogram(allWords).setLabel("num lines " + lineCount + "; num words " + wordCount);
    }
    
    public static TreeHistogram fileSummaryTreeHistogram(Request request) {
        List<String> allWords = new ArrayList<>();
        int lineCount = 0;
        int wordCount = 0;
        TreeHistogram<String> hist = new TreeHistogram<String>();
        try (BufferedReader reader = new BufferedReader(new FileReader(request.getFilename()))) {
            String line = "";
            String[] words = { };
            
            while(reader.ready()) {
                line = reader.readLine();
                lineCount++;
                if(line != null && !"".equals(line)) {
		    line = line.toLowerCase().trim();
		    for(String toReplace : DEFAULT_PREPROCESS_REPLACEMENTS.keySet()) {
			line = line.replaceAll(toReplace, DEFAULT_PREPROCESS_REPLACEMENTS.get(toReplace));
		    }
                    words = Capstone.tokenize(line, request).toArray(new String[] {});
                    if(words == null) {
                        continue;
                    }
                    allWords.addAll(Arrays.asList(words));//TODO: why are we using a separate list here instead of adding it straight into the histogram?
                    wordCount += words.length;
                }
            }
        } catch(IOException e) {
            System.err.println(e.getClass() + " in fileSummaryHistogram(" + request.getFilename() + "):  " + e.getMessage());
        }
        allWords.stream().forEach(word -> hist.insert(word, 1));
        return hist;
    }
    
    public static TreeHistogram fileSummaryTreeHistogram2(Request request) {
        List<String> allWords = new ArrayList<>();
        int lineCount = 0;
        int wordCount = 0;
        TreeHistogram<String> hist = new TreeHistogram<String>();
        try (BufferedReader reader = new BufferedReader(new FileReader(request.getFilename()))) {
	    List<String> sentences = Capstone.readSentencesFromFile(request);
            for(String sentence : sentences) {
		List<String> words = Capstone.tokenize(sentence, request);
		if(request.useBinaryAssociationsOnly()) {
		    words = words.stream().distinct().collect(toList());
		}
		words.forEach(word -> hist.insert(word, 1));
	    }
        } catch(IOException e) {
            System.err.println(e.getClass() + " in fileSummaryHistogram(" + request.getFilename() + "):  " + e.getMessage());
        }
        allWords.stream().forEach(word -> hist.insert(word, 1));
        return hist;
    }
    
    public static List<String> fileSummary(String filename, String delimiter) {
        List<String> allWords = new ArrayList<>();
        int lineCount = 0;
        int wordCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = "";
            String[] words = { };
            
            while(reader.ready()) {
                line = reader.readLine();
                lineCount++;
                if(line != null && !"".equals(line)) {
                    words = line.split(delimiter);
                    if(words == null) {
                        continue;
                    }
                    allWords.addAll(Arrays.asList(words));
                    wordCount += words.length;
                }
            }
        } catch(IOException e) {
            System.err.println(e.getClass() + " in findLongestLine(" + filename + "):  " + e.getMessage());
        }
        List<String> result = new ArrayList<>();
        result.add("lines:  " + lineCount);
        result.add("words:  " + wordCount);
        return result;
    }
    
    /**
     * @deprecated
     * @param words
     * @return 
     */
    public static WordMatrix findWordMatrix(String[] words) {
	return findWordMatrix(words, Constants.DEFAULT_BINARY_ASSOCIATIONS_ONLY);
    }
    
    public static WordMatrix findWordMatrix(String[] words, boolean oncePerSentence) {
	WordMatrix matrix = new WordMatrix();
	if(words == null || words.length == 0) { 
	    return matrix;
	}
	for(int i = 0; i < words.length; i++) {
	    for(int j = i + 1; j < words.length; j++) {
		if(!oncePerSentence || (oncePerSentence && matrix.get(words[i], words[j]) == 0)) {
		    matrix.add(words[i], words[j]);
		}
	    }
	}
	return matrix;
    }
    
    /**
     * @deprecated 
     * @param words
     * @return 
     */
    public static WordMatrix findWordMatrix(List<String> words) {
	return findWordMatrix(words.toArray(new String[]{}));
    }
    
    public static WordMatrix findWordMatrix(List<String> words, Request request) {
	/*if(request.useBinaryAssociationsOnly()) {
	    words = words.stream().distinct().collect(toList());
	}*/
	return findWordMatrix(words.toArray(new String[] {}), request.useBinaryAssociationsOnly());
    }
    
    public static WordMatrix findWordMatrixFromFile(Request request) throws IOException {
	WordMatrix matrix = new WordMatrix();
	if(request == null) {
	    return matrix;
	}
	List<String> sentences = readSentencesFromFile(request.getFilename());
	return findWordMatrixFromSentenceList(sentences, request);
    }
    
    //does not currently remove punctuation
    public static WordMatrix findWordMatrixFromSentenceList(List<String> sentences, Request request) {
	WordMatrix matrix = new WordMatrix();
	if(sentences == null) {
	    return matrix;
	}
	List<String> wordsInSentence = null;
	for(String sentence : sentences) {
	    wordsInSentence = Capstone.tokenize(sentence, request);
	    //System.out.println(sentence);
	    //System.out.println(wordsInSentence);
	    matrix.addAll(findWordMatrix(wordsInSentence, request));
	}
	return matrix;
    }
}
