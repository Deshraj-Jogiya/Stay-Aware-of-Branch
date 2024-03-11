package com.schoolmgmtsys.root.ssg.fonts;

import android.content.Context;
import android.util.AttributeSet;

public class LightStyledTextView extends ParentStyledTextView {

    String fontName = "RobotoCondensed-Light.ttf";

    public LightStyledTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTypeface(fontName);
    }

    public LightStyledTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(fontName);
    }

    public LightStyledTextView(Context context) {
        super(context);
        setTypeface(fontName);
    }

}
