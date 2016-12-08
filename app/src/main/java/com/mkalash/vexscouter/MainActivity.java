package com.mkalash.vexscouter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    static class RetrieveEvents extends AsyncTask<ArrayAdapter<String>, Integer, Map<String, String>> {

        private ProgressBar progressBar;
        private ArrayAdapter<String> eventListAdapter;
        private Map<String, String> events = new TreeMap<String, String>();

        public void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        public void setEventsMap(Map<String, String> events) {
            this.events = events;
        }

        @Override
        protected Map<String, String> doInBackground(ArrayAdapter<String>... params) {
            this.eventListAdapter = params[0];
            try {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(eventListAdapter.getContext());
                String season = sharedPref.getString("filter_season", "");
                season = (season.equals("Any")) ? "" : season;
                String country = sharedPref.getString("filter_country", "");
                country = (country.equals("Any")) ? "" : country;
                String region = sharedPref.getString("filter_region", "");
                region = (region.equals("Any")) ? "" : region;

                String urlString = ("https://api.vexdb.io/v1/get_events?season=" + season + "&country=" + country + "&region=" + region).replace(" ", "%20");
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
                    JSONObject event = result.getJSONObject(i);
                    String name = event.getString("name");
                    String sku = event.getString("sku");
                    events.put(name, sku);
                    publishProgress((int) (((i + 1) / (float) result.length()) * 100));
                    if(isCancelled()) {
                        break;
                    }
                }
                return events;
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
        protected void onPostExecute(Map<String, String> events) {
            eventListAdapter.clear();
            eventListAdapter.addAll(events.keySet());
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }
    }

    static class RetrieveTeams extends AsyncTask<ArrayAdapter<String>, Integer, List<String>> {

        private ProgressBar progressBar;
        private ArrayAdapter<String> teamListAdapter;

        public void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        @Override
        protected List<String> doInBackground(ArrayAdapter<String>... params) {
            this.teamListAdapter = params[0];
            try {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(teamListAdapter.getContext());
                String season = sharedPref.getString("filter_season", "");
                season = (season.equals("Any")) ? "" : season;
                String country = sharedPref.getString("filter_country", "");
                country = (country.equals("Any")) ? "" : country;
                String region = sharedPref.getString("filter_region", "");
                region = (region.equals("Any")) ? "" : region;

                String urlString = ("https://api.vexdb.io/v1/get_teams?program=VRC&season=" + season + "&country=" + country + "&region=" + region).replace(" ", "%20");
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
                List<String> teams = new ArrayList<String>();
                for(int i = 0; i < result.length(); i++) {
                    JSONObject team = result.getJSONObject(i);
                    teams.add(team.getString("number"));
                    publishProgress((int) (((i + 1) / (float) result.length()) * 100));
                    if(isCancelled()) {
                        break;
                    }
                }
                return teams;
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
        protected void onPostExecute(List<String> teams) {
            teamListAdapter.clear();
            teamListAdapter.addAll(teams);
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }
    }

    static class EventListClickListener implements AdapterView.OnItemClickListener {

        private Map<String, String> events;

        public EventListClickListener(Map<String, String> events) {
            this.events = events;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String name = parent.getItemAtPosition(position).toString();
            String sku = events.get(name);
            Intent intent = new Intent(view.getContext(), EventActivity.class);
            intent.putExtra("EVENT_NAME", name);
            intent.putExtra("EVENT_SKU", sku);
            view.getContext().startActivity(intent);
        }
    }

    static class TeamListClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String team = parent.getItemAtPosition(position).toString();
            Intent intent = new Intent(view.getContext(), TeamActivity.class);
            intent.putExtra("TEAM_NUM", team);
            view.getContext().startActivity(intent);
        }
    }

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.filters, false);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_filter:
                Intent intent = new Intent(this, FilterActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class MainFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public MainFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static MainFragment newInstance(int sectionNumber) {
            MainFragment fragment = new MainFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            Map<String, String> events = new TreeMap<String, String>();

            ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.fragment_progress);
            ListView fragmentList = (ListView) rootView.findViewById(R.id.fragment_list);
            ArrayAdapter<String> fragmentListAdapter = new ArrayAdapter<String>(
                    rootView.getContext(),
                    android.R.layout.simple_list_item_1);
            fragmentList.setAdapter(fragmentListAdapter);

            RetrieveEvents retrieveEventsTask = new RetrieveEvents();
            RetrieveTeams retrieveTeamsTask = new RetrieveTeams();

            switch(getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    fragmentList.setOnItemClickListener(new EventListClickListener(events));

                    if (retrieveEventsTask.getStatus() == AsyncTask.Status.RUNNING) {
                        retrieveEventsTask.cancel(true);
                    }
                    retrieveEventsTask.setProgressBar(progressBar);
                    retrieveEventsTask.setEventsMap(events);
                    retrieveEventsTask.execute(fragmentListAdapter);
                    break;
                case 2:
                    fragmentList.setOnItemClickListener(new TeamListClickListener());

                    if (retrieveTeamsTask.getStatus() == AsyncTask.Status.RUNNING) {
                        retrieveTeamsTask.cancel(true);
                    }
                    retrieveTeamsTask.setProgressBar(progressBar);
                    retrieveTeamsTask.execute(fragmentListAdapter);
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
            MainFragment mainFragment = MainFragment.newInstance(position + 1);
            return mainFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.events_tab);
                case 1:
                    return getString(R.string.teams_tab);
            }
            return null;
        }
    }
}
