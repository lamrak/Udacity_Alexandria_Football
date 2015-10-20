package barqsoft.footballscores.service;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.R;
import barqsoft.footballscores.provider.DatabaseContract;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();

    private static final String[] DETAIL_COLUMNS = {
            DatabaseContract.ScoreEntry.MATCH_DAY,
            DatabaseContract.ScoreEntry.HOME_COL,
            DatabaseContract.ScoreEntry.AWAY_COL
    };
    // these indices must match the projection
    public static final int COL_MATCHTIME = 0;
    public static final int COL_HOME = 1;
    public static final int COL_AWAY = 2;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

                String selection[] = {new SimpleDateFormat("yyyy-MM-dd").
                        format(new Date(System.currentTimeMillis())).toString()}; //
                data = getContentResolver().query(DatabaseContract.ScoreEntry.buildScoreWithDate(),
                        DETAIL_COLUMNS, null, selection, null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);

                long dateInMillis = data.getLong(COL_MATCHTIME);
                String homeTeam = data.getString(COL_HOME);
                String awayTeam = data.getString(COL_AWAY);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, "Match description");
                }
                views.setTextViewText(R.id.tv_team_home, homeTeam);
                views.setTextViewText(R.id.tv_team_away, awayTeam);

                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
//                views.setContentDescription(R.id., description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}