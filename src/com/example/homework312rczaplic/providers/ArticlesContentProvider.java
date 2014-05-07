package com.example.homework312rczaplic.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.example.homework312rczaplic.Articles;
import com.example.homework312rczaplic.ArticlesSQLiteOpenHelper;

public class ArticlesContentProvider extends ContentProvider {

    public static final String TAG = ArticlesContentProvider.class.getSimpleName();

    public static final String AUTHORITY = "com.example.homework312rczaplic.providers.ArticlesContentProvider";

    private static final int ARTICLE 			= 1;
    private static final int ARTICLE_ID 		= 2;

    ArticlesSQLiteOpenHelper mSQLHelper;

    private static final UriMatcher mURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static
    {
        mURIMatcher.addURI(AUTHORITY, "articles", ARTICLE);
        mURIMatcher.addURI(AUTHORITY, "articles/#", ARTICLE_ID);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mSQLHelper.getWritableDatabase();
        int count;
        switch (mURIMatcher.match(uri)) {
        case ARTICLE:
            count = db.delete(Articles.Article.TABLE_NAME, selection, selectionArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {

        switch (mURIMatcher.match(uri)) {
        case ARTICLE:
            return Articles.DIR_CONTENT_TYPE;
        case ARTICLE_ID:
        		return Articles.ITEM_CONTENT_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues cv) {

        if (mURIMatcher.match(uri) != ARTICLE) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = mSQLHelper.getWritableDatabase();
        long rowID = db.insert(Articles.Article.TABLE_NAME, null, cv);
        if (rowID > 0) {

            Uri noteUri = ContentUris.withAppendedId(Articles.CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }
        else 
        {
            Log.e(TAG, "insert() Error inserting task");
        }

        return null;
    }

    @Override
    public boolean onCreate() {

        mSQLHelper = new ArticlesSQLiteOpenHelper(getContext());

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String id;
        
        switch (mURIMatcher.match(uri)) 
        {
        case ARTICLE:
            qb.setTables(Articles.Article.TABLE_NAME);
            break;

        case ARTICLE_ID:
    		qb.setTables(Articles.Article.TABLE_NAME);
    		selection = "_ID = ?";
    		id = uri.getLastPathSegment();
    		selectionArgs = new String[] {id};
    		break;
            
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = mSQLHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mSQLHelper.getWritableDatabase();

        int count;
        switch (mURIMatcher.match(uri))
        {
        case ARTICLE:
            count = db.update(Articles.Article.TABLE_NAME, values, selection, selectionArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }
}
