package com.marvinlabs.widget.floatinglabel.instantpicker;


public interface DateInstant extends Instant {

    int getYear();

    int getMonthOfYear();

    int getDayOfMonth();

    void setYear(int year);

    void setMonthOfYear(int monthOfYear);

    void setDayOfMonth(int dayOfMonth);
}
