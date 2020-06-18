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

public class CompareController {
    String md51;
    String md52;
    @FXML
    TextField textField1;
    @FXML
    TextField textField2;
    @FXML
    Label charset1;
    @FXML
    Label charset2;
    @FXML
    Label result;

    /**
     * @param pane The method will use the left side of the splitpane
     *             to build the interface for compare.
     * @throws IOException When can't connect to the server correctly.
     * @version 1.0
     * @author Mou Ynagchen
     */
    public void Compareout(SplitPane pane)throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Compare.fxml"));
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
        charset1.setText("");
        charset2.setText("");
        result.setText("");
        md51 = RootController.calculateMD5(textField1.getText().getBytes()).toUpperCase();
        md52 = RootController.calculateMD5(textField2.getText().getBytes()).toUpperCase();
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
            charset1.setText("");
            charset2.setText("");
            result.setText("");
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                    "txt files (*.txt)", "*.txt");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setTitle("Choose the first file");
            String path1 = fileChooser.showOpenDialog(new Stage()).getPath();
            fileChooser.setTitle("Choose the second file");
            String path2 = fileChooser.showOpenDialog(new Stage()).getPath();
            String[] out1 = RootController.Readin(path1);
            md51 = RootController.calculateMD5(out1[0].getBytes()).toUpperCase();
            System.out.println(out1[1]);
            charset1.setText(out1[1]);
            String[] out2 = RootController.Readin(path2);
            md52 = RootController.calculateMD5(out2[0].getBytes()).toUpperCase();
            System.out.println(out2[1]);
            charset2.setText(out2[1]);
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
     * Read md5 from text-field, if the form doesn't match the standard
     * show an alert
     * @throws IOException
     */
    @FXML
    private void HandleMd5() throws IOException{
        charset1.setText("");
        charset2.setText("");
        result.setText("");
        md51 = textField1.getText().toUpperCase();
        md52 = textField2.getText().toUpperCase();
        if(!md51.matches("[A-F0-9]{32}") || !md52.matches("[A-F0-9]{32}")){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("code doesn't match md5 form");
            alert.setContentText("please type as MD5 form");
            alert.showAndWait();
        }
        check();
    }

    /**
     * This method used the Javalin to organise the command, it will
     * send the command end with "/md51/compare/md52", the server will return a
     * string with the information about the simple difference and the
     * Levenshtein-distance.
     *  <p>
     *      The md51 and md52 refers to the md5 codes of the files. If the user
     *      type the path or the content directly it will invoke the Read
     *      in method to get the md5 code.It will print error massage if the
     *      files doesn't exist in the database.
     *  </p>
     * @throws IOException throws when the client can' connect to the server
     * correctly.
     */
    @FXML
    private void check() throws IOException {
        String endpoint = "http://localhost:7001/files";
        String response = Request.Get(endpoint + "/" + md51 + "/compare/" + md52).execute().returnContent().asString();
        if(response.charAt(8) == '0') {
            result.setText(response.substring(34, response.length() - 2));
        }else {
            result.setText("      No such file in the database");
        }
        System.out.println(response+" "+response.lastIndexOf(","));
    }
}
