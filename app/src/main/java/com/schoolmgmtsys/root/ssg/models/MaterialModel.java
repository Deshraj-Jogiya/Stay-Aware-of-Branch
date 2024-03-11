package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

public class MaterialModel implements Parcelable {
    public static final Parcelable.Creator<MaterialModel> CREATOR = new Parcelable.Creator<MaterialModel>() {
        public MaterialModel createFromParcel(Parcel source) {
            return new MaterialModel(source);
        }

        public MaterialModel[] newArray(int size) {
            return new MaterialModel[size];
        }
    };
    public Integer id;
    public String subjectId;
    public String subjectTitle;
    public String materialTitle;
    public String materialDescription;
    public String materialFile;
    public String classes;

    public MaterialModel(Integer id, String subjectId, String subjectTitle, String materialTitle, String materialDescription, String materialFile, String classes) {
        super();
        this.id = id;
        this.subjectId = subjectId;
        this.subjectTitle = subjectTitle;
        this.materialTitle = materialTitle;
        this.materialDescription = materialDescription;
        this.classes = classes;
        if (!materialFile.equals("")) {
            this.materialFile = materialTitle + "-" + subjectTitle + "." + getFileExt(materialFile);
        }
    }


    protected MaterialModel(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.subjectId = in.readString();
        this.subjectTitle = in.readString();
        this.materialTitle = in.readString();
        this.materialDescription = in.readString();
        this.materialFile = in.readString();
        this.classes = in.readString();
    }

    public static String getFileExt(String fileName) {
        return fileName.substring((fileName.lastIndexOf(".") + 1), fileName.length());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.subjectId);
        dest.writeString(this.subjectTitle);
        dest.writeString(this.materialTitle);
        dest.writeString(this.materialDescription);
        dest.writeString(this.materialFile);
        dest.writeString(this.classes);
    }
}
