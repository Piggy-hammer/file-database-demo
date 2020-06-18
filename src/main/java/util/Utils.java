package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {


    // source: https://www.baeldung.com/java-md5
    public static String calculateMD5(byte[] bytes){
        //TODO

        return null;
    }

    public static String calculateMD5(String str){
        return calculateMD5(str.getBytes(StandardCharsets.UTF_8));
    }
}
