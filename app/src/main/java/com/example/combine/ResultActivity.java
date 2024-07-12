package com.example.combine;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    private Button backButton;
    private TextView resultTextview;
    private String resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        resultTextview = findViewById(R.id.result_textview);
        backButton = findViewById(R.id.back_button);
        resultText = getIntent().getStringExtra(LCOTaceDetection.RESULT_TEXT);

        resultTextview.setText(resultText);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
