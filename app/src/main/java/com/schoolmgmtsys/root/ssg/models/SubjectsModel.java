package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

public class SubjectsModel implements Parcelable {

    public String passGrade;
    public String finalGrade;
    public Integer id;
    public String teacherId;
    public String Name;
    public String Class;
    public String Teacher;


    public SubjectsModel(Integer id, String Name, String Class, String Teacher, String teacherId, String passGrade, String finalGrade) {
        super();
        this.id = id;
        this.Name = Name;
        this.Class = Class;
        this.Teacher = Teacher;
        this.teacherId = teacherId;
        this.passGrade = passGrade;
        this.finalGrade = finalGrade;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.passGrade);
        dest.writeString(this.finalGrade);
        dest.writeValue(this.id);
        dest.writeString(this.teacherId);
        dest.writeString(this.Name);
        dest.writeString(this.Class);
        dest.writeString(this.Teacher);
    }

    protected SubjectsModel(Parcel in) {
        this.passGrade = in.readString();
        this.finalGrade = in.readString();
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.teacherId = in.readString();
        this.Name = in.readString();
        this.Class = in.readString();
        this.Teacher = in.readString();
    }

    public static final Creator<SubjectsModel> CREATOR = new Creator<SubjectsModel>() {
        @Override
        public SubjectsModel createFromParcel(Parcel source) {
            return new SubjectsModel(source);
        }

        @Override
        public SubjectsModel[] newArray(int size) {
            return new SubjectsModel[size];
        }
    };
}
