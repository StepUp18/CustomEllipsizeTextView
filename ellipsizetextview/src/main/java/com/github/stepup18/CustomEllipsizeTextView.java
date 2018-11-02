package com.github.stepup18;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
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
    private int ellipsizeColor;
    private boolean isExactlyMode;
    private boolean enableUpdateOriginText;

    public CustomEllipsizeTextView(Context context) {
        this(context, null);
    }

    public CustomEllipsizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        enableUpdateOriginText = true;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomEllipsizeTextView);
        ellipsizeIndex = typedArray.getInt(R.styleable.CustomEllipsizeTextView_ellipsizeIndex, 0);
        ellipsizeText = typedArray.getText(R.styleable.CustomEllipsizeTextView_ellipsizeText);
        ellipsizeColor = typedArray.getColor(R.styleable.CustomEllipsizeTextView_ellipsizeColor, ContextCompat.getColor(context, android.R.color.black));
        if (ellipsizeText == null) {
            ellipsizeText = "...";
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
        this.setText(originalText);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        try {
           isExactlyMode = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY;
            Layout layout = getLayout();
            if (layout != null && (isExceedMaxLine(layout) || isOutOfBounds(layout))) {
                adjustEllipsizeEndText(layout);
                setTag(layout.getLineCount());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setText(CharSequence text, BufferType type) {
        if (enableUpdateOriginText) {
            originalText = text;
        }
        super.setText(text, type);
        if (isExactlyMode) {
            requestLayout();
        }
    }

    private void setEllipsizeColor() {
        if (!TextUtils.isEmpty(ellipsizeText)) {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(ellipsizeText);
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(ellipsizeColor);
            spannableStringBuilder.setSpan(foregroundColorSpan, spannableStringBuilder.toString().indexOf("Далее"), ellipsizeText.toString().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            setEllipsizeText(spannableStringBuilder);
        }
    }

    private boolean isExceedMaxLine(Layout layout) {
        return layout.getLineCount() > maxLines && maxLines > 0;
    }

    private boolean isOutOfBounds(Layout layout) {
        return layout.getHeight() > getMeasuredHeight() - getPaddingBottom() - getPaddingTop();
    }

    private void adjustEllipsizeEndText(Layout layout) {
        String originalText = ((String) this.originalText).replaceAll("\\n", " ");
        CharSequence restSuffixText = originalText.subSequence(originalText.length() - ellipsizeIndex, originalText.length());
        int width = layout.getWidth() - getPaddingLeft() - getPaddingRight();
        int maxLineCount = Math.max(1, computeMaxLineCount(layout));
        if (maxLineCount < 2) {
            maxLineCount = 2;
        }
        int lastLineWidth = (int) layout.getLineWidth(maxLineCount - 1);
        int lastCharacterIndex = layout.getLineEnd(maxLineCount - 1);
        int suffixWidth = (int) (Layout.getDesiredWidth(ellipsizeText, getPaint()) + Layout.getDesiredWidth(restSuffixText, getPaint())) + 1;
        this.enableUpdateOriginText = false;
        if (lastLineWidth + suffixWidth > width) {
            int widthDiff = lastLineWidth + suffixWidth - width;
            int removedCharacterCount = computeRemovedEllipsizeEndCharacterCount(widthDiff, originalText.subSequence(0, lastCharacterIndex));
            setText(originalText.subSequence(0, lastCharacterIndex - removedCharacterCount));
            append(ellipsizeText);
            append(restSuffixText);
        } else {
            setText(originalText.subSequence(0, lastCharacterIndex));
            append(ellipsizeText);
            append(restSuffixText);
        }
        setEllipsizeColor();
        enableUpdateOriginText = true;
    }

    private int computeMaxLineCount(Layout layout) {
        int availableHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
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
            for (int currentRemovedWidth = 0; codePointIndex > 0 && widthDiff > currentRemovedWidth; currentRemovedWidth = (int) Layout.getDesiredWidth(text.subSequence(characterIndex, text.length()), getPaint())) {
                --codePointIndex;
                characterIndex = textStr.offsetByCodePoints(0, codePointIndex);
                Range<Integer> characterStyleRange = computeCharacterStyleRange(characterStyleRanges, characterIndex);
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

    public void setEllipsizeIndex(int ellipsizeIndex) {
        this.ellipsizeIndex = ellipsizeIndex;
    }

    public static final class Range<T extends Comparable<? super T>> {
        private final T lower;
        private final T upper;

        Range(T lower, T upper) {
            this.lower = lower;
            this.upper = upper;
            if (lower.compareTo(upper) > 0) {
                throw new IllegalArgumentException("lower must be less than or equal to upper");
            }
        }

        T getLower() {
            return this.lower;
        }

        T getUpper() {
            return this.upper;
        }

        boolean contains(T value) {
            boolean gteLower = value.compareTo(this.lower) >= 0;
            boolean lteUpper = value.compareTo(this.upper) < 0;
            return gteLower && lteUpper;
        }
    }
}
