package view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import org.apache.http.client.fluent.Request;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class is the central controller of the client GUI, it
 * will awake different controller according to the button clicked
 * in the left side(main menu)
 */
public class RootController {
    Stage stage;
    SplitPane pane;

    public void set(Stage stage, SplitPane pane) {
        this.stage = stage;
        this.pane = pane;
    }

    /**
     * Invoke the download method.
     * @throws IOException
     * @see DownloadController
     */
    @FXML
    private void handleDownload() throws IOException{
        DownloadController downloadController = new DownloadController();
        downloadController.Downloadout(pane);
    }

    /**
     * Invoke the upload method
     * @throws IOException
     * @see UploadController
     */
    @FXML
    private void handleUpload()throws IOException{
        UploadController uploadController = new UploadController();
        uploadController.Uploadout(pane);
    }

    /**
     * Invoke the list method
     * @throws IOException
     * @see ListController
     */
    @FXML
    private void handleList() throws IOException {
        ListController listController = new ListController();
        listController.Listout(pane);
    }

    /**
     * Invoke the exist method
     * @see ExistController
     * @throws IOException
     */
    @FXML
    private void handleExist()throws IOException{
        ExistController existController = new ExistController();
        existController.Existout(pane);
    }

    /**
     * Invoke the compare method
     * @throws IOException
     * @see CompareController
     */
    @FXML
    private void handleCompare()throws IOException{
        CompareController compareController = new CompareController();
        compareController.Compareout(pane);
    }

    /**
     * Invoke the delete method
     * @throws IOException
     * @see DeleteController
     */
    @FXML
    private void handleDelete() throws IOException{
        DeleteController deleteController = new DeleteController();
        deleteController.Deleteout(pane);
    }

    /**
     * If the file was offered by the file path, this method will turn it
     * to context in string. It can also detect the charset of the file,
     * print it, and turn it into UTF-8
     * @param arg the the whole command sent by main method
     * @return the context of the file
     */
    public static String[] Readin(String  arg){
        String [] r = new  String [2];
        r[1] = "";
        try {
                File file = new File(arg);
                CharsetDetector detector = new CharsetDetector();
                byte[] bytes = Files.readAllBytes(file.toPath());
                detector.setText(bytes);
                CharsetMatch charsetMatch = detector.detect();
                byte[] utf8bytes = charsetMatch.getString().getBytes(StandardCharsets.UTF_8);
                boolean withBoom = false;
                if(utf8bytes.length >= 3 && utf8bytes[0] == -17 && utf8bytes[1] == -69 && utf8bytes[2] == -65){
                    withBoom = true;
                    byte[] temp = new byte[utf8bytes.length - 3];
                    System.arraycopy(utf8bytes,3,temp,0,temp.length);
                    utf8bytes = temp;
                }
                if (withBoom) {
                    r[1] = "detected charset: " + charsetMatch.getName() + "-BOOM";
                }else {
                    r[1] = "detected charset: " + charsetMatch.getName();
                }
                r[0] = new String(utf8bytes,StandardCharsets.UTF_8);
                return r;
        }catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not find data");
            alert.setContentText("Could not find data in file:\n" + arg);
            alert.showAndWait();
        }
        return null;
    }

    /**
     * If the file was provided by directly typed or offering the file path
     * , the method will transfer the context to MD5 code.
     * @param bytes the content in the form of byte
     * @return the MD5 code of content
     */
    public static String calculateMD5(byte[] bytes){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            return new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) { }
        return null;
    }
}
