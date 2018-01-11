/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package capstone;

import static capstone.Capstone.DEFAULT_BREAKS_BETWEEN_WORDS;
import static capstone.Capstone.findWordMatrix;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import static java.util.stream.Collectors.toList;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import toolbox.stats.HistogramEntry;
import toolbox.stats.TreeHistogram;

/**
 *
 * @author paul
 */
public class Wanderer {
    
    private Logger logger;
    
    public static void main(String[] args) {
	Wanderer w = new Wanderer();
	w.run();
    }
    
    public Wanderer() {
	this.logger = toolbox.util.ListArrayUtil.getLogger(Wanderer.class, Level.DEBUG);
    }
    
    public void run() {
	Calendar start = Calendar.getInstance();
	logger.info(start.getTime());
	String filename = "beowulf i to xxii.txt";
	filename = "les_miserables.txt";
	List<MCAgent> agents = this.instantiateAgents(filename, 2);
	Calendar endInstantiation = Calendar.getInstance();
	logger.info(endInstantiation.getTime());
	logger.info(endInstantiation.getTimeInMillis() - start.getTimeInMillis());
	
	List<Thread> threads = this.instantiateThreads(agents);
	threads.forEach(thread -> thread.start());
	/**/while(this.anyStillRunning(threads)) {
	    //wait
	}/**/
	//agents.get(0).run();
	Calendar end = Calendar.getInstance();
	logger.info(end.getTime());
	logger.info((end.getTimeInMillis() - endInstantiation.getTimeInMillis()) / 1000);
	
	agents.forEach(agent -> agent.getLatestRatioCorrect());
	
    }
    
    protected List<MCAgent> instantiateAgents(String filename, int numAgents) {
	List<MCAgent> agents = new ArrayList<>();
	try {
	    //TODO:  Refactor Capstone to be able to make a word histogram from the sentences list, to cut down on how many times it has to read the file.
	    Request request = new Request(filename).setRemoveStopWords(true);
	    List<String> sentences = Capstone.readSentencesFromFile(filename);
	    TreeHistogram<String> ngrams = NGrams.getNGramsOfSentences(sentences, 3);	//TODO:  have the MCAgent compute this based on it's object variable, which should be specified in the genome
	    String ngram = "while he";
	    List<String> matchingNGrams = ngrams.queryFromFirst(word -> word.startsWith(ngram));
	    List<HistogramEntry<String>> m = ngrams.queryAll(word -> word.startsWith(ngram));
	    logger.debug(ngrams.getAsList(TreeHistogram.Sort.COUNT).stream().limit(50).collect(toList()));
	    logger.debug("\n" + matchingNGrams);
	    logger.debug("\n" + m);

	    WordMatrix matrix = Capstone.findWordMatrixFromSentenceList(sentences, request);
	    WordMatrix binaryMatrix = Capstone.findWordMatrixFromSentenceList(sentences, new Request(filename).setRemoveStopWords(true).setBinaryAssociationsOnly(true));

	    TreeHistogram<String> totalAllWordHist = Capstone.fileSummaryTreeHistogram(request.setRemoveStopWords(false));
	    //logger.info("\ntotalAllWordHist");
	    //totalAllWordHist.getAsList(TreeHistogram.Sort.COUNT).stream().limit(50).forEach(System.out::println);
	    TreeHistogram<String> totalNonStopWordHist = Capstone.fileSummaryTreeHistogram(request.setRemoveStopWords(true).setBinaryAssociationsOnly(true));
	    //logger.info("\ntotalNonStopWordHist");
	    //totalNonStopWordHist.getAsList(TreeHistogram.Sort.COUNT).stream().limit(50).forEach(System.out::println);
	    TreeHistogram<String> oncePerSentenceWordHist = Capstone.fileSummaryTreeHistogram(request.setBinaryAssociationsOnly(true));
	    //logger.info("\noncePerSentence");
	    //oncePerSentenceWordHist.getAsList(TreeHistogram.Sort.COUNT).stream().limit(50).forEach(System.out::println);

	    for(int i = 0; i < numAgents; i++) {
		//public MCAgent(List<String> sentences, TreeHistogram<String> totalAllWordHist, TreeHistogram<String> totalNonStopWordHist, TreeHistogram<String> oncePerSentenceWordHist, 
	//TreeHistogram<String> ngrams, WordMatrix weightedMatrix, WordMatrix binaryMatrix) {
		/*MCAgent agent = new MCAgent(totalAllWordHist, sentences, ngrams, matrix);
		agent.setTotalNonStopWordHist(totalNonStopWordHist);
		agent.setOncePerSentenceWordHist(oncePerSentenceWordHist);
		agent.setBinaryMatrix(binaryMatrix);*/
		
		MCAgent agent = new MCAgent(sentences);
		agent.setTotalAllWordHist(totalAllWordHist)
		    .setTotalNonStopWordHist(totalNonStopWordHist)    
		    .setOncePerSentenceWordHist(oncePerSentenceWordHist)
		    .setNgrams(ngrams)
		    .setWeightedMatrix(matrix)
		    .setBinaryMatrix(binaryMatrix)
		    .setNumBatches(10)
		    .setNumRunsPerBatch(20)
		    .setName("Agent" + i);
		agent.setGenome(new double[] { 1.150221459310199, 2.853131659824399, 0.08473017271422967, 4.3904694260382735, 4.444581619967423, 0.5530100314467706, 1.142990210003586, 3.839286815451411 });
		agents.add(agent);
	    }
	} catch(IOException e) {
	    System.err.println(e.getClass() + " " + e.getMessage());
	}
	return agents;
    }
    
    public List<Thread> instantiateThreads(List<MCAgent> agents) {
	List<Thread> threads = new ArrayList<>();
	agents.forEach(agent -> threads.add(new Thread(agent)));
	return threads;
    }
    
    public boolean anyStillRunning(List<Thread> threads) {
	if (threads.stream().anyMatch((thread) -> (thread.isAlive()))) {
	    return true;
	}
	return false;
    }
    
}
