package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

public class HostelModel implements Parcelable {
    public static final Creator<HostelModel> CREATOR = new Creator<HostelModel>() {
        public HostelModel createFromParcel(Parcel source) {
            return new HostelModel(source);
        }

        public HostelModel[] newArray(int size) {
            return new HostelModel[size];
        }
    };
    public Integer id;
    public String hostelTitle;
    public String hostelType;
    public String hostelAddress;
    public String hostelManager;
    public String hostelNotes;

    public HostelModel(Integer id, String hostelTitle, String hostelType, String hostelAddress, String hostelManager, String hostelNotes) {
        super();
        this.id = id;
        this.hostelTitle = hostelTitle;
        this.hostelType = hostelType;
        this.hostelAddress = hostelAddress;
        this.hostelManager = hostelManager;
        this.hostelNotes = hostelNotes;

    }

    protected HostelModel(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.hostelTitle = in.readString();
        this.hostelType = in.readString();
        this.hostelAddress = in.readString();
        this.hostelManager = in.readString();
        this.hostelNotes = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.hostelTitle);
        dest.writeString(this.hostelType);
        dest.writeString(this.hostelAddress);
        dest.writeString(this.hostelManager);
        dest.writeString(this.hostelNotes);
    }
}
