package com.example.myapplication;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.Arrays;

public class MediaProvider extends ContentProvider {
    public static UriMatcher A0B;

    static {
        UriMatcher uriMatcher;
        synchronized (MediaProvider.class) {
            if (A0B == null) {
                UriMatcher uriMatcher2 = new UriMatcher(-1);
                A0B = uriMatcher2;
                String str = "com.whatsapp.provider.media1";
                uriMatcher2.addURI(str, "buckets", 1);
                A0B.addURI(str, "items", 2);
                A0B.addURI(str, "item/#", 3);
                A0B.addURI(str, "gdpr_report", 4);
                A0B.addURI(str, "export_chat/*/*", 5);
            }
            uriMatcher = A0B;
        }
    }

    public MediaProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        Log.i("xiabo", "MediaProvider delete " + uri);
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        Log.i("xiabo", "MediaProvider insert " + uri);
        return uri;
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        Log.i("xiabo", "MediaProvider onCreate");
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        Log.i("xiabo", "MediaProvider query " + uri + " projection " + Arrays.toString(projection));
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        Log.i("xiabo", "MediaProvider update " + uri);
        return 0;
    }
}
