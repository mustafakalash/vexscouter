package com.mkalash.vexscouter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    private RetrieveEvents retrieveEventsTask = new RetrieveEvents();

    class RetrieveEvents extends AsyncTask<ArrayAdapter<String>, Integer, Map<String, String>> {

        private ProgressBar progressBar;
        private ArrayAdapter<String> eventListAdapter;
        private Map<String, String> events = new TreeMap<String, String>();

        public void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        public void setEventsMap(Map<String, String> events) {
            this.events = events;
        }

        @Override
        protected Map<String, String> doInBackground(ArrayAdapter<String>... params) {
            this.eventListAdapter = params[0];
            try {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                String season = sharedPref.getString("filter_season", "");
                season = (season.equals("Any")) ? "" : season;
                String country = sharedPref.getString("filter_country", "");
                country = (country.equals("Any")) ? "" : country;
                String region = sharedPref.getString("filter_region", "");
                region = (region.equals("Any")) ? "" : region;

                String urlString = ("https://api.vexdb.io/v1/get_events?season=" + season + "&country=" + country + "&region=" + region).replace(" ", "%20");
                StringBuilder json = new StringBuilder();
                URL url = new URL(urlString);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                try {
                    String str;
                    int i = 1;
                    while ((str = in.readLine()) != null) {
                        json.append(str);
                        if(isCancelled()) {
                            break;
                        }
                    }
                } finally {
                    in.close();
                }
                JSONArray result = new JSONObject(json.toString()).getJSONArray("result");
                for(int i = 0; i < result.length(); i++) {
                    JSONObject event = result.getJSONObject(i);
                    String name = event.getString("name");
                    String sku = event.getString("sku");
                    events.put(name, sku);
                    publishProgress((int) (((i + 1) / (float) result.length()) * 100));
                    if(isCancelled()) {
                        break;
                    }
                }
                return events;
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
        protected void onPostExecute(Map<String, String> events) {
            eventListAdapter.clear();
            eventListAdapter.addAll(events.keySet());
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }
    }

    class EventListClickListener implements AdapterView.OnItemClickListener {

        private Map<String, String> events;

        public EventListClickListener(Map<String, String> events) {
            this.events = events;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String name = parent.getItemAtPosition(position).toString();
            String sku = events.get(name);
            Intent intent = new Intent(MainActivity.this, EventActivity.class);
            intent.putExtra("EVENT_NAME", name);
            intent.putExtra("EVENT_SKU", sku);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.filters, false);

        Map<String, String> events = new TreeMap<String, String>();

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.event_progress);
        ListView eventList = (ListView) findViewById(R.id.event_list);
        ArrayAdapter<String> eventListAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1);
        eventList.setAdapter(eventListAdapter);
        eventList.setOnItemClickListener(new EventListClickListener(events));

        if (retrieveEventsTask.getStatus() == AsyncTask.Status.RUNNING) {
            retrieveEventsTask.cancel(true);
        }
        retrieveEventsTask.setProgressBar(progressBar);
        retrieveEventsTask.setEventsMap(events);
        retrieveEventsTask.execute(eventListAdapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_filter:
                Intent intent = new Intent(this, FilterActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
