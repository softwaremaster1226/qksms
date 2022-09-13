package com.android.QKSMS.common.util;

import android.os.Parcel;
import android.os.Parcelable;

public class SongsModel implements Parcelable {

    public String _ID;
    public String mSongsName;
    public String mArtistName;
    public String mDuration;
    public String mPath;
    public String mAlbum;
    public String mFileType;
    public String mAlbumId;

    public SongsModel(String _ID,
                      String songsName,
                      String artistName,
                      String duration,
                      String album,
                      String path,
                      String albumId,
                      String fileType) {
        this._ID = _ID;
        mSongsName = songsName;
        mArtistName = artistName;
        mDuration = duration;
        mPath = path;
        mAlbum = album;
        mAlbumId=albumId;
        mFileType = fileType;
    }

    protected SongsModel(Parcel in) {
        _ID = in.readString();
        mSongsName = in.readString();
        mArtistName = in.readString();
        mDuration = in.readString();
        mPath = in.readString();
        mAlbum = in.readString();
        mFileType = in.readString();
        mAlbumId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_ID);
        dest.writeString(mSongsName);
        dest.writeString(mArtistName);
        dest.writeString(mDuration);
        dest.writeString(mPath);
        dest.writeString(mAlbum);
        dest.writeString(mFileType);
        dest.writeString(mAlbumId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SongsModel> CREATOR = new Creator<SongsModel>() {
        @Override
        public SongsModel createFromParcel(Parcel in) {
            return new SongsModel(in);
        }

        @Override
        public SongsModel[] newArray(int size) {
            return new SongsModel[size];
        }
    };
}
