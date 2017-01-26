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
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mkalash.vexscouter.MainActivity.fetchJSON;
import static com.mkalash.vexscouter.MainActivity.getFullResults;

public class TeamActivity extends AppCompatActivity {

    private ShareActionProvider mShareActionProvider;
    private Menu menu;

    private String teamNumber;
    private String sku;
    private String eventName;
    private float vRating = 0;
    private String teamName;
    private String organization;
    private int eventRank = 0;

    private class TeamClickListener implements Button.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(TeamActivity.this, TeamActivity.class);
            String teamNumber = ((Button) view).getText().toString();
            teamNumber = teamNumber.replaceAll("\\s\\(\\d*\\)", "");
            intent.putExtra("TEAM_NUM", teamNumber);
            if(sku != null) {
                intent.putExtra("EVENT_SKU", sku);
                intent.putExtra("EVENT_NAME", eventName);
            }
            TeamActivity.this.startActivity(intent);
        }
    }

    private static class MatchListAdapter extends BaseExpandableListAdapter {

        private LayoutInflater inflater;
        int highlightColor;
        Set<String> favoriteTeams;
        TeamClickListener teamClickListener;
        Map<EventActivity.Round, List<EventActivity.Match>> items;
        int redResultOut;
        int blueResultOut;
        Map<String, Integer> teamRanks;
        int blueResult;
        int redResult;

        MatchListAdapter(Context context, Map<EventActivity.Round, List<EventActivity.Match>> items, Map<String, Integer> teamRanks) {
            this.items = items;
            this.teamRanks = teamRanks;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            highlightColor = ResourcesCompat.getColor(context.getResources(), R.color.highlightColor, null);
            redResult = ResourcesCompat.getColor(context.getResources(), R.color.redResult, null);
            blueResult = ResourcesCompat.getColor(context.getResources(), R.color.blueResult, null);
            final SharedPreferences sharedPref = context.getSharedPreferences("com.mkalash.vexscouter.favorites", Context.MODE_PRIVATE);
            favoriteTeams = sharedPref.getStringSet("favorite_teams", new HashSet<String>());
            teamClickListener = ((TeamActivity) context).new TeamClickListener();
            redResultOut = ResourcesCompat.getColor(context.getResources(), R.color.redResultOut, null);
            blueResultOut = ResourcesCompat.getColor(context.getResources(), R.color.blueResultOut, null);
        }

        @Override
        public int getGroupCount() {
            return EventActivity.Round.values().length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return items.get(getGroup(groupPosition)).size();
        }

        @Override
        public EventActivity.Round getGroup(int groupPosition) {
            return EventActivity.Round.values()[groupPosition];
        }

        @Override
        public EventActivity.Match getChild(int groupPosition, int childPosition) {
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
            groupHeader.setText(EventActivity.Round.getTitle(getGroup(groupPosition)) + " (" + getChildrenCount(groupPosition) + ")");

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = inflater.inflate(R.layout.template_event_matches, parent, false);
            }

            EventActivity.Match match = getChild(groupPosition, childPosition);

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
                } else {
                    redScore.setTypeface(null, Typeface.NORMAL);
                }

                blueScore.setText(Integer.toString(match.blueScore));
                if (match.blueScore > match.redScore) {
                    blueScore.setTypeface(null, Typeface.BOLD);
                } else {
                    blueScore.setTypeface(null, Typeface.NORMAL);
                }
            }

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

    private class RetrieveMatches extends AsyncTask<ExpandableListView, Integer, Map<EventActivity.Round, List<EventActivity.Match>>> {

        private ProgressBar progressBar;
        private ExpandableListView matchList;
        final Map<EventActivity.Round, List<EventActivity.Match>> matches = new HashMap<>();
        private Map<String, Integer> teamRanks = new HashMap();

        void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        @Override
        protected Map<EventActivity.Round, List<EventActivity.Match>> doInBackground(ExpandableListView... params) {
            this.matchList = params[0];
            try {
                String urlString = "https://api.vexdb.io/v1/get_rankings?sku=" + sku;
                JSONArray result = getFullResults(urlString);
                for(int i = 0; i < result.length(); i++) {
                    JSONObject rank = result.getJSONObject(i);
                    teamRanks.put(rank.getString("team"), rank.getInt("rank"));
                }
                urlString = "https://api.vexdb.io/v1/get_matches?sku=" + sku + "&team=" + teamNumber;
                result = getFullResults(urlString);
                final List<EventActivity.Match> practiceMatches = new ArrayList<>();
                final List<EventActivity.Match> qualificationMatches = new ArrayList<>();
                final List<EventActivity.Match> quarterfinalMatches = new ArrayList<>();
                final List<EventActivity.Match> semifinalMatches = new ArrayList<>();
                final List<EventActivity.Match> finalMatches = new ArrayList<>();
                for(int i = 0; i < result.length(); i++) {
                    JSONObject match = result.getJSONObject(i);
                    EventActivity.Round round = EventActivity.Round.values()[match.getInt("round") - 1];
                    String name = EventActivity.Round.getTitle(round);
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
                    EventActivity.Match matchObj = new EventActivity.Match(name, red1, red2, red3, redSit, blue1, blue2, blue3, blueSit, redScore, blueScore, scored, round);
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
                matches.put(EventActivity.Round.PRACTICE, practiceMatches);
                matches.put(EventActivity.Round.QUALIFICATION, qualificationMatches);
                matches.put(EventActivity.Round.QUARTERFINAL, quarterfinalMatches);
                matches.put(EventActivity.Round.SEMIFINAL, semifinalMatches);
                matches.put(EventActivity.Round.FINAL, finalMatches);
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
        protected void onPostExecute(Map<EventActivity.Round, List<EventActivity.Match>> matches) {
            int whiteColor = ResourcesCompat.getColor(matchList.getResources(), R.color.white, null);

            if(matches.size() == 0) {
                TextView emptyPage = new TextView(matchList.getContext());
                emptyPage.setText(R.string.no_results);
                ((LinearLayout) matchList.getParent()).addView(emptyPage);
                matchList.setBackgroundColor(whiteColor);
            } else {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                if (result.length() > 0 && size > 0) {
                    JSONObject team = result.getJSONObject(0);
                    float vRatingRank = (float) team.getInt("vrating_rank");
                    vRating = 10f - ((10f * vRatingRank) / size);
                }

                urlString = "https://api.vexdb.io/v1/get_teams?team=" + teamNumber;
                result = getFullResults(urlString);
                if (result.length() > 0) {
                    JSONObject team = result.getJSONObject(0);
                    teamName = team.getString("team_name");
                    organization = team.getString("organisation");
                }

                if (sku != null) {
                    urlString = "https://api.vexdb.io/v1/get_rankings?sku=" + sku + "&team=" + teamNumber;
                    result = getFullResults(urlString);
                    if (result.length() > 0) {
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

            if (sku != null) {
                if (eventRank > 0) {
                    ((TextView) findViewById(R.id.event_info_header)).setText(String.format(ratingBar.getContext().getString(R.string.event_info), eventRank, eventName));
                } else {
                    findViewById(R.id.event_info_header).setVisibility(View.GONE);
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
        if (eventName != null) {
            title += " @ " + eventName;
        }
        setTitle(title);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.team_toolbar, menu);
        this.menu = menu;

        final SharedPreferences sharedPref = getSharedPreferences("com.mkalash.vexscouter.favorites", Context.MODE_PRIVATE);
        Set<String> favoriteTeams = sharedPref.getStringSet("favorite_teams", new HashSet<String>());

        if (favoriteTeams.contains(teamNumber)) {
            menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_star_big_on));
        }

        MenuItem share = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(share);

        final SharedPreferences notesSharedPref = getSharedPreferences("TeamActivity", Context.MODE_PRIVATE);
        String loadedNotes = notesSharedPref.getString("notes_team_" + teamNumber, "");

        JSONObject notesList = new JSONObject();
        try {
            notesList.put(teamNumber, loadedNotes);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, teamNumber);
        sendIntent.putExtra(Intent.EXTRA_TEXT, notesList.toString());
        sendIntent.setType("text/plain");
        setShareIntent(sendIntent);

        return true;
    }

    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
                Set<String> favoriteTeamsPref = sharedPref.getStringSet("favorite_teams", new HashSet<String>());
                Set<String> favoriteTeams = new HashSet<>(favoriteTeamsPref);
                SharedPreferences.Editor prefEditor = sharedPref.edit();
                if (favoriteTeams.contains(teamNumber)) {
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

    public static class TeamFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public static TeamFragment newInstance(int sectionNumber) {
            TeamFragment fragment = new TeamFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView;
            ProgressBar fragmentProgress;
            final TeamActivity context;

            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                default:
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_team_info, container, false);
                    context = (TeamActivity) rootView.getContext();

                    ProgressBar progress = (ProgressBar) rootView.findViewById(R.id.team_progress);
                    RatingBar ratingBar = (RatingBar) rootView.findViewById(R.id.vrating);

                    RetrieveRating retrieveRatingTask = context.new RetrieveRating();
                    if (retrieveRatingTask.getStatus() == AsyncTask.Status.RUNNING) {
                        retrieveRatingTask.cancel(true);
                    }
                    retrieveRatingTask.setProgressBar(progress);
                    retrieveRatingTask.execute(ratingBar);

                    if(context.sku != null) {
                        TextView eventInfo = ((TextView) rootView.findViewById(R.id.event_info_header));
                        eventInfo.setVisibility(View.VISIBLE);
                        eventInfo.setText(String.format(getString(R.string.event_info), context.eventRank, context.eventName));
                    }

                    final EditText notesEditText = (EditText) rootView.findViewById(R.id.notes_edit_text);

                    final SharedPreferences sharedPref = rootView.getContext().getSharedPreferences("TeamActivity", Context.MODE_PRIVATE);
                    String loadedNotes = sharedPref.getString("notes_team_" + context.teamNumber, "");

                    notesEditText.setText(loadedNotes);

                    class notesSaveListener implements View.OnClickListener {
                        @Override
                        public void onClick(View v) {
                            SharedPreferences.Editor prefEditor = sharedPref.edit();
                            String teamNotes = notesEditText.getText().toString();
                            prefEditor.putString("notes_team_" + context.teamNumber, teamNotes);
                            prefEditor.apply();
                            JSONObject notesList = new JSONObject();
                            try {
                                notesList.put(context.teamNumber, teamNotes);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_SUBJECT, context.teamNumber);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, notesList.toString());
                            sendIntent.setType("text/plain");
                            context.setShareIntent(sendIntent);
                        }
                    }

                    Button saveButton = (Button) rootView.findViewById(R.id.save_notes);
                    saveButton.setOnClickListener(new notesSaveListener());
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_event_expandable, container, false);
                    context = (TeamActivity) rootView.getContext();

                    fragmentProgress = (ProgressBar) rootView.findViewById(R.id.fragment_event_progress);
                    ExpandableListView matchFragmentList = (ExpandableListView) rootView.findViewById(R.id.fragment_event_list);

                    RetrieveMatches retrieveMatchesTask = context.new RetrieveMatches();
                    if (retrieveMatchesTask.getStatus() == AsyncTask.Status.RUNNING) {
                        retrieveMatchesTask.cancel(true);
                    }
                    retrieveMatchesTask.setProgressBar(fragmentProgress);
                    retrieveMatchesTask.execute(matchFragmentList);
                    break;
            }

            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return TeamFragment.newInstance(1 + position);
        }

        @Override
        public int getCount() {
            return (sku != null) ? 2 : 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.info_tab);
                case 1:
                    return getString(R.string.matches_tab);
            }
            return null;
        }
    }
}
