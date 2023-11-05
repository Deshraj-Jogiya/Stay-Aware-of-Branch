package com.marvinlabs.widget.floatinglabel.instantpicker;


public interface TimeInstant extends Instant {

    int getHourOfDay();

    int getMinuteOfHour();

    int getSecondOfMinute();

    void setHourOfDay(int hourOfDay);

    void setMinuteOfHour(int minuteOfHour);

    void setSecondOfMinute(int secondOfMinute);
}
