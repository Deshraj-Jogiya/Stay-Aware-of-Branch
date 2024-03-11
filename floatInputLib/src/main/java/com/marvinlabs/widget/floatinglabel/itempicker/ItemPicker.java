package com.marvinlabs.widget.floatinglabel.itempicker;


public interface ItemPicker<ItemT> {

    /**
     * Get the unique ID for this picker
     * @return
     */
    public int getPickerId();

    /**
     * Set the items that are initially selected by the user
     *
     * @param itemIndices The indices of the selected items
     */
    public void setSelectedItems(int[] itemIndices);

    /**
     * Get the indices of the items currently selected
     *
     * @return an array of indices within the available items list
     */
    public int[] getSelectedIndices();

    /**
     * Returns true if no item has been picked
     *
     * @return
     */
    public boolean isSelectionEmpty();
}