package com.github.stepup18;

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

    private CharSequence ellipsizeText;
    private CharSequence originalText;
    private int ellipsizeIndex;
    private int maxLines;
    private boolean isExactlyMode;
    private boolean enableUpdateOriginText;

    public CustomEllipsizeTextView(Context context) {
        this(context, null);
    }

    public CustomEllipsizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.enableUpdateOriginText = true;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomEllipsizeTextView);
        this.ellipsizeIndex = typedArray.getInt(R.styleable.CustomEllipsizeTextView_ellipsizeIndex, 0);
        this.ellipsizeText = typedArray.getText(R.styleable.CustomEllipsizeTextView_ellipsizeText);
        if (this.ellipsizeText == null) {
            this.ellipsizeText = "...";
        }
        typedArray.recycle();
    }

    public void setMaxLines(int maxLines) {
        if (this.maxLines != maxLines) {
            super.setMaxLines(maxLines);
            this.maxLines = maxLines;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.setText(this.originalText);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        try {
            this.isExactlyMode = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY;
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
        if (this.enableUpdateOriginText) {
            this.originalText = text;
        }
        super.setText(text, type);
        if (this.isExactlyMode) {
            this.requestLayout();
        }
    }

    private boolean isExceedMaxLine(Layout layout) {
        return layout.getLineCount() > this.maxLines && this.maxLines > 0;
    }

    private boolean isOutOfBounds(Layout layout) {
        return layout.getHeight() > this.getMeasuredHeight() - this.getPaddingBottom() - this.getPaddingTop();
    }

    private void adjustEllipsizeEndText(Layout layout) {
        String originalText = ((String) this.originalText).replaceAll("\\n", " ");
        CharSequence restSuffixText = originalText.subSequence(originalText.length() - this.ellipsizeIndex, originalText.length());
        int width = layout.getWidth() - this.getPaddingLeft() - this.getPaddingRight();
        int maxLineCount = Math.max(1, this.computeMaxLineCount(layout));
        if (maxLineCount < 2) {
            maxLineCount = 2;
        }
        int lastLineWidth = (int) layout.getLineWidth(maxLineCount - 1);
        int lastCharacterIndex = layout.getLineEnd(maxLineCount - 1);
        int suffixWidth = (int) (Layout.getDesiredWidth(this.ellipsizeText, this.getPaint()) + Layout.getDesiredWidth(restSuffixText, this.getPaint())) + 1;
        this.enableUpdateOriginText = false;
        if (lastLineWidth + suffixWidth > width) {
            int widthDiff = lastLineWidth + suffixWidth - width;
            int removedCharacterCount = this.computeRemovedEllipsizeEndCharacterCount(widthDiff, originalText.subSequence(0, lastCharacterIndex));
            this.setText(originalText.subSequence(0, lastCharacterIndex - removedCharacterCount));
            this.append(this.ellipsizeText);
            this.append(restSuffixText);
        } else {
            this.setText(originalText.subSequence(0, lastCharacterIndex));
            this.append(this.ellipsizeText);
            this.append(restSuffixText);
        }
        this.enableUpdateOriginText = true;
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
            Iterator iterator = characterStyleRanges.iterator();
            Range characterStyleRange;
            do {
                if (!iterator.hasNext()) {
                    return null;
                }
                characterStyleRange = (Range) iterator.next();
            } while (!characterStyleRange.contains(index));
            return characterStyleRange;
        } else {
            return null;
        }
    }

    private List<Range<Integer>> computeCharacterStyleRanges(CharSequence text) {
        SpannableStringBuilder stringBuilder = SpannableStringBuilder.valueOf(text);
        CharacterStyle[] characterStyles = stringBuilder.getSpans(0, stringBuilder.length(), CharacterStyle.class);
        if (characterStyles != null && characterStyles.length != 0) {
            List<Range<Integer>> ranges = new ArrayList<>();
            for (CharacterStyle characterStyle : characterStyles) {
                ranges.add(new Range(stringBuilder.getSpanStart(characterStyle), stringBuilder.getSpanEnd(characterStyle)));
            }
            return ranges;
        } else {
            return Collections.emptyList();
        }
    }

    public void setEllipsizeText(CharSequence ellipsizeText) {
        this.ellipsizeText = ellipsizeText;
    }

    public void setEllipsizeIndex(int mEllipsizeIndex) {
        this.ellipsizeIndex = mEllipsizeIndex;
    }

    public static final class Range<T extends Comparable<? super T>> {
        private final T lower;
        private final T upper;

        public Range(T lower, T upper) {
            this.lower = lower;
            this.upper = upper;
            if (lower.compareTo(upper) > 0) {
                throw new IllegalArgumentException("lower must be less than or equal to upper");
            }
        }

        public T getLower() {
            return this.lower;
        }

        public T getUpper() {
            return this.upper;
        }

        public boolean contains(T value) {
            boolean gteLower = value.compareTo(this.lower) >= 0;
            boolean lteUpper = value.compareTo(this.upper) < 0;
            return gteLower && lteUpper;
        }
    }
}
