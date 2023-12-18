package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

public class OnlineExamsModel implements Parcelable {
    public static final Parcelable.Creator<OnlineExamsModel> CREATOR = new Parcelable.Creator<OnlineExamsModel>() {
        public OnlineExamsModel createFromParcel(Parcel source) {
            return new OnlineExamsModel(source);
        }

        public OnlineExamsModel[] newArray(int size) {
            return new OnlineExamsModel[size];
        }
    };
    public Integer id;
    public String title;
    public String content;
    public String DeadLine;

    public OnlineExamsModel(Integer id, String title, String content, String DeadLine) {
        super();
        this.id = id;
        this.title = title;
        this.content = content;
        this.DeadLine = DeadLine;
    }

    protected OnlineExamsModel(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.title = in.readString();
        this.content = in.readString();
        this.DeadLine = in.readString();
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
        dest.writeString(this.DeadLine);
    }
}
