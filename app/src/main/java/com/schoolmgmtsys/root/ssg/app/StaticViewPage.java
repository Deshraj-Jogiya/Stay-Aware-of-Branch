package com.schoolmgmtsys.root.ssg.app;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.schoolmgmtsys.root.ssg.models.StaticPagesModel;
import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.fonts.ParentStyledTextView;

import im.delight.android.webview.AdvancedWebView;

public class StaticViewPage extends FragmentActivity implements AdvancedWebView.Listener {


    private AdvancedWebView Content;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_static_view);

        findViewById(R.id.gen_loader).setVisibility(View.GONE);

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.fragment_content);
        ImageView contentBackImage = (ImageView) findViewById(R.id.background_img);


        Resources res = getResources();
        if (!res.getBoolean(R.bool.x_gen_back_is_image)) {
            contentBackImage.setVisibility(View.GONE);
            contentLayout.setBackgroundColor(Concurrent.getColor(this, R.color.x_gen_back));
        }

        ParentStyledTextView Title = (ParentStyledTextView) findViewById(R.id.header_title);
        Content = (AdvancedWebView) findViewById(R.id.content_view);
        Content.setMixedContentAllowed(false);
        Content.setDesktopMode(false);

        ((TextView) findViewById(R.id.head_drawer_title)).setText(Concurrent.getLangSubWords("staticPages", "Static Pages"));

        StaticPagesModel sPage = getIntent().getParcelableExtra("EDIT_OBJECT");
        if (sPage != null) {
            Title.setNotNullText(sPage.title);
            Content.loadHtml(sPage.content);
        }

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
