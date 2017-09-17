package com.shingrus.todaytodo.ui.todolist;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewSwitcher;

import com.shingrus.todaytodo.R;
import com.shingrus.todaytodo.TodoApplication;
import com.shingrus.todaytodo.database.QueryHandler;
import com.shingrus.todaytodo.database.TodoContract;
import com.shingrus.todaytodo.utils.SpacesItemDecoration;
import com.shingrus.todaytodo.utils.Utility;

public class TodoListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        TodoListAdapter.OnRowClickListener {
    private TodoListAdapter mAdapter;
    private TodoListFragmentListener mFragmentListener;
    private ViewSwitcher mSwitcher;
    public static final String URI_KEY = "uri";
    private static final int LOADER_ID = 1;

    public TodoListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof TodoListFragmentListener) {
            mFragmentListener = (TodoListFragmentListener) getActivity();
        }
        Log.d("FRAG", "OnCreate");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {
        Log.d("FRAG", "OnCreateView");

        return inflater.inflate(R.layout.fragment_todo_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d("FRAG", "OnActivityCreated");


        View view = getView();
        mSwitcher = (ViewSwitcher) view.findViewById(R.id.fragment_todo_list_switcher);

        getLoaderManager().initLoader(LOADER_ID, null, this);


        //TODO: use button click listener
        //
        mAdapter = new TodoListAdapter(getContext(), null, this);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id
                .fragment_todo_list_rv_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);

        recyclerView.addItemDecoration(new SpacesItemDecoration((int) Utility.dpToPx(5,
                getContext())));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        view.findViewById(R.id.fragment_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayDetailsFragment(null);
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("FRAG", "OnStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("FRAGM", "OnResume");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        long adjustedNow = System.currentTimeMillis()/ 1000 - TodoListAdapter.AUTODONE_TIMEOUT;

        String[] projection = {TodoContract.Todo.Columns._ID, TodoContract.Todo.Columns.TITLE,
                TodoContract.Todo.Columns.INSERTED, TodoContract.Todo.Columns.STATUS};
        String sortOrder = TodoContract.Todo.Columns.STATUS + " DESC, "
                + TodoContract.Todo.Columns.INSERTED + " > " + adjustedNow
                + "," + TodoContract.Todo.Columns.TOUCHED + " DESC";


        return new CursorLoader(getContext(), TodoContract.Todo.CONTENT_URI, projection, null,
                null, sortOrder);
    }



    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0) {
            mAdapter.swapCursor(data);

            if (R.id.fragment_todo_list_rv_list == mSwitcher.getNextView().getId()) {
                mSwitcher.showNext();
            }
        } else if (R.id.fragment_todo_list_tv_empty_text == mSwitcher.getNextView().getId()) {
            mSwitcher.showNext();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onRowClick(Uri uri, String message, int RowId, boolean isOutDated) {
        if (isOutDated) {
            showRessurectDialog(message, RowId);
        }
        else {
            Bundle bundle = new Bundle();
            bundle.putParcelable(URI_KEY, uri);
            displayDetailsFragment(bundle);
        }
    }

    @Override
    public void onCheckedChanged(int rowId, boolean checked) {
        String selection = TodoContract.Todo.Columns._ID + " = ?";
        ContentValues values = new ContentValues();
        values.put(TodoContract.Todo.Columns.STATUS, String.valueOf(
                checked ? TodoContract.Todo.TODO_STATUS.COMPLETE : TodoContract.Todo.TODO_STATUS.INCOMPLETE));;
        QueryHandler queryHandler = new QueryHandler(getContext(), null);

        String[] selectionArg = {String.valueOf(rowId)};
        queryHandler.startUpdate(QueryHandler.OperationToken.TOKEN_UPDATE, null, TodoContract
                .Todo.CONTENT_URI, values, selection, selectionArg);

    }

    private void displayDetailsFragment(Bundle bundle) {
        if (mFragmentListener != null) {
            mFragmentListener.displayTodoDetailsFragment(bundle);
        }
    }

    @Override
    public void onRowLongClick(String message, int rowId) {
        showDeleteDialog(message, rowId);
    }


    private void showDeleteDialog(String message, final int rowId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String selection = TodoContract.Todo.Columns._ID + " = ?";
                QueryHandler queryHandler = new QueryHandler(getContext(), null);
                queryHandler.startDelete(QueryHandler.OperationToken.TOKEN_DELETE, null,
                        TodoContract.Todo.CONTENT_URI, selection, new
                                String[]{String.valueOf(rowId)});
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setTitle(getString(R.string.delete_dialog_title));
        dialog.setMessage(getString(R.string.delete_dialog_message) + message);
        dialog.show();
    }

    private void showRessurectDialog(String message, final int rowId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String selection = TodoContract.Todo.Columns._ID + " = ?";
                ContentValues values = new ContentValues();
                values.put(TodoContract.Todo.Columns.STATUS, String.valueOf(TodoContract.Todo.TODO_STATUS.INCOMPLETE));;
                long time = System.currentTimeMillis()/1000;
                values.put(TodoContract.Todo.Columns.INSERTED, time);
                QueryHandler queryHandler = new QueryHandler(getContext(), null);

                String[] selectionArg = {String.valueOf(rowId)};
                queryHandler.startUpdate(QueryHandler.OperationToken.TOKEN_UPDATE, null, TodoContract
                        .Todo.CONTENT_URI, values, selection, selectionArg);

            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setTitle(getString(R.string.ressurect_dialog_title));
        dialog.setMessage(getString(R.string.ressurect_dialog_message) + message);
        dialog.show();
    }

    @Override
    public void onDestroy() {
        TodoApplication.getRefWatcher(getContext()).watch(this);
        super.onDestroy();
    }

    public interface TodoListFragmentListener {
        void displayTodoDetailsFragment(Bundle bundle);
    }
}
