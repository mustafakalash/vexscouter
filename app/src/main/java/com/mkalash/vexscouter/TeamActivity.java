package com.mkalash.vexscouter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Rating;
import android.os.AsyncTask;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class TeamActivity extends AppCompatActivity {

    private String teamNumber;
    private String eventSKU;
    private String eventName;
    private RetrieveRating retrieveRatingTask = new RetrieveRating();

    class RetrieveRating extends AsyncTask<RatingBar, Integer, Integer> {

        private ProgressBar progressBar;
        private RatingBar ratingBar;

        public void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        @Override
        protected Integer doInBackground(RatingBar... params) {
            this.ratingBar = params[0];
            try {
                String urlString = "https://api.vexdb.io/v1/get_season_rankings?season=Starstruck&team=" + teamNumber;
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
                JSONObject team = result.getJSONObject(0);
                int vRating = team.getInt("vrating_rank");
                publishProgress(100);
                return vRating;
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
        protected void onPostExecute(Integer vRating) {
            float rating = 10 - ((10 * (float) vRating) / 5000);
            ratingBar.setRating(rating);

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
        eventSKU = intent.getStringExtra("EVENT_SKU");
        eventName = intent.getStringExtra("EVENT_NAME");
        setTitle(teamNumber);

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
                prefEditor.commit();
            }
        }

        Button saveButton = (Button) findViewById(R.id.save_notes);
        saveButton.setOnClickListener(new notesSaveListener());
    }
}
