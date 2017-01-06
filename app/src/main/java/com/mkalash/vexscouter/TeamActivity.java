package com.mkalash.vexscouter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import static com.mkalash.vexscouter.MainActivity.fetchJSON;
import static com.mkalash.vexscouter.MainActivity.getFullResults;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeamActivity extends AppCompatActivity {

    private String teamNumber;
    private String sku;
    private String eventName;
    private final RetrieveRating retrieveRatingTask = new RetrieveRating();
    private Menu menu;
    private float vRating = 0;
    private String teamName;
    private String organization;
    private int eventRank = 0;

    class RetrieveRating extends AsyncTask<RatingBar, Integer, Void> {

        private ProgressBar progressBar;
        private RatingBar ratingBar;

        void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        @Override
        protected Void doInBackground(RatingBar... params) {
            this.ratingBar = params[0];

            try {
                String urlString = "https://api.vexdb.io/v1/get_season_rankings?season=" + getString(R.string.current_season) + "&program=VRC&nodata=true";
                float size = (float) fetchJSON(urlString).getInt("size");

                urlString = "https://api.vexdb.io/v1/get_season_rankings?season=" + getString(R.string.current_season) + "&team=" + teamNumber;
                JSONArray result = getFullResults(urlString);
                if(result.length() > 0 && size > 0) {
                    JSONObject team = result.getJSONObject(0);
                    float vRatingRank = (float) team.getInt("vrating_rank");
                    vRating = 10f - ((10f * vRatingRank) / size);
                }

                urlString = "https://api.vexdb.io/v1/get_teams?team=" + teamNumber;
                result = getFullResults(urlString);
                if(result.length() > 0) {
                    JSONObject team = result.getJSONObject(0);
                    teamName = team.getString("team_name");
                    organization = team.getString("organisation");
                }

                if(sku != null) {
                    urlString = "https://api.vexdb.io/v1/get_rankings?sku=" + sku + "&team=" + teamNumber;
                    result = getFullResults(urlString);
                    if(result.length() > 0) {
                        eventRank = result.getJSONObject(0).getInt("rank");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressBar.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Void result) {
            ratingBar.setRating(vRating);

            ((TextView) findViewById(R.id.team_name)).setText(teamName);
            ((TextView) findViewById(R.id.team_school)).setText(organization);

            if(sku != null) {
                if(eventRank > 0) {
                    ((TextView) findViewById(R.id.event_info_header)).setText(String.format(getString(R.string.event_info), eventRank, eventName));
                } else {
                    ((View) findViewById(R.id.event_info_header).getParent()).setVisibility(View.GONE);
                }
            }

            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);

        Intent intent = getIntent();
        teamNumber = intent.getStringExtra("TEAM_NUM");
        sku = intent.getStringExtra("EVENT_SKU");
        eventName = intent.getStringExtra("EVENT_NAME");

        String title = teamNumber;
        if(eventName != null) {
            title += " @ " + eventName;
        }
        setTitle(title);

        ProgressBar progress = (ProgressBar) findViewById(R.id.team_progress);
        RatingBar ratingBar = (RatingBar) findViewById(R.id.vrating);
        if (retrieveRatingTask.getStatus() == AsyncTask.Status.RUNNING) {
            retrieveRatingTask.cancel(true);
        }
        retrieveRatingTask.setProgressBar(progress);
        retrieveRatingTask.execute(ratingBar);

        final EditText notesEditText = (EditText) findViewById(R.id.notes_edit_text);

        final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String loadedNotes = sharedPref.getString("notes_team_" + teamNumber, "");

        notesEditText.setText(loadedNotes);

        class notesSaveListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor prefEditor = sharedPref.edit();
                String teamNotes = notesEditText.getText().toString();
                prefEditor.putString("notes_team_" + teamNumber, teamNotes);
                prefEditor.apply();
            }
        }

        Button saveButton = (Button) findViewById(R.id.save_notes);
        saveButton.setOnClickListener(new notesSaveListener());

        if(sku != null) {
            findViewById(R.id.event_information).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.event_info_header)).setText(String.format(getString(R.string.event_info), eventRank, eventName));
            ListView eventSchedule = (ListView) findViewById(R.id.event_schedule);
            EventActivity.RetrieveMatches retrieveMatchesTask = new EventActivity.RetrieveMatches();
            retrieveMatchesTask.setTeam(teamNumber);
            retrieveMatchesTask.execute(eventSchedule);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.team_toolbar, menu);
        this.menu = menu;

        final SharedPreferences sharedPref = getSharedPreferences("com.mkalash.vexscouter.favorites", Context.MODE_PRIVATE);
        Set<String> favoriteTeams = sharedPref.getStringSet("favorite_teams", new HashSet<String>());

        if(favoriteTeams.contains(teamNumber)) {
            menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_star_big_on));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_favorite:
                final SharedPreferences sharedPref = getSharedPreferences("com.mkalash.vexscouter.favorites", Context.MODE_PRIVATE);
                Set<String> favoriteTeamsPref = sharedPref.getStringSet("favorite_teams", new HashSet<String>());
                Set<String> favoriteTeams = new HashSet<>(favoriteTeamsPref);
                SharedPreferences.Editor prefEditor = sharedPref.edit();
                if(favoriteTeams.contains(teamNumber)) {
                    favoriteTeams.remove(teamNumber);
                    menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_star_big_off));
                } else {
                    favoriteTeams.add(teamNumber);
                    menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_star_big_on));
                }
                prefEditor.putStringSet("favorite_teams", favoriteTeams);
                prefEditor.apply();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
