package com.schoolmgmtsys.root.ssg.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.schoolmgmtsys.root.ssg.models.StudentsAttendModel;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.solutionsbricks.solbricksframework.Call;
import com.solutionsbricks.solbricksframework.Callback;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.Request;
import com.solutionsbricks.solbricksframework.Response;
import com.solutionsbricks.solbricksframework.helpers.GsonParser;
import com.solutionsbricks.solbricksframework.helpers.ListManager;
import com.schoolmgmtsys.root.ssg.fonts.ParentStyledTextView;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;


public class StudentsAttendancePage extends Fragment implements ListManager.ListInterface {

    public static HashMap<String, String> statusIdentifierKeyFirst;
    private ListView ViewList;
    private ListManager mListManager;
    private StudentAttendHolder holder;
    private StudentsAttendModel posValues;
    private Integer Res_PageLayout = R.layout.page_students_attend;
    private Integer Res_PageList = R.id.student_attend_view_list;
    private Integer Res_PageItemList = R.layout.page_students_attend_item;
    private String TOKEN;
    private ArrayList<StudentsAttendModel> attendItems = new ArrayList<>();
    private HashMap<String, String> subjectsItems;
    private HashMap<String, String> statusIdentifier;
    private View errorView;
    private View emptyView;
    private String response;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        TOKEN = Concurrent.getAppToken(getActivity());
        if (TOKEN != null) {
            if (bundle != null) {

                statusIdentifierKeyFirst = new HashMap<>();
                statusIdentifierKeyFirst.put("All", Concurrent.getLangSubWords("all","All"));
                statusIdentifierKeyFirst.put("1", Concurrent.getLangSubWords("Present","Present"));
                statusIdentifierKeyFirst.put("0", Concurrent.getLangSubWords("Absent","Absent"));
                statusIdentifierKeyFirst.put("2", Concurrent.getLangSubWords("Late","Late"));
                statusIdentifierKeyFirst.put("3", Concurrent.getLangSubWords("LateExecuse","Late with excuse"));
                statusIdentifierKeyFirst.put("4", Concurrent.getLangSubWords("earlyDismissal","Early Dismissal"));
                statusIdentifierKeyFirst.put("9", Concurrent.getLangSubWords("acceptedVacation","Accepted Vacation"));


                statusIdentifier = new HashMap<>();
                statusIdentifier.put("All", Concurrent.getLangSubWords("all","All"));
                statusIdentifier.put("1", Concurrent.getLangSubWords("Present","Present"));
                statusIdentifier.put("0", Concurrent.getLangSubWords("Absent","Absent"));
                statusIdentifier.put("2", Concurrent.getLangSubWords("Late","Late"));
                statusIdentifier.put("3", Concurrent.getLangSubWords("LateExecuse","Late with excuse"));
                statusIdentifier.put("4", Concurrent.getLangSubWords("earlyDismissal","Early Dismissal"));
                statusIdentifier.put("9", Concurrent.getLangSubWords("acceptedVacation","Accepted Vacation"));


                int STUDENT_ID = bundle.getInt("EXTRA_INT_1_FRAG");



                OkHttpClient client = new OkHttpClient().newBuilder(getActivity()).connectTimeout(7, TimeUnit.SECONDS).build();

                /*Request.Builder requestBuilder = new Request.Builder()
                        .url(App.getAppBaseUrl() + Constants.TASK_GET_STUDENT_ATTEND + "/" + STUDENT_ID);
*/
                Request.Builder requestBuilder = new Request.Builder()
                        .url(App.getAppBaseUrl() + Constants.TASK_GET_STUDENT_ATTEND + "/" + STUDENT_ID);
                requestBuilder.get();

                Request request = requestBuilder.build();


                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                mListManager.setErrorView(errorView, emptyView, ViewList);
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

                                            if (ValuesHolder != null) {
                                                GsonParser parserManager = new GsonParser();
                                                JsonArray ValuesArray = ValuesHolder.getAsJsonArray("attendance");
                                                if (!Concurrent.AttendanceModelIsClass) {
                                                    subjectsItems = parserManager.objectLooper(ValuesHolder, "subjects", true);
                                                }
                                                if (ValuesArray != null) {
                                                    Iterator<JsonElement> ValsIter = ValuesArray.iterator();

                                                    if (!ValsIter.hasNext())
                                                        mListManager.setNoDataView(errorView, emptyView, ViewList);

                                                    while (ValsIter.hasNext()) {
                                                        JsonObject CurrObj = ValsIter.next().getAsJsonObject();
                                                        String statusId = Concurrent.tagsStringValidator(CurrObj, "status");
                                                        if (!Concurrent.AttendanceModelIsClass) {
                                                            String subjectId = Concurrent.tagsStringValidator(CurrObj, "subjectId");
                                                            attendItems.add(new StudentsAttendModel(Concurrent.tagsIntValidator(CurrObj, "id"), Concurrent.tagsStringValidator(CurrObj, "studentId"), subjectId, Concurrent.tagsStringValidator(CurrObj, "date"), statusId, Concurrent.repairJsonValueQuotes(subjectsItems.get(subjectId)), statusIdentifier.get(statusId)));
                                                        } else {
                                                            attendItems.add(new StudentsAttendModel(Concurrent.tagsIntValidator(CurrObj, "id"), Concurrent.tagsStringValidator(CurrObj, "studentId"), null, Concurrent.tagsStringValidator(CurrObj, "date"), statusId, null, statusIdentifier.get(statusId)));
                                                        }
                                                    }
                                                    if (mListManager != null)
                                                        mListManager.getListAdapter().notifyDataSetChanged();
                                                } else {
                                                    mListManager.setErrorView(errorView, emptyView, ViewList);

                                                }
                                            }else{
                                                mListManager.setErrorView(errorView, emptyView, ViewList);
                                            }
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

    }
    public void showError(String errorCode) {
        String errorTitle;
        if (errorCode != null) {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: " + errorCode + " )";
        } else {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred");
        }
        mListManager.setErrorView(errorView, emptyView, ViewList);
        Toast.makeText(getContext(), errorTitle, Toast.LENGTH_LONG).show();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(Res_PageLayout, container, false);
        errorView = view.findViewById(R.id.error_view);
        emptyView = view.findViewById(R.id.empty_view);


        ViewList = (ListView) view.findViewById(Res_PageList);

        ViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        mListManager = new ListManager(getActivity(), ViewList, this, attendItems);
        mListManager.removeFooter();
        ViewList.setEmptyView(view.findViewById(R.id.empty));
        return view;
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

            holder.Date = (ParentStyledTextView) convertView.findViewById(R.id.header_title);
            holder.SubjectData = (ParentStyledTextView) convertView.findViewById(R.id.footer_subject_data);
            holder.Status = (ParentStyledTextView) convertView.findViewById(R.id.footer_status_data);
            holder.FooterSubjectCon = (LinearLayout) convertView.findViewById(R.id.footer_subject_con);

            holder.Attendance = (TextView) convertView.findViewById(R.id.Attendance);
            holder.Subject = (TextView) convertView.findViewById(R.id.Subject);

            convertView.setTag(holder);
        } else {
            holder = (StudentAttendHolder) convertView.getTag();
        }
        posValues = attendItems.get(position);
        if (posValues != null) {
            holder.Date.setNotNullText(posValues.date);
            if (!Concurrent.AttendanceModelIsClass) {
                holder.SubjectData.setNotNullText(posValues.subjectName);
            } else {
                holder.FooterSubjectCon.setVisibility(View.GONE);
            }
            holder.Status.setNotNullText(posValues.statusName);

            if (!Concurrent.AttendanceModelIsClass) {
                Concurrent.setLangWords(getActivity(), holder.Attendance, holder.Subject);
            } else {
                Concurrent.setLangWords(getActivity(), holder.Attendance);
            }
        }

        return convertView;
    }

    class StudentAttendHolder {

        ParentStyledTextView SubjectData;
        ParentStyledTextView Status;
        ParentStyledTextView Date;
        LinearLayout FooterSubjectCon;

        TextView Subject;
        TextView Attendance;
    }
}
