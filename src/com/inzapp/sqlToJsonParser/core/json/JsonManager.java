package com.inzapp.sqlToJsonParser.core.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class JsonManager {
    protected JSONObject json = new JSONObject();

    /**
     * add json to value if exist key, else make new list and add value
     * @param key
     * com.inzapp.sqlToJsonParser.config.JsonKey
     * @param value
     * java.util.list.toString();
     */
    protected void putToJson(String key, String value) {
        List<String> list = getConvertedJsonArray(key);
        if (list == null)
            list = new ArrayList<>();

        list.add(value);
        try {
            this.json.put(key, list);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }


    /**
     * only used for sub query and sub query analyse
     * add index to json key
     * if exist json key "SUB QUERY 1", make new key with next index("SUB QUERY 2")
     * @param key
     * com.inzapp.sqlToJsonParser.config.JsonKey
     * @param idx
     * fixed value with 1, only used in recursion
     * @param value
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
     * @param key
     * com.inzapp.sqlToJsonParser.config.JsonKey
     * @param json
     * analysed sub query json object
     */
    private void putToJson(String key, JSONObject json) {
        try {
            this.json.put(key, json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * only used for sub query and sub query analyse
     * add index to json key
     * if exist json key "SUB QUERY 1", make new key with next index("SUB QUERY 2")
     * @param key
     * com.inzapp.sqlToJsonParser.config.JsonKey
     * @param idx
     * fixed value with 1, only used in recursion
     * @param json
     * analysed sub query json object
     */
    protected void putToJson(String key, int idx, JSONObject json) {
        try {
            this.json.getJSONObject(key + idx); // only used for exception check
            putToJson(key, (idx + 1), json);
        } catch (Exception e) {
            putToJson(key + idx, json);
        }
    }

    /**
     * json array to java list converter
     * @param key
     * com.inzapp.sqlToJsonParser.config.JsonKey
     * @return
     * org.json.JSONArray -> java.util.list
     * return null if failed to convert or json key does not exist
     */
    private List<String> getConvertedJsonArray(String key) {
        try {
            JSONArray jsonArray = this.json.getJSONArray(key);
            List<String> list = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); ++i)
                list.add((String) jsonArray.get(i));
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * sort json by key
     * not supported yet
     */
    protected void sortJsonByKey() {
        try {
            Iterator keys = json.keys();
            Map<String, Object> treeMap = new TreeMap<>(String::compareTo);
            while (keys.hasNext()) {
                String key = (String) keys.next();
                treeMap.put(key, json.get(key));
            }
            this.json = new JSONObject(treeMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
