package com.schoolmgmtsys.root.ssg.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.solbricks.solbrickscal.TodayDetector;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.models.CalenderModel;
import com.schoolmgmtsys.root.ssg.models.DashNewsModel;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.CalListView;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.solutionsbricks.solbricksframework.OkHttpClient;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by SolutionsBricks Mobile Dev. Team.
 */

public class DashboardNewsFrag extends Fragment implements TodayDetector.TodayDetectorInterface {

    public static String dashboardJsonDataArg = "dashboard_json_data";
    private JsonObject dashboardJsonData;
    private ArrayList<DashNewsModel> NewsEvents = new ArrayList<>();
    private ArrayList<CalenderModel> upcomingEvents = new ArrayList<>();
    private View parentView;

    private int UPCOMING_DATES_LOADED = 1;
    private int UPCOMING_DATES_FAILED = 2;

    private int UPCOMING_DATES_STATE = -1;

    public static DashboardNewsFrag getInstance(JsonObject mJsonData) {
        DashboardNewsFrag frag = new DashboardNewsFrag();
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

        JsonArray NewsEventsJson = Concurrent.getJsonArray(dashboardJsonData, "newsEvents");
        JsonObject CurrObj;
        if (NewsEventsJson != null) {
            Iterator<JsonElement> ValsIter = NewsEventsJson.iterator();
            while (ValsIter.hasNext()) {
                CurrObj = ValsIter.next().getAsJsonObject();
                NewsEvents.add(new DashNewsModel(Concurrent.tagsIntValidator(CurrObj, "id"),
                        Concurrent.tagsStringValidator(CurrObj, "title"),
                        Concurrent.tagsStringValidator(CurrObj, "type"),
                        Concurrent.tagsStringValidator(CurrObj, "start"),
                        Concurrent.tagsStringValidator(CurrObj, "type").equals("news")
                ));
            }
        }

        //Setup initial text
        if (Concurrent.TDate != null) {
            loadUpcomingEvents(Concurrent.TDate, Concurrent.TDate);
        } else {
            TodayDetector mTDay = new TodayDetector(DashboardNewsFrag.this);
            mTDay.getToday(getContext());
            UPCOMING_DATES_STATE = UPCOMING_DATES_FAILED;
            setUpcomingEvents();
        }
    }

    public void loadUpcomingEvents(final String start, final String end) {
        String TOKEN = Concurrent.getAppToken(getContext());
        if (TOKEN != null) {

            loadingTodayEvents();
            Ion.with(getContext()).load(OkHttpClient.strip(App.getAppBaseUrl() + Constants.TASK_CALENDER + "?start=" + start + "&end=" + end + "")).setTimeout(10000)
                    .asJsonArray().setCallback(new FutureCallback<JsonArray>() {

                public JsonObject CurrObj;

                @Override
                public void onCompleted(Exception exception, JsonArray ValuesArray) {
                    if (ValuesArray != null) {
                        Iterator<JsonElement> ValsIter = ValuesArray.iterator();
                        while (ValsIter.hasNext()) {
                            CurrObj = ValsIter.next().getAsJsonObject();
                            String CompleteDateTime = Concurrent.tagsStringValidator(CurrObj, "start");
                            String onlyDate = CompleteDateTime.substring(0, Math.min(CompleteDateTime.length(), 10));
                            CalenderModel item = new CalenderModel(Concurrent.tagsStringValidator(CurrObj, "id"), Concurrent.tagsStringValidator(CurrObj, "title"), Concurrent.tagsStringValidator(CurrObj, "start"), Concurrent.tagsStringValidator(CurrObj, "backgroundColor"), Concurrent.tagsStringValidator(CurrObj, "url"), Concurrent.tagsStringValidator(CurrObj, "allDay"));
                            item.onlyDate = onlyDate;
                            upcomingEvents.add(item);
                        }
                        UPCOMING_DATES_STATE = UPCOMING_DATES_LOADED;
                    } else {
                        UPCOMING_DATES_STATE = UPCOMING_DATES_FAILED;
                    }
                    setUpcomingEvents();
                }
            });
        }
    }

    public void setUpcomingEvents() {
        if (parentView != null && UPCOMING_DATES_STATE != -1) {
            //=========================== Set Upcoming Events =========================//
            if (UPCOMING_DATES_STATE == UPCOMING_DATES_LOADED) {
                if (upcomingEvents.size() > 0) {
                    todayEventsLoaded();

                    // Set first item
                    CalenderModel comingEventItem = upcomingEvents.get(0);
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
                    CalListView.openCalendarItem(getActivity(), parentView.findViewById(R.id.cal_1_con), comingEventItem.url);

                    // Set second item
                    if (upcomingEvents.size() > 1) {
                        comingEventItem = upcomingEvents.get(1);
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
                        CalListView.openCalendarItem(getActivity(), parentView.findViewById(R.id.cal_2_con), comingEventItem.url);
                    }


                    // Set third item
                    if (upcomingEvents.size() > 2) {
                        comingEventItem = upcomingEvents.get(2);
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
                        CalListView.openCalendarItem(getActivity(), parentView.findViewById(R.id.cal_3_con), comingEventItem.url);
                    }

                    // Set fourth item
                    if (upcomingEvents.size() > 3) {
                        comingEventItem = upcomingEvents.get(3);
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
                        CalListView.openCalendarItem(getActivity(), parentView.findViewById(R.id.cal_4_con), comingEventItem.url);
                    }

                    // Set more items
                    if (upcomingEvents.size() > 4) {
                        parentView.findViewById(R.id.cal_more_con).setVisibility(View.VISIBLE);
                        parentView.findViewById(R.id.cal_more_con).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent MyIntent = new Intent(getContext(), CalenderPage.class);
                                startActivity(MyIntent);
                                getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);

                            }
                        });
                        ((TextView) parentView.findViewById(R.id.cal_more_count)).setText("+" + (upcomingEvents.size() - 4));

                    } else {
                        parentView.findViewById(R.id.cal_more_con).setVisibility(View.GONE);
                    }
                } else {
                    noTodayEvents();
                }
            } else {
                errorLoadingTodayEvents();
            }
        }
    }

    public void noTodayEvents() {
        if (parentView != null) {
            parentView.findViewById(R.id.no_toady_dates_con).setVisibility(View.VISIBLE);
            parentView.findViewById(R.id.today_dates_con).setVisibility(View.GONE);
            parentView.findViewById(R.id.error_loading_toady_dates_con).setVisibility(View.GONE);
            parentView.findViewById(R.id.loading_toady_dates_con).setVisibility(View.GONE);

            View.OnClickListener reLoadToadyEvents = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Load today dates
                    if (Concurrent.TDate == null) {
                        loadingTodayEvents();
                        TodayDetector mTDay = new TodayDetector(DashboardNewsFrag.this);
                        mTDay.getToday(getContext());
                    } else {
                        loadUpcomingEvents(Concurrent.TDate, Concurrent.TDate);
                    }
                }
            };

            parentView.findViewById(R.id.error_loading_toady_dates_con).setOnClickListener(reLoadToadyEvents);
            parentView.findViewById(R.id.no_toady_dates_con).setOnClickListener(reLoadToadyEvents);
        }
    }

    public void errorLoadingTodayEvents() {
        if (parentView != null) {
            parentView.findViewById(R.id.error_loading_toady_dates_con).setVisibility(View.VISIBLE);
            parentView.findViewById(R.id.today_dates_con).setVisibility(View.GONE);
            parentView.findViewById(R.id.no_toady_dates_con).setVisibility(View.GONE);
            parentView.findViewById(R.id.loading_toady_dates_con).setVisibility(View.GONE);

            View.OnClickListener reLoadToadyEvents = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Load today dates
                    if (Concurrent.TDate == null) {
                        loadingTodayEvents();
                        TodayDetector mTDay = new TodayDetector(DashboardNewsFrag.this);
                        mTDay.getToday(getContext());
                    } else {
                        loadUpcomingEvents(Concurrent.TDate, Concurrent.TDate);
                    }
                }
            };

            parentView.findViewById(R.id.error_loading_toady_dates_con).setOnClickListener(reLoadToadyEvents);
            parentView.findViewById(R.id.no_toady_dates_con).setOnClickListener(reLoadToadyEvents);
        }
    }

    public void todayEventsLoaded() {
        if (parentView != null) {
            parentView.findViewById(R.id.error_loading_toady_dates_con).setVisibility(View.GONE);
            parentView.findViewById(R.id.today_dates_con).setVisibility(View.VISIBLE);
            parentView.findViewById(R.id.no_toady_dates_con).setVisibility(View.GONE);
            parentView.findViewById(R.id.loading_toady_dates_con).setVisibility(View.GONE);

        }
    }

    public void loadingTodayEvents() {
        if (parentView != null) {
            parentView.findViewById(R.id.error_loading_toady_dates_con).setVisibility(View.GONE);
            parentView.findViewById(R.id.today_dates_con).setVisibility(View.GONE);
            parentView.findViewById(R.id.no_toady_dates_con).setVisibility(View.GONE);
            parentView.findViewById(R.id.loading_toady_dates_con).setVisibility(View.VISIBLE);
        }
    }

    public void noNewsEvents() {
        if (parentView != null) {
            parentView.findViewById(R.id.no_news_con).setVisibility(View.VISIBLE);
            parentView.findViewById(R.id.news_con).setVisibility(View.GONE);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.dashboard_news_frag, null);

        //=========================== Set News - Events =========================//
        if (NewsEvents.size() > 0) {
            // Set first item
            DashNewsModel newsItem = NewsEvents.get(0);
            parentView.findViewById(R.id.news_1_con).setVisibility(View.VISIBLE);
            ((TextView) parentView.findViewById(R.id.news_1_title)).setText(newsItem.Title);
            ((TextView) parentView.findViewById(R.id.news_1_date)).setText(newsItem.Start);
            ImageView signIcon = ((ImageView) parentView.findViewById(R.id.news_1_sign_icon));
            if (newsItem.isNews) {
                signIcon.setImageResource(R.drawable.news);
            } else {
                signIcon.setImageResource(R.drawable.events);
            }

            //====== Set click listener ================//
            if (newsItem.Type != null && (newsItem.Type.equals("news") || newsItem.Type.equals("event"))) {
                parentView.findViewById(R.id.news_1_con).setTag(newsItem);
                parentView.findViewById(R.id.news_1_con).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DashNewsModel newsItem = (DashNewsModel) v.getTag();
                        openNewsEventItem(newsItem.Type.equals("news"), newsItem.id);
                    }
                });
            }

            // Set second item
            if (NewsEvents.size() > 1) {
                newsItem = NewsEvents.get(1);
                parentView.findViewById(R.id.news_2_con).setVisibility(View.VISIBLE);
                ((TextView) parentView.findViewById(R.id.news_2_title)).setText(newsItem.Title);
                ((TextView) parentView.findViewById(R.id.news_2_date)).setText(newsItem.Start);
                signIcon = ((ImageView) parentView.findViewById(R.id.news_2_sign_icon));
                if (newsItem.isNews) {
                    signIcon.setImageResource(R.drawable.news);
                } else {
                    signIcon.setImageResource(R.drawable.events);
                }

                //====== Set click listener ================//
                if (newsItem.Type != null && (newsItem.Type.equals("news") || newsItem.Type.equals("event"))) {
                    parentView.findViewById(R.id.news_2_con).setTag(newsItem);
                    parentView.findViewById(R.id.news_2_con).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DashNewsModel newsItem = (DashNewsModel) v.getTag();
                            openNewsEventItem(newsItem.Type.equals("news"), newsItem.id);
                        }
                    });
                }
            }


            // Set third item
            if (NewsEvents.size() > 2) {
                newsItem = NewsEvents.get(2);
                parentView.findViewById(R.id.news_3_con).setVisibility(View.VISIBLE);
                ((TextView) parentView.findViewById(R.id.news_3_title)).setText(newsItem.Title);
                ((TextView) parentView.findViewById(R.id.news_3_date)).setText(newsItem.Start);
                signIcon = ((ImageView) parentView.findViewById(R.id.news_3_sign_icon));
                if (newsItem.isNews) {
                    signIcon.setImageResource(R.drawable.news);
                } else {
                    signIcon.setImageResource(R.drawable.events);
                }

                //====== Set click listener ================//
                if (newsItem.Type != null && (newsItem.Type.equals("news") || newsItem.Type.equals("event"))) {
                    parentView.findViewById(R.id.news_3_con).setTag(newsItem);
                    parentView.findViewById(R.id.news_3_con).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DashNewsModel newsItem = (DashNewsModel) v.getTag();
                            openNewsEventItem(newsItem.Type.equals("news"), newsItem.id);
                        }
                    });
                }


            }

            // Set fourth item
            if (NewsEvents.size() > 3) {
                newsItem = NewsEvents.get(3);
                parentView.findViewById(R.id.news_4_con).setVisibility(View.VISIBLE);
                ((TextView) parentView.findViewById(R.id.news_4_title)).setText(newsItem.Title);
                ((TextView) parentView.findViewById(R.id.news_4_date)).setText(newsItem.Start);
                signIcon = ((ImageView) parentView.findViewById(R.id.news_4_sign_icon));
                if (newsItem.isNews) {
                    signIcon.setImageResource(R.drawable.news);
                } else {
                    signIcon.setImageResource(R.drawable.events);
                }

                //====== Set click listener ================//
                if (newsItem.Type != null && (newsItem.Type.equals("news") || newsItem.Type.equals("event"))) {
                    parentView.findViewById(R.id.news_4_con).setTag(newsItem);
                    parentView.findViewById(R.id.news_4_con).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DashNewsModel newsItem = (DashNewsModel) v.getTag();
                            openNewsEventItem(newsItem.Type.equals("news"), newsItem.id);
                        }
                    });
                }


            }

            // Set fifth item
            if (NewsEvents.size() > 4) {
                newsItem = NewsEvents.get(4);
                parentView.findViewById(R.id.news_5_con).setVisibility(View.VISIBLE);
                ((TextView) parentView.findViewById(R.id.news_5_title)).setText(newsItem.Title);
                ((TextView) parentView.findViewById(R.id.news_5_date)).setText(newsItem.Start);
                signIcon = ((ImageView) parentView.findViewById(R.id.news_5_sign_icon));
                if (newsItem.isNews) {
                    signIcon.setImageResource(R.drawable.news);
                } else {
                    signIcon.setImageResource(R.drawable.events);
                }

                //====== Set click listener ================//
                if (newsItem.Type != null && (newsItem.Type.equals("news") || newsItem.Type.equals("event"))) {
                    parentView.findViewById(R.id.news_5_con).setTag(newsItem);
                    parentView.findViewById(R.id.news_5_con).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DashNewsModel newsItem = (DashNewsModel) v.getTag();
                            openNewsEventItem(newsItem.Type.equals("news"), newsItem.id);
                        }
                    });
                }


            }

            // Set sixth item
            if (NewsEvents.size() > 5) {
                newsItem = NewsEvents.get(5);
                parentView.findViewById(R.id.news_6_con).setVisibility(View.VISIBLE);
                ((TextView) parentView.findViewById(R.id.news_6_title)).setText(newsItem.Title);
                ((TextView) parentView.findViewById(R.id.news_6_date)).setText(newsItem.Start);
                signIcon = ((ImageView) parentView.findViewById(R.id.news_6_sign_icon));
                if (newsItem.isNews) {
                    signIcon.setImageResource(R.drawable.news);
                } else {
                    signIcon.setImageResource(R.drawable.events);
                }

                //====== Set click listener ================//
                if (newsItem.Type != null && (newsItem.Type.equals("news") || newsItem.Type.equals("event"))) {
                    parentView.findViewById(R.id.news_6_con).setTag(newsItem);
                    parentView.findViewById(R.id.news_6_con).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DashNewsModel newsItem = (DashNewsModel) v.getTag();
                            openNewsEventItem(newsItem.Type.equals("news"), newsItem.id);
                        }
                    });
                }


            }

            // Set more items
            parentView.findViewById(R.id.news_see_more_con).setVisibility(View.VISIBLE);
            parentView.findViewById(R.id.news_see_more_con).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent MyIntent = new Intent(getContext(), NewsBoardPage.class);
                    startActivity(MyIntent);
                    getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);

                }
            });
            parentView.findViewById(R.id.events_see_more_con).setVisibility(View.VISIBLE);
            parentView.findViewById(R.id.events_see_more_con).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent MyIntent = new Intent(getContext(), EventsPage.class);
                    startActivity(MyIntent);
                    getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            });


        } else {
            noNewsEvents();
        }

        //=========================== Set upcoming events if ready =========================//


        setUpcomingEvents();

        // Set more calendar items
        parentView.findViewById(R.id.calendar_see_more_con).setVisibility(View.VISIBLE);
        parentView.findViewById(R.id.calendar_see_more_con).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent MyIntent = new Intent(getContext(), CalenderPage.class);
                startActivity(MyIntent);
                getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);

            }
        });


        return parentView;
    }


    public void openNewsEventItem(Boolean isNews, Integer ID) {
        if (isNews) {
            Intent MyIntent = new Intent(getContext(), NewsViewPage.class);
            MyIntent.putExtra("PageID", ID);
            startActivity(MyIntent);
            getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        } else {
            Intent MyIntent = new Intent(getContext(), EventsViewPage.class);
            MyIntent.putExtra("PageID", ID);
            startActivity(MyIntent);
            getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }
    }

    @Override
    public void getTodayDate(String today) {
        Concurrent.TDate = today;
        loadUpcomingEvents(Concurrent.TDate, Concurrent.TDate);
    }
}