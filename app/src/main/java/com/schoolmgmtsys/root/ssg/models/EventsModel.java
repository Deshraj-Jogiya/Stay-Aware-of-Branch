package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

public class EventsModel implements Parcelable {
    public static final Creator<EventsModel> CREATOR = new Creator<EventsModel>() {
        public EventsModel createFromParcel(Parcel source) {
            return new EventsModel(source);
        }

        public EventsModel[] newArray(int size) {
            return new EventsModel[size];
        }
    };
    public Integer id;
    public String title;
    public String content;
    public String place;
    public String date;
    public String forWho;

    public EventsModel(Integer id, String title, String content, String date, String forWho, String place) {
        super();
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.forWho = forWho;
        this.place = place;
    }

    protected EventsModel(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.title = in.readString();
        this.content = in.readString();
        this.place = in.readString();
        this.date = in.readString();
        this.forWho = in.readString();
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
        dest.writeString(this.place);
        dest.writeString(this.date);
        dest.writeString(this.forWho);
    }
}
