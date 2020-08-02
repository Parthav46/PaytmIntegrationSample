package io.github.parthav46.httphandler;

import android.app.Activity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequest {

    private Activity parent;
    private HttpResponseCallback callback;
    private String url;
    private String data;
    private String response;
    private Request method;

    public enum  Request {
        GET, POST
    }

    /**
     * Make http request with GET/POST methods
     * @param parent: Parent activity instance
     * @param url: http url
     * @param method: Request method
     * @param data: Request body data
     * @param callback: Callback method to be called when url request is completed
     */

    public HttpRequest (Activity parent, String url, Request method, String data, HttpResponseCallback callback) {
        this.parent = parent;
        this.data = data;
        this.url = url;
        this.callback = callback;
        this.method = method;
    }

    public void execute() {
        response = "";
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                response = makeRequest();
                parent.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResponse(response);
                    }
                });
            }
        });
        t.start();
    }

    private String makeRequest() {
        URL url;
        String response = null;


        if(this.url == null) return null;
        try {
            url = new URL(this.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(this.method.name());
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(this.method != Request.GET);

            if(this.method == Request.POST) {
                DataOutputStream requestWriter = new DataOutputStream(connection.getOutputStream());
                requestWriter.writeBytes(data);
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
