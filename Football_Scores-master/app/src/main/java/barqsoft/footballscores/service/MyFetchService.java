package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.provider.DatabaseContract;
import barqsoft.footballscores.R;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class MyFetchService extends IntentService {
    public static final String LOG_TAG = "MyFetchService";

    public MyFetchService() {
        super("MyFetchService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        getData("n2");
        getData("p2");

        return;
    }

    private void getData(String timeFrame) {
        //Creating fetch URL
        final String BASE_URL = "http://api.football-data.org/alpha/fixtures"; //Base URL
        final String QUERY_TIME_FRAME = "timeFrame"; //Time Frame parameter to determine days
        //final String QUERY_MATCH_DAY = "matchday";

        Uri fetch_build = Uri.parse(BASE_URL).buildUpon().
                appendQueryParameter(QUERY_TIME_FRAME, timeFrame).build();
        //Log.v(LOG_TAG, "The url we are looking at is: "+fetch_build.toString()); //log spam
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String jsonData = null;
        //Opening Connection
        try {
            URL fetch = new URL(fetch_build.toString());
            urlConnection = (HttpURLConnection) fetch.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.addRequestProperty("X-Auth-Token", getString(R.string.api_key));
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            jsonData = buffer.toString();
        } catch (Exception e) {
            Log.e(LOG_TAG, getString(R.string.error_fetching_data));
            Log.e(LOG_TAG, "Exception here" + e.getMessage());
            Toast.makeText(this, getString(R.string.error_fetching_data), Toast.LENGTH_SHORT).show();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, getString(R.string.error_reading_data));
                    Toast.makeText(this, getString(R.string.error_reading_data), Toast.LENGTH_SHORT).show();
                    Log.e(LOG_TAG, "Error Closing Stream");
                }
            }
        }
        try {
            if (jsonData != null) {
                //This bit is to check if the data contains any matches. If not, we call processJson on the dummy data
                JSONArray matches = new JSONObject(jsonData).getJSONArray("fixtures");
                if (matches.length() == 0) {
                    //if there is no data, call the function on dummy data
                    //this is expected behavior during the off season.
                    processJSONdata(getString(R.string.dummy_data), getApplicationContext(), false);
                    return;
                }

                processJSONdata(jsonData, getApplicationContext(), true);
            } else {
                //Could not Connect
                Log.e(LOG_TAG, getString(R.string.error_server_connect));
                Toast.makeText(this, getString(R.string.error_server_connect), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            Log.e(LOG_TAG, getString(R.string.error_fetching_data));
            Toast.makeText(this, getString(R.string.error_fetching_data), Toast.LENGTH_SHORT).show();
        }
    }

    private void processJSONdata(String JSONdata, Context mContext, boolean isReal) {
        //JSON data
        // This set of league codes is for the 2015/2016 season. In fall of 2016, they will need to
        // be updated. Feel free to use the codes
        final String BUNDESLIGA1 = "394";
        final String BUNDESLIGA2 = "395";
        final String LIGUE1 = "396";
        final String LIGUE2 = "397";
        final String PREMIER_LEAGUE = "398";
        final String PRIMERA_DIVISION = "399";
        final String SEGUNDA_DIVISION = "400";
        final String SERIE_A = "401";
        final String PRIMERA_LIGA = "402";
        final String Bundesliga3 = "403";
        final String EREDIVISIE = "404";


        final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
        final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
        final String FIXTURES = "fixtures";
        final String LINKS = "_links";
        final String SOCCER_SEASON = "soccerseason";
        final String SELF = "self";
        final String MATCH_DATE = "date";
        final String HOME_TEAM = "homeTeamName";
        final String AWAY_TEAM = "awayTeamName";
        final String RESULT = "result";
        final String HOME_GOALS = "goalsHomeTeam";
        final String AWAY_GOALS = "goalsAwayTeam";
        final String MATCH_DAY = "matchday";

        //Match data
        String League = null;
        String mDate = null;
        String mTime = null;
        String Home = null;
        String Away = null;
        String Home_goals = null;
        String Away_goals = null;
        String match_id = null;
        String match_day = null;


        try {
            JSONArray matches = new JSONObject(JSONdata).getJSONArray(FIXTURES);


            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector<ContentValues>(matches.length());
            for (int i = 0; i < matches.length(); i++) {

                JSONObject match_data = matches.getJSONObject(i);
                League = match_data.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString("href");
                League = League.replace(SEASON_LINK, "");
                //This if statement controls which leagues we're interested in the data from.
                //add leagues here in order to have them be added to the DB.
                // If you are finding no data in the app, check that this contains all the leagues.
                // If it doesn't, that can cause an empty DB, bypassing the dummy data routine.
                if (League.equals(PREMIER_LEAGUE) ||
                        League.equals(SERIE_A) ||
                        League.equals(BUNDESLIGA1) ||
                        League.equals(BUNDESLIGA2) ||
                        League.equals(LIGUE1) ||
                        League.equals(LIGUE2) ||
                        League.equals(PRIMERA_LIGA) ||
                        League.equals(SEGUNDA_DIVISION) ||
                        League.equals(EREDIVISIE) ||
                        League.equals(PRIMERA_DIVISION)) {
                    match_id = match_data.getJSONObject(LINKS).getJSONObject(SELF).
                            getString("href");
                    match_id = match_id.replace(MATCH_LINK, "");
                    if (!isReal) {
                        //This if statement changes the match ID of the dummy data so that it all goes into the database
                        match_id = match_id + Integer.toString(i);
                    }

                    mDate = match_data.getString(MATCH_DATE);
                    mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"));
                    mDate = mDate.substring(0, mDate.indexOf("T"));
                    SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                    match_date.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                        new_date.setTimeZone(TimeZone.getDefault());
                        mDate = new_date.format(match_date.parse(mDate + mTime));
                        mTime = mDate.substring(mDate.indexOf(":") + 1);
                        mDate = mDate.substring(0, mDate.indexOf(":"));

                        if (!isReal) {
                            //This if statement changes the dummy data's date to match our current date range.
                            mDate = new SimpleDateFormat("yyyy-MM-dd").
                                    format(new Date(System.currentTimeMillis() + ((i - 2) * 86400000)));
                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, getString(R.string.error_parsing_data));
                        Toast.makeText(this, getString(R.string.error_parsing_data), Toast.LENGTH_SHORT).show();
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    Home = match_data.getString(HOME_TEAM);
                    Away = match_data.getString(AWAY_TEAM);
                    Home_goals = match_data.getJSONObject(RESULT).getString(HOME_GOALS);
                    Away_goals = match_data.getJSONObject(RESULT).getString(AWAY_GOALS);
                    match_day = match_data.getString(MATCH_DAY);
                    ContentValues matchValues = new ContentValues();
                    matchValues.put(DatabaseContract.ScoreEntry.MATCH_ID, match_id);
                    matchValues.put(DatabaseContract.ScoreEntry.DATE_COL, mDate);
                    matchValues.put(DatabaseContract.ScoreEntry.TIME_COL, mTime);
                    matchValues.put(DatabaseContract.ScoreEntry.HOME_COL, Home);
                    matchValues.put(DatabaseContract.ScoreEntry.AWAY_COL, Away);
                    matchValues.put(DatabaseContract.ScoreEntry.HOME_GOALS_COL, Home_goals);
                    matchValues.put(DatabaseContract.ScoreEntry.AWAY_GOALS_COL, Away_goals);
                    matchValues.put(DatabaseContract.ScoreEntry.LEAGUE_COL, League);
                    matchValues.put(DatabaseContract.ScoreEntry.MATCH_DAY, match_day);

                    values.add(matchValues);
                }
            }
            int inserted_data = 0;
            ContentValues[] insert_data = new ContentValues[values.size()];
            values.toArray(insert_data);
            inserted_data = mContext.getContentResolver().bulkInsert(
                    DatabaseContract.BASE_CONTENT_URI, insert_data);

            //Log.v(LOG_TAG,"Succesfully Inserted : " + String.valueOf(inserted_data));
        } catch (JSONException e) {
            Log.e(LOG_TAG, getString(R.string.error_parsing_data));
            Toast.makeText(this, getString(R.string.error_parsing_data), Toast.LENGTH_SHORT).show();
            Log.e(LOG_TAG, e.getMessage());
        }

    }
}

