package com.cocoa.myjavaapplication;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class ProfileDeleteDialog extends Dialog {
    private TextView textContents;
    private Button btnYes;
    private Button btnNo;

    public ProfileDeleteDialog(@NonNull Context context, String contents) {
        super(context);
        setContentView(R.layout.activity_profile_delete_dialog);

        textContents = findViewById(R.id.tvProfiledeleteContent);
        textContents.setText(contents);
        btnNo = findViewById(R.id.btn_profiledelete_no);
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        btnYes = findViewById(R.id.btn_profiledelete_yes);
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.resetProfile();
                ((MainActivity)context).refreshTitle();    //다른 액티비티의 메서드 호출
                dismiss();
            }
        });
    }
}
