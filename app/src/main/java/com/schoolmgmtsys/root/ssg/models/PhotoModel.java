package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by SolutionsBricks Mobile Dev. Team.
 */
public class PhotoModel implements Parcelable {

    public String id;
    public String albumId;
    public String mediaType;
    public String mediaURL;
    public String mediaURLThumb;
    public String mediaTitle;
    public String mediaDescription;
    public String mediaDate;

    public PhotoModel(String id, String albumId, String mediaType, String mediaURL, String mediaURLThumb, String mediaTitle, String mediaDescription, String mediaDate) {
        this.id = id;
        this.albumId = albumId;
        this.mediaType = mediaType;
        this.mediaURL = mediaURL;
        this.mediaURLThumb = mediaURLThumb;
        this.mediaTitle = mediaTitle;
        this.mediaDescription = mediaDescription;
        this.mediaDate = mediaDate;
    }

    public PhotoModel() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.albumId);
        dest.writeString(this.mediaType);
        dest.writeString(this.mediaURL);
        dest.writeString(this.mediaURLThumb);
        dest.writeString(this.mediaTitle);
        dest.writeString(this.mediaDescription);
        dest.writeString(this.mediaDate);
    }

    protected PhotoModel(Parcel in) {
        this.id = in.readString();
        this.albumId = in.readString();
        this.mediaType = in.readString();
        this.mediaURL = in.readString();
        this.mediaURLThumb = in.readString();
        this.mediaTitle = in.readString();
        this.mediaDescription = in.readString();
        this.mediaDate = in.readString();
    }

    public static final Creator<PhotoModel> CREATOR = new Creator<PhotoModel>() {
        @Override
        public PhotoModel createFromParcel(Parcel source) {
            return new PhotoModel(source);
        }

        @Override
        public PhotoModel[] newArray(int size) {
            return new PhotoModel[size];
        }
    };
}
