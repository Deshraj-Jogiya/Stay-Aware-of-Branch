package com.schoolmgmtsys.root.ssg.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.models.DashLeadModel;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.solutionsbricks.solbricksframework.helpers.CustomImageView;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by SolutionsBricks Mobile Dev. Team.
 */

public class DashboardLeaderBoardFrag extends Fragment {

    public static String dashboardJsonDataArg ="dashboard_json_data";
    private JsonObject dashboardJsonData;
    private ArrayList<DashLeadModel> StudentsLeadersList = new ArrayList<>();
    private ArrayList<DashLeadModel> TeachersLeadersList = new ArrayList<>();
    private View parentView;

    public static DashboardLeaderBoardFrag getInstance(JsonObject mJsonData) {
        DashboardLeaderBoardFrag frag = new DashboardLeaderBoardFrag();
        Bundle mBundle = new Bundle();
        mBundle.putString(dashboardJsonDataArg,new Gson().toJson(mJsonData));
        frag.setArguments(mBundle);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String dashboardJsonDataString = getArguments().getString(dashboardJsonDataArg);
        dashboardJsonData = new Gson().fromJson(dashboardJsonDataString,JsonObject.class);

        JsonArray StLeaderArray = Concurrent.getJsonArray(dashboardJsonData, "studentLeaderBoard");
        JsonArray TeachLeaderArray = Concurrent.getJsonArray(dashboardJsonData, "teacherLeaderBoard");

        JsonObject CurrObj;
        if (StLeaderArray != null) {
            Iterator<JsonElement> ValsIter = StLeaderArray.iterator();
            while (ValsIter.hasNext()) {
                CurrObj = ValsIter.next().getAsJsonObject();
                StudentsLeadersList.add(new DashLeadModel(Concurrent.tagsIntValidator(CurrObj, "id"), Concurrent.tagsStringValidator(CurrObj, "fullName"), Concurrent.tagsStringValidator(CurrObj, "isLeaderBoard")));
            }
        }

        if (TeachLeaderArray != null) {
            Iterator<JsonElement> ValsIter = TeachLeaderArray.iterator();
            while (ValsIter.hasNext()) {
                CurrObj = ValsIter.next().getAsJsonObject();
                TeachersLeadersList.add(new DashLeadModel(Concurrent.tagsIntValidator(CurrObj, "id"), Concurrent.tagsStringValidator(CurrObj, "fullName"), Concurrent.tagsStringValidator(CurrObj, "isLeaderBoard")));
            }
        }

    }


    public void noStudentsLeaderboard(){
        if(parentView != null){
            parentView.findViewById(R.id.no_sleader_con).setVisibility(View.VISIBLE);
            parentView.findViewById(R.id.sleader_con).setVisibility(View.GONE);
        }
    }

    public void noTeachersLeaderboard(){
        if(parentView != null){
            parentView.findViewById(R.id.no_tleader_con).setVisibility(View.VISIBLE);
            parentView.findViewById(R.id.tleader_con).setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.dashboard_leaderboard_frag, null);

        //============================= Set students LeaderBoard ==================//
        View sLeadCon;
        if(StudentsLeadersList.size() > 0){
            // Set first item
            sLeadCon = parentView.findViewById(R.id.student_lead_1_con);
            DashLeadModel sItem = StudentsLeadersList.get(0);
            sLeadCon.setVisibility(View.VISIBLE);
            ((TextView)parentView.findViewById(R.id.student_lead_1_text)).setText(sItem.name);
            ((TextView)parentView.findViewById(R.id.student_lead_1_msg)).setText(sItem.msg);

            ((CustomImageView)parentView.findViewById(R.id.student_lead_1_img)).profileID = String.valueOf(sItem.id);
            ((CustomImageView)parentView.findViewById(R.id.student_lead_1_img)).load();

            // Set second item
            if(StudentsLeadersList.size() > 1){
                sLeadCon = parentView.findViewById(R.id.student_lead_2_con);
                sItem = StudentsLeadersList.get(1);
                sLeadCon.setVisibility(View.VISIBLE);
                ((TextView)parentView.findViewById(R.id.student_lead_2_text)).setText(sItem.name);
                ((TextView)parentView.findViewById(R.id.student_lead_2_msg)).setText(sItem.msg);

                ((CustomImageView)parentView.findViewById(R.id.student_lead_2_img)).profileID = String.valueOf(sItem.id);
                ((CustomImageView)parentView.findViewById(R.id.student_lead_2_img)).load();

            }

            // Set third item
            if(StudentsLeadersList.size() > 2){
                sLeadCon = parentView.findViewById(R.id.student_lead_3_con);
                sItem = StudentsLeadersList.get(2);
                sLeadCon.setVisibility(View.VISIBLE);
                ((TextView)parentView.findViewById(R.id.student_lead_3_text)).setText(sItem.name);
                ((TextView)parentView.findViewById(R.id.student_lead_3_msg)).setText(sItem.msg);
                ((CustomImageView)parentView.findViewById(R.id.student_lead_3_img)).profileID = String.valueOf(sItem.id);
                ((CustomImageView)parentView.findViewById(R.id.student_lead_3_img)).load();


            }

            // Set more items
            if(StudentsLeadersList.size() > 3) {
                parentView.findViewById(R.id.students_see_more_con).setVisibility(View.VISIBLE);
                parentView.findViewById(R.id.students_see_more_con).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent MyIntent = new Intent(getContext(), ControlActivity.class);
                        MyIntent.putExtra("TARGET_FRAGMENT", "DashLeaderViewPage");
                        MyIntent.putExtra("EXTRA_HEAD_FIND_WORD", "leaderboard");
                        MyIntent.putExtra("EXTRA_HEAD_REPLACE_WORD", "leaderBoard");
                        MyIntent.putExtra("EXTRA_LIST", StudentsLeadersList);
                        MyIntent.putExtra("EXTRA_LIST2", TeachersLeadersList);
                        startActivity(MyIntent);
                        getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    }
                });
            }
        }else{
            noStudentsLeaderboard();
        }

        //============================= Set teachers LeaderBoard ==================//
        if(TeachersLeadersList.size() > 0){
            // Set first item
            sLeadCon = parentView.findViewById(R.id.employees_lead_1_con);
            DashLeadModel tItem = TeachersLeadersList.get(0);
            sLeadCon.setVisibility(View.VISIBLE);
            ((TextView)parentView.findViewById(R.id.employees_lead_1_text)).setText(tItem.name);
            ((TextView)parentView.findViewById(R.id.employees_lead_1_msg)).setText(tItem.msg);

            ((CustomImageView)parentView.findViewById(R.id.employees_lead_1_img)).profileID = String.valueOf(tItem.id);
            ((CustomImageView)parentView.findViewById(R.id.employees_lead_1_img)).load();

            // Set second item
            if(TeachersLeadersList.size() > 1){
                sLeadCon = parentView.findViewById(R.id.employees_lead_2_con);
                tItem = TeachersLeadersList.get(1);
                sLeadCon.setVisibility(View.VISIBLE);
                ((TextView)parentView.findViewById(R.id.employees_lead_2_text)).setText(tItem.name);
                ((TextView)parentView.findViewById(R.id.employees_lead_2_msg)).setText(tItem.msg);
                ((CustomImageView)parentView.findViewById(R.id.employees_lead_2_img)).profileID = String.valueOf(tItem.id);
                ((CustomImageView)parentView.findViewById(R.id.employees_lead_2_img)).load();
            }

            // Set third item
            if(TeachersLeadersList.size() > 2){
                sLeadCon = parentView.findViewById(R.id.employees_lead_3_con);
                tItem = TeachersLeadersList.get(2);
                sLeadCon.setVisibility(View.VISIBLE);
                ((TextView)parentView.findViewById(R.id.employees_lead_3_text)).setText(tItem.name);
                ((TextView)parentView.findViewById(R.id.employees_lead_3_msg)).setText(tItem.msg);
                ((CustomImageView)parentView.findViewById(R.id.employees_lead_3_img)).profileID = String.valueOf(tItem.id);
                ((CustomImageView)parentView.findViewById(R.id.employees_lead_3_img)).load();
            }

            // Set more items
            if(TeachersLeadersList.size() > 3) {
                parentView.findViewById(R.id.employees_see_more_con).setVisibility(View.VISIBLE);
                parentView.findViewById(R.id.employees_see_more_con).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent MyIntent = new Intent(getContext(), ControlActivity.class);
                        MyIntent.putExtra("TARGET_FRAGMENT", "DashLeaderViewPage");
                        MyIntent.putExtra("EXTRA_HEAD_FIND_WORD", "leaderboard");
                        MyIntent.putExtra("EXTRA_HEAD_REPLACE_WORD", "leaderBoard");
                        MyIntent.putExtra("EXTRA_LIST", StudentsLeadersList);
                        MyIntent.putExtra("EXTRA_LIST2", TeachersLeadersList);
                        startActivity(MyIntent);
                        getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    }
                });
            }
        }else{
            noTeachersLeaderboard();
        }

        return parentView;
    }
}