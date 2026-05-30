package com.bukuro.exception;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(String isbn) {
        super("ISBN " + isbn + " の書籍はOpenBDに登録されていません");
    }
}
