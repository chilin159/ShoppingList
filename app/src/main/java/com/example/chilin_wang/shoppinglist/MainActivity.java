package com.example.chilin_wang.shoppinglist;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "test";
    static final String TABLE_NAME = "table";
    static final String TABLE_ID = "table_id";
    static final String TABLE_NAME_BY_USER = "table_name_by_user";
    private static final int MENU_STATS_ADD = Menu.FIRST + 1;
    private static final int MENU_STATS_MERGE = Menu.FIRST + 2;
    private static final int MENU_STATS_DELETE = Menu.FIRST + 3;
    private LinearLayout mLinearLayout;
    private int mTableNum = 1;
    private MyCreateDBTable mMyCreateDBTable;
    private  boolean[] mCheckedList;
    private boolean mHasChecked;
    private float historicX = Float.NaN, historicY = Float.NaN;
    private boolean mIsDeleteItemBySlide = false;
    private boolean mIsFirstAddButton;
    static final int DELTA = 150;
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
        MenuItem addTable = menu.add(0, MENU_STATS_ADD, 0, R.string.menu_add).setIcon(R.drawable.ic_add_black_24dp);
        addTable.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, MENU_STATS_MERGE, 0, R.string.menu_merge_lists);
        menu.add(0, MENU_STATS_DELETE, 0, R.string.menu_delete);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_STATS_ADD:
                addNewTable();
                break;
            case MENU_STATS_MERGE:
                ArrayList<String> itemArray = new ArrayList<String>();
                Cursor cursor = mMyCreateDBTable.getTableList();
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        itemArray.add(cursor.getString(3));
                    } while (cursor.moveToNext());

                String[] itemList = itemArray.toArray(new String[itemArray.size()]);
                mCheckedList = new boolean[itemArray.size()];
                for(int i =0; i< itemArray.size(); i++){
                    mCheckedList[i] = false;
                }
                mHasChecked = false;
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.merge_shopping_list))
                        .setMultiChoiceItems(itemList, mCheckedList, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                mCheckedList[which] = isChecked;
                                if(isChecked) {
                                    mHasChecked = true;
                                }
                            }
                        })
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                                mergeTable();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
                }
                cursor.close();
                break;
            case MENU_STATS_DELETE:
                ArrayList<String> itemdeleteArray = new ArrayList<String>();
                Cursor cursor2 = mMyCreateDBTable.getTableList();
                if (cursor2.getCount() > 0) {
                    cursor2.moveToFirst();
                    do {
                        itemdeleteArray.add(cursor2.getString(3));
                    } while (cursor2.moveToNext());
                }
                cursor2.close();
                String[] itemdeleteList = itemdeleteArray.toArray(new String[itemdeleteArray.size()]);
                mCheckedList = new boolean[itemdeleteArray.size()];
                for(int i =0; i< itemdeleteArray.size(); i++){
                    mCheckedList[i] = false;
                }
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.delete_shopping_list))
                        .setMultiChoiceItems(itemdeleteList, mCheckedList, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                mCheckedList[which] = isChecked;
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                deleteTable();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLinearLayout.removeAllViews();
        mMyCreateDBTable.close();
        System.exit(0);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if(mIsDeleteItemBySlide)return;
        super.onCreateContextMenu(menu, v, menuInfo);
        final View view = v;
        MenuItem rename = menu.add(0, 0, 0, R.string.menu_rename);
        MenuItem copy = menu.add(0, 1, 0, R.string.menu_copy);
        MenuItem share = menu.add(0, 2, 0, R.string.menu_share);
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
        share.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String msgText = "";
                mMyCreateDBTable.openTable(TABLE_NAME + view.getId());
                Cursor cursor = mMyCreateDBTable.getData();
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        msgText = msgText + cursor.getString(1) + "\n" + String.valueOf(cursor.getInt(2)) + cursor.getString(3) +
                                " " + String.valueOf(cursor.getFloat(4)) + cursor.getString(5) +
                                "\n" + String.valueOf(cursor.getFloat(6)) + cursor.getString(5) + "/" + cursor.getString(3) +
                                "\n" + cursor.getString(7) + "\n\n";
                    } while (cursor.moveToNext());
                }
                cursor.close();
                intent.putExtra(Intent.EXTRA_TEXT, msgText);
                startActivity(Intent.createChooser(intent, getString(R.string.menu_share)));
                return true;
            }
        });
    }

    private void initViews() {
        mIsFirstAddButton = true;
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
                .setTitle(getString(R.string.add_new_shopping_list))
                .setMessage(getString(R.string.edit_shoppinglist_title))
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //create new database table
                        String text = input.getText().toString();
                        if (text.trim().equals("")) {
                            text = getString(R.string.default_shoppinglist_title) + mTableNum;
                        }
                        addNewButton(MainActivity.this, text, mTableNum);
                        mMyCreateDBTable.createTable(mTableNum, TABLE_NAME + mTableNum, text);
                        mTableNum++;
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void reNameTable(View v) {
        final View view = v;
        final EditText input = new EditText(v.getContext());
        new AlertDialog.Builder(v.getContext())
                .setTitle(getString(R.string.rename_shopping_list))
                .setMessage(getString(R.string.edit_shoppinglist_title))
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
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
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void addNewButton(Context context, String text, int id) {
        if(mIsFirstAddButton) {
            mLinearLayout.addView(new SlideDeleteView(this));
            mIsFirstAddButton = false;
        }
        Button btn = new Button(context);
        btn.setText(text);
        btn.setId(id);
        btn.setOnClickListener(enterListListener);
        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        historicX = event.getX();
                        historicY = event.getY();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        if (event.getX() - historicX > DELTA) {
                            mLinearLayout.removeView(v);
                            //deleate database table
                            mMyCreateDBTable.deleteTable(TABLE_NAME + v.getId(), v.getId());
                            hideSlideDeleteViewIfClear();
                        }
                        mIsDeleteItemBySlide = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getX() - historicX > 15) {
                            mIsDeleteItemBySlide = true;
                        }
                        break;
                }
                return false;
            }
        });
        registerForContextMenu(btn);
        mLinearLayout.addView(btn);
    }

    private void copyTable(View v){
        final View view = v;
        final EditText input = new EditText(v.getContext());
        new AlertDialog.Builder(v.getContext())
                .setTitle(getString(R.string.copy_shopping_list))
                .setMessage(getString(R.string.edit_shoppinglist_title))
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //create new database table
                        String text = input.getText().toString();
                        if (text.trim().equals("")) {
                            text = getString(R.string.default_shoppinglist_title) + mTableNum;
                        }
                        addNewButton(MainActivity.this, text, mTableNum);
                        mMyCreateDBTable.createTable(mTableNum, TABLE_NAME + mTableNum, text);

                        //copy this table's item to database
                        mMyCreateDBTable.openTable(TABLE_NAME + view.getId());
                        Cursor cursor = mMyCreateDBTable.getData();
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            do {
                                mMyCreateDBTable.insertToTable(TABLE_NAME + mTableNum, cursor.getString(1), cursor.getInt(2), cursor.getString(3), cursor.getFloat(4), cursor.getString(5), cursor.getString(7), cursor.getString(8));
                            } while (cursor.moveToNext());
                        }
                        cursor.close();
                        mTableNum++;
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void mergeTable(){
        if(mHasChecked) {
            final EditText input = new EditText(this);
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.merge_shopping_list))
                    .setMessage(getString(R.string.edit_shoppinglist_title))
                    .setView(input)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //create new database table
                            String text = input.getText().toString();
                            if (text.trim().equals("")) {
                                text = getString(R.string.default_shoppinglist_title) + mTableNum;
                            }
                            addNewButton(MainActivity.this, text, mTableNum);
                            mMyCreateDBTable.createTable(mTableNum, TABLE_NAME + mTableNum, text);

                            //copy  table's item to database from mCheckedList
                            Cursor cursor = mMyCreateDBTable.getTableList();
                            int i = 0;
                            if (cursor.getCount() > 0) {
                                cursor.moveToFirst();
                                do {
                                    if (i < mCheckedList.length && mCheckedList[i] == true) {
                                        mMyCreateDBTable.openTable(TABLE_NAME + cursor.getInt(1));
                                        Cursor cursorInList = mMyCreateDBTable.getData();
                                        if (cursorInList.getCount() > 0) {
                                            cursorInList.moveToFirst();
                                            do {
                                                mMyCreateDBTable.insertToTable(TABLE_NAME + mTableNum, cursorInList.getString(1), cursorInList.getInt(2), cursorInList.getString(3), cursorInList.getFloat(4), cursorInList.getString(5), cursor.getString(3) + "\n" + cursorInList.getString(7), cursorInList.getString(8));
                                            } while (cursorInList.moveToNext());
                                        }
                                        cursorInList.close();
                                    }
                                    i++;
                                } while (cursor.moveToNext());
                            }
                            cursor.close();
                            mTableNum++;
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    }

    private void deleteTable(){
        mLinearLayout.removeAllViews();
        Cursor cursor = mMyCreateDBTable.getTableList();
        int i = 0;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                if (i < mCheckedList.length && mCheckedList[i] == true) {
                    //deleate database table
                    mMyCreateDBTable.deleteTable(TABLE_NAME + cursor.getInt(1), cursor.getInt(1));
                }
                i++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        initViews();
    }

    private void hideSlideDeleteViewIfClear(){
        Cursor cursor = mMyCreateDBTable.getTableList();
        if (cursor.getCount() == 0) {
            mLinearLayout.removeAllViews();
            mIsFirstAddButton = true;
        }
        cursor.close();
    }

}
