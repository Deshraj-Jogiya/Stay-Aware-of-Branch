package com.schoolmgmtsys.root.ssg.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.schoolmgmtsys.root.ssg.app.StaffAttendanceStatPage;

/**
 * Created by SolutionsBricks Mobile Dev. Team.
 */
public class StaffAttendanceStatModel implements Parcelable {

    public Integer id;
    public String TeacherName;
    public String TeacherStatus;
    public String StatusId;
    public String vacation;
    public String vacationStat;
    public String check_in_time;
    public String check_out_time;
    public String attNotes;

    public StaffAttendanceStatModel(Integer id, String TeacherName, String TeacherStatus,String vacation,String vacationStat,String check_in_time,String check_out_time, String attNotes) {
        this.id = id;
        this.TeacherName = TeacherName;
        this.TeacherStatus = TeacherStatus;
        this.vacation = vacation;
        this.vacationStat = vacationStat;
        this.check_in_time = check_in_time;
        this.check_out_time = check_out_time;
        this.attNotes = attNotes;
    }

    public void updateStatus() {
        this.TeacherStatus = StaffAttendanceStatPage.statusIdentifierKeyFirst.get(String.valueOf(StatusId));
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.TeacherName);
        dest.writeString(this.TeacherStatus);
        dest.writeString(this.StatusId);
        dest.writeString(this.vacation);
        dest.writeString(this.vacationStat);
        dest.writeString(this.check_in_time);
        dest.writeString(this.check_out_time);
        dest.writeString(this.attNotes);
    }

    protected StaffAttendanceStatModel(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.TeacherName = in.readString();
        this.TeacherStatus = in.readString();
        this.StatusId = in.readString();
        this.vacation = in.readString();
        this.vacationStat = in.readString();
        this.check_in_time = in.readString();
        this.check_out_time = in.readString();
        this.attNotes = in.readString();
    }

    public static final Creator<StaffAttendanceStatModel> CREATOR = new Creator<StaffAttendanceStatModel>() {
        @Override
        public StaffAttendanceStatModel createFromParcel(Parcel source) {
            return new StaffAttendanceStatModel(source);
        }

        @Override
        public StaffAttendanceStatModel[] newArray(int size) {
            return new StaffAttendanceStatModel[size];
        }
    };
}
