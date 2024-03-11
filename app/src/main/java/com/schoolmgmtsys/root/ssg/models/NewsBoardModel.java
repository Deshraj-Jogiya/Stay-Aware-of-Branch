package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

public class NewsBoardModel implements Parcelable {
    public static final Creator<NewsBoardModel> CREATOR = new Creator<NewsBoardModel>() {
        public NewsBoardModel createFromParcel(Parcel source) {
            return new NewsBoardModel(source);
        }

        public NewsBoardModel[] newArray(int size) {
            return new NewsBoardModel[size];
        }
    };
    public Integer id;
    public String title;
    public String content;
    public String forWho;
    public String date;

    public NewsBoardModel(Integer id, String title, String content, String forWho, String date) {
        super();
        this.id = id;
        this.title = title;
        this.content = content;
        this.forWho = forWho;
        this.date = date;
    }

    protected NewsBoardModel(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.title = in.readString();
        this.content = in.readString();
        this.forWho = in.readString();
        this.date = in.readString();
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
        dest.writeString(this.forWho);
        dest.writeString(this.date);
    }
}
