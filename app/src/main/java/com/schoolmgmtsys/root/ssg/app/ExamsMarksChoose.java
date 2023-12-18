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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.marvinlabs.widget.floatinglabel.itempicker.FloatingLabelItemPicker;
import com.marvinlabs.widget.floatinglabel.itempicker.ItemPickerListener;
import com.marvinlabs.widget.floatinglabel.itempicker.StringPickerDialogFragment;
import com.schoolmgmtsys.root.ssg.models.ClassesModel;
import com.schoolmgmtsys.root.ssg.models.ExamMarkModel;
import com.schoolmgmtsys.root.ssg.models.SectionsModel;
import com.schoolmgmtsys.root.ssg.models.SubjectsModel2;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.solutionsbricks.solbricksframework.Call;
import com.solutionsbricks.solbricksframework.Callback;
import com.solutionsbricks.solbricksframework.FormBody;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.Request;
import com.solutionsbricks.solbricksframework.Response;
import com.solutionsbricks.solbricksframework.helpers.GsonParser;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ExamsMarksChoose extends Fragment implements ItemPickerListener<String>, FloatingLabelItemPicker.OnItemPickerEventListener<String> {

    private FloatingLabelItemPicker<String> ControlSubject;
    private ActionProcessButton ProcessDataBtn;
    private SharedPreferences Prefs;
    private JsonObject ValuesObject;
    private FloatingLabelItemPicker<String> ControlClasses;
    private String TOKEN;
    private GsonParser parserManager;
    private boolean classSelectedLock;
    private HashMap<String, SubjectsModel2> subjectsItems;
    private HashMap<String, SectionsModel> sectionsItems;

    private String selectedItem;
    private String newSelectedItem;
    private String choosenClassID;
    private String choosenSubjectID;
    private HashMap<String, String> attendanceItems;
    private ArrayList<ClassesModel> ClassesList;
    private ArrayList<String> classesString;
    private int EXAM_ID = -1;
    private String subjectsLang;
    private FloatingLabelItemPicker<String> ControlSection;
    private String choosenSectionID;
    private String sectionsLang;
    private boolean IsNewerVersion;
    private String response;

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_exam_mark_select, container, false);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            ClassesList = bundle.getParcelableArrayList("EXTRA_LIST_FRAG");
            EXAM_ID = bundle.getInt("EXTRA_INT_1_FRAG");

        }
        final String classesLang = Concurrent.getLangSubWords("classes", "classes");
        subjectsLang = Concurrent.getLangSubWords("Subjects", "Subjects");
        sectionsLang = Concurrent.getLangSubWords("sections", "Sections");
        final String searchLang = Concurrent.getLangSubWords("Search", "Search");

        parserManager = new GsonParser();

        ProcessDataBtn = (ActionProcessButton) view.findViewById(R.id.process_data);
        ProcessDataBtn.setMode(ActionProcessButton.Mode.ENDLESS);
        ProcessDataBtn.setText(searchLang);

        Prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ControlSubject = (FloatingLabelItemPicker<String>) view.findViewById(R.id.control_subject);
        ControlSubject.setItemPickerListener(this);
        ControlSubject.setLabelText(subjectsLang);

        ControlSection = (FloatingLabelItemPicker<String>) view.findViewById(R.id.control_section);
        ControlSection.setItemPickerListener(this);
        ControlSection.setLabelText(sectionsLang);


        ControlClasses = (FloatingLabelItemPicker<String>) view.findViewById(R.id.control_class);
        ControlClasses.setLabelText(classesLang);

        if (!Concurrent.isSectionEnabled(getActivity())) {
            ControlSection.setVisibility(View.GONE);
        }


        ControlClasses.setItemPickerListener(ExamsMarksChoose.this);
        if (ClassesList != null) {
            classesString = new ArrayList<>();
            for (ClassesModel classObject : ClassesList) classesString.add(classObject.Name);
            ControlClasses.setAvailableItems(classesString);

            ControlClasses.setWidgetListener(new FloatingLabelItemPicker.OnWidgetEventListener<String>() {
                @Override
                public void onShowItemPickerDialog(FloatingLabelItemPicker<String> source) {
                    StringPickerDialogFragment itemPicker3 = StringPickerDialogFragment.newInstance(
                            source.getId(),
                            classesLang,
                            Concurrent.getLangSubWords("ok","OK"), Concurrent.getLangSubWords("cancel","Cancel"),
                            false,
                            source.getSelectedIndices(),
                            new ArrayList<>(source.getAvailableItems()));
                    itemPicker3.setTargetFragment(ExamsMarksChoose.this, 0);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        if (getFragmentManager() != null) {
//                            itemPicker3.show(getFragmentManager(), "Classes");
//                        }
//                    } else {
//                        itemPicker3.show(getChildFragmentManager(), "Classes");
//                    }
                    if (getFragmentManager() != null) {
                        itemPicker3.show(getFragmentManager(), "Classes");
                    }
                }
            });
        }
        ProcessDataBtn.setOnClickListener(new View.OnClickListener() {
                private String TOKEN;

                @Override
                public void onClick(View v) {
                    TOKEN = Concurrent.getAppToken(getActivity());
                    if (TOKEN != null) {
                        if (choosenClassID != null) {
                            ProcessDataBtn.setEnabled(false);
                            ProcessDataBtn.setProgress(10);

                            FormBody.Builder formBody = new FormBody.Builder();
                            formBody.add("classId", String.valueOf(choosenClassID));
                            formBody.add("exam", String.valueOf(EXAM_ID));
                            if (Concurrent.isSectionEnabled(getActivity()))
                                formBody.add("sectionId", String.valueOf(choosenSectionID));
                            if (choosenSubjectID != null) formBody.add("subjectId", String.valueOf(choosenSubjectID));
                            else return;

                            OkHttpClient client = new OkHttpClient().newBuilder(getActivity()).connectTimeout(7, TimeUnit.SECONDS).build();

                            Request.Builder requestBuilder = new Request.Builder()
                                    .url(App.getAppBaseUrl() + Constants.TASK_GET_EXAMS_MARKS);

                            requestBuilder.post(formBody.build());

                            Request request = requestBuilder.build();


                            Call call = client.newCall(request);
                            call.enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, final IOException e) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        public void run() {
                                            ProcessDataBtn.setEnabled(true);
                                            ProcessDataBtn.setProgress(-1);
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
                                                        JsonArray ValuesArray = null;

                                                        try {
                                                            ValuesHolder = parser.parse(response).getAsJsonObject();
                                                        } catch (Exception e) {
                                                            showError("5001");
                                                        }

                                                        classSelectedLock = false;
                                                        ArrayList<ExamMarkModel> marksList = new ArrayList<>();

                                                        if (ValuesHolder != null) {
                                                            //JsonObject studentObj = null;
                                                            try {
                                                                ValuesArray = ValuesHolder.getAsJsonArray("students");
                                                            } catch (ClassCastException e) {
                                                                ProcessDataBtn.setEnabled(true);
                                                                ProcessDataBtn.setText("Empty Result");
                                                                ProcessDataBtn.setProgress(-1);
                                                            } catch (Exception e) {
                                                                ProcessDataBtn.setEnabled(true);
                                                                ProcessDataBtn.setText(Concurrent.getLangSubWords("errorOccurred","Error Occurred"));
                                                                ProcessDataBtn.setProgress(-1);
                                                            }
                                                            if (ValuesArray != null) {


                                                                // Newer MarkSheet Model
                                                                HashMap<Integer, String> ColsMap = null;
                                                                HashMap<String, String> OutputColsMap = null;
                                                                IsNewerVersion = false;

                                                                Intent i = new Intent(getActivity(), ExamMarkPage.class);

                                                                JsonObject subjectObj = ValuesHolder.getAsJsonObject("subject");
                                                                JsonObject examObj = ValuesHolder.getAsJsonObject("exam");
                                                                JsonObject classObj = ValuesHolder.getAsJsonObject("class");

                                                                i.putExtra("class_id", Concurrent.tagsStringValidator(classObj, "id"));
                                                                i.putExtra("class_className", Concurrent.tagsStringValidator(classObj, "className"));
                                                                i.putExtra("class_classTeacher", Concurrent.tagsStringValidator(classObj, "classTeacher"));
                                                                i.putExtra("class_classAcademicYear", Concurrent.tagsStringValidator(classObj, "classAcademicYear"));
                                                                i.putExtra("class_classSubjects", Concurrent.tagsStringValidator(classObj, "classSubjects"));
                                                                i.putExtra("class_dormitoryId", Concurrent.tagsStringValidator(classObj, "dormitoryId"));

                                                                i.putExtra("exam_id", Concurrent.tagsStringValidator(examObj, "id"));
                                                                i.putExtra("exam_examTitle", Concurrent.tagsStringValidator(examObj, "examTitle"));
                                                                i.putExtra("exam_examDescription", Concurrent.tagsStringValidator(examObj, "examDescription"));
                                                                i.putExtra("exam_examDate", Concurrent.tagsStringValidator(examObj, "examDate"));
                                                                i.putExtra("exam_examAcYear", Concurrent.tagsStringValidator(examObj, "examAcYear"));

                                                                i.putExtra("subject_id", Concurrent.tagsStringValidator(subjectObj, "id"));
                                                                i.putExtra("subject_subjectTitle", Concurrent.tagsStringValidator(subjectObj, "subjectTitle"));
                                                                i.putExtra("subject_teacherId", Concurrent.tagsStringValidator(subjectObj, "teacherId"));
                                                                i.putExtra("subject_passGrade", Concurrent.tagsStringValidator(subjectObj, "passGrade"));
                                                                i.putExtra("subject_finalGrade", Concurrent.tagsStringValidator(subjectObj, "finalGrade"));

                                                                if(choosenSectionID != null && !choosenSectionID.equals(""))i.putExtra("sectionId", String.valueOf(choosenSectionID));


                                                                // New Version Variables

                                                                if(examObj.has("examMarksheetColumns")){

                                                                    if(examObj.has("examMarksheetColumns"))i.putExtra("exam_examMarksheetColumns",examObj.get("examMarksheetColumns").toString());
                                                                    if(examObj.has("examClasses"))i.putExtra("exam_examClasses", examObj.get("examClasses").toString());

                                                                    if (examObj.has("examMarksheetColumns")) {
                                                                        IsNewerVersion = true;
                                                                        ColsMap = new HashMap<>();

                                                                        IsNewerVersion = true;
                                                                        JsonArray ColsArray = examObj.get("examMarksheetColumns").getAsJsonArray();
                                                                        for (JsonElement oneCol : ColsArray) {
                                                                            JsonObject ColItem = oneCol.getAsJsonObject();
                                                                            ColsMap.put(Concurrent.tagsIntValidator(ColItem,"id"), Concurrent.tagsStringValidator(ColItem,"title"));
                                                                        }
                                                                        i.putExtra("markcols", ColsMap);
                                                                    }

                                                                }

                                                                for(JsonElement obj:ValuesArray) {
                                                                    JsonObject objResult = obj.getAsJsonObject();

                                                                    ExamMarkModel item = new ExamMarkModel();

                                                                    if(IsNewerVersion){
                                                                        item.id = Concurrent.repairJsonValueQuotes(objResult.get("id").toString());
                                                                        item.MarkComment = Concurrent.repairJsonValueQuotes(objResult.get("markComments").toString());
                                                                        item.StudentName = Concurrent.repairJsonValueQuotes(objResult.get("name").toString());
                                                                        item.StudentRollId = Concurrent.repairJsonValueQuotes(objResult.get("studentRollId").toString());

                                                                        if(objResult.has("examMark")){
                                                                            if(objResult.get("examMark") instanceof JsonArray){
                                                                                item.MarksColsMap = new HashMap<>();
                                                                            }else{
                                                                                if (objResult.get("examMark") instanceof JsonObject) {
                                                                                    JsonObject examMarkMap = objResult.getAsJsonObject("examMark");
                                                                                    if (examMarkMap != null && !examMarkMap.equals("")) {
                                                                                        OutputColsMap = new HashMap<>();
                                                                                        for (Map.Entry<String, JsonElement> entryItem : examMarkMap.entrySet()) {
                                                                                            if(ColsMap != null && ColsMap.containsKey(Integer.valueOf(entryItem.getKey())) && !entryItem.getValue().isJsonNull()){
                                                                                                OutputColsMap.put(String.valueOf(ColsMap.get(Integer.valueOf(entryItem.getKey()))), entryItem.getValue().getAsString());
                                                                                            }
                                                                                        }
                                                                                        item.MarksColsMap = OutputColsMap;
                                                                                    }
                                                                                }
                                                                            }
                                                                        }

                                                                        if(objResult.has("totalMarks") ){
                                                                            try{
                                                                                item.TotalMark = Concurrent.tagsStringValidator(objResult,"totalMarks");
                                                                            }catch (Exception ignored){}
                                                                        }

                                                                    }else{
                                                                        item = new ExamMarkModel(Concurrent.repairJsonValueQuotes(objResult.get("id").toString()), Concurrent.repairJsonValueQuotes(objResult.get("name").toString()), Concurrent.repairJsonValueQuotes(objResult.get("studentRollId").toString()), Concurrent.repairJsonValueQuotes(objResult.get("attendanceMark").toString()), Concurrent.repairJsonValueQuotes(objResult.get("examMark").toString()), Concurrent.repairJsonValueQuotes(objResult.get("markComments").toString()));

                                                                    }
                                                                    marksList.add(item);

                                                                }


                                                                i.putExtra("marksList", marksList);

                                                                ProcessDataBtn.setEnabled(true);
                                                                ProcessDataBtn.setProgress(100);
                                                                startActivity(i);
                                                            } else {
                                                                ProcessDataBtn.setEnabled(true);
                                                                ProcessDataBtn.setProgress(-1);
                                                                ProcessDataBtn.setText("Empty Result");
                                                            }
                                                        } else {
                                                            ProcessDataBtn.setEnabled(true);
                                                            ProcessDataBtn.setProgress(-1);
                                                            ProcessDataBtn.setText("Empty Result");
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
                        } else {
                            ProcessDataBtn.setEnabled(true);
                            ProcessDataBtn.setProgress(-1);
                        }
                    }

                }
            });




        return view;
    }

    public void showError(String errorCode) {
        String errorTitle;
        if (errorCode != null) {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: " + errorCode + " )";
        } else {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred");
        }
        ProcessDataBtn.setEnabled(true);
        ProcessDataBtn.setProgress(-1);
        ProcessDataBtn.setText(Concurrent.getLangSubWords("errorOccurred","Error Occurred"));
        Toast.makeText(getContext(), errorTitle, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSelectionChanged(FloatingLabelItemPicker<String> source, Collection<String> selectedItems) {
        newSelectedItem = (String) ((new ArrayList(selectedItems)).get(0));
        if (source == ControlClasses) {
            choosenClassID = String.valueOf(ClassesList.get(classesString.indexOf(newSelectedItem)).id);
            if (!classSelectedLock || !newSelectedItem.equals(selectedItem)) {
                classSelectedLock = true;

                selectedItem = (String) ((new ArrayList(selectedItems)).get(0));
                TOKEN = Concurrent.getAppToken(getActivity());
                if (TOKEN != null) {

                    FormBody.Builder formBody = new FormBody.Builder();
                    formBody.add("classes", String.valueOf(choosenClassID));

                    OkHttpClient client = new OkHttpClient().newBuilder(getContext()).connectTimeout(7, TimeUnit.SECONDS).build();

                    Request.Builder requestBuilder = new Request.Builder()
                            .url(App.getAppBaseUrl() + Constants.TASK_GET_SECTIONS_SUBJECTS);

                    requestBuilder.post(formBody.build());

                    Request request = requestBuilder.build();


                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, final IOException e) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    if (e instanceof ConnectException) {
                                        Toast.makeText(getContext(), Concurrent.getLangSubWords("noConnection", "No Internet Connection"), Toast.LENGTH_LONG).show();
                                    }else{
                                        Toast.makeText(getActivity(), Concurrent.getLangSubWords("errorOccurred","Error Occurred"), Toast.LENGTH_LONG).show();
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
                                Toast.makeText(getActivity(), Concurrent.getLangSubWords("errorOccurred","Error Occurred"), Toast.LENGTH_LONG).show();
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
                                                    Toast.makeText(getActivity(), Concurrent.getLangSubWords("errorOccurred","Error Occurred"), Toast.LENGTH_LONG).show();
                                                }

                                                classSelectedLock = false;
                                                if (ValuesHolder != null) {
                                                    JsonArray SubjectsArray = ValuesHolder.getAsJsonArray("subjects");
                                                    Iterator<JsonElement> ValsIter = SubjectsArray.iterator();
                                                    subjectsItems = new HashMap<>();
                                                    ArrayList<String> pickerItems = new ArrayList<>();
                                                    JsonObject CurrObj;
                                                    while (ValsIter.hasNext()) {
                                                        CurrObj = ValsIter.next().getAsJsonObject();
                                                        String subjectTitle = Concurrent.tagsStringValidator(CurrObj, "subjectTitle");
                                                        subjectsItems.put(subjectTitle, new SubjectsModel2(Concurrent.tagsIntValidator(CurrObj, "id"), subjectTitle, String.valueOf(ClassesList.get(classesString.indexOf(selectedItem)).id), Concurrent.tagsStringValidator(CurrObj, "teacherId")));

                                                        pickerItems.add(subjectTitle);
                                                    }
                                                    ControlSubject.setAvailableItems(pickerItems);
                                                    ControlSubject.setWidgetListener(new FloatingLabelItemPicker.OnWidgetEventListener<String>() {
                                                        @Override
                                                        public void onShowItemPickerDialog(FloatingLabelItemPicker source) {
                                                            StringPickerDialogFragment itemPicker2 = StringPickerDialogFragment.newInstance(
                                                                    source.getId(),
                                                                    subjectsLang,
                                                                    Concurrent.getLangSubWords("ok","OK"), Concurrent.getLangSubWords("cancel","Cancel"),
                                                                    false,
                                                                    source.getSelectedIndices(),
                                                                    new ArrayList<String>(source.getAvailableItems()));
                                                            itemPicker2.setTargetFragment(ExamsMarksChoose.this, 0);
//                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                                                                if (getFragmentManager() != null) {
//                                                                    itemPicker2.show(getFragmentManager(), "Subject");
//                                                                }
//                                                            } else {
//                                                                itemPicker2.show(getChildFragmentManager(), "Subject");
//                                                            }
                                                            if (getFragmentManager() != null) {
                                                                itemPicker2.show(getFragmentManager(), "Subject");
                                                            }
                                                        }
                                                    });

                                                    JsonArray SectionsArray = ValuesHolder.getAsJsonArray("sections");
                                                    Iterator<JsonElement> sectionsValsIter = SectionsArray.iterator();
                                                    sectionsItems = new HashMap<>();
                                                    pickerItems = new ArrayList<>();
                                                    while (sectionsValsIter.hasNext()) {
                                                        CurrObj = sectionsValsIter.next().getAsJsonObject();
                                                        String Title = Concurrent.tagsStringValidator(CurrObj, "sectionName") + " - " + Concurrent.tagsStringValidator(CurrObj, "sectionTitle");
                                                        sectionsItems.put(Title, new SectionsModel(Concurrent.tagsIntValidator(CurrObj, "id"), Concurrent.tagsStringValidator(CurrObj, "sectionName"), Concurrent.tagsStringValidator(CurrObj, "sectionTitle"), Concurrent.tagsIntValidator(CurrObj, "classId")));
                                                        pickerItems.add(Title);
                                                    }
                                                    ControlSection.setAvailableItems(pickerItems);
                                                    ControlSection.setWidgetListener(new FloatingLabelItemPicker.OnWidgetEventListener<String>() {
                                                        @Override
                                                        public void onShowItemPickerDialog(FloatingLabelItemPicker source) {
                                                            StringPickerDialogFragment itemPicker2 = StringPickerDialogFragment.newInstance(
                                                                    source.getId(),
                                                                    sectionsLang,
                                                                    Concurrent.getLangSubWords("ok","OK"), Concurrent.getLangSubWords("cancel","Cancel"),
                                                                    false,
                                                                    source.getSelectedIndices(),
                                                                    new ArrayList<String>(source.getAvailableItems()));
                                                            itemPicker2.setTargetFragment(ExamsMarksChoose.this, 0);
//                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                                                                if (getFragmentManager() != null) {
//                                                                    itemPicker2.show(getFragmentManager(), "Sections");
//                                                                }
//                                                            } else {
//                                                                itemPicker2.show(getChildFragmentManager(), "Sections");
//                                                            }
                                                            if (getFragmentManager() != null) {
                                                                itemPicker2.show(getFragmentManager(), "Sections");
                                                            }
                                                        }
                                                    });
                                                }

                                            } else {
                                                Toast.makeText(getActivity(), Concurrent.getLangSubWords("errorOccurred","Error Occurred"), Toast.LENGTH_LONG).show();
                                            }
                                        } catch (final Exception e) {
                                            Toast.makeText(getActivity(), Concurrent.getLangSubWords("errorOccurred","Error Occurred"), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(getActivity(), Concurrent.getLangSubWords("errorOccurred","Error Occurred"), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        } else if (source == ControlSubject) {
            choosenSubjectID = String.valueOf(subjectsItems.get(newSelectedItem).id);
        } else if (source == ControlSection) {
            choosenSectionID = String.valueOf(sectionsItems.get(newSelectedItem).id);
        }

    }

    @Override
    public void onCancelled(int pickerId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onItemsSelected(int pickerId, int[] selectedIndices) {
        if (pickerId == R.id.control_class) {
            ControlClasses.setSelectedIndices(selectedIndices);
        } else if (pickerId == R.id.control_subject) {
            ControlSubject.setSelectedIndices(selectedIndices);
        } else if (pickerId == R.id.control_section) {
            ControlSection.setSelectedIndices(selectedIndices);
        }
    }

}
