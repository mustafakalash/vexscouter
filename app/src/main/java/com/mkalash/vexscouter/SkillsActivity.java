package com.mkalash.vexscouter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class SkillsActivity extends AppCompatActivity {
    private String eventName;
    private String sku;

    private Menu menu;

    private static class SkillsListAdapter extends BaseExpandableListAdapter {

        private LayoutInflater inflater;
        int highlightColor;
        Set<String> favoriteTeams;
        TeamClickListener teamClickListener;
        Map<SkillType, List<Skill>> items;
        int whiteColor;

        SkillsListAdapter(Context context, Map<SkillType, List<Skill>> items) {
            this.items = items;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            highlightColor = ResourcesCompat.getColor(context.getResources(), R.color.highlightColor, null);
            whiteColor = ResourcesCompat.getColor(context.getResources(), R.color.white, null);
            final SharedPreferences sharedPref = context.getSharedPreferences("com.mkalash.vexscouter.favorites", Context.MODE_PRIVATE);
            favoriteTeams = sharedPref.getStringSet("favorite_teams", new HashSet<String>());
            teamClickListener = ((SkillsActivity) context).new TeamClickListener();
        }

        @Override
        public int getGroupCount() {
            return SkillType.values().length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return items.get(getGroup(groupPosition)).size();
        }

        @Override
        public SkillType getGroup(int groupPosition) {
            return SkillType.values()[groupPosition];
        }

        @Override
        public Skill getChild(int groupPosition, int childPosition) {
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
            groupHeader.setText(SkillType.getTitle(getGroup(groupPosition)) + " (" + getChildrenCount(groupPosition) + ")");

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.template_event_skills, parent, false);
            }

            Skill skill = getChild(groupPosition, childPosition);

            if (skill != null) {
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

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
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
                    return "Autonomous";
            }
        }
    }

    static class Skill {
        final String team;
        int rank;
        final int attempts;
        final int score;

        Skill(String team, int rank, int attempts, int score) {
            this.team = team;
            this.rank = rank;
            this.attempts = attempts;
            this.score = score;
        }
    }

    private class TeamClickListener implements Button.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(SkillsActivity.this, TeamActivity.class);
            String teamNumber = ((Button) view).getText().toString();
            teamNumber = teamNumber.replaceAll("\\s\\(\\d*\\)", "");
            intent.putExtra("TEAM_NUM", teamNumber);
            intent.putExtra("EVENT_SKU", sku);
            intent.putExtra("EVENT_NAME", eventName);
            SkillsActivity.this.startActivity(intent);
        }
    }

    private class RetrieveSkills extends AsyncTask<ExpandableListView, Integer, Map<SkillType, List<Skill>>> {

        private ProgressBar progressBar;
        private ExpandableListView skillsList;
        final Map<SkillType, List<Skill>> skills = new HashMap();

        void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        @Override
        protected Map<SkillType, List<Skill>> doInBackground(ExpandableListView... params) {
            this.skillsList = params[0];
            List<Skill> robotSkills = new ArrayList();
            List<Skill> driverSkills = new ArrayList();
            List<Skill> autonSkills = new ArrayList();
            try {
                String urlString = "https://api.vexdb.io/v1/get_skills?sku=" + sku;
                JSONArray result = getFullResults(urlString);
                for(int i = 0; i < result.length(); i++) {
                    JSONObject skill = result.getJSONObject(i);
                    String team = skill.getString("team");
                    int rank = skill.getInt("rank");
                    int attempts = skill.getInt("attempts");
                    int score = skill.getInt("score");
                    SkillType type;
                    switch(skill.getInt("type")) {
                        case 0:
                            type = SkillType.DRIVER;
                            break;
                        default:
                        case 1:
                            type = SkillType.AUTON;
                            break;
                        case 2:
                            type = SkillType.ROBOT;
                            break;
                    }

                    Skill skillObj = new Skill(team, rank, attempts, score);
                    switch(type) {
                        default:
                        case ROBOT:
                            robotSkills.add(skillObj);
                            break;
                        case DRIVER:
                            driverSkills.add(skillObj);
                            break;
                        case AUTON:
                            autonSkills.add(skillObj);
                            break;
                    }
                    if(isCancelled()) {
                        break;
                    }
                }
                Collections.sort(driverSkills, new Comparator<Skill>() {
                    @Override
                    public int compare(final Skill a, final Skill b) {
                        return a.score < b.score ? +1 : a.score > b.score ? -1 : 0;
                    }
                });
                Collections.sort(autonSkills, new Comparator<Skill>() {
                    @Override
                    public int compare(final Skill a, final Skill b) {
                        return a.score < b.score ? +1 : a.score > b.score ? -1 : 0;
                    }
                });
                for(int i = 0; i < driverSkills.size(); i++) {
                    driverSkills.get(i).rank = i + 1;
                }
                for(int i = 0; i < autonSkills.size(); i++) {
                    autonSkills.get(i).rank = i + 1;
                }
                skills.put(SkillType.ROBOT, robotSkills);
                skills.put(SkillType.DRIVER, driverSkills);
                skills.put(SkillType.AUTON, autonSkills);
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
        protected void onPostExecute(Map<SkillType, List<Skill>> skills) {
            int whiteColor = ResourcesCompat.getColor(skillsList.getResources(), R.color.white, null);

            if(skills.size() == 0) {
                TextView emptyPage = new TextView(skillsList.getContext());
                emptyPage.setText(R.string.no_results);
                ((LinearLayout) skillsList.getParent()).addView(emptyPage);
                skillsList.setBackgroundColor(whiteColor);
            } else {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout container = (LinearLayout) skillsList.getParent();
                LinearLayout header = (LinearLayout) inflater.inflate(R.layout.template_event_headers,
                        container, false);
                LinearLayout skillsTableHeader = (LinearLayout) header.findViewById(R.id.template_skills_header);
                header.removeView(skillsTableHeader);
                container.addView(skillsTableHeader, 1);

                SkillsListAdapter skillsListAdapter = new SkillsListAdapter(skillsList.getContext(), skills);
                skillsList.setAdapter(skillsListAdapter);
            }

            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skills);

        Intent intent = getIntent();
        eventName = intent.getStringExtra("EVENT_NAME");
        sku = intent.getStringExtra("EVENT_SKU");
        setTitle(getString(R.string.skills_tab) + " @ " + eventName);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.skills_progress);
        ExpandableListView skillsList = (ExpandableListView) findViewById(R.id.skills_list);

        RetrieveSkills retrieveSkillsTask = new RetrieveSkills();
        if (retrieveSkillsTask.getStatus() == AsyncTask.Status.RUNNING) {
            retrieveSkillsTask.cancel(true);
        }
        retrieveSkillsTask.setProgressBar(progressBar);
        retrieveSkillsTask.execute(skillsList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pre_event_toolbar, menu);
        this.menu = menu;

        final SharedPreferences sharedPref = getSharedPreferences("com.mkalash.vexscouter.favorites", Context.MODE_PRIVATE);
        Set<String> favoriteEvents = sharedPref.getStringSet("favorite_events", new HashSet<String>());

        if(favoriteEvents.contains(eventName)) {
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
                if(favoriteEvents.contains(eventName)) {
                    favoriteEvents.remove(eventName);
                    menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_star_big_off));
                } else {
                    favoriteEvents.add(eventName);
                    menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_star_big_on));
                }
                prefEditor.putStringSet("favorite_events", favoriteEvents);
                prefEditor.apply();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
