package com.schoolmgmtsys.root.ssg.app;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.schoolmgmtsys.root.ssg.utils.GlideUrlAdv;
import com.solutionsbricks.solbricksframework.OkHttpClient;

/**
 * Created by SolutionsBricks Mobile Dev. Team.
 */
public class MediaImageView extends ImageView {

    private String TOKEN;
    public String imageID;

    public MediaImageView(Context context) {
        super(context);
    }

    public MediaImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void loadMediaImage() {

        String url = App.getAppBaseUrl() + Constants.TASK_THUMB_LOAD + "/" + imageID;
        Glide.with(getContext())
                .load(new GlideUrlAdv(OkHttpClient.strip(url),url))
                .into(this);

    }
}
