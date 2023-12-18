package com.schoolmgmtsys.root.ssg.models;

public class AssignAnswerModel {
    public Integer id;
    public Integer userID;
    public String Notes;
    public String Time;
    public String FullName;
    public String Class;
    public String AnswerFile;

    public AssignAnswerModel(Integer id, String Notes, String Time, String FullName, String Class, Integer userID, String AnswerFile) {
        super();
        this.id = id;
        this.Notes = Notes;
        this.Time = Time;
        this.FullName = FullName;
        this.Class = Class;
        this.userID = userID;
        this.AnswerFile = FullName +" - "+ AnswerFile;
    }


}
