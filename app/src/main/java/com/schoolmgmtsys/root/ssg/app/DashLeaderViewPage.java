package com.schoolmgmtsys.root.ssg.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.schoolmgmtsys.root.ssg.expanded.LeadersAdapter;
import com.schoolmgmtsys.root.ssg.expanded.Parent;
import com.schoolmgmtsys.root.ssg.models.DashLeadModel;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DashLeaderViewPage extends Fragment {


    private ArrayList<DashLeadModel> StudentsArrayList;
    private ArrayList<DashLeadModel> TeachersArrayList;

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_dash_lead, container, false);

        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.lead_student_teach);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            StudentsArrayList = bundle.getParcelableArrayList("EXTRA_LIST_FRAG");
            TeachersArrayList = bundle.getParcelableArrayList("EXTRA_LIST_FRAG_2");

            Parent studentsLeaders = new Parent(Concurrent.getLangSubWords("studentLeaderboard", "Students Leaderboard"), StudentsArrayList);
            Parent teachersLeader = new Parent(Concurrent.getLangSubWords("teacherLeaderboard", "Teachers Leaderboard"), TeachersArrayList);

            final List<Parent> leaders = Arrays.asList(studentsLeaders, teachersLeader);

            LeadersAdapter mAdapter = new LeadersAdapter(getActivity(), leaders);

            mRecyclerView.setHasFixedSize(false);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(linearLayoutManager);

            mRecyclerView.setAdapter(mAdapter);
            mAdapter.expandAllParents();
        }


        return view;
    }
}
