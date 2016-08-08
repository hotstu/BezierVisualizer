package com.example.slab.beziervisualizer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    BezierView bv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bv = (BezierView) findViewById(R.id.bv);
    }

    public void addlevel(View view) {
        bv.setLevel(Math.min(25, bv.getLevel() + 1));
    }

    public void dellevel(View view) {
        bv.setLevel(Math.max(1, bv.getLevel() - 1));
    }

    public void accelerate(View view) {
        bv.setDuration(Math.max(1000, bv.getDuration() - 1000));
    }

    public void decelerate(View view) {
        bv.setDuration(bv.getDuration() + 1000);
    }
}
