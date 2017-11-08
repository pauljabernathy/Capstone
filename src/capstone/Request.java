/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package capstone;

import java.util.List;
import java.util.Map;

/**
 *
 * @author paul
 */
public class Request {
    
    private String filename;
    private List<String> wordBreaks;
    private List<String> sentenceBreaks;
    StopWords stopWords;
    private Map<String, String> replacements;
    
    private boolean removeStopWords;
    private boolean binaryAssociationsOnly;	//whether or not word assocations should keep track of the count, or just that one exists
    
    public Request(String filename, List<String> wordBreaks, List<String> sentenceBreaks, StopWords stopWords, Map<String, String> replacements, boolean removeStopWords, boolean binaryAssociationOnly) {
	this.filename = filename;
	this.wordBreaks = wordBreaks;
	this.sentenceBreaks = sentenceBreaks;
	this.stopWords = stopWords;
	this.replacements = replacements;
	this.removeStopWords = removeStopWords;
	this.binaryAssociationsOnly = binaryAssociationsOnly;
    }
    
    public Request(String filename) {
	this(filename, Capstone.DEFAULT_BREAKS_BETWEEN_WORDS, Capstone.DEFAULT_SENTENCE_BREAKS, new StopWords(), Capstone.DEFAULT_PREPROCESS_REPLACEMENTS, Constants.DEFAULT_SHOULD_REMOVE_STOP_WORDS, Constants.DEFAULT_BINARY_ASSOCIATIONS_ONLY);
    }

    public String getFilename() {
	return filename;
    }

    public Request setFilename(String filename) {
	this.filename = filename;
	return this;
    }

    public List<String> getWordBreaks() {
	return wordBreaks;
    }

    public Request setWordBreaks(List<String> wordBreaks) {
	this.wordBreaks = wordBreaks;
	return this;
    }

    public List<String> getSentenceBreaks() {
	return sentenceBreaks;
    }

    public Request setSentenceBreaks(List<String> sentenceBreaks) {
	this.sentenceBreaks = sentenceBreaks;
	return this;
    }

    public StopWords getStopWords() {
	return stopWords;
    }

    public Request setStopWords(StopWords stopWords) {
	this.stopWords = stopWords;
	return this;
    }

    public Map<String, String> getReplacements() {
	return replacements;
    }

    public Request setReplacements(Map<String, String> replacements) {
	this.replacements = replacements;
	return this;
    }

    public boolean shouldRemoveStopWords() {
	return removeStopWords;
    }

    public Request setRemoveStopWords(boolean removeStopWords) {
	this.removeStopWords = removeStopWords;
	return this;
    }
}
