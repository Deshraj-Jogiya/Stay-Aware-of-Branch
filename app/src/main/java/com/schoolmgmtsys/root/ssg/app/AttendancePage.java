package com.schoolmgmtsys.root.ssg.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.media.ExifInterface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.dd.processbutton.iml.ActionProcessButton;
import com.dd.processbutton.iml.ActionProcessButton.Mode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.marvinlabs.widget.floatinglabel.instantpicker.Instant;
import com.marvinlabs.widget.floatinglabel.instantpicker.InstantPickerListener;
import com.marvinlabs.widget.floatinglabel.itempicker.FloatingLabelItemPicker;
import com.marvinlabs.widget.floatinglabel.itempicker.FloatingLabelItemPicker.OnItemPickerEventListener;
import com.marvinlabs.widget.floatinglabel.itempicker.FloatingLabelItemPicker.OnWidgetEventListener;
import com.marvinlabs.widget.floatinglabel.itempicker.ItemPickerListener;
import com.marvinlabs.widget.floatinglabel.itempicker.StringPickerDialogFragment;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.fonts.ParentStyledTextView;
import com.schoolmgmtsys.root.ssg.models.AttendanceStatModel;
import com.schoolmgmtsys.root.ssg.models.SectionsModel;
import com.schoolmgmtsys.root.ssg.models.SubjectsModel2;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.schoolmgmtsys.root.ssg.utils.DrawerListFragment;
import com.solutionsbricks.solbricksframework.Call;
import com.solutionsbricks.solbricksframework.Callback;
import com.solutionsbricks.solbricksframework.FormBody.Builder;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.Request;
import com.solutionsbricks.solbricksframework.Response;
import com.solutionsbricks.solbricksframework.helpers.GsonParser;
import com.solutionsbricks.solbricksframework.helpers.SBDatePickerDialog;
import com.solutionsbricks.solbricksframework.helpers.SBDatePickerDialog.DatePickerMultiCalsInterface;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class AttendancePage extends SlidingFragmentActivity implements ItemPickerListener<String>, OnItemPickerEventListener<String>, InstantPickerListener, DatePickerMultiCalsInterface {
    public static HashMap<String, String> statusIdentifierKeyFirst;
    /* access modifiers changed from: private */
    public FloatingLabelItemPicker<String> ControlClasses;
    /* access modifiers changed from: private */
    public FloatingLabelItemPicker<String> ControlSections;
    /* access modifiers changed from: private */
    public FloatingLabelItemPicker<String> ControlSubject;
    private SharedPreferences Prefs;
    /* access modifiers changed from: private */
    public ActionProcessButton ProcessDataBtn;
    /* access modifiers changed from: private */
    public int RetryLevel = 1;
    private String TOKEN;
    private JsonObject ValuesObject;
    String attBook;
    String attCertificate;
    String attNotes;
    String attSpeed;
    boolean attendance;
    /* access modifiers changed from: private */
    public String choosenClassID;
    /* access modifiers changed from: private */
    public String choosenDate;
    /* access modifiers changed from: private */
    public String choosenSectionID;
    /* access modifiers changed from: private */
    public String choosenSubjectID;
    /* access modifiers changed from: private */
    public boolean classSelectedLock;
    /* access modifiers changed from: private */
    public HashMap<String, String> classesItems;
    /* access modifiers changed from: private */
    public String classesLang;
    private RelativeLayout datePickerContainer;
    private ParentStyledTextView datePickerText;
    boolean fee;
    /* access modifiers changed from: private */
    public ProgressBar mProgressBar;
    boolean makeup;
    private String newSelectedItem;
    /* access modifiers changed from: private */
    public GsonParser parserManager;
    /* access modifiers changed from: private */
    public String response;
    /* access modifiers changed from: private */
    public HashMap<String, SectionsModel> sectionsItems;
    /* access modifiers changed from: private */
    public String sectionsLang;
    /* access modifiers changed from: private */
    public String selectedItem;
    private HashMap<String, String> statusIdentifier;
    /* access modifiers changed from: private */
    public HashMap<String, SubjectsModel2> subjectsItems;
    /* access modifiers changed from: private */
    public String subjectsLang;

    @SuppressLint("WrongConstant")
    public void onCreate(Bundle savedInstanceState) {
        ListFragment mFrag;
        super.onCreate(savedInstanceState);
        if (Concurrent.getLangDirection(this).equals("ar")) {
            getSlidingMenu().setMode(1);
        } else {
            getSlidingMenu().setMode(0);
        }
        setContentView((int) R.layout.page_attendance_select);
        SlidingMenu slidingMenu = getSlidingMenu();
        double intValue = (double) Concurrent.getScreenWidth(this).intValue();
        Double.isNaN(intValue);
        slidingMenu.setBehindWidth((int) Math.round((intValue * 70.0d) / 100.0d));
        setBehindContentView((int) R.layout.drawer_frame);
        ImageView backImage = (ImageView) findViewById(R.id.background_img);
        LinearLayout logBack = (LinearLayout) findViewById(R.id.full_layout);
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
        ((TextView) findViewById(R.id.head_drawer_title)).setText(Concurrent.getLangSubWords("studentAttendance", "Student Attendance"));
        ((ImageView) findViewById(R.id.head_drawer_toggle)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                AttendancePage.this.toggle();
            }
        });
        this.mProgressBar = (ProgressBar) findViewById(R.id.gen_loader);
        this.mProgressBar.setVisibility(8);
        this.parserManager = new GsonParser();
        this.statusIdentifier = new HashMap<>();
        this.statusIdentifier.put(Concurrent.getLangSubWords("all", "All"), "All");
        this.statusIdentifier.put(Concurrent.getLangSubWords("Present", "Present"), "1");
        this.statusIdentifier.put(Concurrent.getLangSubWords("Fee", "Fee"), "0");
        this.statusIdentifier.put(Concurrent.getLangSubWords("Makeup", "Makeup"), ExifInterface.GPS_MEASUREMENT_2D);
        this.statusIdentifier.put(Concurrent.getLangSubWords("LateExecuse", "Late with excuse"), ExifInterface.GPS_MEASUREMENT_3D);
        this.statusIdentifier.put(Concurrent.getLangSubWords("earlyDismissal", "Early Dismissal"), "4");
        this.statusIdentifier.put(Concurrent.getLangSubWords("acceptedVacation", "Accepted Vacation"), "9");
        statusIdentifierKeyFirst = new HashMap<>();
        statusIdentifierKeyFirst.put("All", Concurrent.getLangSubWords("all", "All"));
        statusIdentifierKeyFirst.put("1", Concurrent.getLangSubWords("Present", "Present"));
        statusIdentifierKeyFirst.put("0", Concurrent.getLangSubWords("Fee", "Fee"));
        statusIdentifierKeyFirst.put(ExifInterface.GPS_MEASUREMENT_2D, Concurrent.getLangSubWords("Makeup", "Makeup"));
        statusIdentifierKeyFirst.put(ExifInterface.GPS_MEASUREMENT_3D, Concurrent.getLangSubWords("LateExecuse", "Late with excuse"));
        statusIdentifierKeyFirst.put("4", Concurrent.getLangSubWords("earlyDismissal", "Early Dismissal"));
        statusIdentifierKeyFirst.put("9", Concurrent.getLangSubWords("acceptedVacation", "Accepted Vacation"));
        this.classesLang = Concurrent.getLangSubWords("classes", "classes");
        String langSubWords = Concurrent.getLangSubWords("Date", "Date");
        String searchLang = Concurrent.getLangSubWords("Search", "Search");
        this.subjectsLang = Concurrent.getLangSubWords("Subjects", "Subjects");
        this.sectionsLang = Concurrent.getLangSubWords("sections", "Sections");
        this.Prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.ProcessDataBtn = (ActionProcessButton) findViewById(R.id.process_data);
        this.ProcessDataBtn.setMode(Mode.ENDLESS);
        this.ProcessDataBtn.setText(searchLang);
        this.ControlSubject = (FloatingLabelItemPicker) findViewById(R.id.control_subject);
        this.ControlSections = (FloatingLabelItemPicker) findViewById(R.id.control_section);
        this.ControlSubject.setItemPickerListener(this);
        this.ControlSections.setItemPickerListener(this);
        if (Concurrent.AttendanceModelIsClass) {
            this.ControlSubject.setVisibility(8);
        } else {
            this.ControlSubject.setLabelText((CharSequence) this.subjectsLang);
        }
        if (!Concurrent.isSectionEnabled(this).booleanValue()) {
            this.ControlSections.setVisibility(8);
        } else {
            this.ControlSections.setLabelText((CharSequence) this.sectionsLang);
        }
        this.ControlClasses = (FloatingLabelItemPicker) findViewById(R.id.control_class);
        this.ControlClasses.setItemPickerListener(this);
        this.ControlClasses.setLabelText((CharSequence) this.classesLang);
        LoadClasses();
        this.ProcessDataBtn.setOnClickListener(new OnClickListener() {
            private String TOKEN;

            public void onClick(View v) {
                this.TOKEN = Concurrent.getAppToken(AttendancePage.this);
                if (this.TOKEN != null) {
                    AttendancePage.this.mProgressBar.setVisibility(0);
                    if (AttendancePage.this.choosenClassID == null || AttendancePage.this.choosenDate == null) {
                        AttendancePage.this.ProcessDataBtn.setText("Error, Please select required data ");
                        AttendancePage.this.ProcessDataBtn.setEnabled(true);
                        AttendancePage.this.ProcessDataBtn.setProgress(-1);
                    } else {
                        AttendancePage.this.ProcessDataBtn.setEnabled(false);
                        AttendancePage.this.ProcessDataBtn.setProgress(10);
                        Builder formBody = new Builder();
                        formBody.add("attendanceDay", String.valueOf(AttendancePage.this.choosenDate));
                        formBody.add("classId", String.valueOf(AttendancePage.this.choosenClassID));
                        if (Concurrent.isSectionEnabled(AttendancePage.this).booleanValue()) {
                            formBody.add("sectionId", String.valueOf(AttendancePage.this.choosenSectionID));
                        }
                        if (!Concurrent.AttendanceModelIsClass) {
                            if (AttendancePage.this.choosenSubjectID != null) {
                                formBody.add("subjectId", String.valueOf(AttendancePage.this.choosenSubjectID));
                            } else {
                                return;
                            }
                        }
                        OkHttpClient client = new OkHttpClient().newBuilder(AttendancePage.this.getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();
                        Request.Builder builder = new Request.Builder();
                        StringBuilder sb = new StringBuilder();
                        sb.append(App.getAppBaseUrl());
                        sb.append(Constants.TASK_ATTENDANCE_SELECT_POST);
                        Request.Builder requestBuilder = builder.url(sb.toString());
                        requestBuilder.post(formBody.build());
                        client.newCall(requestBuilder.build()).enqueue(new Callback() {
                            public void onFailure(Call call, final IOException e) {
                                AttendancePage.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        AttendancePage.this.mProgressBar.setVisibility(4);
                                        AttendancePage.this.ProcessDataBtn.setEnabled(true);
                                        AttendancePage.this.ProcessDataBtn.setProgress(-1);
                                        if (e instanceof ConnectException) {
                                            Toast.makeText(AttendancePage.this.getBaseContext(), Concurrent.getLangSubWords("noConnection", "No Internet Connection"), 1).show();
                                        } else if (Concurrent.isFloat(e.getMessage())) {
                                            AttendancePage.this.showError(e.getMessage());
                                        } else {
                                            AttendancePage.this.showError("5012");
                                        }
                                    }
                                });
                            }

                            public void onResponse(Call call, Object serverResponse) {
                                final Response responseObj = (Response) serverResponse;
                                try {
                                    AttendancePage.this.response = responseObj.body().string();
                                    if (AttendancePage.this.response != null) {
                                        AttendancePage.this.runOnUiThread(new Runnable() {
                                            public void run() {
                                                try {
                                                    if (responseObj.isSuccessful()) {
                                                        String str = null;
                                                        JsonObject ValuesHolder = null;
                                                        try {
                                                            ValuesHolder = new JsonParser().parse(AttendancePage.this.response).getAsJsonObject();
                                                        } catch (Exception e) {
                                                            Toast.makeText(AttendancePage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), 1).show();
                                                        }
                                                        if (ValuesHolder == null || ValuesHolder.isJsonNull() || !ValuesHolder.isJsonObject()) {
                                                            AttendancePage.this.ProcessDataBtn.setEnabled(true);
                                                            AttendancePage.this.ProcessDataBtn.setProgress(-1);
                                                            AttendancePage.this.ProcessDataBtn.setText("Empty Result");
                                                        } else {
                                                            try {
                                                                JsonArray ValuesArray = ValuesHolder.getAsJsonArray("students");
                                                                Log.e("fvfvdfvdf"," "+ValuesArray);
                                                                AttendancePage.this.classSelectedLock = false;
                                                                ArrayList<AttendanceStatModel> attendanceList = new ArrayList<>();
                                                                if (ValuesArray != null) {
                                                                    Iterator it = ValuesArray.iterator();
                                                                    while (it.hasNext()) {
                                                                        JsonObject objResult = ((JsonElement) it.next()).getAsJsonObject();
                                                                        StringBuilder sb = new StringBuilder();
                                                                        sb.append(" ");
                                                                        sb.append(objResult);
                                                                        if (objResult.has("attBook")) {
                                                                            AttendancePage.this.attBook = Concurrent.repairJsonValueQuotes(objResult.get("attBook").toString());
                                                                        }
                                                                        if (objResult.has("attCertificate")) {
                                                                            AttendancePage.this.attCertificate = Concurrent.repairJsonValueQuotes(objResult.get("attCertificate").toString());
                                                                        }
                                                                        if (objResult.has("attNotes")) {
                                                                            AttendancePage.this.attNotes = Concurrent.repairJsonValueQuotes(objResult.get("attNotes").toString());
                                                                        }
                                                                        if (objResult.has("attSpeed")) {
                                                                            AttendancePage.this.attSpeed = Concurrent.repairJsonValueQuotes(objResult.get("attSpeed").toString());
                                                                        }

                                                                        AttendanceStatModel attendanceStatModel =
                                                                                new AttendanceStatModel(Concurrent.repairJsonValueQuotes(objResult.get("name").toString()),
                                                                                        Concurrent.repairJsonValueQuotes(objResult.get("attendance").toString()),
                                                                                        Concurrent.repairJsonValueQuotes(objResult.get("studentRollId").toString()),
                                                                                        Concurrent.repairJsonValueQuotes(objResult.get("balanceLesson").toString()),
                                                                                        Concurrent.repairJsonValueQuotes(objResult.get("id").toString()),
                                                                                        objResult.has("vacation") ? objResult.get("vacation").toString() : str,
                                                                                        objResult.has("vacationStat") ? objResult.get("vacationStat").toString() : str,
                                                                                        Boolean.valueOf(objResult.has("attendance") ? objResult.get("attendance").getAsBoolean() : false),
                                                                                        Boolean.valueOf(objResult.has("absent") ? objResult.get("absent").getAsBoolean() : false),
                                                                                        Boolean.valueOf(objResult.has("fee") ? objResult.get("fee").getAsBoolean() : false),
                                                                                        Boolean.valueOf(objResult.has("makeup") ? objResult.get("makeup").getAsBoolean() : false),
                                                                                        Boolean.valueOf(objResult.has("break") ? objResult.get("break").getAsBoolean() : false),
                                                                                        AttendancePage.this.attBook, AttendancePage.this.attCertificate, AttendancePage.this.attNotes
                                                                                        ,AttendancePage.this.attSpeed
                                                                                        );
                                                                        attendanceList.add(attendanceStatModel);
                                                                        str = null;
                                                                    }
                                                                    AttendancePage.this.ProcessDataBtn.setEnabled(true);
                                                                    AttendancePage.this.ProcessDataBtn.setProgress(100);
                                                                    Intent i = new Intent(AttendancePage.this, AttendanceStatPage.class);
                                                                    i.putExtra("attendanceList", attendanceList);
                                                                    i.putExtra("attendanceDay", String.valueOf(AttendancePage.this.choosenDate));
                                                                    i.putExtra("classId", String.valueOf(AttendancePage.this.choosenClassID));
                                                                    if (!Concurrent.AttendanceModelIsClass) {
                                                                        i.putExtra("subjectId", String.valueOf(AttendancePage.this.choosenSubjectID));
                                                                    }
                                                                    AttendancePage.this.startActivity(i);
                                                                }
                                                            } catch (ClassCastException e2) {
                                                                AttendancePage.this.ProcessDataBtn.setEnabled(true);
                                                                AttendancePage.this.ProcessDataBtn.setText("Empty Result");
                                                                AttendancePage.this.ProcessDataBtn.setProgress(-1);
                                                                return;
                                                            } catch (Exception e3) {
                                                                AttendancePage.this.ProcessDataBtn.setEnabled(true);
                                                                AttendancePage.this.ProcessDataBtn.setText(Concurrent.getLangSubWords("errorOccurred", "Error Occurred"));
                                                                AttendancePage.this.ProcessDataBtn.setProgress(-1);
                                                                return;
                                                            }
                                                        }
                                                        AttendancePage.this.mProgressBar.setVisibility(4);
                                                    } else {
                                                        AttendancePage.this.showError("5010");
                                                    }
                                                } catch (Exception e4) {
                                                    StringBuilder sb2 = new StringBuilder();
                                                    sb2.append(" ");
                                                    sb2.append(e4.getMessage());
                                                    Log.e("my_exception", sb2.toString());
                                                    AttendancePage.this.showError("5002");
                                                }
                                            }
                                        });
                                    } else {
                                        AttendancePage.this.showError("5001");
                                        AttendancePage.this.mProgressBar.setVisibility(4);
                                        AttendancePage.this.ProcessDataBtn.setEnabled(true);
                                        AttendancePage.this.ProcessDataBtn.setProgress(-1);
                                    }
                                } catch (Exception e) {
                                    AttendancePage.this.showError("5001");
                                }
                            }
                        });
                    }
                    AttendancePage.this.mProgressBar.setVisibility(4);
                }
            }
        });
        this.datePickerContainer = (RelativeLayout) findViewById(R.id.date_input_con);
        this.datePickerText = (ParentStyledTextView) findViewById(R.id.date_input);
        this.datePickerContainer.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                FragmentTransaction ft = AttendancePage.this.getSupportFragmentManager().beginTransaction();
                Fragment prev = AttendancePage.this.getSupportFragmentManager().findFragmentByTag("attendance");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                new SBDatePickerDialog().newInstance(AttendancePage.this.getBaseContext(), "attendance", Concurrent.DateFormat).show(ft, "attendance");
            }
        });
    }

    public void showError(String errorCode) {
        String errorTitle;
        if (errorCode != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(Concurrent.getLangSubWords("errorOccurred", "Error Occurred"));
            sb.append(" ( Error Code: ");
            sb.append(errorCode);
            sb.append(" )");
            errorTitle = sb.toString();
        } else {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred");
        }
        this.mProgressBar.setVisibility(4);
        this.ProcessDataBtn.setEnabled(true);
        this.ProcessDataBtn.setProgress(-1);
        Toast.makeText(getBaseContext(), errorTitle, 1).show();
    }

    public void LoadClasses() {
        if (this.RetryLevel <= 3) {
            this.TOKEN = Concurrent.getAppToken(this);
            if (this.TOKEN != null) {
                this.mProgressBar.setVisibility(0);
                StringBuilder sb = new StringBuilder();
                sb.append(App.getAppBaseUrl());
                sb.append(Constants.TASK_ATTENDANCE_CLASSES_FOR_TEACHER);
                String mLink = sb.toString();
                OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();
                Request.Builder requestBuilder = new Request.Builder().url(mLink);
                requestBuilder.get();
                client.newCall(requestBuilder.build()).enqueue(new Callback() {
                    public void onFailure(Call call, final IOException e) {
                        AttendancePage.this.runOnUiThread(new Runnable() {
                            public void run() {
                                AttendancePage.this.mProgressBar.setVisibility(4);
                                if (e instanceof ConnectException) {
                                    Toast.makeText(AttendancePage.this.getBaseContext(), Concurrent.getLangSubWords("noConnection", "No Internet Connection"), 1).show();
                                    return;
                                }
                                AttendancePage.this.RetryLevel = AttendancePage.this.RetryLevel + 1;
                                AttendancePage attendancePage = AttendancePage.this;
                                StringBuilder sb = new StringBuilder();
                                sb.append("Please wait we trying to reload classes, Attempt : ");
                                sb.append(AttendancePage.this.RetryLevel);
                                Toast.makeText(attendancePage, sb.toString(), 1).show();
                                AttendancePage.this.LoadClasses();
                            }
                        });
                    }

                    public void onResponse(Call call, Object serverResponse) {
                        final Response responseObj = (Response) serverResponse;
                        try {
                            AttendancePage.this.response = responseObj.body().string();
                            if (AttendancePage.this.response != null) {
                                AttendancePage.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        try {
                                            if (responseObj.isSuccessful()) {
                                                JsonObject ValuesHolder = null;
                                                try {
                                                    ValuesHolder = new JsonParser().parse(AttendancePage.this.response).getAsJsonObject();
                                                } catch (Exception e) {
                                                    Toast.makeText(AttendancePage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), 1).show();
                                                }
                                                if (ValuesHolder != null) {
                                                    AttendancePage.this.RetryLevel = 1;
                                                    AttendancePage.this.classesItems = new HashMap();
                                                    Iterator it = ValuesHolder.get("classes").getAsJsonArray().iterator();
                                                    while (it.hasNext()) {
                                                        JsonObject CurrObj = ((JsonElement) it.next()).getAsJsonObject();
                                                        AttendancePage.this.classesItems.put(Concurrent.tagsStringValidator(CurrObj, "className"), Concurrent.tagsStringValidator(CurrObj, "id"));
                                                    }
                                                    if (AttendancePage.this.classesItems.size() > 0) {
                                                        TreeMap<String, String> sorted = new TreeMap<>();
                                                        LinkedHashMap<String, String> sortedClassesItems = new LinkedHashMap<>();
                                                        sorted.putAll(AttendancePage.this.classesItems);
                                                        sortedClassesItems.putAll(sorted);
                                                        AttendancePage.this.ControlClasses.setAvailableItems(AttendancePage.this.parserManager.getListOfMap(sortedClassesItems, Boolean.valueOf(true)));
                                                        AttendancePage.this.ControlClasses.setWidgetListener(new OnWidgetEventListener<String>() {
                                                            public void onShowItemPickerDialog(FloatingLabelItemPicker<String> source) {
                                                                StringPickerDialogFragment.newInstance(source.getId(), AttendancePage.this.classesLang, " ", Concurrent.getLangSubWords("cancel", "Cancel"), false, source.getSelectedIndices(), new ArrayList(source.getAvailableItems())).show(AttendancePage.this.getSupportFragmentManager(), "Class");
                                                            }
                                                        });
                                                    }
                                                    AttendancePage.this.mProgressBar.setVisibility(4);
                                                }
                                            }
                                        } catch (Exception e2) {
                                            AttendancePage.this.RetryLevel = AttendancePage.this.RetryLevel + 1;
                                            AttendancePage attendancePage = AttendancePage.this;
                                            StringBuilder sb = new StringBuilder();
                                            sb.append("Please wait we trying to reload classes, Attempt : ");
                                            sb.append(AttendancePage.this.RetryLevel);
                                            Toast.makeText(attendancePage, sb.toString(), 1).show();
                                            AttendancePage.this.LoadClasses();
                                        }
                                    }
                                });
                            } else {
                                AttendancePage.this.RetryLevel = AttendancePage.this.RetryLevel + 1;
                                AttendancePage attendancePage = AttendancePage.this;
                                StringBuilder sb = new StringBuilder();
                                sb.append("Please wait we trying to reload classes, Attempt : ");
                                sb.append(AttendancePage.this.RetryLevel);
                                Toast.makeText(attendancePage, sb.toString(), 1).show();
                                AttendancePage.this.LoadClasses();
                            }
                        } catch (Exception e) {
                            AttendancePage.this.RetryLevel = AttendancePage.this.RetryLevel + 1;
                            AttendancePage attendancePage2 = AttendancePage.this;
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append("Please wait we trying to reload classes, Attempt : ");
                            sb2.append(AttendancePage.this.RetryLevel);
                            Toast.makeText(attendancePage2, sb2.toString(), 1).show();
                            AttendancePage.this.LoadClasses();
                        }
                    }
                });
            }
        }
    }

    public void onSelectionChanged(FloatingLabelItemPicker<String> source, Collection<String> selectedItems) {
        this.newSelectedItem = (String) new ArrayList(selectedItems).get(0);
        if (source == this.ControlClasses) {
            this.choosenClassID = String.valueOf(this.classesItems.get(this.newSelectedItem));
            if (Concurrent.AttendanceModelIsClass && !Concurrent.isSectionEnabled(this).booleanValue()) {
                return;
            }
            if (!this.classSelectedLock || !this.newSelectedItem.equals(this.selectedItem)) {
                this.classSelectedLock = true;
                this.selectedItem = (String) new ArrayList(selectedItems).get(0);
                if (this.TOKEN != null) {
                    this.mProgressBar.setVisibility(0);
                    Builder formBody = new Builder();
                    formBody.add("classes", String.valueOf(this.classesItems.get(this.selectedItem)));
                    OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();
                    Request.Builder builder = new Request.Builder();
                    StringBuilder sb = new StringBuilder();
                    sb.append(App.getAppBaseUrl());
                    sb.append(Constants.TASK_GET_SECTIONS_SUBJECTS);
                    Request.Builder requestBuilder = builder.url(sb.toString());
                    requestBuilder.post(formBody.build());
                    client.newCall(requestBuilder.build()).enqueue(new Callback() {
                        public void onFailure(Call call, final IOException e) {
                            AttendancePage.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    AttendancePage.this.mProgressBar.setVisibility(4);
                                    if (e instanceof ConnectException) {
                                        Toast.makeText(AttendancePage.this.getBaseContext(), Concurrent.getLangSubWords("noConnection", "No Internet Connection"), 1).show();
                                    } else {
                                        Toast.makeText(AttendancePage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), 1).show();
                                    }
                                }
                            });
                        }

                        public void onResponse(Call call, Object serverResponse) {
                            final Response responseObj = (Response) serverResponse;
                            try {
                                AttendancePage.this.response = responseObj.body().string();
                                if (AttendancePage.this.response != null) {
                                    AttendancePage.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            try {
                                                if (responseObj.isSuccessful()) {
                                                    JsonObject ValuesHolder = null;
                                                    try {
                                                        ValuesHolder = new JsonParser().parse(AttendancePage.this.response).getAsJsonObject();
                                                    } catch (Exception e) {
                                                        Toast.makeText(AttendancePage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), 1).show();
                                                    }
                                                    AttendancePage.this.classSelectedLock = false;
                                                    if (ValuesHolder != null) {
                                                        Iterator<JsonElement> ValsIter = ValuesHolder.getAsJsonArray("subjects").iterator();
                                                        AttendancePage.this.subjectsItems = new HashMap();
                                                        ArrayList<String> pickerItems = new ArrayList<>();
                                                        while (ValsIter.hasNext()) {
                                                            JsonObject CurrObj = ((JsonElement) ValsIter.next()).getAsJsonObject();
                                                            String subjectTitle = Concurrent.tagsStringValidator(CurrObj, "subjectTitle");
                                                            AttendancePage.this.subjectsItems.put(subjectTitle, new SubjectsModel2(Integer.valueOf(Concurrent.tagsIntValidator(CurrObj, "id")), subjectTitle, String.valueOf(AttendancePage.this.classesItems.get(AttendancePage.this.selectedItem)), Concurrent.tagsStringValidator(CurrObj, "teacherId")));
                                                            pickerItems.add(subjectTitle);
                                                        }
                                                        AttendancePage.this.ControlSubject.setAvailableItems(pickerItems);
                                                        AttendancePage.this.ControlSubject.setWidgetListener(new OnWidgetEventListener<String>() {
                                                            public void onShowItemPickerDialog(FloatingLabelItemPicker source) {
                                                                StringPickerDialogFragment.newInstance(source.getId(), AttendancePage.this.subjectsLang," ", Concurrent.getLangSubWords("cancel", "Cancel"), false, source.getSelectedIndices(), new ArrayList(source.getAvailableItems())).show(AttendancePage.this.getSupportFragmentManager(), "Subject");
                                                            }
                                                        });
                                                        Iterator<JsonElement> sectionsValsIter = ValuesHolder.getAsJsonArray("sections").iterator();
                                                        AttendancePage.this.sectionsItems = new HashMap();
                                                        ArrayList arrayList = new ArrayList();
                                                        while (sectionsValsIter.hasNext()) {
                                                            JsonObject CurrObj2 = ((JsonElement) sectionsValsIter.next()).getAsJsonObject();
                                                            StringBuilder sb = new StringBuilder();
                                                            sb.append(Concurrent.tagsStringValidator(CurrObj2, "sectionName"));
                                                            sb.append(" - ");
                                                            sb.append(Concurrent.tagsStringValidator(CurrObj2, "sectionTitle"));
                                                            String Title = sb.toString();
                                                            AttendancePage.this.sectionsItems.put(Title, new SectionsModel(Integer.valueOf(Concurrent.tagsIntValidator(CurrObj2, "id")), Concurrent.tagsStringValidator(CurrObj2, "sectionName"), Concurrent.tagsStringValidator(CurrObj2, "sectionTitle"), Integer.valueOf(Concurrent.tagsIntValidator(CurrObj2, "classId"))));
                                                            arrayList.add(Title);
                                                        }
                                                        Collections.sort(arrayList);
                                                        if (Concurrent.isSectionEnabled(AttendancePage.this).booleanValue()) {
                                                            AttendancePage.this.ControlSections.setAvailableItems(arrayList);
                                                            AttendancePage.this.ControlSections.setWidgetListener(new OnWidgetEventListener<String>() {
                                                                public void onShowItemPickerDialog(FloatingLabelItemPicker source) {
                                                                    StringPickerDialogFragment.newInstance(source.getId(), AttendancePage.this.sectionsLang, " ", Concurrent.getLangSubWords("cancel", "Cancel"), false, source.getSelectedIndices(), new ArrayList(source.getAvailableItems())).show(AttendancePage.this.getSupportFragmentManager(), "Sections");
                                                                }
                                                            });
                                                        }
                                                    }
                                                } else {
                                                    Toast.makeText(AttendancePage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), 1).show();
                                                }
                                            } catch (Exception e2) {
                                                Toast.makeText(AttendancePage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), 1).show();
                                            }
                                            AttendancePage.this.mProgressBar.setVisibility(4);
                                        }
                                    });
                                } else {
                                    Toast.makeText(AttendancePage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), 1).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(AttendancePage.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), 1).show();
                            }
                        }
                    });
                }
            }
        } else if (source == this.ControlSubject) {
            this.choosenSubjectID = String.valueOf(((SubjectsModel2) this.subjectsItems.get(this.newSelectedItem)).id);
        } else if (source == this.ControlSections) {
            this.choosenSectionID = String.valueOf(((SectionsModel) this.sectionsItems.get(this.newSelectedItem)).id);
        }
    }

    public void onCancelled(int pickerId) {
    }

    public void onItemsSelected(int pickerId, int[] selectedIndices) {
        if (pickerId == R.id.control_class) {
            this.ControlClasses.setSelectedIndices(selectedIndices);
        } else if (pickerId == R.id.control_subject) {
            this.ControlSubject.setSelectedIndices(selectedIndices);
        } else if (pickerId == R.id.control_section) {
            this.ControlSections.setSelectedIndices(selectedIndices);
        }
    }

    public void onInstantSelected(int pickerId, Instant instant) {
    }

    public void onDatePicked(Intent data) {
        String SelectedDateAsString = data.getExtras().getString("date");
        this.datePickerText.setText(SelectedDateAsString);
        this.choosenDate = SelectedDateAsString;
    }
}
