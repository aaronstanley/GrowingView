package com.nutty.growingview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;


public class MainActivity extends AppCompatActivity implements GrowingView.GrowingViewListener {

    View button;
    View attachment_view;
    GrowingView launchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        launchView = (GrowingView) findViewById(R.id.opening_view);
        button = findViewById(R.id.button);
        final View thisView = findViewById(R.id.activity_main);
        attachment_view = getLayoutInflater().inflate(R.layout.attachment_view, null);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchView.setVisibility(View.VISIBLE);
                launchView.startGrowAnimation(32, 32, thisView.getWidth() - 32, thisView.getHeight() - 32, 500, MainActivity.this);
            }
        });

        launchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchView.startShrinkAnimation(MainActivity.this);
            }
        });
    }

    @Override
    public void growingStarted() {

    }

    @Override
    public void growingFinished() {
        attachment_view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        launchView.addView(attachment_view);
    }

    @Override
    public void shrinkStarted() {
        launchView.removeView(attachment_view);
    }

    @Override
    public void shrinkFinished() {
        launchView.setVisibility(View.INVISIBLE);
    }
}
