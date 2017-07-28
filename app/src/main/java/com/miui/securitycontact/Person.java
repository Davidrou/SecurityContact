package com.miui.securitycontact;


public class Person {
    private String mName;
    private String mTel;
    private String mDepartment;

    public Person(String name, String tel, String department){
        this.mName = name;
        this.mTel = tel;
        this.mDepartment = department;
    }
    public String getmName() {
        return mName;
    }

    public String getmTel() {
        return mTel;
    }

    public String getmDepartment() {
        return mDepartment;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public void setmTel(String mTel) {
        this.mTel = mTel;
    }

    public void setmDepartment(String mDepartment) {
        this.mDepartment = mDepartment;
    }
}
