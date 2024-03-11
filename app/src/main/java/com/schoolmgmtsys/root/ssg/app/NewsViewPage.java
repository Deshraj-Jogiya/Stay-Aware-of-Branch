package com.schoolmgmtsys.root.ssg.app;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.schoolmgmtsys.root.ssg.models.NewsBoardModel;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.schoolmgmtsys.root.ssg.fonts.ParentStyledTextView;
import com.solutionsbricks.solbricksframework.Call;
import com.solutionsbricks.solbricksframework.Callback;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.Request;
import com.solutionsbricks.solbricksframework.Response;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

import im.delight.android.webview.AdvancedWebView;

public class NewsViewPage extends FragmentActivity implements AdvancedWebView.Listener{


    private ParentStyledTextView Title;
    private AdvancedWebView Content;
    private ParentStyledTextView DateField;
    private String response;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_news_view);

        findViewById(R.id.gen_loader).setVisibility(View.GONE);
        findViewById(R.id.head_drawer_toggle).setVisibility(View.GONE);

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.fragment_content);
        ImageView contentBackImage = (ImageView) findViewById(R.id.background_img);


        Resources res = getResources();
        if (!res.getBoolean(R.bool.x_gen_back_is_image)) {
            contentBackImage.setVisibility(View.GONE);
            contentLayout.setBackgroundColor(Concurrent.getColor(this, R.color.x_gen_back));
        }
        ((TextView) findViewById(R.id.head_drawer_title)).setText(Concurrent.getLangSubWords("newsboard", "News Board"));

        Title = (ParentStyledTextView) findViewById(R.id.header_title);
        DateField = (ParentStyledTextView) findViewById(R.id.header_date);

        Content = (AdvancedWebView) findViewById(R.id.content_view);
        Content.setMixedContentAllowed(false);
        Content.setDesktopMode(false);

        final int pageID = getIntent().getIntExtra("PageID", 0);

        loadNewsItem(pageID);


        findViewById(R.id.error_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pageID != 0)loadNewsItem(pageID);
            }
        });

    }

    private enum pageLayer {
        ErrorLoading,
        DataView,
        Loading
    }

    public void changePageView(pageLayer layerIndex) {
        findViewById(R.id.error_view).setVisibility(layerIndex.equals(pageLayer.ErrorLoading) ? View.VISIBLE : View.GONE);
        findViewById(R.id.loading_view).setVisibility(layerIndex.equals(pageLayer.Loading) ? View.VISIBLE : View.GONE);
        findViewById(R.id.fragment_content).setVisibility(layerIndex.equals(pageLayer.DataView) ? View.VISIBLE : View.GONE);
    }

    public void loadNewsItem(Integer id){
        if(id == 0){
            changePageView(pageLayer.ErrorLoading);
            return;
        }

        String TOKEN = Concurrent.getAppToken(getBaseContext());
        if (TOKEN != null) {
            changePageView(pageLayer.Loading);


            OkHttpClient client = new OkHttpClient().newBuilder(getBaseContext()).connectTimeout(7, TimeUnit.SECONDS).build();

            Request.Builder requestBuilder = new Request.Builder()
                    .url(App.getAppBaseUrl() + Constants.TASK_NEWS_ITEM + "/" + id);

            requestBuilder.get();

            Request request = requestBuilder.build();


            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            changePageView(pageLayer.ErrorLoading);
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

                                        try {
                                            ValuesHolder = parser.parse(response).getAsJsonObject();
                                        } catch (Exception e) {
                                            showError("5001");
                                        }

                                        if(ValuesHolder != null){
                                            changePageView(pageLayer.DataView);
                                            NewsBoardModel pageItem = new NewsBoardModel(Concurrent.tagsIntValidator(ValuesHolder, "id"),
                                                    Concurrent.tagsStringValidator(ValuesHolder, "newsTitle"),
                                                    Concurrent.tagsStringValidator(ValuesHolder, "newsText"),
                                                    Concurrent.tagsStringValidator(ValuesHolder, "newsFor"),
                                                    Concurrent.tagsStringValidator(ValuesHolder, "newsDate"));

                                            Title.setNotNullText(pageItem.title);
                                            Content.loadHtml(pageItem.content);
                                            DateField.setNotNullText(pageItem.date);
                                        }else{
                                            changePageView(pageLayer.ErrorLoading);
                                            showError("5001");
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
        }

    }

    public void showError(String errorCode) {
        String errorTitle;
        if (errorCode != null) {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred") + " ( Error Code: " + errorCode + " )";
        } else {
            errorTitle = Concurrent.getLangSubWords("errorOccurred", "Error Occurred");
        }
        changePageView(pageLayer.ErrorLoading);
        Toast.makeText(getBaseContext(), errorTitle, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        if (!Content.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
    @Override
    public void onResume() {
        super.onResume();
        Content.onResume();
    }

    @Override
    public void onPause() {
        Content.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Content.onDestroy();
        super.onDestroy();
    }


    @Override
    public void onPageStarted(String url, Bitmap favicon) {

    }

    @Override
    public void onPageFinished(String url) {

    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {

    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {

    }

    @Override
    public void onExternalPageRequest(String url) {

    }


}
