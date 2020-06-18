package dao;

import model.DocumentAbstract;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import model.Document;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class TextDao {

    Sql2o sql2o;

    /**
     *This method will construct a sql2o frame to connect the sqlite database.
     * @throws ClassNotFoundException When can not find the class of sqlite
     */
    public TextDao() throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        this.sql2o = new Sql2o("jdbc:sqlite:myc.db", null, null);
        String initSql = "create table if not exists \"docs\" (\n" +
                "\t\"docName\"\ttext not null unique,\n"+
                "\t\"docText\"\ttext not null\n"+
                ")";
        try (Connection connection = sql2o.open()){
            connection.createQuery(initSql).executeUpdate();
        }
    }

    /**
     * The compare method will get the context from the database and return
     * to the TextService.handleCompare(analyst).If there is no such string
     * it will return null.
     * @param k1 The md5 code for the first file
     * @param k2 The md5 code for the second file
     * @return the context of tow files
     */
    public String[] Compare(String k1, String k2){
        Connection connection = sql2o.open();
        String [] strings = new String[2];
        try {
            strings[0] = connection.createQuery("select docName , docText from docs where docName = :key")
                    .addParameter("key",k1)
                    .executeAndFetchFirst(Document.class)
                    .getText();
            strings[1] = connection.createQuery("select docName , docText from docs where docName = :key")
                    .addParameter("key",k2)
                    .executeAndFetchFirst(Document.class)
                    .getText();
            return strings;
        }catch (NullPointerException e){
         return null;
        }
    }

    /**
     * This method will get a list of the files from the database now, and
     * return it back to the TextService.
     * @return A list containing the DocumentAbstract
     * @see DocumentAbstract
     */
    public List<DocumentAbstract> List(){
        Connection connection = sql2o.open();
        List<Document> documentList =  connection.createQuery("select docName , docText from docs").executeAndFetch(Document.class);
        List<DocumentAbstract> List = new LinkedList<>();
        for (Document e: documentList
             ) {
            List.add(new DocumentAbstract(e));
        }
        return List;
    }

    /**
     * This method will return a boolean to claim whether the file exists
     * in the database.
     * @param key The md5 code of the checked file.
     * @return a boolean to claim whether the file exists
     * in the database.
     */
    public boolean Exists(String key){
        Connection connection = sql2o.open();
        Document r = connection.createQuery("select docName from docs where docName = :key")
                .addParameter("key",key)
                .executeAndFetchFirst(Document.class);
        if (r != null){
            return true;
        }else return false;
    }

    /**
     * This method will return a boolean to claim whether the file already
     * exists in the database.(True for no repetition)
     * @param key The md5 code of the uploaded file
     * @param text the context of the uploaded file
     * @return A boolean to claim whether the file already exists
     * in the database.
     */
    public boolean Insert(String key, String text){
        Connection connection = sql2o.open();
        if(connection.createQuery("select docName from docs where docName = :key")
                .addParameter("key",key)
                .executeAndFetchFirst(Document.class) != null){
            return false;
        }else {
            connection.createQuery("insert into docs (docName, docText) values (:name, :text)")
                    .addParameter("name", key)
                    .addParameter("text", text)
                    .executeUpdate();
            return true;
        }
    }

    /**
     * This method is will return whether the chosen file is deleted from
     * the database.(True for success, False for no such file in the database)
     * @param key The md5 code of the target file.
     * @return A boolean to claim whether the file is successfully deleted
     */
    public boolean Delete(String key){
        Connection connection = sql2o.open();
        if (key.toLowerCase().equals("all*")) {
            connection.createQuery("DELETE FROM docs").executeUpdate();
            return true;
        }
        if (connection.createQuery("select docName from docs where docName = :key")
                .addParameter("key",key)
                .executeAndFetchFirst(Document.class) != null){
            connection.createQuery("DELETE FROM docs WHERE docName = :key")
                    .addParameter("key",key)
                    .executeUpdate();
            return true;
        }else return false;
    }

    /**
     * This method will return a String to describe the result of the
     * action(null for no such file,  "Can't find the file output path."
     * for illegal download path)
     * @param key The md5 code for the file.
     * @param path The download path of this file.
     * @return A String to describe the result of the action
     */
    public String Download(String key,String path){
        try {
            Connection connection = sql2o.open();
            String context = connection.createQuery("select docName , docText from docs where docName = :key")
                    .addParameter("key",key)
                    .executeAndFetchFirst(Document.class)
                    .getText();
            String filePath = path + "/" + key + ".txt";
            if (path.equals("")) filePath = "./" + key + ".txt";
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream,StandardCharsets.UTF_8);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(context);
            bufferedWriter.flush();
            fileOutputStream.close();outputStreamWriter.close();bufferedWriter.close();
            return context;
        }catch (NullPointerException e){
            return null;
        }catch (FileNotFoundException e){
            return "Can't find the file output path.";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
