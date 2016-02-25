package com.kalei.activities;

import com.kalei.pholocation.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Created by risaki on 2/24/16.
 */
public class IntroActivity extends PhotoLocationActivity {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        Button b = (Button) findViewById(R.id.intro_btn);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                startActivity(new Intent(IntroActivity.this, MainActivity.class));
            }
        });
    }
}
