package com.schoolmgmtsys.root.ssg.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.schoolmgmtsys.root.ssg.fonts.ParentStyledTextView;
import com.schoolmgmtsys.root.ssg.models.ParentsAttendModel;
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
import com.solutionsbricks.solbricksframework.helpers.PinnedSectionListView;

import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class ParentsAttendancePage extends Fragment {

    public static HashMap<String, String> statusIdentifierKeyFirst;
    public static Integer SECTION_FLAG = 0;
    public static Integer ITEM_FLAG = 1;
    private PinnedSectionListView ViewList;
    private ParentsAttendHolder holder;
    private Integer Res_PageLayout = R.layout.page_parents_attend;
    private Integer Res_PageList = R.id.student_attend_view_list;
    private Integer Res_PageItemList = R.layout.page_parents_attend_item;
    private String TOKEN;
    private HashMap<String, String> subjectsItems;
    private HashMap<String, String> statusIdentifier;
    private HashMap<String, String> breakIdentifier;
    private HashMap<String, String> feesIdentifier;
    private HashMap<String, String> makeU0Identifier;


    private ParentsAttendAdapter AttendListAdapter;
    private String response;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void showError(String errorCode) {
        setErrorView();
        String errorTitle;
        if (errorCode != null) {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: " + errorCode + " )";
        } else {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred");
        }
        Toast.makeText(getContext(), errorTitle, Toast.LENGTH_LONG).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(Res_PageLayout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toast.makeText(getActivity(), Concurrent.getLangSubWords("pleaseWait", "Please Wait"), Toast.LENGTH_SHORT).show();

        Bundle bundle = this.getArguments();
        TOKEN = Concurrent.getAppToken(getActivity());

        if (TOKEN != null) {
            if (bundle != null) {
                AttendListAdapter = new ParentsAttendAdapter(getActivity(), R.layout.list_head_title, R.id.text1);

                statusIdentifierKeyFirst = new HashMap<>();
                statusIdentifierKeyFirst.put("All", Concurrent.getLangSubWords("all", "All"));
                statusIdentifierKeyFirst.put("1", Concurrent.getLangSubWords("Present", "Present"));
                statusIdentifierKeyFirst.put("0", Concurrent.getLangSubWords("Absent", "Absent"));
                statusIdentifierKeyFirst.put("2", Concurrent.getLangSubWords("Late", "Late"));
                statusIdentifierKeyFirst.put("3", Concurrent.getLangSubWords("LateExecuse", "Late with excuse"));
                statusIdentifierKeyFirst.put("4", Concurrent.getLangSubWords("earlyDismissal", "Early Dismissal"));
                statusIdentifierKeyFirst.put("9", Concurrent.getLangSubWords("acceptedVacation", "Accepted Vacation"));


                makeU0Identifier = new HashMap<>();
                makeU0Identifier.put("0", Concurrent.getLangSubWords("no", "No"));
                makeU0Identifier.put("1", Concurrent.getLangSubWords("yes", "Yes"));

                feesIdentifier = new HashMap<>();
                feesIdentifier.put("0", Concurrent.getLangSubWords("unpaid", "Unpaid"));
                feesIdentifier.put("1", Concurrent.getLangSubWords("paid", "Paid"));

                breakIdentifier = new HashMap<>();
                breakIdentifier.put("0", Concurrent.getLangSubWords("no", "No"));
                breakIdentifier.put("1", Concurrent.getLangSubWords("yes", "Yes"));



                statusIdentifier = new HashMap<>();
                statusIdentifier.put("All", Concurrent.getLangSubWords("all", "All"));
                statusIdentifier.put("1", Concurrent.getLangSubWords("Present", "Present"));
                statusIdentifier.put("0", Concurrent.getLangSubWords("Absent", "Absent"));
                statusIdentifier.put("2", Concurrent.getLangSubWords("Late", "Late"));
                statusIdentifier.put("3", Concurrent.getLangSubWords("LateExecuse", "Late with excuse"));
                statusIdentifier.put("4", Concurrent.getLangSubWords("earlyDismissal", "Early Dismissal"));
                statusIdentifier.put("9", Concurrent.getLangSubWords("acceptedVacation", "Accepted Vacation"));


                OkHttpClient client = new OkHttpClient().newBuilder(getActivity()).connectTimeout(7, TimeUnit.SECONDS).build();

                Request.Builder requestBuilder = new Request.Builder()
                        .url(App.getAppBaseUrl() + Constants.TASK_STUDENT_ATTENDANCE);

                requestBuilder.get();

                Request request = requestBuilder.build();

                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                setErrorView();
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
                        final Response responseObj = (Response) serverResponse;
                        try {
                            response = responseObj.body().string();
                            Log.e("asasa"," "+response);
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
                                            JsonArray AttendItem;
                                            JsonObject CurrObj;

                                            try {
                                                ValuesHolder = parser.parse(response).getAsJsonObject();
                                            } catch (Exception e) {
                                                showError("5001");
                                            }

                                            if (ValuesHolder != null) {
                                                GsonParser parserManager = new GsonParser();
                                                if (!Concurrent.AttendanceModelIsClass) {
                                                    subjectsItems = parserManager.objectLooper(ValuesHolder, "subjects", true);
                                                }

                                                JsonObject AttendMap = ValuesHolder.getAsJsonObject("studentAttendance");
                                                if (AttendMap != null) {
                                                    boolean thereData = false;
                                                    for (Map.Entry<String, JsonElement> entry : AttendMap.entrySet()) {
                                                        AttendItem = entry.getValue().getAsJsonObject().get("d").getAsJsonArray();

                                                        JsonObject StudentData = entry.getValue().getAsJsonObject().get("n").getAsJsonObject();

                                                        // check if the "studentRollId" is not empty.
                                                        if(!Concurrent.tagsStringValidator(StudentData, "studentRollId").equals("")) {
                                                            AttendListAdapter.add(new ParentsAttendModel(Concurrent.tagsStringValidator(StudentData, "name"), Concurrent.tagsStringValidator(StudentData, "studentRollId"), SECTION_FLAG));
                                                        }
                                                        if (AttendItem != null) {

                                                            for (int a=AttendItem.size()-1;a>=0;a--){

                                                            /*}
                                                            for (JsonElement aAttendItem : AttendItem) {
                                                            */

                                                            CurrObj = AttendItem.get(a).getAsJsonObject();
                                                                String subjectId = Concurrent.tagsStringValidator(CurrObj, "subject");
                                                                String statusId = Concurrent.tagsStringValidator(CurrObj, "status");
                                                                String makeUpId = Concurrent.tagsStringValidator(CurrObj, "makeup");
                                                                String feesId = Concurrent.tagsStringValidator(CurrObj, "fee_paid");
                                                                String breakId = Concurrent.tagsStringValidator(CurrObj, "break");

                                                                String cert = Concurrent.tagsStringValidator(CurrObj, "cert");
                                                                String book = Concurrent.tagsStringValidator(CurrObj, "book");
                                                                String note = Concurrent.tagsStringValidator(CurrObj, "note");
                                                                String speed_writing = Concurrent.tagsStringValidator(CurrObj, "speed_writing");
                                                                String balance_lesson = Concurrent.tagsStringValidator(CurrObj, "balanceLesson");
                                                                if (!Concurrent.AttendanceModelIsClass) {
                                                                    AttendListAdapter.add(new ParentsAttendModel(subjectId, Concurrent.tagsStringValidator(CurrObj, "date"), statusId, subjectId, statusIdentifier.get(statusId), feesIdentifier.get(feesId), makeU0Identifier.get(makeUpId), breakIdentifier.get(breakId),cert,note,book,speed_writing, ITEM_FLAG, balance_lesson));
                                                                    thereData = true;
                                                                } else {
                                                                    AttendListAdapter.add(new ParentsAttendModel(subjectId, Concurrent.tagsStringValidator(CurrObj, "date"), statusId, null, statusIdentifier.get(statusId), feesIdentifier.get(feesId), makeU0Identifier.get(makeUpId), breakIdentifier.get(breakId),cert,note,book,speed_writing, ITEM_FLAG,balance_lesson));
                                                                    thereData = true;
                                                                }
                                                            }
                                                        }
                                                    }
                                                    if(thereData){
                                                        setDataView();
                                                    }else{
                                                        setNoDataView();
                                                    }
                                                }else{
                                                    setNoDataView();
                                                }
                                            }else{
                                                setErrorView();
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
            }else{
                setErrorView();
            }
        }else{
            setErrorView();
        }
        ViewList = (PinnedSectionListView) view.findViewById(Res_PageList);

        if (AttendListAdapter != null)
        {

            ViewList.setAdapter(AttendListAdapter);
        }

        ViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        ViewList.setEmptyView(view.findViewById(R.id.empty));
    }


    private class ParentsAttendAdapter extends ArrayAdapter<ParentsAttendModel> implements PinnedSectionListView.PinnedSectionListAdapter {


        ParentsAttendAdapter(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ParentsAttendModel posValues = getItem(position);
            if (posValues != null) {
                if (posValues.typeInDisplayList == SECTION_FLAG) {
                    LinearLayout view = (LinearLayout) super.getView(position, convertView, parent);
                    ((TextView)view.findViewById(R.id.text1)).setText(posValues.StudentName);
                    return view;
                } else {
                    LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
                    if (convertView == null || convertView.getTag() == null) {
                        holder = new ParentsAttendHolder();
                        convertView = inflater.inflate(Res_PageItemList, null);

                        holder.Date = (ParentStyledTextView) convertView.findViewById(R.id.header_title);
                        holder.SubjectData = (ParentStyledTextView) convertView.findViewById(R.id.footer_subject_data);
                        holder.Status = (ParentStyledTextView) convertView.findViewById(R.id.footer_status_data);
                        holder.Fees = (ParentStyledTextView) convertView.findViewById(R.id.footer_Fees_data);
                        holder.MakeUP= (ParentStyledTextView) convertView.findViewById(R.id.footer_makeup_data);
                        holder.Break = (ParentStyledTextView) convertView.findViewById(R.id.footer_break_data);

                        holder.Cert = (ParentStyledTextView) convertView.findViewById(R.id.footer_cert_data);
                        holder.Book = (ParentStyledTextView) convertView.findViewById(R.id.footer_book_data);
                        holder.Note = (ParentStyledTextView) convertView.findViewById(R.id.footer_note_data);
                        holder.SpeedWriting = (ParentStyledTextView) convertView.findViewById(R.id.footer_speed_writing_data);
                        holder.FooterSubjectCon = (LinearLayout) convertView.findViewById(R.id.footer_subject_con);

                        holder.Attendance = (TextView) convertView.findViewById(R.id.Attendance);
                        holder.Subject = (TextView) convertView.findViewById(R.id.Subject);
                        holder.balanceLesson = (TextView) convertView.findViewById(R.id.balance_lesson);
                        convertView.setTag(holder);
                    } else {
                        holder = (ParentsAttendHolder) convertView.getTag();
                    }
                    holder.Date.setNotNullText(posValues.date);
                    if (!Concurrent.AttendanceModelIsClass) {
                        holder.SubjectData.setNotNullText(posValues.subjectName);
                    } else {
                        holder.FooterSubjectCon.setVisibility(View.GONE);
                    }
                    holder.Status.setNotNullText(posValues.statusName);
                    holder.Fees.setNotNullText(posValues.feesName);
                    holder.Break.setNotNullText(posValues.breakName);
                    holder.MakeUP.setNotNullText(posValues.makeUpName);


                    holder.Cert.setNotNullText(posValues.cert);
                    holder.Book.setNotNullText(posValues.book);
                    holder.Note.setNotNullText(posValues.note);
                    holder.SpeedWriting.setNotNullText(posValues.speed_writing);

                    if (!Concurrent.AttendanceModelIsClass) {
                        Concurrent.setLangWords(getActivity(), holder.Attendance, holder.Subject);
                    } else {
                        Concurrent.setLangWords(getActivity(), holder.Attendance);
                    }
                    holder.balanceLesson.setText(posValues.balance);
                    return convertView;
                }
            }
            return convertView;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).typeInDisplayList;
        }

        @Override
        public boolean isItemViewTypePinned(int viewType) {
            return viewType == SECTION_FLAG;
        }

    }

    public void setNoDataView() {
        if(getView() != null){
            getView().findViewById(R.id.student_attend_view_list).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.error_view).setVisibility(View.INVISIBLE);
        }
    }

    public void setDataView() {
        if(getView() != null){
            getView().findViewById(R.id.student_attend_view_list).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.empty_view).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.error_view).setVisibility(View.INVISIBLE);
        }
    }

    public void setErrorView() {
        if(getView() != null){
            getView().findViewById(R.id.student_attend_view_list).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.empty_view).setVisibility(View.INVISIBLE);
            getView().findViewById(R.id.error_view).setVisibility(View.VISIBLE);
        }
    }

    class ParentsAttendHolder {

        ParentStyledTextView SubjectData;
        ParentStyledTextView Status;
        ParentStyledTextView Fees;
        ParentStyledTextView MakeUP;
        ParentStyledTextView Break;
        ParentStyledTextView Date;



        ParentStyledTextView Cert;
        ParentStyledTextView Book;
        ParentStyledTextView Note;
        ParentStyledTextView SpeedWriting;

        LinearLayout FooterSubjectCon;

        TextView Subject;
        TextView Attendance;
        TextView balanceLesson;
    }
}
