package com.example.chilin_wang.shoppinglist;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ShoppingListActivity extends AppCompatActivity {

    private static final int MENU_STATS_ADD = Menu.FIRST + 1;
    private static final int MENU_STATS_SORT = Menu.FIRST + 2;
    private final static int CAMERA = 66;
    private final static int PHOTO = 99;
    private View mInputView;
    private EditText mItemName;
    private EditText mItemNum;
    private EditText mItemUnit;
    private EditText mItemPrice;
    private EditText mCurrency;
    private EditText mShopName;
    private ImageView mCalculator, mPhotoImage;
    private String mPhotoUri;
    private DisplayMetrics mPhone;
    private MyCreateDBTable mMyCreateDBTable;
    private int mTableId;
    private LinearLayout mLinearLayout;
    private boolean mIsEnterCamera = false;
    private View.OnClickListener choosePhotoImage = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            choosePhotoImageClick();
        }
    };
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
        mPhone = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mPhone);
        loadValue(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem addTable = menu.add(0, MENU_STATS_ADD, 0, "Add").setIcon(R.drawable.ic_add_black_24dp);
        addTable.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, MENU_STATS_SORT, 0, "Sort by item");
        menu.add(0, MENU_STATS_SORT+1, 0, "Sort by price");
        menu.add(0, MENU_STATS_SORT + 2, 0, "Sort by average price");
        menu.add(0, MENU_STATS_SORT + 3, 0, "Sort by note");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_STATS_ADD:
                addNewItem();
                break;
            case MENU_STATS_SORT:
                orderItem(0);
                break;
            case MENU_STATS_SORT+1:
                orderItem(1);
                break;
            case MENU_STATS_SORT+2:
                orderItem(2);
                break;
            case MENU_STATS_SORT+3:
                orderItem(3);
                break;
            case android.R.id.home:
                super.onBackPressed();
                return true;
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

    @Override
    protected void onResume(){
        Log.d(MainActivity.TAG, "onResume");
        if(mIsEnterCamera){
            choosePhotoImageClick();
            mIsEnterCamera = false;
        }
        super.onResume();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO && data != null) {
            Uri uri = data.getData();
            mPhotoUri = String.valueOf(uri);
            Log.d(MainActivity.TAG, "uri = " + uri + "\n mPhotoUri =" + mPhotoUri);
            showPhoto();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showPhoto() {
        Uri uri = Uri.parse(mPhotoUri);
        ContentResolver cr = this.getContentResolver();
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
            if(bitmap.getWidth() > mPhone.widthPixels){
                float mScale = (float) mPhone.widthPixels / bitmap.getWidth();
                Matrix matrix = new Matrix();
                matrix.postScale(mScale,mScale);
                Bitmap mScaleBitmap = Bitmap.createBitmap(bitmap,
                        0,
                        0,
                        bitmap.getWidth(),
                        bitmap.getHeight(),
                        matrix,
                        true);
                mPhotoImage.setImageBitmap(mScaleBitmap);
            } else {
                mPhotoImage.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                            + "," + cursor.getString(5) + "," + cursor.getString(6) + "," + cursor.getString(7));
                    addNewButton(this, cursor, cursor.getInt(0));
                } while (cursor.moveToNext());
            } else {
                cursor.moveToLast();
                addNewButton(this, cursor, cursor.getInt(0));
            }
        }
        cursor.close();
    }

    private void addNewButton(Context context, Cursor cursor, int id) {
        ShoppingListView btn = new ShoppingListView(context);
        btn.setShoppingList(cursor);
        btn.setId(id);
        btn.setOnClickListener(modifyItemListener);
        registerForContextMenu(btn);
        mLinearLayout.addView(btn);
    }

    private void addNewItem() {
        mPhotoUri = "";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        initDialogLayout();
        builder.setTitle("Add new item")
                .setView(mInputView)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        boolean isFinishDialog = true;
                        if (mItemName.getText().toString().trim().equals("")) {
                            if (mItemName.getText().toString().trim().equals("")) {
                                mItemName.setHint("Please enter item name");
                            }
                            isFinishDialog = false;
                        } else {
                            //insert data to database
                            insertData();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        initDialogLayout();
        loadDialogLayoutValues(view);
        builder.setTitle("Modify item")
                .setView(mInputView)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        boolean isFinishDialog = true;
                        if (mItemName.getText().toString().trim().equals("")) {
                            if (mItemName.getText().toString().trim().equals("")) {
                                mItemName.setHint("Please enter item name");
                            }
                            isFinishDialog = false;
                        } else {
                            //update data to database
                            mMyCreateDBTable.update(view.getId(), mItemName.getText().toString(), Integer.parseInt(mItemNum.getText().toString()),
                                    mItemUnit.getText().toString(), Float.parseFloat(mItemPrice.getText().toString()),
                                    mCurrency.getText().toString(), mShopName.getText().toString(), mPhotoUri);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        initDialogLayout();
        loadDialogLayoutValues(view);
        builder.setTitle("Copy item")
                .setView(mInputView)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        boolean isFinishDialog = true;
                        if (mItemName.getText().toString().trim().equals("")) {
                            if (mItemName.getText().toString().trim().equals("")) {
                                mItemName.setHint("Please enter item name");
                            }
                            isFinishDialog = false;
                        } else {
                            //insert data to database
                            insertData();
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

    private void initDialogLayout() {
        mInputView = getLayoutInflater().inflate(R.layout.shopping_list_dialog, null);
        mItemName = (EditText) mInputView.findViewById(R.id.item_name);
        mItemNum = (EditText) mInputView.findViewById(R.id.item_num);
        mItemUnit = (EditText) mInputView.findViewById(R.id.item_unit);
        mItemPrice = (EditText) mInputView.findViewById(R.id.item_price);
        mCurrency = (EditText) mInputView.findViewById(R.id.currency);
        mShopName = (EditText) mInputView.findViewById(R.id.shop_name);
        mCalculator = (ImageView) mInputView.findViewById(R.id.calculator_image);
        mPhotoImage = (ImageView) mInputView.findViewById(R.id.photo_image);
        ImageView launchCamera = (ImageView) mInputView.findViewById(R.id.launch_camera);
        ImageView choosePhoto = (ImageView) mInputView.findViewById(R.id.choose_photo);
        mItemNum.setText("1");
        mItemPrice.setText("0");
        mItemName.setSelectAllOnFocus(true);
        mItemNum.setSelectAllOnFocus(true);
        mItemUnit.setSelectAllOnFocus(true);
        mCurrency.setSelectAllOnFocus(true);
        mItemPrice.setSelectAllOnFocus(true);
        mShopName.setSelectAllOnFocus(true);
        mPhotoImage.setOnClickListener(choosePhotoImage);
        mPhotoImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mPhotoUri = "";
                mPhotoImage.setImageDrawable(null);
                return false;
            }
        });
        choosePhoto.setOnClickListener(choosePhotoImage);
        launchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsEnterCamera = true;
                Intent intent_camera = new Intent(android.provider.MediaStore.INTENT_ACTION_VIDEO_CAMERA);
                startActivity(intent_camera);
            }
        });

        mCalculator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
                final PackageManager pm = getPackageManager();
                List<PackageInfo> packs = pm.getInstalledPackages(0);
                for (PackageInfo pi : packs) {
                    if (pi.packageName.toString().toLowerCase().contains("calcul")) {
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        map.put("appName", pi.applicationInfo.loadLabel(pm));
                        Object packageName = map.put("packageName", pi.packageName);
                        items.add(map);
                    }
                }
                if (items.size() >= 1) {
                    String packageName = (String) items.get(0).get("packageName");
                    Intent i = pm.getLaunchIntentForPackage(packageName);
                    if (i != null)
                        startActivity(i);
                } else {
                    // Application not found
                }
            }
        });
    }

    private static File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraDemo");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");
    }

    private void loadDialogLayoutValues(View v) {
        Cursor cursor = mMyCreateDBTable.query(v.getId());
        if (cursor.moveToFirst()) {
            mItemName.setText(cursor.getString(1));
            mItemNum.setText(String.valueOf(cursor.getInt(2)));
            mItemUnit.setText(cursor.getString(3));
            mItemPrice.setText(String.valueOf(cursor.getFloat(4)));
            mCurrency.setText(cursor.getString(5));
            mShopName.setText(cursor.getString(7));
            mPhotoUri = cursor.getString(8);
            showPhoto();
        }
    }

    private void choosePhotoImageClick(){
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(intent, PHOTO);
    }

    private void insertData() {
        int num = Integer.parseInt(mItemNum.getText().toString());
        float price = Float.parseFloat(mItemPrice.getText().toString());
        mMyCreateDBTable.insertToTable(MainActivity.TABLE_NAME + mTableId, mItemName.getText().toString(), num,
                mItemUnit.getText().toString(), price, mCurrency.getText().toString(),
                mShopName.getText().toString(), mPhotoUri);
    }

    private void orderItem(int order){
        Cursor cursor = mMyCreateDBTable.orderItem(order);
        if (cursor.getCount() > 0 && cursor != null) {
            mLinearLayout.removeAllViews();
            cursor.moveToFirst();
            do {
                addNewButton(this, cursor, cursor.getInt(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
}
