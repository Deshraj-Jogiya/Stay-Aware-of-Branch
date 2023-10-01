package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

public class DashLeadModel implements Parcelable {

    public static final Parcelable.Creator<DashLeadModel> CREATOR = new Parcelable.Creator<DashLeadModel>() {
        public DashLeadModel createFromParcel(Parcel source) {
            return new DashLeadModel(source);
        }

        public DashLeadModel[] newArray(int size) {
            return new DashLeadModel[size];
        }
    };
    public Integer id;
    public String name;
    public String msg;

    public DashLeadModel(Integer id, String name, String msg) {
        super();
        this.id = id;
        this.name = name;
        this.msg = msg;
    }

    protected DashLeadModel(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.name = in.readString();
        this.msg = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.name);
        dest.writeString(this.msg);
    }
}
