package com.schoolmgmtsys.root.ssg.expanded;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.schoolmgmtsys.root.ssg.models.DashLeadModel;
import com.schoolmgmtsys.root.ssg.R;

import java.util.List;

public class LeadersAdapter extends ExpandableRecyclerAdapter<ParentsViewHolder, ChildsViewHolder> {

    private Context mContext;
    private LayoutInflater mInflator;

    public LeadersAdapter(Context context, @NonNull List<? extends ParentListItem> parentItemList) {
        super(parentItemList);
        this.mContext = context;
        mInflator = LayoutInflater.from(context);
    }

    @Override
    public ParentsViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
        View recipeView = mInflator.inflate(R.layout.leader_expand_parent_view, parentViewGroup, false);
        return new ParentsViewHolder(recipeView);
    }

    @Override
    public ChildsViewHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
        View ingredientView = mInflator.inflate(R.layout.leader_expand_child_view, childViewGroup, false);
        return new ChildsViewHolder(ingredientView);
    }

    @Override
    public void onBindParentViewHolder(ParentsViewHolder recipeViewHolder, int position, ParentListItem parentListItem) {
        Parent parent = (Parent) parentListItem;
        recipeViewHolder.bind(parent);
    }

    @Override
    public void onBindChildViewHolder(ChildsViewHolder ingredientViewHolder, int position, Object childListItem) {
        DashLeadModel ingredient = (DashLeadModel) childListItem;
        ingredientViewHolder.bind(ingredient);
    }
}
