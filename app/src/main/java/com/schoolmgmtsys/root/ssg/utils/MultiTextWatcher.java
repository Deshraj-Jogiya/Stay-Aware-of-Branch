package com.schoolmgmtsys.root.ssg.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class MultiTextWatcher {
    /* access modifiers changed from: private */
    public TextWatcherWithInstance callback;

    public interface TextWatcherWithInstance {
        void onTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3);
    }

    public MultiTextWatcher setCallback(TextWatcherWithInstance callback2) {
        this.callback = callback2;
        return this;
    }

    public MultiTextWatcher registerEditText(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MultiTextWatcher.this.callback.onTextChanged(editText, s, start, before, count);
            }

            public void afterTextChanged(Editable editable) {
            }
        });
        return this;
    }
}
