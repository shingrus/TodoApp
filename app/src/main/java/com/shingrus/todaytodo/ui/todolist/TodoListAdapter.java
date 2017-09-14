package com.shingrus.todaytodo.ui.todolist;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.shingrus.todaytodo.R;
import com.shingrus.todaytodo.database.TodoContract;
import com.shingrus.todaytodo.utils.CursorRecyclerAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TodoListAdapter extends CursorRecyclerAdapter<TodoListAdapter.ItemViewHolder>
{
    private static final String TAG = "TADAPTER";
    private OnRowClickListener mClickListener;
    public static final int AUTODONE_TIMEOUT = 86400*1000; //ms
    

//    private String[] mPriorityArray;
//    private int[] mPrColorArray;

    public interface OnRowClickListener
    {
        void onRowClick(Uri uri);

        void onRowLongClick(String message, int row_id);
    }


    public TodoListAdapter(Context context, Cursor cursor, OnRowClickListener clickListener)
    {
        super(cursor);
        Resources resources = context.getResources();
        mClickListener = clickListener;




//        mPriorityArray = resources.getStringArray(R.array.priority_array);
//        mPrColorArray = new int[]{ContextCompat.getColor(context, android.R.color.holo_red_dark),
//                ContextCompat.getColor(context, android.R.color.holo_orange_dark), ContextCompat
//                .getColor(context, android.R.color.holo_green_dark)};
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, final Cursor cursor) {

        Log.d(TAG, "onBindViewHolder called");
        holder.title.setText(cursor.getString(cursor.getColumnIndex(TodoContract.Todo.Columns
                .TITLE)));

        long currentTimeMillis = System.currentTimeMillis();

        int date = cursor.getInt(cursor.getColumnIndex(TodoContract.Todo.Columns.INSERTED));
        long longdate = (long) date * 1000;
        holder.date.setText(TodoContract.getInsertedDate(date));

        String status = cursor.getString(cursor.getColumnIndex(TodoContract.Todo.Columns.STATUS));
        Log.d(TAG, "LongDate: " + longdate + " Cur:" + currentTimeMillis + " Status: " + status);
        if (status.equals(String.valueOf(TodoContract.Todo.TODO_STATUS.COMPLETE)) ||
                (currentTimeMillis - longdate > AUTODONE_TIMEOUT)) {
//            holder.button.setVisibility(View.INVISIBLE);
            holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            holder.title.setPaintFlags(holder.title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                .todo_list_row, parent, false));
    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View
            .OnLongClickListener
    {
        private final TextView title;
        private final TextView date;
//        private final Button button;
//        private final TextView priority;

        ItemViewHolder(View itemView)
        {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.todo_list_row_tv_title);
            date = (TextView) itemView.findViewById(R.id.todo_list_row_tv_date);
//            button = itemView.findViewById(R.id.todo_list_row_b_done);
//            priority = (TextView) itemView.findViewById(R.id.todo_list_row_tv_priority);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view)
        {
            if (mClickListener != null)
            {
                if (mCursor.moveToPosition(getAdapterPosition()))
                {
                    int columnIdIndex = mCursor.getColumnIndex(TodoContract.Todo.Columns._ID);
                    Uri uri = TodoContract.Todo.buildRowUri(mCursor.getInt(columnIdIndex));
                    mClickListener.onRowClick(uri);
                }
            }
        }

        @Override
        public boolean onLongClick(View view)
        {
            if (mClickListener != null)
            {
                if (mCursor.moveToPosition(getAdapterPosition()))
                {
                    int columnIdIndex = mCursor.getColumnIndex(TodoContract.Todo.Columns._ID);
                    int columnDataIndex = mCursor.getColumnIndex(TodoContract.Todo.Columns.TITLE);
                    mClickListener.onRowLongClick(mCursor.getString(columnDataIndex), mCursor
                            .getInt(columnIdIndex));
                }
            }
            return true;
        }
    }
}
