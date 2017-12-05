package com.zp.rcsv.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.devilist.cardstackview.CardRecyclerView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tv_tower).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardStackActivity.start(MainActivity.this, "tower");
            }
        });
        findViewById(R.id.tv_poker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardStackActivity.start(MainActivity.this, "poker");
            }
        });
    }
}
