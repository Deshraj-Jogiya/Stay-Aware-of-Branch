package com.schoolmgmtsys.root.ssg.models;

public class StudentsAttendModel {
    public Integer id;
    public String subjectId;
    public String subjectName;
    public String studentId;
    public String date;
    public String status;
    public String statusName;

    public StudentsAttendModel(Integer id, String studentId, String subjectId, String date, String status, String subjectName, String statusName) {
        super();
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.date = date;
        this.status = status;
        this.subjectName = subjectName;
        this.statusName = statusName;
    }
}
