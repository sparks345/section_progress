package com.tencent.jinjingcao.section_progress;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Section Progress Bar
 * Created by jinjingcao on 2017/9/29.
 */

public class SectionProgressBar extends View {

    public static final String TAG = "SectionProgressBar";

    // density.
    private final float density = getResources().getDisplayMetrics().density;

    // default attrs.
    private static final int DEFAULT_SPLIT_BLOCK_COLOR = Color.BLUE;
    private static final int DEFAULT_SPLIT_BLOCK_WIDTH = 6;
    private static final int DEFAULT_SPLIT_BLOCK_HEIGHT = 15;
    private static final int DEFAULT_PROGRESS_BAR_COLOR = Color.CYAN;
    private static final int DEFAULT_PROGRESS_BAR_HEIGHT = 10;
    private static final int DEFAULT_SECTION_BLINK_COLOR = Color.YELLOW;

    // use in progress paint.
    private final Paint mPaint;
    private final Paint mSplitBlockPaint;
    private final RectF mRect;

    // tmp attrs.
    private final int mAttrBlockHeight;
    private final int mAttrProgressHeight;

    // attrs.
    private final int mBlockColor;
    private int mBlockWidth;
    private int mBlockHeight;
    private final int mProgressColor;
    private int mProgressSize;
    private final boolean mSectionAnimEnable;
    private final int mSectionAnimBlinkColor;

    // used in section anim.
    private ArgbEvaluator mArgbEvaluator;

    // all sections in current progress bar.
    private ArrayList<Section> mSections = new ArrayList<>();

    // current progress.
    private float mCurrentProgress;

    // anim timer for select section.
    private AnimationTimer mAnimTimer;

    public SectionProgressBar(Context context) {
        this(context, null);
    }

    public SectionProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SectionProgressBar);

        mBlockColor = ta.getColor(R.styleable.SectionProgressBar_split_block_color, DEFAULT_SPLIT_BLOCK_COLOR);
        mBlockWidth = ta.getDimensionPixelSize(R.styleable.SectionProgressBar_split_block_width, (int) (DEFAULT_SPLIT_BLOCK_WIDTH * density));
        mAttrBlockHeight = ta.getDimensionPixelSize(R.styleable.SectionProgressBar_split_block_height, -1);
        mProgressColor = ta.getColor(R.styleable.SectionProgressBar_progress_bar_color, DEFAULT_PROGRESS_BAR_COLOR);
        mAttrProgressHeight = ta.getDimensionPixelSize(R.styleable.SectionProgressBar_progress_bar_height, -1);
        mSectionAnimEnable = ta.getBoolean(R.styleable.SectionProgressBar_section_anim_on_selection, false);
        mSectionAnimBlinkColor = ta.getColor(R.styleable.SectionProgressBar_section_anim_blink_color, DEFAULT_SECTION_BLINK_COLOR);
        ta.recycle();

        fixAttrDefaultHeight();

        if (mSectionAnimEnable) {
            mArgbEvaluator = new ArgbEvaluator();
        }

        Log.i(TAG,
                String.format(
                        Locale.US,
                        "mBlockColor:%d, mBlockWidth:%d, mBlockHeight:%d, mProgressColor:%d, mProgressSize:%d, mSectionAnimEnable:%b, mSectionAnimBlinkColor:%d",
                        mBlockColor, mBlockWidth, mBlockHeight, mProgressColor, mProgressSize, mSectionAnimEnable, mSectionAnimBlinkColor
                )
        );


        this.mPaint = new Paint();
        this.mRect = new RectF();
//        this.mPaint.setAntiAlias(true);
        this.mSplitBlockPaint = new Paint();
//        this.mSplitBlockPaint.setColor(getResources().getColor(R.color.colorPrimary));
        this.mSplitBlockPaint.setColor(mBlockColor);

//        this.mPaint.setColor(getResources().getColor(R.color.colorAccent));
        this.mPaint.setColor(mProgressColor);
        // the IDE says it's not very good to set a progressHeight to a strokeWIDTH.
        // then, I use progressSIZE.
        this.mPaint.setStrokeWidth(mProgressSize);
    }


    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            fixAttrDefaultHeight();
        }
    }

    /**
     * reset height.
     * if visibility is gone at init.
     * if don't set height.
     */
    private void fixAttrDefaultHeight() {
        if (mAttrBlockHeight <= 0) {
            mBlockHeight = getHeight() > 0 ? getHeight() : (int) (DEFAULT_SPLIT_BLOCK_HEIGHT * density);
        } else {
            mBlockHeight = mAttrBlockHeight;
        }

        if (mAttrProgressHeight <= 0) {
            mProgressSize = getHeight() > 0 ? getHeight() : (int) (DEFAULT_PROGRESS_BAR_HEIGHT * density);
        } else {
            mProgressSize = mAttrProgressHeight;
        }
    }

    /**
     * set progress for view
     *
     * @param percent percent of progress
     */
    public void setProgress(float percent) {
        if (percent > 100 || percent < 0) {
            throw new IllegalArgumentException("percent must between 0.0F and 100.0F");
        }

        Section lastSection;
        if (mSections.size() > 0) {
            lastSection = mSections.get(mSections.size() - 1);
            lastSection.setSelection(false);
        }

        mCurrentProgress = percent;
        postInvalidate();
    }

    /**
     * set split block at giving progress, and add a new section automatic.
     *
     * @param percentEnd percent of split block
     */
    private void setSplitAtProgress(float percentEnd) {
        Section lastSection = null;

        if (mSections.size() > 0) {
            lastSection = mSections.get(mSections.size() - 1);
        }

        float percentStart = lastSection != null ? lastSection.end : 0;

        if (percentStart == percentEnd) {
            return;
        }

        if (lastSection != null) {
            lastSection.setSelection(false);
        }

        Section newSection = new Section(percentStart, percentEnd);
        mSections.add(newSection);

        postInvalidate();
    }

    /**
     * set split block at current progress.
     */
    public void setSplitAtCurrent() {
        setSplitAtProgress(mCurrentProgress);
    }

    /**
     * delete last section.
     */
    public void backDelSection() {
        if (mSections.size() > 0) {
            Section lastSection = mSections.get(mSections.size() - 1);
            if (mCurrentProgress > lastSection.end) {
                throw new RuntimeException("current progress > last section end, add a block first, then do backDelSection.");
            }
            mSections.remove(lastSection);
            setProgress(lastSection.start);

            if (mAnimTimer != null) {
                mAnimTimer.stop();
                mAnimTimer = null;
            }
        }

        postInvalidate();
    }

    /**
     * now work.
     *
     * @param canvas canvas.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // draw main progress. only case while progress gt last section end.
        Section lastSection = mSections.size() > 0 ? mSections.get(mSections.size() - 1) : null;
        if (lastSection == null || mCurrentProgress > lastSection.end) {
            int width = getWidth() - getPaddingLeft() - getPaddingRight();
            int aimPos = (int) (mCurrentProgress * width / 100);
            int centerY = getHeight() / 2;
            mRect.left = getPaddingLeft();
            mRect.right = aimPos + mRect.left;// width - getPaddingRight();
            mRect.top = centerY - mProgressSize / 2;
            mRect.bottom = centerY + mProgressSize / 2;
            canvas.drawRect(mRect, mPaint);
        }

        // draw sections.
        for (Section section : mSections) {
            section.doDraw(canvas);
        }

        super.onDraw(canvas);
    }

    /**
     * return current progress.
     *
     * @return current progress.
     */
    public float getCurrentProgress() {
        return mCurrentProgress;
    }

    /**
     * select last section.
     *
     * @return flag.
     */
    public boolean selectLastSection() {
        if (mSections.size() > 0) {
            Section lastSection = mSections.get(mSections.size() - 1);
            if (mCurrentProgress > lastSection.end) {
                throw new RuntimeException("current progress > last section end, add a block first, then do selectLastSection.");
            }
            boolean ret = lastSection.mIsSelected;
            lastSection.setSelection(true);
            postInvalidate();
            return ret;
        }

        return false;
    }

    /**
     * blocked section in progress.
     */
    private class Section {
        private final Paint mBlockPaint;

        private final Paint mPaint;
        private RectF mSectionRect;
        private RectF mBlockRect;

        public Section(float start, float end) {
            this.mPaint = new Paint();
//            this.mPaint.setColor(getResources().getColor(R.color.colorAccent));
            this.mPaint.setColor(mProgressColor);
            this.mBlockPaint = mSplitBlockPaint;
            this.start = start;
            this.end = end;
        }

        private boolean mIsSelected;

        float start;
        float end;

        public void doDraw(Canvas canvas) {
            int centerY = getHeight() / 2;

            if (mSectionRect == null) {
                mSectionRect = new RectF();
                int offsetLeft = getPaddingLeft();
                int width = getWidth() - offsetLeft - getPaddingRight();
                int startPos = (int) (start * width / 100) + offsetLeft;// - mBlockWidth / 2;
                int endPos = (int) (end * width / 100) + offsetLeft;// - mBlockWidth / 2;

                mSectionRect.left = startPos;
                mSectionRect.right = endPos;

                mSectionRect.top = centerY - mProgressSize / 2;
                mSectionRect.bottom = centerY + mProgressSize / 2;
            }

            if (mBlockRect == null) {
                mBlockRect = new RectF();
                int blockStartPos = (int) mSectionRect.right - mBlockWidth / 2;
                int blockEndPos = blockStartPos + mBlockWidth / 2;

                mBlockRect.left = blockStartPos;
                mBlockRect.right = blockEndPos;
                mBlockRect.top = centerY - mBlockHeight / 2;
                mBlockRect.bottom = centerY + mBlockHeight / 2;
            }

            if (mIsSelected && mAnimTimer != null) {
                drawAnim();
            } else {
//                mPaint.setColor(getResources().getColor(R.color.colorAccent));
                mPaint.setColor(mProgressColor);
            }
            canvas.drawRect(mSectionRect, mPaint);
            canvas.drawRect(mBlockRect, mBlockPaint);

        }

        /**
         * you can overwrite this as your wish.
         */
        public void drawAnim() {
            int step = mAnimTimer.getStep();
            float percent = step / 256.0f;

            int color = (int) mArgbEvaluator.evaluate(percent, mProgressColor, mSectionAnimBlinkColor);
            mPaint.setColor(color);

//            mPaint.setColor(Color.parseColor("#ff00ff"));
//            mPaint.setColor(mSectionAnimBlinkColor);
//            mPaint.setAlpha(step);// set alpha after set color!!!

//                // test code
//                Paint tPaint = new Paint();
//                tPaint.setTextSize(24);
//                tPaint.setColor(Color.parseColor("#000000"));
//                tPaint.setStrokeWidth(2);
//                tPaint.setTextAlign(Align.LEFT);
//                Rect bounds = new Rect();
//                String testString = "a:" + alpha;
//                tPaint.getTextBounds(testString, 0, testString.length(), bounds);
//                tPaint.setAlpha(alpha);
//                canvas.drawText(testString, getMeasuredWidth() / 2 - bounds.width() / 2, getMeasuredHeight() / 2 + bounds.height() / 2, tPaint);
        }

        /**
         * make section to selection status.
         *
         * @param flag flag
         */
        public void setSelection(boolean flag) {
            mIsSelected = flag;
            Log.i(TAG, "setSelection: flag->" + flag + ", mSectionAnimEnable->" + mSectionAnimEnable);
            if (mSectionAnimEnable) {
                if (flag && mAnimTimer == null) {
                    mAnimTimer = new AnimationTimer();
                    mAnimTimer.start();
                } else if (!flag && mAnimTimer != null) {
                    mAnimTimer.stop();
                    mAnimTimer = null;
                }
            }
        }
    }

    /**
     * for anim on select.
     * from 50 to 200 (0 ~ 255), step 20, and trigger per 80 milliseconds,
     * you can change below configs as your wish.
     */
    private class AnimationTimer {
        static final int MAX_VALUE = 200;
        static final int MIN_VALUE = 50;
        static final int EVALUATOR_STEP = 20;
        static final int TIMER_TRIGGER = 80;

        TimerTask timerTask;
        Timer timer;
        int step = MAX_VALUE;

        byte animDirection = 1;// 0:fade in, 1: fade out.

        AnimationTimer() {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    switch (animDirection) {
                        case 0:
                            if (step < MAX_VALUE) {
                                step += EVALUATOR_STEP;
                                if (step > MAX_VALUE) {
                                    step = MAX_VALUE;
                                }
                                postInvalidate();
                            } else {
                                step = MAX_VALUE;
                                animDirection = 1;
                            }
                            break;
                        case 1:
                            if (step > MIN_VALUE) {
                                step -= EVALUATOR_STEP;
                                if (step < MIN_VALUE) {
                                    step = MIN_VALUE;
                                }
                                postInvalidate();
                            } else {
                                step = MIN_VALUE;
                                animDirection = 0;
                            }
                            break;
                    }
                }
            };
        }

        void start() {
            timer.schedule(timerTask, 0, TIMER_TRIGGER);
        }

        private void stop() {
            if (this.timerTask != null) {
                this.timerTask.cancel();
                this.timerTask = null;
            }

            if (this.timer != null) {
                this.timer.cancel();
                this.timer = null;
            }
        }

        int getStep() {
            return step;
        }
    }
}
