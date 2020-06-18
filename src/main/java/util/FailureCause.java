package util;

public enum FailureCause{
    FILE_NOT_FOUND(1, "file not found"),
    HASH_NOT_MATCH(2, "hash not match"),
    ALREADY_EXIST(3, "already exist");

    int code;
    String message;

    FailureCause(int code, String message) {
        this.code = code;
        this.message = message;
    }
}