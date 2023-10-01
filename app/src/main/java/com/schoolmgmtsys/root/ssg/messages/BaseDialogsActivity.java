package com.schoolmgmtsys.root.ssg.messages;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.schoolmgmtsys.root.ssg.utils.GlideUrlAdv;
import com.solutionsbricks.solbricksframework.OkHttpClient;
import com.solutionsbricks.solbricksframework.messages.model.Dialog;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;



public abstract class BaseDialogsActivity extends AppCompatActivity
        implements DialogsListAdapter.OnDialogClickListener<Dialog>,
        DialogsListAdapter.OnDialogLongClickListener<Dialog> {

    protected ImageLoader imageLoader;
    protected DialogsListAdapter<Dialog> dialogsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(final ImageView imageView, final String url) {
                Glide.with(getBaseContext())
                        .load(new GlideUrlAdv(OkHttpClient.strip(url),url))
                        .into(imageView);
            }
        };

    }

    @Override
    public void onDialogLongClick(Dialog dialog) {

    }
}
