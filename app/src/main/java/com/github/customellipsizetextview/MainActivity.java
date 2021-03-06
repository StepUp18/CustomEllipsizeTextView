package com.github.customellipsizetextview;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.github.stepup18.CustomEllipsizeTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    public static final int DEFAULT_MAX_LINES = 3;

    @BindView(R.id.firstTextView)
    CustomEllipsizeTextView firstTextView;
    @BindView(R.id.secondTextView)
    CustomEllipsizeTextView secondTextView;
    @BindView(R.id.thirdTextView)
    CustomEllipsizeTextView thirdTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        firstTextView.setText(getString(R.string.firstText));
        secondTextView.setText(getString(R.string.secondText));
        thirdTextView.setText(getString(R.string.thirdText));
    }

    @OnClick({R.id.firstTextView, R.id.secondTextView, R.id.thirdTextView})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.firstTextView: {
                if (firstTextView.getMaxLines() == Integer.MAX_VALUE) {
                    firstTextView.setMaxLines(DEFAULT_MAX_LINES);
                } else {
                    firstTextView.setMaxLines(Integer.MAX_VALUE);
                }
                break;
            }
            case R.id.secondTextView:
                if (secondTextView.getEllipsizeColor() == ContextCompat.getColor(this, android.R.color.black)) {
                    secondTextView.setEllipsizeColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
                } else {
                    secondTextView.setEllipsizeColor(ContextCompat.getColor(this, android.R.color.black));
                }
                break;
            case R.id.thirdTextView:
                if (TextUtils.equals(thirdTextView.getEllipsizeText(), getString(R.string.ellipsizeDefault))) {
                    thirdTextView.setEllipsizeText(getString(R.string.ellipsizeForReplace), Typeface.BOLD);
                } else {
                    thirdTextView.setEllipsizeText(getString(R.string.ellipsizeDefault), null);
                }
                break;
            default:
                break;
        }
    }
}
