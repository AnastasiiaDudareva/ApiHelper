package com.apihelper;

/**
 * Created by korotenko on 30.07.14.
 */
public class Error {
    int code = 0;
    String message = "";

    public Error() {
    }

    public int getCode() {
        return code;
    }

    public Error(String text) {
        this(0, text);
    }

    public Error(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
