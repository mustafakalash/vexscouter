package com.mkalash.vexscouter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Image;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.Space;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class EventActivity extends AppCompatActivity {

    private String name;
    private String sku;

    private RetrieveMatches retrieveMatchesTask = new RetrieveMatches();

    class Match {
        private String red1;
        private String red2;
        private String red3;
        private String redSit;
        private String blue1;
        private String blue2;
        private String blue3;
        private String blueSit;
        private int redScore;
        private int blueScore;
        private boolean scored;

        public Match(String red1, String red2, String red3, String redSit, String blue1, String blue2, String blue3, String blueSit, int redScore, int blueScore, boolean scored) {
            this.red1 = red1;
            this.red2 = red2;
            this.red3 = red3;
            this.redSit = redSit;
            this.blue1 = blue1;
            this.blue2 = blue2;
            this.blue3 = blue3;
            this.blueSit = blueSit;
            this.redScore = redScore;
            this.blueScore = blueScore;
            this.scored = scored;
        }

        public String getRed1() {
            return red1;
        }

        public String getRed2() {
            return red2;
        }

        public String getBlue1() {
            return blue1;
        }

        public String getBlue2() {
            return blue2;
        }

        public int getRedScore() {
            return redScore;
        }

        public int getBlueScore() {
            return blueScore;
        }

        public String getRed3() {
            return red3;
        }

        public String getRedSit() {
            return redSit;
        }

        public String getBlue3() {
            return blue3;
        }

        public String getBlueSit() {
            return blueSit;
        }

        public boolean isScored() {
            return scored;
        }
    }

    class TeamClickListener implements Button.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(EventActivity.this, TeamActivity.class);
            intent.putExtra("TEAM_NUM", ((Button) view).getText());
            startActivity(intent);
        }
    }

    class RetrieveMatches extends AsyncTask<LinearLayout, Integer, ArrayList<Match>> {

        private ProgressBar progressBar;
        private LinearLayout matchList;
        ArrayList<Match> matches = new ArrayList<Match>();

        public void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        @Override
        protected ArrayList<Match> doInBackground(LinearLayout... params) {
            this.matchList = params[0];
            try {
                String urlString = "https://api.vexdb.io/v1/get_matches?sku=" + sku;
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
                    JSONObject match = result.getJSONObject(i);
                    String red1 = match.getString("red1");
                    String red2 = match.getString("red2");
                    String red3 = match.getString("red3");
                    String redSit = match.getString("redsit");
                    String blue1 = match.getString("blue1");
                    String blue2 = match.getString("blue2");
                    String blue3 = match.getString("blue3");
                    String blueSit = match.getString("bluesit");
                    int redScore = match.getInt("redscore");
                    int blueScore = match.getInt("bluescore");
                    boolean scored = "1".equals(match.getString("scored"));
                    Match matchObj = new Match(red1, red2, red3, redSit, blue1, blue2, blue3, blueSit, redScore, blueScore, scored);
                    matches.add(matchObj);
                    publishProgress((int) (((i + 1) / (float) result.length()) * 100));
                    if(isCancelled()) {
                        break;
                    }
                }
                return matches;
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
        protected void onPostExecute(ArrayList<Match> matches) {
            int redResult = ResourcesCompat.getColor(getResources(), R.color.redResult, null);
            int redResultOut = ResourcesCompat.getColor(getResources(), R.color.redResultOut, null);
            int blueResult = ResourcesCompat.getColor(getResources(), R.color.blueResult, null);
            int blueResultOut = ResourcesCompat.getColor(getResources(), R.color.blueResultOut, null);
            int accentColor = ResourcesCompat.getColor(getResources(), R.color.colorAccent, null);

            LinearLayout.LayoutParams matchRowParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            matchRowParams.setMargins(0, 10, 0, 10);

            LinearLayout.LayoutParams teamRowParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

            TeamClickListener teamClickListener = new TeamClickListener();

            for(Match match : matches) {
                LinearLayout matchRow = new LinearLayout(EventActivity.this);
                matchRow.setOrientation(LinearLayout.VERTICAL);
                matchRow.setLayoutParams(matchRowParams);

                LinearLayout redRow = new LinearLayout(EventActivity.this);
                redRow.setOrientation(LinearLayout.HORIZONTAL);
                redRow.setLayoutParams(teamRowParams);
                redRow.setBackgroundColor(redResult);
                matchRow.addView(redRow);

                LinearLayout blueRow = new LinearLayout(EventActivity.this);
                blueRow.setOrientation(LinearLayout.HORIZONTAL);
                blueRow.setLayoutParams(teamRowParams);
                blueRow.setBackgroundColor(blueResult);
                matchRow.addView(blueRow);

                Button red1 = new Button(EventActivity.this);
                red1.setOnClickListener(teamClickListener);
                Button red2 = new Button(EventActivity.this);
                red2.setOnClickListener(teamClickListener);
                Button red3 = new Button(EventActivity.this);
                red3.setOnClickListener(teamClickListener);
                Button blue1 = new Button(EventActivity.this);
                blue1.setOnClickListener(teamClickListener);
                Button blue2 = new Button(EventActivity.this);
                blue2.setOnClickListener(teamClickListener);
                Button blue3 = new Button(EventActivity.this);
                blue3.setOnClickListener(teamClickListener);

                TextView redScore = new TextView(EventActivity.this);
                TextView blueScore = new TextView(EventActivity.this);

                red1.setText(match.getRed1());
                if(match.getRed1().equals(match.getRedSit())) {
                    red1.setBackgroundColor(redResultOut);
                } else {
                    red1.setBackgroundColor(redResult);
                }
                redRow.addView(red1);

                red2.setText(match.getRed2());
                if(match.getRed2().equals(match.getRedSit())) {
                    red2.setBackgroundColor(redResultOut);
                } else {
                    red2.setBackgroundColor(redResult);
                }
                redRow.addView(red2);

                red3.setText(match.getRed3());
                if(match.getRed3().equals(match.getRedSit())) {
                    red3.setBackgroundColor(redResultOut);
                } else {
                    red3.setBackgroundColor(redResult);
                }
                if(!match.getRed3().isEmpty()) {
                    redRow.addView(red3);
                }

                blue1.setText(match.getBlue1());
                if(match.getBlue1().equals(match.getBlueSit())) {
                    blue1.setBackgroundColor(blueResultOut);
                } else {
                    blue1.setBackgroundColor(blueResult);
                }
                blueRow.addView(blue1);

                blue2.setText(match.getBlue2());
                if(match.getBlue2().equals(match.getBlueSit())) {
                    blue2.setBackgroundColor(blueResultOut);
                } else {
                    blue2.setBackgroundColor(blueResult);
                }
                blueRow.addView(blue2);

                blue3.setText(match.getBlue3());
                if(match.getBlue3().equals(match.getBlueSit())) {
                    blue3.setBackgroundColor(blueResultOut);
                } else {
                    blue3.setBackgroundColor(blueResult);
                }
                if(!match.getBlue3().isEmpty()) {
                    blueRow.addView(blue3);
                }

                if(match.isScored()) {
                    redScore.setText(Integer.toString(match.getRedScore()));
                    redScore.setTextSize(25f);
                    if(match.getRedScore() > match.getBlueScore()) {
                        redScore.setTypeface(null, Typeface.BOLD);
                    }
                    redRow.addView(redScore);

                    blueScore.setText(Integer.toString(match.getBlueScore()));
                    blueScore.setTextSize(25f);
                    if(match.getBlueScore() > match.getRedScore()) {
                        blueScore.setTypeface(null, Typeface.BOLD);
                    }
                    blueRow.addView(blueScore);
                }

                matchList.addView(matchRow);
            }

            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        name = intent.getStringExtra("EVENT_NAME");
        sku = intent.getStringExtra("EVENT_SKU");
        setTitle(name);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.match_progress);
        LinearLayout matchList = (LinearLayout) findViewById(R.id.match_list);

        if (retrieveMatchesTask.getStatus() == AsyncTask.Status.RUNNING) {
            retrieveMatchesTask.cancel(true);
        }
        retrieveMatchesTask.setProgressBar(progressBar);
        retrieveMatchesTask.execute(matchList);
    }
}
