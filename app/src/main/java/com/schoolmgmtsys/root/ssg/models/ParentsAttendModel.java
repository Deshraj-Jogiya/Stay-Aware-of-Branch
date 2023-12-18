package com.schoolmgmtsys.root.ssg.models;

import com.schoolmgmtsys.root.ssg.utils.Concurrent;

public class ParentsAttendModel {

    public String StudentName;
    public String StudentRollId;
    public Integer typeInDisplayList;

    public String subjectId;
    public String subjectName;
    public String date;
    public String status;
    public String statusName;
    public String feesName;
    public String makeUpName;
    public String breakName;
    public String note;
    public String speed_writing;
    public String book;
    public String cert;
    public String balance;

    public ParentsAttendModel(String subjectId, String date, String status, String subjectName, String statusName, String feesName, String makeUpName, String breakName, String cert, String note, String book, String speed_writing, Integer typeInDisplayList,String balance) {
        super();
        this.subjectId = subjectId;
        this.date = date;
        this.status = status;
        this.subjectName = subjectName;
        this.statusName = statusName;
        this.feesName = feesName;
        this.makeUpName = makeUpName;
        this.breakName = breakName;
        this.note = note;
        this.book = book;
        this.speed_writing = speed_writing;
        this.cert = cert;
        this.typeInDisplayList = typeInDisplayList;
        this.balance = balance;
    }

    public ParentsAttendModel(String StudentName, String StudentRollId, Integer typeInDisplayList) {
        this.StudentName = Concurrent.repairJsonValueQuotes(StudentName);
        this.StudentRollId = Concurrent.repairJsonValueQuotes(StudentRollId);
        this.typeInDisplayList = typeInDisplayList;
    }
}
