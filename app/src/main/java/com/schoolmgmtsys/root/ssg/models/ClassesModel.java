package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ClassesModel implements Parcelable {
    public static final Parcelable.Creator<ClassesModel> CREATOR = new Parcelable.Creator<ClassesModel>() {
        public ClassesModel createFromParcel(Parcel in) {
            return new ClassesModel(in);
        }

        public ClassesModel[] newArray(int size) {
            return new ClassesModel[size];
        }
    };
    public Integer id;
    public String Name;
    public String Teacher;
    public String Dormitory;

    public ClassesModel(Integer id, String Name, String Teacher, String Dormitory) {
        super();
        this.id = id;
        this.Name = Name;
        this.Teacher = Teacher;
        this.Dormitory = Dormitory;
    }

    private ClassesModel(Parcel in) {
        id = in.readInt();
        Name = in.readString();
        Teacher = in.readString();
        Dormitory = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(Name);
        out.writeString(Teacher);
        out.writeString(Dormitory);
    }

}
