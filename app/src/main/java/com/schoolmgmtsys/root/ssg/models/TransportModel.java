package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

public class TransportModel implements Parcelable {
    public static final Creator<TransportModel> CREATOR = new Creator<TransportModel>() {
        public TransportModel createFromParcel(Parcel source) {
            return new TransportModel(source);
        }

        public TransportModel[] newArray(int size) {
            return new TransportModel[size];
        }
    };
    public Integer id;
    public String transportTitle;
    public String transportDescription;
    public String transportFare;

    public TransportModel(Integer id, String transportTitle, String transportDescription, String transportFare) {
        super();
        this.id = id;
        this.transportTitle = transportTitle;
        this.transportDescription = transportDescription;
        this.transportFare = transportFare;

    }

    protected TransportModel(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.transportTitle = in.readString();
        this.transportDescription = in.readString();
        this.transportFare = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.transportTitle);
        dest.writeString(this.transportDescription);
        dest.writeString(this.transportFare);
    }
}
