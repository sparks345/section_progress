package com.tencent.jinjingcao.section_progress;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Section Progress Bar
 * Created by jinjingcao on 2017/9/29.
 */

public class SectionProgressBar extends View {
    public static final String TAG = "SectionProgressBar";

    private final Paint mPaint;
    private final Paint mSplitBlockPaint;
    private final RectF mRect;

    // all sections in current progress bar
    private ArrayList<Section> sections = new ArrayList<>();

    // current progress.
    private float mCurrentProgress;

    public SectionProgressBar(Context context) {
        this(context, null);
    }

    public SectionProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.mPaint = new Paint();
        this.mRect = new RectF();
//        this.mPaint.setAntiAlias(true);
        this.mSplitBlockPaint = new Paint();
    }

    // attributes
    // split_block_color
    // split_block_width
    // progress_color
    // progress_height
    // section_anim_on_select
    // section_on_focusable

    /**
     * set progress for view
     *
     * @param percent percent of progress
     */
    public void setProgress(float percent) {
        if (percent > 100 || percent < 0) {
            throw new IllegalArgumentException("percent must between 0.0F and 100.0F");
        }
        mCurrentProgress = percent;
        postInvalidate();
    }

    /**
     * set split block at giving progress, and add a new section automatic.
     *
     * @param percent percent of split block
     */
    public void setSplitAtProgress(float percent) {
        Section lastSection = null;
        Section newSection = new Section();
        if (sections.size() > 0) {
            lastSection = sections.get(sections.size() - 1);
        }
        newSection.start = lastSection != null ? lastSection.end : 0;
        newSection.end = percent;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int aimPos = (int) (mCurrentProgress * width / 100);
        int centerY = getHeight() / 2;
        mRect.left = getPaddingLeft();
        mRect.right = getPaddingRight();
        mRect.top = centerY - 10;
        mRect.bottom = centerY + 10;
        canvas.drawRect(mRect, mPaint);
        super.onDraw(canvas);
    }

    private class Section {

        private boolean mIsSelected;

        private Paint mPaint = new Paint();

        float start;
        float end;
    }
}
