package com.example.smartcampuscompanion;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class AddScheduledEventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_scheduled_event);

        EditText etTitle = findViewById(R.id.et_title);
        EditText etDate = findViewById(R.id.et_date);
        EditText etType = findViewById(R.id.et_type);
        EditText etPriority = findViewById(R.id.et_priority);
        Button btnSave = findViewById(R.id.btn_save);

        btnSave.setOnClickListener(v -> {

            EventSchedulerListActivity.EVENT_LIST.add(
                    new ScheduledEventModel(
                            etTitle.getText().toString(),
                            etDate.getText().toString(),
                            etType.getText().toString(),
                            etPriority.getText().toString()
                    )
            );

            finish();
        });
    }
}
