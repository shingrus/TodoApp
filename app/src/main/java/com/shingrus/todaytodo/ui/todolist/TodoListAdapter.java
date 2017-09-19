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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.shingrus.todaytodo.R;
import com.shingrus.todaytodo.database.TodoContract;
import com.shingrus.todaytodo.utils.CursorRecyclerAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


class TodoListAdapter extends CursorRecyclerAdapter<TodoListAdapter.ItemViewHolder> {
    private static final String TAG = "TADAPTER";
    private OnRowClickListener mClickListener;
    static final int AUTODONE_TIMEOUT = 86400 * 1000; //ms
    private final String timeLeftFormat;


//    private String[] mPriorityArray;
//    private int[] mPrColorArray;

    interface OnRowClickListener {
        void onRowClick(Uri uri, String Message, int rowId, boolean outdated);

        void onRowLongClick(String message, int row_id);

        void onCheckedChanged(int rowId, boolean checked);
    }


    TodoListAdapter(Context context, Cursor cursor, OnRowClickListener clickListener) {
        super(cursor);
        Resources resources = context.getResources();
        mClickListener = clickListener;

        timeLeftFormat = context.getString(R.string.time_left_format);

    }


    @Override
    public void onBindViewHolder(ItemViewHolder holder, final Cursor cursor) {

        holder.title.setText(cursor.getString(cursor.getColumnIndex(TodoContract.Todo.Columns
                .TITLE)));

        String status = cursor.getString(cursor.getColumnIndex(TodoContract.Todo.Columns.STATUS));
        boolean isCompleted = status.equals(String.valueOf(TodoContract.Todo.TODO_STATUS.COMPLETE));


        int date = cursor.getInt(cursor.getColumnIndex(TodoContract.Todo.Columns.INSERTED));

        long currentTimeMillis = System.currentTimeMillis();
        long longdate = (long) date * 1000;
        long hoursLeft = (longdate + AUTODONE_TIMEOUT - currentTimeMillis) / 1000 / 3600;

        boolean longerThanTimeout = isOutdated(longdate, currentTimeMillis);

        holder.cbx.setChecked(isCompleted);


        if(longerThanTimeout) {
            holder.cbx.setVisibility(View.GONE);
            holder.date.setText(TodoContract.getInsertedDate(date));
        }
        else {

            holder.cbx.setVisibility(View.VISIBLE);
            holder.date.setText(String.format(timeLeftFormat, hoursLeft));
        }

        if ( isCompleted || longerThanTimeout) {
            holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.itemView.setAlpha(0.4f);
        } else {

            holder.itemView.setAlpha(1f);
            holder.title.setPaintFlags(holder.title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private boolean isOutdated(long longdate, long currentTimeMillis) {
        return ((currentTimeMillis - longdate) > AUTODONE_TIMEOUT);
    }


    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                .todo_list_row, parent, false));
    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View
            .OnLongClickListener, CompoundButton.OnCheckedChangeListener {
        private final TextView title;
        private final TextView date;
        private final CheckBox cbx;
//        private final Button button;
//        private final TextView priority;

        ItemViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.todo_list_row_tv_title);
            date = itemView.findViewById(R.id.todo_list_row_tv_date);
            cbx = itemView.findViewById(R.id.todo_list_row_cx_completed);
//            button = itemView.findViewById(R.id.todo_list_row_b_done);
//            priority = (TextView) itemView.findViewById(R.id.todo_list_row_tv_priority);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            cbx.setOnCheckedChangeListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                if (mCursor.moveToPosition(getAdapterPosition())) {
                    int columnIdIndex = mCursor.getColumnIndex(TodoContract.Todo.Columns._ID);
                    int columnDataIndex = mCursor.getColumnIndex(TodoContract.Todo.Columns.TITLE);
                    int columnInsertedIndex = mCursor.getColumnIndex(TodoContract.Todo.Columns.INSERTED);
                    long date = mCursor.getInt(columnInsertedIndex) *1000L;
                    long currentTime = System.currentTimeMillis();
                    Uri uri = TodoContract.Todo.buildRowUri(mCursor.getInt(columnIdIndex));
                    mClickListener.onRowClick(uri, mCursor.getString(columnDataIndex),
                            mCursor.getInt(columnIdIndex),
                            isOutdated(date,currentTime));
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (mClickListener != null) {
                if (mCursor.moveToPosition(getAdapterPosition())) {
                    int columnIdIndex = mCursor.getColumnIndex(TodoContract.Todo.Columns._ID);
                    int columnDataIndex = mCursor.getColumnIndex(TodoContract.Todo.Columns.TITLE);
                    mClickListener.onRowLongClick(mCursor.getString(columnDataIndex), mCursor
                            .getInt(columnIdIndex));
                }
            }
            return true;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            //checkbox listener
            if (mClickListener != null) {
                if (mCursor.moveToPosition(getAdapterPosition())) {

                    mClickListener.onCheckedChanged(mCursor.getInt(mCursor.getColumnIndex(TodoContract.Todo.Columns._ID)), b);
                }

            }
        }
    }
}
