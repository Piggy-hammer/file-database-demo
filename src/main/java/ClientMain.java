
import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import view.RootController;

import java.io.File;
import java.io.IOException;

/**
 * This cass is a basic client with a GUI helper.
 * <p>
 *     you can click the buttons on the left side to choose the functions
 *     and type the information into the text field.
 * </p>
 */
public class ClientMain extends Application {
    private SplitPane rootlayout;
    private Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * This is the starting method which use the ClientGUI.fxml to build
     * the root layout on the primary stage.
     * @param primaryStage
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            stage = primaryStage;
            stage.setTitle("Database client controller");
            FXMLLoader loader = new FXMLLoader(ClientMain.class.getResource("ClientGUI.fxml"));
            rootlayout = loader.load();
            Scene scene = new Scene(rootlayout);
            stage.setScene(scene);
            RootController rootController = loader.getController();
            rootController.set(stage,rootlayout);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
