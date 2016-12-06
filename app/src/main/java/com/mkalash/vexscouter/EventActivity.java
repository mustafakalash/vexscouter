package com.mkalash.vexscouter;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class EventActivity extends AppCompatActivity {

    private static String name;
    private static String sku;

    static class Skill {
        private String team;
        private int rank;
        private int attempts;
        private int score;
        private int type;

        public Skill(String team, int rank, int attempts, int score, int type) {
            this.team = team;
            this.rank = rank;
            this.attempts = attempts;
            this.score = score;
            this.type = type;
        }

        public String getTeam() {
            return team;
        }

        public int getRank() {
            return rank;
        }

        public int getAttempts() {
            return attempts;
        }

        public int getScore() {
            return score;
        }

        public int getType() {
            return type;
        }
    }

    static class Rank {
        private String team;
        private int rank;
        private int wp;
        private int ap;
        private int trsp;
        private double ccwm;

        public Rank(String team, int rank, int wp, int ap, int trsp, double ccwm) {
            this.team = team;
            this.rank = rank;
            this.wp = wp;
            this.ap = ap;
            this.trsp = trsp;
            this.ccwm = ccwm;
        }

        public String getTeam() {
            return team;
        }

        public int getRank() {
            return rank;
        }

        public int getWP() {
            return wp;
        }

        public int getAP() {
            return ap;
        }

        public int getTRSP() {
            return trsp;
        }

        public double getCCWM() {
            return ccwm;
        }
    }

    static class Match {
        private String name;
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

        public Match(String name, String red1, String red2, String red3, String redSit, String blue1, String blue2, String blue3, String blueSit, int redScore, int blueScore, boolean scored) {
            this.name = name;
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

        public String getName() {
            return name;
        }
    }

    static class TeamClickListener implements Button.OnClickListener {
        private boolean name;

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), TeamActivity.class);
            intent.putExtra("TEAM_NUM", ((Button) view).getText());
            view.getContext().startActivity(intent);
        }
    }

    static class RetrieveMatches extends AsyncTask<LinearLayout, Integer, ArrayList<Match>> {

        private ProgressBar progressBar;
        private LinearLayout matchTable;
        ArrayList<Match> matches = new ArrayList<Match>();

        public void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        @Override
        protected ArrayList<Match> doInBackground(LinearLayout... params) {
            this.matchTable = params[0];
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
                    String name;
                    switch(match.getInt("round")) {
                        case 1:
                            name = "Practice #" + match.getInt("matchnum");
                            break;
                        default:
                        case 2:
                            name = "Qualification #" + match.getInt("matchnum");
                            break;
                        case 3:
                            name = "Quarterfinal " + match.getInt("instance") + " #" + match.getInt("matchnum");
                            break;
                        case 4:
                            name = "Semifinal " + match.getInt("instance") + " #" + match.getInt("matchnum");
                            break;
                        case 5:
                            name = "Final #" + match.getInt("matchnum");
                    }
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
                    Match matchObj = new Match(name, red1, red2, red3, redSit, blue1, blue2, blue3, blueSit, redScore, blueScore, scored);
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
            int redResult = ResourcesCompat.getColor(matchTable.getResources(), R.color.redResult, null);
            int redResultOut = ResourcesCompat.getColor(matchTable.getResources(), R.color.redResultOut, null);
            int blueResult = ResourcesCompat.getColor(matchTable.getResources(), R.color.blueResult, null);
            int blueResultOut = ResourcesCompat.getColor(matchTable.getResources(), R.color.blueResultOut, null);
            int whiteColor = ResourcesCompat.getColor(matchTable.getResources(), R.color.white, null);

            LinearLayout.LayoutParams redRowParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            redRowParams.setMargins(0, 0, 0, 2);

            LinearLayout.LayoutParams blueRowParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            blueRowParams.setMargins(0, 0, 0, 10);

            LinearLayout.LayoutParams teamColParams =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
            teamColParams.setMargins(2, 0, 2, 0);
            teamColParams.weight = 1;

            TeamClickListener teamClickListener = new TeamClickListener();

            if(matches.size() == 0) {
                TextView emptyPage = new TextView(matchTable.getContext());
                emptyPage.setText(R.string.no_results);
                matchTable.addView(emptyPage);
                matchTable.setBackgroundColor(whiteColor);
            }

            for(Match match : matches) {
                TextView matchName = new TextView(matchTable.getContext());
                matchName.setText(match.getName());
                matchName.setLayoutParams(redRowParams);
                matchName.setBackgroundColor(whiteColor);
                matchTable.addView(matchName);

                LinearLayout redRow = new LinearLayout(matchTable.getContext());
                redRow.setOrientation(LinearLayout.HORIZONTAL);
                redRow.setLayoutParams(redRowParams);
                matchTable.addView(redRow);

                LinearLayout blueRow = new LinearLayout(matchTable.getContext());
                blueRow.setOrientation(LinearLayout.HORIZONTAL);
                blueRow.setLayoutParams(blueRowParams);
                matchTable.addView(blueRow);

                Button red1 = new Button(matchTable.getContext());
                red1.setLayoutParams(teamColParams);
                red1.setOnClickListener(teamClickListener);

                Button red2 = new Button(matchTable.getContext());
                red2.setLayoutParams(teamColParams);
                red2.setOnClickListener(teamClickListener);

                Button red3 = new Button(matchTable.getContext());
                red3.setLayoutParams(teamColParams);
                if(!match.getRed3().isEmpty()) {
                    red3.setOnClickListener(teamClickListener);
                }

                Button blue1 = new Button(matchTable.getContext());
                blue1.setLayoutParams(teamColParams);
                blue1.setOnClickListener(teamClickListener);

                Button blue2 = new Button(matchTable.getContext());
                blue2.setLayoutParams(teamColParams);
                blue2.setOnClickListener(teamClickListener);

                Button blue3 = new Button(matchTable.getContext());
                blue3.setLayoutParams(teamColParams);
                if(!match.getBlue3().isEmpty()) {
                    blue3.setOnClickListener(teamClickListener);
                }

                TextView redScore = new TextView(matchTable.getContext());
                redScore.setLayoutParams(teamColParams);
                TextView blueScore = new TextView(matchTable.getContext());
                blueScore.setLayoutParams(teamColParams);

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
                if(!match.getRed3().isEmpty() && match.getRed3().equals(match.getRedSit())) {
                    red3.setBackgroundColor(redResultOut);
                } else {
                    red3.setBackgroundColor(redResult);
                }
                redRow.addView(red3);

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
                if(!match.getBlue3().isEmpty() && match.getBlue3().equals(match.getBlueSit())) {
                    blue3.setBackgroundColor(blueResultOut);
                } else {
                    blue3.setBackgroundColor(blueResult);
                }
                blueRow.addView(blue3);

                if(!match.isScored()) {
                    redScore.setBackgroundColor(redResultOut);
                    blueScore.setBackgroundColor(blueResultOut);
                } else {
                    redScore.setBackgroundColor(redResult);
                    blueScore.setBackgroundColor(blueResult);
                }

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

            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }
    }

    static class RetrieveRankings extends AsyncTask<LinearLayout, Integer, ArrayList<Rank>> {

        private ProgressBar progressBar;
        private LinearLayout rankingTable;
        ArrayList<Rank> rankings = new ArrayList<Rank>();

        public void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        @Override
        protected ArrayList<Rank> doInBackground(LinearLayout... params) {
            this.rankingTable = params[0];
            try {
                String urlString = "https://api.vexdb.io/v1/get_rankings?sku=" + sku;
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
                JSONObject jsonObject = new JSONObject(json.toString());
                if(jsonObject.getInt("size") == 0) {
                    urlString = "https://api.vexdb.io/v1/get_teams?sku=" + sku;
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
                    JSONArray result = new JSONObject(json.toString()).getJSONArray("result");
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject rank = result.getJSONObject(i);
                        String team = rank.getString("number");
                        Rank rankObj = new Rank(team, i, 0, 0, 0, 0);
                        rankings.add(rankObj);
                        publishProgress((int) (((i + 1) / (float) result.length()) * 100));
                        if (isCancelled()) {
                            break;
                        }
                    }
                } else {
                    JSONArray result = jsonObject.getJSONArray("result");
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject rank = result.getJSONObject(i);
                        String team = rank.getString("team");
                        int rankNum = rank.getInt("rank");
                        int wp = rank.getInt("wp");
                        int ap = rank.getInt("ap");
                        int trsp = rank.getInt("trsp");
                        double ccwm = rank.getDouble("ccwm");
                        Rank rankObj = new Rank(team, rankNum, wp, ap, trsp, ccwm);
                        rankings.add(rankObj);
                        publishProgress((int) (((i + 1) / (float) result.length()) * 100));
                        if (isCancelled()) {
                            break;
                        }
                    }
                }
                return rankings;
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
        protected void onPostExecute(ArrayList<Rank> rankings) {

            int accentColor = ResourcesCompat.getColor(rankingTable.getResources(), R.color.colorAccent, null);
            int whiteColor = ResourcesCompat.getColor(rankingTable.getResources(), R.color.white, null);

            LinearLayout.LayoutParams rankRowParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rankRowParams.setMargins(0, 0, 0, 2);

            LinearLayout.LayoutParams rankColParams =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
            rankColParams.setMargins(2, 0, 2, 0);
            rankColParams.weight = 1;

            TeamClickListener teamClickListener = new TeamClickListener();

            if(rankings.size() == 0) {
                TextView emptyPage = new TextView(rankingTable.getContext());
                emptyPage.setText(R.string.no_results);
                rankingTable.addView(emptyPage);
                rankingTable.setBackgroundColor(whiteColor);
            }

            for(Rank rank : rankings) {
                LinearLayout rankRow = new LinearLayout(rankingTable.getContext());
                rankRow.setOrientation(LinearLayout.HORIZONTAL);
                rankRow.setLayoutParams(rankRowParams);

                TextView rankNum = new TextView(rankingTable.getContext());
                rankNum.setText(Integer.toString(rank.getRank()));
                rankNum.setBackgroundColor(whiteColor);
                rankNum.setLayoutParams(rankColParams);
                rankRow.addView(rankNum);

                Button team = new Button(rankingTable.getContext());
                team.setOnClickListener(teamClickListener);
                team.setText(rank.getTeam());
                team.setBackgroundColor(whiteColor);
                team.setLayoutParams(rankColParams);
                rankRow.addView(team, rankColParams);

                TextView wp = new TextView(rankingTable.getContext());
                wp.setText(Integer.toString(rank.getWP()));
                wp.setBackgroundColor(whiteColor);
                wp.setLayoutParams(rankColParams);
                rankRow.addView(wp);

                TextView ap = new TextView(rankingTable.getContext());
                ap.setText(Integer.toString(rank.getAP()));
                ap.setBackgroundColor(whiteColor);
                ap.setLayoutParams(rankColParams);
                rankRow.addView(ap);

                TextView trsp = new TextView(rankingTable.getContext());
                trsp.setText(Integer.toString(rank.getTRSP()));
                trsp.setBackgroundColor(whiteColor);
                trsp.setLayoutParams(rankColParams);
                rankRow.addView(trsp);

                TextView ccwm = new TextView(rankingTable.getContext());
                ccwm.setText(Double.toString(rank.getCCWM()));
                ccwm.setBackgroundColor(whiteColor);
                ccwm.setLayoutParams(rankColParams);
                rankRow.addView(ccwm);

                rankingTable.addView(rankRow);
            }

            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }
    }

    static class RetrieveSkills extends AsyncTask<LinearLayout, Integer, ArrayList<Skill>> {

        private ProgressBar progressBar;
        private LinearLayout skillsTable;
        ArrayList<Skill> skills = new ArrayList<Skill>();

        public void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        @Override
        protected ArrayList<Skill> doInBackground(LinearLayout... params) {
            this.skillsTable = params[0];
            try {
                String urlString = "https://api.vexdb.io/v1/get_skills?sku=" + sku;
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
                    JSONObject skill = result.getJSONObject(i);
                    String team = skill.getString("team");
                    int rank = skill.getInt("rank");
                    int attempts = skill.getInt("attempts");
                    int score = skill.getInt("score");
                    int type = skill.getInt("type");

                    Skill skillObj = new Skill(team, rank, attempts, score, type);
                    skills.add(skillObj);
                    publishProgress((int) (((i + 1) / (float) result.length()) * 100));
                    if(isCancelled()) {
                        break;
                    }
                }
                return skills;
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
        protected void onPostExecute(ArrayList<Skill> skills) {

            int accentColor = ResourcesCompat.getColor(skillsTable.getResources(), R.color.colorAccent, null);
            int whiteColor = ResourcesCompat.getColor(skillsTable.getResources(), R.color.white, null);

            LinearLayout.LayoutParams skillRowParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            skillRowParams.setMargins(0, 0, 0, 2);

            LinearLayout.LayoutParams skillColParams =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
            skillColParams.setMargins(2, 0, 2, 0);
            skillColParams.weight = 1;

            TeamClickListener teamClickListener = new TeamClickListener();

            if(skills.size() == 0) {
                TextView emptyPage = new TextView(skillsTable.getContext());
                emptyPage.setText(R.string.no_results);
                skillsTable.addView(emptyPage);
                skillsTable.setBackgroundColor(whiteColor);
            } else {
                int[] tableOrder = {2, 0, 1};
                for (int i : tableOrder) {
                    TextView tableName = new TextView(skillsTable.getContext());
                    tableName.setBackgroundColor(whiteColor);
                    tableName.setLayoutParams(skillRowParams);
                    switch (i) {
                        default:
                        case 2:
                            tableName.setText(skillsTable.getContext().getString(R.string.robot_skills));
                            break;
                        case 1:
                            tableName.setText(skillsTable.getContext().getString(R.string.auton_skills));
                            break;
                        case 0:
                            tableName.setText(skillsTable.getContext().getString(R.string.driver_skills));
                            break;
                    }
                    skillsTable.addView(tableName);

                    for (Skill skill : skills) {
                        if (skill.getType() != i) {
                            continue;
                        }

                        LinearLayout skillRow = new LinearLayout(skillsTable.getContext());
                        skillRow.setOrientation(LinearLayout.HORIZONTAL);
                        skillRow.setLayoutParams(skillRowParams);

                        TextView rank = new TextView(skillsTable.getContext());
                        rank.setText(Integer.toString(skill.getRank()));
                        rank.setBackgroundColor(whiteColor);
                        rank.setLayoutParams(skillColParams);
                        skillRow.addView(rank);

                        Button team = new Button(skillsTable.getContext());
                        team.setText(skill.getTeam());
                        team.setBackgroundColor(whiteColor);
                        team.setLayoutParams(skillColParams);
                        team.setOnClickListener(teamClickListener);
                        skillRow.addView(team);

                        TextView attempts = new TextView(skillsTable.getContext());
                        attempts.setText(Integer.toString(skill.getAttempts()));
                        attempts.setBackgroundColor(whiteColor);
                        attempts.setLayoutParams(skillColParams);
                        skillRow.addView(attempts);

                        TextView score = new TextView(skillsTable.getContext());
                        score.setText(Integer.toString(skill.getScore()));
                        score.setBackgroundColor(whiteColor);
                        score.setLayoutParams(skillColParams);
                        skillRow.addView(score);

                        skillsTable.addView(skillRow);
                    }
                }
            }

            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }
    }
    
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

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

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class EventFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public EventFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static EventFragment newInstance(int sectionNumber) {
            EventFragment fragment = new EventFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_event, container, false);

            ProgressBar fragmentProgress = (ProgressBar) rootView.findViewById(R.id.fragment_event_progress);
            LinearLayout fragmentTable = (LinearLayout) rootView.findViewById(R.id.fragment_event_table);
            LinearLayout fragmentTableHeader = (LinearLayout) rootView.findViewById(R.id.fragment_event_table_header);

            RetrieveRankings retrieveRankingsTask = new RetrieveRankings();
            RetrieveMatches retrieveMatchesTask = new RetrieveMatches();
            RetrieveSkills retrieveSkillsTask = new RetrieveSkills();

            LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            headerParams.setMargins(2, 0, 2, 5);
            headerParams.weight = 1;

            int whiteColor = ResourcesCompat.getColor(getResources(), R.color.white, null);

            switch(getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    TextView rankHeader = new TextView(fragmentTableHeader.getContext());
                    rankHeader.setText(getString(R.string.rank_header));
                    rankHeader.setBackgroundColor(whiteColor);
                    rankHeader.setLayoutParams(headerParams);
                    fragmentTableHeader.addView(rankHeader);

                    TextView teamHeader = new TextView(fragmentTableHeader.getContext());
                    teamHeader.setText(getString(R.string.team_header));
                    teamHeader.setBackgroundColor(whiteColor);
                    teamHeader.setLayoutParams(headerParams);
                    fragmentTableHeader.addView(teamHeader);

                    TextView wpHeader = new TextView(fragmentTableHeader.getContext());
                    wpHeader.setText(getString(R.string.wp_header));
                    wpHeader.setBackgroundColor(whiteColor);
                    wpHeader.setLayoutParams(headerParams);
                    fragmentTableHeader.addView(wpHeader);

                    TextView apHeader = new TextView(fragmentTableHeader.getContext());
                    apHeader.setText(getString(R.string.ap_header));
                    apHeader.setBackgroundColor(whiteColor);
                    apHeader.setLayoutParams(headerParams);
                    fragmentTableHeader.addView(apHeader);

                    TextView trspHeader = new TextView(fragmentTableHeader.getContext());
                    trspHeader.setText(getString(R.string.trsp_header));
                    trspHeader.setBackgroundColor(whiteColor);
                    trspHeader.setLayoutParams(headerParams);
                    fragmentTableHeader.addView(trspHeader);

                    TextView ccwmHeader = new TextView(fragmentTableHeader.getContext());
                    ccwmHeader.setText(getString(R.string.ccwm_header));
                    ccwmHeader.setBackgroundColor(whiteColor);
                    ccwmHeader.setLayoutParams(headerParams);
                    fragmentTableHeader.addView(ccwmHeader);

                    if (retrieveRankingsTask.getStatus() == AsyncTask.Status.RUNNING) {
                        retrieveRankingsTask.cancel(true);
                    }
                    retrieveRankingsTask.setProgressBar(fragmentProgress);
                    retrieveRankingsTask.execute(fragmentTable);
                    break;
                case 2:
                    TextView team1Header = new TextView(fragmentTableHeader.getContext());
                    team1Header.setText(getString(R.string.team1_header));
                    team1Header.setBackgroundColor(whiteColor);
                    team1Header.setLayoutParams(headerParams);
                    fragmentTableHeader.addView(team1Header);

                    TextView team2Header = new TextView(fragmentTableHeader.getContext());
                    team2Header.setText(getString(R.string.team2_header));
                    team2Header.setBackgroundColor(whiteColor);
                    team2Header.setLayoutParams(headerParams);
                    fragmentTableHeader.addView(team2Header);

                    TextView team3Header = new TextView(fragmentTableHeader.getContext());
                    team3Header.setText(getString(R.string.team3_header));
                    team3Header.setBackgroundColor(whiteColor);
                    team3Header.setLayoutParams(headerParams);
                    fragmentTableHeader.addView(team3Header);

                    TextView scoreHeader = new TextView(fragmentTableHeader.getContext());
                    scoreHeader.setText(getString(R.string.score_header));
                    scoreHeader.setBackgroundColor(whiteColor);
                    scoreHeader.setLayoutParams(headerParams);
                    fragmentTableHeader.addView(scoreHeader);

                    if (retrieveMatchesTask.getStatus() == AsyncTask.Status.RUNNING) {
                        retrieveMatchesTask.cancel(true);
                    }
                    retrieveMatchesTask.setProgressBar(fragmentProgress);
                    retrieveMatchesTask.execute(fragmentTable);
                    break;
                case 3:
                    TextView skillsRankHeader = new TextView(fragmentTableHeader.getContext());
                    skillsRankHeader.setText(getString(R.string.rank_header));
                    skillsRankHeader.setBackgroundColor(whiteColor);
                    skillsRankHeader.setLayoutParams(headerParams);
                    fragmentTableHeader.addView(skillsRankHeader);

                    TextView skillsTeamHeader = new TextView(fragmentTableHeader.getContext());
                    skillsTeamHeader.setText(getString(R.string.team_header));
                    skillsTeamHeader.setBackgroundColor(whiteColor);
                    skillsTeamHeader.setLayoutParams(headerParams);
                    fragmentTableHeader.addView(skillsTeamHeader);

                    TextView attemptsHeader = new TextView(fragmentTableHeader.getContext());
                    attemptsHeader.setText(getString(R.string.attempts));
                    attemptsHeader.setBackgroundColor(whiteColor);
                    attemptsHeader.setLayoutParams(headerParams);
                    fragmentTableHeader.addView(attemptsHeader);

                    TextView skillsScoreHeader = new TextView(fragmentTableHeader.getContext());
                    skillsScoreHeader.setText(getString(R.string.score_header));
                    skillsScoreHeader.setBackgroundColor(whiteColor);
                    skillsScoreHeader.setLayoutParams(headerParams);
                    fragmentTableHeader.addView(skillsScoreHeader);

                    if (retrieveSkillsTask.getStatus() == AsyncTask.Status.RUNNING) {
                        retrieveSkillsTask.cancel(true);
                    }
                    retrieveSkillsTask.setProgressBar(fragmentProgress);
                    retrieveSkillsTask.execute(fragmentTable);
                    break;
            }

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a EventFragment (defined as a static inner class below).
            EventFragment eventFragment = EventFragment.newInstance(position + 1);
            return eventFragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.rankings_tab);
                case 1:
                    return getString(R.string.matches_tab);
                case 2:
                    return getString(R.string.skills_tab);
            }
            return null;
        }
    }
}
