package io.github.parthav46.httphandler;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequestAsyncLoader extends AsyncTaskLoader<String> {

    public enum  Request {
        GET, POST
    }

    public String getRequestStr(Request request) {
        switch (request) {

            case POST:
                return "POST";
            case GET:
            default:
                return "GET";
        }
    }

    private String urlStr;
    private String data;
    private Request request;

    public HttpRequestAsyncLoader(Context context, @NonNull String urlStr, String data, Request request) {
        super(context);
        this.urlStr = urlStr;
        this.data = data;
        this.request = request;
    }

    @Override
    protected void onStartLoading() {
        super.onForceLoad();
    }

    @Nullable
    @Override
    public String loadInBackground() {
        URL url;
        String response = null;

        if(this.urlStr == null) return null;
        try {
            url = new URL(this.urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(getRequestStr(this.request));
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            if(this.request == Request.POST) {
                DataOutputStream requestWriter = new DataOutputStream(connection.getOutputStream());
                requestWriter.writeBytes(this.data);
                requestWriter.close();
            }

            String responseData;
            InputStream is = connection.getInputStream();
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(is));
            if ((responseData = responseReader.readLine()) != null) {
                response = responseData;
            }

            responseReader.close();
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
        return response;
    }
}