package com.schoolmgmtsys.root.ssg.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


import com.solutionsbricks.solbricksframework.CardPager.CardItem;
import com.solutionsbricks.solbricksframework.CardPager.ShadowTransformer;
import com.schoolmgmtsys.root.ssg.models.AlbumsModel;
import com.schoolmgmtsys.root.ssg.models.PhotoModel;
import com.schoolmgmtsys.root.ssg.R;

import java.util.ArrayList;

public class MediaCenterFragment extends Fragment {

    private static final String ARG_POSITION = "position";
    private static final String ARG_TAB_NAME = "tab_name";
    private static final String ARG_DATA_LIST = "data_list";
    private ViewPager mViewPager;
    private LinearLayout mEmptyView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_media_fragment, container, false);
        mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
        mEmptyView = (LinearLayout) view.findViewById(R.id.empty_view);

        String TAB_Name = getArguments().getString(ARG_TAB_NAME);
        if(TAB_Name.equals("Albums")){
            ArrayList<AlbumsModel> AlbumsDataList = getArguments().getParcelableArrayList(ARG_DATA_LIST);

            if(AlbumsDataList != null && AlbumsDataList.size() > 0){
                MediaCenterItemAdapter mCardAdapter = new MediaCenterItemAdapter(getActivity());
                for (AlbumsModel albumItem : AlbumsDataList){
                    mCardAdapter.addCardItem(new CardItem(albumItem.id,albumItem.albumImage,albumItem.albumTitle,albumItem.albumDescription,albumItem.albumParent,albumItem.albumImage,true,false));
                }

                ShadowTransformer mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);
                mViewPager.setAdapter(mCardAdapter);
                mViewPager.setPageTransformer(false, mCardShadowTransformer);
                mViewPager.setOffscreenPageLimit(3);
            }else{
                mEmptyView.setVisibility(View.VISIBLE);
                mViewPager.setVisibility(View.GONE);
            }

        }else{
            ArrayList<PhotoModel> MediaDataList = getArguments().getParcelableArrayList(ARG_DATA_LIST);

            if(MediaDataList != null && MediaDataList.size() > 0){
                MediaCenterItemAdapter mCardAdapter = new MediaCenterItemAdapter(getActivity());
                for (PhotoModel photoItem : MediaDataList){
                    String mediaThumb = null;
                    Boolean mediaIsVideo = null;

                    if(photoItem.mediaType.equals("0")){
                        mediaThumb = photoItem.mediaURL;
                        mediaIsVideo = false;
                    } else if(photoItem.mediaType.equals("1") || photoItem.mediaType.equals("2")){
                        mediaThumb = photoItem.mediaURLThumb;
                        mediaIsVideo = true;
                    }

                    mCardAdapter.addCardItem(new CardItem(photoItem.id,photoItem.mediaURL,photoItem.mediaTitle,photoItem.mediaDescription,null,mediaThumb,false,mediaIsVideo));
                }

                ShadowTransformer mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);
                mViewPager.setAdapter(mCardAdapter);
                mViewPager.setPageTransformer(false, mCardShadowTransformer);
                mViewPager.setOffscreenPageLimit(3);
            }else{
                mEmptyView.setVisibility(View.VISIBLE);
                mViewPager.setVisibility(View.GONE);
            }
        }

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    public static MediaCenterFragment newInstance(int position, String tabName, ArrayList dataList) {
        MediaCenterFragment f = new MediaCenterFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        b.putParcelableArrayList(ARG_DATA_LIST,dataList);
        b.putString(ARG_TAB_NAME, tabName);
        f.setArguments(b);
        return f;
    }

}