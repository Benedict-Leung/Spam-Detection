package SpamDetection;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Controller.
 */
public class Controller {
    @FXML public TableView table;
    @FXML public TableColumn<TestFile, String> fileName, actualClass, spamProbability;
    @FXML public VBox container;
    double accuracy = 0, finalAccuracy = 0, precision = 0, finalPrecision = 0;
    double trainDistribution = 0, finalTrainDistribution = 0, testDistribution = 0, finalTestDistribution = 0;
    double frameIndex = 0;
    ExecutorService executorService = Executors.newFixedThreadPool(1);

    /**
     * Initializes the window
     *
     * @param primaryStage the primary stage
     */
    public void init(Stage primaryStage) {
        // Canvas
        Canvas canvas = new Canvas(800, 200);
        container.getChildren().add(canvas);

        // Initialize columns
        fileName.setCellValueFactory(new PropertyValueFactory<>("filename"));
        actualClass.setCellValueFactory(new PropertyValueFactory<>("actualClass"));
        spamProbability.setCellValueFactory( cellData -> new ReadOnlyStringWrapper(cellData.getValue().getSpamProbRounded()));

        // Initialize column size
        fileName.prefWidthProperty().bind(table.widthProperty().multiply(0.5));
        actualClass.prefWidthProperty().bind(table.widthProperty().multiply(0.20));
        spamProbability.prefWidthProperty().bind(table.widthProperty().multiply(0.20));

        fileName.setResizable(false);
        actualClass.setResizable(false);
        spamProbability.setResizable(false);

        // Ask for directory containing the train/test data
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("."));
        File mainDirectory = directoryChooser.showDialog(primaryStage);

        // Initialize graphics settings
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.web("#0077CA"));
        gc.setFill(Color.web("#0077CA"));
        gc.setFont(Font.font("Montserrat", FontWeight.BOLD, 15));
        gc.setLineWidth(5);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        SpamDetection detection = new SpamDetection(mainDirectory, table);

        // When running, show loading message
        detection.setOnRunning(successEvent -> gc.fillText("Loading...", canvas.getWidth() / 2, canvas.getHeight() / 2));

        // On success, show statistics
        detection.setOnSucceeded(succeededEvent -> {
            // Calculate accuracy and precision
            finalAccuracy = (double) (detection.truePositives + detection.trueNegatives) / (detection.getTestHamLength() + detection.getTestSpamLength());
            finalPrecision = (double) detection.truePositives / (detection.truePositives + detection.falsePositive);
            finalTrainDistribution = (double) detection.getTrainSpamLength() / (detection.getTrainHamLength() + detection.getTrainSpamLength());
            finalTestDistribution = (double) detection.getTestSpamLength() / (detection.getTestHamLength() + detection.getTestSpamLength());

            // Animation for showing the statistics
            Timeline timeline = new Timeline();
            timeline.setCycleCount(40);
            EventHandler<ActionEvent> eventHandler = event -> {
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()); // Clear the canvas
                accuracy += (finalAccuracy * 100 - accuracy) / (40 - frameIndex); // Slowly increment the display accuracy to the final accuracy
                precision += (finalPrecision * 100 - precision) / (40 - frameIndex); // Slowly increment the display precision to the final precision
                trainDistribution += (finalTrainDistribution * 100 - trainDistribution) / (40 - frameIndex); // Slowly increment the display train distribution to the final train distribution
                testDistribution += (finalTestDistribution * 100 - testDistribution) / (40 - frameIndex); // Slowly increment the display test distribution to the final test distribution
                frameIndex++; // Increment the frame index

                // Draw background for the statistics
                gc.setFill(Color.web("#FFFFFF"));
                gc.fillRoundRect(10, 10, 180, 180, 10, 10);
                gc.fillRoundRect(200, 10, 180, 180, 10, 10);
                gc.fillRoundRect(390, 10, 180, 180, 10, 10);
                gc.fillRoundRect(580, 10, 180, 180, 10, 10);

                // Display accuracy, precision and title of the statistics
                gc.setFill(Color.web("#E75D2A"));
                gc.fillText(String.format("%.1f", accuracy) + "%", 102, 114);
                gc.fillText(String.format("%.1f", precision) + "%", 292, 114);
                gc.fillText("Accuracy", 100,30);
                gc.fillText("Precision", 290,30);
                gc.fillText("Train Data Distribution", 480,30);
                gc.fillText("Test Data Distribution", 670,30);

                // Draw full gray circle
                gc.setStroke(Color.web("#F4F4F4"));
                gc.strokeOval(50, 65, 100, 100);
                gc.strokeOval(240, 65, 100, 100);

                // Draw arc based on the accuracy and precision
                gc.setFill(Color.web("#0077CA"));
                gc.setStroke(Color.web("#0077CA"));
                gc.setFont(Font.font("Montserrat", FontWeight.NORMAL, 15));
                gc.strokeArc(50, 65, 100, 100, 90, accuracy * 3.6, ArcType.OPEN);
                gc.strokeArc(240, 65, 100, 100, 90, precision * 3.6, ArcType.OPEN);

                // Display distribution
                gc.strokeOval(430, 65, 100, 100);
                gc.fillText("H: " + String.format("%.1f", 100 - trainDistribution) + "%",480, 105);
                gc.strokeOval(620, 65, 100, 100);
                gc.fillText("H: " + String.format("%.1f", 100 - testDistribution) + "%",670, 105);
                gc.setFill(Color.web("#E75D2A"));
                gc.setStroke(Color.web("#E75D2A"));
                gc.strokeArc(430, 65, 100, 100, 90, trainDistribution * 3.6, ArcType.OPEN);
                gc.fillText("S: " + String.format("%.1f", trainDistribution) + "%",480, 125);
                gc.strokeArc(620, 65, 100, 100, 90, testDistribution * 3.6, ArcType.OPEN);
                gc.fillText("S: " + String.format("%.1f", testDistribution) + "%",670, 125);
            };
            KeyFrame keyFrame = new KeyFrame(Duration.millis(50), eventHandler);
            timeline.getKeyFrames().add(keyFrame);
            timeline.playFromStart();
        });

        // Display error
        detection.setOnFailed(event -> {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.fillText("Can't find directory. Required Directories:\n 'test/ham', 'test/spam', 'train/ham', 'train/ham2', 'train/spam'", canvas.getWidth() / 2, canvas.getHeight() / 2);
        });
        executorService.execute(detection);
        executorService.shutdown();
    }

    /**
     * Shutdown.
     */
    public void shutdown() {
        System.exit(0);
    }
}
