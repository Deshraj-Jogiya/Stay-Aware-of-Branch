package com.marvinlabs.widget.floatinglabel.instantpicker;

import android.os.Parcelable;


public interface InstantPicker<InstantT extends Instant & Parcelable> {

    /**
     * Get the unique ID for this picker
     * @return
     */
    public int getPickerId();

    /**
     * Set the instant that is initially selected by the user
     *
     * @param instant The instant
     */
    public void setSelectedInstant(InstantT instant);

    /**
     * Get the instant currently selected
     *
     * @return an instant
     */
    public InstantT getSelectedInstant();

    /**
     * Returns true if no instant has been picked
     *
     * @return
     */
    public boolean isSelectionEmpty();
}