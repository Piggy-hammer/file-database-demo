package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.TextDao;
import io.javalin.http.Context;
import model.Document;
import util.FailureCause;
import util.FailureResponse;
import util.Response;
import util.SuccessResponse;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class is the analyzer of the server system
 *
 * <p>
 *     When any of its method is waken up by the server controller
 *     it will separate the parameters from the command and pass them
 *     to the TextDao. After get the response form the TextDao it
 *     will package it into Response case in util package and pass it
 *     to client.
 * </p>
 * @see TextDao
 * @see Response
 */
public class TextService {

    TextDao dao;

    /**
     * This constructor will build a TextService object with a TexDao
     * (a method for connecting to database) attaching to it.
     *
     * @param dao The database connector
     */
    public TextService(TextDao dao) {
        this.dao = dao;
    }

    /**
     * This method will awake the list method in TextDao with no parameter
     * attach to it.
     * <p>
     *     The method in TextDao will return a list containing the DocumentAbstract
     *     , once this method receive the list, it will package it into a ObjectMapper
     *     and send it back to server.
     * </p>
     * @param ctx The request message from client.
     * @see TextDao#List()
     */
    public void handleList(Context ctx) {
        Response response = new SuccessResponse();
        response.setCode(0).setMessage("").getResult().set("files",new ObjectMapper().valueToTree(dao.List()));
        ctx.res.setCharacterEncoding("UTF-8");
        ctx.json(response);
    }

    /**
     * This method will awake the Exist method in TextDao with a md5 code string
     * of the file being checked attaching to it as a parameter.
     * <p>
     *     The method in TextDao will return a boolean to tell whether the file
     *     exist in the database.It will than be packaged into Response.
     * </p>
     * @param ctx The request message from client.
     * @see TextDao#Exists(String)
     */
    public void handleExists(Context ctx){
        String key = ctx.pathParam("md5");
        Response response;
        response = new SuccessResponse();
        response.setCode(0).setMessage("").getResult().put("exists",dao.Exists(key));
        ctx.json(response);
    }

    /**
     * This method will awake the Insert method in TextDao with a md5 code String
     * and a context String attaching to it as parameter
     * <p>
     *     The method will check if the md5 code matches the file context, and send
     *     a Hash not match case back to server when the conflict appears. If they
     *     pass the exam, but this file has been found in the database it may receive
     *     a false from the TextDao.Insert, the FailureResponse of "File already Exist"
     *     will be sent to client.Or it will send back a successful response.
     * </p>
     * @param ctx The request message from client.
     * @see TextDao#Insert(String, String)
     * @see #calculateMD5(byte[]) 
     */
    public void handleUpload(Context ctx)  {
        String key = ctx.pathParam("md5");
        String text = ctx.body();
        String ckey = calculateMD5(text.getBytes(StandardCharsets.UTF_8));
        if (!ckey.equals(key.toLowerCase())){
            ctx.json(new FailureResponse(FailureCause.HASH_NOT_MATCH));
            return;
        }
        Response response;
        if (dao.Insert(key,text)){
            response = new SuccessResponse();
            response.setCode(0).getResult().put("success",true);
            ctx.json(response);
        }else {
            response = new FailureResponse(FailureCause.ALREADY_EXIST);
            ctx.json(response);
        }
    }

    /**
     * This method will awake the Download method in TextDao with a md5 code 
     * and a output path String in header "path" attaching to it as parameters. 
     * <p>
     *     The method in TextDao will try to download the file to the output 
     *     path . If there is no such file in the database, it will return 
     *     the FailureResponse of "File no found".
     * </p>
     * @param ctx The request message from client.
     * @see TextDao#Download(String, String)
     */
    public void handleDownload(Context ctx){
        String key = ctx.pathParam("md5");
        String path = ctx.header("path");
        Response response;
        String a = dao.Download(key,path);
        if(a != null){
            if (a.equals("Can't find the file output path.")){
                response = new FailureResponse(4,"Can't find the file output path.");
                ctx.json(response);
            }else {
                response = new SuccessResponse();
                response.setCode(0).getResult().put("content", a);
                ctx.res.setCharacterEncoding("UTF-8");
                ctx.json(response);
            }
        }else {
            response = new FailureResponse(FailureCause.FILE_NOT_FOUND);
            ctx.json(response);
        }
    }

    /**
     * This method will awake the Compare method in TextDao with two md5 code 
     * attaching to it as parameters. 
     * <p>
     *     The method in TextDao will return two string of these corresponding
     *     context. If there is no such file in the database, it will return 
     *     the FailureResponse of "File no found".If the exam is passed, then
     *     it will compare the context sent by the TextDao.Compare, and sent
     *     the results of the calculate methods back to client.
     * </p>
     * @param ctx The request message from client.
     * @see TextDao#Compare(String, String) 
     */
    public void handleCompare(Context ctx){
        String k1 = ctx.pathParam("md51");
        String k2 = ctx.pathParam("md52");
        String [] strings = dao.Compare(k1,k2);
        Response response;
        if(strings == null){
            ctx.json(new FailureResponse(FailureCause.FILE_NOT_FOUND));
            return;
        }
        response = new SuccessResponse();
        int simpleTheSame = 0;
        String word1 = strings[0]; String word2 = strings[1];
        for (int t = 0; t < Math.min(word1.length(),word2.length()); t++){
            if(word1.charAt(t) == word2.charAt(t)) simpleTheSame++;
        }
        float simpleDistance =(float) ((int) ((float)simpleTheSame / Math.max(word1.length(),word2.length()) * 1000)) / 1000;
        int[][] dp = new int[word1.length()+1][word2.length()+1];
        for(int i = 0; i< word1.length() + 1; i++){
            dp[i][0] = i;
        }
        for(int j = 0; j< word2.length() + 1; j++){
            dp[0][j] = j;
        }
        for(int i = 1; i< word1.length() + 1; i++){
            for(int j = 1; j< word2.length() + 1; j++){
                if(word1.charAt(i - 1) == word2.charAt(j - 1)){
                    dp[i][j] = dp[i - 1][j - 1];
                }else{
                    dp[i][j] = (Math.min(Math.min(dp[i-1][j], dp[i][j-1]), dp[i-1][j-1])) + 1;
                }
            }
        }
        response.setCode(0).getResult().put("simple_similarity" , simpleDistance).put("levenshtein_distance",dp[word1.length()][word2.length()]);
        ctx.json(response);
    }

    /**
     * This method will awake the Delete method in TextDao with the md5 code 
     * of the particular file attaching to it as parameters. 
     * <p>
     *     The method in TextDao will send back the boolean value about whether
     *     the delete action is successfully executed in the database.If it returns
     *     the true, it will send back the message of "Success", otherwise it may
     *     return the FailureResponse of "File not founded".
     * </p>
     * @param ctx The request message from client.
     * @see TextDao#Delete(String) 
     */
    public void handleDelete(Context ctx) {
        String key = ctx.pathParam("md5");
        Response response;
        if (dao.Delete(key)){
            response = new SuccessResponse();
            response.setCode(0).getResult().put("success",true);
            ctx.json(response);
        }else {
            response = new FailureResponse(FailureCause.FILE_NOT_FOUND);
            ctx.json(response);
        }
    }

    /**
     * This method will calculate the md5 code form the bytes of context to
     * help the upload method to check if the md5 code matches the context.
     * @param bytes The byte array of the context string  
     * @return The md5 code of context 
     */
    public String calculateMD5(byte[] bytes){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            return new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) { }
        return null;
    }

}
