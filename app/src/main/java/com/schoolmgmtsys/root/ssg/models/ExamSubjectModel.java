package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ExamSubjectModel implements Parcelable {
    public String id;
    public String Name;


    public ExamSubjectModel(String id, String Name) {
        this.id = id;
        this.Name = Name;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.Name);
    }

    protected ExamSubjectModel(Parcel in) {
        this.id = in.readString();
        this.Name = in.readString();
    }

    public static final Parcelable.Creator<ExamSubjectModel> CREATOR = new Parcelable.Creator<ExamSubjectModel>() {
        @Override
        public ExamSubjectModel createFromParcel(Parcel source) {
            return new ExamSubjectModel(source);
        }

        @Override
        public ExamSubjectModel[] newArray(int size) {
            return new ExamSubjectModel[size];
        }
    };
}
