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

/**
 *
 * @author pabernathy
 */
public class Capstone {
    
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
    
    public static List<String> readSentecesFromFile(String filename) throws IOException {
        
        List<String> allSentences = new ArrayList<>();
        if(filename == null || filename.equals("")) {
            return allSentences;
        }
        List<String> sentenceBreaks = Arrays.asList(".", "!", "?");
        String currentLine = null;
        String leftoverSentencePart = null;
        String[] currentSentences = null;
        boolean splitSentenceLastLine = false;
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        while(reader.ready()) {
            currentLine = reader.readLine();
            currentSentences = split(currentLine, sentenceBreaks);
            if(currentSentences.length == 0) {
                //I don't know why this would happen but check anyway.
                continue;
            }
            if(splitSentenceLastLine) {
                //currentSentences[0] = leftoverSentencePart + currentSentences[0];
                if(!allSentences.isEmpty()) {
                    //add the first sentence fragment to the last one recorded, because they are apparently the same sentence split across two lines
                    allSentences.set(allSentences.size() - 1, allSentences.get(allSentences.size() - 1).replace("\n", "") + " " + currentSentences[0]); //The + " " before currentSentences[0] is so it won't combine the last word of the previous line with the first word of the next line into the same word.
                    //Now add the rest to allSentences.
                    for(int i = 1; i < currentSentences.length; i++) {
                        if(currentSentences[i] != null && currentSentences[i].length() > 0) {
                            allSentences.add(currentSentences[i]);
                        }
                    }
                } else {
                    //should never get here
                    allSentences.addAll(Arrays.asList(currentSentences));
                    System.err.println("in Capstone::readSentencesFromFile(): splitSentenceLastLine was true but allSentences was empty");
                }
            } else {
                //There is no leftover sentence fragment, so just add them all
                allSentences.addAll(Arrays.asList(currentSentences));
            }
            splitSentenceLastLine = true;
            for(String punct : sentenceBreaks) {
                if(currentLine.endsWith(punct)) {
                    splitSentenceLastLine = false;
                    break;
                }
            }
            
        }
        reader.close();
        return allSentences;
    }
    
    public static String[] split(String text, List<String> breaks) {
        return split(text, breaks, Arrays.asList("..."));
    }
    
    public static String[] split(String text, List<String> breaks, List<String> whiteList) {
        String[] result = new String[] {};
        if(text == null || text.length() == 0) {
            return result;
        }
        if(breaks == null || breaks.isEmpty()) {
            result = new String[1];
            result[0] = text;
            return result;
        }
        
        //TODO:  a better way of handling it than removing
        for(String s : whiteList) {
            text = text.replace(s, "");
        }
        String regex = "[";
        for(String s : breaks) {
            regex += s;
        }
        regex += "]";
        result = text.split(regex);
        for(int i = 0; i < result.length; i++) {
            result[i] = result[i].replace("\t", "").trim();
        }
        return result;
    }
    
    //unfinished, doing getWordPairs instead
    //TODO:  remove?
    public static Optional<WordPairSeparation> getWordPair(String text, int separation) {
        WordPairSeparation pair = null;
	if(text == null) {
            return Optional.ofNullable(null);
        }
        String[] words = text.split(" ");
        
        return Optional.ofNullable(pair);
    }
    
    public static Optional<List<WordPairSeparation>> getWordPairs(String text) {
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
    
    public static List<WordPairSeparation> getWordPairsFromFile(String filename) throws IOException {
        List<String> sentences = readSentecesFromFile(filename);
        List<WordPairSeparation> pairs = new ArrayList<>();
        Optional<List<WordPairSeparation>> current = null;
        for(String sentence : sentences) {
            current = getWordPairs(sentence);
            if(current.isPresent()) {
                pairs.addAll(current.get());
            }
        }
        return pairs;
    }
    
    public static Map<Integer, TreeHistogram<WordPairSeparation>> getWordPairHistogramsFromFile(String filename) throws IOException {
        Map<Integer, TreeHistogram<WordPairSeparation>> map = new HashMap();
        List<WordPairSeparation> wordPairs = getWordPairsFromFile(filename);
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
    
    
    
    
    
    public void sampleLinesFromFile(String inputFile, String outputFile, double probability) {
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
                            result = Capstone.getWordPairHistogramsFromFile(filename);
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
    
    public static List<String> readFileAsStrings(String filename, String delimiter) {
        List<String> result = new ArrayList<>();
        if(filename == null || filename.equals("") || delimiter == null) {
            return result;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = "";
            String[] words = { };
            
            while(reader.ready()) {
                line = reader.readLine();
                line = line.toLowerCase().trim().replaceAll("\\.", "").replaceAll(":", "").replaceAll("\\-", "").replaceAll("\n", " ").replaceAll(",", "").replaceAll("\"", "");
                    
                if(line != null && !"".equals(line)) {
                    words = line.split(delimiter);
                    result.addAll(Arrays.asList(words));
                }
            }
        } catch(IOException e) {
            System.err.println(e.getClass() + " in findLongestLine(" + filename + "):  " + e.getMessage());
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
    
    public static TreeHistogram fileSummaryTreeHistogram(String filename, String delimiter) {
        List<String> allWords = new ArrayList<>();
        int lineCount = 0;
        int wordCount = 0;
        TreeHistogram<String> hist = new TreeHistogram<String>();
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
}
