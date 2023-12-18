package com.schoolmgmtsys.root.ssg.fonts;

import android.content.Context;
import android.util.AttributeSet;

public class BoldStyledTextView extends ParentStyledTextView {

    String fontName = "RobotoCondensed-Bold.ttf";

    public BoldStyledTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTypeface(fontName);
    }

    public BoldStyledTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(fontName);
    }

    public BoldStyledTextView(Context context) {
        super(context);
        setTypeface(fontName);
    }

}
