package com.schoolmgmtsys.root.ssg.models;

import com.schoolmgmtsys.root.ssg.app.StudentAttendancePage;

/**
 * Created by SolutionsBricks Mobile Dev. Team.
 */
public class StudentAttendanceModel {
    public String Date;
    public String Status;
    public String StatusId;
    public String Subject;

    public StudentAttendanceModel(String Date, String Status, String Subject) {
        this.Date = Date;
        this.Status = StudentAttendancePage.statusIdentifierKeyFirst.get(String.valueOf(Status));
        this.StatusId = Status;
        this.Subject = Subject;
    }

}
