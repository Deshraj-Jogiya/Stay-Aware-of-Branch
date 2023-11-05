package com.marvinlabs.widget.floatinglabel.itempicker;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;


public class StringPickerDialogFragment extends AbstractPickerDialogFragment<String> {

    protected ArrayList<String> availableItems = null;

    // =============================================================================================
    // Factory methods
    // ==

    /**
     * Create a new instance of that picker dialog
     *
     * @param pickerId                The id of the item picker
     * @param title                   The title for the dialog
     * @param positiveButtonText      The text of the positive button
     * @param negativeButtonText      The text of the negative button
     * @param enableMultipleSelection Whether or not to allow selecting multiple items
     * @param selectedItemIndices     The positions of the items already selected
     * @param availableItems          The collection of items that can be picked
     * @return A new instance of the picker dialog
     */
    public static StringPickerDialogFragment newInstance(
            int pickerId,
            String title,
            String positiveButtonText,
            String negativeButtonText,
            boolean enableMultipleSelection,
            int[] selectedItemIndices,
            ArrayList<String> availableItems) {

        Collections.sort(availableItems);

        StringPickerDialogFragment f = new StringPickerDialogFragment();
        Bundle args = buildCommonArgsBundle(pickerId, title, positiveButtonText, negativeButtonText, enableMultipleSelection, selectedItemIndices);
        args.putStringArrayList(ARG_AVAILABLE_ITEMS, availableItems);
        f.setArguments(args);
        return f;
    }

    // =============================================================================================
    // Other methods
    // ==

    @Override
    public ArrayList<String> getAvailableItems() {
        if (availableItems == null) {
            availableItems = getArguments().getStringArrayList(ARG_AVAILABLE_ITEMS);

        }
        return availableItems;
    }
}