package com.schoolmgmtsys.root.ssg.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.marvinlabs.widget.floatinglabel.edittext.FloatingLabelEditText;
import com.schoolmgmtsys.root.ssg.messages.MessagesDialogsActivity;
import com.schoolmgmtsys.root.ssg.models.AssignmentsModel;
import com.schoolmgmtsys.root.ssg.models.ClassesModel;
import com.schoolmgmtsys.root.ssg.models.EventsModel;
import com.schoolmgmtsys.root.ssg.models.ExamsModel;
import com.schoolmgmtsys.root.ssg.models.GradesModel;
import com.schoolmgmtsys.root.ssg.models.HomeworkModel;
import com.schoolmgmtsys.root.ssg.models.HostelModel;
import com.schoolmgmtsys.root.ssg.models.MaterialModel;
import com.schoolmgmtsys.root.ssg.models.OnlineExamsModel;
import com.schoolmgmtsys.root.ssg.models.SubjectsModel;
import com.schoolmgmtsys.root.ssg.models.TransportModel;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.solutionsbricks.solbricksframework.messages.model.Dialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import me.gujun.android.taggroup.TagGroup;

public class SearchView extends Fragment implements FloatingLabelEditText.EditTextListener {


    private FloatingLabelEditText mSearchInput;
    private String mSearchInputValue;
    private String SENDER_CLASS;
    private Queue<String> tagsStack;
    private TagGroup mSearchTags;
    private Intent intent;

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_search_view, container, false);
        final Bundle bundle = this.getArguments();
        tagsStack = new LinkedList<>();

        if (bundle != null) {
            SENDER_CLASS = bundle.getString("EXTRA_STRING_1");
        }
        mSearchInput = (FloatingLabelEditText) view.findViewById(R.id.search_input);
        final ActionProcessButton mSearchBtn = (ActionProcessButton) view.findViewById(R.id.search_btn);
        mSearchTags = (TagGroup) view.findViewById(R.id.search_tags);
        mSearchInput.setEditTextListener(this);


        final String noMatchesLang = Concurrent.getLangSubWords("noMatches", "Sorry No Matches");

        switch (SENDER_CLASS) {
            case "StudentsPage":
                intent = new Intent(getActivity(), StudentsPage.class);
                break;
            case "ParentsPage":
                intent = new Intent(getActivity(), ParentsPage.class);
                break;
            case "TeachersPage":
                intent = new Intent(getActivity(), TeachersPage.class);
                break;
            case "LibraryPage":
                intent = new Intent(getActivity(), LibraryPage.class);
                break;
            case "GradesPage":
                intent = new Intent(getActivity(), GradesPage.class);
                break;
            case "StudyMaterialPage":
                intent = new Intent(getActivity(), StudyMaterialPage.class);
                break;
            case "AssignmentsPage":
                intent = new Intent(getActivity(), AssignmentsPage.class);
                break;
            case "ExamsPage":
                intent = new Intent(getActivity(), ExamsPage.class);
                break;
            case "OnlineExamsPage":
                intent = new Intent(getActivity(), OnlineExamsPage.class);
                break;
            case "NewsBoardPage":
                intent = new Intent(getActivity(), NewsBoardPage.class);
                break;
            case "PaymentsPage":
                intent = new Intent(getActivity(), PaymentsPage.class);
                break;
            case "EventsPage":
                intent = new Intent(getActivity(), EventsPage.class);
                break;
            case "ClassesPage":
                intent = new Intent(getActivity(), ClassesPage.class);
                break;
            case "SubjectsPage":
                intent = new Intent(getActivity(), SubjectsPage.class);
                break;
            case "TransportPage":
                intent = new Intent(getActivity(), TransportPage.class);
                break;
            case "HomeworkPage":
                intent = new Intent(getActivity(), HomeworkPage.class);
                break;
            case "HostelPage":
                intent = new Intent(getActivity(), HostelPage.class);
                break;
            case "MessagesPage":
                intent = new Intent(getActivity(), MessagesDialogsActivity.class);
                break;

        }
        loadArray();

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSearchInputValue != null && !mSearchInputValue.equals("")) {
                    mSearchBtn.setEnabled(false);
                    mSearchBtn.setProgress(10);

                    mSearchInputValue = mSearchInputValue.toLowerCase();

                    Integer j = 0;
                    boolean cancelAdd = false;
                    for (Iterator<String> iterator = tagsStack.iterator(); iterator.hasNext(); ) {
                        String value = iterator.next();
                        if (value != null && value.equals(mSearchInputValue)) {
                            cancelAdd = true;
                            break;
                        }
                        j++;
                    }
                    if (!cancelAdd) {
                        tagsStack.remove();
                        tagsStack.add(mSearchInputValue);
                        saveArray(tagsStack);
                    }
                    mSearchTags.setTags(getTags());

                    if (SENDER_CLASS.equals("GradesPage")) {
                        ArrayList<GradesModel> LIST_DATA = new ArrayList<>();
                        ArrayList<GradesModel> DataArrayList = bundle.getParcelableArrayList("EXTRA_LIST_FRAG");
                        for (GradesModel item : DataArrayList) {
                            if (item.GradeName.toLowerCase().contains(mSearchInputValue) || item.GradeDesc.toLowerCase().contains(mSearchInputValue) || item.GradeFrom.toLowerCase().contains(mSearchInputValue) || item.GradeTo.toLowerCase().contains(mSearchInputValue) || item.GradePoints.toLowerCase().contains(mSearchInputValue))
                                LIST_DATA.add(item);
                        }

                        if (LIST_DATA.size() == 0) {
                            Toast.makeText(getActivity(), noMatchesLang, Toast.LENGTH_LONG).show();
                            mSearchBtn.setProgress(-1);
                            mSearchBtn.setEnabled(true);
                            mSearchBtn.setText(noMatchesLang);
                            return;
                        } else {
                            intent.putExtra("EXTRA_LIST", LIST_DATA);
                        }
                    } else if (SENDER_CLASS.equals("HomeworkPage")) {
                        ArrayList<HomeworkModel> LIST_DATA = new ArrayList<>();
                        ArrayList<HomeworkModel> DataArrayList = bundle.getParcelableArrayList("EXTRA_LIST_FRAG");
                        for (HomeworkModel item : DataArrayList) {
                            if (item.homeworkTitle.toLowerCase().contains(mSearchInputValue) || item.homeworkDate.toLowerCase().contains(mSearchInputValue) || item.homeworkEvaluationDate.toLowerCase().contains(mSearchInputValue) || item.classes.toLowerCase().contains(mSearchInputValue) || item.sections.toLowerCase().contains(mSearchInputValue))
                                LIST_DATA.add(item);
                        }

                        if (LIST_DATA.size() == 0) {
                            Toast.makeText(getActivity(), noMatchesLang, Toast.LENGTH_LONG).show();
                            mSearchBtn.setProgress(-1);
                            mSearchBtn.setEnabled(true);
                            mSearchBtn.setText(noMatchesLang);
                            return;
                        } else {
                            intent.putExtra("EXTRA_LIST", LIST_DATA);
                        }
                    } else if (SENDER_CLASS.equals("StudyMaterialPage")) {
                        ArrayList<MaterialModel> LIST_DATA = new ArrayList<>();
                        ArrayList<MaterialModel> DataArrayList = bundle.getParcelableArrayList("EXTRA_LIST_FRAG");
                        for (MaterialModel item : DataArrayList) {
                            if (item.materialTitle.toLowerCase().contains(mSearchInputValue) || item.subjectTitle.toLowerCase().contains(mSearchInputValue) || item.materialDescription.toLowerCase().contains(mSearchInputValue) || item.classes.toLowerCase().contains(mSearchInputValue)) {
                                LIST_DATA.add(item);
                            }else{
                                if(item.materialFile != null){
                                    if (item.materialFile.toLowerCase().contains(mSearchInputValue)) LIST_DATA.add(item);
                                }
                            }

                        }

                        if (LIST_DATA.size() == 0) {
                            Toast.makeText(getActivity(), noMatchesLang, Toast.LENGTH_LONG).show();
                            mSearchBtn.setProgress(-1);
                            mSearchBtn.setEnabled(true);
                            mSearchBtn.setText(noMatchesLang);
                            return;
                        } else {
                            intent.putExtra("EXTRA_LIST", LIST_DATA);
                        }
                    } else if (SENDER_CLASS.equals("TransportPage")) {
                        ArrayList<TransportModel> LIST_DATA = new ArrayList<>();
                        ArrayList<TransportModel> DataArrayList = bundle.getParcelableArrayList("EXTRA_LIST_FRAG");
                        for (TransportModel item : DataArrayList) {
                            if (item.transportTitle.toLowerCase().contains(mSearchInputValue) || item.transportDescription.toLowerCase().contains(mSearchInputValue) || item.transportFare.toLowerCase().contains(mSearchInputValue))
                                LIST_DATA.add(item);
                        }

                        if (LIST_DATA.size() == 0) {
                            Toast.makeText(getActivity(), noMatchesLang, Toast.LENGTH_LONG).show();
                            mSearchBtn.setProgress(-1);
                            mSearchBtn.setEnabled(true);
                            mSearchBtn.setText(noMatchesLang);
                            return;
                        } else {
                            intent.putExtra("EXTRA_LIST", LIST_DATA);
                        }
                    } else if (SENDER_CLASS.equals("HostelPage")) {
                        ArrayList<HostelModel> LIST_DATA = new ArrayList<>();
                        ArrayList<HostelModel> DataArrayList = bundle.getParcelableArrayList("EXTRA_LIST_FRAG");
                        for (HostelModel item : DataArrayList) {
                            if (item.hostelTitle.toLowerCase().contains(mSearchInputValue) || item.hostelNotes.toLowerCase().contains(mSearchInputValue) || item.hostelManager.toLowerCase().contains(mSearchInputValue) || item.hostelAddress.toLowerCase().contains(mSearchInputValue) || item.hostelType.toLowerCase().contains(mSearchInputValue))
                                LIST_DATA.add(item);
                        }

                        if (LIST_DATA.size() == 0) {
                            Toast.makeText(getActivity(), noMatchesLang, Toast.LENGTH_LONG).show();
                            mSearchBtn.setProgress(-1);
                            mSearchBtn.setEnabled(true);
                            mSearchBtn.setText(noMatchesLang);
                            return;
                        } else {
                            intent.putExtra("EXTRA_LIST", LIST_DATA);
                        }
                    } else if (SENDER_CLASS.equals("AssignmentsPage")) {
                        ArrayList<AssignmentsModel> LIST_DATA = new ArrayList<>();
                        ArrayList<AssignmentsModel> DataArrayList = bundle.getParcelableArrayList("EXTRA_LIST_FRAG");
                        for (AssignmentsModel item : DataArrayList) {
                            if (item.content.toLowerCase().contains(mSearchInputValue) || item.DeadLine.toLowerCase().contains(mSearchInputValue) || item.title.toLowerCase().contains(mSearchInputValue))
                                LIST_DATA.add(item);
                        }

                        if (LIST_DATA.size() == 0) {
                            Toast.makeText(getActivity(), noMatchesLang, Toast.LENGTH_LONG).show();
                            mSearchBtn.setProgress(-1);
                            mSearchBtn.setEnabled(true);
                            mSearchBtn.setText(noMatchesLang);
                            return;
                        } else {
                            intent.putExtra("EXTRA_LIST", LIST_DATA);
                        }
                    } else if (SENDER_CLASS.equals("ExamsPage")) {
                        ArrayList<ExamsModel> LIST_DATA = new ArrayList<>();
                        ArrayList<ExamsModel> DataArrayList = bundle.getParcelableArrayList("EXTRA_LIST_FRAG");
                        for (ExamsModel item : DataArrayList) {
                            if (item.content.toLowerCase().contains(mSearchInputValue) || item.Date.toLowerCase().contains(mSearchInputValue) || item.title.toLowerCase().contains(mSearchInputValue))
                                LIST_DATA.add(item);
                        }

                        if (LIST_DATA.size() == 0) {
                            Toast.makeText(getActivity(), noMatchesLang, Toast.LENGTH_LONG).show();
                            mSearchBtn.setProgress(-1);
                            mSearchBtn.setEnabled(true);
                            mSearchBtn.setText(noMatchesLang);
                            return;
                        } else {
                            intent.putExtra("EXTRA_LIST", LIST_DATA);
                        }
                    } else if (SENDER_CLASS.equals("OnlineExamsPage")) {
                        ArrayList<OnlineExamsModel> LIST_DATA = new ArrayList<>();
                        ArrayList<OnlineExamsModel> DataArrayList = bundle.getParcelableArrayList("EXTRA_LIST_FRAG");
                        for (OnlineExamsModel item : DataArrayList) {
                            if (item.content.toLowerCase().contains(mSearchInputValue) || item.DeadLine.toLowerCase().contains(mSearchInputValue) || item.title.toLowerCase().contains(mSearchInputValue))
                                LIST_DATA.add(item);
                        }

                        if (LIST_DATA.size() == 0) {
                            Toast.makeText(getActivity(), noMatchesLang, Toast.LENGTH_LONG).show();
                            mSearchBtn.setProgress(-1);
                            mSearchBtn.setEnabled(true);
                            mSearchBtn.setText(noMatchesLang);
                            return;
                        } else {
                            intent.putExtra("EXTRA_LIST", LIST_DATA);
                        }
                    } else if (SENDER_CLASS.equals("EventsPage")) {
                        ArrayList<EventsModel> LIST_DATA = new ArrayList<>();
                        ArrayList<EventsModel> DataArrayList = bundle.getParcelableArrayList("EXTRA_LIST_FRAG");
                        for (EventsModel item : DataArrayList) {
                            if (item.content.toLowerCase().contains(mSearchInputValue) || item.date.toLowerCase().contains(mSearchInputValue) || item.forWho.toLowerCase().contains(mSearchInputValue) || item.place.toLowerCase().contains(mSearchInputValue) || item.title.toLowerCase().contains(mSearchInputValue))
                                LIST_DATA.add(item);
                        }

                        if (LIST_DATA.size() == 0) {
                            Toast.makeText(getActivity(), noMatchesLang, Toast.LENGTH_LONG).show();
                            mSearchBtn.setProgress(-1);
                            mSearchBtn.setEnabled(true);
                            mSearchBtn.setText(noMatchesLang);
                            return;
                        } else {
                            intent.putExtra("EXTRA_LIST", LIST_DATA);
                        }
                    } else if (SENDER_CLASS.equals("ClassesPage")) {
                        ArrayList<ClassesModel> LIST_DATA = new ArrayList<>();
                        ArrayList<ClassesModel> DataArrayList = bundle.getParcelableArrayList("EXTRA_LIST_FRAG");
                        for (ClassesModel item : DataArrayList) {
                            if (item.Dormitory.toLowerCase().contains(mSearchInputValue) || item.Name.toLowerCase().contains(mSearchInputValue) || item.Teacher.toLowerCase().contains(mSearchInputValue))
                                LIST_DATA.add(item);
                        }

                        if (LIST_DATA.size() == 0) {
                            Toast.makeText(getActivity(), noMatchesLang, Toast.LENGTH_LONG).show();
                            mSearchBtn.setProgress(-1);
                            mSearchBtn.setEnabled(true);
                            mSearchBtn.setText(noMatchesLang);
                            return;
                        } else {
                            intent.putExtra("EXTRA_LIST", LIST_DATA);
                        }
                    } else if (SENDER_CLASS.equals("SubjectsPage")) {
                        ArrayList<SubjectsModel> LIST_DATA = new ArrayList<>();
                        ArrayList<SubjectsModel> DataArrayList = bundle.getParcelableArrayList("EXTRA_LIST_FRAG");
                        for (SubjectsModel item : DataArrayList) {
                            if (item.Class.toLowerCase().contains(mSearchInputValue) || item.Name.toLowerCase().contains(mSearchInputValue) || item.Teacher.toLowerCase().contains(mSearchInputValue) || String.valueOf(item.passGrade).toLowerCase().contains(mSearchInputValue) || String.valueOf(item.finalGrade).toLowerCase().contains(mSearchInputValue))
                                LIST_DATA.add(item);
                        }

                        if (LIST_DATA.size() == 0) {
                            Toast.makeText(getActivity(), noMatchesLang, Toast.LENGTH_LONG).show();
                            mSearchBtn.setProgress(-1);
                            mSearchBtn.setEnabled(true);
                            mSearchBtn.setText(noMatchesLang);
                            return;
                        } else {
                            intent.putExtra("EXTRA_LIST", LIST_DATA);
                        }
                    } else if (SENDER_CLASS.equals("MessagesPage")) {
                        ArrayList<Dialog> LIST_DATA = new ArrayList<>();
                        ArrayList<Dialog> DataArrayList = bundle.getParcelableArrayList("EXTRA_LIST_FRAG");
                        for (Dialog item : DataArrayList) {
                            if (item.getDialogName().toLowerCase().contains(mSearchInputValue) ){
                                LIST_DATA.add(item);
                            }
                        }

                        if (LIST_DATA.size() == 0) {
                            Toast.makeText(getActivity(), noMatchesLang, Toast.LENGTH_LONG).show();
                            mSearchBtn.setProgress(-1);
                            mSearchBtn.setEnabled(true);
                            mSearchBtn.setText(noMatchesLang);
                            return;
                        } else {
                            intent.putExtra("EXTRA_LIST", LIST_DATA);
                        }
                    } else {
                        intent.putExtra("EXTRA_SEARCH", mSearchInputValue);
                    }
                    mSearchBtn.setProgress(100);
                    mSearchBtn.setEnabled(true);
                    startActivity(intent);
                } else {
                    mSearchBtn.setProgress(-1);
                    mSearchBtn.setEnabled(true);
                }
            }
        });
        mSearchTags.setTags(getTags());
        mSearchTags.setOnTagClickListener(new TagGroup.OnTagClickListener() {
            @Override
            public void onTagClick(String string) {
                mSearchInput.setInputWidgetText(string);
            }
        });
        return view;
    }


    @Override
    public void onTextChanged(FloatingLabelEditText source, String text) {
        if (source == mSearchInput) {
            mSearchInputValue = text;
        }
    }

    public String[] getTags() {

        ArrayList<String> tagsList = new ArrayList<>();
        for (Iterator<String> iterator = tagsStack.iterator(); iterator.hasNext(); ) {
            String value = iterator.next();
            if (value != null && !value.equals("")) tagsList.add(value);
        }
        String[] tagsArray = new String[tagsList.size()];
        for (int i = 0; i <= tagsList.size() - 1; i++) {
            tagsArray[i] = tagsList.get(i);
        }
        return tagsArray;
    }

    public void saveArray(Queue st) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor mEdit1 = sp.edit();

        Integer i = 1;
        for (Iterator<String> iterator = st.iterator(); iterator.hasNext(); ) {
            String value = iterator.next();
            mEdit1.remove(SENDER_CLASS + "_" + i);
            mEdit1.putString(SENDER_CLASS + "_" + i, value);
            i++;
        }
        mEdit1.apply();
    }

    public void loadArray() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        for (int i = 1; i < 15; i++) tagsStack.add(sp.getString(SENDER_CLASS + "_" + i, null));
    }

}
