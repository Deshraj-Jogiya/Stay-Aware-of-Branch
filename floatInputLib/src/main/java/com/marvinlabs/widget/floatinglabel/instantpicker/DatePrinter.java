package com.marvinlabs.widget.floatinglabel.instantpicker;


public interface DatePrinter<DateInstantT extends DateInstant> extends InstantPrinter<DateInstantT> {

    /**
     * Print a date
     *
     * @param dateInstant The date to print
     * @return
     */
    public String print(DateInstantT dateInstant);

}
