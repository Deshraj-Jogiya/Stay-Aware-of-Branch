package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by SolutionsBricks Mobile Dev. Team.
 */
public class UserModel implements Parcelable {

    public String id;
    public String name;
    public String email;
    public String role;
    public String username;

    public UserModel(String id, String name, String email, String role, String username) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.username = username;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.email);
        dest.writeString(this.role);
        dest.writeString(this.username);
    }

    protected UserModel(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.email = in.readString();
        this.role = in.readString();
        this.username = in.readString();
    }

    public static final Creator<UserModel> CREATOR = new Creator<UserModel>() {
        @Override
        public UserModel createFromParcel(Parcel source) {
            return new UserModel(source);
        }

        @Override
        public UserModel[] newArray(int size) {
            return new UserModel[size];
        }
    };
}

