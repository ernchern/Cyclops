package com.example.ahjayo.cyclops;

import android.app.Application;

import org.json.JSONArray;

import java.util.List;

public class UserInfo extends Application {
    private String member_id;
    private String name;
    private JSONArray keys;
    private String address;
    private static UserInfo instance;

    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static UserInfo get_user_info() {
        return instance;
    }

    public String get_name() {
        return name;
    }

    public void set_name(String _name) {
        name = _name;
    }

    public String get_member_id() {
        return member_id;
    }

    public void set_member_id(String _member_id) {
        member_id = _member_id;
    }

    public JSONArray get_keys() {
        return keys;
    }

    public void set_key(JSONArray _keys) {
        keys = _keys;
    }

    public String get_address() {
        return address;
    }

    public void set_address(String _address) {
        address = _address;
    }
}
