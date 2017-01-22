package com.mkalash.vexscouter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import static com.mkalash.vexscouter.MainActivity.fetchJSON;
import static com.mkalash.vexscouter.MainActivity.getFullResults;

public class TeamActivity extends AppCompatActivity {

    private static String teamNumber;
    private static String sku;
    private static String eventName;
    private Menu menu;
    private static float vRating = 0;
    private static String teamName;
    private static String organization;
    private static int eventRank = 0;
    private static final String CURRENT_SEASON = "Starstruck";

    static class RetrieveRating extends AsyncTask<RatingBar, Integer, Void> {

        private ProgressBar progressBar;
        private RatingBar ratingBar;

        void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        @Override
        protected Void doInBackground(RatingBar... params) {
            this.ratingBar = params[0];

            try {
                String urlString = "https://api.vexdb.io/v1/get_season_rankings?season=" + CURRENT_SEASON + "&program=VRC&nodata=true";
                float size = (float) fetchJSON(urlString).getInt("size");

                urlString = "https://api.vexdb.io/v1/get_season_rankings?season=" + CURRENT_SEASON + "&team=" + teamNumber;
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

            ((TextView) ((View) ratingBar.getParent()).findViewById(R.id.team_name)).setText(teamName);
            ((TextView) ((View) ratingBar.getParent()).findViewById(R.id.team_school)).setText(organization);

            if (sku != null) {
                if (eventRank > 0) {
                    ((TextView) ((View) ratingBar.getParent()).findViewById(R.id.event_info_header)).setText(String.format(ratingBar.getContext().getString(R.string.event_info), eventRank, eventName));
                } else {
                    ((View) ratingBar.getParent()).findViewById(R.id.event_info_header).setVisibility(View.GONE);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class TeamFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
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

            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                default:
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_team_info, container, false);

                    ProgressBar progress = (ProgressBar) rootView.findViewById(R.id.team_progress);
                    RatingBar ratingBar = (RatingBar) rootView.findViewById(R.id.vrating);

                    RetrieveRating retrieveRatingTask = new RetrieveRating();
                    if (retrieveRatingTask.getStatus() == AsyncTask.Status.RUNNING) {
                        retrieveRatingTask.cancel(true);
                    }
                    retrieveRatingTask.setProgressBar(progress);
                    retrieveRatingTask.execute(ratingBar);

                    if(sku != null) {
                        TextView eventInfo = ((TextView) rootView.findViewById(R.id.event_info_header));
                        eventInfo.setVisibility(View.VISIBLE);
                        eventInfo.setText(String.format(getString(R.string.event_info), eventRank, eventName));
                    }

                    final EditText notesEditText = (EditText) rootView.findViewById(R.id.notes_edit_text);

                    final SharedPreferences sharedPref = rootView.getContext().getSharedPreferences("TeamActivity", Context.MODE_PRIVATE);
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

                    Button saveButton = (Button) rootView.findViewById(R.id.save_notes);
                    saveButton.setOnClickListener(new notesSaveListener());
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_event_expandable, container, false);

                    fragmentProgress = (ProgressBar) rootView.findViewById(R.id.fragment_event_progress);
                    ExpandableListView matchFragmentList = (ExpandableListView) rootView.findViewById(R.id.fragment_event_list);

                    EventActivity.RetrieveMatches retrieveMatchesTask = new EventActivity.RetrieveMatches();
                    if (retrieveMatchesTask.getStatus() == AsyncTask.Status.RUNNING) {
                        retrieveMatchesTask.cancel(true);
                    }
                    retrieveMatchesTask.setProgressBar(fragmentProgress);
                    retrieveMatchesTask.setTeam(teamNumber);
                    retrieveMatchesTask.execute(matchFragmentList);
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
