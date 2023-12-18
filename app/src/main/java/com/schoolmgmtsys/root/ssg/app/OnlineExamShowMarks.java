package com.schoolmgmtsys.root.ssg.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.schoolmgmtsys.root.ssg.models.OnlineExamMarks;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.solutionsbricks.solbricksframework.Call;
import com.solutionsbricks.solbricksframework.Callback;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.Request;
import com.solutionsbricks.solbricksframework.Response;
import com.solutionsbricks.solbricksframework.helpers.CustomImageView;
import com.solutionsbricks.solbricksframework.helpers.ListManager;
import com.schoolmgmtsys.root.ssg.fonts.ParentStyledTextView;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.view.View.INVISIBLE;


public class OnlineExamShowMarks extends Fragment implements ListManager.ListInterface {

    private ListView ViewList;
    private ListManager mListManager;
    private StudentAttendHolder holder;

    private OnlineExamMarks posValues;
    private Integer Res_PageLayout = R.layout.page_online_exam_marks;
    private Integer Res_PageList = R.id.online_exams_marks_view_list;
    private Integer Res_PageItemList = R.layout.page_online_exam_marks_item;
    private String TOKEN;
    private ArrayList<OnlineExamMarks> marksItems = new ArrayList<>();
    private Integer successGrade;
    private View errorView;
    private View emptyView;
    private ProgressBar mProgressBar;
    private Integer STUDENT_ID;
    private String response;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        STUDENT_ID = bundle.getInt("EXTRA_INT_1_FRAG");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(Res_PageLayout, container, false);

        if (getActivity() instanceof ControlActivity) {
            ControlActivity activity = (ControlActivity) getActivity();
            mProgressBar = activity.getProgressBar();
        }

        errorView = view.findViewById(R.id.error_view);
        emptyView = view.findViewById(R.id.empty_view);


        ViewList = (ListView) view.findViewById(Res_PageList);

        mListManager = new ListManager(getActivity(), ViewList, this, marksItems);
        mListManager.removeFooter();

        TOKEN = Concurrent.getAppToken(getActivity());
        if (TOKEN != null) {
            if (STUDENT_ID != null) {
                mProgressBar.setVisibility(View.VISIBLE);


                OkHttpClient client = new OkHttpClient().newBuilder(getActivity()).connectTimeout(7, TimeUnit.SECONDS).build();

                Request.Builder requestBuilder = new Request.Builder()
                        .url(App.getAppBaseUrl() + Constants.TASK_ONLINE_EXAMS_MARKS + "/" + STUDENT_ID);

                requestBuilder.get();

                Request request = requestBuilder.build();


                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                mListManager.setErrorView(errorView, emptyView, ViewList);
                                mProgressBar.setVisibility(INVISIBLE);
                                if (e instanceof ConnectException) {
                                    Toast.makeText(getActivity(), Concurrent.getLangSubWords("noConnection", "No Internet Connection"), Toast.LENGTH_LONG).show();
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
                        final Response responseObj = (Response) serverResponse;
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

                                            if (ValuesHolder != null) {

                                                try {
                                                    successGrade = Concurrent.tagsIntValidator(ValuesHolder, "examDegreeSuccess");
                                                } catch (Exception ignored) {
                                                }

                                                JsonArray gradeJsonArray = ValuesHolder.getAsJsonArray("grade");
                                                if (gradeJsonArray.isJsonNull() || gradeJsonArray.size() == 0) {
                                                    mListManager.setNoDataView(errorView, emptyView, ViewList);
                                                    mProgressBar.setVisibility(INVISIBLE);
                                                    return;
                                                }

                                                for (JsonElement aGradeJsonArray : gradeJsonArray) {
                                                    JsonObject CurrObj = aGradeJsonArray.getAsJsonObject();
                                                    marksItems.add(new OnlineExamMarks(Concurrent.tagsIntValidator(CurrObj, "id"), Concurrent.tagsStringValidator(CurrObj, "examGrade"), Concurrent.tagsStringValidator(CurrObj, "examDate"), Concurrent.tagsStringValidator(CurrObj, "fullName"), Concurrent.tagsIntValidator(CurrObj, "studentId")));
                                                }
                                                mListManager.getListAdapter().notifyDataSetChanged();


                                            } else {
                                                showError("5001");
                                            }

                                            mProgressBar.setVisibility(INVISIBLE);
                                        } else {
                                            showError("5010");
                                        }
                                    } catch (final Exception e) {
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

        return view;
    }
    public void showError(String errorCode) {
        String errorTitle;
        if (errorCode != null) {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: " + errorCode + " )";
        } else {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred");
        }
        mListManager.setErrorView(errorView, emptyView, ViewList);
        mProgressBar.setVisibility(INVISIBLE);
        Toast.makeText(getContext(), errorTitle, Toast.LENGTH_LONG).show();
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void loadMore() {

    }

    @Override
    public void AdapterConstructor() {

    }

    @Override
    public View AdapterGetView(int position, View convertView) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        if (convertView == null || convertView.getTag() == null) {
            holder = new StudentAttendHolder();
            convertView = inflater.inflate(Res_PageItemList, null);

            holder.StudentName = (ParentStyledTextView) convertView.findViewById(R.id.header_student_data);
            holder.DateData = (ParentStyledTextView) convertView.findViewById(R.id.footer_date_data);
            holder.GradeData = (ParentStyledTextView) convertView.findViewById(R.id.footer_grade_data);
            holder.StudentImg = (CustomImageView) convertView.findViewById(R.id.header_student_img);
            holder.GradePass = (ParentStyledTextView) convertView.findViewById(R.id.header_grade_pass_data);

            holder.student = (TextView) convertView.findViewById(R.id.student);
            holder.Grade = (TextView) convertView.findViewById(R.id.Grade);
            holder.Date = (TextView) convertView.findViewById(R.id.Date);

            convertView.setTag(holder);
        } else {
            holder = (StudentAttendHolder) convertView.getTag();
        }
        posValues = marksItems.get(position);
        if (posValues != null) {
            holder.StudentName.setNotNullText(posValues.FullName);
            holder.DateData.setNotNullText(posValues.examDate);
            holder.GradeData.setNotNullText(posValues.examGrade);
            holder.StudentImg.profileID = String.valueOf(posValues.studentId);
            holder.StudentImg.load();

            if (successGrade != null && successGrade != 0) {
                try{
                    if(posValues.examGrade == null || posValues.examGrade.equals("")){
                        holder.GradePass.setNotNullText("Not completed exam");
                    }else{
                        if (successGrade > Integer.valueOf(posValues.examGrade)) {
                            holder.GradePass.setNotNullText("Failed");
                        } else {
                            holder.GradePass.setNotNullText("Passed");
                        }
                    }
                }catch (Exception ignored){}
            }
            Concurrent.setLangWords(getActivity(), holder.student, holder.Grade, holder.Date);
        }
        return convertView;
    }

    class StudentAttendHolder {
        public TextView Date;
        public TextView Grade;
        public TextView student;
        ParentStyledTextView StudentName;
        ParentStyledTextView GradePass;
        CustomImageView StudentImg;
        ParentStyledTextView DateData;
        ParentStyledTextView GradeData;
    }
}
