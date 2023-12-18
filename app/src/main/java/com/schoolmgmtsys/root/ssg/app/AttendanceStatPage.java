package com.schoolmgmtsys.root.ssg.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders.Any.B;
import com.koushikdutta.ion.builder.Builders.Any.F;
import com.koushikdutta.ion.builder.LoadBuilder;


import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.fonts.ParentStyledTextView;
import com.schoolmgmtsys.root.ssg.models.AttendanceStatModel;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.schoolmgmtsys.root.ssg.utils.DrawerListFragment;
import com.schoolmgmtsys.root.ssg.utils.MultiTextWatcher;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.helpers.ListManager;
import com.solutionsbricks.solbricksframework.helpers.ListManager.ListInterface;
import io.fabric.sdk.android.services.common.AbstractSpiCall;
import java.util.ArrayList;
import java.util.Iterator;
import org.apmem.tools.layouts.FlowLayout;

public class AttendanceStatPage extends SlidingFragmentActivity implements ListInterface {
    private Integer Res_PageItemList = Integer.valueOf(R.layout.page_attendance_stat_list_item);
    private Integer Res_PageLayout = Integer.valueOf(R.layout.page_attendance_stat);
    private Integer Res_PageList = Integer.valueOf(R.id.attendance_view_list);
    private LinearLayout SaveEdits;
    private ListView ViewList;
    /* access modifiers changed from: private */
    public String attendanceDay;
    /* access modifiers changed from: private */
    public ArrayList<AttendanceStatModel> attendanceList;
    /* access modifiers changed from: private */
    public String classId;
    private AttendanceStatHolder holder;
    private ListManager mListManager;
    /* access modifiers changed from: private */
    public ProgressBar mProgressBar;
    private AttendanceStatModel posValues;
    /* access modifiers changed from: private */
    public String subjectId;

    private class AttendanceStatHolder {
        TextView Attendance;
        FlowLayout AttendanceSelectCon;
        LinearLayout AttendanceStateCon;
        CheckBox CHK_Status_txt_F1;
        CheckBox CHK_Status_txt_M2;
        CheckBox CHK_Status_txt_B1;
        ParentStyledTextView Status;
        RelativeLayout Status_0;
        RelativeLayout Status_1;
        RelativeLayout Status_2;
        RelativeLayout Status_3;
        RelativeLayout Status_4;
        CheckBox Status_txt_0;
        TextView Status_txt_3;
        TextView Status_txt_4;
        TextView Status_txt_F1;
        TextView Status_txt_M2;
        CheckBox Status_txt_ab;
        ParentStyledTextView StudentName;
        ParentStyledTextView StudentRollId;
        RelativeLayout VacationAccepted;
        RelativeLayout VacationRejected;
        FlowLayout VacationSelectCon;
        LinearLayout VacationStateCon;
        ParentStyledTextView VacationStateData;
        EditText book_text_2;
        EditText cet_text_0;
        EditText speed_writing_text;
        EditText note_text_1;
        TextView rollid;
        TextView balanceLesson;

        private AttendanceStatHolder() {
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        ListFragment mFrag;
        super.onCreate(savedInstanceState);
        if (Concurrent.getLangDirection(this).equals("ar")) {
            getSlidingMenu().setMode(1);
        } else {
            getSlidingMenu().setMode(0);
        }
        setContentView(this.Res_PageLayout.intValue());
        this.attendanceList = getIntent().getParcelableArrayListExtra("attendanceList");
        this.attendanceDay = getIntent().getStringExtra("attendanceDay");
        this.classId = getIntent().getStringExtra("classId");
        if (!Concurrent.AttendanceModelIsClass) {
            this.subjectId = getIntent().getStringExtra("subjectId");
        }
        SlidingMenu slidingMenu = getSlidingMenu();
        double intValue = (double) Concurrent.getScreenWidth(this).intValue();
        Double.isNaN(intValue);
        slidingMenu.setBehindWidth((int) Math.round((intValue * 70.0d) / 100.0d));
        setBehindContentView((int) R.layout.drawer_frame);
        ImageView backImage = (ImageView) findViewById(R.id.background_img);
        FrameLayout logBack = (FrameLayout) findViewById(R.id.full_layout);
        if (!getResources().getBoolean(R.bool.x_gen_back_is_image)) {
            backImage.setVisibility(8);
            logBack.setBackgroundColor(Concurrent.getColor(this, R.color.x_gen_back));
        }
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        if (savedInstanceState == null) {
            mFrag = new DrawerListFragment();
        } else {
            mFrag = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.menu_frame);
        }
        t.replace(R.id.menu_frame, mFrag);
        t.commit();
        this.mProgressBar = (ProgressBar) findViewById(R.id.gen_loader);
        ((TextView) findViewById(R.id.head_drawer_title)).setText(Concurrent.getLangSubWords("Attendance", "Attendance"));
        ((ImageView) findViewById(R.id.head_drawer_toggle)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                AttendanceStatPage.this.toggle();
            }
        });
        this.mProgressBar.setVisibility(4);
        this.ViewList = (ListView) findViewById(this.Res_PageList.intValue());
        this.ViewList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            }
        });
        this.SaveEdits = (LinearLayout) findViewById(R.id.footer);
        ((TextView) findViewById(R.id.wait_approve_txt)).setText(Concurrent.getLangSubWords("saveAttendance", "Save attendance"));
        this.SaveEdits.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (AttendanceStatPage.this.attendanceList == null || AttendanceStatPage.this.attendanceList.size() <= 0) {
                    Toast.makeText(AttendanceStatPage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), 1).show();
                    AttendanceStatPage.this.mProgressBar.setVisibility(4);
                } else if (Concurrent.getAppToken(AttendanceStatPage.this) != null) {
                    AttendanceStatPage.this.mProgressBar.setVisibility(0);
                    if (AttendanceStatPage.this.attendanceDay != null && AttendanceStatPage.this.attendanceList != null) {
                        JsonObject json = new JsonObject();
                        json.addProperty("attendanceDay", AttendanceStatPage.this.attendanceDay);
                        json.addProperty("classId", (Number) Integer.valueOf(AttendanceStatPage.this.classId));
                        if (!Concurrent.AttendanceModelIsClass) {
                            json.addProperty("subjectId", AttendanceStatPage.this.subjectId);
                            json.addProperty("subject", (Number) Integer.valueOf(AttendanceStatPage.this.subjectId));
                        }
                        json.add("stAttendance", AttendanceStatPage.this.prepareAttendanceList());
                        LoadBuilder with = Ion.with((Context) AttendanceStatPage.this);
                        StringBuilder sb = new StringBuilder();
                        sb.append(App.getAppBaseUrl());
                        sb.append(Constants.TASK_EDIT_ATTENDANCE);
                        ((F) ((B) ((B) with.load(OkHttpClient.strip(sb.toString()))).setTimeout(AbstractSpiCall.DEFAULT_TIMEOUT)).setJsonObjectBody(json)).asJsonObject().setCallback(new FutureCallback<JsonObject>() {
                            public String ResultStatus;

                            public void onCompleted(Exception exception, JsonObject ValuesHolder) {
                                if (exception == null) {
                                    this.ResultStatus = Concurrent.tagsStringValidator(ValuesHolder, "status");
                                    if (this.ResultStatus == null || !this.ResultStatus.equals(Param.SUCCESS)) {
                                        Toast.makeText(AttendanceStatPage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), 1).show();
                                    } else {
                                        Toast.makeText(AttendanceStatPage.this, Concurrent.getLangSubWords("attendanceSaved", "Attendance Saved"), 0).show();
                                    }
                                } else {
                                    Toast.makeText(AttendanceStatPage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), 1).show();
                                }
                                AttendanceStatPage.this.mProgressBar.setVisibility(4);
                            }
                        });
                    }
                }
            }
        });
        this.mListManager = new ListManager(this, this.ViewList, this, this.attendanceList);
        this.mListManager.removeFooter();
        findViewById(R.id.error_view).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                AttendanceStatPage.super.onBackPressed();
            }
        });
        findViewById(R.id.empty_view).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                AttendanceStatPage.super.onBackPressed();
            }
        });
        if (this.attendanceList == null) {
            this.mListManager.setErrorView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(this.Res_PageList.intValue()));
            this.SaveEdits.setVisibility(8);
        } else if (this.attendanceList.size() > 0) {
            this.mListManager.setDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(this.Res_PageList.intValue()));
            this.SaveEdits.setVisibility(0);
        } else {
            this.mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(this.Res_PageList.intValue()));
            this.SaveEdits.setVisibility(8);
        }
    }

    public JsonArray prepareAttendanceList() {
        JsonArray jsonArray = new JsonArray();
        int count = 0;
        Iterator it = this.attendanceList.iterator();
        while (it.hasNext()) {
            AttendanceStatModel attendItem = (AttendanceStatModel) it.next();
            JsonObject innerObject = new JsonObject();
            innerObject.addProperty("name", attendItem.StudentName);
            innerObject.addProperty("studentRollId", attendItem.StudentRollId);
            innerObject.addProperty("id", attendItem.StudentId);
            innerObject.addProperty("fee", attendItem.fee);
            innerObject.addProperty("makeup", attendItem.makeup);
            innerObject.addProperty("break", attendItem.break2);
            innerObject.addProperty("attendance", attendItem.attendance);
            innerObject.addProperty("absent", attendItem.absent);
            innerObject.addProperty("attBook", attendItem.attBook);
            innerObject.addProperty("attCertificate", attendItem.attCertificate);
            innerObject.addProperty("attNotes", attendItem.attNotes);
            innerObject.addProperty("attSpeed", attendItem.attSpeed);
            if (attendItem.vacation != null && attendItem.vacation.equals("true")) {
                innerObject.addProperty("vacation", attendItem.vacation);
                innerObject.addProperty("vacationStat", attendItem.vacationStat);
            }
            count++;
            jsonArray.add((JsonElement) innerObject);
        }
        return jsonArray;
    }

    public void loadMore() {
    }

    public void AdapterConstructor() {
    }

    public View AdapterGetView(int position, View convertView) {
        LayoutInflater inflater = getLayoutInflater();
        if (convertView == null || convertView.getTag() == null) {
            this.holder = new AttendanceStatHolder();
            convertView = inflater.inflate(this.Res_PageItemList.intValue(), null);
            this.holder.StudentName = (ParentStyledTextView) convertView.findViewById(R.id.header_title);
            this.holder.StudentRollId = (ParentStyledTextView) convertView.findViewById(R.id.roll_id_data);
            this.holder.Status = (ParentStyledTextView) convertView.findViewById(R.id.attend_data);
            this.holder.Attendance = (TextView) convertView.findViewById(R.id.Attendance);
            this.holder.rollid = (TextView) convertView.findViewById(R.id.rollid);
            this.holder.balanceLesson = (TextView) convertView.findViewById(R.id.BalanceLessonTotal);
            this.holder.Status_0 = (RelativeLayout) convertView.findViewById(R.id.status_0);
            this.holder.Status_1 = (RelativeLayout) convertView.findViewById(R.id.status_1);
            this.holder.Status_2 = (RelativeLayout) convertView.findViewById(R.id.status_2);
            this.holder.Status_3 = (RelativeLayout) convertView.findViewById(R.id.status_3);
            this.holder.Status_4 = (RelativeLayout) convertView.findViewById(R.id.status_4);
            this.holder.Status_txt_0 = (CheckBox) convertView.findViewById(R.id.Present);
            this.holder.Status_txt_ab = (CheckBox) convertView.findViewById(R.id.Absent);
            this.holder.cet_text_0 = (EditText) convertView.findViewById(R.id.cet_text);
            this.holder.speed_writing_text = (EditText) convertView.findViewById(R.id.speed_writing_text);
            this.holder.note_text_1 = (EditText) convertView.findViewById(R.id.note_text);
            this.holder.book_text_2 = (EditText) convertView.findViewById(R.id.book_text);
            this.holder.Status_txt_F1 = (TextView) convertView.findViewById(R.id.Fee);
            this.holder.Status_txt_M2 = (TextView) convertView.findViewById(R.id.Makeup);
            this.holder.CHK_Status_txt_F1 = (CheckBox) convertView.findViewById(R.id.chkFee);
            this.holder.CHK_Status_txt_M2 = (CheckBox) convertView.findViewById(R.id.ChkMakeup);
            this.holder.CHK_Status_txt_B1 = (CheckBox) convertView.findViewById(R.id.ChkBreak);
            this.holder.Status_txt_3 = (TextView) convertView.findViewById(R.id.LateExecuse);
            this.holder.Status_txt_4 = (TextView) convertView.findViewById(R.id.earlyDismissal);
            this.holder.VacationSelectCon = (FlowLayout) convertView.findViewById(R.id.vacation_con);
            this.holder.AttendanceSelectCon = (FlowLayout) convertView.findViewById(R.id.attendance_con);
            this.holder.VacationStateCon = (LinearLayout) convertView.findViewById(R.id.vacation_state_con);
            this.holder.AttendanceStateCon = (LinearLayout) convertView.findViewById(R.id.attendance_state_con);
            this.holder.VacationStateData = (ParentStyledTextView) convertView.findViewById(R.id.vacation_state);
            this.holder.VacationAccepted = (RelativeLayout) convertView.findViewById(R.id.vacation_1);
            this.holder.VacationRejected = (RelativeLayout) convertView.findViewById(R.id.vacation_0);
            convertView.setTag(this.holder);
        } else {
            this.holder = (AttendanceStatHolder) convertView.getTag();
        }
        this.posValues = (AttendanceStatModel) this.attendanceList.get(position);
        if (this.posValues != null) {
            this.holder.Status_0.setTag(Integer.valueOf(position));
            this.holder.Status_1.setTag(Integer.valueOf(position));
            this.holder.Status_2.setTag(Integer.valueOf(position));
            this.holder.Status_3.setTag(Integer.valueOf(position));
            this.holder.Status_4.setTag(Integer.valueOf(position));
            this.holder.CHK_Status_txt_M2.setTag(Integer.valueOf(position));
            this.holder.CHK_Status_txt_B1.setTag(Integer.valueOf(position));
            this.holder.CHK_Status_txt_F1.setTag(Integer.valueOf(position));
            this.holder.Status_txt_0.setTag(Integer.valueOf(position));
            this.holder.Status_txt_ab.setTag(Integer.valueOf(position));
            this.holder.cet_text_0.setTag(Integer.valueOf(position));
            this.holder.speed_writing_text.setTag(Integer.valueOf(position));
            this.holder.book_text_2.setTag(Integer.valueOf(position));
            this.holder.note_text_1.setTag(Integer.valueOf(position));
            this.holder.VacationAccepted.setTag(Integer.valueOf(position));
            this.holder.VacationRejected.setTag(Integer.valueOf(position));
            this.holder.StudentName.setNotNullText(this.posValues.StudentName);
            this.holder.StudentRollId.setNotNullText(this.posValues.StudentRollId);
            this.holder.balanceLesson.setText(this.posValues.BalanceLesson);
            this.holder.Status.setNotNullText(this.posValues.Status);
            this.holder.cet_text_0.setText(this.posValues.attCertificate);
            this.holder.speed_writing_text.setText(this.posValues.attSpeed);
            this.holder.note_text_1.setText(this.posValues.attNotes);
            this.holder.book_text_2.setText(this.posValues.attBook);
            this.holder.CHK_Status_txt_M2.setChecked(this.posValues.makeup.booleanValue());
            this.holder.CHK_Status_txt_B1.setChecked(this.posValues.break2.booleanValue());
            this.holder.CHK_Status_txt_F1.setChecked(this.posValues.fee.booleanValue());
            this.holder.Status_txt_0.setChecked(this.posValues.attendance.booleanValue());
            this.holder.Status_txt_0.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    AttendanceStatPage.this.changePresentStatus(v, ((CheckBox) v).isChecked());
                }
            });
            this.holder.Status_txt_ab.setChecked(this.posValues.absent.booleanValue());
            this.holder.Status_txt_ab.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    AttendanceStatPage.this.changeAbsentStatus(v, ((CheckBox) v).isChecked());
                }
            });
            this.holder.CHK_Status_txt_F1.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    AttendanceStatPage.this.changeFeeStatus(v, ((CheckBox) v).isChecked());
                }
            });
            this.holder.CHK_Status_txt_M2.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    AttendanceStatPage.this.changeMakeupStatus(v, ((CheckBox) v).isChecked());
                }
            });
            this.holder.CHK_Status_txt_B1.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    AttendanceStatPage.this.changeBreakStatus(v, ((CheckBox) v).isChecked());
                }
            });
            new MultiTextWatcher().registerEditText(this.holder.cet_text_0).setCallback(new MultiTextWatcher.TextWatcherWithInstance() {
                public void onTextChanged(EditText editText, CharSequence s, int start, int before, int count) {
                    AttendanceStatPage.this.saveCerttexts(editText, s.toString());
                }
            });
            new MultiTextWatcher().registerEditText(this.holder.book_text_2).setCallback(new MultiTextWatcher.TextWatcherWithInstance() {
                public void onTextChanged(EditText editText, CharSequence s, int start, int before, int count) {
                    AttendanceStatPage.this.saveBooktexts(editText, s.toString());
                }
            });
            new MultiTextWatcher().registerEditText(this.holder.note_text_1).setCallback(new MultiTextWatcher.TextWatcherWithInstance() {
                public void onTextChanged(EditText editText, CharSequence s, int start, int before, int count) {
                    AttendanceStatPage.this.savenotetexts(editText, s.toString());
                }
            });
            new MultiTextWatcher().registerEditText(this.holder.speed_writing_text).setCallback(new MultiTextWatcher.TextWatcherWithInstance() {
                public void onTextChanged(EditText editText, CharSequence s, int start, int before, int count) {
                    AttendanceStatPage.this.savespeedwritingtexts(editText, s.toString());
                }
            });

            this.holder.VacationAccepted.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    AttendanceStatPage.this.changeVacationStatus(view, "1");
                }
            });
            this.holder.VacationRejected.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    AttendanceStatPage.this.changeVacationStatus(view, "0");
                }
            });
            if (this.posValues.vacation == null || !this.posValues.vacation.equals("true")) {
                this.holder.VacationSelectCon.setVisibility(8);
                this.holder.VacationStateCon.setVisibility(8);
                this.holder.AttendanceSelectCon.setVisibility(0);
               // this.holder.AttendanceStateCon.setVisibility(0);
            } else {
                this.holder.VacationSelectCon.setVisibility(0);
                this.holder.VacationStateCon.setVisibility(0);
                this.holder.AttendanceSelectCon.setVisibility(8);
               // this.holder.AttendanceStateCon.setVisibility(8);
                if (this.posValues.vacationStat != null) {
                    if (this.posValues.vacationStat.equals("-1")) {
                        this.holder.VacationStateData.setText("NA");
                    } else if (this.posValues.vacationStat.equals("1")) {
                        this.holder.VacationStateData.setText("Accepted Vacation");
                    } else if (this.posValues.vacationStat.equals("0")) {
                        this.holder.VacationStateData.setText("Rejected Vacation");
                        this.holder.AttendanceSelectCon.setVisibility(0);
                      //  this.holder.AttendanceStateCon.setVisibility(0);
                    }
                }
            }
            Concurrent.setLangWords(this, this.holder.Attendance, this.holder.rollid, this.holder.Status_txt_0,this.holder.Status_txt_ab, this.holder.Status_txt_F1, this.holder.Status_txt_M2, this.holder.Status_txt_3, this.holder.Status_txt_4);
        }
        return convertView;
    }

    public void changeStatus(View view, String Status) {
        ((AttendanceStatModel) this.attendanceList.get(((Integer) view.getTag()).intValue())).StatusId = Status;
        ((AttendanceStatModel) this.attendanceList.get(((Integer) view.getTag()).intValue())).updateStatus();
        this.mListManager.getListAdapter().notifyDataSetChanged();
    }

    public void changePresentStatus(View view, boolean Status) {
        ((AttendanceStatModel) this.attendanceList.get(((Integer) view.getTag()).intValue())).attendance = Boolean.valueOf(Status);
    }
    public void changeAbsentStatus(View view, boolean Status) {
        ((AttendanceStatModel) this.attendanceList.get(((Integer) view.getTag()).intValue())).absent = Boolean.valueOf(Status);
    }
    public void changeFeeStatus(View view, boolean Status) {
        ((AttendanceStatModel) this.attendanceList.get(((Integer) view.getTag()).intValue())).fee = Boolean.valueOf(Status);
    }

    public void changeBreakStatus(View view, boolean Status) {
        ((AttendanceStatModel) this.attendanceList.get(((Integer) view.getTag()).intValue())).break2 = Boolean.valueOf(Status);
    }


    public void changeMakeupStatus(View view, boolean Status) {
        ((AttendanceStatModel) this.attendanceList.get(((Integer) view.getTag()).intValue())).makeup = Boolean.valueOf(Status);
    }

    public void saveCerttexts(View view, String text) {
        ((AttendanceStatModel) this.attendanceList.get(((Integer) view.getTag()).intValue())).attCertificate = text;
    }

    public void savenotetexts(View view, String text) {
        ((AttendanceStatModel) this.attendanceList.get(((Integer) view.getTag()).intValue())).attNotes = text;
    }


    public void savespeedwritingtexts(View view, String text) {
        ((AttendanceStatModel) this.attendanceList.get(((Integer) view.getTag()).intValue())).attSpeed = text;
    }


    public void saveBooktexts(View view, String text) {
        ((AttendanceStatModel) this.attendanceList.get(((Integer) view.getTag()).intValue())).attBook = text;
    }

    public void changeVacationStatus(View view, String Status) {
        ((AttendanceStatModel) this.attendanceList.get(((Integer) view.getTag()).intValue())).vacationStat = Status;
        ((AttendanceStatModel) this.attendanceList.get(((Integer) view.getTag()).intValue())).Status = null;
        this.mListManager.getListAdapter().notifyDataSetChanged();
    }
}
