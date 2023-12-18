package com.schoolmgmtsys.root.ssg.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.schoolmgmtsys.root.ssg.models.AssignAnswerModel;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.schoolmgmtsys.root.ssg.utils.Downloader;
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


public class AssignmentViewAnswers extends Fragment implements ListManager.ListInterface {

    private ListView ViewList;
    private ListManager mListManager;
    private StudentAttendHolder holder;

    private AssignAnswerModel posValues;
    private Integer Res_PageLayout = R.layout.page_assign_answers;
    private Integer Res_PageList = R.id.assign_answers_view_list;
    private Integer Res_PageItemList = R.layout.page_assign_answers_item;
    private String TOKEN;
    private ArrayList<AssignAnswerModel> answersItems = new ArrayList<>();
    private int ASSIGN_ID;
    private View errorView;
    private View emptyView;
    private ProgressBar mProgressBar;
    private String response;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            ASSIGN_ID = bundle.getInt("EXTRA_INT_1_FRAG");
        }


    }

    public void loadData() {
        TOKEN = Concurrent.getAppToken(getActivity());
        if (TOKEN != null) {
            mProgressBar.setVisibility(View.VISIBLE);

            OkHttpClient client = new OkHttpClient().newBuilder(getActivity()).connectTimeout(7, TimeUnit.SECONDS).build();

            Request.Builder requestBuilder = new Request.Builder()
                    .url(App.getAppBaseUrl() + Constants.TASK_GET_ASSIGN_ANSWERS + "/" + ASSIGN_ID);

            requestBuilder.get();

            Request request = requestBuilder.build();


            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            mListManager.setErrorView(errorView, emptyView, ViewList);
                            mProgressBar.setVisibility(View.INVISIBLE);
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

                                        JsonArray ValuesArray = null;
                                        try {
                                            ValuesArray = parser.parse(response).getAsJsonArray();
                                        } catch (Exception e) {
                                            Toast.makeText(getActivity(), Concurrent.getLangSubWords("errorOccurred","Error Occurred"), Toast.LENGTH_LONG).show();

                                        }
                                        if (ValuesArray != null){
                                            if (ValuesArray.size() == 0) {
                                                mListManager.setNoDataView(errorView, emptyView, ViewList);
                                                mProgressBar.setVisibility(View.INVISIBLE);
                                                return;
                                            }
                                            for (JsonElement aValuesArray : ValuesArray) {
                                                JsonObject CurrObj = aValuesArray.getAsJsonObject();
                                                String FullName = Concurrent.tagsStringValidator(CurrObj, "fullName");
                                                answersItems.add(new AssignAnswerModel(Concurrent.tagsIntValidator(CurrObj, "id"), Concurrent.tagsStringValidator(CurrObj, "userNotes"), Concurrent.tagsStringValidator(CurrObj, "userTime"), FullName, Concurrent.tagsStringValidator(CurrObj, "className"), Concurrent.tagsIntValidator(CurrObj, "userId"), Concurrent.tagsStringValidator(CurrObj, "AssignFile")));
                                            }
                                            mListManager.getListAdapter().notifyDataSetChanged();
                                        }else{
                                            mListManager.setErrorView(errorView, emptyView, ViewList);
                                        }
                                        mProgressBar.setVisibility(View.INVISIBLE);
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
    public void showError(String errorCode) {
        String errorTitle;
        if (errorCode != null) {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: " + errorCode + " )";
        } else {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred");
        }
        mListManager.setErrorView(errorView, emptyView, ViewList);
        mProgressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(getContext(), errorTitle, Toast.LENGTH_LONG).show();
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

        errorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });

        ViewList = (ListView) view.findViewById(Res_PageList);

        ViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        mListManager = new ListManager(getActivity(), ViewList, this, answersItems);
        mListManager.removeFooter();
        ViewList.setEmptyView(view.findViewById(R.id.empty));
        loadData();
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

            holder.StudentName = (ParentStyledTextView) convertView.findViewById(R.id.header_student_data);
            holder.Class = (ParentStyledTextView) convertView.findViewById(R.id.footer_class_data);
            holder.image = (CustomImageView) convertView.findViewById(R.id.header_student_img);
            holder.TimeApplied = (ParentStyledTextView) convertView.findViewById(R.id.footer_date_data);
            holder.NotesData = (ParentStyledTextView) convertView.findViewById(R.id.footer_notes_data);
            holder.MENU_DOWNLOAD = (RelativeLayout) convertView.findViewById(R.id.menu_download);

            holder._class = (TextView) convertView.findViewById(R.id._class);
            holder.Notes = (TextView) convertView.findViewById(R.id.Notes);
            holder.timeApplied = (TextView) convertView.findViewById(R.id.timeApplied);
            holder.student = (TextView) convertView.findViewById(R.id.student);

            convertView.setTag(holder);
        } else {
            holder = (StudentAttendHolder) convertView.getTag();
        }
        posValues = answersItems.get(position);
        if (posValues != null) {
            holder.StudentName.setNotNullText(posValues.FullName);
            holder.Class.setNotNullText(posValues.Class);
            holder.TimeApplied.setNotNullText(posValues.Time);
            holder.NotesData.setNotNullText(posValues.Notes);
            holder.image.profileID = String.valueOf(posValues.id);
            holder.image.load();
            if (posValues.AnswerFile != null) holder.MENU_DOWNLOAD.setTag(posValues);
            else holder.MENU_DOWNLOAD.setVisibility(View.GONE);

            Concurrent.setLangWords(getActivity(), holder._class, holder.Notes, holder.timeApplied, holder.student);
        }
        if (posValues.AnswerFile != null) {
            holder.MENU_DOWNLOAD.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Integer itemID = ((AssignAnswerModel) (v.getTag())).id;
                    String itemFILE = ((AssignAnswerModel) (v.getTag())).AnswerFile;
                    new Downloader().downloadFile(getActivity(), itemFILE, App.getAppBaseUrl() + Constants.TASK_DOWNLOAD_ASSIGN_ANSWERS + "/" + itemID);
                }
            });
        }
        return convertView;
    }

    class StudentAttendHolder {
        TextView Notes;
        TextView _class;
        TextView timeApplied;
        TextView student;
        ParentStyledTextView StudentName;
        ParentStyledTextView Class;
        ParentStyledTextView TimeApplied;
        RelativeLayout MENU_DOWNLOAD;
        CustomImageView image;
        ParentStyledTextView NotesData;
    }
}
