package org.inexture.common;

public class MyException extends RuntimeException {
    public MyException() {
    }

    public MyException(String errorMessage) {
        super(errorMessage);
    }

    public MyException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
