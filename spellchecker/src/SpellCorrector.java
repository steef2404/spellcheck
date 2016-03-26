
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SpellCorrector {

    final private CorpusReader cr;
    final private ConfusionMatrixReader cmr;

    final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz'".toCharArray();
    final double nine = Math.pow(10, 9);

    public SpellCorrector(CorpusReader cr, ConfusionMatrixReader cmr) {
        this.cr = cr;
        this.cmr = cmr;
    }

    public String correctPhrase(String phrase) {
        if (phrase == null || phrase.length() == 0) {
            throw new IllegalArgumentException("phrase must be non-empty.");
        }

        String[] words = phrase.split(" ");
        String finalSuggestion = "";
        HashSet<String[]> sentences = new HashSet<>(); // Will hold all possible sentences
        ArrayList<Map<String, Double>> allWords = new ArrayList<>(); // List of the maps, for each given word.
        String[] Actualwords = new String[words.length];

        for (int k = 0; k < words.length; k++) {
            Actualwords[k] = words[k];
            //System.out.println(Actualwords[k]);
        }

        for (String word : words) {// Add all maps to the list of possible candidates.
            Map<String, Double> map = getCandidateWords(word);
            allWords.add(map);

        }

        //Compute all possible sentences
        for (int i = 0; i < words.length; i++) {
            for (String CandidateI : allWords.get(i).keySet()) {
                //System.out.println(CandidateI + " " + words[i]);
                words[i] = CandidateI; // Replace the word at spot i with this candidate
                String[] temp1 = new String[words.length];
                for (int k = 0; k < words.length; k++) {
                    temp1[k] = words[k];
                }
                sentences.add(temp1); // Add the new sentence to the list, the set makes sure that doubles do not occur.
                System.out.println(sentences.size());
                //Given that only 2 errors occur and are never in consecutive words, skip an index
                for (int j = i + 2; j < words.length; j++) {
                    for (String CandidateJ : allWords.get(j).keySet()) {
                        words[j] = CandidateJ; // Replace the word at spot j with this candidate
                        String[] temp2 = new String[words.length];
                        for (int k = 0; k < words.length; k++) {
                            temp2[k] = words[k];
                        }
                        sentences.add(temp2); // Add the new sentence the list, the set makes sure that doubles do not occur.
                    }//Next CandidateJ
                    words[j] = Actualwords[j]; //Restoration of the jth word        
                }//Next J
            }//Next CandidateI
            words[i] = Actualwords[i]; // Restoration of the ith word.
        }//Next i
//        for (String[] s : sentences) {
//            for (int k = 0; k < s.length; k++) {
//                System.out.println(s[k]);
//            }
//            System.out.println("");
//        }
        Double MaxProb = 0.0;
        //Calculate the probaility of each sentence, using the  bigram model
        for (String[] s : sentences) {
            Double Prob = SentenceP(s, allWords);
            System.out.println(Prob + " " + sentences.size());
            if (Prob >= MaxProb) {
                MaxProb = Prob;
                finalSuggestion = Arrays.toString(s);
            }
        }
        //Return the sentence with the highest probability
        return finalSuggestion.trim();
    }

    /**
     * Given a sentence, computes the probability of the exact sentence, using
     * bigrams.
     *
     *
     * @param s
     * @param maps
     * @return Probability of sentence
     */
    public Double SentenceP(String[] s, ArrayList<Map<String, Double>> maps) {
        Double Prob = 1.0;
        //Compute unigram probabilities
        for (int i = 0; i < s.length; i++) {
            Prob *= maps.get(i).get(s[i]) / nine;

        }
        //Compute bigram probabilities
        for (int j = 0; j < s.length - 1; j++) {
            String Bigram = s[j].concat(" " + s[j + 1]);
            double count = (double) cr.getNGramCount(Bigram);

            Prob *= count / cr.getFrequency(s[j]);
        }
        return Prob;
    }

    /**
     * returns a map with candidate words and their noisy channel probability. *
     */
    public Map<String, Double> getCandidateWords(String word) {
        Map<String, Double> mapOfWords = new HashMap<>();
        if (cr.inVocabulary(word)) {
            mapOfWords.put(word, 0.95 * nine);
        }
        //System.out.println(word);

        insertion(word, mapOfWords);
        deletion(word, mapOfWords);
        substitution(word, mapOfWords);
        transposition(word, mapOfWords);

        //System.out.println(allWords.size() + " = " + insertion(word).size() + " + " + deletion(word).size() + " + " + substitution(word).size() + " + " + transposition(word).size());
        //System.out.println(allWords);
        //word.allWords = cr.inVocabulary(allWords);      //retains only the valid words
        //System.out.println(allWords);
        /**
         * CODE TO BE ADDED *
         */
        /**
         * handle insertions, deletions, etcetera *
         */
        return mapOfWords;
    }

    //P(word) = (#word)/(total #words)
    public double wordP(String word) {
        double p;
        p = cr.getFrequency(word);
        //System.out.println(p + " = word p ");

        return p;
    }

    //getconfusioncount / getcountcount = P
    public double confusionP(String error, String correct) {
        double p = 0;
        double count = cmr.getConfusionCount(error, correct);
        double allCount = cmr.getCountCount(error);
        if (count == 0) {
            p = 0;
        } else {
            p = count / allCount;
        }

        //System.out.println("\'" + error + "\'" + "= error en correct =" + "\'" + correct + "\'");
        //System.out.println(p + " = confusion error p " + count + " = count en countcount = " + allCount);
        return p;
    }

    public double noisyP(String word, String error, String correct) {
        double p;

        //System.out.println("  > " + word);
        p = nine * confusionP(error, correct) * wordP(word);
        //System.out.println(p + " = total p");
        return p;
    }

    /**
     * This method does all possible transpositions on a word.
     *
     * @param word The word for which transposition need to be done
     * @return allTranspositions The set with all strings transpositioned
     */
    public void transposition(String word, Map<String, Double> map) {
        String error;
        String correct;

        for (int i = 0; i < word.length(); i++) {
            for (int j = 0; j < word.length(); j++) {
                if (j == i) {
                    continue;
                }
                String tempWord = word;
                Character tempCharI = tempWord.charAt(i);
                Character tempCharJ = tempWord.charAt(j);
                if (!(tempCharI.equals(tempCharJ))) {
                    if (j < i) {
                        tempWord = tempWord.substring(0, j) + tempCharI + tempWord.substring(j + 1, i) + tempCharJ + tempWord.substring(i + 1);
                        //System.out.println(tempWord + " letter transpositioned are " + tempCharJ + " changed with " + tempCharI);
                    } else {
                        tempWord = tempWord.substring(0, i) + tempCharJ + tempWord.substring(i + 1, j) + tempCharI + tempWord.substring(j + 1);
                        // System.out.println(tempWord + " letter transpositioned are " + tempCharI + " changed with " + tempCharJ);
                    }

                    if (cr.inVocabulary(tempWord)) {        //woorden van 2 letters staan allemaal in woordenboek?
                        if (j < i) {
                            correct = tempCharI.toString() + tempCharJ.toString();
                            error = tempCharJ.toString() + tempCharI.toString();
                        } else {
                            correct = tempCharJ.toString() + tempCharI.toString();
                            error = tempCharI.toString() + tempCharJ.toString();
                        }

                        // System.out.println(cmr.getConfusionCount(error, correct) + " getal voor bovenstaand");
                        double p = noisyP(tempWord, error, correct);
                        double value = map.getOrDefault(tempWord, 0d);
                        map.put(tempWord, value + p);
                    }
                }
            }
        }
    }

    /**
     * This method does all possible substitutions on a word.
     *
     * @param word The word for which substitution need to be done
     * @return allSubstitutions The set with all strings of substitutions
     */
    public void substitution(String word, Map<String, Double> map) {
        String error;
        String correct;
        Character err;

        for (Character letter : ALPHABET) {
            for (int i = 0; i < word.length(); i++) {
                String tempWord = word;
                tempWord = tempWord.substring(0, i) + letter + tempWord.substring(i + 1);
                //System.out.println(tempWord + " letter substituted = " + letter);

                if (cr.inVocabulary(tempWord)) {        //woorden van 2 letters staan allemaal in woordenboek?
                    correct = letter.toString();
                    err = word.charAt(i);
                    error = err.toString();

                    if (!(error.equals(correct))) {
                        //sla over anders want dan a=a etc
                        //    System.out.println(error + "= error en correct =" + correct);
                        //   System.out.println(cmr.getConfusionCount(error, correct) + " getal voor bovenstaand");

                        double p = noisyP(tempWord, error, correct);
                        double value = map.getOrDefault(tempWord, 0d);
                        map.put(tempWord, value + p);
                    }
                }
            }
        }
    }

    /**
     * This method does all possible deletions on a word.
     *
     * @param word The word for which deletion need to be done
     *
     */
    public void deletion(String word, Map<String, Double> map) {
        String error;
        String correct;
        Character err;

        for (int i = 1; i <= word.length(); i++) {
            String tempWord = word;
            tempWord = tempWord.substring(0, i - 1) + tempWord.substring(i);
            // System.out.println(tempWord + " letter verwijderd");

//            if (tempWord.length() > 1) {              nodig?
            if (cr.inVocabulary(tempWord)) {
                if (i > 1) {
                    error = word.substring(i - 2, i - 1);
                } else {
                    error = " ";
                }
                correct = error;
                err = word.charAt(i - 1);
                error = error + err.toString();

                // System.out.println(error + "= error en correct =" + correct);
                //  System.out.println(cmr.getConfusionCount(error, correct) + " getal voor bovenstaand");
                double p = noisyP(tempWord, error, correct);
                double value = map.getOrDefault(tempWord, 0d);
                map.put(tempWord, value + p);
            }
            //          }
        }
    }

    /**
     * This method does all possible insertions on a word.
     *
     * @param word The word for which insertions need to be done
     * @return allInsertions The set with all strings of insertions
     */
    public void insertion(String word, Map<String, Double> map) {
        String error;
        String correct;
        Character err;

        for (Character letter : ALPHABET) {
            for (int i = 0; i <= word.length(); i++) {
                String tempWord = word;
                tempWord = tempWord.substring(0, i) + letter + tempWord.substring(i);
                //System.out.println(tempWord + " letter toegevoegd = " + letter);
                if (cr.inVocabulary(tempWord)) {
                    if (i != 0) {
                        err = tempWord.charAt(i - 1);
                        error = err.toString();
                    } else {
                        error = " ";
                    }
                    correct = error + letter;

                    // System.out.println(error + " = error en correct = " + correct);
                    //System.out.println(cmr.getConfusionCount(error, correct) + " getal voor bovenstaand");
                    double p = noisyP(tempWord, error, correct);
                    double value = map.getOrDefault(tempWord, 0d);
                    map.put(tempWord, value + p);
                }
            }
        }
    }
}
