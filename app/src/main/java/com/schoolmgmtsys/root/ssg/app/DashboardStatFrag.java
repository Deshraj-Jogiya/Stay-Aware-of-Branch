package com.schoolmgmtsys.root.ssg.app;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daasuu.cat.CountAnimationTextView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.liulishuo.magicprogresswidget.MagicProgressBar;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.messages.MessagesDialogsActivity;
import com.schoolmgmtsys.root.ssg.models.DashPollItemModel;
import com.schoolmgmtsys.root.ssg.models.DashPollModel;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.solutionsbricks.solbricksframework.Call;
import com.solutionsbricks.solbricksframework.Callback;
import com.solutionsbricks.solbricksframework.FormBody;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.Request;
import com.solutionsbricks.solbricksframework.Response;
import com.solutionsbricks.solbricksframework.helpers.CircularTextView;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Created by SolutionsBricks Mobile Dev. Team.
 */

public class DashboardStatFrag extends Fragment {


    public static String dashboardJsonDataArg = "dashboard_json_data";
    private JsonObject dashboardJsonData;
    private int mClasses;
    private int mStudents;
    private int mTeachers;
    private int mAssign;
    private JsonObject Polls;
    private JsonArray pollsItemsArray;
    private DashPollModel pollObject;
    private View parentView;
    private boolean PollVoteActivated = true;
    private String response;

    public static DashboardStatFrag getInstance(JsonObject mJsonData) {
        DashboardStatFrag frag = new DashboardStatFrag();
        Bundle mBundle = new Bundle();
        mBundle.putString(dashboardJsonDataArg, new Gson().toJson(mJsonData));
        frag.setArguments(mBundle);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String dashboardJsonDataString = getArguments().getString(dashboardJsonDataArg);
        dashboardJsonData = new Gson().fromJson(dashboardJsonDataString, JsonObject.class);

        JsonObject Stats = Concurrent.getJsonObject(dashboardJsonData, "stats");
        mClasses = Concurrent.tagsIntValidator(Stats, "classes");
        mStudents = Concurrent.tagsIntValidator(Stats, "students");
        mTeachers = Concurrent.tagsIntValidator(Stats, "teachers");

        JsonObject pollsJson = Concurrent.getJsonObject(dashboardJsonData, "polls");

        if (pollsJson != null) {
            pollsItemsArray = (JsonArray) pollsJson.get("items");
            pollObject = new DashPollModel(
                    Concurrent.tagsStringValidator(pollsJson, "id"),
                    Concurrent.tagsStringValidator(pollsJson, "title"),
                    Concurrent.tagsStringValidator(pollsJson, "view"),
                    Concurrent.tagsStringValidator(pollsJson, "voted"),
                    Concurrent.tagsStringValidator(pollsJson, "totalCount"),
                    pollsItemsArray);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.dashboard_stat_frag, null);
        LinearLayout statCon = (LinearLayout) parentView.findViewById(R.id.stat_con);

        if (Concurrent.isUserHavePermission(getContext(), "dashboard.stats")) {
            statCon.setVisibility(View.VISIBLE);







            //============= Set stats data ============================//
            CircularTextView statAttendanceCount = (CircularTextView) parentView.findViewById(R.id.stat_attendance_data);
            statAttendanceCount.solidColor = Concurrent.getColor(getContext(),R.color.x_dash_side_button_1_back_normal);
            statAttendanceCount.setText(String.valueOf(mStudents));
            statAttendanceCount.setVisibility(View.GONE);
            if (Concurrent.isUserHavePermission(getContext(), "Attendance.takeAttendance")) {
            if (Concurrent.isModuleActivated(getContext(), "attendanceAct")) {

                    parentView.findViewById(R.id.attendance_stat_con).setVisibility(View.VISIBLE);
                    parentView.findViewById(R.id.attendance_stat_con).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent MyIntent = new Intent(getActivity(), AttendancePage.class);
                            startActivity(MyIntent);
                            getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                        }
                    });

                }else{
                    if (Concurrent.isUserHavePermission(getContext(), "myAttendance.myAttendance","students.Attendance") ) {
                        if (Concurrent.getAppRole(getContext()) == Concurrent.APP_ROLE_STUDENT) {

                            parentView.findViewById(R.id.attendance_stat_con).setVisibility(View.VISIBLE);
                            parentView.findViewById(R.id.attendance_stat_con).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent MyIntent = new Intent(getActivity(), StudentAttendancePage.class);
                                    startActivity(MyIntent);
                                    getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                                }
                            });


                        }else if (Concurrent.getAppRole(getContext()) == Concurrent.APP_ROLE_PARENT) {
                            parentView.findViewById(R.id.attendance_stat_con).setVisibility(View.VISIBLE);
                            parentView.findViewById(R.id.attendance_stat_con).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent MyIntent = new Intent(getActivity(), ControlActivity.class);

                                    MyIntent.putExtra("TARGET_FRAGMENT", "ParentsAttendance");
                                    MyIntent.putExtra("EXTRA_INT_1", PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("app_user_id", 0));
                                    MyIntent.putExtra("EXTRA_HEAD_FIND_WORD", "Attendance");
                                    MyIntent.putExtra("EXTRA_HEAD_REPLACE_WORD", "Attendance");

                                    startActivity(MyIntent);
                                    getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                                }
                            });

                        }
                    }
                }
            }


/*            if (Concurrent.isUserHavePermission(getContext(), "Assignments.list")) {
                if (Concurrent.isModuleActivated(getContext(), "assignmentsAct"))
                parentView.findViewById(R.id.assignment_stat_con).setVisibility(View.VISIBLE);
            }*/
            if(Concurrent.getAppRole(getContext()) == Concurrent.APP_ROLE_PARENT || Concurrent.getAppRole(getContext()) == Concurrent.APP_ROLE_STUDENT) {
                if (Concurrent.isUserHavePermission(getContext(), "Assignments.list")) {
                    if (Concurrent.isModuleActivated(getContext(), "assignmentsAct"))
                        parentView.findViewById(R.id.assignment_stat_con).setVisibility(View.VISIBLE);
                }
            }
            if (Concurrent.isUserHavePermission(getContext(), "classSch.list")) {
                if (Concurrent.isModuleActivated(getContext(), "classSchAct")) {
                    parentView.findViewById(R.id.class_sch_stat_con).setVisibility(View.VISIBLE);
                }
            }


            if (Concurrent.isUserHavePermission(getContext(), "examsList.list","examsList.View")) {
                parentView.findViewById(R.id.Exams_list_stat_con).setVisibility(View.VISIBLE);

            }

            parentView.findViewById(R.id.classes_stat_con).setVisibility(View.VISIBLE);
            if (Concurrent.isUserHavePermission(getContext(), "mediaCenter.View")) {
                if (Concurrent.isModuleActivated(getContext(), "mediaAct"))
                    parentView.findViewById(R.id.media_center_stat_con).setVisibility(View.VISIBLE);

            }


            if (Concurrent.isUserHavePermission(getContext(), "events.list","events.View")) {
                if (Concurrent.isModuleActivated(getContext(), "eventsAct"))
                    parentView.findViewById(R.id.events_stat_con).setVisibility(View.VISIBLE);

            }

            if (Concurrent.isUserHavePermission(getContext(), "teachers.list")) {
                parentView.findViewById(R.id.teachers_stat_con).setVisibility(View.VISIBLE);

            }

            if (Concurrent.getAppRole(getContext()) == Concurrent.APP_ROLE_PARENT){
                parentView.findViewById(R.id.invoices_stat_con).setVisibility(View.VISIBLE);

            }

            if (Concurrent.getAppRole(getContext()) == Concurrent.APP_ROLE_PARENT){
                parentView.findViewById(R.id.messages_stat_con).setVisibility(View.VISIBLE);

            }

          //  parentView.findViewById(R.id.classes_stat_con).setVisibility(View.VISIBLE);








            parentView.findViewById(R.id.assignment_stat_con).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent MyIntent = new Intent(getActivity(), AssignmentsPage.class);
                    startActivity(MyIntent);
                    getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            });


            //============= Set stats data ============================//









            //============= Set stats data ============================//
            CircularTextView statAssigntmentCount = (CircularTextView) parentView.findViewById(R.id.stat_assignment_data);
            statAssigntmentCount.solidColor = Concurrent.getColor(getContext(),R.color.x_dash_side_button_1_back_normal);
            statAssigntmentCount.setText(String.valueOf(mStudents));
            statAssigntmentCount.setVisibility(View.GONE);


            parentView.findViewById(R.id.assignment_stat_con).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent MyIntent = new Intent(getActivity(), AssignmentsPage.class);
                    startActivity(MyIntent);
                    getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            });


            //============= Set stats data ============================//






            //============= Set schedule data ============================//
            CircularTextView statScheduleCount = (CircularTextView) parentView.findViewById(R.id.stat_class_sch_data);
            statScheduleCount.solidColor = Concurrent.getColor(getContext(),R.color.x_dash_side_button_1_back_normal);
            statScheduleCount.setText(String.valueOf(mStudents));
            statScheduleCount.setVisibility(View.GONE);

            parentView.findViewById(R.id.class_sch_stat_con).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent MyIntent = new Intent(getActivity(), ViewImagePage.class);

                        MyIntent.putExtra("TARGET_FRAGMENT", "ClassesSchPage");
                       // MyIntent.putExtra("EXTRA_INT_1", PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("app_user_id", 0));
                        //MyIntent.putExtra("EXTRA_HEAD_FIND_WORD", "ClassSchedule");
                        //MyIntent.putExtra("EXTRA_HEAD_REPLACE_WORD", "Class Schedule");

                    startActivity(MyIntent);
                    getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            });


            //============= Set schedule data ============================//




            //============= Set exam List data ============================//
            CircularTextView statexamlistCount = (CircularTextView) parentView.findViewById(R.id.stat_Exams_list_data);
            statexamlistCount.solidColor = Concurrent.getColor(getContext(),R.color.x_dash_side_button_1_back_normal);
            statexamlistCount.setText(String.valueOf(mStudents));
            statexamlistCount.setVisibility(View.GONE);


            parentView.findViewById(R.id.Exams_list_stat_con).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent MyIntent = new Intent(getActivity(), ExamsPage.class);

                    startActivity(MyIntent);
                    getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            });


            //============= Set exam List data ============================//

  //============= Set exam List data ============================//
            CircularTextView statClassesCount = (CircularTextView) parentView.findViewById(R.id.stat_classes_data);
            statClassesCount.solidColor = Concurrent.getColor(getContext(),R.color.x_dash_side_button_1_back_normal);
            statClassesCount.setText(String.valueOf(mStudents));
            statClassesCount.setVisibility(View.GONE);


            parentView.findViewById(R.id.classes_stat_con).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent MyIntent = new Intent(getActivity(), ClassesPage.class);

                    startActivity(MyIntent);
                    getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            });


            //============= Set exam List data ============================//



            //============= Set media center data ============================//
            CircularTextView statMediaCenterCount = (CircularTextView) parentView.findViewById(R.id.stat_media_center_data);
            statMediaCenterCount.solidColor = Concurrent.getColor(getContext(),R.color.x_dash_side_button_1_back_normal);
            statMediaCenterCount.setText(String.valueOf(mStudents));
            statMediaCenterCount.setVisibility(View.GONE);


            parentView.findViewById(R.id.media_center_stat_con).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent MyIntent = new Intent(getActivity(), MediaCenterPage.class);

                    startActivity(MyIntent);
                    getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            });


            //============= Set media center data ============================//





            //============= Set event data ============================//
            CircularTextView statEvantsCount = (CircularTextView) parentView.findViewById(R.id.stat_events_data);
            statEvantsCount.solidColor = Concurrent.getColor(getContext(),R.color.x_dash_side_button_1_back_normal);
            statEvantsCount.setText(String.valueOf(mStudents));
            statEvantsCount.setVisibility(View.GONE);


            parentView.findViewById(R.id.events_stat_con).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent MyIntent = new Intent(getActivity(), EventsPage.class);

                    startActivity(MyIntent);
                    getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            });


            //============= Set event data ============================//




            CircularTextView statInvoicesCount = (CircularTextView) parentView.findViewById(R.id.stat_invoices_data);
            statInvoicesCount.solidColor = Concurrent.getColor(getContext(),R.color.x_dash_side_button_1_back_normal);
            statInvoicesCount.setText(String.valueOf(mTeachers));statInvoicesCount.setVisibility(View.GONE);

            parentView.findViewById(R.id.invoices_stat_con).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent MyIntent = new Intent(getActivity(), PaymentsPage.class);
                    startActivity(MyIntent);
                    getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            });


            CircularTextView statMessagesCount = (CircularTextView) parentView.findViewById(R.id.stat_messages_data);
            statMessagesCount.solidColor = Concurrent.getColor(getContext(),R.color.x_dash_side_button_1_back_normal);
            statMessagesCount.setText(String.valueOf(mTeachers));statMessagesCount.setVisibility(View.GONE);

            parentView.findViewById(R.id.messages_stat_con).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent MyIntent = new Intent(getActivity(), MessagesDialogsActivity.class);
                    startActivity(MyIntent);
                    getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            });




            CircularTextView statTeachersCount = (CircularTextView) parentView.findViewById(R.id.stat_teachers_data);
            statTeachersCount.solidColor = Concurrent.getColor(getContext(),R.color.x_dash_side_button_1_back_normal);
            statTeachersCount.setText(String.valueOf(mTeachers));statTeachersCount.setVisibility(View.GONE);

            parentView.findViewById(R.id.teachers_stat_con).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent MyIntent = new Intent(getActivity(), TeachersPage.class);
                    startActivity(MyIntent);
                    getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            });









        }else{
            statCon.setVisibility(View.GONE);
        }



        //============= Set polls data ============================//
        parseDashboardPolls(Polls);

        return parentView;
    }


    public void parseDashboardPolls(JsonObject pollsJsonData) {
        if (pollsJsonData != null) {
            pollsItemsArray = (JsonArray) pollsJsonData.get("items");
            pollObject = new DashPollModel(
                    Concurrent.tagsStringValidator(pollsJsonData, "id"),
                    Concurrent.tagsStringValidator(pollsJsonData, "title"),
                    Concurrent.tagsStringValidator(pollsJsonData, "view"),
                    Concurrent.tagsStringValidator(pollsJsonData, "voted"),
                    Concurrent.tagsStringValidator(pollsJsonData, "totalCount"),
                    pollsItemsArray);
        }else{
            parentView.findViewById(R.id.polls_con).setVisibility(View.GONE);
        }

        if (parentView != null && pollObject != null && pollsItemsArray != null && pollsItemsArray.size() > 0) {

            // Set title
            if (pollObject.pollTitle != null)
                ((TextView) parentView.findViewById(R.id.polls_title_text)).setText(pollObject.pollTitle);

            // Set total votes
            String totalVotes = "0";
            if (pollObject.totalCount != null) totalVotes = pollObject.totalCount;
            ((TextView) parentView.findViewById(R.id.polls_count_text)).setText(totalVotes + " " + Concurrent.getLangSubWords("totalVotes", "Total votes"));

            // Set votes items
            parentView.findViewById(R.id.polls_con).setVisibility(View.VISIBLE);

            Iterator<JsonElement> ValsIter = pollsItemsArray.iterator();
            JsonObject CurrObj;
            ArrayList<DashPollItemModel> pollsItemsList = new ArrayList<>();

            while (ValsIter.hasNext()) {
                CurrObj = ValsIter.next().getAsJsonObject();
                pollsItemsList.add(new DashPollItemModel(Concurrent.tagsStringValidator(CurrObj, "title"),
                        Concurrent.tagsStringValidator(CurrObj, "count"),
                        Concurrent.tagsStringValidator(CurrObj, "perc")
                ));
            }

            fillPollItems(pollsItemsList, ((LinearLayout) parentView.findViewById(R.id.polls_data_con)));
        }
    }


    public void fillPollItems(ArrayList DataArray, LinearLayout BoxContainer) {
        if (DataArray != null && DataArray.size() > 0) {

            if (pollObject.view.equals("vote")) {

                setPollsVoteView(DataArray, BoxContainer);

            } else if (pollObject.view.equals("results")) {

                setPollsResultsView(DataArray, BoxContainer, true);

            }
        }
    }

    public void setPollsVoteView(final ArrayList DataArray, LinearLayout BoxContainer) {
        parentView.findViewById(R.id.polls_progress).setVisibility(View.GONE);
        ((TextView) parentView.findViewById(R.id.view_results_text)).setText(Concurrent.getLangSubWords("votes", "View Votes"));

        parentView.findViewById(R.id.view_results_con).setVisibility(View.VISIBLE);
        parentView.findViewById(R.id.view_results_con).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPollsResultsView(DataArray, ((LinearLayout) parentView.findViewById(R.id.polls_data_con)), false);
            }
        });

        // remove results view
        View pollResultsContainer = BoxContainer.findViewById(R.id.poll_results_items_container);
        if (pollResultsContainer != null) pollResultsContainer.setVisibility(View.GONE);

        // show polls view
        View pollVoteContainer = BoxContainer.findViewById(R.id.poll_vote_items_container);
        if (pollVoteContainer != null) pollVoteContainer.setVisibility(View.VISIBLE);
        else {
            LinearLayout containerLayout = new LinearLayout(getContext());
            containerLayout.setId(R.id.poll_vote_items_container);
            containerLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            containerLayout.setOrientation(LinearLayout.VERTICAL);

            int ListItemLayout = R.layout.dashborad_poll_vote_item;

            for (int i = 0; i < DataArray.size(); i++) {
                DashPollItemModel mItem = (DashPollItemModel) DataArray.get(i);

                View DashBoxItem = getActivity().getLayoutInflater().inflate(ListItemLayout, null);

                DashBoxItem.setTag(mItem.title);
                DashBoxItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (PollVoteActivated) {
                            ((AppCompatRadioButton) view.findViewById(R.id.radio)).setChecked(true);
                            submitPollVote((String) view.getTag());
                        }
                    }
                });
                AppCompatRadioButton radioButton = (AppCompatRadioButton) DashBoxItem.findViewById(R.id.radio);
                radioButton.setTag(mItem.title);
                radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        submitPollVote((String) view.getTag());
                    }
                });

                if (mItem.title != null)
                    ((TextView) DashBoxItem.findViewById(R.id.title)).setText(mItem.title);
                containerLayout.addView(DashBoxItem);
            }
            BoxContainer.addView(containerLayout);

        }
    }

    public void setPollsResultsView(final ArrayList DataArray, LinearLayout BoxContainer, Boolean reDraw) {
        parentView.findViewById(R.id.polls_progress).setVisibility(View.GONE);

        if (pollObject.view.equals("vote")) {
            parentView.findViewById(R.id.view_results_con).setVisibility(View.VISIBLE);

            ((TextView) parentView.findViewById(R.id.view_results_text)).setText(Concurrent.getLangSubWords("votePoll", "Vote poll"));
            parentView.findViewById(R.id.view_results_con).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setPollsVoteView(DataArray, ((LinearLayout) parentView.findViewById(R.id.polls_data_con)));
                }
            });
        } else {
            parentView.findViewById(R.id.view_results_con).setVisibility(View.GONE);
        }

        // remove vote view
        View pollVoteContainer = BoxContainer.findViewById(R.id.poll_vote_items_container);
        if (pollVoteContainer != null) pollVoteContainer.setVisibility(View.GONE);


        // show results view
        View pollResultsContainer = BoxContainer.findViewById(R.id.poll_results_items_container);
        if (pollResultsContainer != null && !reDraw)
            pollResultsContainer.setVisibility(View.VISIBLE);
        else {
            LinearLayout containerLayout = new LinearLayout(getContext());
            containerLayout.setId(R.id.poll_results_items_container);
            containerLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            containerLayout.setOrientation(LinearLayout.VERTICAL);

            int ListItemLayout = R.layout.dashborad_poll_results_item;

            for (int i = 0; i < DataArray.size(); i++) {
                DashPollItemModel mItem = (DashPollItemModel) DataArray.get(i);
                View DashBoxItem = getActivity().getLayoutInflater().inflate(ListItemLayout, null);

                //============================= Set percent progress ============================= //
                float percentageValue = 0;
                try {
                    percentageValue = (Float.parseFloat(mItem.perc) / 100);
                } catch (Exception ignored) {
                }

                MagicProgressBar percentProgress = (MagicProgressBar) DashBoxItem.findViewById(R.id.votes_percent_progress);
                percentProgress.setSmoothPercent(percentageValue);

                //============================= Set percent text ============================= //
                CountAnimationTextView percentText = (CountAnimationTextView) DashBoxItem.findViewById(R.id.votes_percent_text);
                if (mItem.perc != null && !mItem.perc.equals(""))
                    percentText.setInterpolator(new AccelerateInterpolator()).countAnimation(0, Math.round(Float.parseFloat(mItem.perc)));

                //============================= Set title & Counts ============================= //
                if (mItem.title != null)
                    ((TextView) DashBoxItem.findViewById(R.id.title)).setText(mItem.title);

                String totalVotes = "0";
                if (mItem.count != null && !mItem.count.equals("")) totalVotes = mItem.count;
                ((TextView) DashBoxItem.findViewById(R.id.votes_count)).setText(totalVotes + " " + Concurrent.getLangSubWords("totalVotes", "Total votes"));


                //============================= Add View ============================= //
                containerLayout.addView(DashBoxItem);
            }

            BoxContainer.addView(containerLayout);
        }


    }

    public void submitPollVote(String selectedVoteString) {
        // disable voting
        PollVoteActivated = false;
        parentView.findViewById(R.id.polls_progress).setVisibility(View.VISIBLE);
        parentView.findViewById(R.id.view_results_con).setVisibility(View.GONE);

        // send vote to server
        FormBody.Builder formBody = new FormBody.Builder();
        formBody.add("id", pollObject.id);
        formBody.add("selected", selectedVoteString);


        String TOKEN = Concurrent.getAppToken(getContext());
        if (TOKEN != null) {

            OkHttpClient client = new OkHttpClient().newBuilder(getActivity()).connectTimeout(7, TimeUnit.SECONDS).build();

            Request.Builder requestBuilder = new Request.Builder()
                    .url(App.getAppBaseUrl() + Constants.TASK_POLL_POST);

            requestBuilder.post(formBody.build());

            Request request = requestBuilder.build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            if (e instanceof ConnectException) {
                                Toast.makeText(getActivity(), Concurrent.getLangSubWords("noConnection", "No Internet Connection"), Toast.LENGTH_LONG).show();
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
                                            parseDashboardPolls(ValuesHolder);
                                        } catch (Exception e) {}

                                    }
                                } catch (final Exception ignored) {
                                }
                            }
                        });
                    }
                }
            });
        }

    }

}