package com.spreadwin.musicplayer1.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

/**
 * 作者：lixiang on 2016/1/8 10:57
 * 邮箱：xiang.li@spreadwin.com
 */
public class Music implements Parcelable {
    private String mTitle,
            mTitleKey,
            mArtist,
            mArtistKey,
            mComposer,
            mAlbum,
            mAlbumKey,
            mDisplayName,
            mMimeType,
            mPath,
            Letters;

    private int mId,
            mArtistId,
            mAlbumId,
            mYear,
            mTrack;

    private int mDuration = 0,
            mSize = 0;

    private boolean isRingtone = false;
    private boolean isPodcast = false;
    private boolean isAlarm = false;
    private boolean isMusic = false;

    private boolean isNotification = false;


    public Music(Bundle bundle) {
        mId = bundle.getInt(MediaStore.Audio.Media._ID);
        mTitle = bundle.getString(MediaStore.Audio.Media.TITLE);
        mTitleKey = bundle.getString(MediaStore.Audio.Media.TITLE_KEY);
        mArtist = bundle.getString(MediaStore.Audio.Media.ARTIST);
        mArtistKey = bundle.getString(MediaStore.Audio.Media.ARTIST_KEY);
        mComposer = bundle.getString(MediaStore.Audio.Media.COMPOSER);
        mAlbum = bundle.getString(MediaStore.Audio.Media.ALBUM);
        mAlbumKey = bundle.getString(MediaStore.Audio.Media.ALBUM_KEY);
        mDisplayName = bundle.getString(MediaStore.Audio.Media.DISPLAY_NAME);
        mYear = bundle.getInt(MediaStore.Audio.Media.YEAR);
        mMimeType = bundle.getString(MediaStore.Audio.Media.MIME_TYPE);
        mPath = bundle.getString(MediaStore.Audio.Media.DATA);
        mArtistId = bundle.getInt(MediaStore.Audio.Media.ARTIST_ID);
        mAlbumId = bundle.getInt(MediaStore.Audio.Media.ALBUM_ID);
        mTrack = bundle.getInt(MediaStore.Audio.Media.TRACK);
        mDuration = bundle.getInt(MediaStore.Audio.Media.DURATION);
        mSize = bundle.getInt(MediaStore.Audio.Media.SIZE);
        isRingtone = bundle.getInt(MediaStore.Audio.Media.IS_RINGTONE) == 1;
        isPodcast = bundle.getInt(MediaStore.Audio.Media.IS_PODCAST) == 1;
        isAlarm = bundle.getInt(MediaStore.Audio.Media.IS_ALARM) == 1;
        isMusic = bundle.getInt(MediaStore.Audio.Media.IS_MUSIC) == 1;
        isNotification = bundle.getInt(MediaStore.Audio.Media.IS_NOTIFICATION) == 1;

    }

    protected Music(Parcel in) {
        mTitle = in.readString();
        mTitleKey = in.readString();
        mArtist = in.readString();
        mArtistKey = in.readString();
        mComposer = in.readString();
        mAlbum = in.readString();
        mAlbumKey = in.readString();
        mDisplayName = in.readString();
        mMimeType = in.readString();
        mPath = in.readString();
        mId = in.readInt();
        mArtistId = in.readInt();
        mAlbumId = in.readInt();
        mYear = in.readInt();
        mTrack = in.readInt();
        mDuration = in.readInt();
        mSize = in.readInt();
        isRingtone = in.readByte() != 0;
        isPodcast = in.readByte() != 0;
        isAlarm = in.readByte() != 0;
        isMusic = in.readByte() != 0;
        isNotification = in.readByte() != 0;
    }

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    public int getId() {
        return mId;
    }

    public String getmTitle(){return mTitle;}

    public String getMimeType() {
        return mMimeType;
    }

    public int getDuration() {
        return mDuration;
    }

    public int getSize() {
        return mSize;
    }

    public boolean isRingtone() {
        return isRingtone;
    }

    public boolean isPodcast() {
        return isPodcast;
    }

    public boolean isAlarm() {
        return isAlarm;
    }

    public boolean isMusic() {
        return isMusic;
    }

    public boolean isNotification() {
        return isNotification;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getTitleKey() {
        return mTitleKey;
    }

    public String getArtist() {
        return mArtist;
    }

    public int getArtistId() {
        return mArtistId;
    }

    public String getArtistKey() {
        return mArtistKey;
    }

    public String getComposer() {
        return mComposer;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public int getAlbumId() {
        return mAlbumId;
    }

    public String getAlbumKey() {
        return mAlbumKey;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public int getYear() {
        return mYear;
    }

    public int getTrack() {
        return mTrack;
    }

    public String getPath() {
        return mPath;
    }

    public void  setLetters(String letters){
        this.Letters=letters;
    }
    public  String getLetters(){
        return Letters;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mTitleKey);
        dest.writeString(mArtist);
        dest.writeString(mArtistKey);
        dest.writeString(mComposer);
        dest.writeString(mAlbum);
        dest.writeString(mAlbumKey);
        dest.writeString(mDisplayName);
        dest.writeString(mMimeType);
        dest.writeString(mPath);
        dest.writeInt(mId);
        dest.writeInt(mArtistId);
        dest.writeInt(mAlbumId);
        dest.writeInt(mYear);
        dest.writeInt(mTrack);
        dest.writeInt(mDuration);
        dest.writeInt(mSize);
        dest.writeByte((byte) (isRingtone ? 1 : 0));
        dest.writeByte((byte) (isPodcast ? 1 : 0));
        dest.writeByte((byte) (isAlarm ? 1 : 0));
        dest.writeByte((byte) (isMusic ? 1 : 0));
        dest.writeByte((byte) (isNotification ? 1 : 0));
    }
}
