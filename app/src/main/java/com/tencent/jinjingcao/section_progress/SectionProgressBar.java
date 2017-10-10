package com.tencent.jinjingcao.section_progress;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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

    // all sections in current progress bar.
    private ArrayList<Section> mSections = new ArrayList<>();

    // current progress.
    private float mCurrentProgress;

    // block width.
    private int mBlockWidth = 6 * 2;

    // anim timer for select section.
    private AnimationTimer mAnimTimer;

    public SectionProgressBar(Context context) {
        this(context, null);
    }

    public SectionProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.mPaint = new Paint();
        this.mRect = new RectF();
//        this.mPaint.setAntiAlias(true);
        this.mSplitBlockPaint = new Paint();
        this.mSplitBlockPaint.setColor(getResources().getColor(R.color.colorPrimary));

        this.mPaint.setColor(getResources().getColor(R.color.colorAccent));
        this.mPaint.setStrokeWidth(20);
    }

    // attributes:
    // split_block_color
    // split_block_width
    // split_block_height
    // progress_bar_color
    // progress_bar_height
    // section_anim_on_selection
    // section_selectable_by_click

    /**
     * set progress for view
     *
     * @param percent percent of progress
     */
    public void setProgress(float percent) {
        if (percent > 100 || percent < 0) {
            throw new IllegalArgumentException("percent must between 0.0F and 100.0F");
        }

        Section lastSection = null;

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
                throw new RuntimeException("current progress > last block end, add a block first, then do backDelSection.");
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

    @Override
    protected void onDraw(Canvas canvas) {
        Section lastSection = mSections.size() > 0 ? mSections.get(mSections.size() - 1) : null;
        if (lastSection == null || mCurrentProgress > lastSection.end) {
            int width = getWidth() - getPaddingLeft() - getPaddingRight();
            int aimPos = (int) (mCurrentProgress * width / 100);
            int centerY = getHeight() / 2;
            mRect.left = getPaddingLeft();
            mRect.right = aimPos + mRect.left;// width - getPaddingRight();
            mRect.top = centerY - 10;
            mRect.bottom = centerY + 10;
            canvas.drawRect(mRect, mPaint);
        }

        for (Section section : mSections) {
            section.doDraw(canvas);
        }

        super.onDraw(canvas);
    }

    public float getCurrentProgress() {
        return mCurrentProgress;
    }

    public boolean selectLastSection() {
        if (mSections.size() > 0) {
            Section lastSection = mSections.get(mSections.size() - 1);
            if (mCurrentProgress > lastSection.end) {
                throw new RuntimeException("current progress > last block end, add a block first, then do selectLastSection.");
            }
            boolean ret = lastSection.mIsSelected;
            lastSection.setSelection(true);
            postInvalidate();
            return ret;
        }

        return false;
    }

    /**
     * section
     */
    private class Section {
        private final Paint mBlockPaint;

        private final Paint mPaint;
        private RectF mSectionRect;
        private RectF mBlockRect;

        public Section(float start, float end) {
            this.mPaint = new Paint();
            this.mPaint.setColor(getResources().getColor(R.color.colorAccent));
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

                mSectionRect.top = centerY - 10;
                mSectionRect.bottom = centerY + 10;
            }

            if (mBlockRect == null) {
                mBlockRect = new RectF();
                int blockStartPos = (int) mSectionRect.right - mBlockWidth / 2;
                int blockEndPos = blockStartPos + mBlockWidth / 2;

                mBlockRect.left = blockStartPos;
                mBlockRect.right = blockEndPos;
                mBlockRect.top = centerY - 15;
                mBlockRect.bottom = centerY + 15;
            }

            if (mIsSelected && mAnimTimer != null) {
                int alpha = mAnimTimer.getAlpha();
                mPaint.setColor(Color.parseColor("#ff00ff"));
                mPaint.setAlpha(alpha);
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
            } else {
                mPaint.setColor(getResources().getColor(R.color.colorAccent));
            }
            canvas.drawRect(mSectionRect, mPaint);
            canvas.drawRect(mBlockRect, mBlockPaint);

        }

        public void setSelection(boolean flag) {
            mIsSelected = flag;

            if (flag && mAnimTimer == null) {
                mAnimTimer = new AnimationTimer();
                mAnimTimer.start();
            } else if (!flag && mAnimTimer != null) {
                mAnimTimer.stop();
                mAnimTimer = null;
            }
        }
    }

    /**
     * for anim.
     */
    private class AnimationTimer {
        public static final int MAX_ALPHA = 200;
        public static final int MIN_ALPHA = 50;
        public static final int ALPHA_STEP = 20;

        TimerTask timerTask;
        Timer timer;
        int alpha = MAX_ALPHA;

        byte animDirection = 1;// 0:fade in, 1: fade out.

        public AnimationTimer() {
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    switch (animDirection) {
                        case 0:
                            if (alpha < MAX_ALPHA) {
                                alpha += ALPHA_STEP;
                                if (alpha > MAX_ALPHA) {
                                    alpha = MAX_ALPHA;
                                }
                                postInvalidate();
                            } else {
                                alpha = MAX_ALPHA;
                                animDirection = 1;
                            }
                            break;
                        case 1:
                            if (alpha > MIN_ALPHA) {
                                alpha -= ALPHA_STEP;
                                if (alpha < MIN_ALPHA) {
                                    alpha = MIN_ALPHA;
                                }
                                postInvalidate();
                            } else {
                                alpha = MIN_ALPHA;
                                animDirection = 0;
                            }
                            break;
                    }
                }
            };
        }

        public void start() {
            timer.schedule(timerTask, 0, 80);
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

        public int getAlpha() {
            return alpha;
        }
    }
}
