package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ExamsModel implements Parcelable {
    public static final Parcelable.Creator<ExamsModel> CREATOR = new Parcelable.Creator<ExamsModel>() {
        public ExamsModel createFromParcel(Parcel source) {
            return new ExamsModel(source);
        }

        public ExamsModel[] newArray(int size) {
            return new ExamsModel[size];
        }
    };
    public Integer id;
    public String title;
    public String content;
    public String Date;

    public ExamsModel(Integer id, String title, String content, String Date) {
        super();
        this.id = id;
        this.title = title;
        this.content = content;
        this.Date = Date;
    }

    protected ExamsModel(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.title = in.readString();
        this.content = in.readString();
        this.Date = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.title);
        dest.writeString(this.content);
        dest.writeString(this.Date);
    }
}
