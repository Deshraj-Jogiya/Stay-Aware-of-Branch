package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;


public class CalenderModel implements Parcelable {
    public static final Parcelable.Creator<CalenderModel> CREATOR = new Parcelable.Creator<CalenderModel>() {
        public CalenderModel createFromParcel(Parcel source) {
            return new CalenderModel(source);
        }

        public CalenderModel[] newArray(int size) {
            return new CalenderModel[size];
        }
    };
    public String id;
    public String title;
    public String date;
    public String backgroundColor;
    public String url;
    public String allDay;

    public String onlyDate;

    public CalenderModel(String id, String title, String date, String backgroundColor, String url, String allDay) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.backgroundColor = backgroundColor;
        this.url = url;
        this.allDay = allDay;

    }

    protected CalenderModel(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.date = in.readString();
        this.backgroundColor = in.readString();
        this.url = in.readString();
        this.allDay = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.title);
        dest.writeString(this.date);
        dest.writeString(this.backgroundColor);
        dest.writeString(this.url);
        dest.writeString(this.allDay);
    }
}
