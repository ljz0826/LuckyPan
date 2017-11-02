package com.ljz.luckypan;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private LuckyPan luckyPan;
    private ImageView iv_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        luckyPan = findViewById(R.id.luckpan);
        iv_start = findViewById(R.id.iv_start);

        iv_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!luckyPan.isStart()){
                    luckyPan.luckyStart(1);
                    iv_start.setImageResource(R.drawable.stop);
                }else{
                    if(!luckyPan.isShouldEnd())
                    {
                        luckyPan.luckyEnd();
                        iv_start.setImageResource(R.drawable.start);
                    }
                }
            }
        });
    }
}
