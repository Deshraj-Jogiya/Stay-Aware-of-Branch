package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by SolutionsBricks Mobile Dev. Team.
 */
public class AlbumsModel implements Parcelable {

    public String id;
    public String albumTitle;
    public String albumDescription;
    public String albumImage;
    public String albumParent;

    public void AlbumsModel(){
    }

    public AlbumsModel(String id, String albumTitle, String albumDescription, String albumImage, String albumParent) {
        this.id = id;
        this.albumTitle = albumTitle;
        this.albumDescription = albumDescription;
        this.albumImage = albumImage;
        this.albumParent = albumParent;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.albumTitle);
        dest.writeString(this.albumDescription);
        dest.writeString(this.albumImage);
        dest.writeString(this.albumParent);
    }


    protected AlbumsModel(Parcel in) {
        this.id = in.readString();
        this.albumTitle = in.readString();
        this.albumDescription = in.readString();
        this.albumImage = in.readString();
        this.albumParent = in.readString();
    }

    public static final Creator<AlbumsModel> CREATOR = new Creator<AlbumsModel>() {
        @Override
        public AlbumsModel createFromParcel(Parcel source) {
            return new AlbumsModel(source);
        }

        @Override
        public AlbumsModel[] newArray(int size) {
            return new AlbumsModel[size];
        }
    };
}
