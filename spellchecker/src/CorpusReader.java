
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CorpusReader {

    final static String CNTFILE_LOC = "samplecnt.txt";
    final static String VOCFILE_LOC = "samplevoc.txt";

    private HashMap<String, Integer> ngrams;
    private Set<String> vocabulary;
    private double totalWords = 0;

    public CorpusReader() throws IOException {
        readNGrams();
        readVocabulary();
        for (String word : vocabulary) {
            totalWords += ngrams.get(word);
        }
    }

    /**
     * Returns the n-gram count of <NGram> in the file
     *
     *
     * @param nGram : space-separated list of words, e.g. "adopted by him"
     * @return 0 if <NGram> cannot be found, otherwise count of <NGram> in file
     */
    public int getNGramCount(String nGram) throws NumberFormatException {
        if (nGram == null || nGram.length() == 0) {
            throw new IllegalArgumentException("NGram must be non-empty.");
        }
        Integer value = ngrams.get(nGram);
        return value == null ? 0 : value;
    }

    public double getFrequency(String word) {
        double f = 0;

        f = getSmoothedCount(word) / totalWords;

        return f;
    }

    private void readNGrams() throws
            FileNotFoundException, IOException, NumberFormatException {
        ngrams = new HashMap<>();

        FileInputStream fis;
        fis = new FileInputStream(CNTFILE_LOC);
        BufferedReader in = new BufferedReader(new InputStreamReader(fis));

        while (in.ready()) {
            String phrase = in.readLine().trim();
            String s1, s2;
            int j = phrase.indexOf(" ");

            s1 = phrase.substring(0, j);
            s2 = phrase.substring(j + 1, phrase.length());

            int count = 0;
            try {
                count = Integer.parseInt(s1);
                ngrams.put(s2, count);
            } catch (NumberFormatException nfe) {
                throw new NumberFormatException("NumberformatError: " + s1);
            }
        }
    }

    private void readVocabulary() throws FileNotFoundException, IOException {
        vocabulary = new HashSet<>();

        FileInputStream fis = new FileInputStream(VOCFILE_LOC);
        BufferedReader in = new BufferedReader(new InputStreamReader(fis));

        while (in.ready()) {
            String line = in.readLine();
            vocabulary.add(line);
        }
    }

    /**
     * Returns the size of the number of unique words in the dataset
     *
     * @return the size of the number of unique words in the dataset
     */
    public int getVocabularySize() {
        return vocabulary.size();
    }

    /**
     * Returns the subset of words in set that are in the vocabulary
     *
     * @param set
     * @return
     */
    public HashSet<String> inVocabulary(Set<String> set) {
        HashSet<String> h = new HashSet<>(set);
        h.retainAll(vocabulary);
        return h;
    }

    public boolean inVocabulary(String word) {
        return vocabulary.contains(word);
    }

    public double getSmoothedCount(String NGram) {
        if (NGram == null || NGram.length() == 0) {
            throw new IllegalArgumentException("NGram must be non-empty.");
        }

        double smoothedCount = 0.0;
        String[] words = NGram.split(" ");// Split up in words, to get the first one.
        //System.out.println(words[0] + words[1]);
//        if (words.length == 1) { //Good-Turing smoothing for unigrams
//            if (getNGramCount(NGram) < 5) { // Only for words with less than 5 appearances
//                double tempInt = 0.0; // Counting the number of words appearin 1  time more than  Ngram
//                double tempInt1 = 0.0; // Counting the number of words appearing the same number of times as Ngram
//                if (getNGramCount(NGram) > 0) {
//                    for (String word : vocabulary) {
//                        if (getNGramCount(word) == getNGramCount(NGram)) {
//                            tempInt1++;
//                        } else if (getNGramCount(word) == getNGramCount(NGram) + 1) {
//                            tempInt++;
//                        }
//                    }
//                    smoothedCount += (getNGramCount(NGram) + 1) * tempInt / (tempInt1 * totalWords);
//                } else {
//
//                    for (String word : vocabulary) {
//                        if (getNGramCount(word) == 1) {
//                            tempInt++;
//                        }
//                    }
//                    smoothedCount += tempInt / totalWords;
//                }
//
//            } else {
//                smoothedCount = ngrams.get(NGram) / totalWords;
//            }
//
//        } else { // bigram smoothing
            smoothedCount += (getNGramCount(NGram) + 2 * (ngrams.get(words[0]) / totalWords)) / (getNGramCount(words[0]) + 2);
       // }
        return smoothedCount;
    }
}
