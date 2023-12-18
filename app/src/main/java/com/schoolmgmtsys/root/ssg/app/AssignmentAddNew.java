package com.schoolmgmtsys.root.ssg.app;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.marvinlabs.widget.floatinglabel.edittext.FloatingLabelEditText;
import com.marvinlabs.widget.floatinglabel.instantpicker.Instant;
import com.marvinlabs.widget.floatinglabel.instantpicker.InstantPickerListener;
import com.marvinlabs.widget.floatinglabel.itempicker.FloatingLabelItemPicker;
import com.marvinlabs.widget.floatinglabel.itempicker.ItemPickerListener;
import com.marvinlabs.widget.floatinglabel.itempicker.StringPickerDialogFragment;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.fonts.ParentStyledTextView;
import com.schoolmgmtsys.root.ssg.fonts.RegularStyledTextView;
import com.schoolmgmtsys.root.ssg.models.SectionsModel;
import com.schoolmgmtsys.root.ssg.models.SubjectsModel2;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.schoolmgmtsys.root.ssg.utils.DrawerListFragment;
import com.solutionsbricks.solbricksframework.helpers.GsonParser;
import com.solutionsbricks.solbricksframework.helpers.SBDatePickerDialog;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.OkHttpClient;

import static android.view.View.INVISIBLE;

public class AssignmentAddNew extends SlidingFragmentActivity implements ItemPickerListener<String>,
        FloatingLabelItemPicker.OnItemPickerEventListener<String>,
        InstantPickerListener, SBDatePickerDialog.DatePickerMultiCalsInterface {

    private FloatingLabelItemPicker<String> ControlSubject;
    private ActionProcessButton ProcessDataBtn;
    private SharedPreferences Prefs;
    private JsonObject ValuesObject;
    private FloatingLabelItemPicker<String> ControlClasses;
    private String TOKEN;
    private GsonParser parserManager;
    private HashMap<String, String> classesItems;
    private boolean classSelectedLock;
    private HashMap<String, SubjectsModel2> subjectsItems;
    private ArrayList<String> selectedClassesNames;
    private ArrayList<String> selectedClassesIDs = new ArrayList<>();
    private HashMap<String, String> statusIdentifier;
    private File choosenFile;
    private ArrayList<String> newSelectedItem;
    private String choosenClassID;
    private String choosenSubjectID;
    private String choosenDate;
    private FloatingLabelEditText mAssignmentTitle;
    private FloatingLabelEditText mAssignmentDescription;
    private ProgressBar mProgressBar;
    private String subjectsLang;
    private String addFileLang;
    private HashMap<String, SectionsModel> sectionsItems;
    private String sectionsLang;
    private FloatingLabelItemPicker<String> ControlSections;
    private RegularStyledTextView mDateLabelTitle;
    private ActionProcessButton mAddFileButton;
    private ArrayList<String> choosenSectionID = new ArrayList<>();
    private String classesLang;
    private String assignmentTitleLang;
    private String assignmentDescLang;
    private int RetryLevel = 1;
    private RelativeLayout datePickerContainer;
    private ParentStyledTextView datePickerText;
    private String response;
    private static final String TAG = AssignmentAddNew.class.getSimpleName();
    private DialogProperties properties;
    private String choosenFileType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Concurrent.getLangDirection(this).equals("ar"))
            getSlidingMenu().setMode(SlidingMenu.RIGHT);
        else getSlidingMenu().setMode(SlidingMenu.LEFT);

        setContentView(R.layout.page_assign_add_new);

        getSlidingMenu().setBehindWidth((int) Math.round(Concurrent.getScreenWidth(this) * 70.0 / 100.0));
        setBehindContentView(R.layout.drawer_frame);

        ImageView backImage = (ImageView) findViewById(R.id.background_img);
        LinearLayout logBack = (LinearLayout) findViewById(R.id.full_layout);

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

        TextView HeadTitle = (TextView) findViewById(R.id.head_drawer_title);
        HeadTitle.setText(Concurrent.getLangSubWords("AddAssignments", "Add Assignment"));

        ImageView ToogleBtn = (ImageView) findViewById(R.id.head_drawer_toggle);
        ToogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.gen_loader);
        mProgressBar.setVisibility(View.GONE);

        parserManager = new GsonParser();

        classesLang = Concurrent.getLangSubWords("classes", "classes");
        final String dateLang = Concurrent.getLangSubWords("AssignmentDeadline", "Assignment Deadline");
        final String searchLang = Concurrent.getLangSubWords("AddAssignments", "Add Assignment");
        subjectsLang = Concurrent.getLangSubWords("Subjects", "Subjects");
        sectionsLang = Concurrent.getLangSubWords("sections", "Sections");
        addFileLang = Concurrent.getLangSubWords("AssignmentFile", "Assignment File");
        assignmentTitleLang = Concurrent.getLangSubWords("AssignmentTitle", "Assignment Title");
        assignmentDescLang = Concurrent.getLangSubWords("AssignmentDescription", "Assignment Description");

        Prefs = PreferenceManager.getDefaultSharedPreferences(AssignmentAddNew.this);

        mAssignmentTitle = (FloatingLabelEditText) findViewById(R.id.assignment_title);
        mAssignmentDescription = (FloatingLabelEditText) findViewById(R.id.assignment_description);

        mAssignmentTitle.setLabelText(assignmentTitleLang);
        mAssignmentDescription.setLabelText(assignmentDescLang);

        mAddFileButton = (ActionProcessButton) findViewById(R.id.add_file);
        mAddFileButton.setMode(ActionProcessButton.Mode.ENDLESS);
        mAddFileButton.setText(addFileLang);

        ProcessDataBtn = (ActionProcessButton) findViewById(R.id.process_data);
        ProcessDataBtn.setMode(ActionProcessButton.Mode.ENDLESS);
        ProcessDataBtn.setText(searchLang);

        ControlSubject = (FloatingLabelItemPicker<String>) findViewById(R.id.control_subject);
        ControlSections = (FloatingLabelItemPicker<String>) findViewById(R.id.control_section);
        mDateLabelTitle = (RegularStyledTextView) findViewById(R.id.date_input_title);

        mDateLabelTitle.setText(dateLang);

        ControlSubject.setItemPickerListener(this);
        ControlSections.setItemPickerListener(this);

        ControlSubject.setLabelText(subjectsLang);

        if (!Concurrent.isSectionEnabled(AssignmentAddNew.this)) {
            ControlSections.setVisibility(View.GONE);
        } else {
            ControlSections.setLabelText(sectionsLang);
        }

        ControlClasses = (FloatingLabelItemPicker<String>) findViewById(R.id.control_class);
        ControlClasses.setItemPickerListener(AssignmentAddNew.this);

        ControlClasses.setLabelText(classesLang);


        LoadClasses();

        properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;

        mAddFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilePickerDialog fileDialog = new FilePickerDialog(AssignmentAddNew.this, properties);
                fileDialog.setTitle("Select a File");
                fileDialog.setDialogSelectionListener(new DialogSelectionListener() {
                    @Override
                    public void onSelectedFilePaths(String[] files) {
                        choosenFile = new File(files[0]);
                        String extension = MimeTypeMap.getFileExtensionFromUrl(files[0]);
                        if (extension != null) {
                            choosenFileType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                        }
                        ((TextView) findViewById(R.id.add_file_name)).setText(choosenFile.getName());

                    }
                });
                fileDialog.show();
            }
        });

        ProcessDataBtn.setOnClickListener(new View.OnClickListener() {
            private String TOKEN;

            @Override
            public void onClick(View v) {
                TOKEN = Concurrent.getAppToken(AssignmentAddNew.this);
                if (TOKEN != null) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    if (choosenClassID != null && choosenDate != null) {
                        ProcessDataBtn.setEnabled(false);
                        ProcessDataBtn.setProgress(10);


                        MultipartBody.Builder reqBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("AssignTitle", String.valueOf(mAssignmentTitle.getInputWidgetText().toString()))
                                .addFormDataPart("AssignDescription", String.valueOf(mAssignmentDescription.getInputWidgetText().toString()))
                                .addFormDataPart("AssignDeadLine", String.valueOf(choosenDate));

                        if (choosenFile != null) {
                            try {
                                reqBody.addFormDataPart("AssignFile", choosenFile.getName(), RequestBody.create(MediaType.parse(choosenFileType), choosenFile));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        for (int i = 0; i < selectedClassesIDs.size(); i++) {
                            reqBody.addFormDataPart("classId[" + i + "]", selectedClassesIDs.get(i));
                        }
                        if (Concurrent.isSectionEnabled(AssignmentAddNew.this)) {
                            for (int i = 0; i < choosenSectionID.size(); i++) {
                                reqBody.addFormDataPart("sectionId[" + i + "]", choosenSectionID.get(i));
                            }
                        }

                        if (choosenSubjectID != null)
                            reqBody.addFormDataPart("subjectId", String.valueOf(choosenSubjectID));
                        else return;


                        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(7, TimeUnit.SECONDS).build();

                        Request.Builder requestBuilder = new Request.Builder()
                                .url(com.solutionsbricks.solbricksframework.OkHttpClient.strip(App.getAppBaseUrl() + Constants.TASK_ASSIGN_CONTROL));

                        requestBuilder.post(reqBody.build());

                        Request request = requestBuilder.build();


                        Call call = client.newCall(request);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, final IOException e) {

                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        mProgressBar.setVisibility(INVISIBLE);
                                        ProcessDataBtn.setEnabled(true);
                                        ProcessDataBtn.setProgress(-1);
                                        if (e instanceof ConnectException) {
                                            Toast.makeText(getBaseContext(), Concurrent.getLangSubWords("noConnection", "No Internet Connection"), Toast.LENGTH_LONG).show();
                                        } else {
                                            if (Concurrent.isFloat(e.getMessage())) {
                                                showError(e.getMessage());
                                            } else {
                                                showError("5012");
                                            }
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onResponse(Call call, Response serverResponse) throws IOException {
                                Log.v(TAG, "ssa " + serverResponse);

                                final Response responseObj = (Response) serverResponse;
                                try {
                                    response = responseObj.body().string();

                                } catch (Exception e) {
                                    showError("5001");
                                    mProgressBar.setVisibility(INVISIBLE);
                                    ProcessDataBtn.setEnabled(true);
                                    ProcessDataBtn.setProgress(-1);
                                    return;
                                }

                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        try {
                                            if (responseObj.isSuccessful()) {

                                                JsonParser parser = new JsonParser();
                                                JsonObject ValuesHolder = null;
                                                JsonArray ValuesArray;

                                                try {
                                                    ValuesHolder = parser.parse(response).getAsJsonObject();
                                                } catch (Exception e) {
                                                    Toast.makeText(AssignmentAddNew.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                                                    ProcessDataBtn.setEnabled(true);
                                                    ProcessDataBtn.setText(Concurrent.getLangSubWords("errorOccurred", "Error Occurred"));
                                                    ProcessDataBtn.setProgress(-1);
                                                }

                                                if (ValuesHolder != null) {
                                                    if (Concurrent.tagsStringValidator(ValuesHolder, "status").equals("success")) {
                                                        ProcessDataBtn.setEnabled(true);
                                                        ProcessDataBtn.setProgress(100);
                                                        Toast.makeText(AssignmentAddNew.this, Concurrent.getLangSubWords("success", "Success"), Toast.LENGTH_LONG).show();
                                                        AssignmentAddNew.super.onBackPressed();
                                                    } else {
                                                        Toast.makeText(AssignmentAddNew.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                                                        ProcessDataBtn.setEnabled(true);
                                                        ProcessDataBtn.setText(Concurrent.getLangSubWords("errorOccurred", "Error Occurred"));
                                                        ProcessDataBtn.setProgress(-1);
                                                    }
                                                } else {
                                                    Toast.makeText(AssignmentAddNew.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                                                    ProcessDataBtn.setEnabled(true);
                                                    ProcessDataBtn.setText(Concurrent.getLangSubWords("errorOccurred", "Error Occurred"));
                                                    ProcessDataBtn.setProgress(-1);
                                                }

                                                mProgressBar.setVisibility(INVISIBLE);
                                            } else {
                                                showError("5010");
                                                ProcessDataBtn.setEnabled(true);
                                                ProcessDataBtn.setText(Concurrent.getLangSubWords("errorOccurred", "Error Occurred"));
                                                ProcessDataBtn.setProgress(-1);
                                            }
                                        } catch (final Exception e) {
                                            showError("5002");
                                            ProcessDataBtn.setEnabled(true);
                                            ProcessDataBtn.setText(Concurrent.getLangSubWords("errorOccurred", "Error Occurred"));
                                            ProcessDataBtn.setProgress(-1);
                                        }
                                    }
                                });

                            }

                        });
                    } else {
                        ProcessDataBtn.setText("Error, Please select required data ");
                        ProcessDataBtn.setEnabled(true);
                        ProcessDataBtn.setProgress(-1);
                    }
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        });

        datePickerContainer = (RelativeLayout) findViewById(R.id.date_input_con);
        datePickerText = (ParentStyledTextView) findViewById(R.id.date_input);

        datePickerContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //============================= Open View When Click  ====================//
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Fragment prev = getSupportFragmentManager().findFragmentByTag("assignments");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                DialogFragment newFragment = new SBDatePickerDialog().newInstance(getBaseContext(), "assignments", Concurrent.DateFormat);
                newFragment.show(ft, "assignments");

            }
        });


    }

    public void showError(String errorCode) {
        String errorTitle;
        if (errorCode != null) {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: " + errorCode + " )";
        } else {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred");
        }
        mProgressBar.setVisibility(INVISIBLE);
        ProcessDataBtn.setEnabled(true);
        ProcessDataBtn.setProgress(-1);
        Toast.makeText(getBaseContext(), errorTitle, Toast.LENGTH_LONG).show();
    }

    public void LoadClasses() {
        if (RetryLevel <= 3) {
            TOKEN = Concurrent.getAppToken(AssignmentAddNew.this);
            if (TOKEN != null) {
                mProgressBar.setVisibility(View.VISIBLE);

                String mLink = com.solutionsbricks.solbricksframework.OkHttpClient.strip(App.getAppBaseUrl() + Constants.TASK_ASSIGNMENT_LIST);


                OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(7, TimeUnit.SECONDS).build();

                Request.Builder requestBuilder = new Request.Builder()
                        .url(mLink);

                requestBuilder.get();

                Request request = requestBuilder.build();


                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                mProgressBar.setVisibility(INVISIBLE);
                                if (e instanceof ConnectException) {
                                    Toast.makeText(getBaseContext(), Concurrent.getLangSubWords("noConnection", "No Internet Connection"), Toast.LENGTH_LONG).show();
                                } else {
                                    RetryLevel++;
                                    Toast.makeText(AssignmentAddNew.this, "Please wait we trying to reload classes, Attempt : " + RetryLevel, Toast.LENGTH_LONG).show();
                                    LoadClasses();
                                }
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response serverResponse) {
                        final Response responseObj = (Response) serverResponse;
                        try {
                            response = responseObj.body().string();
                        } catch (Exception e) {
                            RetryLevel++;
                            Toast.makeText(AssignmentAddNew.this, "Please wait we trying to reload classes, Attempt : " + RetryLevel, Toast.LENGTH_LONG).show();
                            LoadClasses();
                            return;
                        }

                        if (response != null) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    try {
                                        if (responseObj.isSuccessful()) {

                                            JsonParser parser = new JsonParser();
                                            JsonObject ValuesHolder = null;


                                            try {
                                                ValuesHolder = parser.parse(response).getAsJsonObject();
                                            } catch (Exception e) {
                                                Toast.makeText(AssignmentAddNew.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                                            }

                                            if (ValuesHolder != null) {
                                                RetryLevel = 1;
                                                classesItems = new HashMap<>();
                                                for (JsonElement jsonElement : ValuesHolder.get("classes").getAsJsonArray()) {
                                                    JsonObject CurrObj = jsonElement.getAsJsonObject();
                                                    classesItems.put(Concurrent.tagsStringValidator(CurrObj, "className"), Concurrent.tagsStringValidator(CurrObj, "id"));
                                                }


                                                if (classesItems.size() > 0) {
                                                    ControlClasses.setAvailableItems(parserManager.getListOfMap(classesItems, true));
                                                    ControlClasses.setWidgetListener(new FloatingLabelItemPicker.OnWidgetEventListener<String>() {
                                                        @Override
                                                        public void onShowItemPickerDialog(FloatingLabelItemPicker<String> source) {
                                                            StringPickerDialogFragment itemPicker3 = StringPickerDialogFragment.newInstance(
                                                                    source.getId(),
                                                                    classesLang,
                                                                    Concurrent.getLangSubWords("ok", "OK"), Concurrent.getLangSubWords("cancel", "Cancel"),
                                                                    true,
                                                                    source.getSelectedIndices(),
                                                                    new ArrayList<>(source.getAvailableItems()));
                                                            itemPicker3.show(getSupportFragmentManager(), "Class");
                                                        }
                                                    });
                                                }
                                                mProgressBar.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    } catch (final Exception e) {
                                        RetryLevel++;
                                        Toast.makeText(AssignmentAddNew.this, "Please wait we trying to reload classes, Attempt : " + RetryLevel, Toast.LENGTH_LONG).show();
                                        LoadClasses();
                                    }
                                }
                            });
                        } else {
                            RetryLevel++;
                            Toast.makeText(AssignmentAddNew.this, "Please wait we trying to reload classes, Attempt : " + RetryLevel, Toast.LENGTH_LONG).show();
                            LoadClasses();
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onSelectionChanged(FloatingLabelItemPicker<String> source, Collection<String> selectedItems) {
        newSelectedItem = ((new ArrayList(selectedItems)));
        if (source == ControlClasses) {

            for (int i = 0; i < newSelectedItem.size(); i++) {
                choosenClassID = String.valueOf(classesItems.get(newSelectedItem.get(i)));
            }


            if (!classSelectedLock) {
                classSelectedLock = true;

                selectedClassesNames = newSelectedItem;
                if (TOKEN != null) {
                    mProgressBar.setVisibility(View.VISIBLE);

                    FormBody.Builder formBody = new FormBody.Builder();
                    for (int i = 0; i < newSelectedItem.size(); i++) {
                        formBody.add("classes[" + i + "]", classesItems.get(newSelectedItem.get(i)));
                        selectedClassesIDs.add(classesItems.get(newSelectedItem.get(i)));
                    }

                    OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(7, TimeUnit.SECONDS).build();

                    Request.Builder requestBuilder = new Request.Builder()
                            .url(com.solutionsbricks.solbricksframework.OkHttpClient.strip(App.getAppBaseUrl() + Constants.TASK_GET_SECTIONS_SUBJECTS));

                    requestBuilder.post(formBody.build());

                    Request request = requestBuilder.build();


                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, final IOException e) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                    if (e instanceof ConnectException) {
                                        Toast.makeText(getBaseContext(), Concurrent.getLangSubWords("noConnection", "No Internet Connection"), Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(AssignmentAddNew.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }


                        @Override
                        public void onResponse(Call call, final Response serverResponse) {
                            final Response responseObj = (Response) serverResponse;
                            try {
                                response = responseObj.body().string();
                            } catch (Exception e) {
                                Toast.makeText(AssignmentAddNew.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                                return;
                            }

                            if (response != null) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        try {
                                            if (responseObj.isSuccessful()) {

                                                JsonParser parser = new JsonParser();
                                                JsonObject ValuesHolder = null;

                                                try {
                                                    ValuesHolder = parser.parse(response).getAsJsonObject();
                                                } catch (Exception e) {
                                                    Toast.makeText(AssignmentAddNew.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                                                }

                                                classSelectedLock = false;
                                                if (ValuesHolder != null) {
                                                    JsonArray SubjectsArray = ValuesHolder.getAsJsonArray("subjects");
                                                    Iterator<JsonElement> ValsIter = SubjectsArray.iterator();
                                                    subjectsItems = new HashMap<>();
                                                    ArrayList<String> pickerItems = new ArrayList<>();

                                                    JsonObject CurrObj;
                                                    while (ValsIter.hasNext()) {
                                                        CurrObj = ValsIter.next().getAsJsonObject();
                                                        String subjectTitle = Concurrent.tagsStringValidator(CurrObj, "subjectTitle");
                                                        subjectsItems.put(subjectTitle, new SubjectsModel2(Concurrent.tagsIntValidator(CurrObj, "id"), subjectTitle, String.valueOf(classesItems.get(selectedClassesNames)), Concurrent.tagsStringValidator(CurrObj, "teacherId")));
                                                        pickerItems.add(subjectTitle);
                                                    }
                                                    ControlSubject.setAvailableItems(pickerItems);
                                                    ControlSubject.setWidgetListener(new FloatingLabelItemPicker.OnWidgetEventListener<String>() {
                                                        @Override
                                                        public void onShowItemPickerDialog(FloatingLabelItemPicker source) {
                                                            StringPickerDialogFragment itemPicker2 = StringPickerDialogFragment.newInstance(
                                                                    source.getId(),
                                                                    subjectsLang,
                                                                    Concurrent.getLangSubWords("ok", "OK"), Concurrent.getLangSubWords("cancel", "Cancel"),
                                                                    false,
                                                                    source.getSelectedIndices(),
                                                                    new ArrayList<>(source.getAvailableItems()));
                                                            itemPicker2.show(getSupportFragmentManager(), "Subject");
                                                        }
                                                    });

                                                    if (Concurrent.isSectionEnabled(AssignmentAddNew.this)) {
                                                        JsonArray SectionsArray = ValuesHolder.getAsJsonArray("sections");
                                                        Iterator<JsonElement> sectionsValsIter = SectionsArray.iterator();
                                                        sectionsItems = new HashMap<>();
                                                        pickerItems = new ArrayList<>();
                                                        while (sectionsValsIter.hasNext()) {
                                                            CurrObj = sectionsValsIter.next().getAsJsonObject();
                                                            String Title = Concurrent.tagsStringValidator(CurrObj, "sectionName") + " - " + Concurrent.tagsStringValidator(CurrObj, "sectionTitle");
                                                            sectionsItems.put(Title, new SectionsModel(Concurrent.tagsIntValidator(CurrObj, "id"), Concurrent.tagsStringValidator(CurrObj, "sectionName"), Concurrent.tagsStringValidator(CurrObj, "sectionTitle"), Concurrent.tagsIntValidator(CurrObj, "classId")));
                                                            pickerItems.add(Title);
                                                        }
                                                        if (Concurrent.isSectionEnabled(AssignmentAddNew.this)) {
                                                            ControlSections.setAvailableItems(pickerItems);
                                                            ControlSections.setWidgetListener(new FloatingLabelItemPicker.OnWidgetEventListener<String>() {
                                                                @Override
                                                                public void onShowItemPickerDialog(FloatingLabelItemPicker source) {
                                                                    StringPickerDialogFragment itemPicker2 = StringPickerDialogFragment.newInstance(
                                                                            source.getId(),
                                                                            sectionsLang,
                                                                            Concurrent.getLangSubWords("ok", "OK"), Concurrent.getLangSubWords("cancel", "Cancel"),
                                                                            true,
                                                                            source.getSelectedIndices(),
                                                                            new ArrayList<>(source.getAvailableItems()));
                                                                    itemPicker2.show(getSupportFragmentManager(), "Sections");
                                                                }
                                                            });
                                                        }
                                                    }
                                                }
                                            } else {
                                                Toast.makeText(AssignmentAddNew.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                                            }
                                        } catch (final Exception e) {
                                            Toast.makeText(AssignmentAddNew.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                                        }
                                        mProgressBar.setVisibility(INVISIBLE);
                                    }
                                });
                            } else {
                                Toast.makeText(AssignmentAddNew.this, Concurrent.getLangSubWords("errorOccurred", "Error Occurred"), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }


        } else if (source == ControlSubject) {
            choosenSubjectID = String.valueOf(subjectsItems.get(newSelectedItem.get(0)).id);
        } else if (source == ControlSections) {
            for (String sectionName : newSelectedItem) {
                choosenSectionID.add(String.valueOf(sectionsItems.get(sectionName).id));
            }
        }

    }

    @Override
    public void onCancelled(int pickerId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onItemsSelected(int pickerId, int[] selectedIndices) {
        if (pickerId == R.id.control_class) {
            ControlClasses.setSelectedIndices(selectedIndices);
        } else if (pickerId == R.id.control_subject) {
            ControlSubject.setSelectedIndices(selectedIndices);
        } else if (pickerId == R.id.control_section) {
            ControlSections.setSelectedIndices(selectedIndices);
        }
    }


    @Override
    public void onInstantSelected(int pickerId, Instant instant) {

    }


    @Override
    public void onDatePicked(Intent data) {
        Bundle bundle = data.getExtras();
        String SelectedDateAsString = bundle.getString("date");
        //String SelectedDateAsTimeStamp = bundle.getString("timestamp");       // Not used
        //String senderTag = bundle.getString("tag");                           // Not used

        datePickerText.setText(SelectedDateAsString);
        choosenDate = SelectedDateAsString;
    }
}
