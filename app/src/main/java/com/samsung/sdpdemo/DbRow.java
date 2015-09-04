package com.samsung.sdpdemo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sarang on 4/13/15.
 */
public class DbRow implements Parcelable {

    String id;
    String col1;
    String col2;
    String col3;

    public DbRow() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.col1);
        dest.writeString(this.col2);
        dest.writeString(this.col3);
    }

    public static final Creator<DbRow> CREATOR =
            new Creator<DbRow>() {
                public DbRow createFromParcel(Parcel source) {
                    return new DbRow(source);
                }

                public DbRow[] newArray(int size) {
                    return new DbRow[size];
                }
            };

    private DbRow(Parcel source) {
        this.id = source.readString();
        this.col1 = source.readString();
        this.col2 = source.readString();
        this.col3 = source.readString();
    }

    @Override
    public String toString() {
        return " id= " + id + " col2 " + col2 + " col3 " + col3;
    }
}
