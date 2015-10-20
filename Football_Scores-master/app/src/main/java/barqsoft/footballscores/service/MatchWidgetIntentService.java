package barqsoft.footballscores.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.provider.DatabaseContract;
import barqsoft.footballscores.widget.MatchWidgetProvider;

public class MatchWidgetIntentService extends IntentService {
    private static final String[] SCORE_COLUMNS = {
            DatabaseContract.ScoreEntry.MATCH_DAY
    };
    // these indices must match the projection
//    private static final int INDEX_MATCH_ID = 0;

    public MatchWidgetIntentService() {
        super("MatchWidgetIntentService");
    }

        @Override
        protected void onHandleIntent(Intent intent) {
            // Retrieve all of the Today widget ids: these are the widgets we need to update
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                    MatchWidgetProvider.class));

            // Get today's data from the ContentProvider
            int countGamesYesterday = fetchMatchByDate(new Date(System.currentTimeMillis() - (1 * 86400000)));
            int countGamesToday = fetchMatchByDate(new Date(System.currentTimeMillis()));
            int countGamesTomorrow = fetchMatchByDate(new Date(System.currentTimeMillis() + (1 * 86400000)));

            // Perform this loop procedure for each widget
            for (int appWidgetId : appWidgetIds) {
                int layoutId = R.layout.widget_today_small;
                RemoteViews views = new RemoteViews(getPackageName(), layoutId);

                // Add the data to the RemoteViews
                views.setImageViewResource(R.id.ic_w_before, R.drawable.ic_w_before);
                views.setImageViewResource(R.id.ic_w_now, R.drawable.ic_w_now);
                views.setImageViewResource(R.id.ic_w_after, R.drawable.ic_w_after);
                // Content Descriptions for RemoteViews were only added in ICS MR1
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {

                    setRemoteContentDescription(views, getString(R.string.football_widget));
                }
                views.setTextViewText(R.id.yesterday_games, ": " + countGamesYesterday);
                views.setTextViewText(R.id.today_games, ": " + countGamesToday);
                views.setTextViewText(R.id.tomorrow_games, ": " + countGamesTomorrow);

                // Create an Intent to launch MainActivity
                Intent launchIntent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
                views.setOnClickPendingIntent(R.id.widget, pendingIntent);

                // Tell the AppWidgetManager to perform an update on the current app widget
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }

            // Release the wake lock provided by the BroadcastReceiver.
            FootballAlarmReceiver.completeWakefulIntent(intent);
        }

    private int fetchMatchByDate(Date date) {
        String selection[] = {new SimpleDateFormat("yyyy-MM-dd").format(date).toString()};
        Cursor data = getContentResolver().query(DatabaseContract.ScoreEntry.buildScoreWithDate(), SCORE_COLUMNS, null,
                selection, null);
        if (data == null) {
            return 0;
        }

        int countGamesToday = 0;
        if (data.moveToFirst()) {
            countGamesToday++;
            while (data.moveToNext()) {
                countGamesToday++;
            }
        }
        data.close();

        return countGamesToday;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
        private void setRemoteContentDescription(RemoteViews views, String description) {
            views.setContentDescription(R.id.ic_w_before, description);
            views.setContentDescription(R.id.ic_w_now, description);
            views.setContentDescription(R.id.ic_w_after, description);
        }

}
