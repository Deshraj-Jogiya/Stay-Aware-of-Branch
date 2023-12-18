package com.schoolmgmtsys.root.ssg.models;

public class OnlineExamMarks {
    public Integer id;
    public Integer studentId;
    public String examGrade;
    public String examDate;
    public String FullName;

    public OnlineExamMarks(Integer id, String examGrade, String examDate, String FullName, Integer studentId) {
        super();
        this.id = id;
        this.examGrade = examGrade;
        this.examDate = examDate;
        this.FullName = FullName;
        this.studentId = studentId;
    }


}
