
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.fluent.Request;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;


import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;


/**
 * This cass is a basic client with no GUI helper.
 * <p>
 * The class is used for Client with basic functions (in
 * Operation enum) with simple Scanner to interact with users.
 * To tranfer the command and file data, users can type the file path
 * for this class to read, or type the md5 code or the content diretly.
 * (Notice :if you wanna type the content directly, you may have to
 * add the word "type " before the content.)
 * </p>
 * @see Operation
 *
 * @version 1.0
 * @author Mou Ynagchen
 */
public class Client{

    static String endpoint = "http://localhost:7001/files";


    /**
     * This enum class describes the different operations. When users
     * try to use them, they need to type the type of operation before
     * the instruction.
     */
    enum Operation{
        UPLOAD, DOWNLOAD, COMPARE, EXISTS, DELETE, LIST
    }

    /**
     * This method is used to transfer the String for the operations
     * to enum type.
     *
     * @param op  The word users typed to assign the operation type
     * @return The corresponding operation type in the enum class.
     */
    public static Operation parseOperation(String op){
        //TODO: convert String to Operation
        String ope = op.trim().toUpperCase();
        try {
            return Enum.valueOf(Operation.class,ope);
        }catch (IllegalArgumentException e){
            return null;
        }
    }

    /**
     * This method is the main process
     * <p>
     * The mission of the method is to read the command for scanner
     * and choose the operation types according the first word of
     * the command.
     * </p>
     * It may also pass the whole command (split with blank-space) to the
     * handle-Operation methods. If the command doesn't calls it will invoke
     * printUsage method to print the list of operations
     * @see #handleList()
     * @see #handelDlete(String[])
     * @see #handleCompare(String[])
     * @see #handleDownload(String[])
     * @see #handleExists(String[])
     * @see #handleUpload(String[])
     * @see #printUsage()
     */
    public static void main(String[] args) {

        while(true) {
            Scanner in = new Scanner(System.in);
            args = in.nextLine().split("\\s+");

            if (args.length < 2 && !args[0].toUpperCase().equals("LIST")) {
                System.out.println("Simple Client");
                printUsage();
                return;
            }

            Operation operation = parseOperation(args[0]);
            if (operation == null) {
                System.err.println("Unknown operation");
                printUsage();
                return;
            }
            try {

                switch (operation) {
                    case UPLOAD:
                        handleUpload(args);
                        break;
                    case DOWNLOAD:
                        handleDownload(args);
                        break;
                    case COMPARE:
                        handleCompare(args);
                        break;
                    case EXISTS:
                        handleExists(args);
                        break;
                    case DELETE:
                        handelDlete(args);
                        break;
                    case LIST:
                        handleList();
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     *  This method used the Javalin to organise the command, it will
     *  send the command end with "/list", the server will return a
     *  string with the information about all the files in json(including
     *  md5, file length, and a overview within 100 words)
     * <p>
     *      we will ues ObjectMapper and JsonNode to make the information
     *      readable and print it.It will print error massage if the file
     *      doesn't exist.
     *  </p>
     * @throws IOException throws when the client can' connect
     * to the server correctly.
     */
    private static void handleList() throws IOException {
        String response = Request.Get(endpoint+"/list").execute().returnContent().asString();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode res = objectMapper.readTree(response);
        System.out.println(res.toPrettyString());
    }

    /**
     * This method used the Javalin to organise the command, it will
     * send the command end with "/key/delete", the server will return a
     * string with the information about whether the operation is correctly
     * executed.
     * <p>
     *     The key refers to the md5 code of the file. If the user type the
     *     path or the content directly it will invoke the Read in method
     *     to get the md5 code.
     * </p>
     * @param args the whole command sent by main method
     * @throws IOException throws when the client can' connect
     * to the server correctly.
     * @see #Readin(String[])
     */
    private static void handelDlete(String[] args) throws IOException {
        try {
            String key;
            if (args[1].toLowerCase().equals("all")) {
                key = "all*";
            } else {
                if (args[1].toLowerCase().matches("[a-f0-9]{32}")) {
                    key = args[1].toUpperCase();
                }else {
                    byte[] bytes = Readin(args).getBytes(StandardCharsets.UTF_8);
                    key = calculateMD5(bytes).toUpperCase();
                }
            }
            System.out.println(key);
            String response = Request.Delete(endpoint + "/" + key + "/delete").execute().returnContent().asString();
            System.out.println(response);
        }catch (NullPointerException e){ }
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
     * @param args the whole command sent by main method
     * @throws IOException throws when the client can' connect
     * to the server correctly.
     * @see #Readin(String[])
     */
    private static void handleExists(String[] args) throws IOException {
        try {
            String key = "";
            if (args[1].toLowerCase().matches("[a-f0-9]{32}")) {
                key = args[1].toUpperCase();
            } else {
                byte[] bytes = Readin(args).getBytes(StandardCharsets.UTF_8);
                key = calculateMD5(bytes).toUpperCase();
            }
            System.out.println(key);
            String response = Request.Get(endpoint + "/" + key + "/exists").execute().returnContent().asString();
            System.out.println(response);
        }catch (NullPointerException e){}
    }

    /**
     * This method used the Javalin to organise the command, it will
     * send the command end with "/k1/compare/k2", the server will return a
     * string with the information about the simple difference and the
     * Levenshtein-distance.
     *  <p>
     *      The k1 and k2 refers to the md5 codes of the files. If the user
     *      type the path or the content directly it will invoke the Read
     *      in method to get the md5 code.It will print error massage if the
     *      files doesn't exist in the database.
     *  </p>
     * @param args the whole command sent by main method
     * @throws IOException throws when the client can' connect
     * to the server correctly.
     * @see #Readin(String[])
     */
    private static void handleCompare(String[] args) throws IOException {
        try {

            String k1, k2;
            int add = 0;
            if (args[1].toLowerCase().matches("[a-f0-9]{32}")) {
                k1 = args[1].toUpperCase();
            } else {
                String[] strings = new String[3];
                if (args[1].toLowerCase().equals("type")) {
                    strings[0] = "compare";
                    strings[1] = "type";
                    strings[2] = args[2];
                    add = 1;
                } else {
                    strings[0] = "compare";
                    strings[1] = args[1];
                }
                byte[] bytes = Readin(strings).getBytes(StandardCharsets.UTF_8);
                k1 = calculateMD5(bytes).toUpperCase();
            }
            if (args[2+add].toLowerCase().matches("[a-f0-9]{32}")) {
                k2 = args[2 + add].toUpperCase();
            } else {
                String[] strings = new String[3];
                if (args[2+add].toLowerCase().equals("type")) {
                    strings[0] = "compare";
                    strings[1] = "type";
                    strings[2] = args[3 + add];
                } else {
                    strings[0] = "compare";
                    strings[1] = args[2 + add];
                }
                byte[] bytes = Readin(strings).getBytes(StandardCharsets.UTF_8);
                k2 = calculateMD5(bytes).toUpperCase();
            }
            String response = Request.Get(endpoint + "/" + k1 + "/compare/" + k2).execute().returnContent().asString();
            System.out.println(response);
        }catch (NullPointerException e ){}
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
     * @param args the whole command sent by main method
     * @throws IOException throws when the client can' connect
     * to the server correctly.
     * @see #Readin(String[])
     */
    private static void handleDownload(String[] args) throws IOException {
        if(!args[1].toLowerCase().matches("[a-f0-9]{32}")){
            System.err.println("please type the right md5 code");
        }else {
            String path = "";
            if(args.length > 2){
                path = args[2];
            }
            String response = Request.Get(endpoint + "/" + args[1]).addHeader("path",path).execute().returnContent().asString();
            System.out.println(response);
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
     * @param args the whole command sent by main method
     * @throws IOException throws when the client can' connect
     * to the server correctly.
     * @see #Readin(String[])
     */
    private static void handleUpload(String[] args) throws IOException {
        try {
            byte[] bytes = Readin(args).getBytes(StandardCharsets.UTF_8);
            String key = calculateMD5(bytes).toUpperCase();
            System.out.println(key);
            String response = Request.Post(endpoint + "/" + key).bodyByteArray(bytes).execute().returnContent().asString();
            System.out.println(response);
        }catch (NullPointerException e){}
    }

    /**
     * If the command of user sent to scanner isn't readable or does't
     * match to any operation, this method will print the list of
     * available operations and the correct command form.
     */
    private static void printUsage() {
        System.out.println("Usage: [op] [params]");
        System.out.println("Available Operation: upload, download, compare, exists, delete");
    }

    /**
     * If the file was provided by directly typed or offering the file path
     * , the method will transfer the context to MD5 code.
     * @param bytes the content in the form of byte
     * @return the MD5 code of content
     */
    // source: https://www.baeldung.com/java-md5
    public static String calculateMD5(byte[] bytes){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            return new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) { }
        return null;
    }

    /**
     * If the file was offered by the file path, this method will turn it
     * to context in string. It can also detect the charset of the file,
     * print it, and turn it into UTF-8
     * @param arg the the whole command sent by main method
     * @return the context of the file
     */
    private static String Readin(String [] arg){
        String [] r = new  String [2];
        try {
            if (!arg[1].toLowerCase().equals("type")){
                File file = new File(arg[1]);
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
                    System.out.println("detected charset: " + charsetMatch.getName() + "-BOOM");
                }else {
                    System.out.println("detected charset: " + charsetMatch.getName());
                }
                return new String(utf8bytes,StandardCharsets.UTF_8);
            }else {
                return arg[2];
            }
        }catch (IOException e) {
            System.err.println("No such file in the file path");
        }
        return null;
    }
}
