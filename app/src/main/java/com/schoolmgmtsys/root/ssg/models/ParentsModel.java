package com.schoolmgmtsys.root.ssg.models;

public class ParentsModel {
    public Integer id;
    public String FullName;
    public String Username;
    public String Email;
    public String parentOf;
    public String mobileNo;

    public ParentsModel(Integer id, String FullName, String Username, String Email,String parentOf,String mobileNo) {
        super();
        this.id = id;
        this.FullName = FullName;
        this.Username = Username;
        this.Email = Email;
        this.parentOf = parentOf;
        this.mobileNo = mobileNo;
    }
}
