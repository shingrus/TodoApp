package com.shingrus.todaytodo.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.DatePicker;

import com.shingrus.todaytodo.R;
import com.shingrus.todaytodo.ui.tododetails.TodoDetailsFragment;
import com.shingrus.todaytodo.ui.todolist.TodoListFragment;
import com.shingrus.todaytodo.utils.DatePickerFragment;
import com.shingrus.todaytodo.utils.Utility;

import java.util.Calendar;

public class TodoItemsActivity extends AppCompatActivity implements TodoListFragment
        .TodoListFragmentListener, TodoDetailsFragment.TodoDetailsFragmentListener
{
    private static final String TAG_FRAGMENT_DETAILS = "detailsfragment";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_items);
        showTodoListFragment();
    }

    private void showTodoListFragment()
    {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.activity_todo_items_fl_fragment_container, new TodoListFragment());
        transaction.commit();
        setActionBarTitle(R.string.todo_list);
    }

    @Override
    public void onBackPressed()
    {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
        {
            getSupportFragmentManager().popBackStack();
            setActionBarTitle(R.string.todo_list);
        }
        else
        {
            super.onBackPressed();
        }
    }

    private void setActionBarTitle(@StringRes int stringRes)
    {
        try
        {
            getSupportActionBar().setTitle(stringRes);
        }
        catch (NullPointerException ex)
        {
        }
    }

    @Override
    public void displayTodoDetailsFragment(Bundle bundle)
    {
        TodoDetailsFragment fragment = new TodoDetailsFragment();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id
                .activity_todo_items_fl_fragment_container, fragment, TAG_FRAGMENT_DETAILS)
                .addToBackStack(null).commit();
        setActionBarTitle(R.string.todo_details);
    }

    @Override
    public void finishDetailsFragment()
    {
        getSupportFragmentManager().popBackStack();
        setActionBarTitle(R.string.todo_list);
    }
}
