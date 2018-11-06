package com.apihelper.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class ApiUtils {
    public static JSONObject mapToJsonArray(Map<String, List<Map<String, String>>> map) {
        JSONObject jsonObject = new JSONObject();

        for (Map.Entry<String, List<Map<String, String>>> entry : map.entrySet()) {
            JSONArray jsonArray = new JSONArray();
            for (Map<String, String> m : entry.getValue()) {
                jsonArray.put(mapToJsonObject(m));
            }
            try {
                jsonObject.put(entry.getKey(), jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jsonObject;
    }

    public static JSONObject mapToJsonObject(Map<String, String> map) {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            try {
                jsonObject.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    public static JSONObject mapLogin(Map<String, String> map) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", map.get("type"));
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("email", map.get("email"));
            jsonObject1.put("password", map.get("password"));
            jsonObject.put("payload", jsonObject1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject toJsonObject(String key, String value) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static String getQuery(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> pair : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public static class JsonBuilder {
        private JSONObject jsonObject = new JSONObject();

        public JsonBuilder put(String key, String value) {
            try {
                jsonObject.put(key, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }

        public JSONObject build() {
            return jsonObject;
        }
    }
}