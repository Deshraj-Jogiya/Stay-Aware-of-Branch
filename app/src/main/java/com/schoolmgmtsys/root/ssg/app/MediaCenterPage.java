package com.schoolmgmtsys.root.ssg.app;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.schoolmgmtsys.root.ssg.models.AlbumsModel;
import com.schoolmgmtsys.root.ssg.models.FragmentMakerModel;
import com.schoolmgmtsys.root.ssg.models.PhotoModel;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.DrawerListFragment;
import com.solutionsbricks.solbricksframework.Call;
import com.solutionsbricks.solbricksframework.Callback;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.Request;
import com.solutionsbricks.solbricksframework.Response;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import io.karim.MaterialTabs;

public class MediaCenterPage extends SlidingFragmentActivity {


    private Integer Res_PageLayout = R.layout.page_media;
    private ProgressBar mProgressBar;
    private Integer AlbumID;
    private ArrayList<FragmentMakerModel> TabsData = new ArrayList();
    private String response;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Concurrent.getLangDirection(this).equals("ar"))
            getSlidingMenu().setMode(SlidingMenu.RIGHT);
        else getSlidingMenu().setMode(SlidingMenu.LEFT);

        setContentView(Res_PageLayout);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            AlbumID = extras.getInt("Album_ID", 0);
        }

        getSlidingMenu().setBehindWidth((int) Math.round(Concurrent.getScreenWidth(this) * 70.0 / 100.0));
        setBehindContentView(R.layout.drawer_frame);

        ImageView backImage = (ImageView) findViewById(R.id.background_img);
        FrameLayout logBack = (FrameLayout) findViewById(R.id.full_layout);

        Resources res = getResources();
        if (!res.getBoolean(R.bool.x_gen_back_is_image)) {
            backImage.setVisibility(View.GONE);
            logBack.setBackgroundColor(Concurrent.getColor(this, R.color.x_gen_back));
        }

        mProgressBar = (ProgressBar) findViewById(R.id.gen_loader);
        findViewById(R.id.refresh).setVisibility(View.GONE);


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
        HeadTitle.setText(Concurrent.getLangSubWords("mediaCenter", "Media Center"));

        ImageView ToogleBtn = (ImageView) findViewById(R.id.head_drawer_toggle);
        ToogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        mProgressBar.setVisibility(View.INVISIBLE);


        findViewById(R.id.error_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData(AlbumID);
            }
        });
        findViewById(R.id.empty_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData(AlbumID);
            }
        });
        loadData(AlbumID);
    }


    public void setNoDataView() {
        findViewById(R.id.main_media).setVisibility(View.INVISIBLE);
        findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
        findViewById(R.id.error_view).setVisibility(View.INVISIBLE);

        mProgressBar.setVisibility(View.INVISIBLE);
    }

    public void setDataView() {
        findViewById(R.id.main_media).setVisibility(View.VISIBLE);
        findViewById(R.id.empty_view).setVisibility(View.INVISIBLE);
        findViewById(R.id.error_view).setVisibility(View.INVISIBLE);

        mProgressBar.setVisibility(View.INVISIBLE);
    }

    public void setErrorView() {
        findViewById(R.id.main_media).setVisibility(View.INVISIBLE);
        findViewById(R.id.empty_view).setVisibility(View.INVISIBLE);
        findViewById(R.id.error_view).setVisibility(View.VISIBLE);

        mProgressBar.setVisibility(View.INVISIBLE);
    }


    private class TabsAdapter extends FragmentPagerAdapter {
        TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TabsData.get(position).FragmentTitleShowedOnTabs;
        }

        @Override
        public int getCount() {
            return TabsData.size();
        }

        @Override
        public Fragment getItem(int position) {
            return (Fragment) TabsData.get(position).FragmentObj;
        }
    }

    public void loadData(final Integer albumID) {
        String TOKEN = Concurrent.getAppToken(MediaCenterPage.this);
        if (TOKEN != null) {
            mProgressBar.setVisibility(View.VISIBLE);

            String Url;
            if (albumID != null && albumID != 0) {
                Url = App.getAppBaseUrl() + Constants.TASK_MEDIA_CENTER_LIST + "/" + albumID;
            } else {
                Url = App.getAppBaseUrl() + Constants.TASK_MEDIA_CENTER_LIST;
            }

            OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();

            Request.Builder requestBuilder = new Request.Builder()
                    .url(Url);

            requestBuilder.get();

            Request request = requestBuilder.build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            setErrorView();
                            mProgressBar.setVisibility(View.INVISIBLE);
                            if (e instanceof ConnectException) {
                                Toast.makeText(getBaseContext(), Concurrent.getLangSubWords("noConnection", "No Internet Connection"), Toast.LENGTH_LONG).show();
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
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    if (responseObj.isSuccessful()) {

                                        JsonParser parser = new JsonParser();
                                        JsonObject ValuesHolder = null;
                                        JsonArray AlbumsValuesArray;
                                        JsonArray MediaValuesArray;

                                        try {
                                            ValuesHolder = parser.parse(response).getAsJsonObject();
                                        } catch (Exception e) {
                                            showError("5001");
                                        }

                                        if(ValuesHolder == null){
                                            setErrorView();
                                            return;
                                        }

                                        AlbumsValuesArray = ValuesHolder.getAsJsonArray("albums");
                                        MediaValuesArray = ValuesHolder.getAsJsonArray("media");

                                        if (AlbumsValuesArray.size() == 0 && MediaValuesArray.size() == 0) {
                                            setNoDataView();
                                            return;
                                        }
                                        ArrayList<AlbumsModel> AlbumsList = new ArrayList<>();
                                        ArrayList<PhotoModel> MediaList = new ArrayList<>();

                                        JsonObject CurrObj;
                                        if (AlbumsValuesArray.size() > 0) {
                                            for (JsonElement aAlbumsValuesArray : AlbumsValuesArray) {
                                                CurrObj = aAlbumsValuesArray.getAsJsonObject();
                                                AlbumsList.add(new AlbumsModel(Concurrent.tagsStringValidator(CurrObj, "id"),
                                                        Concurrent.tagsStringValidator(CurrObj, "albumTitle"),
                                                        Concurrent.tagsStringValidator(CurrObj, "albumDescription"),
                                                        Concurrent.tagsStringValidator(CurrObj, "albumImage"),
                                                        Concurrent.tagsStringValidator(CurrObj, "albumParent")));
                                            }
                                            setDataView();
                                        }


                                        if (MediaValuesArray.size() > 0) {
                                            Iterator<JsonElement> ValuesIterator = MediaValuesArray.iterator();
                                            while (ValuesIterator.hasNext()) {
                                                CurrObj = ValuesIterator.next().getAsJsonObject();
                                                MediaList.add(new PhotoModel(Concurrent.tagsStringValidator(CurrObj, "id"),
                                                        Concurrent.tagsStringValidator(CurrObj, "albumId"),
                                                        Concurrent.tagsStringValidator(CurrObj, "mediaType"),
                                                        Concurrent.tagsStringValidator(CurrObj, "mediaURL"),
                                                        Concurrent.tagsStringValidator(CurrObj, "mediaURLThumb"),
                                                        Concurrent.tagsStringValidator(CurrObj, "mediaTitle"),
                                                        Concurrent.tagsStringValidator(CurrObj, "mediaDescription"),
                                                        Concurrent.tagsStringValidator(CurrObj, "mediaDate")));
                                            }
                                            setDataView();
                                        }

                                        createTabs(AlbumsList, MediaList);

                                        mProgressBar.setVisibility(View.INVISIBLE);
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
        }
    }
    public void showError(String errorCode) {
        String errorTitle;
        if (errorCode != null) {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: " + errorCode + " )";
        } else {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred");
        }
        setErrorView();
        mProgressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(getBaseContext(), errorTitle, Toast.LENGTH_LONG).show();
    }

    public void createTabs(ArrayList albumsArrayList, ArrayList mediaArrayList) {
        ViewPager mViewPager = (ViewPager) findViewById(R.id.view_pager);

        TabsData.add(new FragmentMakerModel("Albums", MediaCenterFragment.newInstance(1, "Albums", albumsArrayList),Concurrent.getLangSubWords("albums","Albums")));
        TabsData.add(new FragmentMakerModel("Media", MediaCenterFragment.newInstance(2, "Media", mediaArrayList),Concurrent.getLangSubWords("media","Media")));

        TabsAdapter adapter = new TabsAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        ((MaterialTabs) findViewById(R.id.material_tabs)).setViewPager(mViewPager);
    }

}
