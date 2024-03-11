package com.schoolmgmtsys.root.ssg.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.marvinlabs.widget.floatinglabel.edittext.FloatingLabelEditText;
import com.schoolmgmtsys.root.ssg.models.ExamMarkModel;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.schoolmgmtsys.root.ssg.utils.DrawerListFragment;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.helpers.ListManager;
import com.schoolmgmtsys.root.ssg.fonts.MediumStyledTextView;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExamMarkPage extends SlidingFragmentActivity implements ListManager.ListInterface, FloatingLabelEditText.EditTextListener {

    private static com.orhanobut.dialogplus.ListHolder ListHolder;
    private ListView ViewList;
    private ListManager mListManager;
    private ExamMarkHolder holder;

    private ExamMarkModel posValues;
    private Integer Res_PageLayout = R.layout.page_exam_mark;
    private Integer Res_PageList = R.id.exam_mark_view_list;
    private Integer Res_PageItemList = R.layout.page_exam_mark_list_item;
    private String TOKEN;
    private ArrayList<ExamMarkModel> examList;
    private ProgressBar mProgressBar;
    private SharedPreferences Prefs;
    private JsonObject json;
    private boolean IsNewerVersion;
    private HashMap<Integer, String> MarkCols;
    private HashMap<String, Integer> MarkColsReverse;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Concurrent.getLangDirection(this).equals("ar"))
            getSlidingMenu().setMode(SlidingMenu.RIGHT);
        else getSlidingMenu().setMode(SlidingMenu.LEFT);

        setContentView(Res_PageLayout);
        getSlidingMenu().setBehindWidth((int) Math.round(Concurrent.getScreenWidth(this) * 70.0 / 100.0));
        setBehindContentView(R.layout.drawer_frame);

        MarkColsReverse = new HashMap<>();

        examList = getIntent().getParcelableArrayListExtra("marksList");
        MarkCols = (HashMap<Integer, String>) getIntent().getSerializableExtra("markcols");

        if (MarkCols != null) {
            IsNewerVersion = true;
        }

        if (examList != null) {

            ListFragment mFrag;
            FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
            if (savedInstanceState == null) {
                mFrag = new DrawerListFragment();
            } else {
                mFrag = (ListFragment) this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
            }
            t.replace(R.id.menu_frame, mFrag);
            t.commit();


            json = new JsonObject();

            JsonObject classObject = new JsonObject();
            classObject.addProperty("classAcademicYear", parseExtra("class_classAcademicYear"));
            classObject.addProperty("className", parseExtra("class_className"));
            classObject.addProperty("classSubjects", parseExtra("class_classSubjects"));
            classObject.addProperty("classTeacher", parseExtra("class_classTeacher"));
            classObject.addProperty("dormitoryId", parseExtra("class_dormitoryId"));
            classObject.addProperty("id", parseExtra("class_id"));

            JsonObject examObject = new JsonObject();
            examObject.addProperty("examAcYear", parseExtra("exam_examAcYear"));
            examObject.addProperty("examClasses", parseExtra("exam_examClasses"));
            examObject.addProperty("examMarksheetColumns", parseExtra("exam_examMarksheetColumns"));


            examObject.addProperty("examDate", parseExtra("exam_examDate"));
            examObject.addProperty("examDescription", parseExtra("exam_examDescription"));
            examObject.addProperty("examTitle", parseExtra("exam_examTitle"));
            examObject.addProperty("id", parseExtra("exam_id"));

            JsonObject subjectObject = new JsonObject();
            subjectObject.addProperty("finalGrade", parseExtra("subject_finalGrade"));
            subjectObject.addProperty("id", parseExtra("subject_id"));
            subjectObject.addProperty("passGrade", parseExtra("subject_passGrade"));
            subjectObject.addProperty("subjectTitle", parseExtra("subject_subjectTitle"));
            subjectObject.addProperty("teacherId", parseExtra("subject_teacherId"));

            json.add("respSubject", subjectObject);
            json.add("respExam", examObject);
            json.add("respClass", classObject);

            json.addProperty("classId", parseExtra("class_id"));
            json.addProperty("exam", parseExtra("exam_id"));
            json.addProperty("subjectId", parseExtra("subject_id"));

            String secID = parseExtra("sectionId");
            if(secID != null && !secID.equals(""))json.addProperty("sectionId",secID);

            Prefs = PreferenceManager.getDefaultSharedPreferences(ExamMarkPage.this);


            mProgressBar = (ProgressBar) findViewById(R.id.gen_loader);

            TextView HeadTitle = (TextView) findViewById(R.id.head_drawer_title);
            HeadTitle.setText(Concurrent.getLangSubWords("examMarks", "Exam Marks"));

            ImageView ToogleBtn = (ImageView) findViewById(R.id.head_drawer_toggle);
            ToogleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggle();
                }
            });

            mProgressBar.setVisibility(View.INVISIBLE);

            LinearLayout SaveEdits = (LinearLayout) findViewById(R.id.saving_footer);
            TextView SaveTxt = (TextView) findViewById(R.id.save_txt);
            SaveTxt.setText(Concurrent.getLangSubWords("addUpdateMarks", "Add/Update Marks"));

            if (Concurrent.isUserHavePermission(this, "examsList.controlMarksExam")) {
                SaveEdits.setOnClickListener(new View.OnClickListener() {
                public String TOKEN;

                @Override
                public void onClick(View view) {

                    TOKEN = Concurrent.getAppToken(ExamMarkPage.this);
                    if (TOKEN != null) {
                        mProgressBar.setVisibility(View.VISIBLE);

                        json.add("respStudents", prepareStudentsList());

                        Ion.with(ExamMarkPage.this).load(OkHttpClient.strip(App.getAppBaseUrl() + Constants.TASK_EDIT_EXAM_MARKS + "/" + Concurrent.repairJsonValueQuotes(getIntent().getStringExtra("exam_id")) + "/" + Concurrent.repairJsonValueQuotes(getIntent().getStringExtra("class_id")) + "/" + Concurrent.repairJsonValueQuotes(getIntent().getStringExtra("subject_id")))).setTimeout(10000).setJsonObjectBody(json)
                                .asJsonObject().setCallback(new FutureCallback<JsonObject>() {


                            public String ResultStatus;

                            @Override
                            public void onCompleted(Exception exception, JsonObject ValuesHolder) {
                                if (exception == null) {
                                    ResultStatus = Concurrent.tagsStringValidator(ValuesHolder, "status");


                                    if (ResultStatus != null && ResultStatus.equals("success")) {
                                        Toast.makeText(ExamMarkPage.this, Concurrent.getLangSubWords("updatesSaved","Updates Saved"), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ExamMarkPage.this, Concurrent.getLangSubWords("errorOccurred","Error Occurred"), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(ExamMarkPage.this, Concurrent.getLangSubWords("errorOccurred","Error Occurred"), Toast.LENGTH_LONG).show();

                                }
                                mProgressBar.setVisibility(View.INVISIBLE);
                            }
                        });

                    }


                }
            });
            }else{
                SaveEdits.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getBaseContext(), "You have no access to edit marks, please contact administrator", Toast.LENGTH_LONG).show();
                    }
                });
            }

            ViewList = (ListView) findViewById(Res_PageList);

            ViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                }
            });


            mListManager = new ListManager(this, ViewList, this, examList);
            mListManager.removeFooter();

        }
    }

    public String parseExtra(String childName) {
        String extraString = getIntent().getStringExtra(childName);
        if (extraString != null && !extraString.equals("")) {
            return extraString;
        }
        return "";
    }

    public JsonArray prepareStudentsList() {
        JsonArray jsonArray = new JsonArray();
        JsonObject innerObject;
        for (ExamMarkModel stdItem : examList) {
            innerObject = new JsonObject();
            innerObject.addProperty("id", Integer.valueOf(stdItem.id));
            innerObject.addProperty("markComments", stdItem.MarkComment);
            innerObject.addProperty("name", stdItem.StudentName);
            innerObject.addProperty("studentRollId", stdItem.StudentRollId);

            innerObject.addProperty("attendanceMark","");
            innerObject.addProperty("totalMarks", stdItem.TotalMark);

            JsonObject examMarkObj = new JsonObject();
            for (Map.Entry<Integer, String> entry : stdItem.ExamMarksMap.entrySet()) {
                examMarkObj.addProperty(entry.getKey().toString(),entry.getValue());
            }
            innerObject.add("examMark",examMarkObj);


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
    public View AdapterGetView(int position, View convertView) {
        LayoutInflater inflater = getLayoutInflater();
        if (convertView == null || convertView.getTag() == null) {
            holder = new ExamMarkHolder();
            convertView = inflater.inflate(Res_PageItemList, null);

            holder.StudentName = (MediumStyledTextView) convertView.findViewById(R.id.header_title);
            holder.StudentRollId = (MediumStyledTextView) convertView.findViewById(R.id.roll_id_data);

            holder.MarkComment = (FloatingLabelEditText) convertView.findViewById(R.id.footer_comment_data);
            holder.totalMarksData = (FloatingLabelEditText) convertView.findViewById(R.id.footer_totalMarks_data);

            holder.ColsMarksLayout = (FlowLayout) convertView.findViewById(R.id.footer3);

            holder.Comments = (TextView) convertView.findViewById(R.id.Comments);
            holder.totalMarks = (TextView) convertView.findViewById(R.id.totalMarks);
            holder.rollid = (TextView) convertView.findViewById(R.id.rollid);

            holder.MarksCols = new HashMap<>();
            for (Map.Entry<Integer, String> entry : MarkCols.entrySet()) {
                FormEditText formEditText = getFormEditText(ExamMarkPage.this);
                holder.MarksCols.put(entry.getValue(),formEditText);
                holder.ColsMarksLayout.addView((View) formEditText.ParentLayout);
                MarkColsReverse.put(entry.getValue(),entry.getKey());

                formEditText.ExamMarkDataView.addTextChangedListener(new MyTextWatcher(formEditText.ExamMarkDataView));
            }
            convertView.setTag(holder);
        } else {
            holder = (ExamMarkHolder) convertView.getTag();
        }
        posValues = examList.get(position);

        holder.totalMarksData.setTag(position);

        holder.totalMarksData.setEditTextListener(this);

        holder.MarkComment.setTag(position);
        holder.MarkComment.setEditTextListener(this);

        if (posValues != null) {
            holder.StudentName.setNotNullText(posValues.StudentName);
            holder.StudentRollId.setNotNullText(posValues.StudentRollId);

            holder.MarkComment.setInputWidgetText(posValues.MarkComment);

            if (IsNewerVersion) {
                HashMap<String, String> colsMap = posValues.MarksColsMap;
                if (posValues.TotalMark != null && !posValues.TotalMark.equals(""))
                    holder.totalMarksData.setInputWidgetText(posValues.TotalMark);

                if(holder.MarksCols != null && holder.MarksCols.size() > 0 ){
                    for (Map.Entry<String, FormEditText> entry : holder.MarksCols.entrySet()) {
                        FormEditText formEditText = entry.getValue();
                        EditText ExamMarkDataView = formEditText.ExamMarkDataView;
                        MediumStyledTextView ExamMarkView = formEditText.ExamMarkView;

                        if(colsMap.containsKey(entry.getKey())) {
                            String mValue = colsMap.get(entry.getKey());
                            ExamMarkDataView.setTag(new ExamMarkTag(MarkColsReverse.get(entry.getKey()),position));
                            ExamMarkDataView.setText(mValue);
                        }else{
                            ExamMarkDataView.setTag(new ExamMarkTag(MarkColsReverse.get(entry.getKey()),position));
                            ExamMarkDataView.setText("");
                        }
                        ExamMarkView.setNotNullText(entry.getKey());
                    }
                }


                //LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f);
               // param1.gravity= Gravity.CENTER;
                //LinearLayout examMarkCon = (LinearLayout) convertView.findViewById(R.id.exam_mark_con);
                //examMarkCon.setLayoutParams(param1);
                //LinearLayout footerCon = (LinearLayout) convertView.findViewById(R.id.footer_comment);
                //footerCon.setLayoutParams(param1);

            } else {
                holder.totalMarksData.setInputWidgetText(posValues.ExamMark);
            }
            Concurrent.setLangWords(this, holder.Comments, holder.totalMarks,  holder.rollid);
        }

        return convertView;
    }

    @Override
    public void onTextChanged(FloatingLabelEditText source, String text) {

        Integer position = (Integer) source.getTag();
        if (source.getId() == R.id.footer_totalMarks_data) {
            examList.get(position).TotalMark = text;
        } else if (source.getId() == R.id.footer_comment_data) {
            examList.get(position).MarkComment = text;
        }

    }

    private class MyTextWatcher implements TextWatcher {

        private EditText mEditText;

        public MyTextWatcher(EditText editText) {
            mEditText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if(mEditText.getTag() != null){
                ExamMarkTag cTag = (ExamMarkTag) mEditText.getTag();
                examList.get(cTag.positionINArry).ExamMarksMap.put(cTag.colID,mEditText.getText().toString());
                examList.get(cTag.positionINArry).MarksColsMap.put(MarkCols.get(cTag.colID),mEditText.getText().toString());
            }

        }
    }

    private class ExamMarkTag{
        private Integer colID;
        private Integer positionINArry;
        private ExamMarkTag(Integer colID, Integer positionINArry) {
            this.colID = colID;
            this.positionINArry = positionINArry;
        }
    }

    class ExamMarkHolder {
        public TextView Comments;
        public TextView totalMarks;
        public TextView rollid;
        MediumStyledTextView StudentName;
        MediumStyledTextView StudentRollId;
        FloatingLabelEditText MarkComment;
        FloatingLabelEditText totalMarksData;
        public FlowLayout ColsMarksLayout;
        public HashMap<String,FormEditText> MarksCols;
    }

    private FormEditText getFormEditText(Activity mActivity) {

        LinearLayout parent = new LinearLayout(mActivity);
        parent.setOrientation(LinearLayout.VERTICAL);

        EditText ExamMarkDataView = new EditText(mActivity);
        ExamMarkDataView.setInputType(InputType.TYPE_CLASS_NUMBER);
        ExamMarkDataView.setGravity(Gravity.CENTER);

        MediumStyledTextView ExamMarkView = new MediumStyledTextView(mActivity);
        ExamMarkView.setTextColor(Color.BLACK);
        ExamMarkView.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        parent.addView(ExamMarkDataView,layoutParams);
        parent.addView(ExamMarkView,layoutParams);

        return new FormEditText(parent, ExamMarkDataView, ExamMarkView);
    }

    private class FormEditText {
        public Object ParentLayout;
        public EditText ExamMarkDataView;
        public MediumStyledTextView ExamMarkView;

        FormEditText(Object ParentLayout, EditText ExamMarkDataView, MediumStyledTextView ExamMarkView) {
            this.ParentLayout = ParentLayout;
            this.ExamMarkDataView = ExamMarkDataView;
            this.ExamMarkView = ExamMarkView;
        }
    }

}
