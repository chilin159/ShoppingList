package com.example.chilin_wang.shoppinglist;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

/**
 * Created by Chilin_Wang on 2016/8/18.
 */
public class SlideDeleteView extends LinearLayout{
    public SlideDeleteView(Context context){
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.slide_delete_view, this);
    }
}
