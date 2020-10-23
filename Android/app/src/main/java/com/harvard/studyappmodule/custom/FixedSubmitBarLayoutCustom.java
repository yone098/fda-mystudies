package com.harvard.studyappmodule.custom;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.harvard.R;

import org.researchstack.backbone.ui.step.layout.StepLayout;
import org.researchstack.backbone.ui.views.ObservableScrollView;
import org.researchstack.backbone.ui.views.SubmitBar;

public abstract class FixedSubmitBarLayoutCustom extends FrameLayout implements StepLayout
{
    public FixedSubmitBarLayoutCustom(Context context)
    {
        super(context);
        init();
    }

    public FixedSubmitBarLayoutCustom(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public FixedSubmitBarLayoutCustom(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public FixedSubmitBarLayoutCustom(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public abstract int getContentResourceId();

    private void init()
    {
        // Init root
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.submitbar, this, true);

        // Add contentContainer to the layout
        ViewGroup contentContainer = (ViewGroup) findViewById(R.id.rsb_content_container);
        View content = inflater.inflate(getContentResourceId(), contentContainer, false);
        contentContainer.addView(content, 0);

        // Init scrollview and submit bar guide positioning
//        final View submitBarGuide = findViewById(R.id.rsb_submit_bar_guide);
        final SubmitBar submitBar = (SubmitBar) findViewById(R.id.rsb_submit_bar);
        final ObservableScrollView scrollView = (ObservableScrollView) findViewById(R.id.rsb_content_container_scrollview);
        scrollView.setScrollbarFadingEnabled(false);
        /*scrollView.setScrollListener(new ObservableScrollView.OnScrollListener() {
            @Override
            public void onScrollChanged(int scrollY) {
                FixedSubmitBarLayoutCustom.this.onScrollChanged(scrollView, submitBarGuide, submitBar);
            }
        });*/
        /*scrollView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
                {
                    @Override
                    public void onGlobalLayout()
                    {
                        scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        // Set submitBarGuide the same height as submitBar
                        if(submitBarGuide.getHeight() != submitBar.getHeight())
                        {
                            submitBarGuide.getLayoutParams().height = submitBar.getHeight();
                            submitBarGuide.requestLayout();
                        }

                        onScrollChanged(scrollView, submitBarGuide, submitBar);
                    }
                });*/
    }

    private void onScrollChanged(ScrollView scrollView, View submitBarGuide, View submitBar)
    {
        int scrollY = scrollView.getScrollY();
        int guidePosition = submitBarGuide.getTop() - scrollY;
        int guideHeight = submitBarGuide.getHeight();
        int yLimit = scrollView.getHeight() - guideHeight;

        ViewCompat.setTranslationY(submitBar, 0);
        /*if(guidePosition <= yLimit)
        {
        }
        else
        {
            int translationY = guidePosition - yLimit;
            ViewCompat.setTranslationY(submitBar, translationY);
        }*/

    }
}

