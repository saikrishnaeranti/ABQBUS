package edu.unm.albuquerquebus.albuquerquebus.utils;


import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

/**
 * Copyright (C) PickQuick, Inc - All Rights Reserve
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Created by pruthviraj on 01/04/16.
 * Last Updated by pruthviraj on 01/04/16.
 * Copyright
 */
public class ApiCaller {
    private String LOG_TAG = ApiCaller.class.getSimpleName();

    private String mUrl;
    private Context mContext;
    private Map<String, String> mStringRequestUrlParams;

    private AfterApiCallResponse mAfterApiCallResponse;

    public interface AfterApiCallResponse {
        void successResponse(String response, String url);

        void errorResponse(VolleyError error, String url);
    }

    public ApiCaller() {
    }

    public void reset() {
        this.mUrl = null;
    }

    public String urlConstructionForGetMethod(String url, Map<String, String> params) {

        Uri.Builder builder = Uri.parse(url).buildUpon();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        return builder.build().toString();

    }

    public String urlConstructionForGetMethod(String url, JSONObject params) {

        Uri.Builder builder = Uri.parse(url).buildUpon();
        Iterator<?> keys = params.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            try {
                if (!(params.get(key) instanceof JSONObject)
                        && !(params.get(key) instanceof JSONArray)) {
                    builder.appendQueryParameter(key, params.getString(key));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return builder.build().toString();

    }

    public void setAfterApiCallResponse(AfterApiCallResponse afterApiCallResponse) {
        mAfterApiCallResponse = afterApiCallResponse;
    }

    public void makeStringRequest(Context context, int method,
                                  final String url, Map<String, String> parameters) {
        mContext = context;
        if (method == Request.Method.GET && parameters != null) {
            mUrl = urlConstructionForGetMethod(url, parameters);
            mStringRequestUrlParams = null;
        } else {
            mUrl = url;
            mStringRequestUrlParams = parameters;
        }
        StringRequest request = new StringRequest(method, mUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (mAfterApiCallResponse != null) {
                                mAfterApiCallResponse.successResponse(response, url);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (volleyError != null && volleyError.networkResponse != null) {
                            VolleyError error = null;
                            try {
                                error = new VolleyError(new String(volleyError.networkResponse.data));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                JSONObject jsonObject = null;
                                if (error != null) {
                                    jsonObject = new JSONObject(error.getMessage());
                                }
                                String toastError = jsonObject != null ? jsonObject.getString(Constants.ERROR) : null;
                                Toast.makeText(mContext, toastError, Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (mAfterApiCallResponse != null) {
                                mAfterApiCallResponse.errorResponse(error, url);
                            }
                        } else {
                            Toast.makeText(mContext, Constants.TOAST_CHECK_CONNECTIVITY, Toast.LENGTH_SHORT).show();
                        }
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return mStringRequestUrlParams;
            }
        };
        Volley.newRequestQueue(mContext).add(request);
    }

    public void makeJsonObjectRequest(Context context, int method, final String url, JSONObject parameters) {
        mContext = context;
        if (method == Request.Method.GET && parameters != null) {
            mUrl = urlConstructionForGetMethod(url, parameters);
            parameters = null;
        } else {
            mUrl = url;
        }

        JsonObjectRequest request = new JsonObjectRequest(method, mUrl, parameters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (mAfterApiCallResponse != null) {
                                mAfterApiCallResponse.successResponse(response.toString(), url);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (volleyError != null && volleyError.networkResponse != null) {
                            VolleyError error = null;
                            try {
                                error = new VolleyError(new String(volleyError.networkResponse.data));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                JSONObject jsonObject = null;
                                if (error != null) {
                                    jsonObject = new JSONObject(error.getMessage());
                                }
                                String toastError = jsonObject != null ? jsonObject.getString(Constants.ERROR) : null;
                                Toast.makeText(mContext, toastError, Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (mAfterApiCallResponse != null) {
                                mAfterApiCallResponse.errorResponse(error, url);
                            }
                        } else {
                            Toast.makeText(mContext, Constants.TOAST_CHECK_CONNECTIVITY, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        Volley.newRequestQueue(mContext).add(request);
    }

}