package com.cocoa.myjavaapplication;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileSetActivity extends AppCompatActivity {

    private Handler handler;
    Button btnOk;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_set);
        handler = new Handler();

        btnOk = findViewById(R.id.btn_profileset_ok);
        btnOk.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                okBtnPressed();
            }
        });
    }

    private void okBtnPressed() {
        EditText nameEdit = findViewById(R.id.input_profileset_name);
        EditText weightEdit = findViewById(R.id.input_profileset_weight);
        String name = nameEdit.getText().toString();
        if (name.equals("")) {
            name = MainActivity.defaultUser.name;
        }
        String weightInput = weightEdit.getText().toString();
        if (weightInput.equals("")) {
            weightInput = "0";
        }
        int weight = Integer.parseInt(weightInput);
        MainActivity.user = new User(name, weight);
        finish();
        ((MainActivity)MainActivity.context).refreshTitle();    //다른 액티비티의 메서드 호출
    }
}
