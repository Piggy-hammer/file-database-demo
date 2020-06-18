package view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.http.client.fluent.Request;

import java.io.IOException;

public class UploadController {
    String md5;
    byte [] bytes;
    @FXML
    TextField textField;
    @FXML
    Label result;
    @FXML
    Label charset;

    /**
     * @param pane The method will use the left side of the splitpane
     *             to build the interface for upload.
     * @throws IOException When can't connect to the server correctly.
     * @version 1.0
     * @author Mou Ynagchen
     */
    public void Uploadout(SplitPane pane) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Upload.fxml"));
        System.out.println(loader.getLocation());
        AnchorPane paneL = loader.load();
        pane.getItems().set(1,paneL);
    }

    /**
     * Transfer the String in the text field into the context.
     * @throws IOException
     */
    @FXML
    private void Handletype() throws IOException {
        charset.setText("");
        result.setText("");
        md5 = RootController.calculateMD5(textField.getText().getBytes()).toUpperCase();
        System.out.println(md5);
        bytes = textField.getText().getBytes();
        check();
    }

    /**
     * Read the file from the path from the file chooser.
     * @throws IOException
     * @see RootController#Readin(String)
     */
    @FXML
    private void Handlefile() throws IOException {
        try {
            charset.setText("");
            result.setText("");
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                    "txt files (*.txt)", "*.txt");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setTitle("Choose the first file");
            String path1 = fileChooser.showOpenDialog(new Stage()).getPath();
            System.out.println(path1);
            String[] out = RootController.Readin(path1);
            bytes = out[0].getBytes();
            md5 = RootController.calculateMD5(out[0].getBytes()).toUpperCase();
            System.out.println(out[1]);
            charset.setText(out[1]);
            check();
        }catch (NullPointerException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No txt file was chosen");
            alert.setContentText("please choose your file");
            alert.showAndWait();
        }
    }

    /**
     * This method used the Javalin to organise the command, it will
     * send the command end with "/key", the server will return a
     * string with the information about whether the file is successfully
     * uploaded to the database.(the command will attach the content
     * of the file as bytes in the body of request)
     * <p>
     *     The key refers to the md5 code of the file. If the user type the
     *     path or the content directly it will invoke the Read in method
     *     to get the md5 code.It will print error massage if the
     *     files already exist in the database or the md5 doesn't match
     *     with the file content.
     * </p>
     * @throws IOException throws when the client can' connect
     * to the server correctly.
     */
    private void check() throws IOException {
        String endpoint = "http://localhost:7001/files";
        String response = Request.Post(endpoint + "/" + md5).bodyByteArray(bytes).execute().returnContent().asString();
        if(response.length() == 48){
            result.setText("File already exists");
        } else result.setText("   SUCCESS");
        System.out.println(response + " " + response.length());
    }
}
