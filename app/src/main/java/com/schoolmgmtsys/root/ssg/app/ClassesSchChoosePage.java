package com.schoolmgmtsys.root.ssg.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.marvinlabs.widget.floatinglabel.itempicker.FloatingLabelItemPicker;
import com.marvinlabs.widget.floatinglabel.itempicker.ItemPickerListener;
import com.marvinlabs.widget.floatinglabel.itempicker.StringPickerDialogFragment;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.models.ClassesSchModel;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.helpers.GsonParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ClassesSchChoosePage extends Fragment implements ItemPickerListener<String>, FloatingLabelItemPicker.OnItemPickerEventListener<String> {

    private ActionProcessButton ProcessDataBtn;
    private FloatingLabelItemPicker<String> ControlClasses;
    private String TOKEN;
    private List<String> classesItems = new ArrayList<>();
    private HashMap<String, HashMap<String, Integer>> sectionsItems;

    private String choosenClassName;
    private String classesLang;
    private int RetryLevel = 1;
    private ProgressBar mProgressBar;
    private FloatingLabelItemPicker<String> ControlSections;
    private String sectionsLang;
    private String choosenSectionID;
    private GsonParser parserManager;
    private HashMap<String, String> classesItemsWithoutSections;

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_classes_sch_choose, container, false);


        if (getActivity() instanceof ControlActivity) {
            ControlActivity activity = (ControlActivity) getActivity();
            mProgressBar = activity.getProgressBar();
        }

        parserManager = new GsonParser();
        classesLang = Concurrent.getLangSubWords("classes", "classes");
        sectionsLang = Concurrent.getLangSubWords("sections", "Sections");
        final String searchLang = Concurrent.getLangSubWords("Search", "Search");


        ProcessDataBtn = (ActionProcessButton) view.findViewById(R.id.process_data);
        ProcessDataBtn.setMode(ActionProcessButton.Mode.ENDLESS);

        ProcessDataBtn.setText(searchLang);

        ControlClasses = (FloatingLabelItemPicker<String>) view.findViewById(R.id.control_class);
        ControlSections = (FloatingLabelItemPicker<String>) view.findViewById(R.id.control_section);

        ControlClasses.setLabelText(classesLang);
        ControlSections.setLabelText(sectionsLang);

        ControlClasses.setItemPickerListener(ClassesSchChoosePage.this);
        ControlSections.setItemPickerListener(ClassesSchChoosePage.this);


        if (Concurrent.isSectionEnabled(getActivity())) {
            LoadClassesSections();
            ControlSections.setVisibility(View.VISIBLE);
        } else {
            LoadClasses();
            ControlSections.setVisibility(View.GONE);
        }

        ProcessDataBtn.setOnClickListener(new View.OnClickListener() {
            private String TOKEN;

            @Override
            public void onClick(View v) {
                if (choosenSectionID != null) {
                    TOKEN = Concurrent.getAppToken(getActivity());
                    if (TOKEN != null) {
                        ProcessDataBtn.setEnabled(false);
                        ProcessDataBtn.setProgress(10);

                        Ion.with(getContext()).load(OkHttpClient.strip(App.getAppBaseUrl() + Constants.TASK_GET_CLASS_SCH + "/" + choosenSectionID)).setTimeout(10000)
                                .asJsonObject().setCallback(new FutureCallback<JsonObject>() {

                            @Override
                            public void onCompleted(Exception exception, JsonObject ValuesHolder) {
                                if (exception != null) {
                                    ProcessDataBtn.setEnabled(true);
                                    ProcessDataBtn.setProgress(-1);
                                    ProcessDataBtn.setText(Concurrent.getLangSubWords("errorOccurred", "Error Occurred"));
                                    return;
                                }

                                if (ValuesHolder != null) {

                                    int pos = 0;
                                    HashMap<Integer, ArrayList<ClassesSchModel>> dataMap = new HashMap<>();
                                    ArrayList<String> daysNames = new ArrayList<>();
                                    ArrayList<ClassesSchModel> dataList;
                                    JsonArray inSchData;

                                    if (ValuesHolder.has("schedule")) {
                                        for (Map.Entry<String, JsonElement> entry : ValuesHolder.entrySet()) {
                                            if (entry.getKey().equals("schedule")) {
                                                JsonObject CurrObj = entry.getValue().getAsJsonObject();

                                                for (Map.Entry<String, JsonElement> en : CurrObj.getAsJsonObject().entrySet()) {
                                                    dataList = new ArrayList<>();
                                                    String dayName = Concurrent.tagsStringValidator(en.getValue().getAsJsonObject(), "dayName");
                                                    daysNames.add(dayName);
                                                    if (en.getValue().getAsJsonObject().getAsJsonArray("sub") != null) {
                                                        inSchData = en.getValue().getAsJsonObject().getAsJsonArray("sub");

                                                        for (JsonElement anInSchData : inSchData) {
                                                            JsonObject SchObj = anInSchData.getAsJsonObject();
                                                            dataList.add(new ClassesSchModel(Concurrent.tagsStringValidator(SchObj, "id"),
                                                                    dayName,
                                                                    Concurrent.tagsStringValidator(SchObj, "subjectId"),
                                                                    Concurrent.tagsStringValidator(SchObj, "start"),
                                                                    Concurrent.tagsStringValidator(SchObj, "end")));
                                                        }
                                                        dataMap.put(pos, dataList);
                                                    }
                                                    pos++;
                                                }
                                            }
                                        }
                                    } else {
                                        for (Map.Entry<String, JsonElement> entry : ValuesHolder.entrySet()) {

                                            JsonObject CurrObj = entry.getValue().getAsJsonObject();

                                            if(CurrObj != null){
                                                dataList = new ArrayList<>();
                                                String dayName = Concurrent.tagsStringValidator(CurrObj, "dayName");
                                                daysNames.add(dayName);
                                                if (CurrObj.getAsJsonArray("sub") != null) {
                                                    inSchData = CurrObj.getAsJsonArray("sub");

                                                    for (JsonElement anInSchData : inSchData) {
                                                        JsonObject SchObj = anInSchData.getAsJsonObject();
                                                        dataList.add(new ClassesSchModel(Concurrent.tagsStringValidator(SchObj, "id"),
                                                                dayName,
                                                                Concurrent.tagsStringValidator(SchObj, "subjectId"),
                                                                Concurrent.tagsStringValidator(SchObj, "start"),
                                                                Concurrent.tagsStringValidator(SchObj, "end")));
                                                    }
                                                    dataMap.put(pos, dataList);
                                                }
                                                pos++;
                                            }


                                        }
                                    }

                                    if (dataMap.size() > 0) {
                                        ProcessDataBtn.setEnabled(true);
                                        ProcessDataBtn.setProgress(100);
                                        Intent i = new Intent(getActivity(), ClassesSchPage.class);
                                        i.putExtra("classSchList", dataMap);
                                        i.putExtra("daysNames", daysNames);
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
                            }
                        });

                    }
                } else {
                    ProcessDataBtn.setEnabled(true);
                    ProcessDataBtn.setProgress(-1);
                    ProcessDataBtn.setText(Concurrent.getLangSubWords("errorOccurred", "Error Occurred"));
                }
            }
        });
        return view;
    }

    private void LoadClasses() {
        if (RetryLevel <= 3) {
            TOKEN = Concurrent.getAppToken(getActivity());
            if (TOKEN != null) {
                mProgressBar.setVisibility(View.VISIBLE);

                Ion.with(getContext()).load(OkHttpClient.strip(App.getAppBaseUrl() + Constants.TASK_CLASSES_SCH_LIST)).setTimeout(10000)
                        .asJsonObject().setCallback(new FutureCallback<JsonObject>() {
                    private JsonObject CurrObj;

                    @Override
                    public void onCompleted(Exception exception, JsonObject ValuesHolder) {
                        try {
                            JsonArray ValuesArray;
                            if (exception != null) {
                                RetryLevel++;
                                Toast.makeText(getContext(), "Please wait we trying to reload classes, Attempt : " + RetryLevel, Toast.LENGTH_LONG).show();
                                LoadClasses();
                            } else {
                                RetryLevel = 1;
                                if (ValuesHolder != null) {
                                    ValuesArray = ValuesHolder.getAsJsonArray("classes");
                                    if (ValuesArray != null) {
                                        classesItemsWithoutSections = new HashMap<>();
                                        Iterator<JsonElement> ValsIter = ValuesArray.iterator();

                                        while (ValsIter.hasNext()) {
                                            CurrObj = ValsIter.next().getAsJsonObject();
                                            classesItemsWithoutSections.put(Concurrent.tagsStringValidator(CurrObj, "className"), Concurrent.tagsStringValidator(CurrObj, "id"));
                                        }
                                    }
                                }

                                if (classesItemsWithoutSections != null && classesItemsWithoutSections.size() > 0) {
                                    ControlClasses.setAvailableItems(parserManager.getListOfMap(classesItemsWithoutSections, true));
                                    ControlClasses.setWidgetListener(new FloatingLabelItemPicker.OnWidgetEventListener<String>() {
                                        @Override
                                        public void onShowItemPickerDialog(FloatingLabelItemPicker<String> source) {
                                            StringPickerDialogFragment itemPicker3 = StringPickerDialogFragment.newInstance(
                                                    source.getId(),
                                                    classesLang,
                                                    Concurrent.getLangSubWords("ok", "OK"), Concurrent.getLangSubWords("cancel", "Cancel"),
                                                    false,
                                                    source.getSelectedIndices(),
                                                    new ArrayList<String>(source.getAvailableItems()));
                                            itemPicker3.setTargetFragment(ClassesSchChoosePage.this, 0);
//                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                                                if (getFragmentManager() != null) {
//                                                    itemPicker3.show(getFragmentManager(), "ItemPicker3");
//                                                }
//                                            } else {
//                                                itemPicker3.show(getChildFragmentManager(), "ItemPicker3");
//                                            }
                                            if (getFragmentManager() != null) {
                                                itemPicker3.show(getFragmentManager(), "ItemPicker3");
                                            }
                                        }
                                    });
                                }
                            }
                            mProgressBar.setVisibility(View.INVISIBLE);
                        } catch (Exception e) {
                        }
                    }
                });
            }
        }
    }

    private void LoadClassesSections() {
        if (RetryLevel <= 3) {
            TOKEN = Concurrent.getAppToken(getActivity());
            if (TOKEN != null) {
                mProgressBar.setVisibility(View.VISIBLE);

                Ion.with(getContext()).load(OkHttpClient.strip(App.getAppBaseUrl() + Constants.TASK_CLASSES_SCH_LIST)).setTimeout(10000)
                        .asJsonObject().setCallback(new FutureCallback<JsonObject>() {

                    @Override
                    public void onCompleted(Exception exception, JsonObject ValuesHolder) {
                        if (exception != null) {
                            RetryLevel++;
                            Toast.makeText(getContext(), "Please wait we trying to reload classes, Attempt : " + RetryLevel, Toast.LENGTH_LONG).show();
                            LoadClassesSections();
                        } else {
                            JsonObject ValuesObject;

                            try {
                                RetryLevel = 1;
                                if (ValuesHolder != null) {
                                    ValuesObject = ValuesHolder.getAsJsonObject("sections");

                                    if (ValuesObject != null) {
                                        sectionsItems = new HashMap<>();

                                        for (Map.Entry<String, JsonElement> entry : ValuesObject.entrySet()) {
                                            String className = entry.getKey();

                                            JsonArray sectionsEntries = entry.getValue().getAsJsonArray();
                                            classesItems.add(className);

                                            HashMap<String, Integer> schSections;

                                            schSections = new HashMap<>();

                                            for (JsonElement section : sectionsEntries) {

                                                JsonObject sectionObj = section.getAsJsonObject();
                                                schSections.put(
                                                        Concurrent.tagsStringValidator(sectionObj, "sectionName") + " - " + Concurrent.tagsStringValidator(sectionObj, "sectionTitle"),
                                                        Concurrent.tagsIntValidator(sectionObj, "id")
                                                );

                                            }

                                            sectionsItems.put(className, schSections);

                                        }
                                    }
                                }
                            } catch (Exception e) {

                            }
                            if (classesItems != null && classesItems.size() > 0) {
                                ControlClasses.setAvailableItems(classesItems);
                                ControlClasses.setWidgetListener(new FloatingLabelItemPicker.OnWidgetEventListener<String>() {
                                    @Override
                                    public void onShowItemPickerDialog(FloatingLabelItemPicker<String> source) {
                                        StringPickerDialogFragment itemPicker3 = StringPickerDialogFragment.newInstance(
                                                source.getId(),
                                                classesLang,
                                                Concurrent.getLangSubWords("ok", "OK"), Concurrent.getLangSubWords("cancel", "Cancel"),
                                                false,
                                                source.getSelectedIndices(),
                                                new ArrayList<>(source.getAvailableItems()));
                                        itemPicker3.setTargetFragment(ClassesSchChoosePage.this, 0);
//                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                                            if (getFragmentManager() != null) {
//                                                itemPicker3.show(getFragmentManager(), "classes");
//                                            }
//                                        } else {
//                                            itemPicker3.show(getChildFragmentManager(), "classes");
//                                        }
                                        if (getFragmentManager() != null) {
                                            itemPicker3.show(getFragmentManager(), "classes");
                                        }
                                    }
                                });
                            }
                        }
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }
    }

    @Override
    public void onSelectionChanged(FloatingLabelItemPicker<String> source, Collection<String> selectedItems) {
        String newSelectedItem = (String) ((new ArrayList(selectedItems)).get(0));

        if (Concurrent.isSectionEnabled(getActivity())) {
            if (source == ControlClasses) {
                choosenClassName = newSelectedItem;
                setSectionsByClass(choosenClassName);
            } else if (source == ControlSections) {
                choosenSectionID = String.valueOf(sectionsItems.get(choosenClassName).get(newSelectedItem));
            }
        } else {
            choosenSectionID = String.valueOf(classesItemsWithoutSections.get(newSelectedItem));
        }

    }

    public void setSectionsByClass(String className) {
        if (sectionsItems.containsKey(className)) {
            HashMap<String, Integer> sectionMap = sectionsItems.get(className);
            ArrayList<String> sectionsNames = new ArrayList();
            if (sectionMap != null) {
                for (Map.Entry<String, Integer> entry : sectionMap.entrySet()) {
                    sectionsNames.add(entry.getKey());
                }

                ControlSections.setAvailableItems(sectionsNames);
                ControlSections.setWidgetListener(new FloatingLabelItemPicker.OnWidgetEventListener<String>() {
                    @Override
                    public void onShowItemPickerDialog(FloatingLabelItemPicker<String> source) {
                        StringPickerDialogFragment itemPicker3 = StringPickerDialogFragment.newInstance(
                                source.getId(),
                                sectionsLang,
                                Concurrent.getLangSubWords("ok", "OK"), Concurrent.getLangSubWords("cancel", "Cancel"),
                                false,
                                source.getSelectedIndices(),
                                new ArrayList<>(source.getAvailableItems()));
                        itemPicker3.setTargetFragment(ClassesSchChoosePage.this, 0);
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                            if (getFragmentManager() != null) {
//                                itemPicker3.show(getFragmentManager(), "sections");
//                            }
//                        } else {
//                            itemPicker3.show(getChildFragmentManager(), "sections");
//                        }
                        if (getFragmentManager() != null) {
                            itemPicker3.show(getFragmentManager(), "sections");
                        }
                    }
                });
            }
        }


    }

    @Override
    public void onCancelled(int pickerId) {

    }

    @Override
    public void onItemsSelected(int pickerId, int[] selectedIndices) {
        if (Concurrent.isSectionEnabled(getActivity())) {
            if (pickerId == R.id.control_class) {
                ControlClasses.setSelectedIndices(selectedIndices);
            } else if (pickerId == R.id.control_section) {
                ControlSections.setSelectedIndices(selectedIndices);
            }
        } else {
            ControlClasses.setSelectedIndices(selectedIndices);
        }

    }

}
