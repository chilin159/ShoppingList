package com.example.chilin_wang.shoppinglist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.lang.reflect.Field;

public class ShoppingListActivity extends AppCompatActivity {

    private static final int MENU_STATS_ADD = Menu.FIRST + 1;
    private MyCreateDBTable mMyCreateDBTable;
    private int mTableId;
    private LinearLayout mLinearLayout;
    private int mItemNum = 1;
    private View.OnClickListener modifyItemListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            modifyItem(v);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        mTableId = bundle.getInt(MainActivity.TABLE_ID);
        String tableName = bundle.getString(MainActivity.TABLE_NAME_BY_USER);
        getSupportActionBar().setTitle(tableName);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_shopping_list);
        mMyCreateDBTable = new MyCreateDBTable(getApplicationContext());
        mMyCreateDBTable.openTable(MainActivity.TABLE_NAME + mTableId);
        mLinearLayout = (LinearLayout) findViewById(R.id.viewObj);
        loadValue(true);
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
                addNewItem();
                break;
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final View view = v;
        MenuItem copy = menu.add(0, 0, 0, "Copy");
        MenuItem delete = menu.add(0, 1, 0, "Delete");
        copy.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                copyItem(view);
                return true;
            }
        });
        delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mLinearLayout.removeView(view);
                //deleate database item
                mMyCreateDBTable.delete(view.getId());
                return true;
            }
        });
    }

    private void loadValue(boolean isInit) {
        //init list
        Cursor cursor = mMyCreateDBTable.getData();
        if (cursor.getCount() > 0) {
            if (isInit) {
                cursor.moveToFirst();
                do {
                    Log.d(MainActivity.TAG, cursor.getLong(0) + "," + cursor.getString(1) + ","
                            + cursor.getString(2) + "," + cursor.getString(3) + "," + cursor.getString(4)
                            + "," + cursor.getString(5) + "," + cursor.getString(6));
                    String text = cursor.getLong(0) + "," + cursor.getString(1) + ","
                            + cursor.getString(2) + "," + cursor.getString(3) + "," + cursor.getString(4)
                            + "," + cursor.getString(5) + "," + cursor.getString(6);
                    mItemNum = cursor.getInt(0);
                    addNewButton(this, text, cursor.getInt(0));
                } while (cursor.moveToNext());
            } else {
                cursor.moveToLast();
                String text = cursor.getLong(0) + "," + cursor.getString(1) + ","
                        + cursor.getString(2) + "," + cursor.getString(3) + "," + cursor.getString(4)
                        + "," + cursor.getString(5) + "," + cursor.getString(6);
                addNewButton(this, text, cursor.getInt(0));
            }
        }
        cursor.close();
    }

    private void addNewButton(Context context, String text, int id) {
        Button btn = new Button(context);
        btn.setText(text);
        btn.setId(id);
        btn.setOnClickListener(modifyItemListener);
        registerForContextMenu(btn);
        mLinearLayout.addView(btn);
    }

    private void addNewItem() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View inputView = inflater.inflate(R.layout.shopping_list_dialog, null);
        final EditText itemName = (EditText) inputView.findViewById(R.id.item_name);
        final EditText itemNum = (EditText) inputView.findViewById(R.id.item_num);
        final EditText itemUnit = (EditText) inputView.findViewById(R.id.item_unit);
        final EditText itemPrice = (EditText) inputView.findViewById(R.id.item_price);
        final EditText shopName = (EditText) inputView.findViewById(R.id.shop_name);
        builder.setTitle("Add new item")
                .setView(inputView)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        boolean isFinishDialog = true;
                        if (itemName.getText().toString().trim().equals("")) {
                            if (itemName.getText().toString().trim().equals("")) {
                                itemName.setHint("Please enter item name");
                            }
                            isFinishDialog = false;
                        } else {
                            //insert data to database
                            int num = 0;
                            int price = 0;
                            if (!itemNum.getText().toString().equals("")) {
                                num = Integer.parseInt(itemNum.getText().toString());
                            }
                            if (!itemPrice.getText().toString().equals("")) {
                                price = Integer.parseInt(itemPrice.getText().toString());
                            }
                            mMyCreateDBTable.insert(itemName.getText().toString(), num,
                                    itemUnit.getText().toString(), price,
                                    shopName.getText().toString());
                            loadValue(false);
                        }
                        if (!isFinishDialog) {
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .show();
    }

    private void modifyItem(View v) {
        final View view = v;
        Cursor cursor = mMyCreateDBTable.query(v.getId());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View inputView = inflater.inflate(R.layout.shopping_list_dialog, null);
        final EditText itemName = (EditText) inputView.findViewById(R.id.item_name);
        final EditText itemNum = (EditText) inputView.findViewById(R.id.item_num);
        final EditText itemUnit = (EditText) inputView.findViewById(R.id.item_unit);
        final EditText itemPrice = (EditText) inputView.findViewById(R.id.item_price);
        final EditText shopName = (EditText) inputView.findViewById(R.id.shop_name);
        if (cursor.moveToFirst()) {
            itemName.setText(cursor.getString(1));
            itemNum.setText(String.valueOf(cursor.getInt(2)));
            itemUnit.setText(cursor.getString(3));
            itemPrice.setText(String.valueOf(cursor.getInt(4)));
            shopName.setText(cursor.getString(6));
        }
        builder.setTitle("Modify item")
                .setView(inputView)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        boolean isFinishDialog = true;
                        if (itemName.getText().toString().trim().equals("")) {
                            if (itemName.getText().toString().trim().equals("")) {
                                itemName.setHint("Please enter item name");
                            }
                            isFinishDialog = false;
                        } else {
                            //update data to database
                            mMyCreateDBTable.update(view.getId(), itemName.getText().toString(), Integer.parseInt(itemNum.getText().toString()),
                                    itemUnit.getText().toString(), Integer.parseInt(itemPrice.getText().toString()),
                                    shopName.getText().toString());
                            mLinearLayout.removeAllViews();
                            loadValue(true);
                        }
                        if (!isFinishDialog) {
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .show();
    }

    private void copyItem(View v) {
        final View view = v;
        Cursor cursor = mMyCreateDBTable.query(v.getId());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View inputView = inflater.inflate(R.layout.shopping_list_dialog, null);
        final EditText itemName = (EditText) inputView.findViewById(R.id.item_name);
        final EditText itemNum = (EditText) inputView.findViewById(R.id.item_num);
        final EditText itemUnit = (EditText) inputView.findViewById(R.id.item_unit);
        final EditText itemPrice = (EditText) inputView.findViewById(R.id.item_price);
        final EditText shopName = (EditText) inputView.findViewById(R.id.shop_name);
        if (cursor.moveToFirst()) {
            itemName.setText(cursor.getString(1));
            itemNum.setText(String.valueOf(cursor.getInt(2)));
            itemUnit.setText(cursor.getString(3));
            itemPrice.setText(String.valueOf(cursor.getInt(4)));
            shopName.setText(cursor.getString(6));
        }
        builder.setTitle("Modify item")
                .setView(inputView)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        boolean isFinishDialog = true;
                        if (itemName.getText().toString().trim().equals("")) {
                            if (itemName.getText().toString().trim().equals("")) {
                                itemName.setHint("Please enter item name");
                            }
                            isFinishDialog = false;
                        } else {
                            //insert data to database
                            int num = 0;
                            int price = 0;
                            if (!itemNum.getText().toString().equals("")) {
                                num = Integer.parseInt(itemNum.getText().toString());
                            }
                            if (!itemPrice.getText().toString().equals("")) {
                                price = Integer.parseInt(itemPrice.getText().toString());
                            }
                            mMyCreateDBTable.insert(itemName.getText().toString(), num,
                                    itemUnit.getText().toString(), price,
                                    shopName.getText().toString());
                            loadValue(false);
                        }
                        if (!isFinishDialog) {
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .show();
    }
}
