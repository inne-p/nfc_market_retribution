package com.mateuyabar.android.pillownfc.util;

import android.util.Log;

/**
 * Created by root on 19/07/16.
 */
public class SharedProperty {
    public static String id="";

    public SharedProperty(){
    }

    public void setId(String id){
        this.id = id;
        Log.e("NEW ID", this.id);
    }
    public String getId(){
        return id;
    }
    public Boolean isIdEqual(String id){
        return this.id==id;
    }
}
