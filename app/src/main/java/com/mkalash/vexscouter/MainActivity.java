package com.mkalash.vexscouter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    public static JSONObject fetchJSON(String urlString) {
        StringBuilder json = new StringBuilder();
        JSONObject jsonObject = new JSONObject();

        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return jsonObject;
        }

        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            while ((str = in.readLine()) != null) {
                json.append(str);
            }
        } catch(IOException e) {
            e.printStackTrace();
            return jsonObject;
        } finally {
            try {
                if(in != null) {
                    in.close();
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        try {
            jsonObject = new JSONObject(json.toString());
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public static JSONArray getFullResults(String urlString) {
        JSONArray result = new JSONArray();

        try {
            JSONObject jsonObject = fetchJSON(urlString + "&nodata=true");
            int fullDataSize = 0;
            if (jsonObject != null) {
                fullDataSize = jsonObject.getInt("size");
            }
            int dataSize = 0;
            result = new JSONArray();
            while(dataSize < fullDataSize) {
                jsonObject = fetchJSON(urlString + "&limit_start=" + dataSize);
                if (jsonObject != null) {
                    JSONArray resultFragment = jsonObject.getJSONArray("result");
                    for(int i = 0; i < resultFragment.length(); i++) {
                        result.put(resultFragment.get(i));
                    }
                    dataSize += jsonObject.getInt("size");
                }
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    class RetrieveEvents extends AsyncTask<ArrayAdapter<String>, Integer, Map<String, String>> {

        private ProgressBar progressBar;
        private ArrayAdapter<String> eventListAdapter;
        private Map<String, String> events = new TreeMap<>();

        void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        void setEventsMap(Map<String, String> events) {
            this.events = events;
        }

        @SafeVarargs
        @Override
        protected final Map<String, String> doInBackground(ArrayAdapter<String>... params) {
            this.eventListAdapter = params[0];
            events.clear();
            try {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(eventListAdapter.getContext());
                String season = sharedPref.getString("filter_season", "");
                season = (season.equals("Any")) ? "" : season;
                String country = sharedPref.getString("filter_country", "");
                country = (country.equals("Any")) ? "" : country;
                String region = sharedPref.getString("filter_region", "");
                region = (region.equals("Any")) ? "" : region;
                String program = sharedPref.getString("filter_program", "");
                program = (program.equals("Any")) ? "" : program;

                String urlString = ("https://api.vexdb.io/v1/get_events?program=" + program + "&season=" + season + "&country=" + country + "&region=" + region).replace(" ", "%20");
                JSONArray result = getFullResults(urlString);
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

    class RetrieveTeams extends AsyncTask<ArrayAdapter<String>, Integer, List<String>> {

        private ProgressBar progressBar;
        private ArrayAdapter<String> teamListAdapter;

        void setProgressBar(ProgressBar bar) {
            this.progressBar = bar;
        }

        @SafeVarargs
        @Override
        protected final List<String> doInBackground(ArrayAdapter<String>... params) {
            this.teamListAdapter = params[0];
            try {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(teamListAdapter.getContext());
                String season = sharedPref.getString("filter_season", "");
                season = (season.equals("Any")) ? "" : season;
                String country = sharedPref.getString("filter_country", "");
                country = (country.equals("Any")) ? "" : country;
                String region = sharedPref.getString("filter_region", "");
                region = (region.equals("Any")) ? "" : region;
                String program = sharedPref.getString("filter_program", "");
                program = (program.equals("Any")) ? "" : program;

                String urlString = ("https://api.vexdb.io/v1/get_teams?program=" + program + "&season=" + season + "&country=" + country + "&region=" + region).replace(" ", "%20");
                JSONArray result = getFullResults(urlString);
                List<String> teams = new ArrayList<>();
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
            final SharedPreferences sharedPref = getSharedPreferences("com.mkalash.vexscouter.favorites", Context.MODE_PRIVATE);
            final Set<String> favoriteTeams = sharedPref.getStringSet("favorite_teams", new HashSet<String>());

            for(String team : favoriteTeams) {
                if(!teams.contains(team)) {
                    teams.add(team);
                }
            }

            Collections.sort(teams, new Comparator<String>() {
                @Override
                public int compare(final String a, final String b) {
                    if(favoriteTeams.contains(a) && !favoriteTeams.contains(b)) {
                        return -1;
                    } else if(favoriteTeams.contains(b) && !favoriteTeams.contains(a)) {
                        return +1;
                    } else {
                        return a.compareTo(b);
                    }
                }
            });

            teamListAdapter.clear();
            teamListAdapter.addAll(teams);
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }
    }

    class EventListClickListener implements AdapterView.OnItemClickListener {

        private final Map<String, String> events;

        EventListClickListener(Map<String, String> events) {
            this.events = events;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String name = parent.getItemAtPosition(position).toString();
            String sku = events.get(name);
            Intent intent = new Intent(view.getContext(), DivisionActivity.class);
            intent.putExtra("EVENT_NAME", name);
            intent.putExtra("EVENT_SKU", sku);
            view.getContext().startActivity(intent);
        }
    }

    private class TeamListClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String team = parent.getItemAtPosition(position).toString();
            Intent intent = new Intent(view.getContext(), TeamActivity.class);
            intent.putExtra("TEAM_NUM", team);
            view.getContext().startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.filters, false);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
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
            case R.id.action_refresh:
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recreate();
                    }
                }, 1);
                return true;
            case R.id.action_filter:
                Intent intent = new Intent(this, FilterActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class MainFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";
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
            MainActivity context = (MainActivity) rootView.getContext();

            final SharedPreferences favSharedPref = context.getSharedPreferences("com.mkalash.vexscouter.favorites", Context.MODE_PRIVATE);
            final Set<String> favoriteEvents = favSharedPref.getStringSet("favorite_events", new HashSet<String>());
            Map<String, String> events = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String a, String b) {
                    if(favoriteEvents.contains(a) && !favoriteEvents.contains(b)) {
                        return -1;
                    } else if(favoriteEvents.contains(b) && !favoriteEvents.contains(a)) {
                        return +1;
                    } else {
                        return a.compareTo(b);
                    }
                }
            });

            ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.fragment_progress);
            ListView fragmentList = (ListView) rootView.findViewById(R.id.fragment_list);
            final ArrayAdapter<String> fragmentListAdapter = new ArrayAdapter<>(
                    rootView.getContext(),
                    android.R.layout.simple_list_item_1);
            fragmentList.setAdapter(fragmentListAdapter);

            RetrieveEvents retrieveEventsTask = context.new RetrieveEvents();
            RetrieveTeams retrieveTeamsTask = context.new RetrieveTeams();

            EditText searchBar = (EditText) rootView.findViewById(R.id.fragment_search);
            searchBar.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                    if(cs.length() > 0) {
                        fragmentListAdapter.getFilter().filter(cs);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                              int arg3) {
                }

                @Override
                public void afterTextChanged(Editable arg0) {
                }
            });

            switch(getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    fragmentList.setOnItemClickListener(context.new EventListClickListener(events));

                    if (retrieveEventsTask.getStatus() == AsyncTask.Status.RUNNING) {
                        retrieveEventsTask.cancel(true);
                    }
                    retrieveEventsTask.setProgressBar(progressBar);
                    retrieveEventsTask.setEventsMap(events);
                    retrieveEventsTask.execute(fragmentListAdapter);
                    break;
                case 2:
                    fragmentList.setOnItemClickListener(context.new TeamListClickListener());

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
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return MainFragment.newInstance(position + 1);
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
