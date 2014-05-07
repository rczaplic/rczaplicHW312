package com.example.homework312rczaplic;

import com.example.homework312rczaplic.providers.ArticlesContentProvider;
import android.net.Uri;
import android.provider.BaseColumns;

public class Articles implements BaseColumns {

    public static final Uri CONTENT_URI = Uri.parse("content://" + ArticlesContentProvider.AUTHORITY + "/articles");
    public static final String DIR_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.example.homework312rczaplic.provider.articles";
    public static final String ITEM_CONTENT_TYPE = "vnd.android.cursor.item/vnd.com.example.homework312rczaplic.provider.article";

    public static final class Article {
        
        public static final String TABLE_NAME = "article";

        public static final String ID = BaseColumns._ID;
        public static final String CONTENT = "content";
        public static final String TITLE = "title";
        public static final String DATE = "date";
        public static final String ICON = "icon";
        public static final String DATE_VALUE = "date_value";
        public static final String SOURCE = "source";

        public static final String[] PROJECTION = new String[] {
        /* 0 */ Articles.Article.ID,
        /* 1 */ Articles.Article.CONTENT,
        /* 2 */ Articles.Article.TITLE,
        /* 3 */ Articles.Article.DATE,
        /* 4 */ Articles.Article.ICON,
        /* 5 */ Articles.Article.DATE_VALUE,
        /* 6 */ Articles.Article.SOURCE};

    }

}
