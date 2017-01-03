package com.nutty.growingview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final GrowingView launchView = (GrowingView)findViewById(R.id.opening_view);
        button = (Button) findViewById(R.id.button);
        final View thisView = findViewById(R.id.activity_main);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("this","clicked");

                launchView.setX(button.getX() + (button.getWidth() / 2));
                launchView.setY(button.getY() + (button.getHeight() / 2));

                launchView.startGrowAnimation(32, 32 ,thisView.getWidth() - 32 ,thisView.getHeight() -32 , 500);
            }
        });

        launchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchView.startShrinkAnimation();
            }
        });
    }
}
