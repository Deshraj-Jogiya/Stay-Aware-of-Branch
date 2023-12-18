package com.schoolmgmtsys.root.ssg.models;

public class TeachersModel {
    public Integer id;
    public String FullName;
    public String Username;
    public String Email;
    public String Leaderboard;
    public String mobileNo;

    public TeachersModel(Integer id, String FullName, String Username, String Email,String Leaderboard,String mobileNo) {
        super();
        this.id = id;
        this.FullName = FullName;
        this.Username = Username;
        this.Email = Email;
        this.Leaderboard = Leaderboard;
        this.mobileNo = mobileNo;
    }
}
