package com.marvinlabs.widget.floatinglabel.anim;

import android.view.View;
import android.widget.TextView;


public class TextViewLabelAnimator<InputWidgetT extends TextView> extends DefaultLabelAnimator<InputWidgetT> {

    public TextViewLabelAnimator() {
        super();
    }

    public TextViewLabelAnimator(float alphaAnchored, float alphaFloating, float scaleAnchored, float scaleFloating) {
        super(alphaAnchored, alphaFloating, scaleAnchored, scaleFloating);
    }

    @Override
    protected float getTargetX(InputWidgetT inputWidget, View label, boolean isAnchored) {
        float x = inputWidget.getLeft();

        // Add left drawable size and padding when anchored
        if (isAnchored) {
            x += inputWidget.getCompoundPaddingLeft();
        } else {
            x += inputWidget.getPaddingLeft();
        }

        return x;
    }

    protected float getTargetY(InputWidgetT inputWidget, View label, boolean isAnchored) {
        if (isAnchored) {
            int lineHeight = inputWidget.getLineHeight();
            int lineCount = inputWidget.getLineCount();

            float targetY = inputWidget.getBottom() - inputWidget.getPaddingBottom() - label.getHeight();
            if (lineCount > 1) {
                targetY -= (lineCount - 1) * lineHeight;
            }

            return targetY;
        } else {
            final float targetScale = getTargetScale(inputWidget, label, isAnchored);
            return inputWidget.getTop() - targetScale * label.getHeight();
        }
    }
}