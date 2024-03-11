package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by SolutionsBricks Mobile Dev. Team.
 */
public class ClassesSchModel implements Parcelable {
    public static final Creator<ClassesSchModel> CREATOR = new Creator<ClassesSchModel>() {
        public ClassesSchModel createFromParcel(Parcel in) {
            return new ClassesSchModel(in);
        }

        public ClassesSchModel[] newArray(int size) {
            return new ClassesSchModel[size];
        }
    };
    public String id;
    public String dayName;
    public String classId;
    public String subjectId;
    public String startPeriod;
    public String endPeriod;

    public ClassesSchModel(String id, String dayName, String classId, String subjectId, String startPeriod, String endPeriod) {
        this.id = id;
        this.dayName = dayName;
        this.classId = classId;
        this.subjectId = subjectId;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
    }

    public ClassesSchModel(String id, String dayName, String subjectId, String startPeriod, String endPeriod) {
        this.id = id;
        this.dayName = dayName;
        this.classId = classId;
        this.subjectId = subjectId;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
    }

    private ClassesSchModel(Parcel in) {
        this.id = in.readString();
        this.dayName = in.readString();
        this.classId = in.readString();
        this.subjectId = in.readString();
        this.startPeriod = in.readString();
        this.endPeriod = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(dayName);
        out.writeString(classId);
        out.writeString(subjectId);
        out.writeString(startPeriod);
        out.writeString(endPeriod);
    }

}
