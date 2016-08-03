package com.example.chilin_wang.shoppinglist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "test";
    static final String TABLE_NAME = "table";
    static final String TABLE_ID = "table_id";
    static final String TABLE_NAME_BY_USER = "table_name_by_user";
    private static final int MENU_STATS_ADD = Menu.FIRST + 1;
    private LinearLayout mLinearLayout;
    private int mTableNum = 1;
    private MyCreateDBTable mMyCreateDBTable;
    private OnClickListener enterListListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Button b = (Button) v;
            Log.d(TAG, "button" + v.getId() + "   buttonName =" + b.getText().toString());
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, ShoppingListActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(TABLE_ID, v.getId());
            bundle.putString(TABLE_NAME_BY_USER, b.getText().toString());
            intent.putExtras(bundle);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLinearLayout = (LinearLayout) findViewById(R.id.viewObj);
        mMyCreateDBTable = new MyCreateDBTable(getApplicationContext());
        initViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem addTable = menu.add(0, MENU_STATS_ADD, 0, "Add").setIcon(R.drawable.ic_add_black_24dp);
        addTable.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_STATS_ADD:
                addNewTable();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMyCreateDBTable.close();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final View view = v;
        MenuItem rename = menu.add(0, 0, 0, "Rename");
        MenuItem copy = menu.add(0, 1, 0, "Copy");
        MenuItem delete = menu.add(0, 2, 0, "Delete");
        rename.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                reNameTable(view);
                return true;
            }
        });
        copy.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                copyTable(view);
                return true;
            }
        });
        delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mLinearLayout.removeAllViews();
                //deleate database table
                mMyCreateDBTable.deleteTable(TABLE_NAME + view.getId(), view.getId());
                initViews();
                return true;
            }
        });
    }

    private void initViews() {
        //init table list
        Cursor cursor = mMyCreateDBTable.getTableList();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                Log.d(TAG, " cursor.getInt(0) =" + cursor.getInt(0) + ", cursor.getString(1) =" + cursor.getInt(1) + " ,cursor.getString(3) =" + cursor.getString(3));
                addNewButton(this, cursor.getString(3), cursor.getInt(1));
                mTableNum = cursor.getInt(1) + 1;
            } while (cursor.moveToNext());
        } else {
            mTableNum = 1;
        }
        cursor.close();
    }

    private void addNewTable() {
        final EditText input = new EditText(MainActivity.this);
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Add new shopping list")
                .setMessage("Edit shopping title")
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //create new database table
                        String text = input.getText().toString();
                        if (text.trim().equals("")) {
                            text = "Shopping list " + mTableNum;
                        }
                        addNewButton(MainActivity.this, text, mTableNum);
                        mMyCreateDBTable.createTable(mTableNum, TABLE_NAME + mTableNum, text);
                        mTableNum++;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reNameTable(View v) {
        final View view = v;
        final EditText input = new EditText(v.getContext());
        new AlertDialog.Builder(v.getContext())
                .setTitle("Rename shopping list")
                .setMessage("Edit shopping title")
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //update database table
                        String text = input.getText().toString();
                        if (text.trim().equals("")) {
                            return;
                        }
                        mMyCreateDBTable.updateTable(view.getId(), text);
                        mLinearLayout.removeAllViews();
                        initViews();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addNewButton(Context context, String text, int id) {
        Button btn = new Button(context);
        btn.setText(text);
        btn.setId(id);
        btn.setOnClickListener(enterListListener);
        registerForContextMenu(btn);
        mLinearLayout.addView(btn);
    }

    private void copyTable(View v){
        final View view = v;
        final EditText input = new EditText(v.getContext());
        new AlertDialog.Builder(v.getContext())
                .setTitle("Copy shopping list")
                .setMessage("Edit shopping title")
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //create new database table
                        String text = input.getText().toString();
                        if (text.trim().equals("")) {
                            text = "Shopping list " + mTableNum;
                        }
                        addNewButton(MainActivity.this, text, mTableNum);
                        mMyCreateDBTable.createTable(mTableNum, TABLE_NAME + mTableNum, text);

                        //copy this table's item to database
                        mMyCreateDBTable.openTable(TABLE_NAME + view.getId());
                        Cursor cursor = mMyCreateDBTable.getData();
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            do {
                                Log.d(MainActivity.TAG, cursor.getLong(0) + "," + cursor.getString(1) + ","
                                        + cursor.getString(2) + "," + cursor.getString(3) + "," + cursor.getString(4)
                                        + "," + cursor.getString(5) + "," + cursor.getString(6));
                                mMyCreateDBTable.insertToTable(TABLE_NAME + mTableNum, cursor.getString(1), cursor.getInt(2), cursor.getString(3), cursor.getInt(4), cursor.getString(6));
                            } while (cursor.moveToNext());
                        }
                        cursor.close();
                        mTableNum++;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}
