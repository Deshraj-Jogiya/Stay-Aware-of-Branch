package com.marvinlabs.widget.floatinglabel.instantpicker;


public interface TimePrinter<TimeInstantT extends TimeInstant> extends InstantPrinter<TimeInstantT> {

    /**
     * Print a time
     *
     * @param timeInstant The time to print
     * @return
     */
    public String print(TimeInstantT timeInstant);

}
