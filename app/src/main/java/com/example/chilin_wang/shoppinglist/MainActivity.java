package com.example.chilin_wang.shoppinglist;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Toast;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final String TAG="test";
    private static final String TABLE_NAME="table";
    private Button mAddTableButton;
    private LinearLayout mLinearLayout;
    private int mTableNum;
    private MyCreateDBTable mMyCreateDBTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLinearLayout=(LinearLayout)findViewById(R.id.viewObj);
        mMyCreateDBTable = new MyCreateDBTable(getApplicationContext());
        initViews();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mMyCreateDBTable.close();
    }

    private void initViews(){
        mAddTableButton = new Button(this);
        mAddTableButton.setText("Add");
        mAddTableButton.setOnClickListener(addTableListener);
        mLinearLayout.addView(mAddTableButton);
        //init table list
        Cursor cursor = mMyCreateDBTable.getTableList();
        if(cursor.getCount()>0) {
            cursor.moveToFirst();
            do {
                addNewButton(this, cursor.getString(2), cursor.getInt(0));
                mTableNum = cursor.getInt(0)+1;
            }while(cursor.moveToNext());
        } else {
            mTableNum = 1;
        }
        cursor.close();
    }

    private void addNewButton(Context context,String text,int id){
        Button btn = new Button(context);
        btn.setText(text);
        btn.setId(id);
        btn.setOnClickListener(enterListListener);
        registerForContextMenu(btn);
        mLinearLayout.addView(btn);
    }


    private OnClickListener addTableListener = new OnClickListener(){
        public void onClick(View v){
            final View view = v;
            final EditText input = new EditText(v.getContext());
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Add new shopping list")
                    .setMessage("Edit shopping title")
                    .setView(input)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            addNewButton(view.getContext(), input.getText().toString(), mTableNum);
                            //create new database table
                            mMyCreateDBTable.createTable(TABLE_NAME+mTableNum,input.getText().toString());

                            mTableNum++;
                        }
                    })
                    .show();
        }
    };

    private OnClickListener enterListListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "button" + v.getId());
            mMyCreateDBTable.openTable(TABLE_NAME + v.getId());
            getFragmentManager().beginTransaction().replace(R.id.viewObj, new ShoppingListFragment()).addToBackStack(null).commit();
            /*
            mMyCreateDBTable.insert("item" + v.getId(), 0, "ml", 100, null);
            Cursor cursor = mMyCreateDBTable.getData();
            if(cursor.getCount()>0) {
                cursor.moveToFirst();
                do {
                    Log.d(TAG, cursor.getLong(0) + "," + cursor.getString(1) + "," + cursor.getString(2));
                }while(cursor.moveToNext());
            }
            cursor.close();*/
        }
    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        final View view = v;
        MenuItem delete = menu.add(0,0,0,"Delete");
        delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mLinearLayout.removeView(view);
                //deleate database table
                mMyCreateDBTable.deleteTable(TABLE_NAME + view.getId(),view.getId());
                return true;
            }
        });
    }
}
