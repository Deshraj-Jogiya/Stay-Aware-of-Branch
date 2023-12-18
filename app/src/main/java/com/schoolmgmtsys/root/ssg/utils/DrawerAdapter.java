package com.schoolmgmtsys.root.ssg.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.schoolmgmtsys.root.ssg.app.AssignmentsPage;
import com.schoolmgmtsys.root.ssg.app.AttendancePage;
import com.schoolmgmtsys.root.ssg.app.ClassesPage;
import com.schoolmgmtsys.root.ssg.app.ControlActivity;
import com.schoolmgmtsys.root.ssg.app.DuePaymentsPage;
import com.schoolmgmtsys.root.ssg.app.EventsPage;
import com.schoolmgmtsys.root.ssg.app.ExamsPage;
import com.schoolmgmtsys.root.ssg.app.GradesPage;
import com.schoolmgmtsys.root.ssg.app.HomeworkPage;
import com.schoolmgmtsys.root.ssg.app.HostelPage;
import com.schoolmgmtsys.root.ssg.app.LibraryPage;
import com.schoolmgmtsys.root.ssg.app.LoginPage;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.app.MediaCenterPage;
import com.schoolmgmtsys.root.ssg.app.NewsBoardPage;
import com.schoolmgmtsys.root.ssg.app.OnlineExamsPage;
import com.schoolmgmtsys.root.ssg.app.ParentsPage;
import com.schoolmgmtsys.root.ssg.app.PaymentsPage;
import com.schoolmgmtsys.root.ssg.app.SectionsPage;
import com.schoolmgmtsys.root.ssg.app.SplashPage;
import com.schoolmgmtsys.root.ssg.app.StaffAttendancePage;
import com.schoolmgmtsys.root.ssg.app.StaticPagesPage;
import com.schoolmgmtsys.root.ssg.app.StudentAttendancePage;
import com.schoolmgmtsys.root.ssg.app.StudentsPage;
import com.schoolmgmtsys.root.ssg.app.StudyMaterialPage;
import com.schoolmgmtsys.root.ssg.app.SubjectsPage;
import com.schoolmgmtsys.root.ssg.app.TeachersPage;
import com.schoolmgmtsys.root.ssg.app.TransportPage;
import com.schoolmgmtsys.root.ssg.app.ViewImagePage;
import com.schoolmgmtsys.root.ssg.messages.MessagesDialogsActivity;

@SuppressLint("CutPasteId")
public class DrawerAdapter extends ArrayAdapter<DrawerItemModel> {


    private DrawerItemModel customObj;

    public DrawerAdapter(Context context) {
        super(context, 0);

        Concurrent.drawerItems.clear();
        Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "dashboard", R.string.dashboard, SplashPage.class, "img", R.drawable.dash_tab_stat_unselected, 2, 2));

        if (Concurrent.isUserHavePermission(context, "staticPages.list")) {
            if (Concurrent.isModuleActivated(context, "staticPages") || Concurrent.isModuleActivated(context, "staticpagesAct"))
                Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "staticPages", R.string.static_pages, StaticPagesPage.class, "img", R.drawable.pages_g, 2, 3));
        }

        if (Concurrent.isModuleActivated(context, "messagesAct"))
            Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "Messages", R.string.calender, MessagesDialogsActivity.class, "img", R.drawable.news, 2, 50));

        if (Concurrent.isModuleActivated(context, "calendarAct"))
            Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "Calender", R.string.messages, ViewImagePage.class, "img", R.drawable.calender_g, 2, 5));

        if (Concurrent.isUserHavePermission(context, "Homework.list", "Homework.View")) {
            if (Concurrent.isModuleActivated(context, "homeworkAct")){
                Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "Homework", R.string.homework, HomeworkPage.class, "img", R.drawable.answers_g, 2, 56));
            }
        }
        if (Concurrent.isModuleActivated(context, "attendanceAct")) {
            if (Concurrent.isUserHavePermission(context, "Attendance.takeAttendance")) {
                Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "Attendance", R.string.attendance, AttendancePage.class, "img", R.drawable.attend_g, 2, 6));
            }else{
                if (Concurrent.isUserHavePermission(context, "myAttendance.myAttendance","students.Attendance") ) {
                    if (Concurrent.getAppRole(getContext()) == Concurrent.APP_ROLE_STUDENT) {
                        Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "Attendance", R.string.attendance, StudentAttendancePage.class, "img", R.drawable.attend_g, 2, 6));
                    }else if (Concurrent.getAppRole(getContext()) == Concurrent.APP_ROLE_PARENT) {
                        customObj = new DrawerItemModel(getContext(), "Attendance", R.string.attendance, ControlActivity.class, "img", R.drawable.attend_g, 2, 6);
                        customObj.setActivityTarget("ParentsAttendance");
                        customObj.setHeadText("Attendance", "Attendance");
                        Concurrent.drawerItems.add(customObj);
                    }
                }
            }
        }


        if (Concurrent.isUserHavePermission(context, "staffAttendance.takeAttendance")) {
            if (Concurrent.isModuleActivated(context, "staffAttendanceAct")) {
                Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "staffAttendance", R.string.staff_attendance, StaffAttendancePage.class, "img", R.drawable.ic_users, 2, 35));
            }
        }

        if (Concurrent.isUserHavePermission(context, "Library.list")) {
            if (Concurrent.isModuleActivated(context, "bookslibraryAct"))
                Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "booksLibrary", R.string.library, LibraryPage.class, "img", R.drawable.subject, 2, 8));
        }

        if (Concurrent.isUserHavePermission(context, "teachers.list")) {
            Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "teachers", R.string.teachers, TeachersPage.class, "img", R.drawable.teacher_g, 2, 10));
        }

        if (Concurrent.isUserHavePermission(context, "students.list")) {
            Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "students", R.string.students, StudentsPage.class, "img", R.drawable.username_g, 2, 11));
        }

        if (Concurrent.isUserHavePermission(context, "parents.list")) {
            Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "parents", R.string.parents, ParentsPage.class, "img", R.drawable.parent_g, 2, 12));
        }
        if (Concurrent.isUserHavePermission(context, "gradeLevels.list")) {
            Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "gradeLevels", R.string.gradeLevels, GradesPage.class, "img", R.drawable.marksheet_g, 2, 13));
        }

        if (Concurrent.isUserHavePermission(context, "Assignments.list")) {
            if (Concurrent.isModuleActivated(context, "assignmentsAct"))
                Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "Assignments", R.string.assignments, AssignmentsPage.class, "img", R.drawable.answers_g, 2, 14));
        }


        if (Concurrent.isUserHavePermission(context, "examsList.list","examsList.View")) {
            Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "examsList", R.string.exams_list, ExamsPage.class, "img", R.drawable.exam_g, 2, 15));
        }

        if (Concurrent.isUserHavePermission(context, "onlineExams.list")) {
            if (Concurrent.isModuleActivated(context, "onlineexamsAct"))
                Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "onlineExams", R.string.online_exams, OnlineExamsPage.class, "img", R.drawable.online_exam_g, 2, 16));
        }

        if (Concurrent.isUserHavePermission(context, "newsboard.list","newsboard.View")) {
            if (Concurrent.isModuleActivated(context, "newsboardAct"))
                Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "newsboard", R.string.news_board, NewsBoardPage.class, "img", R.drawable.news_g, 2, 17));
        }
        if (Concurrent.isUserHavePermission(context, "events.list","events.View")) {
            if (Concurrent.isModuleActivated(context, "eventsAct"))
                Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "events", R.string.events, EventsPage.class, "img", R.drawable.place_g, 2, 18));
        }


        if (Concurrent.isUserHavePermission(context, "Invoices.list","Invoices.View")) {
            if (Concurrent.isModuleActivated(context, "paymentsAct"))
                Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "Invoices", R.string.payments, PaymentsPage.class, "img", R.drawable.price_g, 2, 19));
        }

        if (Concurrent.isUserHavePermission(context, "Invoices.dueInvoices")) {
            if (Concurrent.isModuleActivated(context, "paymentsAct"))
                Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "Due Invoices", R.string.due_payments, DuePaymentsPage.class, "img", R.drawable.price_g, 2, 56));
        }


        if (Concurrent.isUserHavePermission(context, "classes.list")) {
            Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "classes", R.string.classes, ClassesPage.class, "img", R.drawable.classes_g, 2, 21));
        }

        if (Concurrent.isUserHavePermission(context, "sections.list")) {
            Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "sections", R.string.sections, SectionsPage.class, "img", R.drawable.sections_g, 2, 32));
        }

        if (Concurrent.isUserHavePermission(context, "Transportation.list")) {
            if (Concurrent.isModuleActivated(context, "transportAct"))
                Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "Transport", R.string.transport, TransportPage.class, "img", R.drawable.tansport_g, 2, 33));
        }

        if (Concurrent.isUserHavePermission(context, "Hostel.list")) {
            if (Concurrent.isModuleActivated(context, "hostelAct"))
                Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "Hostel", R.string.hostel, HostelPage.class, "img", R.drawable.hostel_g, 2, 34));
        }

        if (Concurrent.isUserHavePermission(context, "mediaCenter.View")) {
            if (Concurrent.isModuleActivated(context, "mediaAct"))
                Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "mediaCenter", R.string.media_center, MediaCenterPage.class, "img", R.drawable.media_g, 2, 35));
        }

        if (Concurrent.isUserHavePermission(context, "Subjects.list")) {
            Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "Subjects", R.string.subjects, SubjectsPage.class, "img", R.drawable.subject_g, 2, 22));
        }

        if (Concurrent.isUserHavePermission(context, "classSch.list")) {
            if (Concurrent.isModuleActivated(context, "classSchAct")) {
                customObj = new DrawerItemModel(getContext(), "ClassSchedule", R.string.classes_schedule, ViewImagePage.class, "img", R.drawable.date_g, 2, 30);
                customObj.setActivityTarget("ClassesSchPage");
              //  customObj.setHeadText("ClassSchedule", "Class Schedule");
                Concurrent.drawerItems.add(customObj);
            }
        }

        if (Concurrent.isUserHavePermission(context, "studyMaterial.list")) {
            if (Concurrent.isModuleActivated(context, "materialsAct"))
                Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "studyMaterial", R.string.study_materials, StudyMaterialPage.class, "img", R.drawable.material_g, 2, 31));
        }

        if (Concurrent.isUserHavePermission(context, "Marksheet.Marksheet","students.Marksheet")) {
            if (Concurrent.getAppRole(getContext()) == Concurrent.APP_ROLE_STUDENT) {
                customObj = new DrawerItemModel(getContext(), "Marksheet", R.string.mark_sheet, ControlActivity.class, "img", R.drawable.attend_g, 2, 38);
                customObj.setActivityTarget("studentShowMarks");
                customObj.setHeadText("Marksheet", "Marksheet");
                customObj.appendUserId = true;
                Concurrent.drawerItems.add(customObj);
            }
        }

        Concurrent.drawerItems.add(new DrawerItemModel(getContext(), "logout", R.string.Logout, LoginPage.class, "img", R.drawable.logout_g, 2, 1));
    }

    /*
        public void addItem(int catID, int imgRes, int title, int type, int identifier) {
            Concurrent.drawerItems.add(new DrawerItemModel(catID, imgRes, title, type, identifier));
        }
    */
    public void addItem(DrawerItemModel itemModel) {
        Concurrent.drawerItems.add(itemModel);
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getCount() {
        return Concurrent.drawerItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return Concurrent.drawerItems.get(position).type;
    }

    @Override
    public boolean isEnabled(int position) {
        return Concurrent.drawerItems.get(position).type == 2 ? true : false;
    }

    @SuppressLint("CutPasteId")
    public View getView(int position, View view, ViewGroup parent) {
        final DrawerItemModel item = Concurrent.drawerItems.get(position);

        ItemViewHolder ItemsHolder;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.drawer_item, null);

            ItemsHolder = new ItemViewHolder();
            ItemsHolder.itemsHolder = (RelativeLayout) view.findViewById(R.id.drawer_item_holder);
            ItemsHolder.textHolder = (TextView) view.findViewById(R.id.drawer_item_title);
            ItemsHolder.imgHolder = (ImageView) view.findViewById(R.id.drawer_item_icon);
            view.setTag(ItemsHolder);
        } else
            ItemsHolder = (ItemViewHolder) view.getTag();

        ItemsHolder.textHolder.setText(Concurrent.getLangSubWords(item.Title, item.Title));


        ItemsHolder.itemsHolder.setOnClickListener(new RelativeLayout.OnClickListener() {
            public void onClick(final View v) {
                if (item.referClass != null) {
                    if (item.identifier == 1) {
                        SharedPreferences Prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                        Prefs.edit().remove("app_username").apply();
                        Prefs.edit().remove("app_password").apply();
                        Intent MyIntent = new Intent(getContext(), LoginPage.class);
                        MyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        MyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        getContext().startActivity(MyIntent);
                        ((Activity) getContext()).finish();
                    } else {

                        Class<?> clazz = item.referClass;
                        Intent MyIntent = new Intent(getContext(), clazz);
                        if (item.targetFragment != null)
                            MyIntent.putExtra("TARGET_FRAGMENT", item.targetFragment);
                        if (item.appendUserId)
                            MyIntent.putExtra("EXTRA_INT_1", PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("app_user_id", 0));
                        if (item.findWord != null && item.replaceWord != null) {
                            MyIntent.putExtra("EXTRA_HEAD_FIND_WORD", item.findWord);
                            MyIntent.putExtra("EXTRA_HEAD_REPLACE_WORD", item.replaceWord);
                        }
                        getContext().startActivity(MyIntent);

                    }
                }


            }
        });
        if (item.resIcon != 0) ItemsHolder.imgHolder.setImageResource(item.resIcon);

        return view;
    }

    public static class ItemViewHolder {
        public TextView textHolder;
        public ImageView imgHolder;
        public RelativeLayout itemsHolder;
    }
}
