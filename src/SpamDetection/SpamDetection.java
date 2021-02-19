package SpamDetection;

import javafx.concurrent.Task;
import javafx.scene.control.TableView;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * The Spam detection.
 */
public class SpamDetection extends Task {
    private final TableView table;
    private HashMap<String, Integer> trainHamFreq = new HashMap<>(), trainSpamFreq = new HashMap<>();
    ArrayList<String> stopWords = new ArrayList<>();
    int truePositives = 0, trueNegatives = 0, falsePositive = 0;
    File mainDirectory, trainHamFolder, trainHamFolder2, trainSpamFolder, testHamFolder, testSpamFolder;

    /**
     * Instantiates a new Spam detection.
     *
     * @param mainDirectory the main directory
     * @param table         the table
     */
    public SpamDetection(File mainDirectory, TableView table) {
        this.mainDirectory = mainDirectory;
        this.table = table;
    }

    /**
     * Initializes the spam detection by loading data and then calculates the spam probability
     *
     * @throws FileNotFoundException the file not found exception
     * @throws NullPointerException  the null pointer exception
     */
    public void init() throws FileNotFoundException, NullPointerException {
        loadStopWords(); // Load stop words
        // Get train directories
        trainHamFolder = new File(mainDirectory.getPath() + "/train/ham");
        trainHamFolder2 = new File(mainDirectory.getPath() + "/train/ham2");
        trainSpamFolder = new File(mainDirectory.getPath() + "/train/spam");

        // Load train data
        trainHamFreq = getTrainData(trainHamFolder);
        trainSpamFreq = getTrainData(trainSpamFolder);
        HashMap<String, Integer> ham2 = getTrainData(trainHamFolder2);
        ham2.forEach((key, value) -> trainHamFreq.merge(key, value, Integer::sum));

        // Get test files
        testHamFolder = new File(mainDirectory.getPath() + "/test/ham");
        testSpamFolder = new File(mainDirectory.getPath() + "/test/spam");

        // Calculate spam probability
        int trainSpamLength = getTrainSpamLength(), trainHamLength = getTrainHamLength();
        for (File file : testHamFolder.listFiles()) {
            double spamProbability = getSpamProbability(file, trainSpamLength, trainHamLength);
            TestFile testFile = new TestFile(file.getName(), spamProbability, "Ham");
            if (spamProbability < 0.5) {
                trueNegatives++;
            } else {
                falsePositive++;
            }
            table.getItems().add(testFile);
        }

        for (File file : testSpamFolder.listFiles()) {
            double spamProbability = getSpamProbability(file, trainSpamLength, trainHamLength);
            TestFile testFile = new TestFile(file.getName(), spamProbability, "Spam");
            if (spamProbability >= 0.5) {
                truePositives++;
            }
            table.getItems().add(testFile);
        }
    }

    /**
     * Calculates spam probability given a file.
     *
     * @param file       the file to be evaluated
     * @param spamLength the total spam files length
     * @param hamLength  the total ham files length
     * @return           the probability of a file is a spam
     * @throws FileNotFoundException the file not found exception
     */
    public double getSpamProbability(File file, int spamLength, int hamLength) throws FileNotFoundException {
        double n = 0;
        Scanner scanner = new Scanner(file);

        while (scanner.hasNext()) {
            String word = scanner.next().toLowerCase();

            // If the word contains only letters and is not a stop word
            if (word.matches("^[a-zA-Z]+$") && !stopWords.contains(word)) {
                // Gets the number of files containing the word
                double spamFreq = (trainSpamFreq.get(word) == null) ? 0 : trainSpamFreq.get(word);
                double hamFreq = (trainHamFreq.get(word) == null) ? 0 : trainHamFreq.get(word);

                // Check if the word appears in training phase
                if (spamFreq != 0 && hamFreq != 0) {
                    // Calculate probability, Pr(S|W)
                    double spamContainsWord = spamFreq / spamLength;
                    double hamContainsWord = hamFreq / hamLength;
                    double spamProbability = spamContainsWord / (spamContainsWord + hamContainsWord);
                    spamProbability = (2 + (spamFreq + hamFreq) * spamProbability) / (4 + spamFreq + hamFreq); // Corrected probability for rarity of the word
                    n += Math.log(1 - spamProbability) - Math.log(spamProbability);
                } else {
                    double spamProbability = 0.5;
                    n += Math.log(1 - spamProbability) - Math.log(spamProbability);
                }
            }
        }
        return 1 / (1 + Math.pow(Math.E, n));
    }

    /**
     * Gets train data.
     *
     * @param mainDirectory the main directory
     * @return a hashmap of number of files containing the word
     * @throws FileNotFoundException the file not found exception
     * @throws NullPointerException  the null pointer exception
     */
    public HashMap<String, Integer> getTrainData(File mainDirectory) throws FileNotFoundException, NullPointerException {
        HashMap<String, Integer> frequency = new HashMap<>();
        File[] folder = mainDirectory.listFiles();

        for (int i = 0; i < folder.length; i++) {
            Scanner scanner = new Scanner(folder[i]);

            while (scanner.hasNext()) {
                String word = scanner.next().toLowerCase();

                // If the word contains only letters
                if (word.matches("^[a-zA-Z]+$")) {
                    // Check if it exists. If it does, take the minimum between the current index and the current occurrences of the word. Otherwise, set to 1;
                    if (frequency.get(word) != null) {
                        frequency.put(word, Math.min(i + 1, frequency.get(word) + 1));
                    } else {
                        frequency.put(word, 1);
                    }
                }
            }
        }
        return frequency;
    }

    /**
     * Load stop words.
     *
     * @throws FileNotFoundException the file not found exception
     */
    public void loadStopWords() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File("src/SpamDetection/stopwords.txt"));
        while (scanner.hasNext()) {
            stopWords.add(scanner.next());
        }
    }

    /**
     * Gets the number train spam files.
     *
     * @return the number train spam files
     */
    public int getTrainSpamLength() {
        return trainSpamFolder.listFiles().length;
    }

    /**
     * Gets the number train ham files.
     *
     * @return the number train ham files
     */
    public int getTrainHamLength() {
        return trainHamFolder.listFiles().length + trainHamFolder2.listFiles().length;
    }

    /**
     * Gets the number test spam files.
     *
     * @return the number test spam files
     */
    public int getTestSpamLength() {
        return testSpamFolder.listFiles().length;
    }

    /**
     * Gets the number test ham files.
     *
     * @return the number test ham files
     */
    public int getTestHamLength() {
        return testHamFolder.listFiles().length;
    }

    @Override
    protected Object call() throws Exception {
        init();
        return null;
    }
}