package com.marvinlabs.widget.floatinglabel.itempicker;


public interface ItemPickerListener<ItemT> {

    /**
     * The dialog has been closed and the user does not want to change the selection
     *
     * @param pickerId The id of the item picker
     */
    public void onCancelled(int pickerId);

    /**
     * The dialog has been closed and items have been selected
     *
     * @param pickerId      The id of the item picker
     * @param selectedIndices The items that have been selected (or not if it is empty)
     */
    public void onItemsSelected(int pickerId, int[] selectedIndices);

}