package barqsoft.footballscores;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import barqsoft.footballscores.service.FootballAlarmReceiver;

public class MainActivity extends AppCompatActivity {
    public static String LOG_TAG = MainActivity.class.getSimpleName();
    public static final String SAVE_TAG = "Save Test";

    private static final String PAGER_CURRENT = "pager_current";
    private static final String SELECTED_MATCH = "selected_match";
    private static final String PAGER_FRAG = "pager_fragment";

    public static int selectedMatchId;
    public static int currentFragment = 2;
    private PagerFragment pagerFragment;

    FootballAlarmReceiver alarm = new FootballAlarmReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(LOG_TAG, "Reached MainActivity onCreate");

        if (savedInstanceState == null) {
            pagerFragment = new PagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, pagerFragment)
                    .commit();
        }

        alarm.setAlarm(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        logRestoreInstanceState("Will Save",
                String.valueOf(pagerFragment.mPagerHandler.getCurrentItem()), selectedMatchId);

        outState.putInt(PAGER_CURRENT, pagerFragment.mPagerHandler.getCurrentItem());
        outState.putInt(SELECTED_MATCH, selectedMatchId);
        getSupportFragmentManager().putFragment(outState, PAGER_FRAG, pagerFragment);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        logRestoreInstanceState("Will Restore",
                String.valueOf(savedInstanceState.getInt(PAGER_CURRENT)), savedInstanceState.getInt(SELECTED_MATCH));

        currentFragment = savedInstanceState.getInt(PAGER_CURRENT);
        selectedMatchId = savedInstanceState.getInt(SELECTED_MATCH);
        pagerFragment = (PagerFragment) getSupportFragmentManager().getFragment(savedInstanceState, PAGER_FRAG);

        super.onRestoreInstanceState(savedInstanceState);
    }

    private void logRestoreInstanceState(String state, String fragment, int id) {
        Log.v(SAVE_TAG, state);
        Log.v(SAVE_TAG,"fragment: " + fragment);
        Log.v(SAVE_TAG,"selected id: " + id);
    }

}
