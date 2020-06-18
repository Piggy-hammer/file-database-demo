package view;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import org.apache.http.client.fluent.Request;


import java.io.IOException;

public class ListController {
    @FXML
    TextArea textArea;

    /**
     * This method used the Javalin to organise the command, it will
     *  send the command end with "/list", the server will return a
     *  string with the information about all the files in json(including
     *  md5, file length, and a overview within 100 words)

     * @param pane The method will use the left side of the splitpane
     * to build the interface for list.
     * @throws IOException When can't connect to the server correctly.
     * @version 1.0
     * @author Mou Ynagchen
     */
    public void Listout( SplitPane pane) throws IOException {
        String endpoint = "http://localhost:7001/files";
        String response = Request.Get(endpoint+"/list").execute().returnContent().asString();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode res = objectMapper.readTree(response);
        String out = res.toPrettyString();
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("LIST.fxml"));
        AnchorPane paneL = loader.load();
        ListController listController = loader.getController();
        listController.setTXET(out.substring(55,out.length()-6));
        pane.getItems().set(1,paneL);
    }


    /**
     * Show the result on the text-area
     * @param substring the result
     */
    private void setTXET(String substring) {
        textArea.setText(substring);
    }
}
