package com.schoolmgmtsys.root.ssg.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.solutionsbricks.solbricksframework.CardPager.CardAdapter;
import com.solutionsbricks.solbricksframework.CardPager.CardItem;
import com.schoolmgmtsys.root.ssg.utils.App;
import com.schoolmgmtsys.root.ssg.utils.Constants;
import com.schoolmgmtsys.root.ssg.R;
import java.util.ArrayList;
import java.util.List;

public class MediaCenterItemAdapter extends PagerAdapter implements CardAdapter {

    private final Activity mActivity;
    private List<CardView> mViews;
    private List<CardItem> mData;
    private float mBaseElevation;

    public MediaCenterItemAdapter(Activity mActivity) {
        mData = new ArrayList<>();
        mViews = new ArrayList<>();
        this.mActivity = mActivity;
    }

    public void addCardItem(CardItem item) {
        mViews.add(null);
        mData.add(item);
    }

    public float getBaseElevation() {
        return mBaseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return mViews.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.layout_media_fragment_item, container, false);
        container.addView(view);
        bind(mData.get(position), view);
        CardView cardView = (CardView) view.findViewById(R.id.cardView);

        if (mBaseElevation == 0) {
            mBaseElevation = cardView.getCardElevation();
        }

        cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR);
        mViews.set(position, cardView);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        mViews.set(position, null);
    }

    private void bind(CardItem item, View view) {
        TextView titleTextView =  view.findViewById(R.id.media_title);
        TextView contentTextView = view.findViewById(R.id.media_description);
        MediaImageView imageView = view.findViewById(R.id.media_image);
        imageView.imageID = item.Thumb;

        imageView.loadMediaImage();


        titleTextView.setText(item.Title);
        contentTextView.setText(item.Description);

        if (item.ID != null) {
            view.setTag(item);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CardItem mItem = (CardItem) view.getTag();

                    if (mItem.IsAlbum) {
                        Intent MyIntent = new Intent(mActivity, MediaCenterPage.class);
                        MyIntent.putExtra("Album_ID", Integer.valueOf(mItem.ID));
                        mActivity.startActivity(MyIntent);

                    } else if(mItem.IsExternalVideo) {

                        if (!mItem.ImageLink.startsWith("http://") && !mItem.ImageLink.startsWith("https://"))
                            mItem.ImageLink = "https://" + mItem.ImageLink;

                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mItem.ImageLink));
                        mActivity.startActivity(browserIntent);

                    } else {
                        Intent MyIntent = new Intent(mActivity, MediaViewerPage.class);
                        MyIntent.putExtra("Image_Link", App.getAppBaseUrl() + Constants.TASK_MEDIA_LOAD + "/" + mItem.Thumb);

                        mActivity.startActivity(MyIntent);
                    }

                }
            });
        }

    }

}
