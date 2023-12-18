package com.schoolmgmtsys.root.ssg.fonts;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.schoolmgmtsys.root.ssg.R;
import com.schoolmgmtsys.root.ssg.utils.Concurrent;

public class ParentStyledTextView extends android.support.v7.widget.AppCompatTextView {

    public ParentStyledTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public ParentStyledTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ParentStyledTextView(Context context) {
        super(context);
        init(context, null);

    }

    public void setNotNullText(CharSequence value) {
        if (value != null && !value.equals("") && !value.equals("null")) setText(value);
        else setText("NA");
    }

    private void init(Context context, AttributeSet attrs) {
        if(attrs != null){
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LanguageManager);
            String LangSourceID = a.getString(R.styleable.LanguageManager_lang_source_id);
            if(LangSourceID != null){
                String TranslatedWord = Concurrent.getLangSubWords(LangSourceID,getText().toString());

                if(TranslatedWord != null && !TranslatedWord.equals("")){
                    setText(TranslatedWord);
                }
            }else if(getText() != null){
                String TranslatedWord = Concurrent.getLangSubWords(getText().toString(),getText().toString());
                if(TranslatedWord != null && !TranslatedWord.equals("")){
                    setText(TranslatedWord);
                }
            }
            a.recycle();
        }else{
            if(getText() != null){
                String TranslatedWord = Concurrent.getLangSubWords(getText().toString(),getText().toString());
                if(TranslatedWord != null && !TranslatedWord.equals("")){
                    setText(TranslatedWord);
                }
            }
        }
    }

    public void setTypeface(String FontName){
        setTypeface(FontCache.get(FontName, getContext()));

    }

}
