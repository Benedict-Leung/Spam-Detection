package SpamDetection;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The Main.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();

        primaryStage.setTitle("Spam Detector");
        primaryStage.setResizable(false);
        root.getStylesheets().add("https://fonts.googleapis.com/css2?family=Montserrat:wght@400;700"); // Load font
        primaryStage.setScene(new Scene(root, 800, 675));
        primaryStage.show();

        controller.init(primaryStage);
        primaryStage.setOnCloseRequest(t -> controller.shutdown());
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
