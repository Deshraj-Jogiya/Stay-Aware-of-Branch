package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

public class GradesModel implements Parcelable {
    public static final Parcelable.Creator<GradesModel> CREATOR = new Parcelable.Creator<GradesModel>() {
        public GradesModel createFromParcel(Parcel source) {
            return new GradesModel(source);
        }

        public GradesModel[] newArray(int size) {
            return new GradesModel[size];
        }
    };
    public Integer id;
    public String GradeName;
    public String GradeDesc;
    public String GradePoints;
    public String GradeFrom;
    public String GradeTo;

    public GradesModel(Integer id, String GradeName, String GradeDesc, String GradePoints, String GradeFrom, String GradeTo) {
        super();
        this.id = id;
        this.GradeName = GradeName;
        this.GradeDesc = GradeDesc;
        this.GradePoints = GradePoints;
        this.GradeFrom = GradeFrom;
        this.GradeTo = GradeTo;

    }

    protected GradesModel(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.GradeName = in.readString();
        this.GradeDesc = in.readString();
        this.GradePoints = in.readString();
        this.GradeFrom = in.readString();
        this.GradeTo = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.GradeName);
        dest.writeString(this.GradeDesc);
        dest.writeString(this.GradePoints);
        dest.writeString(this.GradeFrom);
        dest.writeString(this.GradeTo);
    }
}
