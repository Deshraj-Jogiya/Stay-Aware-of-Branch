package com.schoolmgmtsys.root.ssg.app;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.schoolmgmtsys.root.ssg.models.CalenderEventModel;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.CalListView;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.schoolmgmtsys.root.ssg.utils.DrawerListFragment;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.helpers.ListManager;
import com.schoolmgmtsys.root.ssg.fonts.ParentStyledTextView;

import java.util.ArrayList;
import java.util.Iterator;

public class CalenderItemPage extends SlidingFragmentActivity implements ListManager.ListInterface {

    private ArrayList<CalenderEventModel> dayEvents = new ArrayList<>();
    private ListManager mListManager;
    private String dateValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Concurrent.getLangDirection(this).equals("ar"))
            getSlidingMenu().setMode(SlidingMenu.RIGHT);
        else getSlidingMenu().setMode(SlidingMenu.LEFT);

        setContentView(R.layout.page_calender_list);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            dateValue = extras.getString("date_value");
        }
        ImageView backImage = (ImageView) findViewById(R.id.background_img);
        RelativeLayout logBack = (RelativeLayout) findViewById(R.id.full_layout);

        Resources res = getResources();
        if (!res.getBoolean(R.bool.x_gen_back_is_image)) {
            backImage.setVisibility(View.GONE);
            logBack.setBackgroundColor(Concurrent.getColor(this, R.color.x_gen_back));
        }

        getSlidingMenu().setBehindWidth((int) Math.round(Concurrent.getScreenWidth(this) * 70.0 / 100.0));
        setBehindContentView(R.layout.drawer_frame);

        ListFragment mFrag;
        FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
        if (savedInstanceState == null) {
            mFrag = new DrawerListFragment();
        } else {
            mFrag = (ListFragment) this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
        }
        t.replace(R.id.menu_frame, mFrag);
        t.commit();

        TextView HeadTitle = (TextView) findViewById(R.id.head_drawer_title);
        HeadTitle.setText(dateValue);

        ImageView ToogleBtn = (ImageView) findViewById(R.id.head_drawer_toggle);
        ToogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        ListView viewList = (ListView) findViewById(R.id.calender_list);

        mListManager = new ListManager(this, viewList, this, dayEvents);
        mListManager.removeFooter();

        getEvents();

        findViewById(R.id.empty_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEvents();
            }
        });

        findViewById(R.id.error_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEvents();
            }
        });

        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEvents();
            }
        });
        findViewById(R.id.gen_loader).setVisibility(View.GONE);
    }

    public void getEvents(){
        if (dateValue != null) {
            changeCalView(calLayers.Loading);
            getEvents(dateValue);
        } else {
            changeCalView(calLayers.ErrorLoading);
        }
    }

    private enum calLayers {
        EmptyEvents,
        ErrorLoading,
        EventsData,
        Loading
    }

    public void changeCalView( calLayers layerIndex) {
        findViewById(R.id.empty_view).setVisibility(layerIndex.equals(calLayers.EmptyEvents) ? View.VISIBLE : View.GONE);
        findViewById(R.id.error_view).setVisibility(layerIndex.equals(calLayers.ErrorLoading) ? View.VISIBLE : View.GONE);
        findViewById(R.id.loading_view).setVisibility(layerIndex.equals(calLayers.Loading) ? View.VISIBLE : View.GONE);
        findViewById(R.id.calender_list).setVisibility(layerIndex.equals(calLayers.EventsData) ? View.VISIBLE : View.GONE);
    }


    public void getEvents(final String dateName) {
        String TOKEN = Concurrent.getAppToken(getBaseContext());
        if (TOKEN != null) {
            Ion.with(getBaseContext()).load(OkHttpClient.strip(App.getAppBaseUrl() + Constants.TASK_CALENDER + "?start=" + dateName + "&end=" + dateName + "")).setTimeout(10000)
                    .asJsonArray().setCallback(new FutureCallback<JsonArray>() {

                JsonObject CurrObj;

                @Override
                public void onCompleted(Exception exception, JsonArray ValuesArray) {
                    if (ValuesArray != null) {
                        if (ValuesArray.size() > 0) {
                            Iterator<JsonElement> ValsIter = ValuesArray.iterator();
                            CalenderEventModel calEventItem;

                            dayEvents.clear();
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

                                dayEvents.add(calEventItem);
                            }
                            changeCalView(calLayers.EventsData);
                            mListManager.getListAdapter().notifyDataSetChanged();
                        } else {
                            changeCalView(calLayers.EmptyEvents);
                        }
                    } else {
                        changeCalView(calLayers.ErrorLoading);
                    }
                }
            });
        }
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
        ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.page_calender_item, null);

            holder.allDayView = (ParentStyledTextView) convertView.findViewById(R.id.header_all_day);
            holder.TitleView = (ParentStyledTextView) convertView.findViewById(R.id.header_title);
            holder.CardHolder = (CardView) convertView.findViewById(R.id.card);



            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        CalenderEventModel posValues = dayEvents.get(position);

        if (posValues != null) {
            if (posValues.allDay.equals("true")) holder.allDayView.setText("All Day");
            else holder.allDayView.setVisibility(View.GONE);
            holder.TitleView.setNotNullText(posValues.title);

            CalListView.openCalendarItem(CalenderItemPage.this,holder.CardHolder,posValues.url);

        }

        return convertView;
    }

    class ViewHolder {
        ParentStyledTextView allDayView;
        ParentStyledTextView TitleView;
        CardView CardHolder;
    }
}
