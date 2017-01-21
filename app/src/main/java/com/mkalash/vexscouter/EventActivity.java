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
import android.support.annotation.NonNull;
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
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mkalash.vexscouter.MainActivity.getFullResults;

public class EventActivity extends AppCompatActivity {

    private static String name;
    private static String sku;

    private Menu menu;

    private static class RankingListAdapter extends ArrayAdapter<Rank>  {

        private LayoutInflater inflater;
        int highlightColor;
        Set<String> favoriteTeams;
        TeamClickListener teamClickListener;
        List<Rank> items;
        int whiteColor;

        RankingListAdapter(Context context, int resource, List<Rank> items) {
            super(context, resource, items);
            this.items = items;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            highlightColor = ResourcesCompat.getColor(context.getResources(), R.color.highlightColor, null);
            whiteColor = ResourcesCompat.getColor(context.getResources(), R.color.white, null);
            final SharedPreferences sharedPref = context.getSharedPreferences("com.mkalash.vexscouter.favorites", Context.MODE_PRIVATE);
            favoriteTeams = sharedPref.getStringSet("favorite_teams", new HashSet<String>());
            teamClickListener = new TeamClickListener();
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if(convertView == null) {
                convertView = inflater.inflate(R.layout.template_event_rankings, parent, false);
            }

            Rank rank = getItem(position);

            if(rank != null) {
                TextView rankNum = (TextView) convertView.findViewById(R.id.rank);
                rankNum.setText(Integer.toString(rank.rank));

                Button team = (Button) convertView.findViewById(R.id.team);
                team.setOnClickListener(teamClickListener);
                team.setText(rank.team);
                if (favoriteTeams.contains(rank.team)) {
                    team.setBackgroundColor(highlightColor);
                } else {
                    team.setBackgroundColor(whiteColor);
                }

                TextView wp = (TextView) convertView.findViewById(R.id.wp);
                wp.setText(Integer.toString(rank.wp));

                TextView ap = (TextView) convertView.findViewById(R.id.ap);
                ap.setText(Integer.toString(rank.ap));

                TextView sp = (TextView) convertView.findViewById(R.id.sp);
                sp.setText(Integer.toString(rank.sp));

                TextView trsp = (TextView) convertView.findViewById(R.id.trsp);
                trsp.setText(Integer.toString(rank.trsp));

                TextView ccwm = (TextView) convertView.findViewById(R.id.ccwm);
                ccwm.setText(String.format("%.1f", rank.ccwm));
            }

            return convertView;
        }
    }

    private static class MatchListAdapter extends BaseExpandableListAdapter {

        private LayoutInflater inflater;
        int highlightColor;
        Set<String> favoriteTeams;
        TeamClickListener teamClickListener;
        Map<Round, List<Match>> items;
        int redResultOut;
        int blueResultOut;
        Map<String, Integer> teamRanks;
        int blueResult;
        int redResult;

        MatchListAdapter(Context context, Map<Round, List<Match>> items, Map<String, Integer> teamRanks) {
            this.items = items;
            this.teamRanks = teamRanks;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            highlightColor = ResourcesCompat.getColor(context.getResources(), R.color.highlightColor, null);
            redResult = ResourcesCompat.getColor(context.getResources(), R.color.redResult, null);
            blueResult = ResourcesCompat.getColor(context.getResources(), R.color.blueResult, null);
            final SharedPreferences sharedPref = context.getSharedPreferences("com.mkalash.vexscouter.favorites", Context.MODE_PRIVATE);
            favoriteTeams = sharedPref.getStringSet("favorite_teams", new HashSet<String>());
            teamClickListener = new TeamClickListener();
            redResultOut = ResourcesCompat.getColor(context.getResources(), R.color.redResultOut, null);
            blueResultOut = ResourcesCompat.getColor(context.getResources(), R.color.blueResultOut, null);
        }

        @Override
        public int getGroupCount() {
            return Round.values().length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return items.get(getGroup(groupPosition)).size();
        }

        @Override
        public Round getGroup(int groupPosition) {
            return Round.values()[groupPosition];
        }

        @Override
        public Match getChild(int groupPosition, int childPosition) {
            return items.get(getGroup(groupPosition)).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = inflater.inflate(R.layout.list_group, parent, false);
            }
            TextView groupHeader = (TextView) convertView.findViewById(R.id.group_header);
            ImageView expandedIndicator = (ImageView) convertView.findViewById(R.id.expanded_indicator);
            if(isExpanded) {
                expandedIndicator.setImageResource(R.drawable.arrow_up_float);
            } else {
                expandedIndicator.setImageResource(R.drawable.arrow_down_float);
            }
            groupHeader.setText(Round.getTitle(getGroup(groupPosition)) + " (" + getChildrenCount(groupPosition) + ")");

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = inflater.inflate(R.layout.template_event_matches, parent, false);
            }

            Match match = getChild(groupPosition, childPosition);

            if(match != null) {
                TextView matchName = (TextView) convertView.findViewById(R.id.match_name);
                matchName.setText(match.name);

                Button red1 = (Button) convertView.findViewById(R.id.red1);
                red1.setOnClickListener(teamClickListener);

                Button red2 = (Button) convertView.findViewById(R.id.red2);
                red2.setOnClickListener(teamClickListener);

                Button red3 = (Button) convertView.findViewById(R.id.red3);
                if (!match.red3.isEmpty()) {
                    red3.setOnClickListener(teamClickListener);
                }

                Button blue1 = (Button) convertView.findViewById(R.id.blue1);
                blue1.setOnClickListener(teamClickListener);

                Button blue2 = (Button) convertView.findViewById(R.id.blue2);
                blue2.setOnClickListener(teamClickListener);

                Button blue3 = (Button) convertView.findViewById(R.id.blue3);
                if (!match.blue3.isEmpty()) {
                    blue3.setOnClickListener(teamClickListener);
                }

                TextView redScore = (TextView) convertView.findViewById(R.id.red_score);
                TextView blueScore = (TextView) convertView.findViewById(R.id.blue_score);

                if (teamRanks.keySet().contains(match.red1)) {
                    red1.setText(match.red1 + " (" + teamRanks.get(match.red1) + ")");
                } else {
                    red1.setText(match.red1);
                }
                if (match.red1.equals(match.redSit)) {
                    red1.setBackgroundColor(redResultOut);
                } else if (favoriteTeams.contains(match.red1)) {
                    red1.setBackgroundColor(highlightColor);
                } else {
                    red1.setBackgroundColor(redResult);
                }

                if (teamRanks.keySet().contains(match.red2)) {
                    red2.setText(match.red2 + " (" + teamRanks.get(match.red2) + ")");
                } else {
                    red2.setText(match.red2);
                }
                if (match.red2.equals(match.redSit)) {
                    red2.setBackgroundColor(redResultOut);
                } else if (favoriteTeams.contains(match.red2)) {
                    red2.setBackgroundColor(highlightColor);
                } else {
                    red2.setBackgroundColor(redResult);
                }

                if (teamRanks.keySet().contains(match.red3)) {
                    red3.setText(match.red3 + " (" + teamRanks.get(match.red3) + ")");
                } else {
                    red3.setText(match.red3);
                }
                if (!match.red3.isEmpty() && match.red3.equals(match.redSit)) {
                    red3.setBackgroundColor(redResultOut);
                } else if (favoriteTeams.contains(match.red3)) {
                    red3.setBackgroundColor(highlightColor);
                } else {
                    red3.setBackgroundColor(redResult);
                }

                if (teamRanks.keySet().contains(match.blue1)) {
                    blue1.setText(match.blue1 + " (" + teamRanks.get(match.blue1) + ")");
                } else {
                    blue1.setText(match.blue1);
                }
                if (match.blue1.equals(match.blueSit)) {
                    blue1.setBackgroundColor(blueResultOut);
                } else if (favoriteTeams.contains(match.blue1)) {
                    blue1.setBackgroundColor(highlightColor);
                } else {
                    blue1.setBackgroundColor(blueResult);
                }

                if (teamRanks.keySet().contains(match.blue2)) {
                    blue2.setText(match.blue2 + " (" + teamRanks.get(match.blue2) + ")");
                } else {
                    blue2.setText(match.blue2);
                }
                if (match.blue2.equals(match.blueSit)) {
                    blue2.setBackgroundColor(blueResultOut);
                } else if (favoriteTeams.contains(match.blue2)) {
                    blue2.setBackgroundColor(highlightColor);
                } else {
                    blue2.setBackgroundColor(blueResult);
                }

                if (teamRanks.keySet().contains(match.blue3)) {
                    blue3.setText(match.blue3 + " (" + teamRanks.get(match.blue3) + ")");
                } else {
                    blue3.setText(match.blue3);
                }
                if (!match.blue3.isEmpty() && match.blue3.equals(match.blueSit)) {
                    blue3.setBackgroundColor(blueResultOut);
                } else if (favoriteTeams.contains(match.blue3)) {
                    blue3.setBackgroundColor(highlightColor);
                } else {
                    blue3.setBackgroundColor(blueResult);
                }

                if (!match.scored) {
                    redScore.setBackgroundColor(redResultOut);
                    blueScore.setBackgroundColor(blueResultOut);
                } else {
                    redScore.setBackgroundColor(redResult);
                    blueScore.setBackgroundColor(blueResult);
                }

                redScore.setText(Integer.toString(match.redScore));
                if (match.redScore > match.blueScore) {
                    redScore.setTypeface(null, Typeface.BOLD);
                }

                blueScore.setText(Integer.toString(match.blueScore));
                if (match.blueScore > match.redScore) {
                    blueScore.setTypeface(null, Typeface.BOLD);
                }
            }

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

    private static class SkillsListAdapter extends ArrayAdapter<Skill> {

        private LayoutInflater inflater;
        int highlightColor;
        Set<String> favoriteTeams;
        TeamClickListener teamClickListener;
        List<Skill> items;
        int whiteColor;

        SkillsListAdapter(Context context, int resource, List<Skill> items) {
            super(context, resource, items);
            this.items = items;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            highlightColor = ResourcesCompat.getColor(context.getResources(), R.color.highlightColor, null);
            whiteColor = ResourcesCompat.getColor(context.getResources(), R.color.white, null);
            final SharedPreferences sharedPref = context.getSharedPreferences("com.mkalash.vexscouter.favorites", Context.MODE_PRIVATE);
            favoriteTeams = sharedPref.getStringSet("favorite_teams", new HashSet<String>());
            teamClickListener = new TeamClickListener();
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.template_event_skills, parent, false);
            }

            Skill skill = getItem(position);

            if (skill != null) {
                TextView type = (TextView) convertView.findViewById(R.id.type);
                type.setText(SkillType.getTitle(skill.type));

                TextView rank = (TextView) convertView.findViewById(R.id.rank);
                rank.setText(Integer.toString(skill.rank));

                Button team = (Button) convertView.findViewById(R.id.team);
                team.setText(skill.team);
                team.setOnClickListener(teamClickListener);
                if(favoriteTeams.contains(skill.team)) {
                    team.setBackgroundColor(highlightColor);
                } else {
                    team.setBackgroundColor(whiteColor);
                }

                TextView attempts = (TextView) convertView.findViewById(R.id.attempts);
                attempts.setText(Integer.toString(skill.attempts));

                TextView score = (TextView) convertView.findViewById(R.id.score);
                score.setText(Integer.toString(skill.score));
            }

            return convertView;
        }
    }

    enum SkillType {
        ROBOT, DRIVER, AUTON;

       static String getTitle(SkillType type) {
            switch(type) {
                default:
                case ROBOT:
                    return "Robot";
                case DRIVER:
                    return "Driver";
                case AUTON:
                    return "Auton.";
            }
        }
    }

    static class Skill {
        final String team;
        final int rank;
        final int attempts;
        final int score;
        final SkillType type;

        Skill(String team, int rank, int attempts, int score, SkillType type) {
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

    enum Round {
        PRACTICE, QUALIFICATION, QUARTERFINAL, SEMIFINAL, FINAL;

        static String getTitle(Round round) {
            switch(round) {
                case PRACTICE:
                    return "Practice";
                default:
                case QUALIFICATION:
                    return "Qualification";
                case QUARTERFINAL:
                    return "Quarterfinal";
                case SEMIFINAL:
                    return "Semifinal";
                case FINAL:
                    return "Final";
            }
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
        final Round round;

        Match(String name, String red1, String red2, String red3, String redSit, String blue1, String blue2, String blue3, String blueSit, int redScore, int blueScore, boolean scored, Round round) {
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
            this.round = round;
        }
    }

    private static class TeamClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), TeamActivity.class);
            String teamNumber = ((Button) view).getText().toString();
            teamNumber = teamNumber.replaceAll("\\s\\(\\d*\\)", "");
            intent.putExtra("TEAM_NUM", teamNumber);
            intent.putExtra("EVENT_SKU", sku);
            intent.putExtra("EVENT_NAME", name);
            view.getContext().startActivity(intent);
        }
    }

    static class RetrieveMatches extends AsyncTask<ExpandableListView, Integer, Map<Round, List<Match>>> {

        private ProgressBar progressBar;
        private ExpandableListView matchList;
        final Map<Round, List<Match>> matches = new HashMap<>();
        private Map<String, Integer> teamRanks = new HashMap();
        private String team;

        void setTeam(String team) {
            this.team = team;
        }

        void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        @Override
        protected Map<Round, List<Match>> doInBackground(ExpandableListView... params) {
            this.matchList = params[0];
            try {
                String urlString = "https://api.vexdb.io/v1/get_rankings?sku=" + sku;
                JSONArray result = getFullResults(urlString);
                for(int i = 0; i < result.length(); i++) {
                    JSONObject rank = result.getJSONObject(i);
                    teamRanks.put(rank.getString("team"), rank.getInt("rank"));
                }
                urlString = "https://api.vexdb.io/v1/get_matches?sku=" + sku;
                if(team != null) {
                    urlString += "&team=" + team;
                }
                result = getFullResults(urlString);
                final List<Match> practiceMatches = new ArrayList<>();
                final List<Match> qualificationMatches = new ArrayList<>();
                final List<Match> quarterfinalMatches = new ArrayList<>();
                final List<Match> semifinalMatches = new ArrayList<>();
                final List<Match> finalMatches = new ArrayList<>();
                for(int i = 0; i < result.length(); i++) {
                    JSONObject match = result.getJSONObject(i);
                    Round round = Round.values()[match.getInt("round") - 1];
                    String name = Round.getTitle(round);
                    switch(round) {
                        default:
                        case PRACTICE:
                        case QUALIFICATION:
                        case FINAL:
                            name += " #" + match.getInt("matchnum");
                            break;
                        case QUARTERFINAL:
                            name += " " + match.getInt("instance") + " #" + match.getInt("matchnum");
                            break;
                        case SEMIFINAL:
                            name += " " + match.getInt("instance") + " #" + match.getInt("matchnum");
                            break;
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
                    Match matchObj = new Match(name, red1, red2, red3, redSit, blue1, blue2, blue3, blueSit, redScore, blueScore, scored, round);
                    switch(round) {
                        case PRACTICE:
                            practiceMatches.add(matchObj);
                            break;
                        default:
                        case QUALIFICATION:
                            qualificationMatches.add(matchObj);
                            break;
                        case QUARTERFINAL:
                            quarterfinalMatches.add(matchObj);
                            break;
                        case SEMIFINAL:
                            semifinalMatches.add(matchObj);
                            break;
                        case FINAL:
                            finalMatches.add(matchObj);
                            break;
                    }
                    if(isCancelled()) {
                        break;
                    }
                }
                matches.put(Round.PRACTICE, practiceMatches);
                matches.put(Round.QUALIFICATION, qualificationMatches);
                matches.put(Round.QUARTERFINAL, quarterfinalMatches);
                matches.put(Round.SEMIFINAL, semifinalMatches);
                matches.put(Round.FINAL, finalMatches);
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
        protected void onPostExecute(Map<Round, List<Match>> matches) {
            int whiteColor = ResourcesCompat.getColor(matchList.getResources(), R.color.white, null);

            if(matches.size() == 0) {
                TextView emptyPage = new TextView(matchList.getContext());
                emptyPage.setText(R.string.no_results);
                ((LinearLayout) matchList.getParent()).addView(emptyPage);
                matchList.setBackgroundColor(whiteColor);
            } else {
                LayoutInflater inflater = (LayoutInflater) matchList.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout container = (LinearLayout) matchList.getParent();
                LinearLayout header = (LinearLayout) inflater.inflate(R.layout.template_event_headers,
                        container, false);
                LinearLayout matchesTableHeader = (LinearLayout) header.findViewById(R.id.template_matches_header);
                header.removeView(matchesTableHeader);
                container.addView(matchesTableHeader, 1);

                MatchListAdapter matchListAdapter = new MatchListAdapter(matchList.getContext(), matches, teamRanks);
                matchList.setAdapter(matchListAdapter);
            }

            if(progressBar != null) {
                progressBar.setVisibility(View.GONE);
                progressBar.setProgress(0);
            }
        }
    }

    static class RetrieveRankings extends AsyncTask<ListView, Integer, ArrayList<Rank>> {

        private ProgressBar progressBar;
        private ListView rankingList;
        final ArrayList<Rank> rankings = new ArrayList<>();

        void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        @Override
        protected ArrayList<Rank> doInBackground(ListView... params) {
            this.rankingList = params[0];
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

            int whiteColor = ResourcesCompat.getColor(rankingList.getResources(), R.color.white, null);

            if(rankings.size() == 0) {
                TextView emptyPage = new TextView(rankingList.getContext());
                emptyPage.setText(R.string.no_results);
                ((LinearLayout) rankingList.getParent()).addView(emptyPage);
                rankingList.setBackgroundColor(whiteColor);
            } else {
                LayoutInflater inflater = (LayoutInflater) rankingList.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                LinearLayout container = (LinearLayout) rankingList.getParent();
                LinearLayout header = (LinearLayout) inflater.inflate(R.layout.template_event_headers,
                        container, false);
                LinearLayout rankingsTableHeader = (LinearLayout) header.findViewById(R.id.template_rankings_header);
                header.removeView(rankingsTableHeader);
                container.addView(rankingsTableHeader, 1);

                RankingListAdapter rankingListAdapter = new RankingListAdapter(rankingList.getContext(), R.layout.template_event_rankings, rankings);
                rankingList.setAdapter(rankingListAdapter);
            }

            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }
    }

    static class RetrieveSkills extends AsyncTask<ListView, Integer, ArrayList<Skill>> {

        private ProgressBar progressBar;
        private ListView skillsList;
        final ArrayList<Skill> skills = new ArrayList<>();

        void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        @Override
        protected ArrayList<Skill> doInBackground(ListView... params) {
            this.skillsList = params[0];
            try {
                String urlString = "https://api.vexdb.io/v1/get_skills?sku=" + sku;
                JSONArray result = getFullResults(urlString);
                for(int i = 0; i < result.length(); i++) {
                    JSONObject skill = result.getJSONObject(i);
                    String team = skill.getString("team");
                    int rank = skill.getInt("rank");
                    int attempts = skill.getInt("attempts");
                    int score = skill.getInt("score");
                    SkillType type = SkillType.values()[skill.getInt("type")];

                    Skill skillObj = new Skill(team, rank, attempts, score, type);
                    skills.add(skillObj);
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
            int whiteColor = ResourcesCompat.getColor(skillsList.getResources(), R.color.white, null);

            if(skills.size() == 0) {
                TextView emptyPage = new TextView(skillsList.getContext());
                emptyPage.setText(R.string.no_results);
                ((LinearLayout) skillsList.getParent()).addView(emptyPage);
                skillsList.setBackgroundColor(whiteColor);
            } else {
                LayoutInflater inflater = (LayoutInflater) skillsList.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout container = (LinearLayout) skillsList.getParent();
                LinearLayout header = (LinearLayout) inflater.inflate(R.layout.template_event_headers,
                        container, false);
                LinearLayout skillsTableHeader = (LinearLayout) header.findViewById(R.id.template_skills_header);
                header.removeView(skillsTableHeader);
                container.addView(skillsTableHeader, 1);

                Collections.sort(skills, new Comparator<Skill>() {
                    @Override
                    public int compare(final Skill a, final Skill b) {
                        if(a.type.equals(b.type)) {
                            return 0;
                        } else if(a.type == SkillType.ROBOT || (a.type == SkillType.DRIVER && b.type == SkillType.AUTON)) {
                            return -1;
                        } else if(a.type == SkillType.AUTON || (a.type == SkillType.DRIVER && b.type == SkillType.ROBOT)) {
                            return +1;
                        }
                        return 0;
                    }
                });
                SkillsListAdapter skillsListAdapter = new SkillsListAdapter(skillsList.getContext(), R.layout.template_event_skills, skills);
                skillsList.setAdapter(skillsListAdapter);
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
            LinearLayout rootView;
            ProgressBar fragmentProgress;

            switch(getArguments().getInt(ARG_SECTION_NUMBER)) {
                default:
                case 1:
                    rootView = (LinearLayout) inflater.inflate(R.layout.fragment_event, container, false);

                    fragmentProgress = (ProgressBar) rootView.findViewById(R.id.fragment_event_progress);
                    ListView rankingFragmentList = (ListView) rootView.findViewById(R.id.fragment_event_list);

                    RetrieveRankings retrieveRankingsTask = new RetrieveRankings();
                    if (retrieveRankingsTask.getStatus() == AsyncTask.Status.RUNNING) {
                        retrieveRankingsTask.cancel(true);
                    }
                    retrieveRankingsTask.setProgressBar(fragmentProgress);
                    retrieveRankingsTask.execute(rankingFragmentList);
                    break;
                case 2:
                    rootView = (LinearLayout) inflater.inflate(R.layout.fragment_event_expandable, container, false);

                    fragmentProgress = (ProgressBar) rootView.findViewById(R.id.fragment_event_progress);
                    ExpandableListView matchFragmentList = (ExpandableListView) rootView.findViewById(R.id.fragment_event_list);

                    RetrieveMatches retrieveMatchesTask = new RetrieveMatches();
                    if (retrieveMatchesTask.getStatus() == AsyncTask.Status.RUNNING) {
                        retrieveMatchesTask.cancel(true);
                    }
                    retrieveMatchesTask.setProgressBar(fragmentProgress);
                    retrieveMatchesTask.execute(matchFragmentList);
                    break;
                case 3:
                    rootView = (LinearLayout) inflater.inflate(R.layout.fragment_event, container, false);

                    fragmentProgress = (ProgressBar) rootView.findViewById(R.id.fragment_event_progress);
                    ListView skillsFragmentList = (ListView) rootView.findViewById(R.id.fragment_event_list);

                    RetrieveSkills retrieveSkillsTask = new RetrieveSkills();
                    if (retrieveSkillsTask.getStatus() == AsyncTask.Status.RUNNING) {
                        retrieveSkillsTask.cancel(true);
                    }
                    retrieveSkillsTask.setProgressBar(fragmentProgress);
                    retrieveSkillsTask.execute(skillsFragmentList);
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
