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
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;

public class TeamActivity extends AppCompatActivity {

    private String teamNumber;
    private String eventSKU;
    private String eventName;
    private RetrieveRating retrieveRatingTask = new RetrieveRating();

    class RetrieveRating extends AsyncTask<RatingBar, Integer, float[]> {

        private ProgressBar progressBar;
        private RatingBar ratingBar;

        public void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        @Override
        protected float[] doInBackground(RatingBar... params) {
            this.ratingBar = params[0];
            float[] returnValue = new float[2];
            try {
                String urlString = "https://api.vexdb.io/v1/get_season_rankings?season=" + getString(R.string.current_season) + "&team=" + teamNumber;
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
                float vRating = (float) team.getInt("vrating_rank");
                returnValue[0] = vRating;
                publishProgress(50);

                urlString = "https://api.vexdb.io/v1/get_season_rankings?season=" + getString(R.string.current_season) + "&program=VRC&nodata=true";
                json = new StringBuilder();
                url = new URL(urlString);
                in = new BufferedReader(new InputStreamReader(url.openStream()));
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
                int size = new JSONObject(json.toString()).getInt("size");
                publishProgress(100);

                returnValue[1] = (float) size;
                return returnValue;
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
        protected void onPostExecute(float[] result) {
            float rating = 10f - ((10f * result[0]) / result[1]);
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
