package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

/**
 * Created by SolutionsBricks Mobile Dev. Team.
 */
public class ExamMarkModel implements Parcelable {
    public String id;
    public String StudentName;
    public HashMap<Integer,String> ExamMarksMap = new HashMap<>();
    public String ExamMark;
    public String AttendanceMark;
    public String StudentRollId;
    public String MarkComment;
    // Newer version variables
    public HashMap<String, String> MarksColsMap = new HashMap<>();
    public String TotalMark;

    public ExamMarkModel(String id, String StudentName, String StudentRollId, String AttendanceMark, String ExamMark, String MarkComment) {
        this.id = id;
        this.StudentName = StudentName;
        this.ExamMark = ExamMark;
        this.AttendanceMark = AttendanceMark;
        this.StudentRollId = StudentRollId;
        this.MarkComment = MarkComment;
    }

    public ExamMarkModel(){}


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.StudentName);
        dest.writeString(this.ExamMark);
        dest.writeString(this.AttendanceMark);
        dest.writeString(this.StudentRollId);
        dest.writeString(this.MarkComment);
        dest.writeSerializable(this.MarksColsMap);
        dest.writeString(this.TotalMark);
    }

    protected ExamMarkModel(Parcel in) {
        this.id = in.readString();
        this.StudentName = in.readString();
        this.ExamMark = in.readString();
        this.AttendanceMark = in.readString();
        this.StudentRollId = in.readString();
        this.MarkComment = in.readString();
        this.MarksColsMap = (HashMap<String, String>) in.readSerializable();
        this.TotalMark = in.readString();
    }

    public static final Creator<ExamMarkModel> CREATOR = new Creator<ExamMarkModel>() {
        @Override
        public ExamMarkModel createFromParcel(Parcel source) {
            return new ExamMarkModel(source);
        }

        @Override
        public ExamMarkModel[] newArray(int size) {
            return new ExamMarkModel[size];
        }
    };
}
