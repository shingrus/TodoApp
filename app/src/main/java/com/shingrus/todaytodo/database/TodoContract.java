package  com.shingrus.todaytodo.database;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.SimpleDateFormat;

public final class TodoContract
{
    public static final String CONTENT_AUTHORITY = "com.shingrus.todaytodo.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Tables specific path:
    public static final String RELATIVE_TODO_URI = "todo";
    final static  SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    public static final String getInsertedDate (int seconds) {
        return sdf.format(seconds*1000);
    }

    public static class Todo
    {
        // URI for the table
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath
                (RELATIVE_TODO_URI).build();

        // Entire table
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.shingrus" + "" +
                ".todaytodo.provider.todo";
        // Single row within the table
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.shingrus"
                + ".todaytodo.provider.todo";

        // Table name
        public static final String TABLE_NAME = "todo";

        // Define table columns
        public interface Columns extends BaseColumns
        {
            String TITLE = "title";
            String DESCRIPTION = "description";
//            String DATE = "date";
//            String PRIORITY = "priority";
            String INSERTED  = "inserted_at";
            String STATUS = "status";
        }


        private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");




        public enum TODO_STATUS {
            INCOMPLETE, COMPLETE
        }
        public static Uri buildRowUri(long id)
        {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
}
