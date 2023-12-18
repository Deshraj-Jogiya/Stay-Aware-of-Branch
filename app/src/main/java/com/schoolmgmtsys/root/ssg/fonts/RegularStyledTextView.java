package com.schoolmgmtsys.root.ssg.fonts;

import android.content.Context;
import android.util.AttributeSet;

public class RegularStyledTextView extends ParentStyledTextView {

    String fontName = "RobotoCondensed-Regular.ttf";

    public RegularStyledTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTypeface(fontName);
    }

    public RegularStyledTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(fontName);
    }

    public RegularStyledTextView(Context context) {
        super(context);
        setTypeface(fontName);
    }

}
