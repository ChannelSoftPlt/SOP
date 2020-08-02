package com.channelsoft.sop.sharePreference;

import android.content.Context;
import android.content.SharedPreferences;

import com.channelsoft.sop.object.UserObject;
import com.google.gson.Gson;

/**
 * Created by wypan on 2/24/2017.
 */

public class SharedPreferenceManager {

    private static String User = "user";

    private static SharedPreferences getSharedPreferences(Context context) {
        String SharedPreferenceFileName = "VegeApp";
        return context.getSharedPreferences(SharedPreferenceFileName, Context.MODE_PRIVATE);
    }

    public static void clear(Context context) {
        getSharedPreferences(context).edit().clear().apply();
    }

    public static String getUser(Context context) {
        return getSharedPreferences(context).getString(User, "default");
    }

    public static void setUser(Context context, UserObject object) {
        getSharedPreferences(context).edit().putString(User, new Gson().toJson(object)).apply();
    }


    public static String getUserId(Context context) {
        return new Gson().fromJson(SharedPreferenceManager.getUser(context), UserObject.class).getUserId();
    }

    public static String getUserEmail(Context context) {
        return new Gson().fromJson(SharedPreferenceManager.getUser(context), UserObject.class).getEmail();
    }

    public static String getUsername(Context context) {
        return new Gson().fromJson(SharedPreferenceManager.getUser(context), UserObject.class).getUsername();
    }
}
