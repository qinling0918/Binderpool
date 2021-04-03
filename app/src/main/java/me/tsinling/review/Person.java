package me.tsinling.review;

import android.os.Parcel;
import android.os.Parcelable;

public class Person implements Parcelable {

    private String mName;
    private int mAge;

    public static final Creator<Person> CREATOR = new Creator<Person>() {

        @Override
        public Person createFromParcel(Parcel source) {
            return new Person(source.readString(), source.readInt());
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };

    public Person() {}

    private Person(String name, int age) {
        mName = name;
        mAge = age;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeInt(mAge);
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }



}
