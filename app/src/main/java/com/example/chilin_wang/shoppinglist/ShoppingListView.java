package com.example.chilin_wang.shoppinglist;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;

/**
 * Created by Chilin_Wang on 2016/8/5.
 */
public class ShoppingListView extends LinearLayout {

    private TextView mItemName,mItemNum,mItemUnit,mItemPrice,mAveragePrice,mShopName,mCurrency,mPriceUnit;
    private ImageView mPhotoImage;
    private Context mContext;

    public ShoppingListView(Context context){
        super(context);
        mContext =context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.shopping_list_view,this);
        mItemName = (TextView) findViewById(R.id.item_name);
        mItemNum = (TextView) findViewById(R.id.item_num);
        mItemUnit = (TextView) findViewById(R.id.item_unit);
        mItemPrice = (TextView) findViewById(R.id.item_price);
        mCurrency = (TextView) findViewById(R.id.currency);
        mAveragePrice = (TextView) findViewById(R.id.average_price);
        mShopName = (TextView) findViewById(R.id.shop_name);
        mPriceUnit = (TextView) findViewById(R.id.price_unit);
        mPhotoImage = (ImageView) findViewById(R.id.photo_image);
    }

    public void setShoppingList(Cursor cursor){
        mItemName.setText(cursor.getString(1));
        mItemNum.setText(String.valueOf(cursor.getInt(2)));
        if(!cursor.getString(3).equals("")) {
            mItemUnit.setText(cursor.getString(3));
        }
        mItemPrice.setText(String.valueOf(cursor.getFloat(4)));
        if(!cursor.getString(5).equals("")) {
            mCurrency.setText(cursor.getString(5));
        }
        mAveragePrice.setText(String.valueOf(cursor.getFloat(6)));
        mShopName.setText(cursor.getString(7));
        mPriceUnit.setText(mCurrency.getText()+"/"+mItemUnit.getText());

        Uri uri = Uri.parse(cursor.getString(8));
        ContentResolver cr = mContext.getContentResolver();
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
            BitmapDrawable bd = new BitmapDrawable(bitmap);
            mPhotoImage.setImageDrawable(bd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
