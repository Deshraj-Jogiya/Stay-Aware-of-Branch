package com.schoolmgmtsys.root.ssg.utils;

import java.util.Calendar;

public class Constants {
    public static String TASK_CSRF_REQ = "/api/csrf";
    public static String TASK_LOGIN = "/auth/authenticate";

    public static String TASK_NOTIFICATION = "/dashboard/mobnotif";

    public static String TASK_DASHBOARD = "/dashaboard";
    public static String TASK_POLL_POST = "/dashboard/polls";
    public static String TASK_SETTINGS = "/siteSettings/siteSettings";

    public static String TASK_CALENDER = "/calender";
    public static String TASK_PRIVACY = "/terms";

    public static String TASK_PROFILE_IMG = "/dashboard/profileImage";
    public static String TASK_TEACHERS_LEADERBOARD = "/teachers/leaderBoard";
    public static String TASK_STUDNETS_LEADERBOARD = "/students/leaderBoard";
    public static String TASK_THUMB_LOAD = "/uploads/media";
    public static String TASK_MEDIA_LOAD = "/uploads/media";

    public static String TASK_APPROVE_STUDENT = "/students/approveOne";
    public static String TASK_APPROVE_TEACHERS = "/teachers/approveOne";
    public static String TASK_APPROVE_PARENTS = "/parents/approveOne";


    public static String TASK_STATIC_PAGES_LIST = "/static/listAll";
    public static String TASK_STATIC_PAGES_LIST_NON_ADMIN = "/static/listUser";
    public static String TASK_ASSIGNMENTS_LIST = "/assignments/listAll";
    public static String TASK_EVENTS_LIST = "/events/listAll";
    public static String TASK_EXAMS_LIST = "/examsList/listAll";
    public static String TASK_SECTIONS_LIST = "/sections/listAll";
    public static String TASK_GRADES_LIST = "/gradeLevels/listAll";
    public static String TASK_LIBRARY_LIST = "/library/listAll";
    public static String TASK_NEWS_LIST = "/newsboard/listAll";
    public static String TASK_NEWS_ITEM = "/newsboard";
    public static String TASK_EVENTS_ITEM = "/events";
    public static String TASK_ONLINE_EXAMS_LIST = "/onlineExams/listAll";
    public static String TASK_PARENTS_LIST = "/parents/listAll";
    public static String TASK_PAY_LIST = "/invoices/listAll";
    public static String TASK_HOMEWORK_LIST = "/homeworks/listAll";

    public static String TASK_STUDENTS_LIST = "/students/listAll";
    public static String TASK_SUBJECTS_LIST = "/subjects/listAll";
    public static String TASK_TEACHERS_LIST = "/teachers/listAll";
    public static String TASK_CLASSES_LIST = "/classes/listAll";
    public static String TASK_STUDY_MATERIALS_LIST = "/materials/listAll";
    public static String TASK_ASSIGNMENT_LIST = "/assignments/listAll";
    public static String TASK_TRANSPORT_LIST = "/transports/listAll";
    public static String TASK_HOSTEL_LIST = "/hostel/listAll";
    public static String TASK_MEDIA_CENTER_LIST = "/media/listAll";
    public static String TASK_NEWSBOARD_GET_ONE = "/newsboard";

    public static String TASK_APPLY_HOMEWORK = "/homeworks/apply";
    public static String TASK_MESSAGES_DIALOGS_LIST = "/messages/listAll";
    public static String TASK_MESSAGES = "/messages";
    public static String TASK_MESSAGES_OLD = "/messages/before";
    public static String TASK_MESSAGES_NEW = "/messages/ajax";
    public static String TASK_MESSAGES_SEARCH_USER = "/messages/searchUser";

    public static String TASK_PAY_SEARCH = "/payments/search";
    public static String TASK_NEWS_SEARCH = "/newsboard/search";
    public static String TASK_LIBRARY_SEARCH = "/library/search";
    public static String TASK_TEACHERS_SEARCH = "/teachers/search";
    public static String TASK_PARENTS_SEARCH = "/parents/search";
    public static String TASK_STUDENTS_SEARCH = "/students/search";

    public static String TASK_STUDENTS = "/students";
    public static String TASK_PARENTS = "/parents";
    public static String TASK_TEACHERS = "/teachers";

    public static String TASK_STUDENTS_WAIT_APPROVE = "/students/waitingApproval";
    public static String TASK_PARENTS_WAIT_APPROVE = "/parents/waitingApproval";
    public static String TASK_TEACHERS_WAIT_APPROVE = "/teachers/waitingApproval";


    public static String TASK_CLASSES_SCH_LIST = "/classschedule/listAll";

    public static String TASK_STATIC_PAGES_CONTROL = "/static";
    public static String TASK_ASSIGN_CONTROL = "/assignments";
    public static String TASK_TRANSPORT_CONTROL = "/transports";
    public static String TASK_GRADES_LEVEL_CONTROL = "/gradeLevels";
    public static String TASK_STUDENTS_CONTROL = "/students";
    public static String TASK_PARENTS_CONTROL = "/parents";
    public static String TASK_TEACHERS_CONTROL = "/teachers";
    public static String TASK_NEWSBOARD_CONTROL = "/newsboard";
    public static String TASK_EVENTS_CONTROL = "/events";

    public static String TASK_PAY_CONTROL = "/payments";
    public static String TASK_ONLINE_EXAM_CONTROL = "/onlineExams";
    public static String TASK_MATERIAL_CONTROL = "/materials";
    public static String TASK_CLASSES_CONTROL = "/classes";
    public static String TASK_EXAMS_LIST_CONTROL = "/examsList";
    public static String TASK_SUBJECTS_CONTROL = "/subjects";
    public static String TASK_LIBRARY_CONTROL = "/library";

    public static String TASK_EXAM_SUBJECT_SELECT = "/dashboard/subjectList";
    public static String TASK_ATTENDANCE_SELECT_POST = "/attendance/list";
    public static String TASK_STUDENT_ATTENDANCE = "/attendance/stats";
    public static String TASK_ATTENDANCE_CLASSES_FOR_TEACHER = "/attendance/data";

    public static String TASK_ASSIGNMENT_CHECK_UPLOAD = "/assignments/checkUpload";
    public static String TASK_ASSIGNMENT_UPLOAD = "/assignments/upload";
    public static String TASK_AREA = "schstdand-1774711.1-5772204af2f0f";
    public static String PASS_CODE = "sb%$#lic";

    public static String TASK_EDIT_ATTENDANCE = "/attendance";
    public static String TASK_EDIT_STAFF_ATTENDANCE = "/sattendance";
    public static String TASK_EDIT_EXAM_MARKS = "/examsList/saveMarks";

    public static String TASK_STAFF_ATTENDANCE_SELECT_POST = "/sattendance/list";
    public static String TASK_GET_SUBJECTS = "/dashboard/subjectList";
    public static String TASK_GET_SECTIONS_SUBJECTS = "/dashboard/sectionsSubjectsList";
    public static String TASK_GET_EXAMS_MARKS = "/examsList/getMarks";
    public static String TASK_GET_STUDENT_ATTEND = "/students/attendance";
    public static String TASK_GET_STUDENT_MARKSHEET = "/students/marksheet";
    public static String TASK_GET_ASSIGN_ANSWERS = "/assignments/listAnswers";
    public static String TASK_DOWNLOAD_ASSIGN_ANSWERS = "/assignments/downloadAnswer";
    public static String TASK_ONLINE_EXAMS_MARKS = "/onlineExams/marks";
    public static String TASK_DOWNLOAD_HOMEWORK = "/homeworks/download";

    public static String TASK_EXAM_DETAILS = "/examsList";
    public static String TASK_LOAD_INVOICE_DETAILS = "/invoices/invoice";
    public static String TASK_LOAD_HOMEWORK_DETAILS = "/homeworks_view";
    public static String TASK_HOMEWORK_CONTROL = "/homeworks";

    /**
     * add id to end of link => /classschedule/5
     */
    public static String TASK_GET_CLASS_SCH = "/classschedule";

    public static int detectToday(boolean reserved) {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int CurrPage = 4;
        switch (day) {
            case Calendar.SATURDAY:
                CurrPage = 6;
                break;
            case Calendar.SUNDAY:
                CurrPage = 0;
                break;
            case Calendar.MONDAY:
                CurrPage = 1;
                break;
            case Calendar.TUESDAY:
                CurrPage = 2;
                break;
            case Calendar.WEDNESDAY:
                CurrPage = 3;
                break;
            case Calendar.THURSDAY:
                CurrPage = 4;
                break;
            case Calendar.FRIDAY:
                CurrPage = 5;
                break;
            default:
                CurrPage = 4;
                break;
        }
        if (reserved)
            return CurrPage;
        else
            return (7 - CurrPage);
    }
}
