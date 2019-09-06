package com.inzapp.sqlToJsonParser.core.json;

import com.inzapp.sqlToJsonParser.config.SplitKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class JsonManager {
    /**
     * for saving parsed sql
     */
    protected JSONObject json = new JSONObject();
    protected Stack<String> stack = new Stack<>();

    /**
     * add json to value if exist key, else make new list and add value
     *
     * @param key   com.inzapp.sqlToJsonParser.config.JsonKey
     * @param value java.util.list.toString();
     */
    protected void putToJson(String key, String value) {
        List<String> list = getConvertedJsonArray(key);
        if (list == null)
            list = new ArrayList<>();

        stack.push(String.format("%s%s%s", key, SplitKey.SPLIT_KEY, value));
        list.add(value);
        try {
            json.put(key, list);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * only used for sub query and sub query analyse
     * add index to json key
     * if exist json key "SUB QUERY 1", make new key with next index("SUB QUERY 2")
     *
     * @param key   com.inzapp.sqlToJsonParser.config.JsonKey
     * @param idx   fixed value with 1, only used in recursion
     * @param value java.util.List<>().toString()
     *              list value as string
     */
    protected void putToJson(String key, int idx, String value) {
        List<String> list = getConvertedJsonArray(key + idx);
        if (list != null)
            putToJson(key + (idx + 1), value);
        else
            putToJson(key + idx, value);
    }

    /**
     * add json object as json value : used for recursive sub query analyse
     *
     * @param key  com.inzapp.sqlToJsonParser.config.JsonKey
     * @param json analysed sub query json object
     */
    private void putToJson(String key, JSONObject json) {
        try {
            json.put(key, json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * only used for sub query and sub query analyse
     * add index to json key
     * if exist json key "SUB QUERY 1", make new key with next index("SUB QUERY 2")
     *
     * @param key  com.inzapp.sqlToJsonParser.config.JsonKey
     * @param idx  fixed value with 1, only used in recursion
     * @param json analysed sub query json object
     */
    protected void putToJson(String key, int idx, JSONObject json) {
        try {
            json.getJSONObject(key + idx); // only used for exception check
            putToJson(key, (idx + 1), json);
        } catch (Exception e) {
            putToJson(key + idx, json);
        }
    }

    /**
     * json array to java list converter
     *
     * @param key com.inzapp.sqlToJsonParser.config.JsonKey
     * @return org.json.JSONArray -> java.util.list
     * return null if failed to convert or json key does not exist
     */
    private List<String> getConvertedJsonArray(String key) {
        try {
            JSONArray jsonArray = json.getJSONArray(key);
            List<String> list = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); ++i)
                list.add((String) jsonArray.get(i));
            return list;
        } catch (Exception e) {
            return null;
        }
    }
}
