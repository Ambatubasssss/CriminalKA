package com.example.criminalintent;

import java.util.Date;
import java.util.UUID;

public class Crime {
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;
    private String mSuspect;
    private String mSuspectPhone;
    private String mPoliceName;
    private String mPoliceNumber;

    public Crime() {
        mId = UUID.randomUUID();
        mDate = new Date();
        mTitle = "";
        mSolved = false;
        mSuspect = "";
        mSuspectPhone = "";
        mPoliceName = "";
        mPoliceNumber = "";
    }

    public Crime(UUID id) {
        mId = id;
        mDate = new Date();
        mTitle = "";
        mSolved = false;
        mSuspect = "";
        mSuspectPhone = "";
        mPoliceName = "";
        mPoliceNumber = "";
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public String getSuspect() {
        return mSuspect;
    }

    public void setSuspect(String suspect) {
        mSuspect = suspect;
    }

    public String getSuspectPhone() {
        return mSuspectPhone;
    }

    public void setSuspectPhone(String suspectPhone) {
        mSuspectPhone = suspectPhone;
    }

    public String getPoliceName() {
        return mPoliceName;
    }

    public void setPoliceName(String policeName) {
        mPoliceName = policeName;
    }

    public String getPoliceNumber() {
        return mPoliceNumber;
    }

    public void setPoliceNumber(String policeNumber) {
        mPoliceNumber = policeNumber;
    }

    public String getPhotoFilename() {
        return "IMG_" + mId.toString() + ".jpg";
    }
}
