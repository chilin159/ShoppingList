package com.example.chilin_wang.shoppinglist;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ShoppingListActivity extends AppCompatActivity {

    private static final int MENU_STATS_ADD = Menu.FIRST + 1;
    private static final int MENU_STATS_SORT = Menu.FIRST + 2;
    private final static int PHOTO = 99;
    private final static int REQUEST_EXTERNAL_STORAGE = 0;
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
    private boolean mIsFirstRequestPermission = false;
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
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                loadValue(true);
            }
        });
        thread.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem addTable = menu.add(0, MENU_STATS_ADD, 0, R.string.menu_add).setIcon(R.drawable.ic_add_black_24dp);
        addTable.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, MENU_STATS_SORT, 0, R.string.menu_sort_by_item);
        menu.add(0, MENU_STATS_SORT + 1, 0, R.string.menu_sort_by_price);
        menu.add(0, MENU_STATS_SORT + 2, 0, R.string.menu_sort_by_average_price);
        menu.add(0, MENU_STATS_SORT + 3, 0, R.string.menu_sort_by_note);
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
            case MENU_STATS_SORT + 1:
                orderItem(1);
                break;
            case MENU_STATS_SORT + 2:
                orderItem(2);
                break;
            case MENU_STATS_SORT + 3:
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
        mLinearLayout.removeAllViews();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final View view = v;
        MenuItem copy = menu.add(0, 0, 0, R.string.menu_copy);
        MenuItem delete = menu.add(0, 1, 0, R.string.menu_delete);
        MenuItem share = menu.add(0,2,0,R.string.menu_share);
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
                new AlertDialog.Builder(ShoppingListActivity.this)
                        .setTitle(getString(R.string.delete_item))
                        .setMessage(getString(R.string.confirm_to_delete))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mLinearLayout.removeView(view);
                                //deleate database item
                                mMyCreateDBTable.delete(view.getId());
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;
            }
        });
        share.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Cursor cursor = mMyCreateDBTable.query(view.getId());
                if (cursor.moveToFirst()) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    String msgTitle = cursor.getString(1);
                    String msgText = msgTitle + "\n" + String.valueOf(cursor.getInt(2)) + cursor.getString(3) +
                            " " + String.valueOf(cursor.getFloat(4)) + cursor.getString(5) +
                            "\n" + String.valueOf(cursor.getFloat(6)) + cursor.getString(5) + "/" + cursor.getString(3) +
                            "\n" + cursor.getString(7);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, msgText);
                    startActivity(Intent.createChooser(intent, getString(R.string.menu_share)));
                }
                cursor.close();
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        Log.d(MainActivity.TAG, "onResume");
        if (mIsEnterCamera) {
            choosePhotoImageClick();
            mIsEnterCamera = false;
        }
        if(mIsFirstRequestPermission){
            launchCamera();
            mIsFirstRequestPermission = false;
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mIsFirstRequestPermission = true;
                } else {
                }
                return;
        }
    }

    private void showPhoto() {
        Uri uri = Uri.parse(mPhotoUri);
        ContentResolver cr = this.getContentResolver();
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
            if (bitmap.getWidth() > mPhone.widthPixels) {
                float mScale = (float) mPhone.widthPixels / bitmap.getWidth();
                Matrix matrix = new Matrix();
                matrix.postScale(mScale, mScale);
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
        final ShoppingListView sv = btn;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLinearLayout.addView(sv);
            }
        });
    }

    private void addNewItem() {
        mPhotoUri = "";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        initDialogLayout();
        builder.setTitle(getString(R.string.add_new_item))
                .setView(mInputView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        boolean isFinishDialog = true;
                        if (mItemName.getText().toString().trim().equals("")) {
                            if (mItemName.getText().toString().trim().equals("")) {
                                mItemName.setHint(getString(R.string.please_enter_item_name));
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
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
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
                            int num = 1;
                            float price = 0;
                            if (!mItemNum.getText().toString().equals("")) {
                                num = Integer.parseInt(mItemNum.getText().toString());
                            }
                            if (!mItemPrice.getText().toString().equals("")) {
                                price = Float.parseFloat(mItemPrice.getText().toString());
                            }
                            mMyCreateDBTable.update(view.getId(), mItemName.getText().toString(), num,
                                    mItemUnit.getText().toString(), price,
                                    mCurrency.getText().toString(), mShopName.getText().toString(), mPhotoUri);
                            mLinearLayout.removeView(view);
                            Cursor cursor = mMyCreateDBTable.query(view.getId());
                            if (cursor.moveToFirst()) {
                                addNewButton(ShoppingListActivity.this, cursor, view.getId());
                            }
                            cursor.close();
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
        builder.setTitle(getString(R.string.copy_item))
                .setView(mInputView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        boolean isFinishDialog = true;
                        if (mItemName.getText().toString().trim().equals("")) {
                            if (mItemName.getText().toString().trim().equals("")) {
                                mItemName.setHint(getString(R.string.please_enter_item_name));
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
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
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
                int permission = ActivityCompat.checkSelfPermission(ShoppingListActivity.this,
                        Manifest.permission.CAMERA);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ShoppingListActivity.this,
                            new String[]{CAMERA},
                            REQUEST_EXTERNAL_STORAGE
                    );
                } else {
                    launchCamera();
                }
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

    private void launchCamera() {
        mIsEnterCamera = true;
        Intent intent_camera = new Intent(android.provider.MediaStore.INTENT_ACTION_VIDEO_CAMERA);
        startActivity(intent_camera);
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
        cursor.close();
    }

    private void choosePhotoImageClick() {
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
        int num = 1;
        float price = 0;
        String unit = getString(R.string.default_unit);
        String currency = getString(R.string.default_currency);
        if (!mItemNum.getText().toString().equals("")) {
            num = Integer.parseInt(mItemNum.getText().toString());
        }
        if (!mItemPrice.getText().toString().equals("")) {
            price = Float.parseFloat(mItemPrice.getText().toString());
        }
        if(!mItemUnit.getText().toString().equals("")){
            unit = mItemUnit.getText().toString();
        }
        if(!mCurrency.getText().toString().equals("")){
            currency = mCurrency.getText().toString();
        }
        mMyCreateDBTable.insertToTable(MainActivity.TABLE_NAME + mTableId, mItemName.getText().toString(), num,
               unit, price, currency,
                mShopName.getText().toString(), mPhotoUri);
    }

    private void orderItem(int order) {
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
