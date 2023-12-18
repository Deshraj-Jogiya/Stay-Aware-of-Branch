package com.schoolmgmtsys.root.ssg.models;

public class StudentsModel {
    public Integer id;
    public String FullName;
    public String Username;
    public String Email;
    public String Class;
    public String Leaderboard;
    public String MobileNo;
    public String FatherName;
    public String FatherEmail;
    public String admissionDate;
    public String phoneNo;
    public String cat_title;
    public String attendanceRec;
    public String studentSection;
    public String age;
    public String school;


    public StudentsModel(Integer id, String FullName, String Username, String Email, String Class,String Leaderboard,String MobileNo,String FatherName,String FatherEmail,String admissionDate,String phoneNo,String cat_title,String attendanceRec,String studentSection,String age,String school) {
        super();
        this.id = id;
        this.FullName = FullName;
        this.Username = Username;
        this.Email = Email;
        this.Class = Class;
        this.Leaderboard = Leaderboard;
        this.MobileNo = MobileNo;
        this.FatherName = FatherName;
        this.FatherEmail = FatherEmail;
        this.admissionDate = admissionDate;
        this.phoneNo = phoneNo;
        this.cat_title = cat_title;
        this.attendanceRec = attendanceRec;
        this.studentSection = studentSection;
        this.age = age;
        this.school = school;

    }
}
