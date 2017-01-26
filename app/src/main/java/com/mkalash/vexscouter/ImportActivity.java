package com.mkalash.vexscouter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class ImportActivity extends AppCompatActivity {

    ArrayAdapter<String> importTeamAdapter;
    final Map<String, String> sharedTeamNotes = new HashMap<>();

    private class ParseFile extends AsyncTask<Uri, Void, String> {

        @Override
        protected String doInBackground(Uri... params) {
            Uri uri = params[0];
            StringBuilder sb = new StringBuilder();
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                if(is != null) {
                    Scanner s = new Scanner(is);
                    while (s.hasNext()) {
                        sb.append(s.next());
                        if (isCancelled()) {
                            break;
                        }
                    }
                    s.close();
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return sb.toString();
        }

        @Override
        protected void onPostExecute(String sharedText) {
            if(sharedText.isEmpty()) {
                finish();
            }
            sharedTeamNotes.clear();
            try {
                JSONObject sharedTeams = new JSONObject(sharedText);
                Iterator<String> teamIterator = sharedTeams.keys();
                while (teamIterator.hasNext()) {
                    String team = teamIterator.next();
                    sharedTeamNotes.put(team, sharedTeams.getString(team));
                }
                importTeamAdapter.clear();
                importTeamAdapter.addAll(sharedTeamNotes.keySet());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_import);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (action.equals(Intent.ACTION_SEND) && type != null && intent.getExtras().get(Intent.EXTRA_STREAM) != null) {
            new ParseFile().execute((Uri) intent.getExtras().get(Intent.EXTRA_STREAM));
        } else {
            finish();
        }

        importTeamAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_checked);
        final ListView importTeamListView = (ListView) findViewById(R.id.import_check_list);
        importTeamListView.setAdapter(importTeamAdapter);
        importTeamListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((CheckedTextView) view).setChecked(!((CheckedTextView) view).isChecked());
            }
        });

        Button importButton = (Button) findViewById(R.id.import_button);
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < importTeamAdapter.getCount(); i++) {
                    if (((CheckedTextView) importTeamListView.getChildAt(i)).isChecked()) {
                        String teamNumber = importTeamAdapter.getItem(i);
                        final SharedPreferences sharedPref = getSharedPreferences("TeamActivity", Context.MODE_PRIVATE);
                        SharedPreferences.Editor prefEditor = sharedPref.edit();
                        String teamNotes = sharedPref.getString("notes_team_" + teamNumber, "") + "\n" + sharedTeamNotes.get(teamNumber);
                        prefEditor.putString("notes_team_" + teamNumber, teamNotes);
                        prefEditor.apply();
                    }
                }
                AlertDialog alertDialog = new AlertDialog.Builder(ImportActivity.this).create();
                alertDialog.setTitle(R.string.import_label);
                alertDialog.setMessage(getString(R.string.import_complete));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                alertDialog.show();
            }
        });
    }
}
