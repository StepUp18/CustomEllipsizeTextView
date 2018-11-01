package com.github.stepup18;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CustomEllipsizeTextView extends AppCompatTextView {
    public static final int UNIVERSAL_MEASURE_POSITION_VALUE = 1073741824;
    private CharSequence mEllipsizeText;
    private CharSequence mOriginText;
    private int mEllipsizeIndex;
    private int mMaxLines;
    private boolean mIsExactlyMode;
    private boolean mEnableUpdateOriginText;

    public CustomEllipsizeTextView(Context context) {
        this(context, null);
    }

    public CustomEllipsizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mEnableUpdateOriginText = true;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CustomEllipsizeTextView);
        this.mEllipsizeIndex = ta.getInt(R.styleable.CustomEllipsizeTextView_ellipsizeIndex, 0);
        this.mEllipsizeText = ta.getText(R.styleable.CustomEllipsizeTextView_ellipsizeText);
        if (this.mEllipsizeText == null) {
            this.mEllipsizeText = "...";
        }

        ta.recycle();
    }

    public void setMaxLines(int maxLines) {
        if (this.mMaxLines != maxLines) {
            super.setMaxLines(maxLines);
            this.mMaxLines = maxLines;
        }

    }

    @SuppressLint("WrongConstant")
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.setText(this.mOriginText);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //Чтобы ellipsize мог появляться независимо от позиции последней строки (начало, середина и конец)
        try {
            this.mIsExactlyMode = MeasureSpec.getMode(widthMeasureSpec) == UNIVERSAL_MEASURE_POSITION_VALUE;
            Layout layout = this.getLayout();
            if (layout != null && (this.isExceedMaxLine(layout) || this.isOutOfBounds(layout))) {
                this.adjustEllipsizeEndText(layout);
                setTag(layout.getLineCount());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void setText(CharSequence text, BufferType type) {
        if (this.mEnableUpdateOriginText) {
            this.mOriginText = text;
        }

        super.setText(text, type);
        if (this.mIsExactlyMode) {
            this.requestLayout();
        }

    }

    private boolean isExceedMaxLine(Layout layout) {
        return layout.getLineCount() > this.mMaxLines && this.mMaxLines > 0;
    }

    private boolean isOutOfBounds(Layout layout) {
        return layout.getHeight() > this.getMeasuredHeight() - this.getPaddingBottom() - this.getPaddingTop();
    }

    private void adjustEllipsizeEndText(Layout layout) {
        String originText = ((String) this.mOriginText).replaceAll("\\n", " ");
        CharSequence restSuffixText = originText.subSequence(originText.length() - this.mEllipsizeIndex, originText.length());
        int width = layout.getWidth() - this.getPaddingLeft() - this.getPaddingRight();
        int maxLineCount = Math.max(1, this.computeMaxLineCount(layout));
        if (maxLineCount < 2) {
            maxLineCount = 2;
        }
        int lastLineWidth = (int) layout.getLineWidth(maxLineCount - 1);
        int mLastCharacterIndex = layout.getLineEnd(maxLineCount - 1);
        int suffixWidth = (int) (Layout.getDesiredWidth(this.mEllipsizeText, this.getPaint()) + Layout.getDesiredWidth(restSuffixText, this.getPaint())) + 1;
        this.mEnableUpdateOriginText = false;
        if (lastLineWidth + suffixWidth > width) {
            int widthDiff = lastLineWidth + suffixWidth - width;
            int removedCharacterCount = this.computeRemovedEllipsizeEndCharacterCount(widthDiff, originText.subSequence(0, mLastCharacterIndex));
            this.setText(originText.subSequence(0, mLastCharacterIndex - removedCharacterCount));
            this.append(this.mEllipsizeText);
            this.append(restSuffixText);
        } else {
            this.setText(originText.subSequence(0, mLastCharacterIndex));
            this.append(this.mEllipsizeText);
            this.append(restSuffixText);
        }

        this.mEnableUpdateOriginText = true;
    }

    private int computeMaxLineCount(Layout layout) {
        int availableHeight = this.getMeasuredHeight() - this.getPaddingTop() - this.getPaddingBottom();

        for (int i = 0; i < layout.getLineCount(); ++i) {
            if (availableHeight < layout.getLineBottom(i)) {
                return i;
            }
        }

        return layout.getLineCount();
    }

    private int computeRemovedEllipsizeEndCharacterCount(int widthDiff, CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return 0;
        } else {
            List<Range<Integer>> characterStyleRanges = this.computeCharacterStyleRanges(text);
            String textStr = text.toString();
            int characterIndex;
            int codePointIndex = textStr.codePointCount(0, text.length());

            for (int currentRemovedWidth = 0; codePointIndex > 0 && widthDiff > currentRemovedWidth; currentRemovedWidth = (int) Layout.getDesiredWidth(text.subSequence(characterIndex, text.length()), this.getPaint())) {
                --codePointIndex;
                characterIndex = textStr.offsetByCodePoints(0, codePointIndex);
                Range<Integer> characterStyleRange = this.computeCharacterStyleRange(characterStyleRanges, characterIndex);
                if (characterStyleRange != null) {
                    characterIndex = characterStyleRange.getLower();
                    codePointIndex = textStr.codePointCount(0, characterIndex);
                }
            }

            return text.length() - textStr.offsetByCodePoints(0, codePointIndex);
        }
    }

    private Range<Integer> computeCharacterStyleRange(List<Range<Integer>> characterStyleRanges, int index) {
        if (characterStyleRanges != null && !characterStyleRanges.isEmpty()) {
            Iterator var3 = characterStyleRanges.iterator();

            Range characterStyleRange;
            do {
                if (!var3.hasNext()) {
                    return null;
                }

                characterStyleRange = (Range) var3.next();
            } while (!characterStyleRange.contains(index));

            return characterStyleRange;
        } else {
            return null;
        }
    }

    private List<Range<Integer>> computeCharacterStyleRanges(CharSequence text) {
        SpannableStringBuilder ssb = SpannableStringBuilder.valueOf(text);
        CharacterStyle[] characterStyles = ssb.getSpans(0, ssb.length(), CharacterStyle.class);
        if (characterStyles != null && characterStyles.length != 0) {
            List<Range<Integer>> ranges = new ArrayList();
            CharacterStyle[] var5 = characterStyles;
            int var6 = characterStyles.length;

            for (int var7 = 0; var7 < var6; ++var7) {
                CharacterStyle characterStyle = var5[var7];
                ranges.add(new Range(Integer.valueOf(ssb.getSpanStart(characterStyle)), Integer.valueOf(ssb.getSpanEnd(characterStyle))));
            }

            return ranges;
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    public void setEllipsizeText(CharSequence ellipsizeText, int ellipsizeIndex) {
        this.mEllipsizeText = ellipsizeText;
        this.mEllipsizeIndex = ellipsizeIndex;
    }

    public static final class Range<T extends Comparable<? super T>> {
        private final T mLower;
        private final T mUpper;

        public Range(T lower, T upper) {
            this.mLower = lower;
            this.mUpper = upper;
            if (lower.compareTo(upper) > 0) {
                throw new IllegalArgumentException("lower must be less than or equal to upper");
            }
        }

        public T getLower() {
            return this.mLower;
        }

        public T getUpper() {
            return this.mUpper;
        }

        public boolean contains(T value) {
            boolean gteLower = value.compareTo(this.mLower) >= 0;
            boolean lteUpper = value.compareTo(this.mUpper) < 0;
            return gteLower && lteUpper;
        }
    }
}
