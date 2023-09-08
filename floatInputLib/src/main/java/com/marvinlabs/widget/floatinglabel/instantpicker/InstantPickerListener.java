package com.marvinlabs.widget.floatinglabel.instantpicker;


public interface InstantPickerListener<InstantT extends Instant> {

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
     * @param instant The instant that has been selected
     */
    public void onInstantSelected(int pickerId, InstantT instant);

}