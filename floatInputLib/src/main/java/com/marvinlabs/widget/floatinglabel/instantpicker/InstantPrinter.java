package com.marvinlabs.widget.floatinglabel.instantpicker;


public interface InstantPrinter<IntantT extends Instant> {

    /**
     * Print an instant
     *
     * @param instant The instant to print
     * @return
     */
    public String print(IntantT instant);

}
