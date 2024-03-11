package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

public class StaticPagesModel implements Parcelable {
    public static final Parcelable.Creator<StaticPagesModel> CREATOR = new Parcelable.Creator<StaticPagesModel>() {
        public StaticPagesModel createFromParcel(Parcel source) {
            return new StaticPagesModel(source);
        }

        public StaticPagesModel[] newArray(int size) {
            return new StaticPagesModel[size];
        }
    };
    public Integer id;
    public String title;
    public String content;
    public Boolean Active;

    public StaticPagesModel(Integer id, String title, String content, Boolean active) {
        super();
        this.id = id;
        this.title = title;
        this.content = content;
        this.Active = active;
    }

    protected StaticPagesModel(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.title = in.readString();
        this.content = in.readString();
        this.Active = (Boolean) in.readValue(Boolean.class.getClassLoader());
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
        dest.writeValue(this.Active);
    }
}