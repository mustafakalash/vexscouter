package com.mkalash.vexscouter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DivisionActivity extends AppCompatActivity {
    private String name;
    private String sku;
    private List<String> divisions = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_division);

        Intent intent = getIntent();
        name = intent.getStringExtra("EVENT_NAME");
        sku = intent.getStringExtra("EVENT_SKU");
        setTitle(name);

        DivisionListClickListener divisionListClickListener = new DivisionListClickListener();
        RetrieveDivisions retrieveDivisionsTask = new RetrieveDivisions();

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.division_progress);
        ListView divisionList = (ListView) findViewById(R.id.division_list);
        final ArrayAdapter<String> divisionListAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1);
        divisionList.setAdapter(divisionListAdapter);
        divisionList.setOnItemClickListener(divisionListClickListener);

        retrieveDivisionsTask.setProgressBar(progressBar);
        retrieveDivisionsTask.execute(divisionListAdapter);
    }

    class DivisionListClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String division = parent.getItemAtPosition(position).toString();
            Intent intent;
            if(divisions.contains(division)) {
                intent = new Intent(DivisionActivity.this, EventActivity.class);
                intent.putExtra("DIVISION", division);
                intent.putExtra("EVENT_NAME", name);
                intent.putExtra("EVENT_SKU", sku);
            } else {
                intent = new Intent(DivisionActivity.this, SkillsActivity.class);
                intent.putExtra("EVENT_NAME", name);
                intent.putExtra("EVENT_SKU", sku);
            }
            DivisionActivity.this.startActivity(intent);
        }
    }

    class RetrieveDivisions extends AsyncTask<ArrayAdapter<String>, Integer, List<String>> {

        private ProgressBar progressBar;
        private ArrayAdapter<String> divisionListAdapter;

        void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        @SafeVarargs
        @Override
        protected final List<String> doInBackground(ArrayAdapter<String>... params) {
            this.divisionListAdapter = params[0];
            divisions.clear();
            try {
                String urlString = "https://api.vexdb.io/v1/get_events?sku=" + sku;
                JSONArray result = MainActivity.getFullResults(urlString);
                for(int i = 0; i < result.length(); i++) {
                    JSONObject event = result.getJSONObject(i);
                    JSONArray jsonDivisions = event.getJSONArray("divisions");
                    for(int j = 0; j < jsonDivisions.length(); j++) {
                        divisions.add(jsonDivisions.getString(j));
                    }
                    publishProgress((int) (((i + 1) / (float) result.length()) * 100));
                    if(isCancelled()) {
                        break;
                    }
                }
                return divisions;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressBar.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(List<String> divisions) {
            divisionListAdapter.clear();
            divisionListAdapter.addAll(divisions);
            divisionListAdapter.add(getString(R.string.skills_tab));
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }
    }
}
