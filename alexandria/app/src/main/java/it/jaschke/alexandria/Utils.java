package it.jaschke.alexandria;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import it.jaschke.alexandria.data.AlexandriaContract;

/**
 * Created by Dobrunov on 16.10.2015.
 */
public class Utils {

    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static String readStringDataFromCursor(Context context, Cursor data, String columnProjection) {
        String value = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));

        return null != value ? value : context.getString(R.string.no_information);
    }
}
