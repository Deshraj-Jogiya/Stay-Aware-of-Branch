package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.schoolmgmtsys.root.ssg.app.AttendancePage;

public class AttendanceStatModel implements Parcelable {
    public static final Creator<AttendanceStatModel> CREATOR = new Creator<AttendanceStatModel>() {
        public AttendanceStatModel createFromParcel(Parcel source) {
            return new AttendanceStatModel(source);
        }

        public AttendanceStatModel[] newArray(int size) {
            return new AttendanceStatModel[size];
        }
    };
    public String Status;
    public String StatusId;
    public String StudentId;
    public String StudentName;
    public String StudentRollId;
    public String BalanceLesson;
    public String attBook;
    public String attCertificate;
    public String attNotes;
    public Boolean attendance;
    public Boolean absent;
    public Boolean fee;
    public Boolean makeup;
    public Boolean break2;
    public String vacation;
    public String vacationStat;
    public String attSpeed;

    public AttendanceStatModel(String StudentName2, String Status2, String StudentRollId2,String BalanceLesson, String StudentId2, String vacation2, String vacationStat2, Boolean attendance2,Boolean absent, Boolean fee2, Boolean makeup2,Boolean break2, String attBook2, String attCertificate2, String attNotes2,String attSpeed) {
        this.StudentName = StudentName2;
        this.Status = (String) AttendancePage.statusIdentifierKeyFirst.get(String.valueOf(Status2));
        this.StatusId = Status2;
        this.StudentRollId = StudentRollId2;
        this.BalanceLesson = BalanceLesson;
        this.StudentId = StudentId2;
        this.vacation = vacation2;
        this.vacationStat = vacationStat2;
        this.attendance = attendance2;
        this.absent = absent;
        this.fee = fee2;
        this.makeup = makeup2;
        this.break2 = break2;
        this.attBook = attBook2;
        this.attCertificate = attCertificate2;
        this.attNotes = attNotes2;
        this.attSpeed=attSpeed;

    }

    public void updateStatus() {
        this.Status = (String) AttendancePage.statusIdentifierKeyFirst.get(String.valueOf(this.StatusId));
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.StudentName);
        dest.writeString(this.Status);
        dest.writeString(this.StatusId);
        dest.writeString(this.StudentRollId);
        dest.writeString(this.BalanceLesson);
        dest.writeString(this.StudentId);
        dest.writeString(this.vacation);
        dest.writeString(this.vacationStat);
        dest.writeByte(this.attendance.booleanValue() ? (byte) 1 : 0);
        dest.writeByte(this.absent.booleanValue() ? (byte) 1 : 0);
        dest.writeByte(this.fee.booleanValue() ? (byte) 1 : 0);
        dest.writeByte(this.makeup.booleanValue() ? (byte) 1 : 0);
        dest.writeByte(this.break2.booleanValue() ? (byte) 1 : 0);
        dest.writeString(this.attBook);
        dest.writeString(this.attCertificate);
        dest.writeString(this.attNotes);
        dest.writeString(this.attSpeed);
    }

    protected AttendanceStatModel(Parcel in) {
        this.StudentName = in.readString();
        this.Status = in.readString();
        this.StatusId = in.readString();
        this.StudentRollId = in.readString();
        this.BalanceLesson = in.readString();
        this.StudentId = in.readString();
        this.vacation = in.readString();
        this.vacationStat = in.readString();
        boolean z = false;
        this.attendance = Boolean.valueOf(in.readByte() != 0);
        this.absent = Boolean.valueOf(in.readByte() != 0);
        this.fee = Boolean.valueOf(in.readByte() != 0);
        if (in.readByte() != 0) {
            z = true;
        }
        this.makeup = Boolean.valueOf(z);
        this.break2 = Boolean.valueOf(in.readByte() != 0);
        this.attBook = in.readString();
        this.attCertificate = in.readString();
        this.attNotes = in.readString();
        this.attSpeed = in.readString();
    }
}
