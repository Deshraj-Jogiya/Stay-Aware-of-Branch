package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

public class SectionsModel implements Parcelable {
    public static final Creator<SectionsModel> CREATOR = new Creator<SectionsModel>() {
        public SectionsModel createFromParcel(Parcel source) {
            return new SectionsModel(source);
        }

        public SectionsModel[] newArray(int size) {
            return new SectionsModel[size];
        }
    };
    public String ClassName;
    public String SectionName;
    public String SectionTitle;
    public String SectionTeacher;
    public Integer id;
    public Integer ClassId;

    public SectionsModel(Integer id, String ClassName, String SectionName, String SectionTitle, String SectionTeacher) {
        super();
        this.id = id;
        this.ClassName = ClassName;
        this.SectionName = SectionName;
        this.SectionTitle = SectionTitle;
        this.SectionTeacher = SectionTeacher;

    }

    public SectionsModel(Integer id, String SectionName, String SectionTitle, Integer ClassId) {
        super();
        this.id = id;
        this.ClassId = ClassId;
        this.SectionName = SectionName;
        this.SectionTitle = SectionTitle;
    }

    protected SectionsModel(Parcel in) {
        this.ClassName = in.readString();
        this.SectionName = in.readString();
        this.SectionTitle = in.readString();
        this.SectionTeacher = in.readString();
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ClassName);
        dest.writeString(this.SectionName);
        dest.writeString(this.SectionTitle);
        dest.writeString(this.SectionTeacher);
        dest.writeValue(this.id);
    }
}
