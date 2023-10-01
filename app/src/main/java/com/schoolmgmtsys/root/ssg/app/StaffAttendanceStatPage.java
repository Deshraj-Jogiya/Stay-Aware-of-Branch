package com.schoolmgmtsys.root.ssg.app;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.app.TimePickerDialog;
//import android.widget.TimePicker;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.schoolmgmtsys.root.ssg.models.AttendanceStatModel;
import com.schoolmgmtsys.root.ssg.models.StaffAttendanceStatModel;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.schoolmgmtsys.root.ssg.utils.DrawerListFragment;
import com.schoolmgmtsys.root.ssg.utils.MultiTextWatcher;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.helpers.ListManager;
import com.schoolmgmtsys.root.ssg.fonts.ParentStyledTextView;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class StaffAttendanceStatPage extends SlidingFragmentActivity implements ListManager.ListInterface {

    public static HashMap<String, String> statusIdentifierKeyFirst;
    private static com.orhanobut.dialogplus.ListHolder ListHolder;
    private ArrayList<StaffAttendanceStatModel> attendanceList;
    private ListView ViewList;
    private ListManager mListManager;
    private AttendanceStatHolder holder;
    private StaffAttendanceStatModel posValues;
    private Integer Res_PageLayout = R.layout.page_staff_attendance_stat;
    private Integer Res_PageList = R.id.staff_attendance_view_list;
    private Integer Res_PageItemList = R.layout.page_staff_attendance_stat_list_item;
    private ProgressBar mProgressBar;
    private String attendanceDay;
    private TimePickerDialog picker1;
    private TimePickerDialog picker2;
    private TimePickerDialog PickerDialog;
    private Calendar calendar;
    private int currentHour;
    private int currentMinute;

    private String amPm;
    private int hour;
    private int minutes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Concurrent.getLangDirection(this).equals("ar"))
            getSlidingMenu().setMode(SlidingMenu.RIGHT);
        else getSlidingMenu().setMode(SlidingMenu.LEFT);

        setContentView(Res_PageLayout);

        statusIdentifierKeyFirst = new HashMap<>();
        statusIdentifierKeyFirst.put("All", Concurrent.getLangSubWords("all", "All"));
        statusIdentifierKeyFirst.put("1", Concurrent.getLangSubWords("Present", "Present"));
        statusIdentifierKeyFirst.put("0", Concurrent.getLangSubWords("Absent", "Absent"));
        statusIdentifierKeyFirst.put("2", Concurrent.getLangSubWords("Late", "Late"));
        statusIdentifierKeyFirst.put("3", Concurrent.getLangSubWords("LateExecuse", "Late with excuse"));
        statusIdentifierKeyFirst.put("4", Concurrent.getLangSubWords("earlyDismissal", "Early Dismissal"));
        statusIdentifierKeyFirst.put("9", Concurrent.getLangSubWords("acceptedVacation", "Accepted Vacation"));


        attendanceList = getIntent().getParcelableArrayListExtra("staffAttendanceList");
        attendanceDay = getIntent().getStringExtra("attendanceDay");

        getSlidingMenu().setBehindWidth((int) Math.round(Concurrent.getScreenWidth(this) * 70.0 / 100.0));
        setBehindContentView(R.layout.drawer_frame);

        ImageView backImage = (ImageView) findViewById(R.id.background_img);
        FrameLayout logBack = (FrameLayout) findViewById(R.id.full_layout);

        Resources res = getResources();
        if (!res.getBoolean(R.bool.x_gen_back_is_image)) {
            backImage.setVisibility(View.GONE);
            logBack.setBackgroundColor(Concurrent.getColor(this, R.color.x_gen_back));
        }

        ListFragment mFrag;
        FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
        if (savedInstanceState == null) {
            mFrag = new DrawerListFragment();
        } else {
            mFrag = (ListFragment) this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
        }
        t.replace(R.id.menu_frame, mFrag);
        t.commit();

        mProgressBar = (ProgressBar) findViewById(R.id.gen_loader);

        TextView HeadTitle = (TextView) findViewById(R.id.head_drawer_title);
        HeadTitle.setText(Concurrent.getLangSubWords("staffAttendance", "Staff Attendance"));

        ImageView ToogleBtn = (ImageView) findViewById(R.id.head_drawer_toggle);
        ToogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        mProgressBar.setVisibility(View.INVISIBLE);

        ViewList = (ListView) findViewById(Res_PageList);

        ViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });


        LinearLayout SaveEdits = (LinearLayout) findViewById(R.id.footer);
        TextView WaitingApproveTxt = (TextView) findViewById(R.id.wait_approve_txt);
        WaitingApproveTxt.setText(Concurrent.getLangSubWords("saveAttendance", "Save attendance"));

        SaveEdits.setOnClickListener(new View.OnClickListener() {
            public String TOKEN;

            @Override
            public void onClick(View view) {
                if (attendanceList != null && attendanceList.size() > 0) {
                    TOKEN = Concurrent.getAppToken(StaffAttendanceStatPage.this);
                    if (TOKEN != null) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        if (attendanceDay != null && attendanceList != null) {

                            JsonObject json = new JsonObject();
                            json.addProperty("attendanceDay", attendanceDay);
                            json.add("stAttendance", prepareAttendanceList());

                            Ion.with(StaffAttendanceStatPage.this).load(OkHttpClient.strip(App.getAppBaseUrl() + Constants.TASK_EDIT_STAFF_ATTENDANCE)).setTimeout(10000).setJsonObjectBody(json)
                                    .asJsonObject().setCallback(new FutureCallback<JsonObject>() {


                                public String ResultStatus;

                                @Override
                                public void onCompleted(Exception exception, JsonObject ValuesHolder) {
                                    if (exception == null) {
                                        ResultStatus = Concurrent.tagsStringValidator(ValuesHolder, "status");


                                        if (ResultStatus != null && ResultStatus.equals("success")) {
                                            Toast.makeText(StaffAttendanceStatPage.this, Concurrent.getLangSubWords("attendanceSaved", "Attendance Saved"), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(StaffAttendanceStatPage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(StaffAttendanceStatPage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();

                                    }
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                }
                            });

                        }
                    }
                } else {
                    Toast.makeText(StaffAttendanceStatPage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        });

        mListManager = new ListManager(this, ViewList, this, attendanceList);
        mListManager.removeFooter();

        findViewById(R.id.error_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StaffAttendanceStatPage.super.onBackPressed();
            }
        });
        findViewById(R.id.empty_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StaffAttendanceStatPage.super.onBackPressed();
            }
        });

        if (attendanceList != null) {
            if (attendanceList.size() > 0) {
                mListManager.setDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                SaveEdits.setVisibility(View.VISIBLE);
            } else {
                mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                SaveEdits.setVisibility(View.GONE);
            }
        } else {
            mListManager.setErrorView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
            SaveEdits.setVisibility(View.GONE);
        }
    }

    public JsonArray prepareAttendanceList() {
        JsonArray jsonArray = new JsonArray();
        JsonObject innerObject;

        for (StaffAttendanceStatModel attendItem : attendanceList) {
            innerObject = new JsonObject();
            innerObject.addProperty("name", attendItem.TeacherName);
            innerObject.addProperty("attendance", attendItem.StatusId);
            innerObject.addProperty("id", attendItem.id);
            innerObject.addProperty("check_in_time", attendItem.check_in_time);
            innerObject.addProperty("check_out_time", attendItem.check_out_time);
            innerObject.addProperty("attNotes", attendItem.attNotes);

            if (attendItem.vacation != null && attendItem.vacation.equals("true")) {
                innerObject.addProperty("vacation", attendItem.vacation);
                innerObject.addProperty("vacationStat", attendItem.vacationStat);
            }

            jsonArray.add(innerObject);
        }
        return jsonArray;
    }

    @Override
    public void loadMore() {

    }

    @Override
    public void AdapterConstructor() {

    }

    @Override
    public View AdapterGetView(final int position, View convertView) {
        LayoutInflater inflater = getLayoutInflater();
        if (convertView == null || convertView.getTag() == null) {
            holder = new AttendanceStatHolder();
            convertView = inflater.inflate(Res_PageItemList, null);

            holder.TeacherName = (ParentStyledTextView) convertView.findViewById(R.id.header_title);
            holder.TeacherStatus = (ParentStyledTextView) convertView.findViewById(R.id.attend_data);
            holder.Status_0 = (RelativeLayout) convertView.findViewById(R.id.status_0);
            holder.Status_1 = (RelativeLayout) convertView.findViewById(R.id.status_1);
            holder.Status_2 = (RelativeLayout) convertView.findViewById(R.id.status_2);
            holder.Status_3 = (RelativeLayout) convertView.findViewById(R.id.status_3);

            holder.Status_txt_0 = (TextView) convertView.findViewById(R.id.Present);
            holder.Status_txt_1 = (TextView) convertView.findViewById(R.id.Absent);
            holder.Status_txt_2 = (TextView) convertView.findViewById(R.id.Late);
            holder.Status_txt_3 = (TextView) convertView.findViewById(R.id.LateExecuse);

            holder.VacationSelectCon = (FlowLayout) convertView.findViewById(R.id.vacation_con);
            holder.AttendanceSelectCon = (FlowLayout) convertView.findViewById(R.id.attendance_con);
            holder.VacationStateCon = (LinearLayout) convertView.findViewById(R.id.vacation_state_con);
            holder.AttendanceStateCon = (LinearLayout) convertView.findViewById(R.id.attendance_state_con);

            holder.VacationStateData = (ParentStyledTextView) convertView.findViewById(R.id.vacation_state);
            holder.VacationAccepted = (RelativeLayout) convertView.findViewById(R.id.vacation_1);
            holder.VacationRejected = (RelativeLayout) convertView.findViewById(R.id.vacation_0);

            holder.checkin = (EditText) convertView.findViewById(R.id.checkin_staff);
            holder.checkout = (EditText) convertView.findViewById(R.id.checkout_staff);
            holder.notes = (EditText) convertView.findViewById(R.id.notes_staff);

            convertView.setTag(holder);
        } else {
            holder = (AttendanceStatHolder) convertView.getTag();
        }
        posValues = attendanceList.get(position);

        if (posValues != null) {
            holder.Status_0.setTag(position);
            holder.Status_1.setTag(position);
            holder.Status_2.setTag(position);
            holder.Status_3.setTag(position);
            holder.checkin.setTag(position);
            holder.checkout.setTag(position);
            holder.notes.setTag(position);
            holder.VacationAccepted.setTag(position);
            holder.VacationRejected.setTag(position);

            holder.TeacherName.setNotNullText(posValues.TeacherName);
            holder.TeacherStatus.setNotNullText(posValues.TeacherStatus);

            holder.Status_0.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    changeStatus(view, "1");
                }
            });
            holder.Status_1.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    //changeStatus(view, "0");
                    holder.checkin.setText("");
                    holder.checkout.setText("");
                    holder.notes.setText("");
                    attendanceList.get(position).StatusId = "0";
                    attendanceList.get(position).updateStatus();
                    attendanceList.get(position).check_in_time = "";
                    attendanceList.get(position).check_out_time = "";
                    attendanceList.get(position).attNotes = "";
                    mListManager.getListAdapter().notifyDataSetChanged();
                }
            });
            holder.Status_2.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    changeStatus(view, "2");
                }
            });
            holder.Status_3.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    changeStatus(view, "3");
                }
            });

            holder.VacationAccepted.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    changeVacationStatus(view, "1");
                }
            });
            holder.VacationRejected.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    changeVacationStatus(view, "0");
                }
            });


            // There vacation
            if (posValues.vacation != null && posValues.vacation.equals("true")) {

                holder.VacationSelectCon.setVisibility(View.VISIBLE);
                holder.VacationStateCon.setVisibility(View.VISIBLE);

                holder.AttendanceSelectCon.setVisibility(View.GONE);
                holder.AttendanceStateCon.setVisibility(View.GONE);

                if (posValues.vacationStat != null) {
                    if (posValues.vacationStat.equals("-1")) {
                        holder.VacationStateData.setText("NA");
                    } else if (posValues.vacationStat.equals("1")) {
                        holder.VacationStateData.setText("Accepted Vacation");
                    } else if (posValues.vacationStat.equals("0")) {
                        holder.VacationStateData.setText("Rejected Vacation");
                        holder.AttendanceSelectCon.setVisibility(View.VISIBLE);
                        holder.AttendanceStateCon.setVisibility(View.VISIBLE);
                        if (posValues.TeacherStatus == null) {
                            holder.TeacherStatus.setText("NA");
                        } else {
                            holder.TeacherStatus.setNotNullText(posValues.TeacherStatus);
                        }
                    }
                }
            } else {
                holder.VacationSelectCon.setVisibility(View.GONE);
                holder.VacationStateCon.setVisibility(View.GONE);

                holder.AttendanceSelectCon.setVisibility(View.VISIBLE);
                holder.AttendanceStateCon.setVisibility(View.VISIBLE);

            }
        }

        if(posValues.check_in_time != null && !posValues.check_in_time.equalsIgnoreCase("")){
            String checkins = posValues.check_in_time.replaceAll("\"", "");
            if(!checkins.equals("")){
                holder.checkin.setText(checkins.replaceAll("\"", ""));
            }
           // Log.d("CheckINNNNNNNNNNN Time",posValues.check_in_time);
        }
        if(posValues.check_out_time != null && !posValues.check_out_time.equalsIgnoreCase("")){
            String checkouts = posValues.check_out_time.replaceAll("\"", "");
            if(!checkouts.equals("")){
                holder.checkout.setText(checkouts.replaceAll("\"", ""));
            }
           // Log.d("CheckOUTTTTTT Time",posValues.check_out_time);
        }
        if(posValues.attNotes != null && !posValues.attNotes.equalsIgnoreCase("")){
            String notess = posValues.attNotes.replaceAll("\"", "");
            //holder.notes.setTextDirection();
            if(!notess.equals("")){
                holder.notes.setText(notess.replaceAll("\"", ""));
            }
        }

        new MultiTextWatcher().registerEditText(holder.notes).setCallback(new MultiTextWatcher.TextWatcherWithInstance() {
            public void onTextChanged(EditText editText, CharSequence s, int start, int before, int count) {
                editText.setSelection(editText.getText().length());
                attendanceList.get(position).StatusId = "1";
                attendanceList.get(position).updateStatus();
                StaffAttendanceStatPage.this.savenotetexts(editText, s.toString());
            }
        });
        //this.attendanceList.get(position).attNotes = text;
        holder.checkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar = Calendar.getInstance();
                currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                currentMinute = calendar.get(Calendar.MINUTE);

                PickerDialog = new TimePickerDialog(StaffAttendanceStatPage.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
//                                if(hourOfDay < 12) {
//                                    amPm = "AM";
//                                } else {
//                                    amPm = "PM";
//                                }
                                String hourString = "";
                                if(hourOfDay < 12 && hourOfDay != 0) {
                                    hourString = hourOfDay < 10 ? "0"+hourOfDay : ""+hourOfDay;
                                }else if(hourOfDay == 12) {
                                    hourString = "12";
                                }else if(hourOfDay == 0) {
                                    hourString = "12";
                                }else {
                                    hourString = (hourOfDay - 12) < 10 ? "0"+(hourOfDay - 12) : ""+(hourOfDay - 12);
                                }
                                String minuteString = minute < 10 ? "0"+minute : ""+minute;
                                String am_pm = (hourOfDay < 12) ? "AM" : "PM";
                                String time = hourString+":"+minuteString + " " + am_pm;
                                holder.checkin.setText(time);
                                attendanceList.get(position).StatusId = "1";
                                attendanceList.get(position).updateStatus();
                                attendanceList.get(position).check_in_time = time.replaceAll("\"", "");
                                mListManager.getListAdapter().notifyDataSetChanged();
                            }
                        }, currentHour, currentMinute, false);
                PickerDialog.show();
            }
        });

        holder.checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar = Calendar.getInstance();
                currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                currentMinute = calendar.get(Calendar.MINUTE);

                PickerDialog = new TimePickerDialog(StaffAttendanceStatPage.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
//                                if(hourOfDay < 12) {
//                                    amPm = "AM";
//                                } else {
//                                    amPm = "PM";
//                                }
                                String hourString = "";
                                if(hourOfDay < 12 && hourOfDay != 0) {
                                    hourString = hourOfDay < 10 ? "0"+hourOfDay : ""+hourOfDay;
                                } else if(hourOfDay == 12) {
                                    hourString = "12";
                                }else if(hourOfDay == 0) {
                                    hourString = "12";
                                }else {
                                    hourString = (hourOfDay - 12) < 10 ? "0"+(hourOfDay - 12) : ""+(hourOfDay - 12);
                                }
                                String minuteString = minute < 10 ? "0"+minute : ""+minute;
                                String am_pm = (hourOfDay < 12) ? "AM" : "PM";
                                String time = hourString+":"+minuteString + " " + am_pm;
                                holder.checkout.setText(time);
                                attendanceList.get(position).StatusId = "1";
                                attendanceList.get(position).updateStatus();
                                attendanceList.get(position).check_out_time = time.replaceAll("\"", "");

                                mListManager.getListAdapter().notifyDataSetChanged();
                            }
                        }, currentHour, currentMinute, false);
                PickerDialog.show();
            }
        });
        Concurrent.setLangWords(this, holder.Status_txt_0, holder.Status_txt_1, holder.Status_txt_2, holder.Status_txt_3);

        return convertView;
    }

    public void changeStatus(View view, String Status) {
        attendanceList.get((Integer) view.getTag()).StatusId = Status;
        attendanceList.get((Integer) view.getTag()).updateStatus();
        mListManager.getListAdapter().notifyDataSetChanged();
    }

    public void changeVacationStatus(View view, String Status) {
        attendanceList.get((Integer) view.getTag()).vacationStat = Status;
        attendanceList.get((Integer) view.getTag()).TeacherStatus = null;
        mListManager.getListAdapter().notifyDataSetChanged();
    }

    public void changeCheckinText(View view, String text) {
        attendanceList.get((Integer) view.getTag()).check_in_time = text;
        mListManager.getListAdapter().notifyDataSetChanged();
    }

    public void changeCheckOutText(View view, String text) {
        attendanceList.get((Integer) view.getTag()).check_out_time = text;
        mListManager.getListAdapter().notifyDataSetChanged();
    }

    public void savenotetexts(View view, String text) {
        attendanceList.get((Integer) view.getTag()).attNotes = text;
        mListManager.getListAdapter().notifyDataSetChanged();
    }

    class AttendanceStatHolder {
        ParentStyledTextView TeacherName;
        ParentStyledTextView TeacherStatus;
        RelativeLayout Status_0;
        RelativeLayout Status_1;
        RelativeLayout Status_2;
        RelativeLayout Status_3;

        TextView Status_txt_0;
        TextView Status_txt_1;
        TextView Status_txt_2;
        TextView Status_txt_3;

        FlowLayout VacationSelectCon;
        FlowLayout AttendanceSelectCon;
        LinearLayout VacationStateCon;
        LinearLayout AttendanceStateCon;

        ParentStyledTextView VacationStateData;
        RelativeLayout VacationAccepted;
        RelativeLayout VacationRejected;

        EditText checkin;
        EditText checkout;
        EditText notes;

    }

}
