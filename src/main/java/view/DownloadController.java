package view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.http.client.fluent.Request;

import java.io.File;
import java.io.IOException;

public class DownloadController {
    String md5;
    String path;

    @FXML
    Label result;

    @FXML
    TextField textField;

    /**
     * @param pane The method will use the left side of the splitpane
     *             to build the interface for download .
     * @throws IOException When can't connect to the server correctly.
     * @version 1.0
     * @author Mou Ynagchen
     */
    public void Downloadout(SplitPane pane) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Download.fxml"));
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
    public void Handlefile(ActionEvent actionEvent) throws IOException{
        result.setText("");
        md5 = textField.getText().toUpperCase();
        System.out.println(md5);
        if(!md5.matches("[A-F0-9]{32}")){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("code doesn't match md5 form");
            alert.setContentText("please type as MD5 form");
            alert.showAndWait();
        }else {
            check();
        }
    }
    /**
     * This method used the Javalin to organise the command, it will
     * send the command end with "/key", the server will return a
     * string with the information about whether the file is successfully
     * download to the path.(the command will attach the output path in
     * the header "path")
     * <p>
     *     The key refers to the md5 code of the file. If the user type the
     *     path or the content directly it will invoke the Read in method
     *     to get the md5 code.It will print error massage if the
     *     files doesn't exist in the database.
     * </p>
     * @throws IOException throws when the client can' connect
     * to the server correctly.
     */
    private void check() throws IOException {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Choose a file directory as your download path");
            dc.setInitialDirectory(new File("C:" + File.separator));
            path = dc.showDialog(new Stage()).getPath().replace('\\', '/');
            System.out.println(path);
            String endpoint = "http://localhost:7001/files";
            String response = Request.Get(endpoint + "/" + md5).addHeader("path", path).execute().returnContent().asString();
            System.out.println(response + " " + response.length());
            if (response.length() == 67) {
                result.setText(" Illegal char in the path");
            } else {
                result.setText("         SUCCESS");
            }
            if (response.length() == 49) {
                result.setText("No such file in the database");
            }

    }
}
