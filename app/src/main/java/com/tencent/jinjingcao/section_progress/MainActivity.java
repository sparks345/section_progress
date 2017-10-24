package com.tencent.jinjingcao.section_progress;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {

    private SectionProgressBar secBar;
    private SectionProgressBar secBar2;
    private Button btnIncreaseProgress;
    private Button btnAddBlock;
    private Button btnDelBlock;
    private Button btnReset;

    private void assignViews() {
        secBar = (SectionProgressBar) findViewById(R.id.sec_bar);
        secBar2 = (SectionProgressBar) findViewById(R.id.sec_bar2);

        btnIncreaseProgress = (Button) findViewById(R.id.btnIncreaseProgress);
        btnAddBlock = (Button) findViewById(R.id.btnAddBlock);
        btnDelBlock = (Button) findViewById(R.id.btnDelBlock);
        btnReset = (Button) findViewById(R.id.btnReset);

        btnIncreaseProgress.setOnClickListener(this);
        btnAddBlock.setOnClickListener(this);
        btnDelBlock.setOnClickListener(this);
        btnReset.setOnClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        assignViews();
    }

    private void init() {
        secBar = (SectionProgressBar) findViewById(R.id.sec_bar);
        secBar.setProgress(25.0f);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnIncreaseProgress:
                float aimProgress = secBar.getCurrentProgress() + 15.0f;
                if (aimProgress > 100) {
                    aimProgress = 100;
                }
                secBar.setProgress(aimProgress);
                secBar2.setProgress(aimProgress);
                break;
            case R.id.btnAddBlock:
                secBar.setSplitAtCurrent();
                secBar2.setSplitAtCurrent();
                break;
            case R.id.btnDelBlock:
                if (secBar.selectLastSection()) {
                    secBar.backDelSection();
                }

                if (secBar2.selectLastSection()) {
                    secBar2.backDelSection();
                }
                break;
            case R.id.btnReset:
                secBar.reset();
                secBar2.reset();
                break;
        }
    }
}
