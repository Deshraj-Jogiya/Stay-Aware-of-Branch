package com.schoolmgmtsys.root.ssg.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.schoolmgmtsys.root.ssg.fonts.ParentStyledTextView;
import com.solutionsbricks.solbricksframework.Call;
import com.solutionsbricks.solbricksframework.Callback;
import com.solutionsbricks.solbricksframework.FormBody;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.Request;
import com.solutionsbricks.solbricksframework.Response;

import org.apmem.tools.layouts.FlowLayout;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class StudentsShowMarksPage extends Fragment {

    private ListView ViewList;

    private Integer Res_PageLayout = R.layout.page_marksheet;
    private Integer Res_PageList = R.id.marksheet_view_list;
    private String TOKEN;
    private ProgressBar mProgressBar;
    private int STUDENT_ID;
    private View view;
    private boolean TokenRetry;
    private ArrayList<NLevelItem> NLListDATA;
    private View EmptyView;
    private View ErrorView;
    private boolean IsNewerVersion;
    private String response;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();

        if (bundle != null) STUDENT_ID = bundle.getInt("EXTRA_INT_1_FRAG");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(Res_PageLayout, container, false);
        ViewList = (ListView) view.findViewById(Res_PageList);


        if (getActivity() instanceof ControlActivity) {
            ControlActivity activity = (ControlActivity) getActivity();
            mProgressBar = activity.getProgressBar();
        }

        mProgressBar.setVisibility(View.INVISIBLE);

        ViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        ErrorView = view.findViewById(R.id.error_view);
        ErrorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();
            }
        });
        EmptyView = view.findViewById(R.id.empty_view);
        EmptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();
            }
        });
        loadData();


        return view;
    }

    public void loadData() {
        TOKEN = Concurrent.getAppToken(getActivity());
        NLListDATA = new ArrayList<>();
        final LayoutInflater inflater = LayoutInflater.from(getActivity());

        if (TOKEN != null) {
            if (STUDENT_ID != 0) {
                mProgressBar.setVisibility(View.VISIBLE);



                OkHttpClient client = new OkHttpClient().newBuilder(getActivity()).connectTimeout(7, TimeUnit.SECONDS).build();

                Request.Builder requestBuilder = new Request.Builder()
                        .url(App.getAppBaseUrl() + Constants.TASK_GET_STUDENT_MARKSHEET + "/" + STUDENT_ID);

                requestBuilder.get();

                Request request = requestBuilder.build();


                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                setErrorView(ErrorView, EmptyView, ViewList);
                                mProgressBar.setVisibility(View.INVISIBLE);
                                if (e instanceof ConnectException) {
                                    Toast.makeText(getContext(), Concurrent.getLangSubWords("noConnection", "No Internet Connection"), Toast.LENGTH_LONG).show();
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
                    public void onResponse(Call call, final Object serverResponse) {
                        final Response responseObj = (Response)serverResponse;
                        try {
                            response = responseObj.body().string();
                        } catch (Exception e) {
                            showError("5001");
                            return;
                        }

                        if (response != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    try {
                                        if (responseObj.isSuccessful()) {

                                            JsonParser parser = new JsonParser();
                                            JsonObject ValuesHolder = null;

                                            try {
                                                ValuesHolder = parser.parse(response).getAsJsonObject();
                                            } catch (Exception e) {
                                                showError("5001");
                                            }

                                            // Newer MarkSheet Model
                                            HashMap<Integer, String> ColsMap = null;
                                            HashMap<String, String> OutputColsMap = null;

                                            if (ValuesHolder != null) {
                                                String status = null;
                                                try {
                                                    status = Concurrent.tagsStringValidator(ValuesHolder, "status");
                                                } catch (Exception ignored) {}

                                                if (status != null && status.equals("failed")) {
                                                    setNoDataView(ErrorView, EmptyView, ViewList);
                                                    mProgressBar.setVisibility(View.INVISIBLE);
                                                    return;
                                                }

                                                ArrayList<MarksDetails> detailsArray;
                                                for (Map.Entry<String, JsonElement> entry : ValuesHolder.entrySet()) {
                                                    JsonObject itemObj = entry.getValue().getAsJsonObject();
                                                    detailsArray = new ArrayList<>();

                                                    MarkExamItem MarkParentItem = new MarkExamItem(Concurrent.tagsStringValidator(itemObj, "title"), Concurrent.tagsStringValidator(itemObj, "totalMarks"), Concurrent.tagsStringValidator(itemObj, "pointsAvg"), detailsArray);
                                                    if (itemObj.has("examMarksheetColumns")) {
                                                        ColsMap = new HashMap<>();

                                                        IsNewerVersion = true;

                                                        if(itemObj.has("examMarksheetColumns")){
                                                            JsonElement cols = itemObj.get("examMarksheetColumns");
                                                            if(cols != null && !cols.isJsonNull()){
                                                                JsonArray ColsArray = cols.getAsJsonArray();
                                                                for (JsonElement oneCol : ColsArray) {
                                                                    JsonObject ColItem = oneCol.getAsJsonObject();
                                                                    ColsMap.put(Concurrent.tagsIntValidator(ColItem,"id"),Concurrent.tagsStringValidator(ColItem,"title"));
                                                                }
                                                            }
                                                        }

                                                    }

                                                    final NLevelItem grandParent = new NLevelItem(MarkParentItem, null, new NLevelView() {
                                                        @Override
                                                        public View getView(NLevelItem item) {
                                                            View view = inflater.inflate(R.layout.page_marksheet_list_item, null);
                                                            ParentStyledTextView Title = (ParentStyledTextView) view.findViewById(R.id.header_title);
                                                            ParentStyledTextView AveragePoint = (ParentStyledTextView) view.findViewById(R.id.average_points_data);
                                                            ParentStyledTextView ExamMark = (ParentStyledTextView) view.findViewById(R.id.exam_marks_data);
                                                            TextView AveragePoints = (TextView) view.findViewById(R.id.AveragePoints);
                                                            TextView examMarks = (TextView) view.findViewById(R.id.examMarks);


                                                            MarkExamItem parentItem = (MarkExamItem) item.getWrappedObject();
                                                            Title.setNotNullText(parentItem.title);
                                                            AveragePoint.setNotNullText(parentItem.pointsAvg);
                                                            ExamMark.setNotNullText(parentItem.totalMarks);
                                                            Concurrent.setLangWords(getActivity(), AveragePoints, examMarks);

                                                            return view;
                                                        }
                                                    });
                                                    NLListDATA.add(grandParent);


                                                    if (itemObj.has("data")) {
                                                        for (Map.Entry<String, JsonElement> entryDetails : itemObj.get("data").getAsJsonObject().entrySet()) {
                                                            JsonObject itemDetailObj = entryDetails.getValue().getAsJsonObject();
                                                            MarksDetails MarkChildItem = null;

                                                            if (IsNewerVersion) {
                                                                MarkChildItem = new MarksDetails();

                                                                MarkChildItem.setTotalMark(Concurrent.tagsStringValidator(itemDetailObj, "totalMarks"));

                                                                if (itemDetailObj.has("examMark") && itemDetailObj.get("examMark") instanceof JsonObject) {
                                                                    JsonObject examMarkMap = itemDetailObj.getAsJsonObject("examMark");
                                                                    OutputColsMap = new HashMap<>();

                                                                    if (examMarkMap != null && !examMarkMap.isJsonNull()) {
                                                                        for (Map.Entry<String, JsonElement> entryItem : examMarkMap.entrySet()) {
                                                                            if (ColsMap != null && ColsMap.containsKey(Integer.valueOf(entryItem.getKey())) && !entryItem.getValue().isJsonNull()) {
                                                                                OutputColsMap.put(String.valueOf(ColsMap.get(Integer.valueOf(entryItem.getKey()))), entryItem.getValue().getAsString());
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                MarkChildItem.setMarksColsMap(OutputColsMap);
                                                                MarkChildItem.setExamState(Concurrent.tagsStringValidator(itemDetailObj, "examState"));
                                                                MarkChildItem.setFinalGrade(Concurrent.tagsStringValidator(itemDetailObj, "finalGrade"));
                                                                MarkChildItem.setPassGrade(Concurrent.tagsStringValidator(itemDetailObj, "passGrade"));
                                                                MarkChildItem.setGrade(Concurrent.tagsStringValidator(itemDetailObj, "grade"));
                                                                MarkChildItem.setMarkComments(Concurrent.tagsStringValidator(itemDetailObj, "markComments"));
                                                                MarkChildItem.setSubjectName(Concurrent.tagsStringValidator(itemDetailObj, "subjectName"));


                                                            } else {
                                                                MarkChildItem = new MarksDetails(Concurrent.tagsStringValidator(itemDetailObj, "subjectName"), Concurrent.tagsStringValidator(itemDetailObj, "examState"), Concurrent.tagsStringValidator(itemDetailObj, "examMark"), Concurrent.tagsStringValidator(itemDetailObj, "attendanceMark"), Concurrent.tagsStringValidator(itemDetailObj, "markComments"), Concurrent.tagsStringValidator(itemDetailObj, "passGrade"), Concurrent.tagsStringValidator(itemDetailObj, "finalGrade"), Concurrent.tagsStringValidator(itemDetailObj, "grade"));
                                                            }

                                                            NLevelItem child = new NLevelItem(MarkChildItem, grandParent, new NLevelView() {

                                                                @Override
                                                                public View getView(NLevelItem item) {

                                                                    View view = inflater.inflate(R.layout.page_marksheet_detail_list, null);

                                                                    ParentStyledTextView examMark = (ParentStyledTextView) view.findViewById(R.id.exam_mark_data);
                                                                    ParentStyledTextView examState = (ParentStyledTextView) view.findViewById(R.id.footer_status_data);
                                                                    ParentStyledTextView finalGradeData = (ParentStyledTextView) view.findViewById(R.id.final_grade_data);
                                                                    ParentStyledTextView GradeData = (ParentStyledTextView) view.findViewById(R.id.grade_data);
                                                                    ParentStyledTextView markComments = (ParentStyledTextView) view.findViewById(R.id.footer_comment_data);
                                                                    ParentStyledTextView passGradeData = (ParentStyledTextView) view.findViewById(R.id.pass_grade_data);
                                                                    ParentStyledTextView subjectName = (ParentStyledTextView) view.findViewById(R.id.header_title);

                                                                    FlowLayout ColsMarksLayout = (FlowLayout) view.findViewById(R.id.footer3);

                                                                    TextView Grade = (TextView) view.findViewById(R.id.Grade);
                                                                    TextView StatusTxt = (TextView) view.findViewById(R.id.Status);
                                                                    TextView Comments = (TextView) view.findViewById(R.id.Comments);
                                                                    TextView examMarks = (TextView) view.findViewById(R.id.examMarks);
                                                                    TextView passGrade = (TextView) view.findViewById(R.id.passGrade);
                                                                    TextView finalGrade = (TextView) view.findViewById(R.id.finalGrade);


                                                                    MarksDetails posDetailValues = (MarksDetails) item.getWrappedObject();

                                                                    if (IsNewerVersion) {
                                                                        HashMap<String, String> colsMap = posDetailValues.getMarksColsMap();
                                                                        if (!posDetailValues.TotalMark.equals(""))
                                                                            examMark.setNotNullText(posDetailValues.TotalMark);

                                                                        if (colsMap != null && colsMap.size() > 0) {
                                                                            for (Map.Entry<String, String> entry : colsMap.entrySet()) {

                                                                                LinearLayout colGradeItemItem = (LinearLayout) inflater.inflate(R.layout.marksheet_grade_item, null);
                                                                                ((TextView) colGradeItemItem.findViewById(R.id.col_grade_data)).setText(entry.getValue());
                                                                                ((TextView) colGradeItemItem.findViewById(R.id.col_grade)).setText(entry.getKey());
                                                                                ColsMarksLayout.addView(colGradeItemItem);

                                                                            }
                                                                        }


                                                                    }

                                                                    if (!posDetailValues.examState.equals(""))
                                                                        examState.setNotNullText(posDetailValues.examState);
                                                                    if (!posDetailValues.finalGrade.equals(""))
                                                                        finalGradeData.setNotNullText(posDetailValues.finalGrade);
                                                                    if (!posDetailValues.Grade.equals(""))
                                                                        GradeData.setNotNullText(posDetailValues.Grade);
                                                                    if (!posDetailValues.markComments.equals(""))
                                                                        markComments.setNotNullText(posDetailValues.markComments);
                                                                    if (!posDetailValues.passGrade.equals(""))
                                                                        passGradeData.setNotNullText(posDetailValues.passGrade);
                                                                    if (!posDetailValues.subjectName.equals(""))
                                                                        subjectName.setNotNullText(posDetailValues.subjectName);

                                                                    Concurrent.setLangWords(getActivity(), Grade, StatusTxt, Comments, examMarks, passGrade, finalGrade);


                                                                    return view;
                                                                }
                                                            });

                                                            NLListDATA.add(child);

                                                        }
                                                    }

                                                }

                                                NLevelAdapter adapter = new NLevelAdapter(NLListDATA);
                                                ViewList.setAdapter(adapter);
                                                ViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                    @Override
                                                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                                                        ((NLevelAdapter) ViewList.getAdapter()).toggle(arg2);
                                                        ((NLevelAdapter) ViewList.getAdapter()).getFilter().filter();
                                                    }
                                                });

                                            }
                                            mProgressBar.setVisibility(View.INVISIBLE);
                                            TokenRetry = false;
                                        } else {
                                            renewToken();
                                        }
                                    } catch (final Exception e) {
                                        Log.v("mtag","ffe "+e);
                                        showError("5002");
                                    }
                                }
                            });
                        } else {
                            showError("5001");
                        }
                    }
                });
            }
        }
    }


    public void renewToken(){
        if (!TokenRetry) {

            FormBody.Builder formBody = new FormBody.Builder();
            formBody.add("username", Concurrent.getAppUsername(getActivity()));
            formBody.add("password", Concurrent.getAppPassword(getActivity()));

            String refreshedToken = FirebaseInstanceId.getInstance().getToken();            if(refreshedToken != null)formBody.add("android_token", refreshedToken);

            OkHttpClient client = new OkHttpClient().newBuilder(getActivity()).connectTimeout(7, TimeUnit.SECONDS).build();

            Request.Builder requestBuilder = new Request.Builder()
                    .url(App.getAppBaseUrl() + Constants.TASK_LOGIN);

            requestBuilder.post(formBody.build());

            Request request = requestBuilder.build();


            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            setErrorView(ErrorView, EmptyView, ViewList);
                            mProgressBar.setVisibility(View.INVISIBLE);
                            if (e instanceof ConnectException) {
                                Toast.makeText(getActivity(), Concurrent.getLangSubWords("noConnection", "No Internet Connection"), Toast.LENGTH_LONG).show();
                            } else {
                                if (Concurrent.isFloat(e.getMessage())) {
                                    showError(e.getMessage());
                                } else {
                                    showError("5010");
                                }
                            }
                        }
                    });
                }

                @Override
                public void onResponse(Call call, final Object serverResponse) {
                    final Response responseObj = (Response)serverResponse;
                    try {
                        response = responseObj.body().string();
                    } catch (Exception e) {
                        showError("5001");
                        return;
                    }

                    if (response != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    if (responseObj.isSuccessful()) {

                                        String token;
                                        JsonParser parser = new JsonParser();
                                        JsonObject ValuesHolder = null;

                                        try {
                                            ValuesHolder = parser.parse(response).getAsJsonObject();
                                        } catch (Exception e) {
                                            showError("5001");
                                        }

                                        if (ValuesHolder != null) {
                                            token = Concurrent.tagsStringValidator(ValuesHolder, "token");
                                            if (token != null && token.length() > 1) {

                                                token = Concurrent.tagsStringValidator(ValuesHolder, "token");
                                                Concurrent.setAppToken(getActivity(), token);
                                                loadData();

                                            } else {
                                                showError("5011");
                                            }
                                        } else {
                                            showError("5001");
                                        }
                                    } else {
                                        renewToken();
                                    }
                                } catch (final Exception e) {
                                    Log.v("mtag","ddd "+e);

                                    showError("5002");
                                }
                            }
                        });
                    }else{
                        showError("5001");
                    }
                    TokenRetry = true;

                }
            });

        } else {
            showError("5010");
            setErrorView(ErrorView, EmptyView, ViewList);
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void showError(String errorCode) {
        String errorTitle;
        if (errorCode != null) {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: " + errorCode + " )";
        } else {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred");
        }
        setErrorView(ErrorView, EmptyView, ViewList);
        mProgressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(getActivity(), errorTitle, Toast.LENGTH_LONG).show();
    }


    public void setNoDataView(View errorView, View emptyView, ListView viewList) {
        if (viewList != null) viewList.setVisibility(View.INVISIBLE);
        if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
        if (errorView != null) errorView.setVisibility(View.INVISIBLE);
    }

    public void setDataView(View errorView, View emptyView, ListView viewList) {
        if (viewList != null) viewList.setVisibility(View.VISIBLE);
        if (emptyView != null) emptyView.setVisibility(View.INVISIBLE);
        if (errorView != null) errorView.setVisibility(View.INVISIBLE);
    }

    public void setErrorView(View errorView, View emptyView, ListView viewList) {
        if (viewList != null) viewList.setVisibility(View.INVISIBLE);
        if (emptyView != null) emptyView.setVisibility(View.INVISIBLE);
        if (errorView != null) errorView.setVisibility(View.VISIBLE);
    }

    class MarkExamItem {
        String title;
        String totalMarks;
        String pointsAvg;
        ArrayList<MarksDetails> detailsArray;

        public MarkExamItem(String title, String totalMarks, String pointsAvg, ArrayList<MarksDetails> detailsArray) {
            this.title = title;
            this.totalMarks = totalMarks;
            this.pointsAvg = pointsAvg;
            this.detailsArray = detailsArray;
        }
    }

    class MarksDetails {
        String subjectName;
        String examState;
        String examMark;
        String attendanceMark;
        String markComments;
        String passGrade;
        String finalGrade;
        String Grade;
        // Newer version variables
        HashMap<String, String> MarksColsMap = new HashMap<>();
        String TotalMark;

        public MarksDetails(String subjectName, String examState, String examMark, String attendanceMark, String markComments, String passGrade, String finalGrade, String Grade) {
            this.subjectName = subjectName;
            this.examState = examState;
            this.examMark = examMark;
            this.attendanceMark = attendanceMark;
            this.markComments = markComments;
            this.passGrade = passGrade;
            this.finalGrade = finalGrade;
            this.Grade = Grade;
        }

        public MarksDetails() {

        }

        // Newer version functions
        public String getTotalMark() {
            return TotalMark;
        }

        public void setTotalMark(String totalMark) {
            TotalMark = totalMark;
        }

        public HashMap<String, String> getMarksColsMap() {
            return MarksColsMap;
        }

        public void setMarksColsMap(HashMap<String, String> marksColsMap) {
            MarksColsMap = marksColsMap;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public void setSubjectName(String subjectName) {
            this.subjectName = subjectName;
        }

        public String getExamState() {
            return examState;
        }

        public void setExamState(String examState) {
            this.examState = examState;
        }

        public String getExamMark() {
            return examMark;
        }

        public void setExamMark(String examMark) {
            this.examMark = examMark;
        }

        public String getAttendanceMark() {
            return attendanceMark;
        }

        public void setAttendanceMark(String attendanceMark) {
            this.attendanceMark = attendanceMark;
        }

        public String getMarkComments() {
            return markComments;
        }

        public void setMarkComments(String markComments) {
            this.markComments = markComments;
        }

        public String getPassGrade() {
            return passGrade;
        }

        public void setPassGrade(String passGrade) {
            this.passGrade = passGrade;
        }

        public String getFinalGrade() {
            return finalGrade;
        }

        public void setFinalGrade(String finalGrade) {
            this.finalGrade = finalGrade;
        }

        public String getGrade() {
            return Grade;
        }

        public void setGrade(String grade) {
            Grade = grade;
        }
    }

}
