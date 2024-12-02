package com.cocoa.myjavaapplication;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileViewActivity extends AppCompatActivity {

    private Handler handler;
    Button btnOk;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);
        handler = new Handler();

        showProfile();

        btnOk = findViewById(R.id.btn_profileview_ok);
        btnOk.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void showProfile() {
        TextView title = findViewById(R.id.text_profileview_title);
        title.setText(Messages.get("%s님의 프로필", MainActivity.user.name));

        TextView weight = findViewById(R.id.text_profileview_maxweight);
        weight.setText(Messages.get("최대 하중: %d Kg", MainActivity.user.maxWeight));
    }
}
