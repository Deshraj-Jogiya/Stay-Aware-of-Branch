package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

public class HomeworkModel implements Parcelable {

    public String id;
    public String homeworkTitle;
    public String homeworkDescription;
    public String homeworkFile;
    public String homeworkDate;
    public String homeworkSubmissionDate;
    public String homeworkEvaluationDate;
    public String subject;
    public String classes;
    public String sections;

    public HomeworkModel(String id, String homeworkTitle, String homeworkDescription, String homeworkFile, String homeworkDate, String homeworkSubmissionDate, String homeworkEvaluationDate, String subject, String classes, String sections) {
        this.id = id;
        this.homeworkTitle = homeworkTitle;
        this.homeworkDescription = homeworkDescription;
        this.homeworkFile = homeworkFile;
        this.homeworkDate = homeworkDate;
        this.homeworkSubmissionDate = homeworkSubmissionDate;
        this.homeworkEvaluationDate = homeworkEvaluationDate;
        this.subject = subject;
        this.classes = classes;
        this.sections = sections;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.homeworkTitle);
        dest.writeString(this.homeworkDescription);
        dest.writeString(this.homeworkFile);
        dest.writeString(this.homeworkDate);
        dest.writeString(this.homeworkSubmissionDate);
        dest.writeString(this.homeworkEvaluationDate);
        dest.writeString(this.subject);
        dest.writeString(this.classes);
        dest.writeString(this.sections);
    }

    protected HomeworkModel(Parcel in) {
        this.id = in.readString();
        this.homeworkTitle = in.readString();
        this.homeworkDescription = in.readString();
        this.homeworkFile = in.readString();
        this.homeworkDate = in.readString();
        this.homeworkSubmissionDate = in.readString();
        this.homeworkEvaluationDate = in.readString();
        this.subject = in.readString();
        this.classes = in.readString();
        this.sections = in.readString();
    }

    public static final Parcelable.Creator<HomeworkModel> CREATOR = new Parcelable.Creator<HomeworkModel>() {
        @Override
        public HomeworkModel createFromParcel(Parcel source) {
            return new HomeworkModel(source);
        }

        @Override
        public HomeworkModel[] newArray(int size) {
            return new HomeworkModel[size];
        }
    };
}
