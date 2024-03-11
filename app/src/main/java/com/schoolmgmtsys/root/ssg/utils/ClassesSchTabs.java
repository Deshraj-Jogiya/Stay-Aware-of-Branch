package com.schoolmgmtsys.root.ssg.utils;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.schoolmgmtsys.root.ssg.fonts.MediumStyledTextView;
import com.schoolmgmtsys.root.ssg.models.ClassesSchModel;
import com.schoolmgmtsys.root.ssg.R;
import com.solutionsbricks.solbricksframework.helpers.ListManager;

import java.util.ArrayList;


public class ClassesSchTabs extends Fragment implements ListManager.ListInterface {
    private ArrayList<ClassesSchModel> schArrayList;

    private ListView ViewList;
    private ListManager mListManager;
    private ClassesSchHolder holder;

    private ClassesSchModel posValues;
    private Integer Res_PageLayout = R.layout.page_classes_sch_adap;
    private Integer Res_PageList = R.id.classes_sch_view_list;
    private Integer Res_PageItemList = R.layout.page_classes_sch_adap_item;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //int position = FragmentPagerItem.getPosition(getArguments());
        //mAdapter = new RadioProgsAdapter(getActivity(),position) {};
        Bundle extras = getArguments();
        if (extras != null) {
            schArrayList = extras.getParcelableArrayList("schArrayList");
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(Res_PageLayout, container, false);

        ViewList = (ListView) view.findViewById(Res_PageList);

        ViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        mListManager = new ListManager(getActivity(), ViewList, this, schArrayList);
        mListManager.removeFooter();
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

        if (schArrayList != null) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            if (convertView == null || convertView.getTag() == null) {
                holder = new ClassesSchHolder();
                convertView = inflater.inflate(Res_PageItemList, null);

                holder.Title = (MediumStyledTextView) convertView.findViewById(R.id.header_title);
                holder.StartTime = (MediumStyledTextView) convertView.findViewById(R.id.start_time_data);
                holder.EndTime = (MediumStyledTextView) convertView.findViewById(R.id.end_time_data);

                holder.StartTime_txt = (TextView) convertView.findViewById(R.id.startTime);
                holder.EndTime_txt = (TextView) convertView.findViewById(R.id.endTime);

                convertView.setTag(holder);
            } else {
                holder = (ClassesSchHolder) convertView.getTag();
            }
            posValues = schArrayList.get(position);
            holder.Title.setNotNullText(posValues.subjectId);
            holder.StartTime.setNotNullText(posValues.startPeriod);
            holder.EndTime.setNotNullText(posValues.endPeriod);

            Concurrent.setLangWords(getActivity(), holder.StartTime_txt, holder.EndTime_txt);

            return convertView;
        } else {
            return null;
        }

    }

    class ClassesSchHolder {
        MediumStyledTextView Title;
        MediumStyledTextView StartTime;
        MediumStyledTextView EndTime;

        public TextView StartTime_txt;
        public TextView EndTime_txt;
    }
}
