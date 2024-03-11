package com.schoolmgmtsys.root.ssg.models;

public class LibraryModel {
    public Integer id;
    public String title;
    public String author;
    public String state;
    public String price;
    public String bookFile;


    public LibraryModel(Integer id, String title, String author, String state, String price, String bookFile) {
        super();
        this.id = id;
        this.title = title;
        this.author = author;
        this.state = state;
        this.price = price;
        if (!bookFile.equals("")) {
            this.bookFile = (title + "-" + author) + "." + getFileExt(bookFile);
        }
    }

    public static String getFileExt(String fileName) {
        return fileName.substring((fileName.lastIndexOf(".") + 1), fileName.length());
    }
}
