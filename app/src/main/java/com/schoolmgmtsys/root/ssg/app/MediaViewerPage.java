package com.schoolmgmtsys.root.ssg.app;

import android.app.Activity;
import android.os.Bundle;

import com.schoolmgmtsys.root.ssg.utils.Concurrent;
import com.schoolmgmtsys.root.ssg.R;
import java.util.ArrayList;
import java.util.List;
import ru.truba.touchgallery.GalleryWidget.BasePagerAdapter;
import ru.truba.touchgallery.GalleryWidget.GalleryViewPager;
import ru.truba.touchgallery.GalleryWidget.UrlPagerAdapter;

public class MediaViewerPage extends Activity {

    private GalleryViewPager mViewPager;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_viewer_page);
        String imageLink = getIntent().getStringExtra("Image_Link");

        if(imageLink != null){
            List<String> items = new ArrayList<String>();
            items.add(imageLink);

            UrlPagerAdapter pagerAdapter = new UrlPagerAdapter(this, items);
            pagerAdapter.setOnItemChangeListener(new BasePagerAdapter.OnItemChangeListener()
            {
                @Override
                public void onItemChange(int currentPosition) {}
            });

            mViewPager = (GalleryViewPager)findViewById(R.id.viewer);
            GalleryViewPager.mToken = Concurrent.getAppToken(this);
            mViewPager.setOffscreenPageLimit(3);
            mViewPager.setAdapter(pagerAdapter);
        }


    }

}