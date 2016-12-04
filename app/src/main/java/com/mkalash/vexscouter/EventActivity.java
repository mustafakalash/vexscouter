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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class EventActivity extends AppCompatActivity {

    private String name;
    private String sku;
    
    private RetrieveMatches retrieveMatchesTask = new RetrieveMatches();
    private RetrieveRankings retrieveRankingsTask = new RetrieveRankings();

    class Rank {
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
            intent.putExtra("EVENT_SKU", sku);
            intent.putExtra("EVENT_NAME", name);
            startActivity(intent);
        }
    }

    class RetrieveMatches extends AsyncTask<TableLayout, Integer, ArrayList<Match>> {

        private ProgressBar progressBar;
        private TableLayout matchTable;
        ArrayList<Match> matches = new ArrayList<Match>();

        public void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        @Override
        protected ArrayList<Match> doInBackground(TableLayout... params) {
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

            LinearLayout.LayoutParams teamRowParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

            TeamClickListener teamClickListener = new TeamClickListener();

            if(matches.size() == 0) {
                TextView emptyPage = new TextView(matchTable.getContext());
                emptyPage.setText(R.string.no_results);
                matchTable.addView(emptyPage);
            }

            for(Match match : matches) {
                TableRow redRow = new TableRow(matchTable.getContext());
                redRow.setOrientation(LinearLayout.HORIZONTAL);
                redRow.setLayoutParams(teamRowParams);
                redRow.setBackgroundColor(redResult);
                matchTable.addView(redRow);

                TableRow blueRow = new TableRow(matchTable.getContext());
                blueRow.setOrientation(LinearLayout.HORIZONTAL);
                blueRow.setLayoutParams(teamRowParams);
                blueRow.setBackgroundColor(blueResult);
                matchTable.addView(blueRow);

                TableRow divider = new TableRow(matchTable.getContext());
                divider.setMinimumHeight(5);
                divider.setBackgroundColor(accentColor);
                matchTable.addView(divider);

                Button red1 = new Button(matchTable.getContext());
                red1.setOnClickListener(teamClickListener);
                Button red2 = new Button(matchTable.getContext());
                red2.setOnClickListener(teamClickListener);
                Button red3 = new Button(matchTable.getContext());
                red3.setOnClickListener(teamClickListener);
                Button blue1 = new Button(matchTable.getContext());
                blue1.setOnClickListener(teamClickListener);
                Button blue2 = new Button(matchTable.getContext());
                blue2.setOnClickListener(teamClickListener);
                Button blue3 = new Button(matchTable.getContext());
                blue3.setOnClickListener(teamClickListener);

                TextView redScore = new TextView(matchTable.getContext());
                TextView blueScore = new TextView(matchTable.getContext());

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
                if(match.getRed3().isEmpty()) {
                    redRow.addView(new TextView(matchTable.getContext()));
                } else {
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
                if(!match.getBlue3().isEmpty() && match.getBlue3().equals(match.getBlueSit())) {
                    blue3.setBackgroundColor(blueResultOut);
                } else {
                    blue3.setBackgroundColor(blueResult);
                }
                if(match.getBlue3().isEmpty()) {
                    blueRow.addView(new TextView(matchTable.getContext()));
                } else {
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
            }

            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }
    }

    class RetrieveRankings extends AsyncTask<TableLayout, Integer, ArrayList<Rank>> {

        private ProgressBar progressBar;
        private TableLayout rankingTable;
        ArrayList<Rank> rankings = new ArrayList<Rank>();

        public void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        @Override
        protected ArrayList<Rank> doInBackground(TableLayout... params) {
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
                JSONArray result = new JSONObject(json.toString()).getJSONArray("result");
                for(int i = 0; i < result.length(); i++) {
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
                    if(isCancelled()) {
                        break;
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

            int accentColor = ResourcesCompat.getColor(getResources(), R.color.colorAccent, null);

            LinearLayout.LayoutParams rankRowParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            rankRowParams.setMargins(0, 10, 0, 10);

            LinearLayout.LayoutParams vertDividerParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);

            TeamClickListener teamClickListener = new TeamClickListener();

            if(rankings.size() == 0) {
                TextView emptyPage = new TextView(rankingTable.getContext());
                emptyPage.setText(R.string.no_results);
                rankingTable.addView(emptyPage);
            }

            for(Rank rank : rankings) {
                TableRow rankRow = new TableRow(rankingTable.getContext());
                rankRow.setOrientation(LinearLayout.HORIZONTAL);
                rankRow.setLayoutParams(rankRowParams);

                TextView rankNum = new TextView(rankingTable.getContext());
                rankNum.setText(Integer.toString(rank.getRank()));
                rankRow.addView(rankNum);

                Button team = new Button(rankingTable.getContext());
                team.setOnClickListener(teamClickListener);
                team.setText(rank.getTeam());
                rankRow.addView(team);

                TextView wp = new TextView(rankingTable.getContext());
                wp.setText(Integer.toString(rank.getWP()));
                rankRow.addView(wp);

                TextView ap = new TextView(rankingTable.getContext());
                ap.setText(Integer.toString(rank.getAP()));
                rankRow.addView(ap);

                TextView trsp = new TextView(rankingTable.getContext());
                trsp.setText(Integer.toString(rank.getTRSP()));
                rankRow.addView(trsp);

                TextView ccwm = new TextView(rankingTable.getContext());
                ccwm.setText(Double.toString(rank.getCCWM()));
                rankRow.addView(ccwm);

                rankingTable.addView(rankRow);

                TableRow horizDivider = new TableRow(rankingTable.getContext());
                horizDivider.setMinimumHeight(2);
                horizDivider.setBackgroundColor(accentColor);
                rankingTable.addView(horizDivider);
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
            TableLayout fragmentTable = (TableLayout) rootView.findViewById(R.id.fragment_event_table);
            TableRow fragmentTableHeader = (TableRow) rootView.findViewById(R.id.fragment_event_table_header);

            RetrieveRankings retrieveRankingsTask = ((EventActivity) container.getContext()).retrieveRankingsTask;
            RetrieveMatches retrieveMatchesTask = ((EventActivity) container.getContext()).retrieveMatchesTask;

            switch(getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    TextView rankHeader = new TextView(fragmentTableHeader.getContext());
                    rankHeader.setText(getString(R.string.rank_header));
                    rankHeader.setPadding(5, 5, 5, 5);
                    fragmentTableHeader.addView(rankHeader);

                    TextView teamHeader = new TextView(fragmentTableHeader.getContext());
                    teamHeader.setText(getString(R.string.team_header));
                    teamHeader.setPadding(5, 5, 5, 5);
                    fragmentTableHeader.addView(teamHeader);

                    TextView wpHeader = new TextView(fragmentTableHeader.getContext());
                    wpHeader.setText(getString(R.string.wp_header));
                    wpHeader.setPadding(5, 5, 5, 5);
                    fragmentTableHeader.addView(wpHeader);

                    TextView apHeader = new TextView(fragmentTableHeader.getContext());
                    apHeader.setText(getString(R.string.ap_header));
                    apHeader.setPadding(5, 5, 5, 5);
                    fragmentTableHeader.addView(apHeader);

                    TextView trspHeader = new TextView(fragmentTableHeader.getContext());
                    trspHeader.setText(getString(R.string.trsp_header));
                    trspHeader.setPadding(5, 5, 5, 5);
                    fragmentTableHeader.addView(trspHeader);

                    TextView ccwmHeader = new TextView(fragmentTableHeader.getContext());
                    ccwmHeader.setText(getString(R.string.ccwm_header));
                    ccwmHeader.setPadding(5, 5, 5, 5);
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
                    fragmentTableHeader.addView(team1Header);

                    TextView team2Header = new TextView(fragmentTableHeader.getContext());
                    team2Header.setText(getString(R.string.team2_header));
                    fragmentTableHeader.addView(team2Header);

                    TextView team3Header = new TextView(fragmentTableHeader.getContext());
                    team3Header.setText(getString(R.string.team3_header));
                    fragmentTableHeader.addView(team3Header);

                    TextView scoreHeader = new TextView(fragmentTableHeader.getContext());
                    scoreHeader.setText(getString(R.string.score_header));
                    fragmentTableHeader.addView(scoreHeader);

                    if (retrieveMatchesTask.getStatus() == AsyncTask.Status.RUNNING) {
                        retrieveMatchesTask.cancel(true);
                    }
                    retrieveMatchesTask.setProgressBar(fragmentProgress);
                    retrieveMatchesTask.execute(fragmentTable);
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
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.rankings_tab);
                case 1:
                    return getString(R.string.matches_tab);
            }
            return null;
        }
    }
}
