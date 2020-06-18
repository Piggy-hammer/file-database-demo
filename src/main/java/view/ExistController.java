package view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jdk.internal.org.objectweb.asm.Handle;
import org.apache.http.client.fluent.Request;

import java.io.IOException;

public class ExistController {
    String md5;

    @FXML
    Button file;
    @FXML
    TextField textArea;
    @FXML
    Label result;
    @FXML
    Label charset;

    /**
     * @param pane The method will use the left side of the splitpane
     *             to build the interface for exist.
     * @throws IOException When can't connect to the server correctly.
     * @version 1.0
     * @author Mou Ynagchen
     */
    public void Existout(SplitPane pane)throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Exist.fxml"));
        System.out.println(loader.getLocation());
        AnchorPane paneL = loader.load();
        pane.getItems().set(1,paneL);
    }

    /**
     * Read md5 from text-field, if the form doesn't match the standard
     * show an alert
     * @throws IOException
     */
    @FXML
    private void HandleMd5() throws IOException {
        //ExistController controller = loader.getController();
        charset.setText("");
        result.setText("");
        md5 = textArea.getText().toUpperCase();
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
     * Transfer the String in the text field into the context.
     * @throws IOException
     */
    @FXML
    private void Handletype() throws IOException {
        charset.setText("");
        result.setText("");
        md5 = RootController.calculateMD5(textArea.getText().getBytes()).toUpperCase();
        System.out.println(md5);
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
            md5 = RootController.calculateMD5(out[0].getBytes()).toUpperCase();
            System.out.println(out[1]);
            charset.setText(out[1]);
            check();
        }catch(NullPointerException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No txt file was chosen");
            alert.setContentText("please choose your file");
            alert.showAndWait();
        }
    }


    /**
     * This method used the Javalin to organise the command, it will
     * send the command end with "/key/exist", the server will return a
     * string with the information about whether the file exists
     * <p>
     *     The key refers to the md5 code of the file. If the user type the
     *     path or the content directly it will invoke the Read in method
     *     to get the md5 code.
     * </p>
     * @throws IOException throws when the client can' connect
     * to the server correctly.
     */
    @FXML
    private void check() throws IOException {
        String endpoint = "http://localhost:7001/files";
        String response = Request.Get(endpoint + "/" + md5 + "/exists").execute().returnContent().asString();
        System.out.println(response.length());
        if (response.length() == 49){
         result.setText("FALSE");
        }else {
            result.setText("TRUE");
        }
    }
}

