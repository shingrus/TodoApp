package com.shingrus.todaytodo.ui.tododetails;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.shingrus.todaytodo.R;
import com.shingrus.todaytodo.TodoApplication;
import com.shingrus.todaytodo.database.QueryHandler;
import com.shingrus.todaytodo.database.TodoContract;
import com.shingrus.todaytodo.ui.todolist.TodoListFragment;

import java.util.Calendar;

import static com.shingrus.todaytodo.utils.Utility.getDate;



public class TodoDetailsFragment extends Fragment implements View.OnClickListener, QueryHandler
        .AsyncQueryListener, TextView.OnEditorActionListener {
    private TextView mDate;
    private TodoDetailsFragmentListener mFragmentListener;
    private EditText mTitle;
    private EditText mDescription;
    private CheckBox mCheckBoxStatus;
    private View mDateLayout;
    private View mStatusLayout;
//    private Spinner mSpinner;
    private Uri mUri;

    public TodoDetailsFragment()
    {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof TodoListFragment.TodoListFragmentListener)
        {
            mFragmentListener = (TodoDetailsFragmentListener) getActivity();
        }

        Bundle bundle = getArguments();
        if (bundle != null && !bundle.isEmpty())
        {
            mUri = bundle.getParcelable(TodoListFragment.URI_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState)
    {
        Log.d("FRAG", "OnCreateView");
        return inflater.inflate(R.layout.fragment_todo_details, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        if (mUri != null)
        {
            QueryHandler handler = new QueryHandler(getContext(), this);
            handler.startQuery(QueryHandler.OperationToken.TOKEN_QUERY, null, mUri, null, null,
                    null, null);
        }

    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
//        mSpinner = (Spinner) view.findViewById(R.id.fragment_todo_details_spn_priority);
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R
//                .array.priority_array, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mSpinner.setAdapter(adapter);

        mDate = view.findViewById(R.id.fragment_todo_details_tv_date);

//        setDate(getDate(Calendar.getInstance()));

        mTitle = view.findViewById(R.id.fragment_todo_details_et_title);
        mTitle.setOnEditorActionListener(this);
        if(mUri==null) {
            mTitle.requestFocus();
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mTitle, InputMethodManager.SHOW_IMPLICIT);
        }
        mDescription = view.findViewById(R.id.fragment_todo_details_et_desc);
        mCheckBoxStatus = view.findViewById(R.id.fragment_todo_details_cx_status);
        mDateLayout = view.findViewById(R.id.fragment_todo_details_ll_date_container);
        mDateLayout.setVisibility(View.GONE);

        mStatusLayout = view.findViewById(R.id.fragment_todo_details_ll_status_container);
        mStatusLayout.setVisibility(View.GONE);
        view.findViewById(R.id.fragment_todo_details_btn_save_todo).setOnClickListener(this);
    }


    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
//            case R.id.fragment_todo_details_ll_date_container:
//                if (mFragmentListener != null)
//                {
//                    mFragmentListener.displayDatePickerDialog();
//                }
//                break;
            case R.id.fragment_todo_details_btn_save_todo:
            case R.id.fragment_todo_details_et_title:
                if (validateTitle())
                {
                    saveTodo();
                    InputMethodManager inputManager = (InputMethodManager) getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    // check if no view has focus:
                    View v = ((Activity) getContext()).getCurrentFocus();
                    if (v == null)
                        return;

                    inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                else
                {
                    Toast.makeText(getContext(), R.string.err_title_blank, Toast.LENGTH_SHORT)
                            .show();
                }
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_ACTION_DONE) {
            //do the same as save button
            onClick(textView);
            return true;
        }
        return false;
    }

    private boolean validateTitle()
    {
        return !mTitle.getText().toString().trim().isEmpty();
    }


    private void saveTodo()
    {
        QueryHandler queryHandler = new QueryHandler(getContext(), null);
        String title = mTitle.getText().toString().replaceAll("\\s{2,}", " ").replaceAll("" + "" +
                "(\\n\\r?)+", " ").trim();
        String description = mDescription.getText().toString().replaceAll("\\s{2,}", " ")
                .replaceAll("(\\n\\r?)+", " ").trim();
        String date = mDate.getText().toString();
        ContentValues values = new ContentValues();
        values.put(TodoContract.Todo.Columns.TITLE, title);
        values.put(TodoContract.Todo.Columns.DESCRIPTION, description);
        values.put(TodoContract.Todo.Columns.STATUS,
                String.valueOf(mCheckBoxStatus.isChecked() ? TodoContract.Todo.TODO_STATUS.COMPLETE : TodoContract.Todo.TODO_STATUS.INCOMPLETE));
        if (mUri != null)
        {
            long _id = ContentUris.parseId(mUri);
            String selection = TodoContract.Todo.Columns._ID + " = ?";
            String[] selectionArg = {String.valueOf(_id)};
            queryHandler.startUpdate(QueryHandler.OperationToken.TOKEN_UPDATE, null, TodoContract
                    .Todo.CONTENT_URI, values, selection, selectionArg);
        }
        else
        {
            queryHandler.startInsert(QueryHandler.OperationToken.TOKEN_INSERT, null, TodoContract
                    .Todo.CONTENT_URI, values);
        }
        finishFragment();
    }

    private void finishFragment()
    {
        if (mFragmentListener != null)
        {
            mFragmentListener.finishDetailsFragment();
        }
    }

    @Override
    public void onDestroy()
    {

        TodoApplication.getRefWatcher(getContext()).watch(this);
        super.onDestroy();
    }

//    public void setDate(String date)
//    {
//        mDate.setText(date);
//    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor)
    {
        if (cursor != null && cursor.getCount() == 1 && cursor.moveToFirst())
        {
            mTitle.setText(cursor.getString(cursor.getColumnIndex(TodoContract.Todo.Columns
                    .TITLE)));

            mDescription.setText(cursor.getString(cursor.getColumnIndex(TodoContract.Todo.Columns
                    .DESCRIPTION)));
            int date = cursor.getInt(cursor.getColumnIndex(TodoContract.Todo.Columns.INSERTED));
            mDate.setText(TodoContract.getInsertedDate(date));
            mDateLayout.setVisibility(View.VISIBLE);
            mStatusLayout.setVisibility(View.VISIBLE);


            if (cursor.getString(cursor.getColumnIndex(TodoContract.Todo.Columns.STATUS)).equals(String.valueOf(TodoContract.Todo.TODO_STATUS.COMPLETE))) {
                mCheckBoxStatus.setChecked(true);
            }


//            mSpinner.setSelection(cursor.getInt(cursor.getColumnIndex(TodoContract.Todo.Columns
//                    .PRIORITY)), true);
            cursor.close();
        }
        else
        {
            Toast.makeText(getContext(), R.string.invalid_state, Toast.LENGTH_SHORT)
                    .show();
            finishFragment();
        }
    }



    public interface TodoDetailsFragmentListener
    {
//        void displayDatePickerDialog();

        void finishDetailsFragment();
    }
}
