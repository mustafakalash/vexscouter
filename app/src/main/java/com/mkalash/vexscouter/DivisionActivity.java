package com.mkalash.vexscouter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DivisionActivity extends AppCompatActivity {
    private String name;
    private String sku;
    private List<String> divisions = new ArrayList();

    private Menu menu;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.event_toolbar, menu);
        this.menu = menu;

        final SharedPreferences sharedPref = getSharedPreferences("com.mkalash.vexscouter.favorites", Context.MODE_PRIVATE);
        Set<String> favoriteEvents = sharedPref.getStringSet("favorite_events", new HashSet<String>());

        if(favoriteEvents.contains(name)) {
            menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_star_big_on));
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_refresh:
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recreate();
                    }
                }, 1);
                return true;
            case R.id.action_favorite:
                final SharedPreferences sharedPref = getSharedPreferences("com.mkalash.vexscouter.favorites", Context.MODE_PRIVATE);
                Set<String> favoriteEventsPref = sharedPref.getStringSet("favorite_events", new HashSet<String>());
                Set<String> favoriteEvents = new HashSet<>(favoriteEventsPref);
                SharedPreferences.Editor prefEditor = sharedPref.edit();
                if(favoriteEvents.contains(name)) {
                    favoriteEvents.remove(name);
                    menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_star_big_off));
                } else {
                    favoriteEvents.add(name);
                    menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_star_big_on));
                }
                prefEditor.putStringSet("favorite_events", favoriteEvents);
                prefEditor.apply();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
