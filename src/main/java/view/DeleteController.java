package view;

import javafx.event.ActionEvent;
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

public class DeleteController {
    String md5;
    @FXML
    Label result;

    @FXML
    TextField textField;

    @FXML
    Label charset;
    /**
     * @param pane The method will use the left side of the splitpane
     *             to build the interface for delete.
     * @throws IOException When can't connect to the server correctly.
     * @version 1.0
     * @author Mou Ynagchen
     */
    public void Deleteout(SplitPane pane) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Delete.fxml"));
        System.out.println(loader.getLocation());
        AnchorPane pane1 = loader.load();
        pane.getItems().set(1,pane1);
    }
    /**
     * Read md5 from text-field, if the form doesn't match the standard
     * show an alert
     * @throws IOException
     */
    @FXML
    private void HandleMd5() throws IOException {
        charset.setText("");
        result.setText("");
        md5 = textField.getText().toUpperCase();
        System.out.println(md5);
        if(!md5.matches("[A-F0-9]{32}")){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("code doesn't match md5 form");
            alert.setContentText("please type as MD5 form");
            alert.showAndWait();
        }
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
            fileChooser.setTitle("Choose the file");
            String path1 = fileChooser.showOpenDialog(new Stage()).getPath();
            System.out.println(path1);
            String[] out = RootController.Readin(path1);
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
     * Transfer the String in the text field into the context.
     * @throws IOException
     */
    @FXML
    private void Handletype() throws IOException {
        charset.setText("");
        result.setText("");
        md5 = RootController.calculateMD5(textField.getText().getBytes()).toUpperCase();
        System.out.println(md5);
        check();
    }

    /**
     * send the code of delete all the files to the server
     * @throws IOException
     */
    @FXML
    private void Handleall() throws IOException {
        charset.setText("");
        result.setText("");
        md5 = "all*";
        check();
    }
    /**
     * This method used the Javalin to organise the command, it will
     * send the command end with "/key/delete", the server will return a
     * string with the information about whether the operation is correctly
     * executed.
     * <p>
     *     The key refers to the md5 code of the file. If the user type
     *     the content or ues filechooser directly it will invoke the Read
     *     in method to get the md5 code.
     * </p>
     * @throws IOException throws when the client can' connect
     * to the server correctly.
     */
    private void check() throws IOException {
        String endpoint = "http://localhost:7001/files";
        String response = Request.Delete(endpoint + "/" + md5 + "/delete").execute().returnContent().asString();
        if (response.charAt(8) == '0'){
            result.setText("              SUCCESS");
        }else {
            result.setText("No such file in the database");
        }
    }
}
