package com.mkalash.vexscouter;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class TeamActivity extends AppCompatActivity {

    private String teamNumber;
    private String eventSKU;
    private String eventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);

        Intent intent = getIntent();
        teamNumber = intent.getStringExtra("TEAM_NUM");
        eventSKU = intent.getStringExtra("EVENT_SKU");
        eventName = intent.getStringExtra("EVENT_NAME");
        setTitle(teamNumber);
    }
}
