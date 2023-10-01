package com.schoolmgmtsys.root.ssg.app;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.schoolmgmtsys.root.ssg.models.StudentAttendanceModel;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.DrawerListFragment;
import com.solutionsbricks.solbricksframework.Call;
import com.solutionsbricks.solbricksframework.Callback;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.Request;
import com.solutionsbricks.solbricksframework.Response;
import com.solutionsbricks.solbricksframework.helpers.ListManager;
import com.schoolmgmtsys.root.ssg.fonts.ParentStyledTextView;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static android.view.View.INVISIBLE;

public class StudentAttendancePage extends SlidingFragmentActivity implements ListManager.ListInterface {

    public static HashMap<String, String> statusIdentifierKeyFirst;
    private static com.orhanobut.dialogplus.ListHolder ListHolder;
    private ListView ViewList;
    private ListManager mListManager;
    private AttendanceStatHolder holder;
    private StudentAttendanceModel posValues;
    private Integer Res_PageLayout = R.layout.page_student_attendance;
    private Integer Res_PageList = R.id.attendance_view_list;
    private Integer Res_PageItemList = R.layout.page_student_attendance_item;
    private ProgressBar mProgressBar;
    private ArrayList<StudentAttendanceModel> attendanceList = new ArrayList<>();
    private String response;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Concurrent.getLangDirection(this).equals("ar"))
            getSlidingMenu().setMode(SlidingMenu.RIGHT);
        else getSlidingMenu().setMode(SlidingMenu.LEFT);

        setContentView(Res_PageLayout);

        if (attendanceList != null) {

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

            statusIdentifierKeyFirst = new HashMap<>();
            statusIdentifierKeyFirst.put("All", Concurrent.getLangSubWords("all","All"));
            statusIdentifierKeyFirst.put("1", Concurrent.getLangSubWords("Present","Present"));
            statusIdentifierKeyFirst.put("0", Concurrent.getLangSubWords("Absent","Absent"));
            statusIdentifierKeyFirst.put("2", Concurrent.getLangSubWords("Late","Late"));
            statusIdentifierKeyFirst.put("3", Concurrent.getLangSubWords("LateExecuse","Late with excuse"));
            statusIdentifierKeyFirst.put("4", Concurrent.getLangSubWords("earlyDismissal","Early Dismissal"));
            statusIdentifierKeyFirst.put("9", Concurrent.getLangSubWords("acceptedVacation","Accepted Vacation"));

            mProgressBar = (ProgressBar) findViewById(R.id.gen_loader);

            TextView HeadTitle = (TextView) findViewById(R.id.head_drawer_title);
            HeadTitle.setText(Concurrent.getLangSubWords("studentAttendance", "Student Attendance"));

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


            mListManager = new ListManager(this, ViewList, this, attendanceList);
            mListManager.removeFooter();

            String TOKEN = Concurrent.getAppToken(this);
            if (TOKEN != null) {
                mProgressBar.setVisibility(View.VISIBLE);

                OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();

/*
                Request.Builder requestBuilder = new Request.Builder()
                        .url(App.getAppBaseUrl() + Constants.TASK_STUDENT_ATTENDANCE);
*/

                Request.Builder requestBuilder = new Request.Builder()
                        .url("https://rootssg.schoolmgmtsys.com/upload/attendance/stats");

                requestBuilder.get();

                Request request = requestBuilder.build();


                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                mListManager.setErrorView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                mProgressBar.setVisibility(INVISIBLE);
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
                    public void onResponse(Call call, final Object serverResponse) {
                        final Response responseObj = (Response)serverResponse;
                        try {
                            response = responseObj.body().string();
                        } catch (Exception e) {
                            showError("5001");
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
                                                showError("5001");
                                            }

                                            if (ValuesHolder != null) {
                                                JsonArray ValuesArray = ValuesHolder.getAsJsonArray("studentAttendance");
                                                if (ValuesArray != null && ValuesArray.size() > 0) {
                                                    for (JsonElement aValuesArray : ValuesArray) {
                                                        JsonObject objResult = aValuesArray.getAsJsonObject();
                                                        if (!Concurrent.AttendanceModelIsClass) {
                                                            attendanceList.add(new StudentAttendanceModel(Concurrent.repairJsonValueQuotes(objResult.get("date").toString()), Concurrent.repairJsonValueQuotes(objResult.get("status").toString()), Concurrent.repairJsonValueQuotes(objResult.get("subject").toString())));
                                                        } else {
                                                            attendanceList.add(new StudentAttendanceModel(Concurrent.repairJsonValueQuotes(objResult.get("date").toString()), Concurrent.repairJsonValueQuotes(objResult.get("status").toString()), null));
                                                        }
                                                    }

                                                } else {
                                                    mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
                                                }
                                                mListManager.getListAdapter().notifyDataSetChanged();
                                            } else {
                                                mListManager.setNoDataView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
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
    }
    public void showError(String errorCode) {
        String errorTitle;
        if (errorCode != null) {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: " + errorCode + " )";
        } else {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred");
        }
        mListManager.setErrorView(findViewById(R.id.error_view), findViewById(R.id.empty_view), (ListView) findViewById(Res_PageList));
        mProgressBar.setVisibility(INVISIBLE);
        Toast.makeText(getBaseContext(), errorTitle, Toast.LENGTH_LONG).show();
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
            holder = new AttendanceStatHolder();
            convertView = inflater.inflate(Res_PageItemList, null);

            holder.Date = (ParentStyledTextView) convertView.findViewById(R.id.date_data);
            holder.Subject = (ParentStyledTextView) convertView.findViewById(R.id.subject_data);
            holder.Status = (ParentStyledTextView) convertView.findViewById(R.id.attend_data);
            holder.GradeCon = (LinearLayout) convertView.findViewById(R.id.grade_con);

            holder.AttendanceWord = (TextView) convertView.findViewById(R.id.Attendance);
            holder.SubjectWord = (TextView) convertView.findViewById(R.id.Subject);


            convertView.setTag(holder);
        } else {
            holder = (AttendanceStatHolder) convertView.getTag();
        }

        posValues = attendanceList.get(position);
        if (posValues != null) {
            holder.Date.setNotNullText(posValues.Date);
            if (!Concurrent.AttendanceModelIsClass) {
                holder.Subject.setNotNullText(posValues.Subject);
            } else {
                holder.GradeCon.setVisibility(View.GONE);
            }
            holder.Status.setNotNullText(posValues.Status);

            Concurrent.setLangWords(this, holder.AttendanceWord, holder.SubjectWord);
        }

        return convertView;
    }

    class AttendanceStatHolder {
        TextView AttendanceWord;
        TextView SubjectWord;
        LinearLayout GradeCon;
        ParentStyledTextView Date;
        ParentStyledTextView Subject;
        ParentStyledTextView Status;
    }

}
