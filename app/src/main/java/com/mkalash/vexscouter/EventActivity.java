package com.mkalash.vexscouter;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import java.util.HashSet;
import java.util.Set;

import static com.mkalash.vexscouter.MainActivity.getFullResults;

public class EventActivity extends AppCompatActivity {

    private static String name;
    private static String sku;

    private Menu menu;

    static class Skill {
        final String team;
        final int rank;
        final int attempts;
        final int score;
        final int type;

        Skill(String team, int rank, int attempts, int score, int type) {
            this.team = team;
            this.rank = rank;
            this.attempts = attempts;
            this.score = score;
            this.type = type;
        }
    }

    static class Rank {
        final String team;
        final int rank;
        final int wp;
        final int ap;
        final int sp;
        final int trsp;
        final double ccwm;

        Rank(String team, int rank, int wp, int ap, int sp, int trsp, double ccwm) {
            this.team = team;
            this.rank = rank;
            this.wp = wp;
            this.ap = ap;
            this.sp = sp;
            this.trsp = trsp;
            this.ccwm = ccwm;
        }
    }

    static class Match {
        final String name;
        final String red1;
        final String red2;
        final String red3;
        final String redSit;
        final String blue1;
        final String blue2;
        final String blue3;
        final String blueSit;
        final int redScore;
        final int blueScore;
        final boolean scored;

        Match(String name, String red1, String red2, String red3, String redSit, String blue1, String blue2, String blue3, String blueSit, int redScore, int blueScore, boolean scored) {
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
    }

    private static class TeamClickListener implements Button.OnClickListener {

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
        final ArrayList<Match> matches = new ArrayList<>();

        void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        @Override
        protected ArrayList<Match> doInBackground(LinearLayout... params) {
            this.matchTable = params[0];
            try {
                String urlString = "https://api.vexdb.io/v1/get_matches?sku=" + sku;
                JSONArray result = getFullResults(urlString);
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
            int redResultOut = ResourcesCompat.getColor(matchTable.getResources(), R.color.redResultOut, null);
            int blueResultOut = ResourcesCompat.getColor(matchTable.getResources(), R.color.blueResultOut, null);
            int whiteColor = ResourcesCompat.getColor(matchTable.getResources(), R.color.white, null);
            int highlightColor = ResourcesCompat.getColor(matchTable.getResources(), R.color.highlightColor, null);

            TeamClickListener teamClickListener = new TeamClickListener();

            if(matches.size() == 0) {
                TextView emptyPage = new TextView(matchTable.getContext());
                emptyPage.setText(R.string.no_results);
                matchTable.addView(emptyPage);
                matchTable.setBackgroundColor(whiteColor);
            }

            final SharedPreferences sharedPref = matchTable.getContext().getSharedPreferences("com.mkalash.vexscouter.favorites", Context.MODE_PRIVATE);
            Set<String> favoriteTeams = sharedPref.getStringSet("favorite_teams", new HashSet<String>());

            LayoutInflater inflater = (LayoutInflater) matchTable.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            for(Match match : matches) {
                LinearLayout matchRow = (LinearLayout) inflater.inflate(R.layout.template_event_matches,
                        matchTable, false);
                matchTable.addView(matchRow);

                TextView matchName = (TextView) matchRow.findViewById(R.id.match_name);
                matchName.setText(match.name);

                Button red1 = (Button) matchRow.findViewById(R.id.red1);
                red1.setOnClickListener(teamClickListener);

                Button red2 = (Button) matchRow.findViewById(R.id.red2);
                red2.setOnClickListener(teamClickListener);

                Button red3 = (Button) matchRow.findViewById(R.id.red3);
                if(!match.red3.isEmpty()) {
                    red3.setOnClickListener(teamClickListener);
                }

                Button blue1 = (Button) matchRow.findViewById(R.id.blue1);
                blue1.setOnClickListener(teamClickListener);

                Button blue2 = (Button) matchRow.findViewById(R.id.blue2);
                blue2.setOnClickListener(teamClickListener);

                Button blue3 = (Button) matchRow.findViewById(R.id.blue3);
                if(!match.blue3.isEmpty()) {
                    blue3.setOnClickListener(teamClickListener);
                }

                TextView redScore = (TextView) matchRow.findViewById(R.id.red_score);
                TextView blueScore = (TextView) matchRow.findViewById(R.id.blue_score);

                red1.setText(match.red1);
                if(match.red1.equals(match.redSit)) {
                    red1.setBackgroundColor(redResultOut);
                } else if(favoriteTeams.contains(match.red1)) {
                    red1.setBackgroundColor(highlightColor);
                }

                red2.setText(match.red2);
                if(match.red2.equals(match.redSit)) {
                    red2.setBackgroundColor(redResultOut);
                } else if(favoriteTeams.contains(match.red2)) {
                    red2.setBackgroundColor(highlightColor);
                }

                red3.setText(match.red3);
                if(!match.red3.isEmpty() && match.red3.equals(match.redSit)) {
                    red3.setBackgroundColor(redResultOut);
                } else if(favoriteTeams.contains(match.red3)) {
                    red3.setBackgroundColor(highlightColor);
                }

                blue1.setText(match.blue1);
                if(match.blue1.equals(match.blueSit)) {
                    blue1.setBackgroundColor(blueResultOut);
                } else if(favoriteTeams.contains(match.blue1)) {
                    blue1.setBackgroundColor(highlightColor);
                }

                blue2.setText(match.blue2);
                if(match.blue2.equals(match.blueSit)) {
                    blue2.setBackgroundColor(blueResultOut);
                } else if(favoriteTeams.contains(match.blue2)) {
                    blue2.setBackgroundColor(highlightColor);
                }

                blue3.setText(match.blue3);
                if(!match.blue3.isEmpty() && match.blue3.equals(match.blueSit)) {
                    blue3.setBackgroundColor(blueResultOut);
                } else if(favoriteTeams.contains(match.blue3)) {
                    blue3.setBackgroundColor(highlightColor);
                }

                if(!match.scored) {
                    redScore.setBackgroundColor(redResultOut);
                    blueScore.setBackgroundColor(blueResultOut);
                }

                redScore.setText(Integer.toString(match.redScore));
                if(match.redScore > match.blueScore) {
                    redScore.setTypeface(null, Typeface.BOLD);
                }

                blueScore.setText(Integer.toString(match.blueScore));
                if(match.blueScore > match.redScore) {
                    blueScore.setTypeface(null, Typeface.BOLD);
                }
            }

            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }
    }

    static class RetrieveRankings extends AsyncTask<LinearLayout, Integer, ArrayList<Rank>> {

        private ProgressBar progressBar;
        private LinearLayout rankingTable;
        final ArrayList<Rank> rankings = new ArrayList<>();

        void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        @Override
        protected ArrayList<Rank> doInBackground(LinearLayout... params) {
            this.rankingTable = params[0];
            try {
                String urlString = "https://api.vexdb.io/v1/get_rankings?sku=" + sku;
                JSONArray result = getFullResults(urlString);
                if(result.length() == 0) {
                    urlString = "https://api.vexdb.io/v1/get_teams?sku=" + sku;
                    result = getFullResults(urlString);
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject rank = result.getJSONObject(i);
                        String team = rank.getString("number");
                        Rank rankObj = new Rank(team, i + 1, 0, 0, 0, 0, 0);
                        rankings.add(rankObj);
                        publishProgress((int) (((i + 1) / (float) result.length()) * 100));
                        if (isCancelled()) {
                            break;
                        }
                    }
                } else {
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject rank = result.getJSONObject(i);
                        String team = rank.getString("team");
                        int rankNum = rank.getInt("rank");
                        int wp = rank.getInt("wp");
                        int ap = rank.getInt("ap");
                        int sp = rank.getInt("sp");
                        int trsp = rank.getInt("trsp");
                        double ccwm = rank.getDouble("ccwm");
                        Rank rankObj = new Rank(team, rankNum, wp, ap, sp, trsp, ccwm);
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

            int whiteColor = ResourcesCompat.getColor(rankingTable.getResources(), R.color.white, null);
            int highlightColor = ResourcesCompat.getColor(rankingTable.getResources(), R.color.highlightColor, null);

            TeamClickListener teamClickListener = new TeamClickListener();

            if(rankings.size() == 0) {
                TextView emptyPage = new TextView(rankingTable.getContext());
                emptyPage.setText(R.string.no_results);
                rankingTable.addView(emptyPage);
                rankingTable.setBackgroundColor(whiteColor);
            }

            final SharedPreferences sharedPref = rankingTable.getContext().getSharedPreferences("com.mkalash.vexscouter.favorites", Context.MODE_PRIVATE);
            Set<String> favoriteTeams = sharedPref.getStringSet("favorite_teams", new HashSet<String>());

            LayoutInflater inflater = (LayoutInflater) rankingTable.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            for(Rank rank : rankings) {
                LinearLayout rankRow = (LinearLayout) inflater.inflate(R.layout.template_event_rankings,
                        rankingTable, false);
                rankingTable.addView(rankRow);

                TextView rankNum = (TextView) rankRow.findViewById(R.id.rank);
                rankNum.setText(Integer.toString(rank.rank));

                Button team = (Button) rankRow.findViewById(R.id.team);
                team.setOnClickListener(teamClickListener);
                team.setText(rank.team);
                if(favoriteTeams.contains(rank.team)) {
                    team.setBackgroundColor(highlightColor);
                }

                TextView wp = (TextView) rankRow.findViewById(R.id.wp);
                wp.setText(Integer.toString(rank.wp));

                TextView ap = (TextView) rankRow.findViewById(R.id.ap);
                ap.setText(Integer.toString(rank.ap));

                TextView sp = (TextView) rankRow.findViewById(R.id.sp);
                sp.setText(Integer.toString(rank.sp));

                TextView trsp = (TextView) rankRow.findViewById(R.id.trsp);
                trsp.setText(Integer.toString(rank.trsp));

                TextView ccwm = (TextView) rankRow.findViewById(R.id.ccwm);
                ccwm.setText(String.format("%.1f", rank.ccwm));
            }

            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }
    }

    static class RetrieveSkills extends AsyncTask<LinearLayout, Integer, ArrayList<Skill>> {

        private ProgressBar progressBar;
        private LinearLayout skillsTable;
        final ArrayList<Skill> skills = new ArrayList<>();

        void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        @Override
        protected ArrayList<Skill> doInBackground(LinearLayout... params) {
            this.skillsTable = params[0];
            try {
                String urlString = "https://api.vexdb.io/v1/get_skills?sku=" + sku;
                JSONArray result = getFullResults(urlString);
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

            int whiteColor = ResourcesCompat.getColor(skillsTable.getResources(), R.color.white, null);
            int accentColor = ResourcesCompat.getColor(skillsTable.getResources(), R.color.colorAccent, null);
            int highlightColor = ResourcesCompat.getColor(skillsTable.getResources(), R.color.highlightColor, null);

            final SharedPreferences sharedPref = skillsTable.getContext().getSharedPreferences("com.mkalash.vexscouter.favorites", Context.MODE_PRIVATE);
            Set<String> favoriteTeams = sharedPref.getStringSet("favorite_teams", new HashSet<String>());

            float density = skillsTable.getResources().getDisplayMetrics().density;

            LinearLayout.LayoutParams skillTypeParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            skillTypeParams.setMargins((int) (1 * density), 0, (int) (1 * density), (int) (2 * density));

            TeamClickListener teamClickListener = new TeamClickListener();

            LayoutInflater inflater = (LayoutInflater) skillsTable.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if(skills.size() == 0) {
                TextView emptyPage = new TextView(skillsTable.getContext());
                emptyPage.setText(R.string.no_results);
                skillsTable.addView(emptyPage);
                skillsTable.setBackgroundColor(whiteColor);
            } else {
                int[] tableOrder = {2, 0, 1};
                for (int i : tableOrder) {
                    TextView skillType = new TextView(skillsTable.getContext());
                    skillType.setBackgroundColor(accentColor);
                    skillType.setTextColor(whiteColor);
                    skillType.setLayoutParams(skillTypeParams);
                    switch (i) {
                        default:
                        case 2:
                            skillType.setText(skillsTable.getContext().getString(R.string.robot_skills));
                            break;
                        case 1:
                            skillType.setText(skillsTable.getContext().getString(R.string.auton_skills));
                            break;
                        case 0:
                            skillType.setText(skillsTable.getContext().getString(R.string.driver_skills));
                            break;
                    }
                    skillsTable.addView(skillType);

                    for (Skill skill : skills) {
                        if (skill.type != i) {
                            continue;
                        }

                        LinearLayout skillRow = (LinearLayout) inflater.inflate(R.layout.template_event_skills, skillsTable, false);
                        skillsTable.addView(skillRow);

                        TextView rank = (TextView) skillRow.findViewById(R.id.rank);
                        rank.setText(Integer.toString(skill.rank));

                        Button team = (Button) skillRow.findViewById(R.id.team);
                        team.setText(skill.team);
                        team.setOnClickListener(teamClickListener);
                        if(favoriteTeams.contains(skill.team)) {
                            team.setBackgroundColor(highlightColor);
                        }

                        TextView attempts = (TextView) skillRow.findViewById(R.id.attempts);
                        attempts.setText(Integer.toString(skill.attempts));

                        TextView score = (TextView) skillRow.findViewById(R.id.score);
                        score.setText(Integer.toString(skill.score));

                    }
                }
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
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        name = intent.getStringExtra("EVENT_NAME");
        sku = intent.getStringExtra("EVENT_SKU");
        setTitle(name);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        /*
      The {@link android.support.v4.view.PagerAdapter} that will provide
      fragments for each of the sections. We use a
      {@link FragmentPagerAdapter} derivative, which will keep every
      loaded fragment in memory. If this becomes too memory intensive, it
      may be best to switch to a
      {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        /*
      The {@link ViewPager} that will host the section contents.
     */
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class EventFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

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
            LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.fragment_event, container, false);

            ProgressBar fragmentProgress = (ProgressBar) rootView.findViewById(R.id.fragment_event_progress);
            LinearLayout fragmentTable = (LinearLayout) rootView.findViewById(R.id.fragment_event_table);

            RetrieveRankings retrieveRankingsTask = new RetrieveRankings();
            RetrieveMatches retrieveMatchesTask = new RetrieveMatches();
            RetrieveSkills retrieveSkillsTask = new RetrieveSkills();

            LinearLayout template = (LinearLayout) inflater.inflate(R.layout.template_event_headers,
                    rootView, false);

            switch(getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    LinearLayout rankingsTableHeader = (LinearLayout) template.findViewById(R.id.template_rankings_header);
                    template.removeView(rankingsTableHeader);
                    rootView.addView(rankingsTableHeader, 0);

                    if (retrieveRankingsTask.getStatus() == AsyncTask.Status.RUNNING) {
                        retrieveRankingsTask.cancel(true);
                    }
                    retrieveRankingsTask.setProgressBar(fragmentProgress);
                    retrieveRankingsTask.execute(fragmentTable);
                    break;
                case 2:
                    LinearLayout matchesTableHeader = (LinearLayout) template.findViewById(R.id.template_matches_header);
                    template.removeView(matchesTableHeader);
                    rootView.addView(matchesTableHeader, 0);

                    if (retrieveMatchesTask.getStatus() == AsyncTask.Status.RUNNING) {
                        retrieveMatchesTask.cancel(true);
                    }
                    retrieveMatchesTask.setProgressBar(fragmentProgress);
                    retrieveMatchesTask.execute(fragmentTable);
                    break;
                case 3:
                    LinearLayout skillsTableHeader = (LinearLayout) template.findViewById(R.id.template_skills_header);
                    template.removeView(skillsTableHeader);
                    rootView.addView(skillsTableHeader, 0);

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

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a EventFragment (defined as a static inner class below).
            return EventFragment.newInstance(1 + position);
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
