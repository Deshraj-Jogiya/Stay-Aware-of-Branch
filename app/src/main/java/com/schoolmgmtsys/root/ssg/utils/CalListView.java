package com.schoolmgmtsys.root.ssg.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.solbricks.solbrickscal.Detector;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.app.CalenderItemPage;
import com.schoolmgmtsys.root.ssg.app.EventsViewPage;
import com.schoolmgmtsys.root.ssg.app.ExamsPage;
import com.schoolmgmtsys.root.ssg.app.NewsViewPage;
import com.schoolmgmtsys.root.ssg.app.OnlineExamsPage;
import com.schoolmgmtsys.root.ssg.fonts.ParentStyledTextView;
import com.schoolmgmtsys.root.ssg.models.CalenderDayModel;
import com.schoolmgmtsys.root.ssg.models.CalenderEventModel;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.helpers.ListManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by SolutionsBricks Mobile Dev. Team.
 */

public class CalListView extends FrameLayout implements ListManager.ListInterface, Detector.DetectorInterface {

    private CalEventsHolder holder;
    private CalenderDayModel posValues;
    private Detector detectorObj;
    private ListManager mListManager;
    private Handler errorCheckHandler;
    private Runnable errorCheckRunnable;
    private View calView;

    enum BULK_STATE{
        IDLE,
        LOADING,
        FAILED,
        LOADED
    }
    private ArrayList<CalenderDayModel> daysList = new ArrayList<>();
    private HashMap<String,Integer> daysSpecsMap = new HashMap<>();
    private HashMap<String,BULK_STATE> daysBulkState = new HashMap<>(); // start Day,bulk State



    public CalListView(Context context) {
        super(context);
        init();
    }

    public CalListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public CalListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        calView = inflater.inflate(R.layout.page_cal_view, null);
        addView(calView,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        mListManager = new ListManager((Activity) getContext(), (ListView) calView.findViewById(R.id.calendarView), this, daysList);

        tryToGetDays();

        findViewById(R.id.error_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryToGetDays();
            }
        });

    }

    public void tryToGetDays(){
        changePageView(pageLayer.Loading);
        detectorObj = new Detector(this);
        detectorObj.getNextDays(getContext());

        errorCheckHandler = new Handler();
        errorCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if(detectorObj.tryingToGetDays)changePageView(pageLayer.ErrorLoading);
            }
        };
        errorCheckHandler.postDelayed(errorCheckRunnable, 5000);
    }

    public void tryToGetMoreDays(){
        if(daysList.size() > 0){
            if(detectorObj == null) detectorObj = new Detector(this);
            detectorObj.getNextDays(getContext());
        }
    }

    @Override
    public void loadMore() {
        tryToGetMoreDays();
    }

    @Override
    public void AdapterConstructor() {

    }

    @Override
    public View AdapterGetView(int position, View convertView) {
        LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
        if (convertView == null || convertView.getTag() == null) {
            holder = new CalEventsHolder();
            convertView = inflater.inflate(R.layout.calendar_list_item, null);

            holder.parentEventsCon = (RelativeLayout) convertView.findViewById(R.id.parent_events_con);
            holder.dateTextTitle = (ParentStyledTextView) convertView.findViewById(R.id.title);
            holder.noEventsCon = (RelativeLayout) convertView.findViewById(R.id.no_events_con);
            holder.errorLoadingEventsCon = (RelativeLayout) convertView.findViewById(R.id.error_loading_events_con);
            holder.loadingEventsCon = (RelativeLayout) convertView.findViewById(R.id.loading_events_con);
            holder.eventsCon = (LinearLayout) convertView.findViewById(R.id.events_con);

            convertView.setTag(holder);
        } else {
            holder = (CalEventsHolder) convertView.getTag();
        }
        posValues = daysList.get(position);
        if (posValues != null) {

            if (position == 0) {
                holder.dateTextTitle.setNotNullText(Concurrent.getLangSubWords("today","Today")+", "+posValues.dayName);
            } else if (position == 1) {
                holder.dateTextTitle.setNotNullText(Concurrent.getLangSubWords("tomorrow","Tomorrow")+", "+posValues.dayName);
            } else {
                holder.dateTextTitle.setNotNullText(posValues.dayName);
            }
            if(daysBulkState.containsKey(posValues.startDayNameInItsCollection)){
                BULK_STATE dayState = daysBulkState.get(posValues.startDayNameInItsCollection);
                if(dayState.equals(BULK_STATE.LOADED)){
                    if (posValues.dayEvents.size() > 0) {
                        changeItemCurrentView(holder, eventLayers.EventsData);
                        setItemEvents(holder.eventsCon, posValues.dayEvents);
                        holder.parentEventsCon.setBackgroundColor(Concurrent.getColor(getContext(), R.color.x_calendar_have_events_back));
                    } else {
                        changeItemCurrentView(holder, eventLayers.EmptyEvents);
                        holder.parentEventsCon.setBackgroundColor(Concurrent.getColor(getContext(), R.color.x_calendar_no_events_back));
                    }
                }else{
                    if(dayState.equals(BULK_STATE.LOADING))
                        changeItemCurrentView(holder, eventLayers.Loading);
                    else if(dayState.equals(BULK_STATE.FAILED)) {
                        changeItemCurrentView(holder, eventLayers.ErrorLoading);
                    }
                    holder.parentEventsCon.setBackgroundColor(Concurrent.getColor(getContext(), R.color.x_calendar_no_events_back));
                }
            }
        }
        return convertView;
    }

    @Override
    public void daysListReady(final ArrayList<String> nextDays) {
        if (nextDays != null && nextDays.size() > 0) {
            detectorObj.tryingToGetDays = false;
            ((Activity) getContext()).runOnUiThread(new Runnable() {
                public void run() {
                    changePageView(pageLayer.DaysData);
                    String sDay = nextDays.get(0);
                    String eDay = nextDays.get(nextDays.size() - 1);

                    for (String day : nextDays) {
                        CalenderDayModel calItem = new CalenderDayModel(day, nextDays.indexOf(day), sDay, eDay);
                        daysList.add(calItem);
                        daysSpecsMap.put(day,daysList.indexOf(calItem));
                    }
                    daysBulkState.put(sDay,BULK_STATE.IDLE);
                    getEvents(sDay,eDay);
                    mListManager.getListAdapter().notifyDataSetChanged();
                }
            });
        }
    }

    private enum eventLayers {
        EmptyEvents,
        ErrorLoading,
        EventsData,
        Loading
    }

    public void changeItemCurrentView(CalEventsHolder holder, eventLayers layerIndex) {
        holder.noEventsCon.setVisibility(layerIndex.equals(eventLayers.EmptyEvents) ? VISIBLE : GONE);
        holder.errorLoadingEventsCon.setVisibility(layerIndex.equals(eventLayers.ErrorLoading) ? VISIBLE : GONE);
        holder.loadingEventsCon.setVisibility(layerIndex.equals(eventLayers.Loading) ? VISIBLE : GONE);
        holder.eventsCon.setVisibility(layerIndex.equals(eventLayers.EventsData) ? VISIBLE : GONE);
    }

    private enum pageLayer {
        ErrorLoading,
        DaysData,
        Loading
    }

    public void changePageView(pageLayer layerIndex) {
        calView.findViewById(R.id.error_view).setVisibility(layerIndex.equals(pageLayer.ErrorLoading) ? View.VISIBLE : View.GONE);
        calView.findViewById(R.id.loading_view).setVisibility(layerIndex.equals(pageLayer.Loading) ? View.VISIBLE : View.GONE);
        calView.findViewById(R.id.calendarView).setVisibility(layerIndex.equals(pageLayer.DaysData) ? View.VISIBLE : View.GONE);
    }


    private class CalEventsHolder {
        ParentStyledTextView dateTextTitle;
        RelativeLayout noEventsCon;
        RelativeLayout errorLoadingEventsCon;
        RelativeLayout loadingEventsCon;
        LinearLayout eventsCon;
        RelativeLayout parentEventsCon;

    }

    public void getEvents(final String sDay, final String eDay) {

        String TOKEN = Concurrent.getAppToken(getContext());
        if (TOKEN != null) {

            daysBulkState.put(sDay,BULK_STATE.LOADING);

            Ion.with(getContext()).load(OkHttpClient.strip(App.getAppBaseUrl() + Constants.TASK_CALENDER + "?start=" + sDay + "&end=" + eDay + "")).setTimeout(10000)
                    .asJsonArray().setCallback(new FutureCallback<JsonArray>() {

                public JsonObject CurrObj;

                @Override
                public void onCompleted(Exception exception, JsonArray ValuesArray) {

                    if(exception != null)
                        daysBulkState.put(sDay,BULK_STATE.FAILED);

                    if (ValuesArray != null) {
                        Iterator<JsonElement> ValsIter = ValuesArray.iterator();
                        CalenderEventModel calEventItem;

                        while (ValsIter.hasNext()) {

                            CurrObj = ValsIter.next().getAsJsonObject();
                            String CompDate = Concurrent.tagsStringValidator(CurrObj, "start");
                            String DestDate = CompDate.substring(0, Math.min(CompDate.length(), 10));

                            calEventItem = new CalenderEventModel(
                                    Concurrent.tagsStringValidator(CurrObj, "id"),
                                    Concurrent.tagsStringValidator(CurrObj, "title"),
                                    Concurrent.tagsStringValidator(CurrObj, "start"),
                                    Concurrent.tagsStringValidator(CurrObj, "url"),
                                    Concurrent.tagsStringValidator(CurrObj, "backgroundColor"),
                                    Concurrent.tagsStringValidator(CurrObj, "textColor"),
                                    Concurrent.tagsStringValidator(CurrObj, "allDay"));
                            calEventItem.onlyDate = DestDate;

                            if(daysSpecsMap.containsKey(DestDate)){
                                Integer dayPosition = daysSpecsMap.get(DestDate);
                                daysList.get(dayPosition).dayEvents.add(calEventItem);
                            }
                        }
                        daysBulkState.put(sDay,BULK_STATE.LOADED);
                    }else{
                        daysBulkState.put(sDay,BULK_STATE.FAILED);
                    }
                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        public void run() {
                            mListManager.getListAdapter().notifyDataSetChanged();
                        }
                    });
                }
            });
        }
    }


    public void setItemEvents(LinearLayout parentView, ArrayList<CalenderEventModel> eventsList) {
        if (parentView != null) {
            //=========================== Set Upcoming Events =========================//
            if (eventsList.size() > 0) {

                // Set first item
                CalenderEventModel comingEventItem = eventsList.get(0);
                parentView.findViewById(R.id.cal_1_con).setVisibility(View.VISIBLE);
                ((TextView) parentView.findViewById(R.id.cal_1_title)).setText(comingEventItem.title);
                ImageView signIcon = ((ImageView) parentView.findViewById(R.id.cal_1_sign_icon));
                if (comingEventItem.url.contains("news")) {
                    signIcon.setImageResource(R.drawable.news);
                } else if (comingEventItem.url.contains("event")) {
                    signIcon.setImageResource(R.drawable.events);
                } else {
                    signIcon.setImageResource(R.drawable.date);
                }
                openCalendarItem((Activity) getContext(),parentView.findViewById(R.id.cal_1_con),comingEventItem.url);

                // Set second item
                if (eventsList.size() > 1) {
                    comingEventItem = eventsList.get(1);
                    parentView.findViewById(R.id.cal_2_con).setVisibility(View.VISIBLE);
                    ((TextView) parentView.findViewById(R.id.cal_2_title)).setText(comingEventItem.title);
                    signIcon = ((ImageView) parentView.findViewById(R.id.cal_2_sign_icon));
                    if (comingEventItem.url.contains("news")) {
                        signIcon.setImageResource(R.drawable.news);
                    } else if (comingEventItem.url.contains("event")) {
                        signIcon.setImageResource(R.drawable.events);
                    } else {
                        signIcon.setImageResource(R.drawable.date);
                    }
                    openCalendarItem((Activity) getContext(),parentView.findViewById(R.id.cal_2_con),comingEventItem.url);
                }else{
                    parentView.findViewById(R.id.cal_2_con).setVisibility(View.GONE);
                }


                // Set third item
                if (eventsList.size() > 2) {
                    comingEventItem = eventsList.get(2);
                    parentView.findViewById(R.id.cal_3_con).setVisibility(View.VISIBLE);
                    ((TextView) parentView.findViewById(R.id.cal_3_title)).setText(comingEventItem.title);
                    signIcon = ((ImageView) parentView.findViewById(R.id.cal_3_sign_icon));
                    if (comingEventItem.url.contains("news")) {
                        signIcon.setImageResource(R.drawable.news);
                    } else if (comingEventItem.url.contains("event")) {
                        signIcon.setImageResource(R.drawable.events);
                    } else {
                        signIcon.setImageResource(R.drawable.date);
                    }
                    openCalendarItem((Activity) getContext(),parentView.findViewById(R.id.cal_3_con),comingEventItem.url);
                }else{
                    parentView.findViewById(R.id.cal_3_con).setVisibility(View.GONE);
                }

                // Set fourth item
                if (eventsList.size() > 3) {
                    comingEventItem = eventsList.get(3);
                    parentView.findViewById(R.id.cal_4_con).setVisibility(View.VISIBLE);
                    ((TextView) parentView.findViewById(R.id.cal_4_title)).setText(comingEventItem.title);
                    signIcon = ((ImageView) parentView.findViewById(R.id.cal_4_sign_icon));
                    if (comingEventItem.url.contains("news")) {
                        signIcon.setImageResource(R.drawable.news);
                    } else if (comingEventItem.url.contains("event")) {
                        signIcon.setImageResource(R.drawable.events);
                    } else {
                        signIcon.setImageResource(R.drawable.date);
                    }
                    openCalendarItem((Activity) getContext(),parentView.findViewById(R.id.cal_4_con),comingEventItem.url);
                }else{
                    parentView.findViewById(R.id.cal_4_con).setVisibility(View.GONE);
                }

                // Set more items
                if(eventsList.size() > 4){
                    comingEventItem = eventsList.get(4);
                    parentView.findViewById(R.id.cal_more_con).setVisibility(View.VISIBLE);
                    parentView.findViewById(R.id.cal_more_con).setTag(comingEventItem.onlyDate);
                    parentView.findViewById(R.id.cal_more_con).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent MyIntent = new Intent(getContext(), CalenderItemPage.class);
                            MyIntent.putExtra("date_value", (String) v.getTag());
                            getContext().startActivity(MyIntent);
                            ((Activity)getContext()).overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                        }
                    });
                }else{
                    parentView.findViewById(R.id.cal_more_con).setVisibility(View.GONE);
                }

            }
        }
    }

    public static void openCalendarItem(final Activity mActivity, View clickOnView, String url) {
        if (url != null && !url.equals("")) {
            String itemID;
            if (url.contains("#events")) {
                itemID = url.replace("#events/", "");
                if (itemID != null && !itemID.equals("")) {
                    try {
                        clickOnView.setTag(Integer.parseInt(itemID));
                        clickOnView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent MyIntent = new Intent(mActivity, EventsViewPage.class);
                                MyIntent.putExtra("PageID", (Integer) v.getTag());
                                mActivity.startActivity(MyIntent);
                                mActivity.overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                            }
                        });
                    } catch (Exception e) {
                    }
                }
            }else if (url.contains("#newsboard")) {
                itemID = url.replace("#newsboard/", "");
                if (itemID != null && !itemID.equals("")) {
                    try {
                        clickOnView.setTag(Integer.parseInt(itemID));
                        clickOnView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent MyIntent = new Intent(mActivity, NewsViewPage.class);
                                MyIntent.putExtra("PageID", (Integer) v.getTag());
                                mActivity.startActivity(MyIntent);
                                mActivity.overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                            }
                        });
                    } catch (Exception e) {
                    }
                }
            }else if (url.contains("examsList")) {
                clickOnView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent MyIntent = new Intent(mActivity, ExamsPage.class);
                        mActivity.startActivity(MyIntent);
                        mActivity.overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    }
                });
            } else if (url.contains("onlineExams")) {
                clickOnView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent MyIntent = new Intent(mActivity, OnlineExamsPage.class);
                        mActivity.startActivity(MyIntent);
                        mActivity.overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    }
                });
            }
        }


    }
}